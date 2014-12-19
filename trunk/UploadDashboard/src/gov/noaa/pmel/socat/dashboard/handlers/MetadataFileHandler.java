/**
 * 
 */
package gov.noaa.pmel.socat.dashboard.handlers;

import gov.noaa.pmel.socat.dashboard.ome.OmeMetadata;
import gov.noaa.pmel.socat.dashboard.server.DashboardDataStore;
import gov.noaa.pmel.socat.dashboard.server.DashboardServerUtils;
import gov.noaa.pmel.socat.dashboard.shared.DashboardCruise;
import gov.noaa.pmel.socat.dashboard.shared.DashboardMetadata;
import gov.noaa.pmel.socat.dashboard.shared.DashboardUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.tmatesoft.svn.core.SVNException;

/**
 * Handles storage and retrieval of metadata files.
 *  
 * @author Karl Smith
 */
public class MetadataFileHandler extends VersionedFileHandler {

	private static final String METADATA_INFOFILE_SUFFIX = ".properties";
	private static final String UPLOAD_TIMESTAMP_ID = "uploadtimestamp";
	private static final String METADATA_OWNER_ID = "metadataowner";
	private static final String METADATA_CONFLICTED_ID = "metadataconflicted";

	/**
	 * Handles storage and retrieval of metadata files 
	 * under the given metadata files directory.
	 * 
	 * @param metadataFilesDirName
	 * 		name of the metadata files directory
	 * @param svnUsername
	 * 		username for SVN authentication
	 * @param svnPassword
	 * 		password for SVN authentication
	 * @throws IllegalArgumentException
	 * 		if the specified directory does not exist,
	 * 		is not a directory, or is not under SVN 
	 * 		version control
	 */
	public MetadataFileHandler(String metadataFilesDirName, 
							String svnUsername, String svnPassword) 
									throws IllegalArgumentException {
		super(metadataFilesDirName, svnUsername, svnPassword);
	}

	/**
	 * Generates the cruise-specific metadata file for a metadata document
	 * from the cruise expocode and the upload filename.
	 * 
	 * @param cruiseExpocode
	 * 		expocode of the cruise associated with this metadata document
	 * @param uploadName
	 * 		user's name of the uploaded metadata document 
	 * @return
	 * 		cruise-specific metadata file on the server
	 * @throws IllegalArgumentException
	 * 		if uploadName is null or ends in a slash or backslash, or 
	 * 		if the expocode is invalid
	 */
	public File getMetadataFile(String cruiseExpocode, String uploadName) 
											throws IllegalArgumentException {
		// Check and standardize the expocode
		String expocode = DashboardServerUtils.checkExpocode(cruiseExpocode);
		// Remove any path from uploadName
		String basename = DashboardUtils.baseName(uploadName);
		if ( basename.isEmpty() )
			throw new IllegalArgumentException(
					"Invalid metadate document name " + uploadName);
		// Generate the full path filename for this cruise metadata
		File metadataFile = new File(filesDir, expocode.substring(0,4) +
				File.separator + expocode + "_" + basename);
		return metadataFile;
	}

	/**
	 * Returns the list of metadata (including supplemental) documents associated
	 * with the given expocode.
	 * 
	 * @param cruiseExpocode
	 * 		get metadata documents for this expocode
	 * @return
	 * 		list of metadata documents; never null but may be empty
	 * @throws IllegalArgumentException
	 * 		if the expocode is invalid
	 */
	public ArrayList<DashboardMetadata> getMetadataFiles(String cruiseExpocode)
											throws IllegalArgumentException {
		ArrayList<DashboardMetadata> metadataList = new ArrayList<DashboardMetadata>();
		// Check and standardize the expocode
		final String expocode = DashboardServerUtils.checkExpocode(cruiseExpocode);
		// Get the parent directory for these metadata documents;
		// if it does not exist, return the empty list
		File parentDir = new File(filesDir, expocode.substring(0,4));
		if ( ! parentDir.isDirectory() )
			return metadataList;
		// Get all the metadata info files for this expocode 
		File[] metafiles = parentDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if ( name.startsWith(expocode + "_") && 
					 name.endsWith(METADATA_INFOFILE_SUFFIX) )
					return true;
				return false;
			}
		});
		// Add the metadata info for all the metadata files (may be an empty array)
		for ( File mfile : metafiles ) {
			String basename = mfile.getName().substring(expocode.length() + 1, 
					mfile.getName().length() - METADATA_INFOFILE_SUFFIX.length());
			try {
				DashboardMetadata mdata = getMetadataInfo(expocode, basename);
				if ( mdata != null )
					metadataList.add(mdata);
			} catch ( Exception ex ) {
				// Ignore this entry if there are problems
			}
		}
		return metadataList;
	}

	/**
	 * Validates that a user has permission to delete or overwrite
	 * and existing metadata document.
	 * 	
	 * @param username
	 * 		name of user wanting to delete or overwrite the metadata document
	 * @param expocode
	 * 		expocode of the cruise associated with this metadata document
	 * @param metaname
	 * 		name of the metadata document to be deleted or overwritten
	 * @throws IllegalArgumentException
	 * 		if expocode or metaname are invalid, or
	 * 		if the user is not permitted to overwrite the metadata document
	 */
	private void verifyOkayToDelete(String username, String expocode, 
							String metaname) throws IllegalArgumentException {
		// If the info file does not exist, okay to delete the metadata
		DashboardMetadata oldMetadata = getMetadataInfo(expocode, metaname);
		if ( oldMetadata == null )
			return;
		DashboardDataStore dataStore;
		try {
			dataStore = DashboardDataStore.get();
		} catch (IOException ex) {
			throw new IllegalArgumentException(
					"Unexpected error obtaining the dashboard configuration");
		}
		String oldOwner = oldMetadata.getOwner();
		if ( ! dataStore.userManagesOver(username, oldOwner) )
			throw new IllegalArgumentException(
					"Not permitted to update metadata document " + 
					oldMetadata.getFilename() + " for cruise " + 
					oldMetadata.getExpocode() + " owned by " + oldOwner);
	}

	/**
	 * Create or update a metadata document from the contents of a file upload.
	 * 
	 * @param cruiseExpocode
	 * 		expocode of the cruise associated with this metadata document.
	 * @param owner
	 * 		owner of this metadata document.
	 * @param uploadTimestamp
	 * 		timestamp giving the time of the upload.  This should be 
	 * 		generated on the client and sent to the server so it is 
	 * 		in local time for the user.
	 * @param uploadFilename
	 * 		upload filename to use for this metadata document; 
	 * 		may or may not match the basename of uploadFileItem.getName()
	 * @param uploadFileItem
	 * 		upload file item providing the metadata contents
	 * @return
	 * 		a DashboardMetadata describing the new or updated metadata 
	 * 		document; never null 
	 * @throws IllegalArgumentException
	 * 		if the expocode is invalid,
	 * 		if problems reading from the file upload stream,
	 * 		if problems writing to the new metadata document, or
	 * 		if problems committing the new metadata document to version control
	 */
	public DashboardMetadata saveMetadataFileItem(String cruiseExpocode, 
			String owner, String uploadTimestamp, String uploadFilename,
			FileItem uploadFileItem) throws IllegalArgumentException {
		// Create the metadata filename
		File metadataFile = getMetadataFile(cruiseExpocode, uploadFilename);

		// Make sure the parent directory exists 
		File parentDir = metadataFile.getParentFile();
		if ( ! parentDir.exists() ) {
			if ( ! parentDir.mkdirs() )
				throw new IllegalArgumentException(
						"Problems creating the parent directory for " + 
						metadataFile.getPath());
		}

		// Check if this will overwrite existing metadata
		boolean isUpdate;
		if ( metadataFile.exists() ) {
			verifyOkayToDelete(owner, cruiseExpocode, uploadFilename);
			isUpdate = true;
		}
		else {
			isUpdate = false;
		}

		// Copy the uploaded data to the metadata document
		try {
			uploadFileItem.write(metadataFile);
		} catch ( Exception ex ) {
			throw new IllegalArgumentException(
					"Problems creating/updating the metadata document " +
					metadataFile.getPath() + ":\n    " + ex.getMessage());
		}

		// Create the appropriate check-in message
		String message;
		if ( isUpdate ) {
			message = "Updated metadata document " + uploadFilename + 
					  " for cruise " + cruiseExpocode + " and owner " + owner;
		}
		else {
			message = "Added metadata document " + uploadFilename + 
					  " for cruise " + cruiseExpocode + " and owner " + owner;
		}

		// Commit the new/updated metadata document to version control
		try {
			commitVersion(metadataFile, message);
		} catch ( SVNException ex ) {
			throw new IllegalArgumentException("Problems committing " + 
					metadataFile.getPath() + " to version control:\n    " + 
					ex.getMessage());
		}

		// Create the DashboardMetadata to return
		DashboardMetadata metadata = new DashboardMetadata();
		metadata.setExpocode(cruiseExpocode);
		metadata.setFilename(uploadFilename);
		metadata.setUploadTimestamp(uploadTimestamp);
		metadata.setOwner(owner);

		// Save the metadata properties
		if ( isUpdate ) {
			message = "Updated properties of metadata document " + uploadFilename + 
					  " for cruise " + cruiseExpocode + " and owner " + owner;
		}
		else {
			message = "Added properties of metadata document " + uploadFilename + 
					  " for cruise " + cruiseExpocode + " and owner " + owner;
		}
		saveMetadataInfo(metadata, message);

		return metadata;
	}

	/**
	 * Copy a metadata document to another cruise.  The document,
	 * as well as the owner and upload timestamp properties, is 
	 * copied under appropriate names for the new cruise.
	 * 
	 * @param destCruiseExpo
	 * 		expocode of the cruise to be associated with the 
	 * 		copy of the metadata file
	 * @param srcMetadata
	 * 		metadata document to be copied
	 * @return
	 * 		a DashboardMetadata describing the new or updated metadata 
	 * 		document copied from the another cruise; never null
	 * @throws IllegalArgumentException
	 * 		if the expocode is invalid, 
	 * 		if the metadata document to be copied does not exist,
	 * 		if there were problems reading from the source metadata document, or 
	 * 		if there were problems writing to the destination metadata document.
	 */
	public DashboardMetadata copyMetadataFile(String destCruiseExpo,
			DashboardMetadata srcMetadata) throws IllegalArgumentException {
		String owner = srcMetadata.getOwner();
		String uploadName = srcMetadata.getFilename();
		// Get the source metadata document
		File srcFile = getMetadataFile(srcMetadata.getExpocode(), uploadName);
		return saveMetadataFile(destCruiseExpo, owner, uploadName, 
				srcMetadata.getUploadTimestamp(), srcFile);
	}

	/**
	 * Create or update a dashboard metadata document from the given file.
	 * 
	 * @param cruiseExpocode
	 * 		expocode of the cruise associated with this metadata document.
	 * @param owner
	 * 		owner of this metadata document.
	 * @param origName
	 * 		"original" or "upload" filename to use for this metadata document
	 * @param timestamp
	 * 		"upload" timestamp to assign for this metadata document
	 * @param srcFile
	 * 		metadata file to copy
	 * @return
	 * 		a DashboardMetadata describing the new or updated metadata 
	 * 		document; never null 
	 * @throws IllegalArgumentException
	 * 		if the expocode is invalid,
	 * 		if problems reading the given metadata file,
	 * 		if problems writing to the new metadata document, or
	 * 		if problems committing the new metadata document to version control
	 */
	public DashboardMetadata saveMetadataFile(String cruiseExpocode, 
			String owner, String origName, String timestamp, File srcFile) 
											throws IllegalArgumentException {
		if ( ! srcFile.exists() )
			throw new IllegalArgumentException("Source metadata file " + 
					srcFile.getPath() + " does not exist");
		// Get the destination metadata document 
		File destFile = getMetadataFile(cruiseExpocode, origName);
		File parentDir = destFile.getParentFile();
		if ( ! parentDir.exists() ) {
			if ( ! parentDir.mkdirs() )
				throw new IllegalArgumentException(
						"Problems creating the parent directory for " + 
						destFile.getPath());
		}

		// Check if this will overwrite existing metadata
		boolean isUpdate;
		if ( destFile.exists() ) {
			verifyOkayToDelete(owner, cruiseExpocode, origName);
			isUpdate = true;
		}
		else {
			isUpdate = false;
		}

		// Copy the metadata document
		try {
			FileInputStream src = null;
			FileOutputStream dest = null;
			try {
				src = new FileInputStream(srcFile);
				dest = new FileOutputStream(destFile);
				byte[] buff = new byte[4096];
				int numRead = src.read(buff);
				while ( numRead > 0 ) {
					dest.write(buff, 0, numRead);
					numRead = src.read(buff);
				}
			} finally {
				if ( dest != null )
					dest.close();
				if ( src != null )
					src.close();
			}
		} catch ( IOException ex ) {
			throw new IllegalArgumentException(
					"Problems copying the metadata document " + 
					srcFile.getPath() + " to " + destFile.getPath() + 
					":\n    " + ex.getMessage());
		}

		// Create the appropriate check-in message
		String message;
		if ( isUpdate ) {
			message = "Updated metadata document " + origName + 
					  " for cruise " + cruiseExpocode + " and owner " + owner;
		}
		else {
			message = "Added metadata document " + origName + 
					  " for cruise " + cruiseExpocode + " and owner " + owner;
		}

		// Commit the new/updated metadata document to version control
		try {
			commitVersion(destFile, message);
		} catch ( SVNException ex ) {
			throw new IllegalArgumentException("Problems committing " + 
					destFile.getPath() + " to version control:\n    " + 
					ex.getMessage());
		}
		
		// Create the DashboardMetadata to return
		DashboardMetadata metadata = new DashboardMetadata();
		metadata.setExpocode(cruiseExpocode);
		metadata.setFilename(origName);
		metadata.setUploadTimestamp(timestamp);
		metadata.setOwner(owner);

		// Create the appropriate check-in message
		if ( isUpdate ) {
			message = "Updated properties of metadata document " + origName + 
					  " for cruise " + cruiseExpocode + " and owner " + owner;
		}
		else {
			message = "Added properties of metadata document " + origName + 
					  " for cruise " + cruiseExpocode + " and owner " + owner;
		}

		// Save the metadata properties
		saveMetadataInfo(metadata, message);

		return metadata;
	}

	/**
	 * Generates a DashboardMetadata initialized with the contents of
	 * the information (properties) file for the metadata.  It will not 
	 * be "selected".
	 * 
	 * @param expocode
	 * 		expocode of the cruise associated with this metadata
	 * @param metaname
	 * 		name of the metadata document
	 * @return
	 * 		DashboardMetadata assigned from the properties file for the 
	 * 		given metadata document.  If the properties file does not 
	 * 		exist, null is returned.
	 * @throws IllegalArgumentException
	 * 		if expocode or metaname is invalid, or
	 * 		if there were problems reading from the properties file
	 */
	public DashboardMetadata getMetadataInfo(String expocode, String metaname) 
											throws IllegalArgumentException {
		// Get the full path filename of the metadata file
		File metadataFile = getMetadataFile(expocode, metaname);
		// Read the properties associated with this metadata document
		Properties metaProps = new Properties();
		try {
			FileReader propsReader = new FileReader(
					new File(metadataFile.getPath() + METADATA_INFOFILE_SUFFIX));
			try {
				metaProps.load(propsReader);
			} finally {
				propsReader.close();
			}
		} catch ( FileNotFoundException ex ) {
			return null;
		} catch ( IOException ex ) {
			throw new IllegalArgumentException(ex);
		}

		// Create and assign the DashboardMetadata object to return
		DashboardMetadata metadata = new DashboardMetadata();
		// Cruise expocode
		metadata.setExpocode(expocode);
		// Metadata document name
		metadata.setFilename(metaname);
		// Upload timestamp
		String value = metaProps.getProperty(UPLOAD_TIMESTAMP_ID);
		metadata.setUploadTimestamp(value);
		// Owner
		value = metaProps.getProperty(METADATA_OWNER_ID);
		metadata.setOwner(value);
		// Conflicted flag
		value = metaProps.getProperty(METADATA_CONFLICTED_ID);
		metadata.setConflicted(Boolean.valueOf(value));

		return metadata;
	}

	/**
	 * Saves the properties for a metadata document to the appropriate
	 * metadata properties file.  A new properties file is saved and
	 * committed, even if there are no changes from what is currently 
	 * saved. 
	 * 
	 * @param metadata
	 * 		metadata to save
	 * @param message
	 * 		version control commit message; if null, the commit is not
	 * 		performed
	 * @throws IllegalArgumentException
	 * 		if there were problems saving the properties to file, or
	 * 		if there were problems committing the properties file 
	 */
	public void saveMetadataInfo(DashboardMetadata metadata, String message) 
											throws IllegalArgumentException {
		// Get full path name of the properties file
		File metadataFile = getMetadataFile(metadata.getExpocode(), 
											metadata.getFilename());
		File propsFile = new File(metadataFile.getPath() + METADATA_INFOFILE_SUFFIX);
		// Make sure the parent subdirectory exists
		File parentDir = propsFile.getParentFile();
		if ( ! parentDir.exists() )
			parentDir.mkdirs();
		// Create the properties for this metadata properties file
		Properties metaProps = new Properties();
		// Upload timestamp
		metaProps.setProperty(UPLOAD_TIMESTAMP_ID, metadata.getUploadTimestamp());
		// Owner 
		metaProps.setProperty(METADATA_OWNER_ID, metadata.getOwner());
		// Conflicted flag
		metaProps.setProperty(METADATA_CONFLICTED_ID, Boolean.toString(metadata.isConflicted()));
		// Save the properties to the metadata properties file
		try {
			FileWriter propsWriter = new FileWriter(propsFile);
			try {
				metaProps.store(propsWriter, null);
			} finally {
				propsWriter.close();
			}
		} catch ( Exception ex ) {
			throw new IllegalArgumentException(
					"Problems writing metadata information for " + 
					metadata.getFilename() + " to " + propsFile.getPath() + 
					":\n    " + ex.getMessage());
		}
		
		if ( (message == null) || message.trim().isEmpty() )
			return;

		// Submit the updated information file to version control
		try {
			commitVersion(propsFile, message);
		} catch ( Exception ex ) {
			throw new IllegalArgumentException(
					"Problems committing updated metadata information for  " + 
					metadata.getFilename() + ":\n    " + ex.getMessage());
		}
	}

	/**
	 * Appropriately renames any cruise metadata documents and info files 
	 * for a change in cruise expocode.  Renames the expocode in the OME 
	 * metadata file if it exists.
	 * 
	 * @param oldExpocode
	 * 		standardized old expocode of the cruise
	 * @param newExpocode
	 * 		standardized new expocode for the cruise
	 * @throws IllegalArgumentException
	 * 		if a metadata or info file for the new expocode already exists, 
	 * 		if the OME metadata exists but is invalid, or
	 * 		if unable to rename a metadata or info file
	 */
	public void renameMetadataFiles(String oldExpocode, String newExpocode) 
											throws IllegalArgumentException {
		// Rename all the metadata documents associated with the old expocode
		OmeMetadata omeMData = null;
		for ( DashboardMetadata metaDoc : getMetadataFiles(oldExpocode) ) {
			String uploadFilename = metaDoc.getFilename();

			// If this is the OME metadata file, read the contents 
			if ( OmeMetadata.OME_FILENAME.equals(uploadFilename) ) {
				omeMData = new OmeMetadata(metaDoc);
			}

			File oldMetaFile = getMetadataFile(oldExpocode, uploadFilename);
			if ( ! oldMetaFile.exists() )
				throw new RuntimeException("Unexpected failure: metadata file " + 
						oldMetaFile.getName() + " does not exist");

			File oldMetaInfoFile = new File(oldMetaFile.getPath() + METADATA_INFOFILE_SUFFIX);
			if ( ! oldMetaInfoFile.exists() )
				throw new RuntimeException("Unexpected failure: metadata info file " + 
						oldMetaInfoFile.getName() + " does not exist");

			File newMetaFile = getMetadataFile(newExpocode, uploadFilename);
			if ( newMetaFile.exists() )
				throw new IllegalArgumentException("Metadata file " + 
						uploadFilename + " already exists for " + newExpocode);

			File newMetaInfoFile = new File(newMetaFile.getPath() + METADATA_INFOFILE_SUFFIX);
			if ( newMetaInfoFile.exists() )
			throw new IllegalArgumentException("Metadata info file for " + 
					uploadFilename + " already exists for " + newExpocode);

			// Make sure the parent directory exists for the new file
			File parent = newMetaFile.getParentFile();
			if ( ! parent.exists() )
				parent.mkdirs();

			String commitMsg = "Move metadata document " + uploadFilename + 
					" from " + oldExpocode + " to " + newExpocode;
			try {
				moveVersionedFile(oldMetaFile, newMetaFile, commitMsg);
				moveVersionedFile(oldMetaInfoFile, newMetaInfoFile, commitMsg);
			} catch (SVNException ex) {
				throw new IllegalArgumentException(ex);
			}
		}

		if ( omeMData != null ) {
			omeMData.changeExpocode(newExpocode);
			saveAsOmeXmlDoc(omeMData, "Change expocode from " + 
					oldExpocode + " to " + newExpocode);
		}
	}

	/**
	 * Removes (deletes) a metadata document and its properties
	 * file, committing the change to version control.
	 * 
	 * @param username
	 * 		name of the user wanting to remove the metadata document
	 * @param expocode
	 * 		expocode of the cruise associated with this metadata
	 * @param metaname
	 * 		name of the metadata document
	 * @throws IllegalArgumentException 
	 * 		if expocode or metaname is invalid, 
	 * 		if the user is not permitted to delete the metadata document,
	 * 		if there are problems deleting the document.
	 */
	public void removeMetadata(String username, String expocode,
			String metaname) throws IllegalArgumentException {
		File metadataFile = getMetadataFile(expocode, metaname);
		File propsFile = new File(metadataFile.getPath() + METADATA_INFOFILE_SUFFIX);
		// Do not throw an error if the props file does not exist
		if ( propsFile.exists() ) { 
			// Throw an exception if not allowed to overwrite
			verifyOkayToDelete(username, expocode, metaname);
			try {
				deleteVersionedFile(propsFile, 
						"Deleted metadata properties " + propsFile.getPath());
			} catch ( Exception ex ) {
				throw new IllegalArgumentException(
						"Unable to delete metadata properties file " + propsFile.getPath());
			}
		}
		// Do not throw an error if the metadata file does not exist.
		// If the props file does not exist, assume it is okay to delete the metadata file.
		if ( metadataFile.exists() ) { 
			try {
				deleteVersionedFile(metadataFile, 
						"Deleted metadata document " + metadataFile.getPath());
			} catch ( Exception ex ) {
				throw new IllegalArgumentException(
						"Unable to delete metadata file " + metadataFile.getPath());
			}
		}
	}

	/**
	 * Save the OME XML document created by {@link #createOmeXmlDoc()} 
	 * from the given OmeDocument as the document file for this metadata.  
	 * The parent directory for this file is expected to exist and 
	 * this method will overwrite any existing OME metadata file.
	 * 
	 * @param mdata
	 * 		OME metadata to save as an OME XML document
	 * @param message
	 * 		version control commit message; if null, the commit is not
	 * 		performed
	 * @throws IllegalArgumentException
	 * 		if the expocode or uploadFilename in this object is invalid, or
	 * 		writing the metadata document file generates one.
	 */
	public void saveAsOmeXmlDoc(OmeMetadata mdata, String message) 
											throws IllegalArgumentException {
		File mdataFile = getMetadataFile(mdata.getExpocode(), mdata.getFilename());

		// Generate the pseudo-OME XML document
		Document omeDoc = mdata.createOmeXmlDoc();

		// Save the XML document to the metadata document file
		try {
			FileOutputStream out = new FileOutputStream(mdataFile);
			try {
				(new XMLOutputter(Format.getPrettyFormat())).output(omeDoc, out);
			} finally {
				out.close();
			}
		} catch (IOException ex) {
			throw new IllegalArgumentException(
					"Problems writing the OME metadata document: " +
					ex.getMessage());
		}

		if ( (message == null) || message.trim().isEmpty() )
			return;

		// Submit the updated information file to version control
		try {
			commitVersion(mdataFile, message);
		} catch ( Exception ex ) {
			throw new IllegalArgumentException(
					"Problems committing updated pseudo-OME metadata information " + 
					mdataFile.getPath() + ":\n    " + ex.getMessage());
		}
	}

	/**
	 * Downloads the metadata files at given links and associates with the corresponding cruises.  
	 * Expocodes without links or NULL for the link are ignored.  Expocodes that have been 
	 * updated in the current version of SOCAT are ignored.  Metadata files that already exist
	 * are not overwritten.
	 * 
	 * @param args
	 * 		ExpocodeLinksFile
	 * 			List of expocodes and metadata file links; multiple links separated by space-semicolon-space
	 * 		RenamedExpocodesFile
	 * 			List of renamed expocodes in the form: Rename from (old_expocode) to (new_expocode)
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if ( args.length != 5 ) {
			System.err.println();
			System.err.println("arguments:  ExpocodeLinksFile  RenamedExpocodesFile  MetadataFilesDir  CruiseDataFilesDir  SVNUsername");
			System.err.println("where:");
			System.err.println("    ExpocodesLinksFile - list of expocode, tab, and metadata ");
			System.err.println("        file links; multiple links separated by space-semicolon-space");
			System.err.println("    RenamedExpocodesdFile - list of renamed expocodes in the form: ");
			System.err.println("        \"Rename from <old_expocode> to <new_expocode>\"");
			System.err.println("    MetadataFilesDir - version-controlled metadata documents directory");
			System.err.println("    CruiseDataFilesDir - version-controlled cruise data documents directory");
			System.err.println("    SVNUsername - username for version control");
			System.err.println();
			System.err.println("Downloads the metadata files at given links and associates with the ");
			System.err.println("corresponding cruises.  Expocodes without links or NULL for the link ");
			System.err.println("are ignored.  Expocodes that have been updated in version \"3.0\" ");
			System.err.println("of SOCAT are ignored.  Metadata files that already exist are not overwritten.");
			System.err.println();
			System.exit(1);
		}

		String expoLinksFilename = args[0];
		String renamesFilename = args[1];
		String metadataDirname = args[2];
		String cruiseDataDirname = args[3];
		String svnUsername = args[4];
		String svnPassword = "";

		// Read the expocodes and their links
		BufferedReader buffReader = null;
		try {
			buffReader = new BufferedReader(new FileReader(expoLinksFilename));
		} catch (Exception ex) {
			System.err.println("Problems opening the expocode and links file for reading:");
			ex.printStackTrace();
			System.exit(1);
		}

		// Map of link to set of expocode using that link
		TreeMap<String,TreeSet<String>> linkExposMap = new TreeMap<String,TreeSet<String>>();
		try {
			String dataline = buffReader.readLine();
			while ( dataline != null ) {
				String[] expoLinks = dataline.split("\t");
				if ( expoLinks.length != 2 ) {
					System.err.println("No tab found in expo-link line: '" + dataline + "'");
					System.exit(1);
				}
				for ( String link : expoLinks[1].split(" ; ") ) {
					link = link.trim();
					if ( link.isEmpty() || link.equalsIgnoreCase("NULL") )
						continue;
					TreeSet<String> expos = linkExposMap.get(link);
					if ( expos == null )
						expos = new TreeSet<String>();
					String expocode = null;
					try {
						expocode = DashboardServerUtils.checkExpocode(expoLinks[0]);
					} catch (Exception ex) {
						System.err.println("Invalid expocode given in links line: '" + dataline + "'");
						ex.printStackTrace();
						System.exit(1);
					}
					expos.add(expocode);
					linkExposMap.put(link, expos);
				}
				dataline = buffReader.readLine();
			}
		} finally {
			buffReader.close();
		}

		// Read the expocode renames
		try {
			buffReader = new BufferedReader(new FileReader(renamesFilename));
		} catch (Exception ex) {
			System.err.println("Problems opening the renames file for reading:");
			ex.printStackTrace();
			System.exit(1);
		}

		// Map of old to new expocodes
		Pattern patt = Pattern.compile("Rename from (\\S+) to (\\S+)");
		TreeMap<String,String> renames = new TreeMap<String,String>();
		try {
			String dataline = buffReader.readLine();
			while ( dataline != null ) {
				Matcher mat = patt.matcher(dataline.trim());
				if ( ! mat.matches() ) {
					System.err.println("Unexpected rename line: '" + dataline + "'");
					System.exit(1);
				}
				String fromExpo = null;
				try {
					fromExpo = DashboardServerUtils.checkExpocode(mat.group(1));
				} catch (Exception ex) {
					System.err.println("Invalid 'from' expocode in '" + dataline + "'");
					ex.printStackTrace();
					System.exit(1);
				}
				String toExpo = null;
				try {
					toExpo = DashboardServerUtils.checkExpocode(mat.group(2));
				} catch (Exception ex) {
					System.err.println("Invalid 'to' expocode in '" + dataline + "'");
					ex.printStackTrace();
					System.exit(1);
				}
				renames.put(fromExpo, toExpo);
				dataline = buffReader.readLine();
			}
		} finally {
			buffReader.close();
		}

		CruiseFileHandler cruiseHandler = null;
		try {
			cruiseHandler = new CruiseFileHandler(cruiseDataDirname, svnUsername, svnPassword);
		} catch (Exception ex) {
			System.err.println("Problems with the cruise data documents directory");
			ex.printStackTrace();
			System.exit(1);
		}
		try {
			// Correct the list of expocodes by renaming and removing updated cruises
			TreeMap<String,TreeSet<String>> linkNewExposMap = new TreeMap<String,TreeSet<String>>();
			for ( String link : linkExposMap.keySet() ) {
				TreeSet<String> newExpoSet = new TreeSet<String>();
				for ( String expo : linkExposMap.get(link) ) {
					String newExpo = renames.get(expo);
					if ( newExpo == null )
						newExpo = expo;
					try {
						DashboardCruise cruise = cruiseHandler.getCruiseFromInfoFile(newExpo);
						if ( cruise == null )
							throw new IllegalArgumentException("info file does not exist");
						if ( ! cruise.getVersion().equals("3.0") ) {
							newExpoSet.add(newExpo);
						}
					} catch (Exception ex) {
						System.err.println("Problems reading the info file for cruise " + newExpo);
						ex.printStackTrace();
						System.exit(1);
					}
				}
				if ( newExpoSet.size() > 0 ) {
					linkNewExposMap.put(link, newExpoSet);
				}
			}
		} finally {
			cruiseHandler.shutdown();
		}

		boolean success = true;

		MetadataFileHandler metaHandler = null;
		try {
			metaHandler = new MetadataFileHandler(metadataDirname, svnUsername, svnPassword);
		} catch (Exception ex) {
			System.err.println("Problems with the metadata documents directory");
			ex.printStackTrace();
			System.exit(1);
		}
		try {
			// TODO:
		} finally {
			metaHandler.shutdown();
		}

		if ( ! success )
			System.exit(1);
		System.exit(0);
	}

}

/**
 * 
 */
package gov.noaa.pmel.socat.dashboard.server;

import gov.noaa.pmel.socat.dashboard.shared.DashboardCruiseWithData;
import gov.noaa.pmel.socat.dashboard.shared.DashboardUtils;
import gov.noaa.pmel.socat.dashboard.shared.DataColumnType;
import gov.noaa.pmel.socat.dashboard.shared.DataLocation;
import gov.noaa.pmel.socat.dashboard.shared.SCMessage;
import gov.noaa.pmel.socat.dashboard.shared.SCMessage.SCMsgSeverity;
import gov.noaa.pmel.socat.dashboard.shared.SCMessage.SCMsgType;
import gov.noaa.pmel.socat.dashboard.shared.SCMessageList;
import gov.noaa.pmel.socat.dashboard.shared.SocatWoceEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import uk.ac.uea.socat.sanitychecker.Message;
import uk.ac.uea.socat.sanitychecker.Output;
import uk.ac.uea.socat.sanitychecker.data.SocatDataRecord;

/**
 * Processes SanityChecker messages for a cruise.
 * 
 * @author Karl Smith
 */
public class CheckerMessageHandler {

	private static final String CRUISE_MSGS_FILENAME_EXTENSION = ".messages";
	private static final String SCMSG_KEY_VALUE_SEP = ":";
	private static final String SCMSG_TYPE_KEY = "SCMsgType";
	private static final String SCMSG_SEVERITY_KEY = "SCMsgSeverity";
	private static final String SCMSG_ROW_NUMBER_KEY = "SCMsgRowNumber";
	private static final String SCMSG_LONGITUDE_KEY = "SCMsgLongitude";
	private static final String SCMSG_LATITUDE_KEY = "SCMsgLatitude";
	private static final String SCMSG_TIMESTAMP_KEY = "SCMsgTimestamp";
	private static final String SCMSG_COLUMN_NUMBER_KEY = "SCMsgColumnNumber";
	private static final String SCMSG_COLUMN_NAME_KEY = "SCMsgColumnName";
	private static final String SCMSG_MESSAGE_KEY = "SCMsgMessage";
	private static final DateTimeFormatter DATETIME_FORMATTER = 
			DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss");

	private static final Set<Entry<String,SCMsgType>> MSG_FRAGMENT_TO_TYPE_SET;
	static {
		HashMap<String,SCMsgType> msgFragsToTypes = new HashMap<String,SCMsgType>();
		// value outside range (warning)
		msgFragsToTypes.put("outside the expected range", SCMsgType.DATA_QUESTIONABLE_VALUE);
		// value outside range (error)
		msgFragsToTypes.put("outside the extreme range", SCMsgType.DATA_BAD_VALUE);
		// ship speed is excessive - either warning or error
		msgFragsToTypes.put("Ship speed between measurements", SCMsgType.DATA_QUESTIONABLE_SPEED);
		// data point times out of order
		msgFragsToTypes.put("The timestamp is either before or identical", SCMsgType.DATA_TIME);
		// missing value for required data value
		msgFragsToTypes.put("Missing required value", SCMsgType.DATA_MISSING);
		// values constant over some number of data points
		msgFragsToTypes.put("constant for", SCMsgType.DATA_CONSTANT);
		// data value markedly different from values in previous and subsequent data points
		msgFragsToTypes.put("standard deviations", SCMsgType.DATA_JUMP);
		// excessive time gap between successive data points
		msgFragsToTypes.put("days apart", SCMsgType.DATA_GAP);
		// metadata given in a data column is not constant
		msgFragsToTypes.put("Value for column has changed", SCMsgType.DATA_METADATA_NOT_SAME);
		// unexpected exception not handled
		msgFragsToTypes.put("Unhandled exception", SCMsgType.DATA_ERROR);
		MSG_FRAGMENT_TO_TYPE_SET = msgFragsToTypes.entrySet();
	}

	private File filesDir;

	/**
	 * Handler for SanityChecker messages, including categorizing and making WOCE flags
	 * and events from them.
	 * 
	 * @param filesDirName
	 * 		save SanityChecker messages under this directory
	 */
	CheckerMessageHandler(String filesDirName) {
		filesDir = new File(filesDirName);
		if ( ! filesDir.isDirectory() )
			throw new IllegalArgumentException(filesDirName + " is not a directory");
	}

	/**
	 * 
	 * @param expocode
	 * 		expocode of the cruise
	 * @return
	 * 		the cruise messages file associated with the cruise
	 * @throws IllegalArgumentException
	 * 		if the cruise expocode is invalid
	 */
	private File cruiseMsgsFile(String expocode) throws IllegalArgumentException {
		// Check that the expocode is somewhat reasonable
		String upperExpo = DashboardServerUtils.checkExpocode(expocode);
		// Get the name of the cruise messages file
		return new File(filesDir, upperExpo.substring(0,4) + 
				File.separatorChar + upperExpo + CRUISE_MSGS_FILENAME_EXTENSION);
	}

	/**
	 * Deletes the sanity checker messages file, if it exists, associated with a cruise.
	 * 
	 * @param expocode
	 * 		delete the messages file associated with the cruise with this expocode
	 * @return
	 * 		true if messages file exists and was deleted; 
	 * 		false if the messages file does not exist.
	 * @throws IllegalArgumentException
	 * 		if the expocode is invalid, or
	 * 		if the messages file exists but could not be deleted
	 */
	public boolean deleteMsgsFile(String expocode) throws IllegalArgumentException {
		File msgsFile = cruiseMsgsFile(expocode);
		if ( ! msgsFile.exists() )
			return false;
		if ( ! msgsFile.delete() ) {
			throw new IllegalArgumentException("Unable to delete " +
					"the sanity checker messages file for " + expocode);
		}
		return true;
	}

	/**
	 * Saves the list of messages produced by the SanityChecker to file.
	 * Clears and assigns the WOCE-3 or WOCE-4 flags for the given cruise from 
	 * the given SanityChecker output for the cruise as well as any user-provided 
	 * WOCE flags in the cruise data.  A row index may appear in multiple WOCE 
	 * sets, including both WOCE-3 and WOCE-4 sets.
	 * 
	 * @param cruiseData
	 * 		save messages for this cruise, and assign WOCE flags to this cruise
	 * @param output
	 * 		SanityChecker output for this cruise  
	 * @throws IllegalArgumentException
	 * 		if the expocode is invalid
	 */
	public void saveCruiseMessages(DashboardCruiseWithData cruiseData, Output output) 
											throws IllegalArgumentException {
		// Directly modify the sets in the cruise data
		ArrayList<HashSet<Integer>> woceFourSets = cruiseData.getWoceFourRowIndices();
		ArrayList<HashSet<Integer>> woceThreeSets = cruiseData.getWoceThreeRowIndices();
		HashSet<Integer> noColumnWoceFourSet = cruiseData.getNoColumnWoceFourRowIndices();
		HashSet<Integer> noColumnWoceThreeSet = cruiseData.getNoColumnWoceThreeRowIndices();
		HashSet<Integer> userWoceFourSet = cruiseData.getUserWoceFourRowIndices();
		HashSet<Integer> userWoceThreeSet = cruiseData.getUserWoceThreeRowIndices();

		// Clear all WOCE flag sets
		for ( HashSet<Integer> rowIdxSet : woceFourSets )
			rowIdxSet.clear();
		for ( HashSet<Integer> rowIdxSet : woceThreeSets )
			rowIdxSet.clear();
		noColumnWoceFourSet.clear();
		noColumnWoceThreeSet.clear();
		userWoceFourSet.clear();
		userWoceThreeSet.clear();

		// Get the cruise messages file to be written
		File msgsFile = cruiseMsgsFile(cruiseData.getExpocode());

		// Create the NODC subdirectory if it does not exist
		File parentFile = msgsFile.getParentFile();
		if ( ! parentFile.exists() )
			parentFile.mkdirs();

		// Write the messages to file and save WOCE flags from these messages
		PrintWriter msgsWriter;
		try {
			msgsWriter = new PrintWriter(msgsFile);
		} catch (FileNotFoundException ex) {
			throw new IllegalArgumentException(
					"Unexpected error opening messages file " + 
					msgsFile.getPath() + "\n    " + ex.getMessage(), ex);
		}
		try {

			List<SocatDataRecord> dataRecs = output.getRecords();
			int numRecs = dataRecs.size();
			for ( Message msg : output.getMessages().getMessages() ) {
				// Generate a list of key-value strings describing this message
				ArrayList<String> mappings = new ArrayList<String>();

				// Message string should never be null
				String checkerMsg = msg.getMessage();

				int severityInt = msg.getSeverity();
				if ( severityInt == Message.ERROR ) {
					mappings.add(SCMSG_SEVERITY_KEY + SCMSG_KEY_VALUE_SEP + 
							SCMsgSeverity.ERROR.name());
				}
				else if ( severityInt == Message.WARNING ) {
					mappings.add(SCMSG_SEVERITY_KEY + SCMSG_KEY_VALUE_SEP + 
							SCMsgSeverity.WARNING.name());
				}

				int msgTypeInt = msg.getMessageType();
				if ( msgTypeInt == Message.DATA_MESSAGE ) {
					SCMsgType msgType = SCMsgType.UNKNOWN;
					// Determine the error/warning type from the message itself
					for ( Entry<String, SCMsgType> fragEntry : MSG_FRAGMENT_TO_TYPE_SET ) {
						if ( checkerMsg.contains(fragEntry.getKey()) ) {
							if ( msgType != SCMsgType.UNKNOWN )
								throw new IllegalArgumentException("More than one message type (" + 
										msgType.toString() + " and " + fragEntry.getValue().toString() + 
										") associated with the data check message\n    " + checkerMsg);
							msgType = fragEntry.getValue();
						}
					}
					if ( msgType.equals(SCMsgType.UNKNOWN) )
						throw new IllegalArgumentException("No message type found " +
								"for the data check message\n    " + checkerMsg);
					// Resolve whether speed speed was a warning or an error
					if ( msgType.equals(SCMsgType.DATA_QUESTIONABLE_SPEED) && 
						 (severityInt == Message.ERROR) )
						msgType = SCMsgType.DATA_BAD_SPEED;
					mappings.add(SCMSG_TYPE_KEY + SCMSG_KEY_VALUE_SEP + 
							msgType.name());
				}
				else if ( msgTypeInt == Message.METADATA_MESSAGE ) {
					mappings.add(SCMSG_TYPE_KEY + SCMSG_KEY_VALUE_SEP + 
							SCMsgType.METADATA.name());
				}

				int rowNum = msg.getLineNumber();
				if ( (rowNum > 0) && (rowNum <= numRecs) ) {
					mappings.add(SCMSG_ROW_NUMBER_KEY + SCMSG_KEY_VALUE_SEP + 
							Integer.toString(rowNum));

					SocatDataRecord stdData = dataRecs.get(rowNum - 1);
					try {
						double longitude = stdData.getLongitude();
						if ( ! Double.isNaN(longitude) )
							mappings.add(SCMSG_LONGITUDE_KEY + SCMSG_KEY_VALUE_SEP + 
									Double.toString(longitude));
					} catch ( Exception ex ) {
						// no entry
					}
					try {
						double latitude = stdData.getLatitude();
						if ( ! Double.isNaN(latitude) )
							mappings.add(SCMSG_LATITUDE_KEY + SCMSG_KEY_VALUE_SEP + 
									Double.toString(latitude));
					} catch ( Exception ex ) {
						// no entry
					}
					try {
						DateTime timestamp = stdData.getTime();
						if ( timestamp != null )
							mappings.add(SCMSG_TIMESTAMP_KEY + SCMSG_KEY_VALUE_SEP +
									DATETIME_FORMATTER.print(timestamp));
					} catch ( Exception ex ) {
						// no entry
					}
				}

				int colNum = msg.getInputItemIndex();
				if ( colNum > 0 )
					mappings.add(SCMSG_COLUMN_NUMBER_KEY + SCMSG_KEY_VALUE_SEP + 
							Integer.toString(colNum));

				String colName = msg.getInputItemName();
				if ( colName != null )
					mappings.add(SCMSG_COLUMN_NAME_KEY + SCMSG_KEY_VALUE_SEP + colName);

				// Escape all newlines in the message string when saving it
				mappings.add(SCMSG_MESSAGE_KEY + SCMSG_KEY_VALUE_SEP + checkerMsg.replace("\n",  "\\n"));

				// Write this array list of key-value strings to file
				msgsWriter.println(DashboardUtils.encodeStringArrayList(mappings));

				// Assign the WOCE flag
				if ( (msgTypeInt == Message.DATA_MESSAGE) && (rowNum > 0) ) {
					if ( severityInt == Message.ERROR ) {
						if ( colNum > 0 ) {
							woceFourSets.get(colNum - 1).add(rowNum - 1);
						}
						else {
							noColumnWoceFourSet.add(rowNum - 1);
						}
					}
					else if ( severityInt == Message.WARNING ) {
						if ( colNum > 0 ) {
							woceThreeSets.get(colNum - 1).add(rowNum - 1);
						}
						else {
							noColumnWoceThreeSet.add(rowNum - 1);
						}
					}
				}
			}

		} finally {
			msgsWriter.close();
		}

		// Assign any user-provided WOCE-3 and WOCE-4 flags
		ArrayList<DataColumnType> columnTypes = cruiseData.getDataColTypes();
		for (int k = 0; k < columnTypes.size(); k++) {
			DataColumnType colType = columnTypes.get(k);
			if ( ! ( colType.equals(DataColumnType.WOCE_CO2_WATER) ||
					 colType.equals(DataColumnType.WOCE_CO2_ATM) ) )
				continue;
			for (int rowIdx = 0; rowIdx < cruiseData.getNumDataRows(); rowIdx++) {
				try {
					int value = Integer.parseInt(cruiseData.getDataValues().get(rowIdx).get(k));
					if ( value == 4 )
						userWoceFourSet.add(rowIdx);
					else if ( value == 3 )
						userWoceThreeSet.add(rowIdx);
					// Only handle 3 and 4
				} catch (NumberFormatException ex) {
					// Assuming a missing value
				}
			}
		}

	}

	/**
	 * Reads the list of messages produced by the SanityChecker from the messages 
	 * file written by {@link #saveCruiseMessages(DashboardCruiseWithData, Output)}.
	 * 
	 * @param expocode
	 * 		get messages for the cruise with this expocode
	 * @return
	 * 		the sanity checker messages for the cruise;
	 * 		never null, but may be empty if there were no sanity
	 * 		checker messages for the cruise.
	 * 		The expocode, but not the username, will be assigned 
	 * 		in the returned SCMessageList
	 * @throws IllegalArgumentException
	 * 		if the expocode is invalid, or 
	 * 		if the messages file is invalid
	 * @throws FileNotFoundException
	 * 		if there is no messages file for the cruise
	 */
	public SCMessageList getCruiseMessages(String expocode) 
					throws IllegalArgumentException, FileNotFoundException {
		// Create the list of messages to be returned
		SCMessageList msgList = new SCMessageList();
		msgList.setExpocode(expocode);
		// Read the cruise messages file
		File msgsFile = cruiseMsgsFile(expocode);
		BufferedReader msgReader;
		msgReader = new BufferedReader(new FileReader(msgsFile));
		try {
			try {
				String msgline = msgReader.readLine();
				while ( msgline != null ) {
					if ( ! msgline.trim().isEmpty() ) {

						Properties msgProps = new Properties();
						for ( String msgPart : DashboardUtils.decodeStringArrayList(msgline) ) {
							String[] keyValue = msgPart.split(SCMSG_KEY_VALUE_SEP, 2);
							if ( keyValue.length != 2 )
								throw new IOException("Invalid key:value pair '" + msgPart + "'");
							msgProps.setProperty(keyValue[0], keyValue[1]);
						}

						SCMessage msg = new SCMessage();

						String propVal = msgProps.getProperty(SCMSG_TYPE_KEY);
						try {
							msg.setType(SCMsgType.valueOf(propVal));
						} catch ( Exception ex ) {
							// leave as the default SCMsgType.UNKNOWN
						}

						propVal = msgProps.getProperty(SCMSG_SEVERITY_KEY);
						try {
							msg.setSeverity(SCMsgSeverity.valueOf(propVal));
						} catch ( Exception ex ) {
							// leave as the default SCMsgSeverity.UNKNOWN
						}

						propVal = msgProps.getProperty(SCMSG_ROW_NUMBER_KEY);
						try {
							msg.setRowNumber(Integer.parseInt(propVal));
						} catch ( Exception ex ) {
							// leave as the default -1
						}

						propVal = msgProps.getProperty(SCMSG_LONGITUDE_KEY);
						try {
							msg.setLongitude(Double.valueOf(propVal));
						} catch ( Exception ex ) {
							// leave as the default Double.NaN
						}

						propVal = msgProps.getProperty(SCMSG_LATITUDE_KEY);
						try {
							msg.setLatitude(Double.valueOf(propVal));
						} catch ( Exception ex ) {
							// leave as the default Double.NaN
						}

						propVal = msgProps.getProperty(SCMSG_TIMESTAMP_KEY);
						if ( propVal != null ) {
							msg.setTimestamp(propVal);
						}
						// default timestamp is an empty string

						propVal = msgProps.getProperty(SCMSG_COLUMN_NUMBER_KEY);
						try {
							msg.setColNumber(Integer.parseInt(propVal));
						} catch ( Exception ex ) {
							// leave as the default -1
						}

						propVal = msgProps.getProperty(SCMSG_COLUMN_NAME_KEY);
						if ( propVal != null ) {
							msg.setColName(propVal);
						}
						// default column name is an empty string 

						propVal = msgProps.getProperty(SCMSG_MESSAGE_KEY);
						if ( propVal != null ) {
							// Replace all escaped newlines in the message string
							propVal = propVal.replace("\\n", "\n");
							msg.setExplanation(propVal);
						}
						// default explanation is an empty string

						msgList.add(msg);

						msgline = msgReader.readLine();
					}
				}
			} finally {
				msgReader.close();
			}
		} catch (IOException ex) {
			throw new IllegalArgumentException(
					"Unexpected problem reading messages from " + msgsFile.getPath() +
					"\n    " + ex.getMessage(), ex);
		}

		return msgList;
	}

	/**
	 * Generates a list of SocatWoceEvents to to be submitted from the saved cruise
	 * messages as well as PI-provided WOCE flags.
	 * 
	 * @param cruiseData
	 * 		get the list of SocatWoceEvents for this cruise
	 * @return
	 * 		the list of SocatWoceEvents for the cruise
	 * @throws IllegalArgumentException
	 * 		if the expocode in cruiseData is invalid, or 
	 * 		if the messages file is invalid
	 * @throws FileNotFoundException
	 * 		if there is no messages file for the cruise
	 */
	public ArrayList<SocatWoceEvent> generateWoceEvents(DashboardCruiseWithData cruiseData) 
						throws IllegalArgumentException, FileNotFoundException {
		// Get the SanityChecker messages and sort for assigning WOCE flags
		String expocode = cruiseData.getExpocode();
		ArrayList<SCMessage> orderedMsgs = new ArrayList<SCMessage>(getCruiseMessages(expocode));
		Collections.sort(orderedMsgs, SCMessage.woceTypeComparator);

		ArrayList<SocatWoceEvent> woceList = new ArrayList<SocatWoceEvent>();
		SCMsgType lastType = SCMsgType.UNKNOWN;
		SCMsgSeverity lastSeverity = SCMsgSeverity.UNKNOWN;
		int lastColNum = 0;
		ArrayList<DataLocation> locations = null;
		for ( SCMessage msg : orderedMsgs ) {
			SCMsgType msgType = msg.getType();
			if ( msgType.equals(SCMsgType.UNKNOWN) || msgType.equals(SCMsgType.METADATA) ) 
				continue;

			SCMsgSeverity severity = msg.getSeverity();
			if ( severity.equals(SCMsgSeverity.UNKNOWN) )
				continue;

			int rowNum = msg.getRowNumber();
			if ( rowNum <= 0 )
				continue;

			// if no specific column associated with this message, the column number is -1
			int colNum = msg.getColNumber();
			if ( colNum == 0 )
				continue;

			// Check if a new WOCE event is needed
			if ( ( ! msgType.equals(lastType) ) || 
				 ( ! severity.equals(lastSeverity) ) ||
				 ( colNum != lastColNum ) ) {
				SocatWoceEvent woceEvent = new SocatWoceEvent();
				woceEvent.setExpocode(expocode);
				woceEvent.setSocatVersion(cruiseData.getVersion());
				woceEvent.setFlagDate(new Date());
				woceEvent.setUsername(DashboardUtils.SANITY_CHECKER_USERNAME);
				woceEvent.setRealname(DashboardUtils.SANITY_CHECKER_REALNAME);
				if ( colNum > 0 )
					woceEvent.setColumnName(cruiseData.getUserColNames().get(colNum-1));

				if ( severity.equals(SCMsgSeverity.ERROR) )
					woceEvent.setFlag('4');
				else if ( severity.equals(SCMsgSeverity.WARNING) )
					woceEvent.setFlag('3');
				else
					throw new RuntimeException("Unexpected message severity of " + severity.toString());

				// TODO: need better code to generate a reasonable generic explanation
				String msgComment = msg.getExplanation();
				if ( msgType.equals(SCMsgType.DATA_QUESTIONABLE_VALUE) ) {
					int k = msgComment.indexOf("outside the expected range");
					woceEvent.setComment("Value is " + msgComment.substring(k));
				}
				else if ( msgType.equals(SCMsgType.DATA_BAD_VALUE) ) {
					int k = msgComment.indexOf("outside the extreme range");
					woceEvent.setComment("Value is " + msgComment.substring(k));
				}
				else if ( msgType.equals(SCMsgType.DATA_QUESTIONABLE_SPEED) ) {
					int k = msgComment.indexOf("should be");
					woceEvent.setComment("Calculated ship speed is excessive; " + msgComment.substring(k));
				}
				else if ( msgType.equals(SCMsgType.DATA_BAD_SPEED) ) {
					int k = msgComment.indexOf("should be");
					woceEvent.setComment("Calculated ship speed is unreasonable; " + msgComment.substring(k));
				}
				else if ( msgType.equals(SCMsgType.DATA_TIME) ) {
					woceEvent.setComment(msgComment);
				}
				else if ( msgType.equals(SCMsgType.DATA_MISSING) ) {
					woceEvent.setComment(msgComment);
				}
				else if ( msgType.equals(SCMsgType.DATA_CONSTANT) ) {
					woceEvent.setComment(msgComment);
				}
				else if ( msgType.equals(SCMsgType.DATA_JUMP) ) {
					woceEvent.setComment(msgComment);
				}
				else if ( msgType.equals(SCMsgType.DATA_GAP) ) {
					woceEvent.setComment(msgComment);
				}
				else if ( msgType.equals(SCMsgType.DATA_METADATA_NOT_SAME) ) {
					woceEvent.setComment(msgComment);
				}
				else if ( msgType.equals(SCMsgType.DATA_ERROR) ) {
					woceEvent.setComment(msgComment);
				}
				else
					throw new RuntimeException("Unexpected message type of " + msgType.toString());

				locations = new ArrayList<DataLocation>();
				woceEvent.setLocations(locations);

				woceList.add(woceEvent);
				lastType = msgType;
				lastSeverity = severity;
				lastColNum = colNum;
			}

			// Add this location to the current WOCE event
			DataLocation dataLoc = new DataLocation();
			dataLoc.setRowNumber(rowNum);
			// dataLoc.setDataDate(dataDate);
			// dataLoc.setDataValue(dataValue);
			// dataLoc.setLatitude(latitude);
			// dataLoc.setLongitude(longitude);
			// dataLoc.setRegionID(regionID);

			locations.add(dataLoc);
		}
		return woceList;
	}

}

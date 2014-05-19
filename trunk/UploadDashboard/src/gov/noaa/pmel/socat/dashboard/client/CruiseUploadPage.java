/**
 * 
 */
package gov.noaa.pmel.socat.dashboard.client;

import gov.noaa.pmel.socat.dashboard.client.SocatUploadDashboard.PagesEnum;
import gov.noaa.pmel.socat.dashboard.shared.DashboardUtils;

import java.util.ArrayList;
import java.util.Date;

import org.moxieapps.gwt.uploader.client.File;
import org.moxieapps.gwt.uploader.client.Uploader;
import org.moxieapps.gwt.uploader.client.events.FileQueuedEvent;
import org.moxieapps.gwt.uploader.client.events.FileQueuedHandler;
import org.moxieapps.gwt.uploader.client.events.UploadCompleteEvent;
import org.moxieapps.gwt.uploader.client.events.UploadCompleteHandler;
import org.moxieapps.gwt.uploader.client.events.UploadErrorEvent;
import org.moxieapps.gwt.uploader.client.events.UploadErrorHandler;
import org.moxieapps.gwt.uploader.client.events.UploadProgressEvent;
import org.moxieapps.gwt.uploader.client.events.UploadProgressHandler;
import org.moxieapps.gwt.uploader.client.events.UploadStartEvent;
import org.moxieapps.gwt.uploader.client.events.UploadStartHandler;
import org.moxieapps.gwt.uploader.client.events.UploadSuccessEvent;
import org.moxieapps.gwt.uploader.client.events.UploadSuccessHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * Page for uploading new or updated cruise data files.
 * 
 * @author Karl Smith
 */
public class CruiseUploadPage extends Composite {

	private static final String TITLE_TEXT = "Upload Data Files";
	private static final String WELCOME_INTRO = "Logged in as ";
	private static final String LOGOUT_TEXT = "Logout";

	private static final String SETTINGS_CAPTION_TEXT = "Settings";

	private static final String COMMA_FORMAT_HELP = 
			"the data file starts with lines of metadata, " +
			"then have a line of comma-separated column headers, and finally " +
			"a line of comma-separated data values for each data sample";
	private static final String TAB_FORMAT_HELP =
			"the data file starts with lines of metadata, " +
			"then have a line of tab-separated column headers, and finally " +
			"a line of tab-separated data values for each data sample";

	private static final String ADVANCED_HTML_MSG = 
			"Select a character set encoding for this file." +
			"<ul>" +
			"<li>If you are unsure of the encoding, UTF-8 should work fine.</li>" +
			"<li>The main differences in UTF-8 and ISO encodings are the " +
			"\"extended\" characters.</li>" +
			"<li>Use UTF-16 only if you know your file is encoded in that format, " +
			"but be aware that only Western European characters can be " +
			"properly handled.</li>" +
			"<li>Use the Window encoding only for files produced by older " +
			"Window programs. </li>" +
			"<li>The preview button will show the beginning of the file as it will " +
			"be seen by SOCAT using the given encoding.</li>" +
			"</ul>";
	private static final String ENCODING_TEXT = "File encoding:";
	private static final String[] KNOWN_ENCODINGS = {
		"ISO-8859-1", "ISO-8859-15", "UTF-8", "UTF-16", "Windows-1252"
	};
	private static final String PREVIEW_TEXT = "Preview Data File";
	private static final String NO_PREVIEW_HTML_MSG = "<p>(No file previewed)</p>";

	private static final String CREATE_TEXT = "create a new dataset";
	private static final String CREATE_HOVER_HELP = 
			"the data uploaded must create a new dataset to be successful";
	private static final String OVERWRITE_TEXT = "update an existing dataset";
	private static final String OVERWRITE_HOVER_HELP = 
			"the data uploaded must replace an existing dataset to be successful";

	private static final String SUBMIT_TEXT = "Upload";
	private static final String CANCEL_TEXT = "Cancel";

	private static final String NO_FILE_ERROR_MSG = 
			"Please select a data file to upload";
	private static final String FAIL_MSG_HEADER = 
			"<h3>Upload failed.</h3>";
	private static final String UNEXPLAINED_FAIL_MSG = 
			FAIL_MSG_HEADER + 
			"<p>Unexpectedly, no explanation of the failure was given</p>";
	private static final String SEE_PREVIEW_FAIL_MSG =
			FAIL_MSG_HEADER + 
			"<p>See the contents of the Preview for more explanation</p>";
	private static final String NO_EXPOCODE_FAIL_MSG = 
			"<h3>No cruise expocode found.</h3>" +
			"<p>The data file needs to contain the dataset expocode in the lines " +
			"of metadata preceding the data.  This expocode metadata line should " +
			"look something like<br />" +
			"&nbsp;&nbsp;&nbsp;&nbsp;expocode&nbsp;=&nbsp;49P120101218<br />" +
			"The 12 character expocode is the NODC code for the vessel carrying " +
			"the instrumentation followed by the numeric year, month, and day of " +
			"departure or initial measurement.  For example, 49P120101218 indicates " +
			"a cruise on the Japanese (49) ship of opportunity Pyxis (P1) with the " +
			"first day of the cruise on 18 December 2010.</p>" +
			"<p>The preview on the page contains the beginning of the file as it " +
			"appears to SOCAT.  If the contents look very strange, you might need " +
			"to change the character encoding in the advanced settings.</p>";
	private static final String FILE_EXISTS_FAIL_HTML = 
			"<h3>A dataset already exists with this expocode.</h3>" +
			"<p>The beginning of the existing dataset is given in the preview " +
			"window.  Select the <em>" + OVERWRITE_TEXT + "</em> setting if this " +
			"is an update to this existing dataset.";
	private static final String CANNOT_OVERWRITE_FAIL_MSG = 
			"<h3>A dataset already exists with this expocode.</h3>" +
			"<p>The existing dataset cannot be overwritten because it either has " +
			"been submitted to for QC or does not belong to you.  The beginning of " +
			"the existing dataset is given in the preview window.";
	private static final String FILE_DOES_NOT_EXIST_FAIL_HTML = 
			"<h3>A dataset with this expocode does not exist.</h3>  " +
			"The beginning of the uploaded dataset is given in the preview window.  " +
			"If the expocode in the data file is correct, use the <em>" + CREATE_TEXT + 
			"</em> setting to create a new dataset.";

	interface DashboardCruiseUploadPageUiBinder extends UiBinder<Widget, CruiseUploadPage> {
	}

	private static DashboardCruiseUploadPageUiBinder uiBinder = 
			GWT.create(DashboardCruiseUploadPageUiBinder.class);

	@UiField InlineLabel titleLabel;
	@UiField InlineLabel userInfoLabel;
	@UiField Button logoutButton;
	@UiField FlowPanel filesPanel;
	@UiField Uploader filesUploader;
	@UiField CaptionPanel settingsPanel;
	@UiField RadioButton commaRadio;
	@UiField RadioButton tabRadio;
	@UiField DisclosurePanel advancedPanel;
	@UiField HTML advancedHtml;
	@UiField Label encodingLabel;
	@UiField ListBox encodingListBox;
	@UiField Button previewButton;
	@UiField HTML previewHtml;
	@UiField RadioButton createRadio;
	@UiField RadioButton overwriteRadio;
	@UiField Button submitButton;
	@UiField Button cancelButton;

	private String username;
	private String uploadAction;
	private boolean continueUpload;
	private ArrayList<File> queuedFiles;
	private ArrayList<FileUploadEntry> queuedEntries;
	private ArrayList<String> uploadedExpocodes;

	// Singleton instance of this page
	private static CruiseUploadPage singleton = null;

	/**
	 * Creates an empty cruise upload page.  Do not call this 
	 * constructor; instead use the showPage static method 
	 * to show the singleton instance of this page. 
	 */
	CruiseUploadPage() {
		initWidget(uiBinder.createAndBindUi(this));
		singleton = this;

		username = "";
		queuedFiles = new ArrayList<File>();
		queuedEntries = new ArrayList<FileUploadEntry>();
		uploadedExpocodes = new ArrayList<String>();

		titleLabel.setText(TITLE_TEXT);
		logoutButton.setText(LOGOUT_TEXT);

		filesUploader.setUploadURL(
				GWT.getModuleBaseURL() + "CruiseUploadService");
		filesUploader.setButtonImageURL(SocatUploadDashboard.resources
						.getUploadButtonsPng().getSafeUri().asString());
		filesUploader.setButtonWidth(192);
		filesUploader.setButtonHeight(32);
		filesUploader.setButtonCursor(Uploader.Cursor.HAND);
		filesUploader.setFileQueuedHandler(new FileQueuedHandler() {
			@Override
			public boolean onFileQueued(FileQueuedEvent evnt) {
				updateFilesPanel(evnt.getFile());
				return true;
			}
		});
		filesUploader.setUploadStartHandler(new UploadStartHandler() {
			@Override
			public boolean onUploadStart(UploadStartEvent evnt) {
				// Add the POST parameters and continue with the upload
				assignPostOptions();
				int idx = queuedFiles.indexOf(evnt.getFile());
				if ( idx >= 0 )
					queuedEntries.get(idx).showUploadStarted();
				return true;
			}
			
		});
		filesUploader.setUploadProgressHandler(new UploadProgressHandler() {
			@Override
			public boolean onUploadProgress(UploadProgressEvent evnt) {
				int idx = queuedFiles.indexOf(evnt.getFile());
				if ( idx >= 0 )
					queuedEntries.get(idx).showProgress(evnt);
				return true;
			}
		});
		filesUploader.setUploadErrorHandler(new UploadErrorHandler() {
			@Override
			public boolean onUploadError(UploadErrorEvent evnt) {
				int idx = queuedFiles.indexOf(evnt.getFile());
				if ( idx >= 0 )
					queuedEntries.get(idx).showUploadFailed();
				// Serious problem in the upload (not normal errors)
				String msg = evnt.getMessage();
				if ( msg == null ) {
					SocatUploadDashboard.showMessage(UNEXPLAINED_FAIL_MSG);
				}
				else {
					SocatUploadDashboard.showMessage(FAIL_MSG_HEADER + 
							"<p>" + SafeHtmlUtils.htmlEscape(msg) + "</p>");
				}
				// Stop uploading files
				continueUpload = false;
				return true;
			}
		});
		filesUploader.setUploadSuccessHandler(new UploadSuccessHandler() {
			@Override
			public boolean onUploadSuccess(UploadSuccessEvent evnt) {
				int idx = queuedFiles.indexOf(evnt.getFile());
				if ( idx >= 0 )
					queuedEntries.get(idx).showUploadDone();
				// Process the returned message to get the expocode or identify an error
				String expocode = processResultMsg(evnt.getServerData());
				if ( expocode == null ) {
					continueUpload = false;
				}
				else {
					uploadedExpocodes.add(expocode);
				}
				return true;
			}
		});
		filesUploader.setUploadCompleteHandler(new UploadCompleteHandler() {
			@Override
			public boolean onUploadComplete(UploadCompleteEvent evnt) {
				// Clear the progress label and post parameters 
				clearPostOptions();
				if ( continueUpload ) {
					// If more files queued, start the next file upload;
					// otherwise go to the DataColumnSpecsPage.
					if ( filesUploader.getStats().getFilesQueued() > 0 ) {
						filesUploader.startUpload();
					}
					else {
						// TODO: send the whole list of expocodes for batch processing
						DataColumnSpecsPage.showPage(uploadedExpocodes.get(0));
					}
				}
				return true;
			}
		});

		settingsPanel.setCaptionText(SETTINGS_CAPTION_TEXT);

		commaRadio.setText(DashboardUtils.CRUISE_FORMAT_COMMA);
		commaRadio.setTitle(COMMA_FORMAT_HELP);
		tabRadio.setText(DashboardUtils.CRUISE_FORMAT_TAB);
		tabRadio.setTitle(TAB_FORMAT_HELP);
		commaRadio.setValue(false, false);
		tabRadio.setValue(true, false);

		createRadio.setText(CREATE_TEXT);
		createRadio.setTitle(CREATE_HOVER_HELP);
		overwriteRadio.setText(OVERWRITE_TEXT);
		overwriteRadio.setTitle(OVERWRITE_HOVER_HELP);
		overwriteRadio.setValue(false, false);
		createRadio.setValue(true, false);

		submitButton.setText(SUBMIT_TEXT);
		cancelButton.setText(CANCEL_TEXT);

		advancedHtml.setHTML(ADVANCED_HTML_MSG);
		encodingLabel.setText(ENCODING_TEXT);
		encodingListBox.setVisibleItemCount(1);
		for ( String encoding : KNOWN_ENCODINGS )
			encodingListBox.addItem(encoding);
		previewButton.setText(PREVIEW_TEXT);
	}

	/**
	 * Display the cruise upload page in the RootLayoutPanel
	 * after clearing as much of the page as possible.  
	 * The upload filename cannot be cleared. 
	 * Adds this page to the page history.
	 */
	static void showPage() {
		if ( singleton == null )
			singleton = new CruiseUploadPage();
		singleton.username = DashboardLoginPage.getUsername();
		singleton.userInfoLabel.setText(WELCOME_INTRO + 
				singleton.username);
		singleton.removeAllUploadFiles();
		singleton.uploadedExpocodes.clear();
		singleton.clearPostOptions();
		singleton.previewHtml.setHTML(NO_PREVIEW_HTML_MSG);
		singleton.encodingListBox.setSelectedIndex(2);
		singleton.advancedPanel.setOpen(false);
		SocatUploadDashboard.updateCurrentPage(singleton);
		History.newItem(PagesEnum.UPLOAD_DATASETS.name(), false);
	}

	/**
	 * Redisplays the last version of this page if the username
	 * associated with this page matches the current login username.
	 * 
	 * @param addToHistory 
	 * 		if true, adds this page to the page history 
	 */
	static void redisplayPage(boolean addToHistory) {
		// If never show before, or if the username does not match the 
		// current login username, show the login page instead
		if ( (singleton == null) || 
			 ! singleton.username.equals(DashboardLoginPage.getUsername()) ) {
			DashboardLoginPage.showPage(true);
		}
		else {
			SocatUploadDashboard.updateCurrentPage(singleton);
			if ( addToHistory )
				History.newItem(PagesEnum.UPLOAD_DATASETS.name(), false);
		}
	}

	/**
	 * Adds the given upload file to the list of queued files, and 
	 * creates an entry in the scrolled panel of files.
	 * 
	 * @param uploadFile
	 * 		upload file to add
	 */
	private void updateFilesPanel(File uploadFile) {
		queuedFiles.add(uploadFile);
		FileUploadEntry entry = new FileUploadEntry(uploadFile);
		queuedEntries.add(entry);
		filesPanel.add(entry);
	}

	/**
	 * Removes all files from the queue of files to upload.
	 */
	private void removeAllUploadFiles() {
		while ( queuedFiles.size() > 0 ) {
			removeUploadFileFromQueue(queuedFiles.get(0));
		}
	}

	/**
	 * Removes a file from the queue of files to upload.
	 * 
	 * @param uploadFile
	 * 		file to remove
	 */
	static void removeUploadFile(File uploadFile) {
		if ( singleton == null )
			return;
		singleton.removeUploadFileFromQueue(uploadFile);
	}

	/**
	 * Removes a file from the queue of files to upload.
	 * 
	 * @param uploadFile
	 * 		file to remove
	 */
	private void removeUploadFileFromQueue(File uploadFile) {
		int idx = queuedFiles.indexOf(uploadFile);
		if ( idx < 0 )
			return;
		filesUploader.cancelUpload(uploadFile.getId(), false);
		filesPanel.remove(queuedEntries.get(idx));
		queuedEntries.remove(idx);
		queuedFiles.remove(idx);
	}

	/**
	 * Sets the POST parameters contained in filesUploader.
	 */
	private void assignPostOptions() {
		String localTimestamp = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm")
											  .format(new Date());
		String encoding = KNOWN_ENCODINGS[encodingListBox.getSelectedIndex()];
		String format;
		if ( commaRadio.getValue() )
			format = DashboardUtils.CRUISE_FORMAT_COMMA;
		else
			format = DashboardUtils.CRUISE_FORMAT_TAB;
		String lastFile;
		if ( filesUploader.getStats().getFilesQueued() > 1 )
			lastFile = "false";
		else
			lastFile = "true";
		
		JSONObject postParams = new JSONObject();
		postParams.put("username", new JSONString(DashboardLoginPage.getUsername()));
		postParams.put("passhash", new JSONString(DashboardLoginPage.getPasshash()));
		postParams.put("timestamp", new JSONString(localTimestamp));
		postParams.put("cruiseaction", new JSONString(uploadAction));
		postParams.put("cruiseencoding", new JSONString(encoding));
		postParams.put("cruiseformat", new JSONString(format)); 
		postParams.put("lastfile", new JSONString(lastFile));
		filesUploader.setPostParams(postParams);
	}

	/**
	 * Clears the POST parameters contained in filesUploader.
	 */
	private void clearPostOptions() {
		JSONObject postParams = new JSONObject();
		postParams.put("username", new JSONString(""));
		postParams.put("passhash", new JSONString(""));
		postParams.put("timestamp", new JSONString(""));
		postParams.put("cruiseaction", new JSONString(""));
		postParams.put("cruiseencoding", new JSONString(""));
		postParams.put("cruiseformat", new JSONString("")); 
		postParams.put("lastfile", new JSONString(""));
		filesUploader.setPostParams(postParams);
	}

	@UiHandler("logoutButton")
	void logoutOnClick(ClickEvent event) {
		DashboardLogoutPage.showPage();
	}

	@UiHandler("previewButton") 
	void previewButtonOnClick(ClickEvent event) {
		// Check if any files have been selected
		if ( filesUploader.getStats().getFilesQueued() < 1 ) {
			SocatUploadDashboard.showMessage(NO_FILE_ERROR_MSG);
			return;
		}
		// Assign the server action requested 
		uploadAction = DashboardUtils.REQUEST_PREVIEW_TAG;
		// Have it submit only the first file
		continueUpload = false;
		// Submit the first file
		filesUploader.startUpload();
	}

	@UiHandler("submitButton") 
	void createButtonOnClick(ClickEvent event) {
		// Check if any files have been selected
		if ( filesUploader.getStats().getFilesQueued() < 1 ) {
			SocatUploadDashboard.showMessage(NO_FILE_ERROR_MSG);
			return;
		}
		if ( overwriteRadio.getValue() )
			uploadAction = DashboardUtils.REQUEST_OVERWRITE_CRUISE_TAG;
		else
			uploadAction = DashboardUtils.REQUEST_NEW_CRUISE_TAG;
		// Have it submit all files
		continueUpload = true;
		// Clear the list of uploaded dataset expocodes
		uploadedExpocodes.clear();
		// Submit the first file
		filesUploader.startUpload();
	}

	@UiHandler("cancelButton")
	void cancelButtonOnClick(ClickEvent event) {
		// Stop any uploads that may be in progress
		continueUpload = false;
		// Return to the cruise list page after updating the cruise list
		CruiseListPage.showPage(false);
	}

	/**
	 * Process the message returned from the upload of a dataset.
	 * 
	 * @param resultMsg
	 * 		message returned from the upload of a dataset
	 * @return
	 * 		if unsuccessful or if a preview-only, null;
	 * 		otherwise, the expocode of the dataset.
	 */
	private String processResultMsg(String resultMsg) {
		// Check the returned results
		if ( resultMsg == null ) {
			SocatUploadDashboard.showMessage(UNEXPLAINED_FAIL_MSG);
			return null;
		}

		String expocode = null;
		String[] tagMsg = resultMsg.split("\n", 2);
		if ( tagMsg.length < 2 ) {
			// probably an error response; display the message in the preview
			String previewMsg;
			if ( resultMsg.contains("</pre>") )
				previewMsg = "<pre>" + SafeHtmlUtils.htmlEscape(resultMsg) + "</pre>";
			else
				previewMsg = "<pre>" + resultMsg + "</pre>";
			advancedPanel.setOpen(true);
			previewHtml.setHTML(previewMsg);
			SocatUploadDashboard.showMessage(SEE_PREVIEW_FAIL_MSG);
		}
		else if ( tagMsg[0].equals(DashboardUtils.FILE_PREVIEW_HEADER_TAG) ) {
			// preview file; show partial file contents in the preview
			String previewMsg;
			if ( tagMsg[1].contains("</pre>") )
				previewMsg = "<pre>" + SafeHtmlUtils.htmlEscape(tagMsg[1]) + "</pre>";
			else
				previewMsg = "<pre>" + tagMsg[1] + "</pre>";
			advancedPanel.setOpen(true);
			previewHtml.setHTML(previewMsg);
		}
		else if ( tagMsg[0].equals(DashboardUtils.NO_EXPOCODE_HEADER_TAG) ) {
			// no expocode found; show uploaded file partial contents
			String previewMsg;
			if ( tagMsg[1].contains("</pre>") )
				previewMsg = "<pre>" + SafeHtmlUtils.htmlEscape(tagMsg[1]) + "</pre>";
			else
				previewMsg = "<pre>" + tagMsg[1] + "</pre>";
			advancedPanel.setOpen(true);
			previewHtml.setHTML(previewMsg);
			SocatUploadDashboard.showMessage(NO_EXPOCODE_FAIL_MSG);
		}
		else if ( tagMsg[0].equals(DashboardUtils.FILE_EXISTS_HEADER_TAG) ) {
			// cruise file exists and request was to create a new cruise; 
			// show existing file partial contents in the preview
			String previewMsg;
			if ( tagMsg[1].contains("</pre>") )
				previewMsg = "<pre>" + SafeHtmlUtils.htmlEscape(tagMsg[1]) + "</pre>";
			else
				previewMsg = "<pre>" + tagMsg[1] + "</pre>";
			advancedPanel.setOpen(true);
			previewHtml.setHTML(previewMsg);
			SocatUploadDashboard.showMessage(FILE_EXISTS_FAIL_HTML);
		}
		else if ( tagMsg[0].equals(DashboardUtils.CANNOT_OVERWRITE_HEADER_TAG) ) {
			// cruise file exists and not permitted to overwrite; 
			// show existing file partial contents in the preview
			String previewMsg;
			if ( tagMsg[1].contains("</pre>") )
				previewMsg = "<pre>" + SafeHtmlUtils.htmlEscape(tagMsg[1]) + "</pre>";
			else
				previewMsg = "<pre>" + tagMsg[1] + "</pre>";
			advancedPanel.setOpen(true);
			previewHtml.setHTML(previewMsg);
			SocatUploadDashboard.showMessage(CANNOT_OVERWRITE_FAIL_MSG);
		}
		else if ( tagMsg[0].equals(DashboardUtils.NO_FILE_HEADER_TAG) ) {
			// cruise file does not exist and request was to overwrite
			// an existing cruise; show partial file contents in preview
			String previewMsg;
			if ( (tagMsg[1]).contains("</pre>") )
				previewMsg = "<pre>" + SafeHtmlUtils.htmlEscape(tagMsg[1]) + "</pre>";
			else
				previewMsg = "<pre>" + tagMsg[1] + "</pre>";
			advancedPanel.setOpen(true);
			previewHtml.setHTML(previewMsg);
			SocatUploadDashboard.showMessage(FILE_DOES_NOT_EXIST_FAIL_HTML);
		}
		else if ( tagMsg[0].startsWith(DashboardUtils.FILE_CREATED_HEADER_TAG) ) {
			expocode = tagMsg[0].substring(
					DashboardUtils.FILE_CREATED_HEADER_TAG.length()).trim();
		}
		else if ( tagMsg[0].startsWith(DashboardUtils.FILE_UPDATED_HEADER_TAG) ) {
			expocode = tagMsg[0].substring(
					DashboardUtils.FILE_UPDATED_HEADER_TAG.length()).trim();
		}
		else {
			// Unknown response with a newline, display the whole message in the preview
			String previewMsg;
			if ( resultMsg.contains("</pre>") )
				previewMsg = "<pre>" + SafeHtmlUtils.htmlEscape(resultMsg) + "</pre>";
			else
				previewMsg = "<pre>" + resultMsg + "</pre>";
			advancedPanel.setOpen(true);
			previewHtml.setHTML(previewMsg);
			SocatUploadDashboard.showMessage(SEE_PREVIEW_FAIL_MSG);
		}
		return expocode;
	}

}

package gov.noaa.pmel.dashboard.client.metadata.pipanels;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import gov.noaa.pmel.dashboard.client.UploadDashboard;
import gov.noaa.pmel.dashboard.client.metadata.LabeledTextArea;
import gov.noaa.pmel.dashboard.client.metadata.LabeledTextBox;
import gov.noaa.pmel.socatmetadata.shared.core.MultiString;
import gov.noaa.pmel.socatmetadata.shared.person.Investigator;

import java.util.HashSet;

public class InvestigatorPanel extends Composite {

    interface InvestigatorPanelUiBinder extends UiBinder<FlowPanel,InvestigatorPanel> {
    }

    private static final InvestigatorPanelUiBinder uiBinder = GWT.create(InvestigatorPanelUiBinder.class);

    @UiField(provided = true)
    final LabeledTextBox firstNameValue;
    @UiField(provided = true)
    final LabeledTextBox middleInitValue;
    @UiField(provided = true)
    final LabeledTextBox lastNameValue;
    @UiField(provided = true)
    final LabeledTextBox idValue;
    @UiField(provided = true)
    final LabeledTextBox idTypeValue;
    @UiField(provided = true)
    final LabeledTextBox orgValue;
    @UiField(provided = true)
    final LabeledTextArea streetsValue;
    @UiField(provided = true)
    final LabeledTextBox cityValue;
    @UiField(provided = true)
    final LabeledTextBox regionValue;
    @UiField(provided = true)
    final LabeledTextBox zipValue;
    @UiField(provided = true)
    final LabeledTextBox countryValue;
    @UiField(provided = true)
    final LabeledTextBox emailValue;
    @UiField(provided = true)
    final LabeledTextBox phoneValue;

    private final Investigator investigator;
    private final HTML header;

    /**
     * Creates a FlowPanel associated with the given Investigator.
     *
     * @param investigator
     *         associate this panel with this Investigator; cannot be null
     * @param header
     *         header that should be updated when appropriate values change; can be null
     */
    public InvestigatorPanel(Investigator investigator, HTML header) {
        firstNameValue = new LabeledTextBox("First name:", "7em", "14em", null, null);
        middleInitValue = new LabeledTextBox("Middle initial(s):", "8em", "5em", null, null);
        lastNameValue = new LabeledTextBox("Last name:", "6em", "15.75em", null, null);
        //
        idTypeValue = new LabeledTextBox("ID type:", "7em", "23em", null, null);
        idValue = new LabeledTextBox("ID:", "5em", "23em", null, null);
        //
        orgValue = new LabeledTextBox("Organization:", "7em", "53em", null, null);
        //
        streetsValue = new LabeledTextArea("Street/Box:", "7em", "3em", "53em");
        //
        cityValue = new LabeledTextBox("City:", "7em", "23em", null, null);
        regionValue = new LabeledTextBox("Region:", "5em", "23em", null, null);
        //
        zipValue = new LabeledTextBox("Postal code:", "7em", "23em", null, null);
        countryValue = new LabeledTextBox("Country:", "5em", "23em", null, null);
        //
        emailValue = new LabeledTextBox("E-mail:", "7em", "23em", null, null);
        phoneValue = new LabeledTextBox("Phone:", "5em", "23em", null, null);

        initWidget(uiBinder.createAndBindUi(this));

        this.investigator = investigator;
        this.header = header;

        firstNameValue.setText(investigator.getFirstName());
        middleInitValue.setText(investigator.getMiddle());
        lastNameValue.setText(investigator.getLastName());
        idValue.setText(investigator.getId());
        idTypeValue.setText(investigator.getIdType());
        orgValue.setText(investigator.getOrganization());
        streetsValue.setText(investigator.getStreets().asOneString());
        cityValue.setText(investigator.getCity());
        regionValue.setText(investigator.getRegion());
        zipValue.setText(investigator.getZipCode());
        countryValue.setText(investigator.getCountry());
        emailValue.setText(investigator.getEmail());
        phoneValue.setText(investigator.getPhone());

        markInvalids();
    }

    @UiHandler("firstNameValue")
    void firstNameValueOnValueChange(ValueChangeEvent<String> event) {
        investigator.setFirstName(firstNameValue.getText());
        markInvalids();
    }

    @UiHandler("middleInitValue")
    void middleInitValueOnValueChange(ValueChangeEvent<String> event) {
        investigator.setMiddle(middleInitValue.getText());
        markInvalids();
    }

    @UiHandler("lastNameValue")
    void lastNameValueOnValueChange(ValueChangeEvent<String> event) {
        investigator.setLastName(lastNameValue.getText());
        markInvalids();
    }

    @UiHandler("idTypeValue")
    void idTypeValueOnValueChange(ValueChangeEvent<String> event) {
        investigator.setIdType(idTypeValue.getText());
        markInvalids();
    }

    @UiHandler("idValue")
    void idValueOnValueChange(ValueChangeEvent<String> event) {
        investigator.setId(idValue.getText());
        markInvalids();
    }

    @UiHandler("orgValue")
    void orgValueOnValueChange(ValueChangeEvent<String> event) {
        investigator.setOrganization(orgValue.getText());
        markInvalids();
    }

    @UiHandler("streetsValue")
    void streetsValueOnValueChange(ValueChangeEvent<String> event) {
        investigator.setStreets(new MultiString(streetsValue.getText()));
        markInvalids();
    }

    @UiHandler("cityValue")
    void cityValueOnValueChange(ValueChangeEvent<String> event) {
        investigator.setCity(cityValue.getText());
        markInvalids();
    }

    @UiHandler("regionValue")
    void regionValueOnValueChange(ValueChangeEvent<String> event) {
        investigator.setRegion(regionValue.getText());
        markInvalids();
    }

    @UiHandler("zipValue")
    void zipValueOnValueChange(ValueChangeEvent<String> event) {
        investigator.setZipCode(zipValue.getText());
        markInvalids();
    }

    @UiHandler("countryValue")
    void countryValueOnValueChange(ValueChangeEvent<String> event) {
        investigator.setCountry(countryValue.getText());
        markInvalids();
    }

    @UiHandler("emailValue")
    void emailValueOnValueChange(ValueChangeEvent<String> event) {
        investigator.setEmail(emailValue.getText());
        markInvalids();
    }

    @UiHandler("phoneValue")
    void phoneValueOnValueChange(ValueChangeEvent<String> event) {
        investigator.setPhone(phoneValue.getText());
        markInvalids();
    }

    /**
     * Indicate which fields contain invalid values and which contain acceptable values.
     */
    private void markInvalids() {
        HashSet<String> invalids = investigator.invalidFieldNames();

        if ( header != null ) {
            String oldVal = header.getHTML();
            SafeHtml val = SafeHtmlUtils.fromString(investigator.getReferenceName());
            if ( !invalids.isEmpty() )
                val = UploadDashboard.invalidLabelHtml(val);
            if ( !val.asString().equals(oldVal) )
                header.setHTML(val);
        }

        firstNameValue.markInvalid(invalids.contains("firstName"));
        middleInitValue.markInvalid(invalids.contains("middle"));
        lastNameValue.markInvalid(invalids.contains("lastName"));
        idTypeValue.markInvalid(invalids.contains("idType"));
        idValue.markInvalid(invalids.contains("id"));
        orgValue.markInvalid(invalids.contains("organization"));
        streetsValue.markInvalid(invalids.contains("streets"));
        cityValue.markInvalid(invalids.contains("city"));
        regionValue.markInvalid(invalids.contains("region"));
        zipValue.markInvalid(invalids.contains("zipCode"));
        countryValue.markInvalid(invalids.contains("country"));
        emailValue.markInvalid(invalids.contains("email"));
        phoneValue.markInvalid(invalids.contains("phone"));
    }

    /**
     * @return the updated Investigator; never null
     */
    public Investigator getUpdatedInvestigator() {
        return investigator;
    }

}
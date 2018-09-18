package gov.noaa.pmel.sdimetadata.test;

import gov.noaa.pmel.sdimetadata.person.Person;
import gov.noaa.pmel.sdimetadata.platform.PlatformType;
import gov.noaa.pmel.sdimetadata.util.Datestamp;
import gov.noaa.pmel.sdimetadata.util.NumericString;
import gov.noaa.pmel.sdimetadata.xml.DocumentHandler;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static gov.noaa.pmel.sdimetadata.xml.DocumentHandler.SEP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DocumentHandlerTest {

    private class MyDocHandler extends DocumentHandler {
        MyDocHandler(String xmlString) {
            Document omeDoc = null;
            try {
                omeDoc = (new SAXBuilder()).build(new StringReader(xmlString));
            } catch ( Exception ex ) {
                throw new RuntimeException(ex);
            }
            rootElement = omeDoc.getRootElement();
            if ( rootElement == null )
                throw new RuntimeException("No root element found");
        }
    }

    @Test
    public void testGuessPlatformType() {
        String name = "Ronald H. Brown";
        String datasetId = "33RO20150114";
        assertEquals(PlatformType.SHIP, DocumentHandler.guessPlatformType(name, datasetId));

        name = "MySpecialPlatform";
        datasetId = "316420100523-1";
        assertEquals(PlatformType.MOORING, DocumentHandler.guessPlatformType(name, datasetId));

        name = "MySpecialPlatform";
        datasetId = "35DR20100523-2";
        assertEquals(PlatformType.DRIFTING_BUOY, DocumentHandler.guessPlatformType(name, datasetId));

        name = "my special buoy that does not drift in the ocean";
        datasetId = "MySpecialID";
        assertEquals(PlatformType.MOORING, DocumentHandler.guessPlatformType(name, datasetId));

        name = "my special drifting buoy in the ocean";
        datasetId = "MySpecialID";
        assertEquals(PlatformType.DRIFTING_BUOY, DocumentHandler.guessPlatformType(name, datasetId));

        name = "My Mooring with an incorrect expocode";
        datasetId = "35DR20100523";
        assertEquals(PlatformType.MOORING, DocumentHandler.guessPlatformType(name, datasetId));

        name = "my special mooring in the ocean";
        datasetId = "";
        assertEquals(PlatformType.MOORING, DocumentHandler.guessPlatformType(name, datasetId));

        name = "";
        datasetId = "";
        assertEquals(PlatformType.SHIP, DocumentHandler.guessPlatformType(name, datasetId));
    }

    @Test
    public void testGetPersonNames() {
        assertEquals(new Person("Brown", "H.M.S.", "Ronald H.", null, null, null),
                DocumentHandler.getPersonNames("H.M.S. Ronald H. Brown"));
        assertEquals(new Person("Brown", "Ronald", "H.", null, null, null),
                DocumentHandler.getPersonNames("Brown, Ronald H."));
        assertEquals(new Person("Brown", "Ronald", null, null, null, null),
                DocumentHandler.getPersonNames("Ronald Brown"));
        assertEquals(new Person("Brown", "Ronald", null, null, null, null),
                DocumentHandler.getPersonNames("Brown, Ronald"));
        assertEquals(new Person("Brown", null, null, null, null, null),
                DocumentHandler.getPersonNames("Brown"));
        assertEquals(new Person(null, null, null, null, null, null),
                DocumentHandler.getPersonNames(" \t "));
        assertEquals(new Person(null, null, null, null, null, null),
                DocumentHandler.getPersonNames(null));
    }

    @Test
    public void testGetListOfLines() {
        final ArrayList<String> lines = new ArrayList<String>(Arrays.asList(
                "first line of information",
                "second line\twith a tab",
                "third line part; next line part",
                "another line"
        ));
        String concat = lines.get(0) + "\n" + lines.get(1) + "\r\n" + lines.get(2) + "\r" + lines.get(3);
        assertEquals(lines, DocumentHandler.getListOfLines(concat));
        concat = lines.get(0) + "\n\n" + lines.get(1) + "\r\n" + lines.get(2) + "\r\r" + lines.get(3);
        assertEquals(lines, DocumentHandler.getListOfLines(concat));
        assertEquals(new ArrayList<String>(), DocumentHandler.getListOfLines(""));
        assertEquals(new ArrayList<String>(), DocumentHandler.getListOfLines(null));
    }

    @Test
    public void testGetDatestamp() {
        final String year = "2010";
        final String month = "3";
        final String day = "24";
        final Datestamp stamp = new Datestamp(year, month, day);

        String concat = year + "0" + month + day;
        assertEquals(stamp, DocumentHandler.getDatestamp(concat));
        concat = year + month + day;
        assertNull(DocumentHandler.getDatestamp(concat));
        concat = year + "-" + month + "-" + day;
        assertEquals(stamp, DocumentHandler.getDatestamp(concat));
        concat = year + "/" + month + "/" + day;
        assertEquals(stamp, DocumentHandler.getDatestamp(concat));
        assertNull(DocumentHandler.getDatestamp("Mar 24, 2010"));
        assertNull(DocumentHandler.getDatestamp((String) null));
        assertNull(DocumentHandler.getDatestamp("\t"));

        Date time = stamp.getEarliestTime();
        assertEquals(stamp, DocumentHandler.getDatestamp(time));
        time = new Date(stamp.getEarliestTime().getTime() + 24L * 60L * 60L * 1000L - 1000L);
        assertEquals(stamp, DocumentHandler.getDatestamp(time));
        assertNull(DocumentHandler.getDatestamp((Date) null));
    }

    @Test
    public void testGetNumericString() {
        final String numVal = "345.0";
        final String unitVal = "umol / mol";
        final NumericString numstr = new NumericString(numVal, unitVal);
        final NumericString invalid = new NumericString();

        assertEquals(numstr, DocumentHandler.getNumericString(numVal, unitVal));
        String concat = numVal + " " + unitVal;
        assertEquals(numstr, DocumentHandler.getNumericString(concat, null));
        assertEquals(invalid, DocumentHandler.getNumericString(concat, ""));
        concat = numVal + " (" + unitVal + ")";
        assertEquals(numstr, DocumentHandler.getNumericString(concat, null));
        concat = numVal + "[" + unitVal + "] ";
        assertEquals(numstr, DocumentHandler.getNumericString(concat, null));
        concat = numVal + " {" + unitVal + "}";
        assertEquals(numstr, DocumentHandler.getNumericString(concat, null));
        concat = numVal + " ({" + unitVal + "})";
        assertEquals(numstr, DocumentHandler.getNumericString(concat, null));
    }

    @Test
    public void testGetElementList() {
        MyDocHandler docHandler = new MyDocHandler(CdiacReaderTest.AOML_CDIAC_XML_DATA_STRING);
        List<Element> elems = docHandler.getElementList("Investigator");
        assertEquals(1, elems.size());
        assertEquals("Rik Wanninkhof", elems.get(0).getChildTextTrim("Name"));
        elems = docHandler.getElementList("Variables_Info" + SEP + "Variable");
        assertEquals(13, elems.size());
        assertEquals("xCO2_EQU_ppm", elems.get(0).getChildTextTrim("Variable_Name"));
        elems = docHandler.getElementList("Cruise_Info" + SEP + "Experiment" + SEP + "Experiment_Name");
        assertEquals(1, elems.size());
        assertEquals("RB1501A", elems.get(0).getTextTrim());
        elems = docHandler.getElementList("Cruise_Info" + SEP + "Experiment" + SEP + "garbage");
        assertEquals(0, elems.size());
        elems = docHandler.getElementList("");
        assertEquals(0, elems.size());
    }

    @Test
    public void testGetElementText() {
        MyDocHandler docHandler = new MyDocHandler(CdiacReaderTest.AOML_CDIAC_XML_DATA_STRING);
        assertEquals("Robert Castle", docHandler.getElementText("User" + SEP + "Name"));
        assertEquals("", docHandler.getElementText("Cruise_Info"));
        assertEquals("", docHandler.getElementText("User" + SEP + "garbage"));
        assertEquals("", docHandler.getElementText(""));
    }

    @Test
    public void testSetElementText() {
        MyDocHandler docHandler = new MyDocHandler(EMPTY_OCADS_XML_DATA_STRING);
        String name = "Expocode";
        String value = "316420100523";
        docHandler.setElementText(name, value);
        assertEquals(value, docHandler.getElementText(name));
        name = "person" + SEP + "name";
        value = "Ronald H. Brown";
        docHandler.setElementText(name, value);
        assertEquals(value, docHandler.getElementText(name));
        String otherval = "John Smith";
        docHandler.setElementText(name, otherval);
        List<Element> elemList = docHandler.getElementList(name);
        assertEquals(1, elemList.size());
        assertEquals(otherval, elemList.get(0).getText());
        name = "some" + SEP + "path" + SEP + "name";
        docHandler.setElementText(name, null);
        assertEquals(0, docHandler.getElementList(name).size());
        assertEquals(0, docHandler.getElementList("some" + SEP + "path").size());
        assertEquals(0, docHandler.getElementList("some").size());
        docHandler.setElementText(name, "\t");
        assertEquals(0, docHandler.getElementList(name).size());
        assertEquals(0, docHandler.getElementList("some" + SEP + "path").size());
        assertEquals(0, docHandler.getElementList("some").size());
    }

    @Test
    public void testAddListElement() {
        MyDocHandler docHandler = new MyDocHandler(EMPTY_OCADS_XML_DATA_STRING);
        String name = "investigators" + SEP + "investigator" + SEP + "name";
        String value = "Ronald H. Brown";
        String otherval = "John Smith";
        Element elem = docHandler.addListElement(name);
        elem.setText(value);
        elem = docHandler.addListElement(name);
        elem.setText(otherval);
        List<Element> elemList = docHandler.getElementList(name);
        assertEquals(2, elemList.size());
        assertEquals(value, elemList.get(0).getText());
        assertEquals(otherval, elemList.get(1).getText());
        name = "update";
        value = "2018-01-23";
        otherval = "2018-08-03";
        elem = docHandler.addListElement(name);
        elem.setText(value);
        elem = docHandler.addListElement(name);
        elem.setText(otherval);
        elemList = docHandler.getElementList(name);
        assertEquals(2, elemList.size());
        assertEquals(value, elemList.get(0).getText());
        assertEquals(otherval, elemList.get(1).getText());
    }

    static final String EMPTY_OCADS_XML_DATA_STRING = "<?xml-stylesheet href=\"xmlblob.xsl\" type=\"text/xsl\"?>" +
            "<metadata></metadata>";

}


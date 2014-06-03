/**
 * 
 */
package gov.noaa.pmel.socat.dashboard.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import gov.noaa.pmel.socat.dashboard.shared.DataColumnType;
import gov.noaa.pmel.socat.dashboard.shared.DataLocation;
import gov.noaa.pmel.socat.dashboard.shared.SocatCruiseData;
import gov.noaa.pmel.socat.dashboard.shared.SocatEvent;
import gov.noaa.pmel.socat.dashboard.shared.SocatMetadata;
import gov.noaa.pmel.socat.dashboard.shared.SocatWoceEvent;

import java.util.ArrayList;
import java.util.Date;

import org.junit.Test;

/**
 * Unit test for methods of SocatWoceFlag
 * 
 * @author Karl Smith
 */
public class SocatWoceEventTest {

	private static final Character MY_WOCE_FLAG = '3';
	private static final String MY_EXPOCODE = "26NA20140427";
	private static final Double MY_SOCAT_VERSION = 3.0;
	private static final DataColumnType MY_DATA_TYPE = DataColumnType.SEA_LEVEL_PRESSURE;
	private static final String MY_COLUMN_NAME = "SLP";
	private static final ArrayList<DataLocation> MY_LOCATIONS;
	static {
		MY_LOCATIONS = new ArrayList<DataLocation>(2);
		DataLocation loc = new DataLocation();
		loc.setRegionID('T');
		loc.setRowNumber(345);
		loc.setDataDate(new Date(3458139048000L));
		loc.setLongitude(-179.5);
		loc.setLatitude(3.5);
		loc.setDataValue(1105.450);
		MY_LOCATIONS.add(loc);
		loc = new DataLocation();
		loc.setRegionID('T');
		loc.setRowNumber(346);
		loc.setDataDate(new Date(3458139203000L));
		loc.setLongitude(-179.6);
		loc.setLatitude(3.4);
		loc.setDataValue(1105.453);
		MY_LOCATIONS.add(loc);
	}
	private static final Date MY_FLAG_DATE = new Date();
	private static final String MY_USERNAME = "Karl.Smith";
	private static final String MY_REALNAME = "Karl M. Smith";
	private static final String MY_COMMENT = "from SocatWoceEvent unit test";

	/**
	 * Test method for {@link gov.noaa.pmel.socat.dashboard.shared.SocatEvent#getFlag()} 
	 * and {@link gov.noaa.pmel.socat.dashboard.shared.SocatEvent#setFlag(java.lang.String)}.
	 */
	@Test
	public void testGetSetQcFlag() {
		SocatWoceEvent myflag = new SocatWoceEvent();
		assertEquals(SocatCruiseData.CHAR_MISSING_VALUE, myflag.getFlag());
		myflag.setFlag(MY_WOCE_FLAG);
		assertEquals(MY_WOCE_FLAG, myflag.getFlag());
		myflag.setFlag(null);
		assertEquals(SocatCruiseData.CHAR_MISSING_VALUE, myflag.getFlag());
	}

	/**
	 * Test method for {@link gov.noaa.pmel.socat.dashboard.shared.SocatEvent#getExpocode()} 
	 * and {@link gov.noaa.pmel.socat.dashboard.shared.SocatEvent#setExpocode(java.lang.String)}.
	 */
	@Test
	public void testGetSetExpocode() {
		SocatWoceEvent myflag = new SocatWoceEvent();
		assertEquals("", myflag.getExpocode());
		myflag.setExpocode(MY_EXPOCODE);
		assertEquals(MY_EXPOCODE, myflag.getExpocode());
		assertEquals(SocatCruiseData.CHAR_MISSING_VALUE, myflag.getFlag());
		myflag.setExpocode(null);
		assertEquals("", myflag.getExpocode());
	}

	/**
	 * Test method for {@link gov.noaa.pmel.socat.dashboard.shared.SocatEvent#getSocatVersion()} 
	 * and {@link gov.noaa.pmel.socat.dashboard.shared.SocatEvent#setSocatVersion(java.lang.Double)}.
	 */
	@Test
	public void testGetSetSocatVersion() {
		SocatWoceEvent myflag = new SocatWoceEvent();
		assertEquals(0.0, myflag.getSocatVersion(), 1.0E-6);
		myflag.setSocatVersion(MY_SOCAT_VERSION);
		assertEquals(MY_SOCAT_VERSION, myflag.getSocatVersion(), 1.0E-6);
		assertEquals("", myflag.getExpocode());
		assertEquals(SocatCruiseData.CHAR_MISSING_VALUE, myflag.getFlag());
		myflag.setSocatVersion(null);
		assertEquals(0.0, myflag.getSocatVersion(), 1.0E-6);
	}

	/**
	 * Test method for {@link gov.noaa.pmel.socat.dashboard.shared.SocatWoceEvent#getDataType()} 
	 * and {@link gov.noaa.pmel.socat.dashboard.shared.SocatWoceEvent#setDataType(gov.noaa.pmel.socat.dashboard.shared.DataColumnType)}.
	 */
	@Test
	public void testGetSetDataType() {
		SocatWoceEvent myflag = new SocatWoceEvent();
		assertEquals(DataColumnType.UNKNOWN, myflag.getDataType());
		myflag.setDataType(MY_DATA_TYPE);
		assertEquals(MY_DATA_TYPE, myflag.getDataType());
		assertEquals(0.0, myflag.getSocatVersion(), 1.0E-6);
		assertEquals("", myflag.getExpocode());
		assertEquals(SocatCruiseData.CHAR_MISSING_VALUE, myflag.getFlag());
		myflag.setDataType(null);
		assertEquals(DataColumnType.UNKNOWN, myflag.getDataType());
	}

	/**
	 * Test method for {@link gov.noaa.pmel.socat.dashboard.shared.SocatWoceEvent#getColumnName()} 
	 * and {@link gov.noaa.pmel.socat.dashboard.shared.SocatWoceEvent#setColumnName(java.lang.String)}.
	 */
	@Test
	public void testGetSetColumnName() {
		SocatWoceEvent myflag = new SocatWoceEvent();
		assertEquals("", myflag.getColumnName());
		myflag.setColumnName(MY_COLUMN_NAME);
		assertEquals(MY_COLUMN_NAME, myflag.getColumnName());
		assertEquals(DataColumnType.UNKNOWN, myflag.getDataType());
		assertEquals(0.0, myflag.getSocatVersion(), 1.0E-6);
		assertEquals("", myflag.getExpocode());
		assertEquals(SocatCruiseData.CHAR_MISSING_VALUE, myflag.getFlag());
		myflag.setColumnName(null);
		assertEquals("", myflag.getColumnName());
	}

	/**
	 * Test method for {@link gov.noaa.pmel.socat.dashboard.shared.SocatWoceEvent#getLocations()} 
	 * and {@link gov.noaa.pmel.socat.dashboard.shared.SocatWoceEvent#setLocations(java.util.ArrayList)}.
	 */
	@Test
	public void testGetSetLocations() {
		SocatWoceEvent myflag = new SocatWoceEvent();
		assertEquals(0, myflag.getLocations().size());
		myflag.setLocations(MY_LOCATIONS);
		assertEquals(MY_LOCATIONS, myflag.getLocations());
		assertEquals("", myflag.getColumnName());
		assertEquals(DataColumnType.UNKNOWN, myflag.getDataType());
		assertEquals(0.0, myflag.getSocatVersion(), 1.0E-6);
		assertEquals("", myflag.getExpocode());
		assertEquals(SocatCruiseData.CHAR_MISSING_VALUE, myflag.getFlag());
		myflag.setLocations(null);
		assertEquals(0, myflag.getLocations().size());
	}

	/**
	 * Test method for {@link gov.noaa.pmel.socat.dashboard.shared.SocatEvent#getFlagDate()} 
	 * and {@link gov.noaa.pmel.socat.dashboard.shared.SocatEvent#setFlagDate(java.util.Date)}.
	 */
	@Test
	public void testGetSetFlagDate() {
		SocatWoceEvent myflag = new SocatWoceEvent();
		assertEquals(SocatMetadata.DATE_MISSING_VALUE, myflag.getFlagDate());
		myflag.setFlagDate(MY_FLAG_DATE);
		assertEquals(MY_FLAG_DATE, myflag.getFlagDate());
		assertEquals(0, myflag.getLocations().size());
		assertEquals("", myflag.getColumnName());
		assertEquals(DataColumnType.UNKNOWN, myflag.getDataType());
		assertEquals(0.0, myflag.getSocatVersion(), 1.0E-6);
		assertEquals("", myflag.getExpocode());
		assertEquals(SocatCruiseData.CHAR_MISSING_VALUE, myflag.getFlag());
		myflag.setFlagDate(null);
		assertEquals(SocatMetadata.DATE_MISSING_VALUE, myflag.getFlagDate());
	}

	/**
	 * Test method for {@link gov.noaa.pmel.socat.dashboard.shared.SocatEvent#getUsername()} 
	 * and {@link gov.noaa.pmel.socat.dashboard.shared.SocatEvent#setUsername(java.lang.String)}.
	 */
	@Test
	public void testGetSetUsername() {
		SocatWoceEvent myflag = new SocatWoceEvent();
		assertEquals("", myflag.getUsername());
		myflag.setUsername(MY_USERNAME);
		assertEquals(MY_USERNAME, myflag.getUsername());
		assertEquals(SocatMetadata.DATE_MISSING_VALUE, myflag.getFlagDate());
		assertEquals(0, myflag.getLocations().size());
		assertEquals("", myflag.getColumnName());
		assertEquals(DataColumnType.UNKNOWN, myflag.getDataType());
		assertEquals(0.0, myflag.getSocatVersion(), 1.0E-6);
		assertEquals("", myflag.getExpocode());
		assertEquals(SocatCruiseData.CHAR_MISSING_VALUE, myflag.getFlag());
		myflag.setUsername(null);
		assertEquals("", myflag.getUsername());
	}

	/**
	 * Test method for {@link gov.noaa.pmel.socat.dashboard.shared.SocatEvent#getRealname()} 
	 * and {@link gov.noaa.pmel.socat.dashboard.shared.SocatEvent#setRealname(java.lang.String)}.
	 */
	@Test
	public void testGetSetRealname() {
		SocatWoceEvent myflag = new SocatWoceEvent();
		assertEquals("", myflag.getRealname());
		myflag.setRealname(MY_REALNAME);
		assertEquals(MY_REALNAME, myflag.getRealname());
		assertEquals("", myflag.getUsername());
		assertEquals(SocatMetadata.DATE_MISSING_VALUE, myflag.getFlagDate());
		assertEquals(0, myflag.getLocations().size());
		assertEquals("", myflag.getColumnName());
		assertEquals(DataColumnType.UNKNOWN, myflag.getDataType());
		assertEquals(0.0, myflag.getSocatVersion(), 1.0E-6);
		assertEquals("", myflag.getExpocode());
		assertEquals(SocatCruiseData.CHAR_MISSING_VALUE, myflag.getFlag());
		myflag.setRealname(null);
		assertEquals("", myflag.getRealname());
	}

	/**
	 * Test method for {@link gov.noaa.pmel.socat.dashboard.shared.SocatEvent#getComment()} 
	 * and {@link gov.noaa.pmel.socat.dashboard.shared.SocatEvent#setComment(java.lang.String)}.
	 */
	@Test
	public void testGetSetComment() {
		SocatWoceEvent myflag = new SocatWoceEvent();
		assertEquals("", myflag.getComment());
		myflag.setComment(MY_COMMENT);
		assertEquals(MY_COMMENT, myflag.getComment());
		assertEquals("", myflag.getRealname());
		assertEquals("", myflag.getUsername());
		assertEquals(SocatMetadata.DATE_MISSING_VALUE, myflag.getFlagDate());
		assertEquals(0, myflag.getLocations().size());
		assertEquals("", myflag.getColumnName());
		assertEquals(DataColumnType.UNKNOWN, myflag.getDataType());
		assertEquals(0.0, myflag.getSocatVersion(), 1.0E-6);
		assertEquals("", myflag.getExpocode());
		assertEquals(SocatCruiseData.CHAR_MISSING_VALUE, myflag.getFlag());
		myflag.setComment(null);
		assertEquals("", myflag.getComment());
	}

	/**
	 * Test method for {@link gov.noaa.pmel.socat.dashboard.shared.SocatWoceEvent#hashCode()}
	 * and {@link gov.noaa.pmel.socat.dashboard.shared.SocatWoceEvent#equals(java.lang.Object)}.
	 */
	@Test
	public void testHashCodeEqualsObject() {
		SocatWoceEvent myflag = new SocatWoceEvent();
		assertFalse( myflag.equals(null) );
		assertFalse( myflag.equals(new SocatEvent()) );

		SocatWoceEvent otherflag = new SocatWoceEvent();
		assertTrue( myflag.hashCode() == otherflag.hashCode() );
		assertTrue( myflag.equals(otherflag) );

		myflag.setFlag(MY_WOCE_FLAG);
		assertFalse( myflag.hashCode() == otherflag.hashCode() );
		assertFalse( myflag.equals(otherflag) );
		otherflag.setFlag(MY_WOCE_FLAG);
		assertTrue( myflag.hashCode() == otherflag.hashCode() );
		assertTrue( myflag.equals(otherflag) );

		myflag.setExpocode(MY_EXPOCODE);
		assertFalse( myflag.hashCode() == otherflag.hashCode() );
		assertFalse( myflag.equals(otherflag) );
		otherflag.setExpocode(MY_EXPOCODE);
		assertTrue( myflag.hashCode() == otherflag.hashCode() );
		assertTrue( myflag.equals(otherflag) );

		myflag.setSocatVersion(MY_SOCAT_VERSION);
		// socatVersion is ignored in the hash code
		assertTrue( myflag.hashCode() == otherflag.hashCode() );
		assertFalse( myflag.equals(otherflag) );
		otherflag.setSocatVersion(MY_SOCAT_VERSION);
		assertTrue( myflag.hashCode() == otherflag.hashCode() );
		assertTrue( myflag.equals(otherflag) );

		myflag.setDataType(MY_DATA_TYPE);
		assertFalse( myflag.hashCode() == otherflag.hashCode() );
		assertFalse( myflag.equals(otherflag) );
		otherflag.setDataType(MY_DATA_TYPE);
		assertTrue( myflag.hashCode() == otherflag.hashCode() );
		assertTrue( myflag.equals(otherflag) );

		myflag.setColumnName(MY_COLUMN_NAME);
		assertFalse( myflag.hashCode() == otherflag.hashCode() );
		assertFalse( myflag.equals(otherflag) );
		otherflag.setColumnName(MY_COLUMN_NAME);
		assertTrue( myflag.hashCode() == otherflag.hashCode() );
		assertTrue( myflag.equals(otherflag) );

		myflag.setLocations(MY_LOCATIONS);
		assertFalse( myflag.hashCode() == otherflag.hashCode() );
		assertFalse( myflag.equals(otherflag) );
		otherflag.setLocations(MY_LOCATIONS);
		assertTrue( myflag.hashCode() == otherflag.hashCode() );
		assertTrue( myflag.equals(otherflag) );

		myflag.setFlagDate(MY_FLAG_DATE);
		assertFalse( myflag.hashCode() == otherflag.hashCode() );
		assertFalse( myflag.equals(otherflag) );
		otherflag.setFlagDate(MY_FLAG_DATE);
		assertTrue( myflag.hashCode() == otherflag.hashCode() );
		assertTrue( myflag.equals(otherflag) );

		myflag.setUsername(MY_USERNAME);
		assertFalse( myflag.hashCode() == otherflag.hashCode() );
		assertFalse( myflag.equals(otherflag) );
		otherflag.setUsername(MY_USERNAME);
		assertTrue( myflag.hashCode() == otherflag.hashCode() );
		assertTrue( myflag.equals(otherflag) );

		myflag.setRealname(MY_REALNAME);
		assertFalse( myflag.hashCode() == otherflag.hashCode() );
		assertFalse( myflag.equals(otherflag) );
		otherflag.setRealname(MY_REALNAME);
		assertTrue( myflag.hashCode() == otherflag.hashCode() );
		assertTrue( myflag.equals(otherflag) );

		myflag.setComment(MY_COMMENT);
		assertFalse( myflag.hashCode() == otherflag.hashCode() );
		assertFalse( myflag.equals(otherflag) );
		otherflag.setComment(MY_COMMENT);
		assertTrue( myflag.hashCode() == otherflag.hashCode() );
		assertTrue( myflag.equals(otherflag) );
	}

}

package socat.sanitychecker.data.conversion;

/**
 * Performs temperature conversions
 */
public class LongitudeConverter extends AnyUnitsConverter {

	/**
	 * Dummy constructor - nothing to be done here!
	 */
	public LongitudeConverter() {
		// Nothing to do!
	}
	
	@Override
	public String convert(String value, String units) {
		
		String result = value;
		
		// SOCAT uses negative longitudes for degrees west
		Double sourceLon = Double.parseDouble(value);
		if (sourceLon > 180) {
			result = Double.toString((360 - sourceLon) * -1);
		}
		
		return result;
	}
}
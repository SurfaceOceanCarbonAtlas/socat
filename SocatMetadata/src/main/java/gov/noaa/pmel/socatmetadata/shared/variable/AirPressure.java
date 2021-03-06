package gov.noaa.pmel.socatmetadata.shared.variable;

import com.google.gwt.user.client.rpc.IsSerializable;
import gov.noaa.pmel.socatmetadata.shared.core.Duplicable;

import java.io.Serializable;

/**
 * Information about an air pressure measurement.
 * The default unit is hectopascals instead of empty.
 * Also provides a pressure correction field.
 */
public class AirPressure extends InstData implements Duplicable, Serializable, IsSerializable {

    private static final long serialVersionUID = -2487294094448486927L;

    public static final String HECTOPASCALS_UNIT = "hPa";

    private String pressureCorrection;

    /**
     * Create with all fields empty except for units which are {@link #HECTOPASCALS_UNIT}
     */
    public AirPressure() {
        super();
        super.setVarUnit(HECTOPASCALS_UNIT);
        pressureCorrection = "";
    }

    /**
     * Create using as many of the values in the given variable subclass as possible,
     * except for units which are {@link #HECTOPASCALS_UNIT}
     */
    public AirPressure(Variable var) {
        super(var);
        if ( var instanceof AirPressure ) {
            AirPressure press = (AirPressure) var;
            pressureCorrection = press.pressureCorrection;
        }
        else {
            super.setVarUnit(HECTOPASCALS_UNIT);
            pressureCorrection = "";
        }
    }

    /**
     * @return the pressure correction information; never null but may be empty
     */
    public String getPressureCorrection() {
        return pressureCorrection;
    }

    /**
     * @param pressureCorrection
     *         assign as the pressure correction string; if null, and empty string is assigned
     */
    public void setPressureCorrection(String pressureCorrection) {
        this.pressureCorrection = (pressureCorrection != null) ? pressureCorrection.trim() : "";
    }

    /**
     * @param varUnit
     *         assign as the unit for values of this variable as well as the accuracy and precision;
     *         if null or blank, hectopascals is assigned
     */
    @Override
    public void setVarUnit(String varUnit) {
        if ( (varUnit == null) || (varUnit.trim().isEmpty()) )
            super.setVarUnit(HECTOPASCALS_UNIT);
        else
            super.setVarUnit(varUnit);
    }

    @Override
    public Object duplicate(Object dup) {
        AirPressure press;
        if ( dup == null )
            press = new AirPressure();
        else
            press = (AirPressure) dup;
        super.duplicate(press);
        press.pressureCorrection = pressureCorrection;
        return press;
    }

    @Override
    public int hashCode() {
        final int prime = 37;
        int result = super.hashCode();
        result = result * prime + pressureCorrection.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( null == obj )
            return false;
        if ( !(obj instanceof AirPressure) )
            return false;
        if ( !super.equals(obj) )
            return false;

        AirPressure other = (AirPressure) obj;

        if ( !pressureCorrection.equals(other.pressureCorrection) )
            return false;

        return true;
    }

    @Override
    public String toString() {
        String repr = super.toString().replaceFirst(super.getSimpleName(), getSimpleName());
        return repr.substring(0, repr.length() - 2) +
                ", pressureCorrection='" + pressureCorrection + "'" +
                " }";
    }

    @Override
    public String getSimpleName() {
        return "AirPressure";
    }

}

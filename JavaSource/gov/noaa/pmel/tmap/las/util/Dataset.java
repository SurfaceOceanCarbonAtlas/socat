/**
 * 
 */
package gov.noaa.pmel.tmap.las.util;

import gov.noaa.pmel.tmap.las.client.VariableSerializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

/**
 * @author Roland Schweitzer
 *
 */
public class Dataset extends Container implements DatasetInterface {
    public Dataset(Element dataset) {
        super(dataset);
    }
    /* (non-Javadoc)
     * @see gov.noaa.pmel.tmap.las.util.DatasetInterface#getXPath()
     */
    public String getXPath() {
        return "/lasdata/datasets/dataset@[ID='"+getID()+"']";
    }
    public String getDoc() {
    	return element.getAttributeValue("doc");
    }
    public VariableSerializable[] getVariablesSerializable() {
    	List variables = element.getChild("variables").getChildren("variable");
    	VariableSerializable[] vs = new VariableSerializable[variables.size()];
    	int i = 0;
    	for (Iterator varIt = variables.iterator(); varIt.hasNext();) {
			Element var = (Element) varIt.next();
			Variable v = new Variable(var, getID(), getName());
			vs[i] = v.getVariableSerializable();
		    i++;	
		}
    	return vs;
    }
}

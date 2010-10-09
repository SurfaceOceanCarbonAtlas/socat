package gov.noaa.pmel.tmap.las.client;


import java.util.HashMap;
import java.util.List;

import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.ConfigSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.DatasetSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.OptionSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.RegionSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;

import com.google.gwt.user.client.rpc.RemoteService;

public interface RPCService extends RemoteService {
	/**
	 * 
	 */
	public CategorySerializable[] getCategories(String id) throws RPCException;
	public VariableSerializable getVariable(String dsid, String varid) throws RPCException;
	public GridSerializable getGrid(String dsID, String varID) throws RPCException;
	public OperationSerializable[] getOperations(String view, String dsID, String varID) throws RPCException;
	public OperationSerializable[] getOperations(String view, String[] xpath) throws RPCException;
	public OptionSerializable[] getOptions(String opid) throws RPCException;
	public CategorySerializable[] getTimeSeries() throws RPCException;
	public HashMap<String, String> getPropertyGroup(String name) throws RPCException;
	public RegionSerializable[] getRegions(String dsid, String varid) throws RPCException;
	public ConfigSerializable getConfig(String view, String dsid, String varid) throws RPCException;
}

/*
 * Generated by MyEclipse Struts
 * Template path: templates/java/JavaClass.vtl
 */
package gov.noaa.pmel.tmap.las.ui;

import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.product.server.LASConfigPlugIn;
import gov.noaa.pmel.tmap.las.util.Container;
import gov.noaa.pmel.tmap.las.util.ContainerComparator;
import gov.noaa.pmel.tmap.las.util.NameValuePair;
import gov.noaa.pmel.tmap.las.util.Option;
import gov.noaa.pmel.tmap.las.util.View;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.tools.ant.taskdefs.SendEmail;
import org.jdom.JDOMException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

/** 
 * MyEclipse Struts
 * Creation date: 05-04-2007
 * 
 * XDoclet definition:
 * @struts.action validate="true"
 */
public class GetViews extends ConfigService {
    /*
     * Generated Methods
     */
    private static Logger log = LogManager.getLogger(GetViews.class.getName());

    /** 
     * Method execute
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) {
        LASConfig lasConfig = (LASConfig)servlet.getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
        String dsID = request.getParameter("dsid");
        String varID = request.getParameter("varid");
        String[] xpath = request.getParameterValues("xpath");
        String format = request.getParameter("format");        
        if ( format == null ) {
            format = "json";
        }
        
        try {
        	ArrayList<View> views;
        	if ( xpath != null && xpath.length > 0 ) {
				log.info("Starting xpaths: getViews");
				views = lasConfig.getViewsByXpath(xpath);
        	} else {
        		log.info("Starting: getViews.do?dsid="+dsID+"&varid="+varID+"&format="+format);
               views = lasConfig.getViewsByDatasetAndVariable(dsID, varID);
        	}
            //Collections.sort(views, new ContainerComparator("value"));
            PrintWriter respout = response.getWriter();
            if ( format.equals("xml") ) {
                response.setContentType("application/xml");
                respout.print(Util.toXML(views, "views"));
            } else {
                response.setContentType("application/json");
                //JSONObject json_response = Util.toJSON_keep_array(views, "views");
                JSONObject json_response = toJSON(views, "views");
                log.debug(json_response.toString(3));
                json_response.write(respout);
            }
            // Catch for IOException, JSONException and JDOMException and anything unexpected.
        } catch (Exception e) {
            sendError(response, "views", format, e.toString());
        }
        log.info("Finished: getViews.do?dsid="+dsID+"&varid="+varID+"&format="+format);		
        return null;
    }
    public JSONObject toJSON(ArrayList<View> views, String wrapper) throws JSONException {
        JSONObject json_response = new JSONObject();
        JSONObject views_object = new JSONObject();
        for (Iterator viewIt = views.iterator(); viewIt.hasNext();) {
            View view = (View) viewIt.next();
            JSONObject view_object = view.toJSON();            
            views_object.array_accumulate("view", view_object);
        }
        views_object.put("status", "ok");
        views_object.put("error", "");
        json_response.put("views", views_object);
        return json_response;
    }
}

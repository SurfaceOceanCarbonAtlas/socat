/*
 * Generated by MyEclipse Struts
 * Template path: templates/java/JavaClass.vtl
 */
package gov.noaa.pmel.tmap.las.ui;

import gov.noaa.pmel.tmap.las.exception.LASException;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.product.server.LASConfigPlugIn;
import gov.noaa.pmel.tmap.las.ui.json.JSONUtil;
import gov.noaa.pmel.tmap.las.util.Category;
import gov.noaa.pmel.tmap.las.util.Dataset;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

/** 
 * MyEclipse Struts
 * Creation date: 01-05-2007
 * 
 * XDoclet definition:
 * @struts.action validate="true"
 */
public class GetDatasets extends ConfigService {
    /*
     * Generated Methods
     */

    /** 
     * Method execute
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     */
    private static Logger log = LogManager.getLogger(GetDatasets.class.getName());
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) {
		String query = request.getQueryString();
		if ( query != null ) {
			try{
				query = URLDecoder.decode(query, "UTF-8");
				log.info("START: "+request.getRequestURL()+"?"+query);
			} catch (UnsupportedEncodingException e) {
				// Don't care we missed a log message.
			}			
		} else {
			log.info("START: "+request.getRequestURL());
		}
        String format = request.getParameter("format");
        if ( format == null ) {
            format = "json";
        }
        
        // Get the LASConfig (sub-class of JDOM Document) from the servlet context.
        log.debug("Processing request for dataset list.");
        LASConfig lasConfig = (LASConfig)servlet.getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY); 
                
        ArrayList<Category> datasets = new ArrayList<Category>();
		try {
			datasets = lasConfig.getDatasets();
		} catch (JDOMException e) {
			sendError(response, "<datasets>", format, e.getMessage());
		} catch (LASException e) {
			sendError(response, "<datasets>", format, e.getMessage());
		}
        StringBuffer xml = new StringBuffer();
        
        xml.append("<datasets>");
        for (Iterator dsIt = datasets.iterator(); dsIt.hasNext();) {
            Dataset ds = (Dataset) dsIt.next();
            xml.append(ds.toXML());
        }
        xml.append("</datasets>");
        
        try {
            PrintWriter respout = response.getWriter();
            if (format.equals("xml")) {
                response.setContentType("application/xml");
                respout.print(xml.toString());
            } else {
                response.setContentType("application/json");
                JSONObject json_response = XML.toJSONObject(xml.toString());
                log.debug(json_response.toString(3));
                json_response.write(respout);
            }
        } catch (IOException e) {
        	sendError(response, "<datasets>", format, e.getMessage());
        } catch (JSONException e) {
        	sendError(response, "<datasets>", format, e.getMessage());
        }   
        if ( query != null ) {
			log.info("END:   "+request.getRequestURL()+"?"+query);						
		} else {
			log.info("END:   "+request.getRequestURL());
		}
        return null;
    }
}
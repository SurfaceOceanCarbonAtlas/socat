/*
 * Generated by MyEclipse Struts
 * Template path: templates/java/JavaClass.vtl
 */
package gov.noaa.pmel.tmap.las.ui;

import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.product.server.InitThread;
import gov.noaa.pmel.tmap.las.product.server.LASConfigPlugIn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts2.ServletActionContext;

/** 
 * MyEclipse Struts
 * Creation date: 01-08-2007
 * 
 * XDoclet definition:
 * @struts.action validate="true"
 */
public class GetUI7 extends ConfigService {
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
    private static Logger log = LoggerFactory.getLogger(GetUI.class.getName());
    private static String V7UI = "V7UI";
    private static String LAZY_START = "lazy_start";
    public String execute() throws Exception {    
        String lazy_start = (String) contextAttributes.get(LASConfigPlugIn.LAS_LAZY_START_KEY);
        String data_url = request.getParameter("data_url");
        LASConfig lasConfig = (LASConfig) contextAttributes.get(LASConfigPlugIn.LAS_CONFIG_KEY);
        if ( lazy_start != null && lazy_start.equals("true") ) {
        	// Start the initialization and forward to lazy start page
        	InitThread thread = new InitThread(ServletActionContext.getServletContext());
        	thread.start();
        	return LAZY_START;
        } else {
        	// forward to the UI
        	
        	if ( data_url != null && !data_url.equals("") ) {
        		String ids = lasConfig.getIDs(data_url);
        		response.sendRedirect("getUI.do?"+ids);
        	} 
        	return V7UI;
        }
    }
}

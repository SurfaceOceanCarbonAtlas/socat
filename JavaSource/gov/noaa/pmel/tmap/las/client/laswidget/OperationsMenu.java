package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
/**
 * A widget that shows the "non-plot" operations from an LAS (like Google Earth, animation, etc).
 * @author rhs
 *
 */
public class OperationsMenu extends Composite {
	/*
	 * The way this menu is used in the old UI is that all of the buttons appear all at once on the UI, but they are disabled.
	 * Then when the state changes, those buttons which apply to to the new state are enabled.
	 */
	HorizontalPanel buttonBar;
	OperationPushButton animationButton = new OperationPushButton("Animate");
	OperationPushButton compareButton = new OperationPushButton("Compare");
	OperationPushButton correlationButton = new OperationPushButton("Correlation Viewer");
	OperationPushButton googleEarthButton = new OperationPushButton("Google Earth");
	OperationPushButton showValuesButton = new OperationPushButton("Show Values");
	OperationPushButton exportToDesktopButton = new OperationPushButton("Export to Desktop Application");
	OperationPushButton saveAsButton = new OperationPushButton("Save As...");
	OperationPushButton climateAnalysis = new OperationPushButton("Climate Analysis...");

	boolean hasComparison = false;
	boolean hasAnimation = false;
	boolean hasCorrelation = false;
	boolean hasGoogleEarth = false;
	ClickHandler clickHandler;
	public OperationsMenu() {
		buttonBar = new HorizontalPanel();
		buttonBar.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        turnOffButtons();
        animationButton.addStyleDependentName("SMALLER");
        animationButton.setTitle("Interactive interface for making a sequence of plots over time.");
        compareButton.addStyleDependentName("SMALLER");
        correlationButton.addStyleDependentName("SMALLER");
        correlationButton.setTitle("Beta interface to make a scatter plot of a property vs. another property.");
        googleEarthButton.addStyleDependentName("SMALLER");
        googleEarthButton.setTitle("View a plot of data draped over the globe using Google Earth.");
        showValuesButton.addStyleDependentName("SMALLER");
        showValuesButton.setTitle("Look at the data values in a new window.");
        exportToDesktopButton.addStyleDependentName("SMALLER");
        exportToDesktopButton.setTitle("Get a few lines of native script for various analysis packages.");
        saveAsButton.addStyleDependentName("SMALLER");
        saveAsButton.ensureDebugId("saveAsButton");
        saveAsButton.setTitle("Save data in various text and binary formats.");
        climateAnalysis.addStyleDependentName("SMALLER");
        climateAnalysis.setTitle("Perform time average spectrum and other advanced analysis.");
		buttonBar.add(animationButton);
		buttonBar.add(correlationButton);
		buttonBar.add(googleEarthButton);
		buttonBar.add(showValuesButton);
		buttonBar.add(exportToDesktopButton);
		buttonBar.add(saveAsButton);
		buttonBar.add(climateAnalysis);
		climateAnalysis.setVisible(false);
		initWidget(buttonBar);
		buttonBar.setSize("100%", "100%");
	}
    private void turnOffButtons() {
		animationButton.setEnabled(false);
		compareButton.setEnabled(false);
		googleEarthButton.setEnabled(false);
		showValuesButton.setEnabled(false);
		exportToDesktopButton.setEnabled(false);
		saveAsButton.setEnabled(false);
		correlationButton.setEnabled(false);
	    climateAnalysis.setEnabled(false);

    }
	public void setMenus(OperationSerializable[] ops, String view) {
		turnOffButtons();
		hasComparison = false;
		hasAnimation = false;
		hasGoogleEarth = false;
		hasCorrelation = false;
		
	
	    for (int i = 0; i < ops.length; i++) {
			OperationSerializable op = ops[i];
			String category = op.getAttributes().get("category").toLowerCase();
			List<String> views = op.getViews();
			for (Iterator viewIt = views.iterator(); viewIt.hasNext();) {
				String op_view = (String) viewIt.next();
				if ( category.equals("comparison")) {
					if ( op.getName().toLowerCase().contains("compar") ) {
						if ( op_view.equals(view) ) {
							if ( (op.getAttributes().get("private") == null || !op.getAttributes().get("private").equalsIgnoreCase("true") ) ) {
								if ( !hasComparison ) {
									hasComparison = true;
									compareButton.setOperation(op);
									compareButton.setEnabled(true);
								}
							}
						}
					}
				} else if ( category.contains("animation") ) {
					if ( op_view.equals(view) ) {
						if ( (op.getAttributes().get("private") == null || !op.getAttributes().get("private").equalsIgnoreCase("true") ) 
								&& ( op.getAttributes().get("default") != null && op.getAttributes().get("default").equalsIgnoreCase("true") ) ) {	
							if ( !hasAnimation ) {
								hasAnimation = true;
								animationButton.setOperation(op);
								animationButton.setEnabled(true);
							}
						}
					}
				} else if ( category.contains("correlation") ) {
                    if ( op_view.equals(view) ) {
                        if ( (op.getAttributes().get("private") == null || !op.getAttributes().get("private").equalsIgnoreCase("true") ) 
                                && ( op.getAttributes().get("default") != null && op.getAttributes().get("default").equalsIgnoreCase("true") ) ) {  
                            if ( !hasCorrelation ) {
                                hasCorrelation = true;
                                correlationButton.setOperation(op);
                                correlationButton.setEnabled(true);
                            }
                        }
                    }
				} else if ( category.contains("globe") ) {
					if ( op_view.equals(view) ) {
						if ( (op.getAttributes().get("private") == null || !op.getAttributes().get("private").equalsIgnoreCase("true") ) 
								&& ( op.getAttributes().get("default") != null && op.getAttributes().get("default").equalsIgnoreCase("true") ) ) {
							if ( !hasGoogleEarth ) {
								hasGoogleEarth = true;
								googleEarthButton.setOperation(op);
								googleEarthButton.setEnabled(true);
							}
						}
					}
				} else if ( category.contains("table") ) {
					if ( op_view.equals(view) ) {
						if ( (op.getAttributes().get("private") == null || !op.getAttributes().get("private").equalsIgnoreCase("true") ) ) {
							if ( op.getName().toLowerCase().contains("values") ) {
								showValuesButton.setOperation(op);
								showValuesButton.setEnabled(true);
							}
							if ( op.getName().toLowerCase().contains("download") ) {
								saveAsButton.setOperation(op);
								saveAsButton.setEnabled(true);
							}
						}
					}
				} else if ( category.equals("file") ) {
					if ( op_view.equals(view) ) {
						if ( (op.getAttributes().get("private") == null || !op.getAttributes().get("private").equalsIgnoreCase("true") ) ) {
							if ( op.getName().toLowerCase().contains("script") ) {
								exportToDesktopButton.setOperation(op);
								exportToDesktopButton.setEnabled(true);
							}
						}
					}
				} else if ( category.equals("climate_analysis") ) {
				    if ( op_view.equals(view) ) {
				        if ( (op.getAttributes().get("private") == null || !op.getAttributes().get("private").equalsIgnoreCase("true") ) ) {
                            if ( op.getName().toLowerCase().contains("climate analysis") ) {
                                climateAnalysis.setOperation(op);
                                climateAnalysis.setVisible(true);
                                climateAnalysis.setEnabled(true);
                            }
				        }
				    }
				}
			}
		}
	}
    public void addClickHandler(ClickHandler clickHandler) {
    	this.clickHandler = clickHandler;   	
    	compareButton.addClickHandler(clickHandler);
        animationButton.addClickHandler(clickHandler);
        correlationButton.addClickHandler(clickHandler);
        googleEarthButton.addClickHandler(clickHandler);
        showValuesButton.addClickHandler(clickHandler);
        saveAsButton.addClickHandler(clickHandler);
        exportToDesktopButton.addClickHandler(clickHandler);
        climateAnalysis.addClickHandler(clickHandler);
    }
    public void setGoogleEarthButtonEnabled(boolean enable) {
        googleEarthButton.setEnabled(enable);
    }
    public void setCorrelationButtonEnabled(boolean b) {
        correlationButton.setEnabled(b);
    }
    public void enableByView(String view, boolean hasT) {
        if ( animationButton.getOperation() != null && animationButton.getOperation().getViews().contains(view) && hasT ) {
            animationButton.setEnabled(true);
        } else {
            animationButton.setEnabled(false);
        }
        if ( compareButton.getOperation() != null && compareButton.getOperation().getViews().contains(view) ) {
            compareButton.setEnabled(true);
        } else {
            compareButton.setEnabled(false);
        }
        if ( googleEarthButton.getOperation() != null && googleEarthButton.getOperation().getViews().contains(view) ) {
            googleEarthButton.setEnabled(true);
        } else {
            googleEarthButton.setEnabled(false);
        }
        if ( showValuesButton.getOperation() != null && showValuesButton.getOperation().getViews().contains(view) ) {
            showValuesButton.setEnabled(true);
        } else {
            showValuesButton.setEnabled(false);
        }
        if ( exportToDesktopButton.getOperation() != null && exportToDesktopButton.getOperation().getViews().contains(view) ) {
            exportToDesktopButton.setEnabled(true);

        } else {
            exportToDesktopButton.setEnabled(false);
        }
        if ( saveAsButton.getOperation() != null && saveAsButton.getOperation().getViews().contains(view) ) {
            saveAsButton.setEnabled(true);
        } else {
            saveAsButton.setEnabled(false);
        }
        if ( correlationButton.getOperation() != null && correlationButton.getOperation().getViews().contains(view) ) {
            correlationButton.setEnabled(true);
        } else {
            correlationButton.setEnabled(false);
        }
        String profile = DOM.getElementProperty(DOM.getElementById("las-profile"), "content");
        if (profile != null && profile.equals("LAS-ESGF")) {
            if ( climateAnalysis != null && climateAnalysis.getOperation() != null && climateAnalysis.getOperation().getViews().contains(view) ) {
                climateAnalysis.setEnabled(true);
            } else {
                climateAnalysis.setVisible(false);
            }
        } else {
            climateAnalysis.setVisible(false);
        }
        
    }
}

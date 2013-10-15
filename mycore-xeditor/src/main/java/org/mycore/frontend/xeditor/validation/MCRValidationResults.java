package org.mycore.frontend.xeditor.validation;

import java.util.HashMap;
import java.util.Map;

import org.mycore.common.MCRConfiguration;

public class MCRValidationResults {

    private static final String MARKER_DEFAULT;

    private static final String MARKER_SUCCESS;

    private static final String MARKER_ERROR;

    static {
        String prefix = "MCR.XEditor.Validation.Marker.";
        MARKER_DEFAULT = MCRConfiguration.instance().getString(prefix + "default", "");
        MARKER_SUCCESS = MCRConfiguration.instance().getString(prefix + "success", "has-success");
        MARKER_ERROR = MCRConfiguration.instance().getString(prefix + "error", "has-error");
    }

    private Map<String, String> xPath2Marker = new HashMap<String, String>();

    private boolean isValid = true;

    public boolean hasError(String xPath) {
        return MARKER_ERROR.equals(xPath2Marker.get(xPath));
    }

    public void mark(String xPath, boolean isValid) {
        if (hasError(xPath))
            return;

        xPath2Marker.put(xPath, isValid ? MARKER_SUCCESS : MARKER_ERROR);
        this.isValid = this.isValid && isValid;
    }

    public String getValidationMarker(String xPath) {
        return xPath2Marker.containsKey(xPath) ? xPath2Marker.get(xPath) : MARKER_DEFAULT;
    }

    public boolean isValid() {
        return isValid;
    }
}
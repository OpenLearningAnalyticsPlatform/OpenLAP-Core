package de.rwthaachen.openlap.analyticsmodules.exceptions;

import de.rwthaachen.openlap.OpenLAPCoreApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lechip on 29/11/15.
 */
public class AnalyticsModulesBadRequestException extends RuntimeException  {

    private	static	final Logger log =
            LoggerFactory.getLogger(OpenLAPCoreApplication.class);

    public AnalyticsModulesBadRequestException(String message) {
        super(message);
        log.error("Bad request: " + message);
    }
}

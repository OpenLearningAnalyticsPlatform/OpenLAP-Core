package de.rwthaachen.openlap.analyticsmethods.exceptions;

import de.rwthaachen.openlap.OpenLAPCoreApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Exception to be thrown whenever the macro component receives a malformed request.
 */
public class AnalyticsMethodsBadRequestException extends RuntimeException  {

    private	static	final Logger log =
            LoggerFactory.getLogger(OpenLAPCoreApplication.class);

    public AnalyticsMethodsBadRequestException(String message) {
        super(message);
        log.error("Bad request: " + message);
    }
}

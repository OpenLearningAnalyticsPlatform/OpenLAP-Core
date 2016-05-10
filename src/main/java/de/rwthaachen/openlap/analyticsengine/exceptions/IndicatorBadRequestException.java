package de.rwthaachen.openlap.analyticsengine.exceptions;

import de.rwthaachen.openlap.OpenLAPCoreApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Exception to be thrown whenever the AnalyticsGoal macro component has a bad request.
 */
public class IndicatorBadRequestException extends RuntimeException {

    private static final Logger log =
            LoggerFactory.getLogger(OpenLAPCoreApplication.class);

    public IndicatorBadRequestException(String message) {
        super(message);
        log.error("Bad request: " + message);
    }
}

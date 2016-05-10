package de.rwthaachen.openlap.analyticsmethods.exceptions;

import de.rwthaachen.openlap.OpenLAPCoreApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Exception to be thrown whenever an AnalyticsMethod is not found.
 */
public class AnalyticsMethodNotFoundException extends RuntimeException {

    private static final Logger log =
            LoggerFactory.getLogger(OpenLAPCoreApplication.class);

    public AnalyticsMethodNotFoundException(String message) {
        super(message);
        log.error("Analytics Method not found: " + message);
    }

}

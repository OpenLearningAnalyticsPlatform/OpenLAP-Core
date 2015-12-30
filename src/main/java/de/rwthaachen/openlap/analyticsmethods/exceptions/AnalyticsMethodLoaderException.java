package de.rwthaachen.openlap.analyticsmethods.exceptions;

import de.rwthaachen.openlap.OpenLAPCoreApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Exception to be thrown whenever the AnalyticsMethod Loader does not work properly.
 */
public class AnalyticsMethodLoaderException extends RuntimeException {

    private	static	final Logger log =
            LoggerFactory.getLogger(OpenLAPCoreApplication.class);

    public AnalyticsMethodLoaderException(String message) {
        super(message);
        log.error("Analytics Method could not be loaded: " + message);
    }

}

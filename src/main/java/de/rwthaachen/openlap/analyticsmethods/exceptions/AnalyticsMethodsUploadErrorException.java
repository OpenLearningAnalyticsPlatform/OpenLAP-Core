package de.rwthaachen.openlap.analyticsmethods.exceptions;

import de.rwthaachen.openlap.OpenLAPCoreApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Exception to be thrown whenever an erroneous AnalyticsMethod is attempted to be uploaded.
 */
public class AnalyticsMethodsUploadErrorException extends RuntimeException {

    private static final Logger log =
            LoggerFactory.getLogger(OpenLAPCoreApplication.class);

    public AnalyticsMethodsUploadErrorException(String message) {
        super(message);
        log.error("Upload Error Exception: " + message);
    }

}

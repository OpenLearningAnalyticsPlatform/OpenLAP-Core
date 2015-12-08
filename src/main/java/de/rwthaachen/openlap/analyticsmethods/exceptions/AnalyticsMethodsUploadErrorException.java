package de.rwthaachen.openlap.analyticsmethods.exceptions;

import de.rwthaachen.openlap.OpenLAPCoreApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lechip on 14/11/15.
 */
public class AnalyticsMethodsUploadErrorException extends RuntimeException {

    private	static	final Logger log =
            LoggerFactory.getLogger(OpenLAPCoreApplication.class);

    public AnalyticsMethodsUploadErrorException(String message) {
        super(message);
        log.error("Upload Error Exception: " + message);
    }

}

package de.rwthaachen.openlap.analyticsengine.exceptions;

import de.rwthaachen.openlap.OpenLAPCoreApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Exception to be thrown whenever the Analytics Engine macro component has a bad request.
 */
public class BadRequestException extends RuntimeException {

    private static final Logger log = LoggerFactory.getLogger(OpenLAPCoreApplication.class);
    private String errorCode;

    public BadRequestException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        log.error("BadRequestException: " + message);
    }

    public String getErrorCode() {
        return errorCode;
    }
}

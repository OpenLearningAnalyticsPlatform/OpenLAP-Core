package de.rwthaachen.openlap.analyticsengine.exceptions;

import de.rwthaachen.openlap.OpenLAPCoreApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Exception to be thrown whenever the Analytics Engine is not able to find any item.
 */
public class ItemNotFoundException extends RuntimeException  {

    private static final Logger log = LoggerFactory.getLogger(OpenLAPCoreApplication.class);
    private String errorCode;

    public ItemNotFoundException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        log.error("ItemNotFoundException: " + message);
    }

    public String getErrorCode() {
        return errorCode;
    }
}

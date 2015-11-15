package org.rwthaachen.olap.analyticsmethods.exceptions;

/**
 * Created by lechip on 15/11/15.
 */
public class AnalyticsMethodUploadValidationException extends AnalyticsMethodException {

    public AnalyticsMethodUploadValidationException(String message) {
        super(message);
        log.error("Error in validation of Analytics Method upload/update: " + message);
    }
}

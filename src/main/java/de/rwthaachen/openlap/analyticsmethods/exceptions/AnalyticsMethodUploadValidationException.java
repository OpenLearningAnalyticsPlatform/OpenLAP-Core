package de.rwthaachen.openlap.analyticsmethods.exceptions;

/**
 * Custom Exception to be thrown whenever a validation error happens with an AnalyticsMethod.
 */
public class AnalyticsMethodUploadValidationException extends AnalyticsMethodException {

    public AnalyticsMethodUploadValidationException(String message) {
        super(message);
        log.error("Error in validation of Analytics Method upload/update: " + message);
    }
}

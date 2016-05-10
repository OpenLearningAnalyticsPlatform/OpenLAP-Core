package de.rwthaachen.openlap.analyticsmethods.service;

/**
 * Class to encapsulate validation information of Analytics Method upload requests. Contains a boolean that represents
 * the result of the validation operation and a message with additional information of the operation.
 */
public class AnalyticsMethodsValidationInformation {
    boolean isValid;
    String message;

    /**
     * Empty constructor.
     */
    public AnalyticsMethodsValidationInformation() {
        this(false, "");
    }

    /**
     * Standard constructor.
     *
     * @param isValid Value for validation.
     * @param message Message of validation.
     */
    public AnalyticsMethodsValidationInformation(boolean isValid, String message) {
        this.isValid = isValid;
        this.message = message;
    }

    /**
     * @return true if Analytics Method upload is valid, false otherwise.
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * @param valid Value to set as validation output.
     */
    public void setValid(boolean valid) {
        isValid = valid;
    }

    /**
     * @return Message of validation operation.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message Message of validation operation.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Appends a string to the message field.
     *
     * @param message String to be appended to the message field.
     * @return New message field.
     */
    public String appendMessage(String message) {
        if (message.isEmpty()) {
            this.setMessage(message);
        } else {
            this.message = this.message + "\n" + message;
        }
        return this.message;
    }
}

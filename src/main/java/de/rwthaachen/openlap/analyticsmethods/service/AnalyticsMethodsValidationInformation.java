package de.rwthaachen.openlap.analyticsmethods.service;

/**
 * Created by lechip on 15/11/15.
 */
public class AnalyticsMethodsValidationInformation {
    boolean isValid;
    String message;

    public AnalyticsMethodsValidationInformation() {
        this(false, "");
    }

    public AnalyticsMethodsValidationInformation(boolean isValid, String message) {
        this.isValid = isValid;
        this.message = message;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String appendMessage(String message)
    {
        if (message.isEmpty())
        {
            this.setMessage(message);
        }
        else
        {
            this.message = this.message + "\n" + message;
        }
        return this.message;
    }
}

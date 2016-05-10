package de.rwthaachen.openlap.analyticsmodules.exceptions;

import de.rwthaachen.openlap.OpenLAPCoreApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Exception to be thrown whenever the AnalyticsGoal is not found.
 */
public class AnalyticsGoalNotFoundException extends RuntimeException {
    private static final Logger log =
            LoggerFactory.getLogger(OpenLAPCoreApplication.class);

    public AnalyticsGoalNotFoundException(String message) {
        super(message);
        log.error("AnalyticsGoal not found: " + message);
    }
}

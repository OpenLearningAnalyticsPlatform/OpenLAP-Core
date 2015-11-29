package org.rwthaachen.olap.analyticsmodules.exceptions;

import org.rwthaachen.olap.OpenLAPCoreApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lechip on 29/11/15.
 */
public class LearningGoalNotFoundException extends RuntimeException {
    private	static	final Logger log =
            LoggerFactory.getLogger(OpenLAPCoreApplication.class);

    public LearningGoalNotFoundException(String message) {
        super(message);
        log.error("LearningGoal not found: " + message);
    }
}

package org.rwthaachen.olap.analyticsmethods.exceptions;

import org.rwthaachen.olap.OpenLAPCoreApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lechip on 10/11/15.
 */
public class AnalyticsMethodNotFoundException extends RuntimeException {

    private	static	final Logger log =
            LoggerFactory.getLogger(OpenLAPCoreApplication.class);

    public AnalyticsMethodNotFoundException(String message) {
        super(message);
        log.error("Analytics Method not found: " + message);
    }

}

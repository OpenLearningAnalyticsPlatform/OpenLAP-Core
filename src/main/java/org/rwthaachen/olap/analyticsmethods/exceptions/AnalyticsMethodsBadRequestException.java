package org.rwthaachen.olap.analyticsmethods.exceptions;

import org.rwthaachen.olap.OpenLAPCoreApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lechip on 14/11/15.
 */
public class AnalyticsMethodsBadRequestException extends RuntimeException  {

    private	static	final Logger log =
            LoggerFactory.getLogger(OpenLAPCoreApplication.class);

    public AnalyticsMethodsBadRequestException(String message) {
        super(message);
        log.error("Bad request: " + message);
    }
}

package org.rwthaachen.olap.analyticsmethods.exceptions;

import org.rwthaachen.olap.analyticsmethods.AnalyticsMethodsApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lechip on 10/11/15.
 */
public class AnalyticsMethodLoaderException extends RuntimeException {

    private	static	final Logger log =
            LoggerFactory.getLogger(AnalyticsMethodsApplication.class);

    public AnalyticsMethodLoaderException(String message) {
        super(message);
        log.error("Analytics Method could not be loaded: " + message);
    }

}

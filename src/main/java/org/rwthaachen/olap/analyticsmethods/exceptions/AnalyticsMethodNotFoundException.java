package org.rwthaachen.olap.analyticsmethods.exceptions;

import org.rwthaachen.olap.analyticsmethods.AnalyticsMethodsApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by lechip on 10/11/15.
 */
public class AnalyticsMethodNotFoundException extends RuntimeException {

    private	static	final Logger log =
            LoggerFactory.getLogger(AnalyticsMethodsApplication.class);

    public AnalyticsMethodNotFoundException(String message) {
        super(message);
        log.error("Analytics Method not found: " + message);
    }

}

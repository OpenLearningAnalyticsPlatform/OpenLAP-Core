package org.rwthaachen.olap.analyticsmethods.exceptions;

import org.rwthaachen.olap.analyticsmethods.AnalyticsMethodsApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by lechip on 14/11/15.
 */
@ResponseStatus(value= HttpStatus.BAD_REQUEST, reason="Analytics Method Metadata is not correctly constructed")
public class AnalyticsMethodsBadRequestException extends RuntimeException  {

    private	static	final Logger log =
            LoggerFactory.getLogger(AnalyticsMethodsApplication.class);

    public AnalyticsMethodsBadRequestException(String message) {
        super(message);
        log.error("Not found exception on request: " + message);
    }
}
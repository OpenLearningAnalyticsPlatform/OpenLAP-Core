package org.rwthaachen.olap.analyticsmethods.exceptions;

import org.rwthaachen.olap.analyticsmethods.AnalyticsMethodsApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by lechip on 14/11/15.
 */
@ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR, reason="Analytics Methods Internal Server Error")
public class AnalyticsMethodsUploadErrorException extends RuntimeException {

    private	static	final Logger log =
            LoggerFactory.getLogger(AnalyticsMethodsApplication.class);

    public AnalyticsMethodsUploadErrorException(String message) {
        super(message);
        log.error("Not found exception on request: " + message);
    }

}
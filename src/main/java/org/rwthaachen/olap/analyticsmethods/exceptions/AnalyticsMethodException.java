package org.rwthaachen.olap.analyticsmethods.exceptions;

import org.rwthaachen.olap.OpenLAPCoreApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lechip on 15/11/15.
 */
public abstract class AnalyticsMethodException extends Exception{

    protected static final Logger log =
            LoggerFactory.getLogger(OpenLAPCoreApplication.class);

    public AnalyticsMethodException(String message) {
        super(message);
    }
}

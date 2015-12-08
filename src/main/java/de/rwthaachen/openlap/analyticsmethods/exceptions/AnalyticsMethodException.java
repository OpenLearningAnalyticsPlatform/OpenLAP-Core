package de.rwthaachen.openlap.analyticsmethods.exceptions;

import de.rwthaachen.openlap.OpenLAPCoreApplication;
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

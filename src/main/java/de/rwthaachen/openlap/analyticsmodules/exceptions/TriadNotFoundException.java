package de.rwthaachen.openlap.analyticsmodules.exceptions;

import de.rwthaachen.openlap.OpenLAPCoreApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lechip on 28/11/15.
 */
public class TriadNotFoundException extends RuntimeException {
    private	static	final Logger log =
            LoggerFactory.getLogger(OpenLAPCoreApplication.class);

    public TriadNotFoundException(String message) {
        super(message);
        log.error("Triad not found: " + message);
    }
}

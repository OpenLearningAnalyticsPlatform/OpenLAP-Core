package org.rwthaachen.olap.analyticsmethods.controller;

import java.time.Instant;
import java.util.HashMap;

/**
 * Created by lechip on 17/11/15.
 */
public class AnalyticsMethodsErrorHandlerDTO {

    private static final String TIMESTAMP = "timestamp";
    private static final String STATUS = "status";
    private static final String ERROR = "error";
    private static final String EXCEPTION= "exception";
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String PATH = "path";

    HashMap<String, Object> content = new HashMap<>();

    public AnalyticsMethodsErrorHandlerDTO(int status, String exception,
                                           String message, String path) {
        content.put(TIMESTAMP, Instant.now().getEpochSecond());
        content.put(STATUS, status);
        content.put(EXCEPTION, exception);
        content.put(ERROR_MESSAGE, message);
        content.put(PATH, path);

    }

    public HashMap<String, Object> getContent() {
        return content;
    }

    public void setContent(HashMap<String, Object> content) {
        this.content = content;
    }
}

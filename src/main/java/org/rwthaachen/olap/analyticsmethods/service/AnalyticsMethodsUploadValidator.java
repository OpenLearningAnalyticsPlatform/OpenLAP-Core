package org.rwthaachen.olap.analyticsmethods.service;

import core.AnalyticsMethod;
import org.rwthaachen.olap.analyticsmethods.AnalyticsMethodsApplication;
import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodUploadValidationException;

import org.rwthaachen.olap.analyticsmethods.model.AnalyticsMethodMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by lechip on 05/11/15.
 */
@Service
public class AnalyticsMethodsUploadValidator {

    private AnalyticsMethodsClassPathLoader classPathLoader;

    protected static final Logger log =
            LoggerFactory.getLogger(AnalyticsMethodsApplication.class);

    public AnalyticsMethodsValidationInformation validatemethod
            (AnalyticsMethodMetadata methodMetadata, String analyticsMethodsJarsFolder) {

        AnalyticsMethodsValidationInformation validationInformation = new AnalyticsMethodsValidationInformation();
        classPathLoader = new AnalyticsMethodsClassPathLoader(analyticsMethodsJarsFolder);

        // TODO make a method to validate non empty fields of the metadata
        if(methodMetadata.getName().isEmpty()
                        || methodMetadata.getImplementingClass().isEmpty()
                        || methodMetadata.getCreator().isEmpty()
                        || methodMetadata.getDescription().isEmpty()
                )
        {
            validationInformation.setValid(false);
            validationInformation.setMessage("Name, Implementing Class, Author and Description must have content");
            return validationInformation;
        }

        // Validate that the class exist and implements the interface and that the class implements the interface
        try {
            AnalyticsMethod method = classPathLoader.loadClass(methodMetadata.getImplementingClass());
            validationInformation.setValid(true);
            log.info("Validation successful: " + methodMetadata.getImplementingClass());
            log.info("OLAPInputOf the method: " + method.getInputPorts());
        } catch (AnalyticsMethodUploadValidationException e) {
            validationInformation.setValid(false);
            validationInformation.appendMessage(e.getMessage());
        }

        // Return a validation object
        return validationInformation;
    }


}

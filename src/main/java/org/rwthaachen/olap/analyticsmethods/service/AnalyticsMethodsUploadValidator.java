package org.rwthaachen.olap.analyticsmethods.service;

import core.AnalyticsMethod;
import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodUploadValidationException;

import org.rwthaachen.olap.analyticsmethods.model.AnalyticsMethodMetadata;
import org.springframework.stereotype.Service;

/**
 * Created by lechip on 05/11/15.
 */
@Service
public class AnalyticsMethodsUploadValidator {

    private AnalyticsMethodsClassPathLoader classPathLoader;

    public AnalyticsMethodsValidationInformation validatemethod
            (AnalyticsMethodMetadata methodMetadata, String analyticsMethodsJarsFolder) {

        AnalyticsMethodsValidationInformation validationInformation = new AnalyticsMethodsValidationInformation();
        classPathLoader = new AnalyticsMethodsClassPathLoader(analyticsMethodsJarsFolder);

        // TODO make a method to validate non empty fields of the metadata

        // Validate that the class exist and implements the interface and that the class implements the interface
        try {
            AnalyticsMethod method = classPathLoader.loadClass(methodMetadata.getImplementingClass());
            validationInformation.setValid(true);
            //TODO remove this
            validationInformation.appendMessage(method.sayHi());
        } catch (AnalyticsMethodUploadValidationException e) {
            validationInformation.setValid(false);
            validationInformation.appendMessage(e.getMessage());
        }

        // Return a validation object
        return validationInformation;
    }


}

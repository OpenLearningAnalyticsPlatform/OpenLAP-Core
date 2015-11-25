package org.rwthaachen.olap.analyticsmethods.service;

import core.AnalyticsMethod;
import org.rwthaachen.olap.analyticsmethods.AnalyticsMethodsApplication;
import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodLoaderException;
import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodUploadValidationException;

import org.rwthaachen.olap.analyticsmethods.model.AnalyticsMethodMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created by lechip on 05/11/15.
 */
@Service
public class AnalyticsMethodsUploadValidator {

    private AnalyticsMethodsClassPathLoader classPathLoader;

    @Value("${pmmlxsd}")
    private String pmmlXsdUrl;

    protected static final Logger log =
            LoggerFactory.getLogger(AnalyticsMethodsApplication.class);

    public AnalyticsMethodsValidationInformation validatemethod
            (AnalyticsMethodMetadata methodMetadata, String analyticsMethodsJarsFolder) {

        AnalyticsMethodsValidationInformation validationInformation = new AnalyticsMethodsValidationInformation();
        classPathLoader = new AnalyticsMethodsClassPathLoader(analyticsMethodsJarsFolder);

        // Validate non empty fields of the metadata
        if (methodMetadata.getName().isEmpty()
                        || methodMetadata.getImplementingClass().isEmpty()
                        || methodMetadata.getCreator().isEmpty()
                        || methodMetadata.getDescription().isEmpty()
                        || methodMetadata.getFilename().isEmpty()
                        || !validateFilename(methodMetadata.getFilename())
                )
        {
            validationInformation.setValid(false);
            validationInformation.setMessage("Metadata Name, Implementing Class, "
                    + "Author and Description must have content "
                    + "and filename must match the regex ^[a-zA-Z0-9]* $"
                    + "(ASCII Alphanumeric, do not include file extensions)");
            return validationInformation;
        }

        // Validate that the class exist and implements the interface and that the class implements the interface
        try {
            AnalyticsMethod method = classPathLoader.loadClass(methodMetadata.getImplementingClass());
            // Validate pmml if the method has a PMML
            validationInformation.setValid(true);
            if(method.hasPMML())
            {
                SimpleXmlSchemaValidator
                        .validateXML(validationInformation, method.getPMMLInputStream(),pmmlXsdUrl);
            }
            log.info("Validation successful: " + methodMetadata.getImplementingClass());
            log.info("OLAPInputOf the method: " + method.getInputPorts());
        } catch (AnalyticsMethodLoaderException e) {
            validationInformation.setValid(false);
            validationInformation.appendMessage(e.getMessage());
        }

        // Return a validation object
        return validationInformation;
    }

    private boolean validateFilename(String input){
        final Pattern pattern = Pattern.compile("^[a-zA-Z0-9]*$");
        if (!pattern.matcher(input).matches()) return false;
        return true;
    }


}

package de.rwthaachen.openlap.analyticsmethods.service;

import core.AnalyticsMethod;
import de.rwthaachen.openlap.OpenLAPCoreApplication;
import de.rwthaachen.openlap.analyticsmethods.exceptions.AnalyticsMethodLoaderException;
import de.rwthaachen.openlap.analyticsmethods.model.AnalyticsMethodMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * A validation utility class to check for upload requests of Analytics Methods so the files submitted effectively
 * implement the OpenLAP-AnalytisMethodFramework correctly.
 */
@Service
public class AnalyticsMethodsUploadValidator {

    private AnalyticsMethodsClassPathLoader classPathLoader;

    @Value("${pmmlxsd}")
    private String pmmlXsdUrl;

    protected static final Logger log =
            LoggerFactory.getLogger(OpenLAPCoreApplication.class);

    /**
     * Validates the JAR so it contains the class specified on the AnalyticsMethodsMetadata field that describes the
     * implementing class. Additionally checks for the JAR containing valid files and, if provided, the validity of
     * the PMML file of the AnalyticsMethod.
     * @param methodMetadata The AnalyticsMethodMetadata that describes the location of the JAR file and
     *                       class implementing the OpenLAP-AnalyticsMethodsFramework.
     * @param analyticsMethodsJarsFolder The location where the JAR file resides.
     * @return A AnalyticsMethodsValidationInformation that encapsulates the validation information of the
     * Analytics Method uploaded
     */
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

    /**
     * Utility method to check only ASCII Alphanumeric filenames
     * @param input The filename
     * @return true if the filename is ASCII Alphanumeric, false otherwise
     */
    private boolean validateFilename(String input){
        final Pattern pattern = Pattern.compile("^[a-zA-Z0-9]*$");
        if (!pattern.matcher(input).matches()) return false;
        return true;
    }


}

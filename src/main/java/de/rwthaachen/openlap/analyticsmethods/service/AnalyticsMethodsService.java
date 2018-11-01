package de.rwthaachen.openlap.analyticsmethods.service;

import core.AnalyticsMethod;
import de.rwthaachen.openlap.OpenLAPCoreApplication;
import de.rwthaachen.openlap.analyticsmethods.dataaccess.AnalyticsMethodsRepository;
import de.rwthaachen.openlap.analyticsmethods.exceptions.AnalyticsMethodLoaderException;
import de.rwthaachen.openlap.analyticsmethods.exceptions.AnalyticsMethodNotFoundException;
import de.rwthaachen.openlap.analyticsmethods.exceptions.AnalyticsMethodsBadRequestException;
import de.rwthaachen.openlap.analyticsmethods.exceptions.AnalyticsMethodsUploadErrorException;
import de.rwthaachen.openlap.analyticsmethods.model.AnalyticsMethodMetadata;
import de.rwthaachen.openlap.dataset.OpenLAPColumnConfigData;
import de.rwthaachen.openlap.dataset.OpenLAPDataSetConfigValidationResult;
import de.rwthaachen.openlap.dataset.OpenLAPPortConfig;
import de.rwthaachen.openlap.dynamicparam.OpenLAPDynamicParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This service handles the "business logic" of the macro component. It also works as a facade for other
 * macro components that happen to be running on the same server, i.e. the Analytics Engine and Analytics Modules
 */

@Service
public class AnalyticsMethodsService {

    // Strings for method names
    private static final String JAR_EXTENSION = ".jar";
    private static final String TEMP_FILE_SUFIX = "_temp";
    private static final String INPUT_PORTS = "input";
    private static final String OUTPUT_PORTS = "output";
    private static final Logger log =
            LoggerFactory.getLogger(OpenLAPCoreApplication.class);
    @Value("${analyticsMethodsJarsFolder}")
    String analyticsMethodsJarsFolder;
    @Autowired
    AnalyticsMethodsRepository analyticsMethodsRepository;
    @Autowired
    AnalyticsMethodsUploadValidator validator;

    /**
     * Lists all the Metadata of the  AnalyticsMethods available
     *
     * @return A List of the available AnalyticsMethods
     */
    public List<AnalyticsMethodMetadata> viewAllAnalyticsMethods() {
        ArrayList<AnalyticsMethodMetadata> result = new ArrayList<AnalyticsMethodMetadata>();
        // (A ::B ) denotes A consumer execute B with the iterator given.
        analyticsMethodsRepository.findAllByOrderByNameAsc().forEach(result::add);
        return result;
    }

    /**
     * Returns the Metadata of the Analytics Method of the specified ID
     *
     * @param id ID of the AnalyticsMethod to view
     * @return The AnalyticsMethod with Metadata of the specified ID
     * @throws AnalyticsMethodNotFoundException
     */
    public AnalyticsMethodMetadata viewAnalyticsMethod(long id) throws AnalyticsMethodNotFoundException {
        AnalyticsMethodMetadata result = analyticsMethodsRepository.findOne(id);
        //if (result == null || id == null) {
        if (result == null || id < 0) {
            throw new AnalyticsMethodNotFoundException("Analytics Method with id not found: " + id);
        } else {
            return result;
        }
    }

    /**
     * Post an AnalyticsMethod to the Server to be validated and made available for usage.
     *
     * @param methodMetadata The metadata to upload as manifest of the AnalyticsMethod
     * @param jarBundle      The JAR file with the implementation of the AnalyticsMethod
     * @return The Metadata of the uploaded AnalyticsMethod if deemed valid by the OpenLAP
     */
    public AnalyticsMethodMetadata uploadAnalyticsMethod(
            AnalyticsMethodMetadata methodMetadata, MultipartFile jarBundle) {

        AnalyticsMethodsValidationInformation validationInformation;
        FileHandler fileHandler = new FileHandler(log);

        if (!jarBundle.isEmpty()) {
            try {
                // Save the jar in the filesystem
                fileHandler.saveFile(jarBundle, analyticsMethodsJarsFolder, methodMetadata.getFilename()
                        + JAR_EXTENSION);

                //Validation
                validationInformation = validator.validatemethod(methodMetadata, analyticsMethodsJarsFolder);
                if (!validationInformation.isValid()) {
                    // If the submitted jar is not valid, remove it from the filesystem
                    fileHandler.deleteFile(analyticsMethodsJarsFolder, methodMetadata.getFilename() + JAR_EXTENSION);
                    // Throw exception (that can be used by the controller to send the bad request method)
                    throw new AnalyticsMethodsUploadErrorException(validationInformation.getMessage());
                } else {
                    log.info(validationInformation.getMessage());
                    // Stamp the location of the method in metadata and save it
                    methodMetadata.setBinariesLocation(analyticsMethodsJarsFolder);
                    return analyticsMethodsRepository.save(methodMetadata);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new AnalyticsMethodsUploadErrorException(e.getMessage());
            } catch (DataIntegrityViolationException sqlException) {
                sqlException.printStackTrace();
                throw new AnalyticsMethodsBadRequestException("Analytics Method already exists.");
            } catch (Exception e) {
                e.printStackTrace();
                throw new AnalyticsMethodsBadRequestException(e.getMessage());
            }
        }
        throw new AnalyticsMethodsBadRequestException("Empty jar bundle.");
    }

    /**
     * Update an AnalyticsMethod to the Server to be validated and made available for usage.
     *
     * @param methodMetadata The metadata to upload as manifest of the AnalyticsMethod
     * @param id             ID of the AnalyticsMethod Metadata that is to be updated.
     * @param jarBundle      The JAR file with the implementation of the AnalyticsMethod
     * @return The Metadata of the uploaded AnalyticsMethod if deemed valid by the OpenLAP
     */
    public AnalyticsMethodMetadata updateAnalyticsMethod(AnalyticsMethodMetadata methodMetadata,
                                                         long id, MultipartFile jarBundle) {
        AnalyticsMethodsValidationInformation validationInformation;
        FileHandler fileHandler = new FileHandler(log);

        //Try to fetch the method, if does not exist, throw exception
        AnalyticsMethodMetadata result = analyticsMethodsRepository.findOne(id);

        if (result == null) {
            throw new AnalyticsMethodNotFoundException("Analytics Method with id not found: " + methodMetadata.getId());
        } else {
            //log.info("Attemting to update Analytics method: " + methodMetadata.getId());
            //Make bundle required.
            if (!jarBundle.isEmpty()) {
                // Save the jar in the filesystem
                try {
                    AnalyticsMethodMetadata tempMetadata = (AnalyticsMethodMetadata) result.clone();
                    //Name bundle method_temp.jar
                    tempMetadata.setName(tempMetadata.getFilename() + TEMP_FILE_SUFIX);
                    tempMetadata.setImplementingClass(methodMetadata.getImplementingClass());
                    fileHandler.saveFile(jarBundle, analyticsMethodsJarsFolder, tempMetadata.getFilename()
                            + JAR_EXTENSION);
                    //Perform validation with than name.
                    //If valid, delete the old jar with the new one, check that there are no leftovers
                    validationInformation = validator.validatemethod(tempMetadata, analyticsMethodsJarsFolder);
                    if (validationInformation.isValid()) {
                        //delete temp file
                        fileHandler.deleteFile(analyticsMethodsJarsFolder, tempMetadata.getFilename()
                                + JAR_EXTENSION);
                        //write real file
                        fileHandler.saveFile(jarBundle, analyticsMethodsJarsFolder, methodMetadata.getFilename()
                                + JAR_EXTENSION);
                        //update metadata
                        result.updateWithMetadata(methodMetadata);
                        return analyticsMethodsRepository.save(result);
                    } else {
                        //delete temp file
                        fileHandler.deleteFile(analyticsMethodsJarsFolder, tempMetadata.getFilename()
                                + JAR_EXTENSION);
                        throw new AnalyticsMethodsBadRequestException(validationInformation.getMessage());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new AnalyticsMethodsUploadErrorException(e.getMessage());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                    throw new AnalyticsMethodsUploadErrorException(e.getMessage());
                }
            } else {
                throw new AnalyticsMethodsBadRequestException("Empty jar bundle.");
            }
        }
    }

    /**
     * Method that allows to validate an OpenLAPPortConfig of a specific AnalyticsMethod.
     *
     * @param analyticsMethodId ID of the AnalyticsMethod Metadata to be validated against the OpenLAPPortConfig.
     * @param configuration     The OpenLAPPortConfig to be validated
     * @return An Object with the validation information of the OpenLAPPortConfig against the specified Analytics
     * Method.
     * @throws AnalyticsMethodLoaderException
     */
    public OpenLAPDataSetConfigValidationResult validateConfiguration(
            long analyticsMethodId, OpenLAPPortConfig configuration) throws AnalyticsMethodLoaderException {
        //log.info("Attempting to validateConfiguration :" + configuration.getMapping()
        //        + "for method with id: " + analyticsMethodId);
        AnalyticsMethod method = loadAnalyticsMethodInstance(analyticsMethodId);
        return method.getInput().validateConfiguration(configuration);
    }

    /**
     * A Method that creates an instance of an AnalyticsMethod object from the JAR location contained in the
     * corresponding AnalyticsMethodMetadata.
     *
     * @param analyticsMethodId The ID of the Analytics Method to instantiate
     * @return A new instance of the specified Analytics Method.
     * @throws AnalyticsMethodLoaderException
     */
    public AnalyticsMethod loadAnalyticsMethodInstance(long analyticsMethodId) throws AnalyticsMethodLoaderException {
        AnalyticsMethodMetadata analyticsMethodMetadata = analyticsMethodsRepository.findOne(analyticsMethodId);
        if (analyticsMethodMetadata == null || analyticsMethodId < 0) {
            throw new AnalyticsMethodNotFoundException("Analytics Method with id not found: " + analyticsMethodId);
        } else {

            AnalyticsMethod method;
            AnalyticsMethodsClassPathLoader classPathLoader =
                    new AnalyticsMethodsClassPathLoader(analyticsMethodsJarsFolder + "/" + analyticsMethodMetadata.getFilename()
                            + JAR_EXTENSION);

            method = classPathLoader.loadClass(analyticsMethodMetadata.getImplementingClass());
            return method;
        }
    }

    /**
     * Method that returns the OpenLAPColumnConfigData of the input ports of a specific AnalyticsMethod
     *
     * @param id ID of the AnalyticsMethod Metadata
     * @return A list of OpenLAPColumnConfigData corresponding to the input ports of the AnalyticsMethod
     */
    public List<OpenLAPColumnConfigData> GetInputPortsForMethod(long id) {
        return getPortsForMethod(id, INPUT_PORTS);
    }

    /**
     * Method that returns the OpenLAPColumnConfigData of the output ports of a specific AnalyticsMethod
     *
     * @param id ID of the AnalyticsMethod Metadata
     * @return A list of OpenLAPColumnConfigData corresponding to the output ports of the AnalyticsMethod
     */
    public List<OpenLAPColumnConfigData> GetOutputPortsForMethod(long id) {
        return getPortsForMethod(id, OUTPUT_PORTS);
    }

    public List<OpenLAPDynamicParam> GetDynamicParamsForMethod(long id) {
        AnalyticsMethod method = loadAnalyticsMethodInstance(id);

        return method.getParams().getParamsAsList(false);
    }

    /**
     * Returns a List of OpenLAPColumnConfigData of either the Input ports or output ports of the Analytics Method
     * of the given <code>id</code>.
     *
     * @param id            of the Analytics Method
     * @param portParameter Either <code>INPUT_PORT</code> or <code>OUTPUT_PORTS</code>
     * @return List of the OpenLAPColumnConfigData corresponding to the inputs or outputs of the Analytics Method
     * @throws AnalyticsMethodLoaderException
     */
    private List<OpenLAPColumnConfigData> getPortsForMethod(long id, String portParameter)
            throws AnalyticsMethodLoaderException {

        AnalyticsMethod method = loadAnalyticsMethodInstance(id);
        //log.info("Attempting to return " + portParameter + " ports of the method with id {" + id + "}");

        List<OpenLAPColumnConfigData> ports;

        switch (portParameter) {
            case INPUT_PORTS:
                ports = method.getInputPorts();
                break;
            case OUTPUT_PORTS:
                ports = method.getOutputPorts();
                break;
            default:
                throw new AnalyticsMethodsBadRequestException("Only can return Inputs or Outputs");
        }

        Collections.sort(ports, (OpenLAPColumnConfigData o1, OpenLAPColumnConfigData o2) -> (o1.getTitle().compareTo(o2.getTitle())));

        return ports;
    }

    /**
     * Delete the specified AnalyticsMethod
     *
     * @param id id of the AnalyticsMethod to be deleted
     */
    public void deleteAnalyticsMethod(long id) {

        FileHandler fileHandler = new FileHandler(log);
        AnalyticsMethodMetadata metadata = analyticsMethodsRepository.findOne(id);

        //if (metadata == null || id == null || id.isEmpty()) {
        if (metadata == null || id <0) {
            throw new AnalyticsMethodNotFoundException("Analytics Method with id = {"
                    + id + "} not found.");
        }

        // Delete Files
        fileHandler.deleteFile(analyticsMethodsJarsFolder, metadata.getFilename());

        // Delete from database
        analyticsMethodsRepository.delete(metadata);

    }
}

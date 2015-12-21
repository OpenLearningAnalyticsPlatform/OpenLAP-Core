package de.rwthaachen.openlap.analyticsmethods.service;

import DataSet.OLAPColumnConfigurationData;
import DataSet.OLAPDataSetConfigurationValidationResult;
import DataSet.OLAPPortConfiguration;
import core.AnalyticsMethod;
import de.rwthaachen.openlap.OpenLAPCoreApplication;
import de.rwthaachen.openlap.analyticsmethods.dataaccess.AnalyticsMethodsRepository;
import de.rwthaachen.openlap.analyticsmethods.exceptions.AnalyticsMethodLoaderException;
import de.rwthaachen.openlap.analyticsmethods.exceptions.AnalyticsMethodNotFoundException;
import de.rwthaachen.openlap.analyticsmethods.exceptions.AnalyticsMethodsBadRequestException;
import de.rwthaachen.openlap.analyticsmethods.exceptions.AnalyticsMethodsUploadErrorException;
import de.rwthaachen.openlap.analyticsmethods.model.AnalyticsMethodMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
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

    @Value("${analyticsMethodsJarsFolder}")
    String analyticsMethodsJarsFolder;

    @Autowired
    AnalyticsMethodsRepository analyticsMethodsRepository;

    @Autowired
    AnalyticsMethodsUploadValidator validator;

    private	static	final Logger log =
            LoggerFactory.getLogger(OpenLAPCoreApplication.class);

    /**
     * TODO
     * @return
     */
    public List<AnalyticsMethodMetadata> viewAllAnalyticsMethods() {
        ArrayList<AnalyticsMethodMetadata> result = new ArrayList<AnalyticsMethodMetadata>();
        // (A ::B ) denotes A consumer execute B with the iterator given.
        analyticsMethodsRepository.findAll().forEach(result :: add);
        return result;
    }

    /**
     * TODO
     * @param id
     * @return
     * @throws AnalyticsMethodNotFoundException
     */
    public AnalyticsMethodMetadata viewAnalyticsMethod(String id) throws AnalyticsMethodNotFoundException {
        AnalyticsMethodMetadata result = analyticsMethodsRepository.findOne(id);
        if (result == null || id == null)
        {
            throw new AnalyticsMethodNotFoundException("Analytics Method with id not found: " + id);
        }
        else
        {
            log.info("viewAnalyticsMethod returns " + analyticsMethodsRepository.findOne(id).toString());
            return result;
        }
    }

    /**
     * TODO
     * @param methodMetadata
     * @param jarBundle
     * @return
     */
    public AnalyticsMethodMetadata uploadAnalyticsMethod(
            AnalyticsMethodMetadata methodMetadata, MultipartFile jarBundle) {

        AnalyticsMethodsValidationInformation validationInformation;
        FileHandler fileHandler = new FileHandler(log);

        if (!jarBundle.isEmpty())
        {
            try
            {
                // Save the jar in the filesystem
                fileHandler.saveFile(jarBundle, analyticsMethodsJarsFolder, methodMetadata.getFilename()
                        + JAR_EXTENSION);

                //Validation
                validationInformation = validator.validatemethod(methodMetadata, analyticsMethodsJarsFolder);
                if (!validationInformation.isValid())
                {
                    // If the submitted jar is not valid, remove it from the filesystem
                    fileHandler.deleteFile(analyticsMethodsJarsFolder, methodMetadata.getFilename() + JAR_EXTENSION);
                    // Throw exception (that can be used by the controller to send the bad request method)
                    throw new AnalyticsMethodsUploadErrorException(validationInformation.getMessage());
                }
                else
                {
                    log.info(validationInformation.getMessage());
                    // Stamp the location of the method in metadata and save it
                    methodMetadata.setBinariesLocation(analyticsMethodsJarsFolder);
                    return analyticsMethodsRepository.save(methodMetadata);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new AnalyticsMethodsUploadErrorException(e.getMessage());
            } catch (DataIntegrityViolationException sqlException){
                sqlException.printStackTrace();
                throw new AnalyticsMethodsBadRequestException("Analytics Method already exists.");
            } catch (Exception e){
                e.printStackTrace();
                throw new AnalyticsMethodsBadRequestException(e.getMessage());
            }
        }
        throw new AnalyticsMethodsBadRequestException("Empty jar bundle.");
    }

    /**
     * TODO
     * @param methodMetadata
     * @param id
     * @param jarBundle
     * @return
     */
    public AnalyticsMethodMetadata updateAnalyticsMethod(AnalyticsMethodMetadata methodMetadata,
                                                         String id, MultipartFile jarBundle) {
        AnalyticsMethodsValidationInformation validationInformation;
        FileHandler fileHandler = new FileHandler(log);

        //Try to fetch the method, if does not exist, throw exception
        AnalyticsMethodMetadata result = analyticsMethodsRepository.findOne(id);

        if (result == null)
        {
            throw new AnalyticsMethodNotFoundException("Analytics Method with id not found: " + methodMetadata.getId());
        }
        else
        {
            log.info("Attemting to update Analytics method: " + methodMetadata.getId());
            //Make bundle required.
            if (!jarBundle.isEmpty())
            {
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
                    if (validationInformation.isValid())
                    {
                        //delete temp file
                        fileHandler.deleteFile(analyticsMethodsJarsFolder, tempMetadata.getFilename()
                                + JAR_EXTENSION);
                        //write real file
                        fileHandler.saveFile(jarBundle, analyticsMethodsJarsFolder, methodMetadata.getFilename()
                                + JAR_EXTENSION);
                        //update metadata
                        result.updateWithMetadata(methodMetadata);
                        return analyticsMethodsRepository.save(result);
                    }
                    else
                    {
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
            }
            else
            {
                throw new AnalyticsMethodsBadRequestException("Empty jar bundle.");
            }
        }
    }

    /**
     * TODO
     * @param analyticsMethodId
     * @param configuration
     * @return
     * @throws AnalyticsMethodLoaderException
     */
    public OLAPDataSetConfigurationValidationResult validateConfiguration(
            String analyticsMethodId, OLAPPortConfiguration configuration) throws AnalyticsMethodLoaderException
    {
        log.info("Attempting to validateConfiguration :" + configuration.getMapping()
                + "for method with id: " + analyticsMethodId);
        AnalyticsMethod method = loadAnalyticsMethodInstance(analyticsMethodId);
        return method.getInput().validateConfiguration(configuration);
    }

    /**
     * TODO
     * @param analyticsMethodId
     * @return
     * @throws AnalyticsMethodLoaderException
     */
    public AnalyticsMethod loadAnalyticsMethodInstance(String analyticsMethodId) throws AnalyticsMethodLoaderException
    {
        AnalyticsMethod method;
        AnalyticsMethodsClassPathLoader classPathLoader =
                new AnalyticsMethodsClassPathLoader(analyticsMethodsJarsFolder);

        AnalyticsMethodMetadata analyticsMethodMetadata = analyticsMethodsRepository.findOne(analyticsMethodId);
        if (analyticsMethodMetadata == null || analyticsMethodId == null)
        {
            throw new AnalyticsMethodNotFoundException("Analytics Method with id not found: " + analyticsMethodId);
        }
        else
        {
            log.info("Attempting to Load method: " + analyticsMethodMetadata.getFilename()
                    + " for method with id: {" + analyticsMethodId + "}");
            method = classPathLoader.loadClass(analyticsMethodMetadata.getImplementingClass());
            return method;
        }
    }

    /**
     * TODO
     * @param id
     * @return
     */
    public List<OLAPColumnConfigurationData> GetInputPortsForMethod(String id) {
        return getPortsForMethod(id, INPUT_PORTS);
    }

    /**
     * TODO
     * @param id
     * @return
     */
    public List<OLAPColumnConfigurationData> GetOutputPortsForMethod(String id) {
        return getPortsForMethod(id, OUTPUT_PORTS);
    }

    /**
     * Returns a List of OLAPColumnConfigurationData of either the Input ports or output ports of the Analytics Method
     * of the given <code>id</code>.
     * @param id of the Analytics Method
     * @param portParameter Either <code>INPUT_PORT</code> or <code>OUTPUT_PORTS</code>
     * @return List of the OLAPColumnConfigurationData corresponding to the inputs or outputs of the Analytics Method
     * @throws AnalyticsMethodLoaderException
     */
    private List<OLAPColumnConfigurationData> getPortsForMethod(String id, String portParameter)
            throws AnalyticsMethodLoaderException
    {

        AnalyticsMethod method = loadAnalyticsMethodInstance(id);
        log.info("Attempting to return " + portParameter + " ports of the method with id {" + id + "}");

        switch (portParameter)
        {
            case INPUT_PORTS:
                return method.getInputPorts();
            case OUTPUT_PORTS:
                return method.getOutputPorts();
            default:
                throw new AnalyticsMethodsBadRequestException("Only can return Inputs or Outputs");
        }
    }

    /**
     * Delete the specified AnalyticsMethod
     * @param id id of the AnalyticsMethod to be deleted
     */
    public void deleteAnalyticsMethod(String id) {

        FileHandler fileHandler = new FileHandler(log);
        AnalyticsMethodMetadata metadata = analyticsMethodsRepository.findOne(id);

        if(metadata == null || id == null || id.isEmpty()){
            throw new AnalyticsMethodNotFoundException("Analytics Method with id = {"
                    + id + "} not found.");
        }

        // Delete Files
        fileHandler.deleteFile(analyticsMethodsJarsFolder, metadata.getFilename());

        // Delete from database
        analyticsMethodsRepository.delete(metadata);

    }
}
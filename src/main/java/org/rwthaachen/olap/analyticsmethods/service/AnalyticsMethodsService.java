package org.rwthaachen.olap.analyticsmethods.service;

import org.h2.jdbc.JdbcSQLException;
import org.rwthaachen.olap.analyticsmethods.AnalyticsMethodsApplication;
import org.rwthaachen.olap.analyticsmethods.dataAccess.AnalyticsMethodsRepository;
import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodNotFoundException;
import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodsBadRequestException;
import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodsUploadErrorException;
import org.rwthaachen.olap.analyticsmethods.model.AnalyticsMethodMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import java.util.List;

/**
 * This service handles the "business logic" of the macro component. It also works as a facade for other macro components
 * that happen to be running on the same server, i.e. the Analyics Engine.
 */

@Service
public class AnalyticsMethodsService {

    // Strings for method names
    private static final String JAR_EXTENSION = ".jar";
    private static final String TEMP_FILE_SUFIX = "_temp";

    @Value("${analyticsMethodsJarFolder}")
    String analyticsMethodsJarsFolder;

    @Autowired
    AnalyticsMethodsRepository analyticsMethodsRepository;

    @Autowired
    DataBaseLoader databaseLoader;

    @Autowired
    AnalyticsMethodsUploadValidator validator;

    private	static	final Logger log =
            LoggerFactory.getLogger(AnalyticsMethodsApplication.class);

    /**
     *
     * @return
     */
    public List<AnalyticsMethodMetadata> viewAllAnalyticsMethods() {
        ArrayList<AnalyticsMethodMetadata> result = new ArrayList<AnalyticsMethodMetadata>();
        analyticsMethodsRepository.findAll().forEach(result :: add);
        return result;
    }

    /**
     *
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
     *
     * @param methodMetadata
     * @param jarBundle
     * @return
     */
    public AnalyticsMethodMetadata uploadAnalyticsMethod(
            AnalyticsMethodMetadata methodMetadata, MultipartFile jarBundle) {

        AnalyticsMethodsValidationInformation validationInformation;
        FileHandler fileHandler = new FileHandler();

        if (!jarBundle.isEmpty())
        {
            try
            {
                // Save the jar in the filesystem
                fileHandler.saveFile(jarBundle, analyticsMethodsJarsFolder, methodMetadata.getName() + JAR_EXTENSION);

                //Validation
                validationInformation = validator.validatemethod(methodMetadata, analyticsMethodsJarsFolder);
                if (!validationInformation.isValid())
                {
                    // If the submitted jar is not valid, remove it from the filesystem
                    fileHandler.deleteFile(analyticsMethodsJarsFolder, methodMetadata.getName() + JAR_EXTENSION);
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
     *
     * @param methodMetadata
     * @param id
     *@param jarBundle  @return
     */

    public AnalyticsMethodMetadata updateAnalyticsMethod(AnalyticsMethodMetadata methodMetadata, String id, MultipartFile jarBundle) {
        //TODO implement
        AnalyticsMethodsValidationInformation validationInformation;
        FileHandler fileHandler = new FileHandler();

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
                    tempMetadata.setName(tempMetadata.getName() + TEMP_FILE_SUFIX);
                    fileHandler.saveFile(jarBundle, analyticsMethodsJarsFolder, tempMetadata.getName()
                            + JAR_EXTENSION);
                    //Perform validation with than name.
                    //If valid, delete the old jar with the new one, check that there are no leftovers
                    validationInformation = validator.validatemethod(tempMetadata, analyticsMethodsJarsFolder);
                    if (validationInformation.isValid())
                    {
                        //delete temp file
                        fileHandler.deleteFile(analyticsMethodsJarsFolder, tempMetadata.getName()
                                + JAR_EXTENSION);
                        //write real file
                        fileHandler.saveFile(jarBundle, analyticsMethodsJarsFolder, methodMetadata.getName()
                                + JAR_EXTENSION);
                        //update metadata
                        result.updateWithMetadata(methodMetadata);
                        return analyticsMethodsRepository.save(result);
                    }
                    else
                    {
                        //delete temp file
                        fileHandler.deleteFile(analyticsMethodsJarsFolder, tempMetadata.getName()
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
}

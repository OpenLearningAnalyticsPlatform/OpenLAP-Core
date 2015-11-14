package org.rwthaachen.olap.analyticsmethods.service;

import org.rwthaachen.olap.analyticsmethods.AnalyticsMethodsApplication;
import org.rwthaachen.olap.analyticsmethods.dataAccess.AnalyticsMethodsRepository;
import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodNotFoundException;
import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodsUploadErrorException;
import org.rwthaachen.olap.analyticsmethods.model.AnalyticsMethodMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
    public static final String VIEW_ANALYTICS_METHOD = "viewAnalyticsMethod";

    @Value("${analyticsMethodsJarFolder}")
    String analyticsMethodsJarsFolder;

    @Autowired
    AnalyticsMethodsRepository analyticsMethodsRepository;

    @Autowired
    DataBaseLoader databaseLoader;

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
            throw new AnalyticsMethodNotFoundException(VIEW_ANALYTICS_METHOD + "/" + id);
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
        if (!jarBundle.isEmpty())
        {
            try
            {
                byte[] bytes = jarBundle.getBytes();

                BufferedOutputStream stream = new BufferedOutputStream
                        (
                                new FileOutputStream
                                (
                                        new File(analyticsMethodsJarsFolder + methodMetadata.getName() + ".jar")
                                )
                        );

                stream.write(bytes);
                stream.close();

                //TODO Validation
                // validator.validatemethod(metadata)
                // Make the metadata contain the name of the class that implements the AnaltyicsMethod and make the
                // binariesLocation an optional attribute for the upload request.
                // validate and if error, throw an invalidMethod exception, should be also a bad request.
                return methodMetadata;
            } catch (IOException e) {
                e.printStackTrace();
                throw new AnalyticsMethodsUploadErrorException(e.getMessage());
            }
        }
        throw new AnalyticsMethodsUploadErrorException("Empty jar bundle");
    }

}

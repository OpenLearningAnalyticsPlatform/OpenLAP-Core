package org.rwthaachen.olap.analyticsmethods.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rwthaachen.olap.analyticsmethods.AnalyticsMethodsApplication;
import org.rwthaachen.olap.analyticsmethods.model.AnalyticsMethodMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

/**
 * A service class bean to load data programatically when using H2 for testing
 */
@Service
@Profile("development")
public class DataBaseLoader {

    private static final String RESOURCE_JSON_FOR_UPDATING_MANIFEST =
            "development/testManifests/UploadMethodManifestForDevelopment.json";
    private static final String RESOURCE_JAR_UPLOAD_FOR_UPDATING =
            "development/testJars/AnalyticsMethodForDevelopment.jar";


    @Autowired
    private	AnalyticsMethodsService service;

    private	static final Logger log	=
            LoggerFactory.getLogger(AnalyticsMethodsApplication.class);

    @PostConstruct
    private void initDatabase()
    {
        ObjectMapper mapper = new ObjectMapper();
        //InputStream is = getClass().getClassLoader().getResourceAsStream("/development/testManifests/UploadMethodManifestForDevelopment.json");
        URL dirURL = getClass().getClassLoader().getResource(RESOURCE_JAR_UPLOAD_FOR_UPDATING);
        String methodJson = "{\n" +
                "  \"id\":\"1\",\n" +
                "  \"name\":\"Development Analytics Method\",\n" +
                "  \"creator\":\"lechip\",\n" +
                "  \"description\":\"Analytics Method for Development Environment.\",\n" +
                "  \"filename\":\"UploadMethodManifestForDevelopment\",\n" +
                "  \"implementingClass\": \"main.AnalyticsMethodForDevelopment\"\n" +
                "}";
        log.info("Started DataBaseLoader initDatabase");

        AnalyticsMethodMetadata metadata1 = null;

        try {
            //metadata1 = mapper.readValue(IOUtils.toString(is),AnalyticsMethodMetadata.class);
            metadata1 = mapper.readValue(methodJson,AnalyticsMethodMetadata.class);
            // Load the directory as a resource

            File file = new File(dirURL.toURI());
            // Make an input stream for the mock
            FileInputStream fi1 = new FileInputStream(file);

            MultipartFile mpf = new MockMultipartFile("jarBundle", file.getName(), "multipart/form-data",fi1);

            service.uploadAnalyticsMethod(metadata1, mpf);
            log.info("Logged metadata: " + metadata1.toString());

        } catch (Exception e) {
            log.info("DataBaseLoader PostConstruct Failed");
            e.printStackTrace();
        }

    }


}

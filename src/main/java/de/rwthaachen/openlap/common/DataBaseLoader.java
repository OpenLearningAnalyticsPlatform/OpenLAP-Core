package de.rwthaachen.openlap.common;

import DataSet.OLAPPortConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rwthaachen.openlap.OpenLAPCoreApplication;
import de.rwthaachen.openlap.analyticsmethods.model.AnalyticsMethodMetadata;
import de.rwthaachen.openlap.analyticsmethods.service.AnalyticsMethodsService;
import de.rwthaachen.openlap.analyticsmodules.model.AnalyticsGoal;
import de.rwthaachen.openlap.analyticsmodules.model.IndicatorReference;
import de.rwthaachen.openlap.analyticsmodules.model.Triad;
import de.rwthaachen.openlap.analyticsmodules.model.VisualizerReference;
import de.rwthaachen.openlap.analyticsmodules.service.AnalyticsModulesService;
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
 * A service class bean to load data programmatically when using H2 for testing
 */
@Service
@Profile("development")
public class DataBaseLoader {

    private static final String RESOURCE_JSON_FOR_UPDATING_MANIFEST =
            "development/testManifests/UploadMethodManifestForDevelopment.json";
    private static final String RESOURCE_JAR_UPLOAD_FOR_UPDATING =
            "development/testJars/AnalyticsMethodForDevelopment.jar";
    private static final Logger log =
            LoggerFactory.getLogger(OpenLAPCoreApplication.class);
    ObjectMapper mapper;
    @Autowired
    private AnalyticsMethodsService analyticsMethodsService;
    @Autowired
    private AnalyticsModulesService analyticsModulesService;

    /**
     * TODO
     */
    @PostConstruct
    private void initDatabase() {
        mapper = new ObjectMapper();
        AnalyticsMethodMetadata preloadedMetadata;
        Triad preloadedTriad;

        preloadedMetadata = loadAnalyticsMethod();
        preloadedTriad = loadTriad(preloadedMetadata);
        loadAnalyticsGoal(preloadedMetadata);

    }

    /**
     * TODO
     *
     * @return
     */
    private AnalyticsMethodMetadata loadAnalyticsMethod() {
        AnalyticsMethodMetadata result = null;

        //InputStream is = getClass().getClassLoader().getResourceAsStream("/development/testManifests/UploadMethodManifestForDevelopment.json");
        URL dirURL = getClass().getClassLoader().getResource(RESOURCE_JAR_UPLOAD_FOR_UPDATING);
        log.info("Started DataBaseLoader initDatabase");


        try {
            //metadata1 = mapper.readValue(IOUtils.toString(is),AnalyticsMethodMetadata.class);
            result = mapper.readValue(getJsonString(JsonGeneratorIndex.ANALYTICS_METHOD_MANIFEST),
                    AnalyticsMethodMetadata.class);
            // Load the directory as a resource

            File file = new File(dirURL.toURI());
            // Make an input stream for the mock
            FileInputStream fi1 = new FileInputStream(file);

            MultipartFile mpf = new MockMultipartFile("jarBundle", file.getName(), "multipart/form-data", fi1);

            analyticsMethodsService.uploadAnalyticsMethod(result, mpf);
            log.info("Loaded metadata: " + result.toString());
            return result;

        } catch (Exception e) {
            log.info("DataBaseLoader PostConstruct Failed loading Analytics Method");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * TODO
     *
     * @param preloadedMetadata
     * @return
     */
    private Triad loadTriad(AnalyticsMethodMetadata preloadedMetadata) {
        // Create a Triad
        // String indicator = "indicator1";
        IndicatorReference indicator = new IndicatorReference(1, "indicator1", "The first indicator ever");
        //String visualization = "visualization1";
        VisualizerReference visualization = new VisualizerReference(1, 1, "visualization1", "Something made by Bassim");

        // Save the Triad using the service
        try {
            OLAPPortConfiguration indicatorToMethodMapping = mapper.readValue(
                    getJsonString(JsonGeneratorIndex.OLAPCONFIGURATION_INDICATOR_TO_METHOD),
                    OLAPPortConfiguration.class);
            OLAPPortConfiguration methodToVisualizationMapping = mapper.readValue(
                    getJsonString(JsonGeneratorIndex.OLAPCONFIGURATION_INDICATOR_TO_METHOD),
                    OLAPPortConfiguration.class);
            Triad triad = new Triad(indicator, preloadedMetadata, visualization,
                    indicatorToMethodMapping, methodToVisualizationMapping);
            Triad result = analyticsModulesService.saveTriad(triad);
            log.info("Loaded Triad: " + result.toString());
            return result;
        } catch (Exception e) {
            log.info("DataBaseLoader PostConstruct Failed loading Triad");
            e.printStackTrace();
            return null;
        }

    }

    /**
     * TODO
     *
     * @param preloadedMetadata
     */
    private void loadAnalyticsGoal(AnalyticsMethodMetadata preloadedMetadata) {
        //Create a AnalyticsGoal
        AnalyticsGoal analyticsGoal = new AnalyticsGoal("AnalyticsGoal1", "Testing Learning Goal", "lechip", false);
        try {
            analyticsGoal = analyticsModulesService.saveAnalyticsGoal(analyticsGoal);
            analyticsGoal = analyticsModulesService.setAnalyticsGoalActive(analyticsGoal.getId(), true);
            analyticsGoal = analyticsModulesService.addAnalyticsMethodToAnalyticsGoal(analyticsGoal.getId(),
                    preloadedMetadata);
            // To test that does not save twice
            analyticsGoal = analyticsModulesService.addAnalyticsMethodToAnalyticsGoal(analyticsGoal.getId(),
                    preloadedMetadata);
            log.info("Loaded AnalyticsGoal: " + analyticsGoal.toString());
        } catch (Exception e) {
            log.info("DataBaseLoader PostConstruct Failed loading AnalyticsGoal");
            e.printStackTrace();
        }
    }

    /**
     * Used to generate a string with the json content of different files. It is done like this because
     * it is not possible to load resources when the postexecute methods are run
     *
     * @param index an index to the particular JSON string
     * @return a String containing JSON data
     */
    private String getJsonString(JsonGeneratorIndex index) {
        switch (index) {
            case OLAPCONFIGURATION_INDICATOR_TO_METHOD:
                return "{" +
                        "  \"mapping\" : [" +
                        "    {" +
                        "      \"outputPort\" : {" +
                        "        \"type\" : \"STRING\"," +
                        "        \"id\" : \"incomingColumn\"," +
                        "        \"required\" : false" +
                        "      }," +
                        "      \"inputPort\" : {" +
                        "        \"type\" : \"STRING\"," +
                        "        \"id\" : \"inputColumn1\"," +
                        "        \"required\" : true" +
                        "      }" +
                        "    }" +
                        "  ]" +
                        "}";
            case OLAPCONFIGURATION_METHOD_TO_INDICATOR:
                return "{" +
                        "  \"mapping\" : [" +
                        "    {" +
                        "      \"outputPort\" : {" +
                        "        \"type\" : \"INTEGER\"," +
                        "        \"id\" : \"outputColumn1\"," +
                        "        \"required\" : false" +
                        "      }," +
                        "      \"inputPort\" : {" +
                        "        \"type\" : \"INTEGER\"," +
                        "        \"id\" : \"visualizerInputColumn1\"," +
                        "        \"required\" : true" +
                        "      }" +
                        "    }" +
                        "  ]" +
                        "}";
            case ANALYTICS_METHOD_MANIFEST:
                return "{" +
                        "  \"id\":\"1\"," +
                        "  \"name\":\"Development Analytics Method\"," +
                        "  \"creator\":\"lechip\"," +
                        "  \"description\":\"Analytics Method for Development Environment.\"," +
                        "  \"filename\":\"UploadMethodManifestForDevelopment\"," +
                        "  \"implementingClass\": \"main.AnalyticsMethodForDevelopment\"" +
                        "}";
            default:
                return "";
        }
    }

    private enum JsonGeneratorIndex {
        OLAPCONFIGURATION_INDICATOR_TO_METHOD,
        OLAPCONFIGURATION_METHOD_TO_INDICATOR,
        ANALYTICS_METHOD_MANIFEST
    }
}

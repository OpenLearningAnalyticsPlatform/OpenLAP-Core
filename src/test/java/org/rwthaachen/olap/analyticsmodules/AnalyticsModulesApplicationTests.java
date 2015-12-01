package org.rwthaachen.olap.analyticsmodules;

import OLAPDataSet.OLAPPortConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rwthaachen.olap.OpenLAPCoreApplication;
import org.rwthaachen.olap.analyticsmethods.dataAccess.AnalyticsMethodsRepository;
import org.rwthaachen.olap.analyticsmethods.model.AnalyticsMethodMetadata;
import org.rwthaachen.olap.analyticsmodules.controller.AnalyticsModulesController;
import org.rwthaachen.olap.analyticsmodules.dataAccess.LearningGoalsRepository;
import org.rwthaachen.olap.analyticsmodules.dataAccess.TriadsRepository;
import org.rwthaachen.olap.analyticsmodules.model.IndicatorReference;
import org.rwthaachen.olap.analyticsmodules.model.LearningGoal;
import org.rwthaachen.olap.analyticsmodules.model.Triad;
import org.rwthaachen.olap.analyticsmodules.model.VisualizerReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Created by lechip on 28/11/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = OpenLAPCoreApplication.class)
// Necessary for getting the autowired WebApplicationContext
@WebAppConfiguration
@ActiveProfiles("development")
public class AnalyticsModulesApplicationTests {

    // FILES
    private static final String RESOURCE_JAR_TEST_METHOD =
            "frameworkImplementationJar/AnalyticsMethodForTesting.jar";
    //MANIFESTS
    private static final String RESOURCE_JSON_MANIFEST_TEST_METHOD =
            "jsonManifest/UploadMethodManifestForTesting.json";


    private	static final Logger log =
            LoggerFactory.getLogger(OpenLAPCoreApplication.class);

    // Won't work Test without the WebAppConfiguration
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private AnalyticsModulesController controller;

    @Autowired
    private LearningGoalsRepository learningGoalsRepository;

    @Autowired
    private TriadsRepository triadsRepository;

    @Autowired
    private AnalyticsMethodsRepository analyticsMethodsRepository;

    private MockMvc mockMvc;

    @Value("${analyticsMethodsJarsFolder}")
    String analyticsMethodsJarsFolder;
    String testingLearningGoalId;
    String testingTriadId;
    String testingMethodId;

    @Before
    public void setUp() throws Exception{
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        //Create a Method to test
        saveTestingMethod();
        saveTestingLearningGoal();
        saveTestingTriad();
    }

    @After
    public void cleanUp() throws Exception{
        // Delete Jar folder
        deleteFolder(analyticsMethodsJarsFolder);
        analyticsMethodsRepository.deleteAll();
        triadsRepository.deleteAll();
        learningGoalsRepository.deleteAll();
    }

    //region Triads

    @Test
    public void saveTriadTest() throws Exception{
        //Test with valid Triad
        ObjectMapper mapper = new ObjectMapper();
        //Get the AnalyticsMethod
        AnalyticsMethodMetadata analyticsMethodForTesting =  analyticsMethodsRepository.findOne(testingMethodId);
        //Create a Triad
        OLAPPortConfiguration config1 =
                mapper.readValue(getJsonString(JsonGeneratorIndex.OLAPCONFIGURATION_INDICATOR_TO_METHOD),
                        OLAPPortConfiguration.class);
        OLAPPortConfiguration config2 =
                mapper.readValue(getJsonString(JsonGeneratorIndex.OLAPCONFIGURATION_METHOD_TO_INDICATOR),
                        OLAPPortConfiguration.class);
        Triad triadForTesting = new Triad
                (
                        new IndicatorReference(1,"IndicatorInTest","An indicator"),
                        analyticsMethodForTesting,
                        new VisualizerReference(1,"VisualizerInTest","A Visualization"),
                        config1,
                        config2
                );
        //Put the Triad as a String
        String triadAsJsonString = triadForTesting.toString();
        //Save it through the controller
        MvcResult result = mockMvc.perform
                (
                        post("/AnalyticsModules/Triads/")
                                .contentType(MediaType.APPLICATION_JSON).content(triadAsJsonString)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analyticsMethodReference.id").value(testingMethodId))
                .andExpect(jsonPath("$.indicatorReference.id").value(1))
                .andExpect(jsonPath("$.visualizationReference.id").value(1))
                .andExpect(jsonPath("$.indicatorToAnalyticsMethodMapping").exists())
                .andExpect(jsonPath("$.analyticsMethodToVisualizationMapping").exists())
                .andReturn();
        log.info("TEST - Triad: " + result.getResponse().getContentAsString());
    }

    @Test
    public void getTriadByIdTest(){
        //TODO Test with get a valid Triad
        //TODO Test with get an invalid Triad
    }

    @Test
    public void getAllTriadsTest(){
        //TODO Test getting all Triads
    }

    @Test
    public void updateTriadTest(){
        //TODO test updating existing Triad
        //TODO test updating invalid Triad
    }

    @Test
    public void deleteTriadTest(){
        //TODO test deleting existing Triad
        //TODO test deleting invalid Triad
    }
    //endregion

    //region LearningGoals
    @Test
    public void getLearningGoalByIdTest(){
        //TODO Test getting valid LearningGoal
        //TODO Test getting invalid LearningGoal
        //TODO Test getting unapproved LearningGoal
    }

    @Test
    public void saveLearningGoalTest(){
        //TODO Test creating valid LearningGoal
        //TODO Test creating invalid LearningGoal
    }

    @Test
    public void authorizeLearningGoalTest(){
        //TODO Test approving a valid LearningGoal
        //TODO Test approving an invalid LearningGoal
    }

    @Test
    public void getAllLearningGoalsTest(){
        //TODO Test
    }


    @Test
    public void addAnalyticsMethodToLearningGoalTest(){
        //TODO Test linking existing AnalyticsMethod to existing and approved LearningGoal
        //TODO Test linking exsiting AnalyticsMethod to exsiting but not approved LearningGoal
        //TODO Test linking nonexisting AnalyticsMethod to existing but not approved LearningGoal
        //TODO Test linking existing AnalyticsMethod to nonexisting but not approved LearningGoal
    }

    @Test
    public void updateLearningGoalTest(){
        //TODO test updating existing LearningGoal
        //TODO test updating invalid LearningGoal
    }

    @Test
    public void deleteLearningGoalTest(){
        //TODO test deleting existing LearningGoal
        //TODO test deleting invalid LearningGoal
    }


    //endregion

    //region Utility Methods

    private void deleteFolder(String dir) {
        File directoryToDelete = new File(dir);
        try {
            log.info("TEST - Deleting folder :" + directoryToDelete);
            FileUtils.deleteDirectory(directoryToDelete);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Failed to delete :" + directoryToDelete);
        }
    }

    private void saveTestingMethod() throws Exception{
        // Test with jar that implements the framework correctly
        MockMultipartFile fstmp = prepareMultiPartFile(RESOURCE_JAR_TEST_METHOD);
        String jsonTxt1 = prepareJsonString(RESOURCE_JSON_MANIFEST_TEST_METHOD);
        MvcResult result = mockMvc.perform
                (
                        MockMvcRequestBuilders.fileUpload("/AnalyticsMethods")
                                .file(fstmp)
                                .param("methodMetadata",jsonTxt1)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        log.info("TEST - AnalyticsMethod uploaded: " + result.getResponse().getContentAsString());
        testingMethodId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        log.info("TEST - AnalyticsMethod id: " + testingMethodId);
    }

    private void saveTestingLearningGoal() throws Exception{
        //Create the LearningGoal
        LearningGoal testLearningGoal = new LearningGoal("Test LearningGoal", "A Learning Goal", "lechip", true);
        //Put the LearningGoal as a String
        String learningGoalAsJsonString = testLearningGoal.toString();
        MvcResult result = mockMvc.perform
                (
                        post("/AnalyticsModules/LearningGoals/")
                        .contentType(MediaType.APPLICATION_JSON).content(learningGoalAsJsonString)
                )
                .andExpect(status().isOk())
                .andReturn();
        log.info("TEST - LearningGoal uploaded (is not approved): " + result.getResponse().getContentAsString());
        testingLearningGoalId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        log.info("TEST - LearningGoal id: " + testingLearningGoalId);
    }

    private void saveTestingTriad() throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        //Get the AnalyticsMethod
        AnalyticsMethodMetadata analyticsMethodForTesting =  analyticsMethodsRepository.findOne(testingMethodId);
        //Create a Triad
        OLAPPortConfiguration config1 =
                mapper.readValue(getJsonString(JsonGeneratorIndex.OLAPCONFIGURATION_INDICATOR_TO_METHOD),
                        OLAPPortConfiguration.class);
        OLAPPortConfiguration config2 =
                mapper.readValue(getJsonString(JsonGeneratorIndex.OLAPCONFIGURATION_METHOD_TO_INDICATOR),
                        OLAPPortConfiguration.class);
        Triad triadForTesting = new Triad
                (
                        new IndicatorReference(1,"Indicator1","An indicator"),
                        analyticsMethodForTesting,
                        new VisualizerReference(1,"Visualizer1","A Visualization"),
                        config1,
                        config2
                );
        //Put the Triad as a String
        String triadAsJsonString = triadForTesting.toString();
        //Save it through the controller
        MvcResult result = mockMvc.perform
                (
                        post("/AnalyticsModules/Triads/")
                                .contentType(MediaType.APPLICATION_JSON).content(triadAsJsonString)
                )
                .andExpect(status().isOk())
                .andReturn();
        log.info("TEST - Triad: " + result.getResponse().getContentAsString());
        testingTriadId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        log.info("TEST - Triad id: " + testingTriadId);
    }

    /**
     * Helper method to prepare a String Test with the JSON of a resource file
     * @param resourceJsonUploadManifest the complete filepath of the resource json
     * @return a String Test with the Json read from the resource json file
     * @throws Exception
     */
    private String prepareJsonString(String resourceJsonUploadManifest) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourceJsonUploadManifest);
        return IOUtils.toString(is);
    }

    /**
     * Helper method to prepare a MockMultipartFile Test with the content of a file in the resource folder
     * @param resourceJarUpload the complete filepath of the resource jar file
     * @return a MockMultipartFile Test with the content of the resource jar file
     * @throws Exception
     */
    private MockMultipartFile prepareMultiPartFile(String resourceJarUpload) throws Exception {
        // Load the directory as a resource
        URL dirURL = getClass().getClassLoader().getResource(resourceJarUpload);
        log.info("TEST - Resource dir URL: " + dirURL.toString());
        // Make file from the uri
        File file = new File(dirURL.toURI());
        // Check file content
        log.info("TEST - File check: isFile:" + file.isFile()
                + ",  fileName: " +file.getName()
                + ",  exists: " + file.exists());
        // Make an input stream for the mock
        FileInputStream fi1 = new FileInputStream(file);
        // Create a mock
        return new MockMultipartFile("jarBundle", file.getName(), "multipart/form-data",fi1);
    }

    /**
     * Used to generate a string with the json content of different files. It is done like this because
     * it is not possible to load resources when the postexecute methods are run
     * @param index an index to the particular JSON string
     * @return a String containing JSON data
     */
    private String getJsonString(JsonGeneratorIndex index) {
        switch (index)
        {
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
                return  "{" +
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

    private enum JsonGeneratorIndex{
        OLAPCONFIGURATION_INDICATOR_TO_METHOD,
        OLAPCONFIGURATION_METHOD_TO_INDICATOR,
        ANALYTICS_METHOD_MANIFEST
    }
    //endregion
}

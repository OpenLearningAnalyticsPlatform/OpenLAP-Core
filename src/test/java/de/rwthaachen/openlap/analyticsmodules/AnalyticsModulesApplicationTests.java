package de.rwthaachen.openlap.analyticsmodules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import de.rwthaachen.openlap.OpenLAPCoreApplication;
import de.rwthaachen.openlap.analyticsmethods.dataaccess.AnalyticsMethodsRepository;
import de.rwthaachen.openlap.analyticsmethods.model.AnalyticsMethodMetadata;
import de.rwthaachen.openlap.analyticsmodules.controller.AnalyticsModulesController;
import de.rwthaachen.openlap.analyticsmodules.dataaccess.AnalyticsGoalRepository;
import de.rwthaachen.openlap.analyticsmodules.dataaccess.TriadsRepository;
import de.rwthaachen.openlap.analyticsmodules.model.AnalyticsGoal;
import de.rwthaachen.openlap.analyticsmodules.model.IndicatorReference;
import de.rwthaachen.openlap.analyticsmodules.model.Triad;
import de.rwthaachen.openlap.analyticsmodules.model.VisualizerReference;
import de.rwthaachen.openlap.dataset.OpenLAPPortConfig;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the Analytics Modules macro component
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


    private static final Logger log =
            LoggerFactory.getLogger(OpenLAPCoreApplication.class);
    @Value("${analyticsMethodsJarsFolder}")
    String analyticsMethodsJarsFolder;
    long testingAnalyticsGoalId;
    long testingTriadId;
    long testingMethodId;
    // Won't work Test without the WebAppConfiguration
    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private AnalyticsModulesController controller;
    @Autowired
    private AnalyticsGoalRepository analyticsGoalRepository;
    @Autowired
    private TriadsRepository triadsRepository;
    @Autowired
    private AnalyticsMethodsRepository analyticsMethodsRepository;
    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        //Create a Method to test
        saveTestingMethod();
        saveTestingAnalyticsGoal();
        saveTestingTriad();
    }

    @After
    public void cleanUp() throws Exception {
        // Delete Jar folder
        deleteFolder(analyticsMethodsJarsFolder);
        analyticsMethodsRepository.deleteAll();
        triadsRepository.deleteAll();
        analyticsGoalRepository.deleteAll();
    }

    //region Triads

    @Test
    public void saveTriadTest() throws Exception {
        //Test with valid Triad
        /*ObjectMapper mapper = new ObjectMapper();
        //Get the AnalyticsMethod
        AnalyticsMethodMetadata analyticsMethodForTesting = analyticsMethodsRepository.findOne(testingMethodId);
        //Create a Triad
        OpenLAPPortConfig config1 =
                mapper.readValue(getJsonString(JsonGeneratorIndex.OLAPCONFIGURATION_INDICATOR_TO_METHOD),
                        OpenLAPPortConfig.class);
        OpenLAPPortConfig config2 =
                mapper.readValue(getJsonString(JsonGeneratorIndex.OLAPCONFIGURATION_METHOD_TO_INDICATOR),
                        OpenLAPPortConfig.class);
        Triad triadForTesting = new Triad
                (
                        new IndicatorReference(1, "An indicator"),
                        analyticsMethodForTesting,
                        new VisualizerReference(1, 1, "VisualizerFramework", "VisualizerMethod"),
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
        log.info("TEST - Triad: " + result.getResponse().getContentAsString());*/
    }

    @Test
    public void getTriadByIdTest() throws Exception {
        MvcResult result;

        //Test with get a valid Triad
        result = mockMvc.perform(get("/AnalyticsModules/Triads/" + testingTriadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testingTriadId))
                .andExpect(jsonPath("$.indicatorReference").exists())
                .andExpect(jsonPath("$.analyticsMethodReference").exists())
                .andExpect(jsonPath("$.visualizationReference").exists())
                .andExpect(jsonPath("$.indicatorToAnalyticsMethodMapping").exists())
                .andExpect(jsonPath("$.analyticsMethodToVisualizationMapping").exists())
                .andReturn();
        log.info("TEST - Triads response content: " + result.getResponse().getContentAsString());

        //Test with get an invalid Triad
        result = mockMvc.perform(get("/AnalyticsModules/Triads/wrongId"))
                .andExpect(status().isNotFound())
                .andReturn();
        log.info("TEST - Triads response content: " + result.getResponse().getContentAsString());
    }

    @Test
    public void getAllTriadsTest() throws Exception {
        MvcResult result;
        // Test getting all Triads
        result = mockMvc.perform(get("/AnalyticsModules/Triads/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].indicatorReference").exists())
                .andExpect(jsonPath("$[0].analyticsMethodReference").exists())
                .andExpect(jsonPath("$[0].visualizationReference").exists())
                .andExpect(jsonPath("$[0].indicatorToAnalyticsMethodMapping").exists())
                .andExpect(jsonPath("$[0].analyticsMethodToVisualizationMapping").exists())
                .andReturn();
        log.info("TEST - Triads response content: " + result.getResponse().getContentAsString());
    }

    @Test
    public void updateTriadTest() throws Exception {
        /*ObjectMapper mapper = new ObjectMapper();
        //Get the AnalyticsMethod
        AnalyticsMethodMetadata analyticsMethodForTesting = analyticsMethodsRepository.findOne(testingMethodId);
        analyticsMethodForTesting.setDescription("updated");
        //Create a Triad
        OpenLAPPortConfig config1 =
                mapper.readValue(getJsonString(JsonGeneratorIndex.OLAPCONFIGURATION_INDICATOR_TO_METHOD),
                        OpenLAPPortConfig.class);
        OpenLAPPortConfig config2 =
                mapper.readValue(getJsonString(JsonGeneratorIndex.OLAPCONFIGURATION_METHOD_TO_INDICATOR),
                        OpenLAPPortConfig.class);
        Triad triadForTesting = new Triad
                (
                        new IndicatorReference(1, "An indicator"),
                        analyticsMethodForTesting,
                        new VisualizerReference(1, 1, "VisualizerFrameworkUpdate", "VisualizerMethodUpdate"),
                        config1,
                        config2
                );
        //Put the Triad as a String
        String triadAsJsonString = triadForTesting.toString();
        // Test updating existing Triad
        MvcResult result = mockMvc.perform
                (
                        put("/AnalyticsModules/Triads/" + testingTriadId)
                                .contentType(MediaType.APPLICATION_JSON).content(triadAsJsonString)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analyticsMethodReference.id").value(testingMethodId))
                .andExpect(jsonPath("$.analyticsMethodReference.description").value("updated"))
                .andExpect(jsonPath("$.indicatorReference.id").value(1))
                .andExpect(jsonPath("$.visualizationReference.id").value(1))
                .andExpect(jsonPath("$.indicatorToAnalyticsMethodMapping").exists())
                .andExpect(jsonPath("$.analyticsMethodToVisualizationMapping").exists())
                .andReturn();
        log.info("TEST - Triad update: " + result.getResponse().getContentAsString());
        // Test updating invalid Triad
        MvcResult result2 = mockMvc.perform
                (
                        put("/AnalyticsModules/Triads/" + testingTriadId)
                                .contentType(MediaType.APPLICATION_JSON).content(analyticsMethodForTesting
                                .toString())
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        log.info("TEST - Triad wrong update: " + result2.getResponse().getContentAsString());*/
    }

    @Test
    public void deleteTriadTest() throws Exception {
        MvcResult result;

        // Test deleting invalid Triad
        result = mockMvc.perform(delete("/AnalyticsModules/Triads/worngId"))
                .andExpect(status().isNotFound())
                .andReturn();
        log.info("TEST - Delete wrong Triad response content: " + result.getResponse().getContentAsString());

        // Test deleting existing Triad
        result = mockMvc.perform(delete("/AnalyticsModules/Triads/" + testingTriadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..status").value(200))
                .andExpect(jsonPath("$..message").value("Triad with id {" + testingTriadId + "} deleted"))
                .andReturn();
        log.info("TEST - Delete Triad content: " + result.getResponse().getContentAsString());


    }
    //endregion

    //region AnalyticsGoals
    @Test
    public void getAnalyticsGoalByIdTest() throws Exception {
        MvcResult result;
        // Test getting valid AnalyticsGoal
        result = mockMvc.perform(get("/AnalyticsModules/AnalyticsGoals/" + testingAnalyticsGoalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testingAnalyticsGoalId))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.author").exists())
                .andExpect(jsonPath("$.active").value(false))
                .andReturn();
        log.info("TEST - AnalyticsGoals get by Id result: " + result.getResponse().getContentAsString());

        // Test getting invalid AnalyticsGoal
        result = mockMvc.perform(get("/AnalyticsModules/AnalyticsGoals/wrongId"))
                .andExpect(status().isNotFound())
                .andReturn();
        log.info("TEST - AnalyticsGoals get by wrong Id result: " + result.getResponse().getContentAsString());
    }

    @Test
    public void saveAnalyticsGoalTest() throws Exception {
        // Test creating valid AnalyticsGoal
        // Create the AnalyticsGoal
        AnalyticsGoal testAnalyticsGoal = new AnalyticsGoal("Test AnalyticsGoal 2", "A Analytics Goal", "lechip", true);
        // Put the AnalyticsGoal as a String
        String analyticsGoalAsJsonString = testAnalyticsGoal.toString();
        MvcResult result = mockMvc.perform
                (
                        post("/AnalyticsModules/AnalyticsGoals/")
                                .contentType(MediaType.APPLICATION_JSON).content(analyticsGoalAsJsonString)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test AnalyticsGoal 2"))
                .andExpect(jsonPath("$.author").exists())
                .andExpect(jsonPath("$.active").value(false))
                .andReturn();
        log.info("TEST - AnalyticsGoal saved: " + result.getResponse().getContentAsString());
        // Test creating invalid AnalyticsGoal
        result = mockMvc.perform
                (
                        post("/AnalyticsModules/AnalyticsGoals/")
                                .contentType(MediaType.APPLICATION_JSON).content("wrongString")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        log.info("TEST - AnalyticsGoal saving failure test: " + result.getResponse().getContentAsString());

    }

    @Test
    public void authorizeAnalyticsGoalTest() throws Exception {
        MvcResult result;
        // Test approving a valid AnalyticsGoal
        result = mockMvc.perform(put("/AnalyticsModules/AnalyticsGoals/" + testingAnalyticsGoalId + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testingAnalyticsGoalId))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.author").exists())
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();
        log.info("TEST - AnalyticsGoals activate: " + result.getResponse().getContentAsString());

        result = mockMvc.perform(put("/AnalyticsModules/AnalyticsGoals/" + testingAnalyticsGoalId + "/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testingAnalyticsGoalId))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.author").exists())
                .andExpect(jsonPath("$.active").value(false))
                .andReturn();
        log.info("TEST - AnalyticsGoals activate: " + result.getResponse().getContentAsString());

        // Test approving an invalid AnalyticsGoal
        result = mockMvc.perform(put("/AnalyticsModules/AnalyticsGoals/wrongId/activate"))
                .andExpect(status().isNotFound())
                .andReturn();
        log.info("TEST - AnalyticsGoals activate with wrong Id result: " + result.getResponse().getContentAsString());

    }

    @Test
    public void getAllAnalyticsGoalsTest() throws Exception {
        // Test
        MvcResult result;
        result = mockMvc.perform(get("/AnalyticsModules/AnalyticsGoals/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].author").exists())
                .andExpect(jsonPath("$[0].active").exists())
                .andReturn();
        log.info("TEST - AnalyticsGoals get all: " + result.getResponse().getContentAsString());
    }


    @Test
    public void addAnalyticsMethodToAnalyticsGoalTest() throws Exception {
        MvcResult result;
        // Test linking exsiting AnalyticsMethod to exsiting but not approved AnalyticsGoal
        AnalyticsMethodMetadata methodMetadata = analyticsMethodsRepository.findOne(testingMethodId);

        String metadataAsString = methodMetadata.toString();
        result = mockMvc.perform
                (
                        put("/AnalyticsModules/AnalyticsGoals/" + testingAnalyticsGoalId + "/addAnalyticsMethod")
                                .contentType(MediaType.APPLICATION_JSON).content(metadataAsString)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        log.info("TEST - AnalyticsGoal attach method failure: " + result.getResponse().getContentAsString());

        // Approving a valid AnalyticsGoal
        result = mockMvc.perform(put("/AnalyticsModules/AnalyticsGoals/" + testingAnalyticsGoalId + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testingAnalyticsGoalId))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.author").exists())
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();
        log.info("TEST - AnalyticsGoals activate: " + result.getResponse().getContentAsString());

        // Test linking existing AnalyticsMethod to existing and approved AnalyticsGoal
        result = mockMvc.perform
                (
                        put("/AnalyticsModules/AnalyticsGoals/" + testingAnalyticsGoalId + "/addAnalyticsMethod")
                                .contentType(MediaType.APPLICATION_JSON).content(metadataAsString)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analyticsMethods.[0].id").value(testingMethodId))
                .andExpect(jsonPath("$.analyticsMethods.[0].name").exists())
                .andExpect(jsonPath("$.analyticsMethods.[0].creator").exists())
                .andReturn();
        log.info("TEST - AnalyticsGoal attach method success: " + result.getResponse().getContentAsString());

    }

    @Test
    public void updateAnalyticsGoalTest() throws Exception {
        //Create the AnalyticsGoal
        AnalyticsGoal testAnalyticsGoal = new AnalyticsGoal("Updated", "Updated", "Updated", true);
        //Put the AnalyticsGoal as a String
        String analyticsGoalAsJsonString = testAnalyticsGoal.toString();

        MvcResult result;
        // Test updating existing AnalyticsGoal
        result = mockMvc.perform
                (
                        put("/AnalyticsModules/AnalyticsGoals/" + testingAnalyticsGoalId)
                                .contentType(MediaType.APPLICATION_JSON).content(analyticsGoalAsJsonString)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testingMethodId))
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.description").value("Updated"))
                .andReturn();
        log.info("TEST - AnalyticsGoal update success: " + result.getResponse().getContentAsString());
        // Test updating invalid AnalyticsGoal
        result = mockMvc.perform
                (
                        put("/AnalyticsModules/AnalyticsGoals/worngId")
                                .contentType(MediaType.APPLICATION_JSON).content(analyticsGoalAsJsonString)
                )
                .andExpect(status().isNotFound())
                .andReturn();
        log.info("TEST - AnalyticsGoal update failure: " + result.getResponse().getContentAsString());
    }

    @Test
    public void deleteAnalyticsGoalTest() throws Exception {
        MvcResult result;

        // Test deleting existing AnalyticsGoal
        result = mockMvc.perform(delete("/AnalyticsModules/AnalyticsGoals/worngId"))
                .andExpect(status().isNotFound())
                .andReturn();
        log.info("TEST - Delete wrong Goal response content: " + result.getResponse().getContentAsString());

        // Test deleting invalid AnalyticsGoal
        result = mockMvc.perform(delete("/AnalyticsModules/AnalyticsGoals/" + testingTriadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..status").value(200))
                .andExpect(jsonPath("$..message").value("Analytics Goal with id {" + testingTriadId + "} deleted"))
                .andReturn();
        log.info("TEST - Delete Goal content: " + result.getResponse().getContentAsString());
    }

    //endregion

    //region Utility Methods

    private void deleteFolder(String dir) {
        File directoryToDelete = new File(dir);
        try {
            log.info("TEST SETUP - Deleting folder :" + directoryToDelete);
            FileUtils.deleteDirectory(directoryToDelete);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Failed to delete :" + directoryToDelete);
        }
    }

    private void saveTestingMethod() throws Exception {
        // Test with jar that implements the framework correctly
        MockMultipartFile fstmp = prepareMultiPartFile(RESOURCE_JAR_TEST_METHOD);
        String jsonTxt1 = prepareJsonString(RESOURCE_JSON_MANIFEST_TEST_METHOD);
        MvcResult result = mockMvc.perform
                (
                        MockMvcRequestBuilders.fileUpload("/AnalyticsMethods")
                                .file(fstmp)
                                .param("methodMetadata", jsonTxt1)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        log.info("TEST SETUP - AnalyticsMethod uploaded: " + result.getResponse().getContentAsString());
        testingMethodId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        log.info("TEST SETUP - AnalyticsMethod id: " + testingMethodId);
    }

    private void saveTestingAnalyticsGoal() throws Exception {
        //Create the AnalyticsGoal
        AnalyticsGoal testAnalyticsGoal = new AnalyticsGoal("Test AnalyticsGoal", "A Analytics Goal", "lechip", true);
        //Put the AnalyticsGoal as a String
        String analyticsGoalAsJsonString = testAnalyticsGoal.toString();
        MvcResult result = mockMvc.perform
                (
                        post("/AnalyticsModules/AnalyticsGoals/")
                                .contentType(MediaType.APPLICATION_JSON).content(analyticsGoalAsJsonString)
                )
                .andExpect(status().isOk())
                .andReturn();
        log.info("TEST SETUP - AnalyticsGoal uploaded (is not approved): " + result.getResponse().getContentAsString());
        testingAnalyticsGoalId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        log.info("TEST SETUP - AnalyticsGoal id: " + testingAnalyticsGoalId);
    }

    private void saveTestingTriad() throws Exception {
        /*ObjectMapper mapper = new ObjectMapper();
        //Get the AnalyticsMethod
        AnalyticsMethodMetadata analyticsMethodForTesting = analyticsMethodsRepository.findOne(testingMethodId);
        //Create a Triad
        OpenLAPPortConfig config1 =
                mapper.readValue(getJsonString(JsonGeneratorIndex.OLAPCONFIGURATION_INDICATOR_TO_METHOD),
                        OpenLAPPortConfig.class);
        OpenLAPPortConfig config2 =
                mapper.readValue(getJsonString(JsonGeneratorIndex.OLAPCONFIGURATION_METHOD_TO_INDICATOR),
                        OpenLAPPortConfig.class);
        Triad triadForTesting = new Triad
                (
                        new IndicatorReference(1, "An indicator"),
                        analyticsMethodForTesting,
                        new VisualizerReference(1, 1, "Google Chats", "Bar Chart"),
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
        log.info("TEST SETUP - Triad: " + result.getResponse().getContentAsString());
        testingTriadId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        log.info("TEST SETUP - Triad id: " + testingTriadId);*/
    }

    /**
     * Helper method to prepare a String Test with the JSON of a resource file
     *
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
     *
     * @param resourceJarUpload the complete filepath of the resource jar file
     * @return a MockMultipartFile Test with the content of the resource jar file
     * @throws Exception
     */
    private MockMultipartFile prepareMultiPartFile(String resourceJarUpload) throws Exception {
        // Load the directory as a resource
        URL dirURL = getClass().getClassLoader().getResource(resourceJarUpload);
        log.info("TEST SETUP - Resource dir URL: " + dirURL.toString());
        // Make file from the uri
        File file = new File(dirURL.toURI());
        // Check file content
        log.info("TEST SETUP - File check: isFile:" + file.isFile()
                + ",  fileName: " + file.getName()
                + ",  exists: " + file.exists());
        // Make an input stream for the mock
        FileInputStream fi1 = new FileInputStream(file);
        // Create a mock
        return new MockMultipartFile("jarBundle", file.getName(), "multipart/form-data", fi1);
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
    //endregion
}

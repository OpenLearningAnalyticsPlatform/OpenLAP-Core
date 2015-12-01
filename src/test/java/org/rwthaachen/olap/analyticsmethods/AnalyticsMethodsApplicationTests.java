package org.rwthaachen.olap.analyticsmethods;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.rwthaachen.olap.OpenLAPCoreApplication;
import org.rwthaachen.olap.analyticsmethods.controller.AnalyticsMethodsUploadController;
import org.rwthaachen.olap.analyticsmethods.dataAccess.AnalyticsMethodsRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = OpenLAPCoreApplication.class)
// Necessary for getting the autowired WebApplicationContext
@WebAppConfiguration
@ActiveProfiles("development")
public class AnalyticsMethodsApplicationTests {

	// FILES
	private static final String RESOURCE_JAR_UPLOAD =
			"frameworkImplementationJar/AnalyticsMethodImplementation.jar";
	private static final String RESOURCE_JAR_UPLOAD_FOR_UPDATING =
			"frameworkImplementationJar/AnalyticsMethodImplementationForUpdating.jar";
	private static final String RESOURCE_JAR_UPLOAD_EMPTY_FILE =
			"frameworkImplementationJar/emptyFile.txt";
	private static final String RESOURCE_JAR_TEST_METHOD =
			"frameworkImplementationJar/AnalyticsMethodForTesting.jar";
	private static final String RESOURCE_JAR_UPLOAD_VALID_PMML =
			"frameworkImplementationJar/AnalyticsMethodImplementationWithValidPMML.jar";
	private static final String RESOURCE_JAR_UPLOAD_INVALID_PMML =
			"frameworkImplementationJar/AnalyticsMethodImplementationWithInvalidPMML.jar";

	//MANIFESTS
	private static final String RESOURCE_JSON_UPLOAD_MANIFEST =
			"jsonManifest/UploadMethodManifest_correct.json";
	private static final String RESOURCE_JSON_UPLOAD_MANIFEST_WRONG_FILENAME =
			"jsonManifest/UploadMethodManifest_wrong_filename.json";
	private static final String RESOURCE_JSON_FOR_UPDATING_MANIFEST =
			"jsonManifest/UploadMethodManifestForUpdating.json";
	private static final String RESOURCE_JSON_UPLOAD_MANIFEST_INCORRECT_CLASS =
			"jsonManifest/UploadMethodManifest_incorrectClass.json";
	private static final String RESOURCE_JSON_UPLOAD_MANIFEST_EMPTYFILE =
			"jsonManifest/UploadMethodManifest_emptyFile.json";
	private static final String RESOURCE_JSON_UPDATE_MANIFEST_INCORRECT_CLASS =
			"jsonManifest/UpdateMethodManifest_incorrectClass.json";
	private static final String RESOURCE_JSON_MANIFEST_TEST_METHOD =
			"jsonManifest/UploadMethodManifestForTesting.json";
	private static final String RESOURCE_JSON_MANIFEST_VALID_PMML =
			"jsonManifest/UpdateMethodManifest_WithCorrectPMML.json";
	private static final String RESOURCE_JSON_MANIFEST_INVALID_PMML =
			"jsonManifest/UpdateMethodManifest_WithInorrectPMML.json";

	//CONFIGURATION JSON
	private static final String RESOURCE_JSON_CONFIGURATION_VALID =
			"olapPortconfiguration/valid_portConfiguration.json";
	private static final String RESOURCE_JSON_CONFIGURATION_WRONG =
			"olapPortconfiguration/wrong_portConfiguration.json";


	private	static final Logger log =
			LoggerFactory.getLogger(OpenLAPCoreApplication.class);

	// Won't work Test without the WebAppConfiguration
	@Autowired
	private WebApplicationContext wac;

	@Autowired
	private AnalyticsMethodsUploadController controller;

	@Autowired
	private AnalyticsMethodsRepository repository;

	private MockMvc mockMvc;

	@Value("${analyticsMethodsJarsFolder}")
	String analyticsMethodsJarsFolder;

	String testingMethodId;

	@Before
	public void setup() throws Exception{
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		//Create a Method to test
		saveTestingMethod();
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

	@After
	public void testsCleanup(){
		// Delete Jar folder
		deleteFolder(analyticsMethodsJarsFolder);
		repository.deleteAll();
	}

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

	@Test
	public void controllerViewAllAnalyticsMethodsTest() throws Exception {
		// Test well formed
		this.mockMvc.perform(get("/AnalyticsMethods"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").exists());

	}

	@Test
	public void controllerViewAnalyticsMethodByIdTest() throws Exception {

		// Test well formed Test with existing method
		this.mockMvc.perform(get("/AnalyticsMethods/"+testingMethodId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(testingMethodId))
				.andExpect(jsonPath("$.name").value("Analytics Method For Testing"))
				.andExpect(jsonPath("$.creator").value("lechip"))
				.andExpect(jsonPath("$.description").value("A Method For Testing"))
				.andExpect(jsonPath("$.implementingClass").value("main.AnalyticsMethodForTesting"));
				//.andExpect(jsonPath("$.binariesLocation").value(analyticsMethodsJarFolder));
		// Test with error for not found
		this.mockMvc.perform(get("/AnalyticsMethods/10"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.content.exception")
						.value("org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodNotFoundException"));
	}

	@Test
	public void controllerUploadAnalyticsMethodTest() throws Exception{

		// Test with jar that implements the framework correctly
		MockMultipartFile fstmp = prepareMultiPartFile(RESOURCE_JAR_UPLOAD);
		String jsonTxt1 = prepareJsonString(RESOURCE_JSON_UPLOAD_MANIFEST);
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

		log.info("TEST - upload response content: " + result.getResponse().getContentAsString());

		// Test with jar that implements the framework correctly, but sends wrong information about implementing class
		String jsonTxt2 = prepareJsonString(RESOURCE_JSON_UPLOAD_MANIFEST_INCORRECT_CLASS);
		result = mockMvc.perform
				(
						MockMvcRequestBuilders.fileUpload("/AnalyticsMethods")
								.file(fstmp)
								.param("methodMetadata",jsonTxt2)
								.contentType(MediaType.MULTIPART_FORM_DATA)
								.accept(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.content.exception")
						.value("org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodsBadRequestException"))
				.andReturn();
		log.info("TEST - upload response content: " + result.getResponse().getContentAsString());

		// Test with empty file
		MockMultipartFile fstmp2 = prepareMultiPartFile(RESOURCE_JAR_UPLOAD_EMPTY_FILE);
		String jsonTxt3 = prepareJsonString(RESOURCE_JSON_UPLOAD_MANIFEST_EMPTYFILE);
		result = mockMvc.perform
				(
						MockMvcRequestBuilders.fileUpload("/AnalyticsMethods")
								.file(fstmp2)
								.param("methodMetadata",jsonTxt3)
								.contentType(MediaType.MULTIPART_FORM_DATA)
								.accept(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.content.exception")
						.value("org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodsBadRequestException"))
				.andExpect(jsonPath("$.content.errorMessage").value("Empty jar bundle."))
				.andReturn();
		log.info("TEST - upload response content: " + result.getResponse().getContentAsString());

		// Test with wrong filename
		MockMultipartFile fstmp3 = prepareMultiPartFile(RESOURCE_JAR_UPLOAD);
		String jsonTxt4 = prepareJsonString(RESOURCE_JSON_UPLOAD_MANIFEST_WRONG_FILENAME);
		result = mockMvc.perform
				(
						MockMvcRequestBuilders.fileUpload("/AnalyticsMethods")
								.file(fstmp3)
								.param("methodMetadata",jsonTxt4)
								.contentType(MediaType.MULTIPART_FORM_DATA)
								.accept(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.content.exception")
						.value("org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodsBadRequestException"))
				.andReturn();
		log.info("TEST - upload response content: " + result.getResponse().getContentAsString());

		// Test with correct metadata (PMML and security key)
		MockMultipartFile fstmp4 = prepareMultiPartFile(RESOURCE_JAR_UPLOAD_VALID_PMML);
		String jsonTxt5 = prepareJsonString(RESOURCE_JSON_MANIFEST_VALID_PMML);
		result = mockMvc.perform
				(
						MockMvcRequestBuilders.fileUpload("/AnalyticsMethods")
								.file(fstmp4)
								.param("methodMetadata",jsonTxt5)
								.contentType(MediaType.MULTIPART_FORM_DATA)
								.accept(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn();
		log.info("TEST - upload response content: " + result.getResponse().getContentAsString());

		// Test with incorrect metadata (PMML and security key)
		MockMultipartFile fstmp5 = prepareMultiPartFile(RESOURCE_JAR_UPLOAD_INVALID_PMML);
		String jsonTxt6 = prepareJsonString(RESOURCE_JSON_MANIFEST_INVALID_PMML);
		result = mockMvc.perform
				(
						MockMvcRequestBuilders.fileUpload("/AnalyticsMethods")
								.file(fstmp5)
								.param("methodMetadata",jsonTxt6)
								.contentType(MediaType.MULTIPART_FORM_DATA)
								.accept(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isBadRequest())
				.andReturn();
		log.info("TEST - upload response content: " + result.getResponse().getContentAsString());
	}


	@Test
	public void controllerUpdateAnalyticsMethodTest() throws Exception{

		MvcResult result;
		MockMultipartFile fstmp = prepareMultiPartFile(RESOURCE_JAR_UPLOAD_FOR_UPDATING);
		String jsonTxt1 = prepareJsonString(RESOURCE_JSON_FOR_UPDATING_MANIFEST);
		// Test with jar that implements the framework correctly
		MockMultipartFile fstmp2 = prepareMultiPartFile(RESOURCE_JAR_UPLOAD_FOR_UPDATING);
		result = mockMvc.perform
				(
						MockMvcRequestBuilders.fileUpload("/AnalyticsMethods/" + testingMethodId)
								.file(fstmp)
								.param("methodMetadata",jsonTxt1)
								.contentType(MediaType.MULTIPART_FORM_DATA)
								.accept(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn();

		log.info("TEST - upload response content: " + result.getResponse().getContentAsString());

		// Test with jar that implements the framework correctly, but sends wrong information about implementing class
		String jsonTxt3 = prepareJsonString(RESOURCE_JSON_UPDATE_MANIFEST_INCORRECT_CLASS);
		result = mockMvc.perform
				(
						MockMvcRequestBuilders.fileUpload("/AnalyticsMethods/" + testingMethodId)
								.file(fstmp2)
								.param("methodMetadata",jsonTxt3)
								.contentType(MediaType.MULTIPART_FORM_DATA)
								.accept(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.content.exception")
						.value("org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodsBadRequestException"))
				.andReturn();
		log.info("TEST - upload response content: " + result.getResponse().getContentAsString());

		// Test with invalid (empty) file
		MockMultipartFile fstmp3 = prepareMultiPartFile(RESOURCE_JAR_UPLOAD_EMPTY_FILE);
		result = mockMvc.perform
				(
						MockMvcRequestBuilders.fileUpload("/AnalyticsMethods/" + testingMethodId)
								.file(fstmp3)
								.param("methodMetadata",jsonTxt3)
								.contentType(MediaType.MULTIPART_FORM_DATA)
								.accept(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.content.exception")
						.value("org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodsBadRequestException"))
				.andExpect(jsonPath("$.content.errorMessage").value("Empty jar bundle."))
				.andReturn();
		log.info("TEST - upload response content: " + result.getResponse().getContentAsString());
	}

	@Test
	public void controllerValidateConfigurationTest() throws Exception{
		MvcResult result;

		// Test with correct olapPortconfiguration
		String jsonTxt1 = prepareJsonString(RESOURCE_JSON_CONFIGURATION_VALID);
		result = mockMvc.perform(put("/AnalyticsMethods/"+testingMethodId+"/validateConfiguration")
						.contentType(MediaType.APPLICATION_JSON).content(jsonTxt1))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.validationMessage").value("Valid configuration"))
				.andExpect(jsonPath("$.valid").value(true))
				.andReturn();
		log.info("TEST - Configuration response content: " + result.getResponse().getContentAsString());

		// Test with incorrect olapPortconfiguration
		String jsonTxt2 = prepareJsonString(RESOURCE_JSON_CONFIGURATION_WRONG);
		result = mockMvc.perform(put("/AnalyticsMethods/"+testingMethodId+"/validateConfiguration")
				.contentType(MediaType.APPLICATION_JSON).content(jsonTxt2))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.valid").value(false))
				.andReturn();
		log.info("TEST - Configuration response content: " + result.getResponse().getContentAsString());
		// Test with wrong method id
		result = mockMvc.perform(put("/AnalyticsMethods/worngId/validateConfiguration")
				.contentType(MediaType.APPLICATION_JSON).content(jsonTxt2))
				.andExpect(status().isNotFound())
				.andReturn();
		log.info("TEST - Configuration response content: " + result.getResponse().getContentAsString());
	}

	@Test
	public void controllerGetInputTest() throws Exception {
		MvcResult result;

		// Test with normal request
		result = mockMvc.perform(get("/AnalyticsMethods/"+testingMethodId+"/getInputPorts"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value("inputColumn1"))
				.andExpect(jsonPath("$[0].type").value("STRING"))
				.andExpect(jsonPath("$[0].required").value(true))
				.andReturn();
		log.info("TEST - Configuration response content: " + result.getResponse().getContentAsString());

		// Test with wrong method id
		result = mockMvc.perform(get("/AnalyticsMethods/worngId/getInputPorts"))
				.andExpect(status().isNotFound())
				.andReturn();
		log.info("TEST - Configuration response content: " + result.getResponse().getContentAsString());
	}

	@Test
	public void controllerGetOutputTest() throws Exception {
		MvcResult result;

		// Test with normal request
		result = mockMvc.perform(get("/AnalyticsMethods/"+testingMethodId+"/getOutputPorts"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value("outputColumn1"))
				.andExpect(jsonPath("$[0].type").value("INTEGER"))
				.andReturn();
		log.info("TEST - Configuration response content: " + result.getResponse().getContentAsString());

		// Test with wrong method id
		result = mockMvc.perform(get("/AnalyticsMethods/worngId/getOutputPorts"))
				.andExpect(status().isNotFound())
				.andReturn();
		log.info("TEST - Configuration response content: " + result.getResponse().getContentAsString());
	}

	@Test
	public void controllerDeleteAnalyticsMethodTest() throws Exception{
		MvcResult result;

		// Test with wrong method id
		result = mockMvc.perform(delete("/AnalyticsMethods/worngId"))
				.andExpect(status().isNotFound())
				.andReturn();
		log.info("TEST - Configuration response content: " + result.getResponse().getContentAsString());

		// Test with normal request
		result = mockMvc.perform(delete("/AnalyticsMethods/"+testingMethodId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$..status").value(200))
				.andExpect(jsonPath("$..message").value("Analytics Method with id {" + testingMethodId + "} deleted"))
				.andReturn();
		log.info("TEST - Configuration response content: " + result.getResponse().getContentAsString());
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

}

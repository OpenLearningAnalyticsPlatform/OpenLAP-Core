package org.rwthaachen.olap.analyticsmethods;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AnalyticsMethodsApplication.class)
// Necessary for getting the autowired WebApplicationContext
@WebAppConfiguration
@ActiveProfiles("development")
public class AnalyticsMethodsApplicationTests {

	// FILES
	private static final String RESOURCE_JAR_UPLOAD =
			"FrameworkImplementationJars/AnalyticsMethodImplementation.jar";
	private static final String RESOURCE_JAR_UPLOAD_FOR_UPDATING =
			"FrameworkImplementationJars/AnalyticsMethodImplementationForUpdating.jar";
	private static final String RESOURCE_JAR_UPLOAD_EMPTY_FILE =
			"FrameworkImplementationJars/emptyFile.txt";

	//MANIFESTS
	private static final String RESOURCE_JSON_UPLOAD_MANIFEST =
			"fstmp2/UploadMethodManifest_correct.json";
	private static final String RESOURCE_JSON_FOR_UPDATING_MANIFEST =
			"AnalyticsMethodUploadJsonManifest/UploadMethodManifestForUpdating.json";
	private static final String RESOURCE_JSON_UPLOAD_MANIFEST_INCORRECT_CLASS =
			"AnalyticsMethodUploadJsonManifest/UploadMethodManifest_incorrectClass.json";
	private static final String RESOURCE_JSON_UPLOAD_MANIFEST_EMPTYFILE =
			"AnalyticsMethodUploadJsonManifest/UploadMethodManifest_emptyFile.json";
	private static final String RESOURCE_JSON_UPDATE_MANIFEST_INCORRECT_CLASS =
			"AnalyticsMethodUploadJsonManifest/UpdateMethodManifest_incorrectClass.json";

	private	static final Logger log =
			LoggerFactory.getLogger(AnalyticsMethodsApplication.class);

	// Won't work witouth the WebAppConfiguration
	@Autowired
	private WebApplicationContext wac;

	@Autowired
	private AnalyticsMethodsUploadController controller;
	@Autowired
	private AnalyticsMethodsRepository repository;

	private MockMvc mockMvc;

	@Value("${analyticsMethodsJarFolder}")
	private String analyticsMethodsJarFolder;


	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

	}

	@After
	public void testsCleanup(){
		// Delete Jar folder
		deleteFolder(analyticsMethodsJarFolder);
	}

	private void deleteFolder(String dir) {
		File directoryToDelete = new File(dir);
		try {
			log.info("Deleting folder :" + directoryToDelete);
			FileUtils.deleteDirectory(directoryToDelete);
		} catch (IOException e) {
			e.printStackTrace();
			log.error("Failed to delete :" + directoryToDelete);
		}
	}

	@Test
	public void contextLoads() {
	}

	@Test
	public void controllerViewAllAnalyticsMethodsTest() throws Exception {
		// Test well formed
		this.mockMvc.perform(get("/AnalyticsMethods"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value("1"));

	}

	@Test
	public void controllerViewAnalyticsMethodByIdTest() throws Exception {

		// test well formed with existing method
		this.mockMvc.perform(get("/AnalyticsMethods/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("1"))
				.andExpect(jsonPath("$.name").value("A Method"))
				.andExpect(jsonPath("$.creator").value("lechip"))
				.andExpect(jsonPath("$.description").value("First Method"))
				.andExpect(jsonPath("$.implementingClass").value("com.example.core.method1"))
				.andExpect(jsonPath("$.binariesLocation").value(analyticsMethodsJarFolder));
		// test with error for not found
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
				.andDo(print())
				.andReturn();

		log.info("upload response content: " + result.getResponse().getContentAsString());

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
				.andDo(print())
				.andReturn();
		log.info("upload response content: " + result.getResponse().getContentAsString());

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
				.andDo(print())
				.andReturn();
		log.info("upload response content: " + result.getResponse().getContentAsString());
		// TODO test with incorrect metadata (PMML and security key)
	}


	@Test
	public void controllerUpdateAnalyticsMethodTest() throws Exception{

		String methodId;
		// Preparation
		MockMultipartFile fstmp = prepareMultiPartFile(RESOURCE_JAR_UPLOAD_FOR_UPDATING);
		String jsonTxt1 = prepareJsonString(RESOURCE_JSON_FOR_UPDATING_MANIFEST);
		MvcResult result = mockMvc.perform
				(
						MockMvcRequestBuilders.fileUpload("/AnalyticsMethods")
								.file(fstmp)
								.param("methodMetadata",jsonTxt1)
								.contentType(MediaType.MULTIPART_FORM_DATA)
								.accept(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andDo(print())
				.andReturn();


		log.info("upload response content: " + result.getResponse().getContentAsString());
		methodId = JsonPath.read(result.getResponse().getContentAsString(),"$.id");
		log.info("ID of uploaded method: " + methodId);

		// Test with jar that implements the framework correctly
		MockMultipartFile fstmp2 = prepareMultiPartFile(RESOURCE_JAR_UPLOAD_FOR_UPDATING);
		String jsonTxt2 = prepareJsonString(RESOURCE_JSON_FOR_UPDATING_MANIFEST);
		result = mockMvc.perform
				(
						MockMvcRequestBuilders.fileUpload("/AnalyticsMethods/" + methodId)
								.file(fstmp)
								.param("methodMetadata",jsonTxt1)
								.contentType(MediaType.MULTIPART_FORM_DATA)
								.accept(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andDo(print())
				.andReturn();

		log.info("upload response content: " + result.getResponse().getContentAsString());

		// Test with jar that implements the framework correctly, but sends wrong information about implementing class
		String jsonTxt3 = prepareJsonString(RESOURCE_JSON_UPDATE_MANIFEST_INCORRECT_CLASS);
		result = mockMvc.perform
				(
						MockMvcRequestBuilders.fileUpload("/AnalyticsMethods/" + methodId)
								.file(fstmp2)
								.param("methodMetadata",jsonTxt3)
								.contentType(MediaType.MULTIPART_FORM_DATA)
								.accept(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.content.exception")
						.value("org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodsBadRequestException"))
				.andDo(print())
				.andReturn();
		log.info("upload response content: " + result.getResponse().getContentAsString());

		// Test with invalid (empty) file
		MockMultipartFile fstmp3 = prepareMultiPartFile(RESOURCE_JAR_UPLOAD_EMPTY_FILE);
		result = mockMvc.perform
				(
						MockMvcRequestBuilders.fileUpload("/AnalyticsMethods/" + methodId)
								.file(fstmp3)
								.param("methodMetadata",jsonTxt3)
								.contentType(MediaType.MULTIPART_FORM_DATA)
								.accept(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.content.exception")
						.value("org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodsBadRequestException"))
				.andExpect(jsonPath("$.content.errorMessage").value("Empty jar bundle."))
				.andDo(print())
				.andReturn();
		log.info("upload response content: " + result.getResponse().getContentAsString());

		// TODO test with incorrect metadata (PMML and security key)
	}

	@Test
	public void controllerValidateConfigurationTest(){
		// TODO test with correct configuration
		// TODO test with incorrect configuration
		// TODO test with wrong method id
	}

	@Test
	public void controllerGetInputTest(){
		// TODO test with normal request
		// TODO test with wrong method id

	}

	@Test
	public void controllerGetOutputTest(){
		// TODO test with normal request
		// TODO test with wrong method id
	}

	/**
	 * Helper method to prepare a String with the JSON of a resource file
	 * @param resourceJsonUploadManifest the complete filepath of the resource json
	 * @return a String with the Json read from the resource json file
	 * @throws Exception
	 */
	private String prepareJsonString(String resourceJsonUploadManifest) throws Exception {
		InputStream is = getClass().getClassLoader().getResourceAsStream(resourceJsonUploadManifest);
		return IOUtils.toString(is);
	}

	/**
	 * Helper method to prepare a MockMultipartFile with the content of a file in the resource folder
	 * @param resourceJarUpload the complete filepath of the resource jar file
	 * @return a MockMultipartFile with the content of the resource jar file
	 * @throws Exception
	 */
	private MockMultipartFile prepareMultiPartFile(String resourceJarUpload) throws Exception {
		// Load the directory as a resource
		URL dirURL = getClass().getClassLoader().getResource(resourceJarUpload);
		log.info("Resource dir URL: " + dirURL.toString());
		// Make file from the uri
		File file = new File(dirURL.toURI());
		// Check file content
		log.info("File check: isFile:" + file.isFile()
				+ ",  fileName: " +file.getName()
				+ ",  exists: " + file.exists());
		// Make an input stream for the mock
		FileInputStream fi1 = new FileInputStream(file);
		// Create a mock
		return new MockMultipartFile("jarBundle", file.getName(), "multipart/form-data",fi1);
	}

}

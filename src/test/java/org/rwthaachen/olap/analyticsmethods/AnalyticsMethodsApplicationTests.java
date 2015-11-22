package org.rwthaachen.olap.analyticsmethods;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rwthaachen.olap.analyticsmethods.controller.AnalyticsMethodsUploadController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AnalyticsMethodsApplication.class)
// Necessary for getting the autowired WebApplicationContext
@WebAppConfiguration
public class AnalyticsMethodsApplicationTests {

	// Won't work witouth the WebAppConfiguration
	@Autowired
	private WebApplicationContext wac;

	@Autowired
	private AnalyticsMethodsUploadController controller;
	private MockMvc mockMvc;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
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
	public void controllerViewAnalyticsMethodByIdTest(){
		// TODO test well formed with existing method
		// TODO test with error for not found
	}

	@Test
	public void controllerUploadAnalyticsMethodTest(){
		// TODO test with jar that implements the framework correctly
		// TODO test with jar that implements the framework correctly, but sends wrong information about implementing class
		// TODO test with jar that does not implement the framework
		// TODO test with invalid file
		// TODO test with empty file
	}

	@Test
	public void controllerUpdateAnalyticsMethodTest(){
		// TODO test with jar that implements the framework correctly
		// TODO test with jar that implements the framework correctly, but sends wrong information about implementing class
		// TODO test with jar that does not implement the framework
		// TODO test with invalid file
		// TODO test with empty file
		// TODO test with incorrect metadata
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

}

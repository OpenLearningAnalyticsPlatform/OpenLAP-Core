package org.rwthaachen.olap.analyticsmodules;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rwthaachen.olap.OpenLAPCoreApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

/**
 * Created by lechip on 28/11/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = OpenLAPCoreApplication.class)
// Necessary for getting the autowired WebApplicationContext
@WebAppConfiguration
@ActiveProfiles("development")
public class AnalyticsModulesApplicationTests {

    private	static final Logger log =
            LoggerFactory.getLogger(OpenLAPCoreApplication.class);

    // Won't work Test without the WebAppConfiguration
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setUp(){
        //TODO create a new valid triad to be created
    }
    @Test
    public void saveTriadTest(){
        //TODO Test with valid Triad
        //TODO Test with invalid Triad
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
    public void getLearningGoalByIdTest(){
        //TODO Test getting valid LearningGoal
        //TODO Test getting invalid LearningGoal
        //TODO Test getting unapproved LearningGoal
    }

    @Test
    public void requestCreationOfLearningGoalTest(){
        //TODO Test creating valid LearningGoal
        //TODO Test creating invalid LearningGoal
    }

    @Test
    public void getAllApprovedLearningGoalsTest(){
        //TODO Test getting all approved LearningGoals
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
}

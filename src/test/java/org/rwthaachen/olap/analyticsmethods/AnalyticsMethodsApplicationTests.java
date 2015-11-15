package org.rwthaachen.olap.analyticsmethods;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AnalyticsMethodsApplication.class)
public class AnalyticsMethodsApplicationTests {

	@Test
	public void contextLoads() {
	}

    @Test
	public void viewAllAnalyticsMethodsTest(){
		// TODO test well formed
	}

    @Test
    public void viewAnalyticsMethodByIdTest(){
        // TODO test well formed with existing method
        // TODO test with error for not found
    }

	@Test
	public void uploadAnalyticsMethod(){
		// TODO test with jar that implements the framework right
		// TODO test with jar that implements the framework right, but sends wrong information about implementing class
		// TODO test with jar that does not implement the framework
		// TODO test with invalid file
		// TODO test with empty file
	}

}

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

}

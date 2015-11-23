package org.rwthaachen.olap.analyticsmethods;

import org.rwthaachen.olap.analyticsmethods.service.DataBaseLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.ActiveProfiles;

@SpringBootApplication
public class AnalyticsMethodsApplication {

    @Autowired(required = false)
    DataBaseLoader databaseLoader;

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsMethodsApplication.class, args);
    }
}

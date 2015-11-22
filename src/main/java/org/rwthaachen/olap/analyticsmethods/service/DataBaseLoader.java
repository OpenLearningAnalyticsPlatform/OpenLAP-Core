package org.rwthaachen.olap.analyticsmethods.service;

import org.rwthaachen.olap.analyticsmethods.AnalyticsMethodsApplication;
import org.rwthaachen.olap.analyticsmethods.dataAccess.AnalyticsMethodsRepository;
import org.rwthaachen.olap.analyticsmethods.model.AnalyticsMethodMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * A service class bean to load data programatically when using H2 for testing
 */
@Service
@Profile("development")
public class DataBaseLoader {

    @Value("${analyticsMethodsJarFolder}")
    String analyticsMethodsJarsFolder;

    private final AnalyticsMethodsRepository analyticsMethodsRepository;
    private	static	final Logger log	=
            LoggerFactory.getLogger(AnalyticsMethodsApplication.class);

    @Autowired(required	= false)
    public DataBaseLoader(AnalyticsMethodsRepository analyticsMethodsRepository) {
        this.analyticsMethodsRepository = analyticsMethodsRepository;
    }

    @PostConstruct
    private void initDatabase()
    {
        log.info("Started DataBaseLoader initDatabase");
        AnalyticsMethodMetadata metadata1 = new AnalyticsMethodMetadata(
                "A Method", "lechip","First Method","com.example.core.method1", analyticsMethodsJarsFolder);
        analyticsMethodsRepository.save(metadata1);
        log.info("Logged metadata: " + metadata1.toString());
    }
}

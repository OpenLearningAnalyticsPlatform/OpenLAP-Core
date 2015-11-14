package org.rwthaachen.olap.analyticsmethods.service;

import org.rwthaachen.olap.analyticsmethods.AnalyticsMethodsApplication;
import org.rwthaachen.olap.analyticsmethods.dataAccess.AnalyticsMethodsRepository;
import org.rwthaachen.olap.analyticsmethods.model.AnalyticsMethodMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * A service class bean to load data programatically when using H2 for testing
 */
@Service
public class DataBaseLoader {
    private final AnalyticsMethodsRepository analyticsMethodsRepository;
    private	static	final Logger log	=
            LoggerFactory.getLogger(AnalyticsMethodsApplication.class);

    @Autowired
    public DataBaseLoader(AnalyticsMethodsRepository analyticsMethodsRepository) {
        this.analyticsMethodsRepository = analyticsMethodsRepository;
    }

    @PostConstruct
    private void initDatabase()
    {
        log.info("Started DataBaseLoader initDatabase");
        AnalyticsMethodMetadata metadata1 = new AnalyticsMethodMetadata("Method1", "lechip","First Method", null);
        analyticsMethodsRepository.save(metadata1);
        log.info("Logged metadata: " + metadata1.toString());
    }
}

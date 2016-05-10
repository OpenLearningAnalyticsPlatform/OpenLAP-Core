package de.rwthaachen.openlap;

import de.rwthaachen.openlap.common.DataBaseLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Application of the OpenLAP-Core
 */
@SpringBootApplication
public class OpenLAPCoreApplication {

    @Autowired(required = false)
    DataBaseLoader databaseLoader;

    /**
     * Start the application
     *
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(OpenLAPCoreApplication.class, args);
    }
}

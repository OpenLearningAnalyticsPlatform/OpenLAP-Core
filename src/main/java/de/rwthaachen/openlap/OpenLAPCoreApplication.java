package de.rwthaachen.openlap;

import de.rwthaachen.openlap.common.DataBaseLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OpenLAPCoreApplication {

    @Autowired(required = false)
    DataBaseLoader databaseLoader;

    public static void main(String[] args) {
        SpringApplication.run(OpenLAPCoreApplication.class, args);
    }
}

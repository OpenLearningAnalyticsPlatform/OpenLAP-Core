package org.rwthaachen.olap.analyticsmethods.model;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.net.URL;

/**
 * Created by lechip on 05/11/15.
 */

@Entity
public class AnalyticsMethodMetadata {

    @Id
    @GeneratedValue
    String id;
    String name;
    String creator;
    String description;
    URL binariesLocation;

    public AnalyticsMethodMetadata() {
        this.id = "";
        this.name = "";
        this.creator = "";
        this.description = "";
        this.binariesLocation = null;
    }

    public AnalyticsMethodMetadata(String name, String creator, String description, URL binariesLocation) {
        this.name = name;
        this.creator = creator;
        this.description = description;
        this.binariesLocation = binariesLocation;
    }

    public AnalyticsMethodMetadata(String id, String name, String creator, String description, URL binariesLocation) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.description = description;
        this.binariesLocation = binariesLocation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public URL getBinariesLocation() {
        return binariesLocation;
    }

    public void setBinariesLocation(URL binariesLocation) {
        this.binariesLocation = binariesLocation;
    }

    @Override
    public String toString() {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return super.toString();
    }
}

package org.rwthaachen.olap.analyticsmethods.model;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import javax.persistence.*;
import java.net.URL;

/**
 * Created by lechip on 05/11/15.
 */

@Entity
@Table(name="AnalyticsMethodMetadata")
public class AnalyticsMethodMetadata implements Cloneable{

    @Id
    @GeneratedValue
    @Column(name = "METHODMETADATA_ID")
    String id;
    @Column(unique=true, nullable=false)
    String name;
    @Column(nullable = false)
    String creator;
    @Column(nullable = false)
    String description;
    @Column(unique=true, nullable=false)
    String implementingClass;
    @Column(nullable = false)
    String binariesLocation;
    @Column(unique=true, nullable=false)
    String filename;

    public AnalyticsMethodMetadata() {
        this.name = null;
        this.creator = "";
        this.description = "";
        this.binariesLocation = null;
        this.implementingClass = "";
        this.filename = "";
    }

    public AnalyticsMethodMetadata(String name, String creator, String description,
                                   String implementingClass, String binariesLocation) {
        this.name = name;
        this.creator = creator;
        this.description = description;
        this.implementingClass = implementingClass;
        this.binariesLocation = binariesLocation;
    }

    public AnalyticsMethodMetadata(String name, String creator, String description, String binariesLocation) {
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

    public String getBinariesLocation() {
        return binariesLocation;
    }

    public void setBinariesLocation(String binariesLocation) {
        this.binariesLocation = binariesLocation;
    }

    public String getImplementingClass() {
        return implementingClass;
    }

    public void setImplementingClass(String implementingClass) {
        this.implementingClass = implementingClass;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
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

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public void updateWithMetadata(AnalyticsMethodMetadata updatedMetadata) {
        this.setCreator(updatedMetadata.getCreator());
        this.setDescription(updatedMetadata.getDescription());
        this.setImplementingClass(updatedMetadata.getImplementingClass());
        this.setFilename(updatedMetadata.getFilename());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnalyticsMethodMetadata)) return false;

        AnalyticsMethodMetadata that = (AnalyticsMethodMetadata) o;

        if (!getId().equals(that.getId())) return false;
        if (!getName().equals(that.getName())) return false;
        if (!getCreator().equals(that.getCreator())) return false;
        if (!getDescription().equals(that.getDescription())) return false;
        if (!getImplementingClass().equals(that.getImplementingClass())) return false;
        if (!getBinariesLocation().equals(that.getBinariesLocation())) return false;
        return getFilename().equals(that.getFilename());

    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + getCreator().hashCode();
        result = 31 * result + getDescription().hashCode();
        result = 31 * result + getImplementingClass().hashCode();
        result = 31 * result + getBinariesLocation().hashCode();
        result = 31 * result + getFilename().hashCode();
        return result;
    }
}

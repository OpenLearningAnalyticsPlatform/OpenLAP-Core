package de.rwthaachen.openlap.analyticsmethods.model;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import javax.persistence.*;

/**
 * This class represents the main metadata of the Analytics Methods to be uploaded and used in the macro component.
 * In particular, it holds the location of the JAR files relative to the deployment server's file system.
 * It also stores information about the creator, description, class in the JAR that implements the
 * OpenLAP-AnalyticsMethodsFramework and an ID that allows to locate the Analytics Method logically.
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

    /**
     * Empty constructor.
     */
    public AnalyticsMethodMetadata() {
        this.name = null;
        this.creator = "";
        this.description = "";
        this.binariesLocation = null;
        this.implementingClass = "";
        this.filename = "";
    }

    /**
     * Standard constructor
     * @param name Name of the Analytics Method
     * @param creator Creator of te Analytics Method
     * @param description Short description of the Analytisc Method
     * @param implementingClass Class that implements the OpenLAP-AnalyticsFramework
     * @param binariesLocation Path of the server where the JAR files of the Analytics Method are located.
     */
    public AnalyticsMethodMetadata(String name, String creator, String description,
                                   String implementingClass, String binariesLocation) {
        this.name = name;
        this.creator = creator;
        this.description = description;
        this.implementingClass = implementingClass;
        this.binariesLocation = binariesLocation;
    }

    /**
     * Standard constructor
     * @param name Name of the Analytics Method
     * @param creator Creator of te Analytics Method
     * @param description Short description of the Analytisc Method
     * @param binariesLocation Path of the server where the JAR files of the Analytics Method are located.
     */
    public AnalyticsMethodMetadata(String name, String creator, String description, String binariesLocation) {
        this.name = name;
        this.creator = creator;
        this.description = description;
        this.binariesLocation = binariesLocation;
    }

    /**
     * @return ID of the Analytics Method Metadata
     */
    public String getId() {
        return id;
    }

    /**
     * @param id ID to be set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return Name of the Analytics Method Metadata
     */
    public String getName() {
        return name;
    }

    /**
     * @param name Name to be set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Creator of the Analytics Method Metadata
     */
    public String getCreator() {
        return creator;
    }

    /**
     * @param creator Creator to be set
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * @return Description of the Analytics Method Metadata
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description Description to be set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Location where the JAR files of the Analytics Method are located in the server.
     */
    public String getBinariesLocation() {
        return binariesLocation;
    }

    /**
     * @param binariesLocation to be set
     */
    public void setBinariesLocation(String binariesLocation) {
        this.binariesLocation = binariesLocation;
    }

    /**
     * @return Class in the JAR files that implements the OpenLAP-AnalyticsMethodsFramework
     */
    public String getImplementingClass() {
        return implementingClass;
    }

    /**
     * @param implementingClass Name of the Class implementing the OpenLAP-AnalyticsMethodsFramework to be set
     */
    public void setImplementingClass(String implementingClass) {
        this.implementingClass = implementingClass;
    }

    /**
     * @return Name of the JAR file ontaining the binaries of the AnalyticsMethod
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param filename Name of the File to be set
     */
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

    /**
     * Cloning Method for the AnalyticsMethod Metadata.
     * @return An Object with the properties of this AnalyticsMethodMetadata
     * @throws CloneNotSupportedException
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Use another AnalyticsMethodMetadata to update this object
     * @param updatedMetadata
     */
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

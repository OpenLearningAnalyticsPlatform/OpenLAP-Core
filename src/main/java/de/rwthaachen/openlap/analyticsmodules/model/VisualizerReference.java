package de.rwthaachen.openlap.analyticsmodules.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * This class represents a reference to a particular Visualizer technique of a Visualizing Library of the Visualizer
 * macro component of the OpenLAP. It is modeled after the corresponding Class on the Visualization
 * and holds metadata necessary to reference on a Triad
 */
public class VisualizerReference {
    long frameworkId;
    long methodId;
    Map<String, String> additionalParams;

    /**
     * Empty constructor
     */
    public VisualizerReference() {
    }

    /**
     * Standard Constructor
     *
     * @param frameworkId ID of the Visualizer Framework
     * @param methodId    ID of the Visualizer Method
     * @param additionalParams additional paramaters for the visualizer
     */
    public VisualizerReference(long frameworkId, long methodId, Map<String, String> additionalParams) {
        this.frameworkId = frameworkId;
        this.methodId = methodId;
        this.additionalParams = additionalParams;
    }

    /**
     * Standard Constructor
     *
     * @param frameworkName Name of the Visualizer Framework
     * @param methodName    Name of the Visualizer Method
     *//*
    public VisualizerReference(String frameworkName, String methodName) {
        this.frameworkName = frameworkName;
        this.methodName = methodName;
    }

    *//**
     * Standard Constructor
     *
     * @param frameworkId   ID of the Visualizer Framework
     * @param methodId      ID of the Visualizer Method
     * @param frameworkName Name of the Visualizer Framework
     * @param methodName    Name of the Visualizer Method
     *//*
    public VisualizerReference(long frameworkId, long methodId, String frameworkName, String methodName) {
        this.frameworkId = frameworkId;
        this.methodId = methodId;
        this.frameworkName = frameworkName;
        this.methodName = methodName;
    }*/

    /**
     * @return ID of the Visualization Framework
     */
    public long getFrameworkId() {
        return frameworkId;
    }

    /**
     * @param frameworkId ID of the Visualization Framework to be set.
     */
    public void setFrameworkId(long frameworkId) {
        this.frameworkId = frameworkId;
    }

    /**
     * @return ID of the Visualization Method
     */
    public long getMethodId() {
        return methodId;
    }

    /**
     * @param methodId ID of the Visualization Method to be set.
     */
    public void setMethodId(long methodId) {
        this.methodId = methodId;
    }

    public Map<String, String> getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(Map<String, String> additionalParams) {
        this.additionalParams = additionalParams;
    }

    //    /**
//     * @return Name of the Visualization Framework
//     */
//    public String getFrameworkName() {
//        return frameworkName;
//    }
//
//    /**
//     * @param frameworkName Name of the Visualization Framework to be set.
//     */
//    public void setFrameworkName(String frameworkName) {
//        this.frameworkName = frameworkName;
//    }
//
//    /**
//     * @return Name of the Visualization Method
//     */
//    public String getMethodName() {
//        return methodName;
//    }
//
//    /**
//     * @param methodName Name of the Visualization Method to be set.
//     */
//    public void setMethodName(String methodName) {
//        this.methodName = methodName;
//    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "VisualizerReference{" +
                    "frameworkId=" + frameworkId +
                    ", methodId=" + methodId +
                    ", additionalParams=" + additionalParams +
                    '}';
        }
    }
}

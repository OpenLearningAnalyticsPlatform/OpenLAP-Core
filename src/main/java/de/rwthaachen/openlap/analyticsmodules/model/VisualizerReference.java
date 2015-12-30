package de.rwthaachen.openlap.analyticsmodules.model;

/**
 * This class represents a reference to a particular Visualizer technique of a Visualizing Library of the Visualizer
 * macro component of the OpenLAP. It is modeled after the corresponding Class on the Visualization
 * and holds metadata necessary to reference on a Triad
 */
public class VisualizerReference {
    long id;
    String shortName;
    String visualizationName;

    /**
     * Empty constructor
     */
    public VisualizerReference() {
    }

    /**
     * Standard Constructor
     * @param id ID of the Visualizer Library
     * @param shortName Short Name of the Library used
     * @param visualizationName Visualizer Name
     */
    public VisualizerReference(long id, String shortName, String visualizationName) {
        this.id = id;
        this.shortName = shortName;
        this.visualizationName = visualizationName;
    }

    /**
     * @return ID of the Visualization Library
     */
    public long getId() {
        return id;
    }

    /**
     * @param id ID of the Visualization library to be set.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return Short name of the Visualization Library
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @param shortName Short name of the Visualization Lirbary to be set
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * @return Visualization Library name
     */
    public String getVisualizationName() {
        return visualizationName;
    }

    /**
     * @param visualizationName Name of the Visualization Lirbary to be set
     */
    public void setVisualizationName(String visualizationName) {
        this.visualizationName = visualizationName;
    }
}

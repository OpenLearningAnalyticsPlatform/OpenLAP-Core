package org.rwthaachen.olap.analyticsmodules.model;

/**
 * Created by lechip on 29/11/15.
 */
public class VisualizerReference {
    long id;
    String shortName;
    String visualizationName;

    public VisualizerReference() {
    }

    public VisualizerReference(long id, String shortName, String visualizationName) {
        this.id = id;
        this.shortName = shortName;
        this.visualizationName = visualizationName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getVisualizationName() {
        return visualizationName;
    }

    public void setVisualizationName(String visualizationName) {
        this.visualizationName = visualizationName;
    }
}

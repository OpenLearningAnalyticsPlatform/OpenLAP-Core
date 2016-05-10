package de.rwthaachen.openlap.analyticsmodules.model;

import DataSet.OLAPPortConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rwthaachen.openlap.analyticsmethods.model.AnalyticsMethodMetadata;

import javax.persistence.*;

/**
 * A Triad is used to save the Indicator/Method/Visualization along with the particular configuration between the
 * Indicator to the Analytics Method as well as the one between the Analytics Method to the Visualization
 */
@Entity
@Table(name = "Triad")
public class Triad {

    @Id
    @GeneratedValue
    String id;

    @Column(nullable = false, columnDefinition = "LONGVARCHAR")
    @Convert(converter = AnalyticsMethodMetadataConverter.class)
    AnalyticsMethodMetadata analyticsMethodReference;

    @Column(nullable = false, columnDefinition = "LONGVARCHAR")
    @Convert(converter = IndicatorReferenceConverter.class)
    IndicatorReference indicatorReference;

    @Column(nullable = false, columnDefinition = "LONGVARCHAR")
    @Convert(converter = VisualizerReferenceConverter.class)
    VisualizerReference visualizationReference;

    @Column(columnDefinition = "LONGVARCHAR")
    @Convert(converter = OLAPPortConfigurationConverter.class)
    OLAPPortConfiguration indicatorToAnalyticsMethodMapping;

    @Column(columnDefinition = "LONGVARCHAR")
    @Convert(converter = OLAPPortConfigurationConverter.class)
    OLAPPortConfiguration analyticsMethodToVisualizationMapping;

    /**
     * Empty constructor
     */
    public Triad() {
    }

    /**
     * Standard constructor
     *
     * @param id                                    ID of the Triad
     * @param indicatorReference                    An Indicator Reference that corresponds to an Indicator of the Indicator Engine
     *                                              macro component of the OpenLAP.
     * @param analyticsMethodReference              An Analytics Method Reference that corresponds to an Analytics Method metadata of
     *                                              the Analytics Methods macro component of the OpenLAP.
     * @param visualizationReference                A Visualization Reference that correponds to a Visualization technique of the
     *                                              Visualizer macro component of the OpenLAP.
     * @param indicatorToAnalyticsMethodMapping     The OLAPPortConfiguration between the Indicator and the Analytics Method
     *                                              of this Triad.
     * @param analyticsMethodToVisualizationMapping The OLAPPortConfiguration between the Analytics Method and the
     *                                              Visualization of this Triad.
     */
    public Triad(String id,
                 IndicatorReference indicatorReference,
                 AnalyticsMethodMetadata analyticsMethodReference,
                 VisualizerReference visualizationReference,
                 OLAPPortConfiguration indicatorToAnalyticsMethodMapping,
                 OLAPPortConfiguration analyticsMethodToVisualizationMapping) {
        //this.id = id;
        this.indicatorReference = indicatorReference;
        this.analyticsMethodReference = analyticsMethodReference;
        this.visualizationReference = visualizationReference;
        this.indicatorToAnalyticsMethodMapping = indicatorToAnalyticsMethodMapping;
        this.analyticsMethodToVisualizationMapping = analyticsMethodToVisualizationMapping;
    }

    /**
     * Standard constructor
     *
     * @param indicatorReference                    An Indicator Reference that corresponds to an Indicator of the Indicator Engine
     *                                              macro component of the OpenLAP.
     * @param analyticsMethodReference              An Analytics Method Reference that corresponds to an Analytics Method metadata of
     *                                              the Analytics Methods macro component of the OpenLAP.
     * @param visualizationReference                A Visualization Reference that correponds to a Visualization technique of the
     *                                              Visualizer macro component of the OpenLAP.
     * @param indicatorToAnalyticsMethodMapping     The OLAPPortConfiguration between the Indicator and the Analytics Method
     *                                              of this Triad.
     * @param analyticsMethodToVisualizationMapping The OLAPPortConfiguration between the Analytics Method and the
     *                                              Visualization of this Triad.
     */
    public Triad(IndicatorReference indicatorReference,
                 AnalyticsMethodMetadata analyticsMethodReference,
                 VisualizerReference visualizationReference,
                 OLAPPortConfiguration indicatorToAnalyticsMethodMapping,
                 OLAPPortConfiguration analyticsMethodToVisualizationMapping) {
        this.indicatorReference = indicatorReference;
        this.analyticsMethodReference = analyticsMethodReference;
        this.visualizationReference = visualizationReference;
        this.indicatorToAnalyticsMethodMapping = indicatorToAnalyticsMethodMapping;
        this.analyticsMethodToVisualizationMapping = analyticsMethodToVisualizationMapping;
    }

    /**
     * @return The VisualizerReference of this Triad.
     */
    public VisualizerReference getVisualizationReference() {
        return visualizationReference;
    }

    /**
     * @param visualizationReference The VisualizerReference to set in this Triad.
     */
    public void setVisualizationReference(VisualizerReference visualizationReference) {
        this.visualizationReference = visualizationReference;
    }

    /**
     * @return The OLAPPortConfiguration between the Indicator and the Analytics Method of this Triad.
     */
    public OLAPPortConfiguration getIndicatorToAnalyticsMethodMapping() {
        return indicatorToAnalyticsMethodMapping;
    }

    /**
     * @param indicatorToAnalyticsMethodMapping The OLAPPortConfiguration to set between the the Indicator and the
     *                                          Analytics Method of this Triad.
     */
    public void setIndicatorToAnalyticsMethodMapping(OLAPPortConfiguration indicatorToAnalyticsMethodMapping) {
        this.indicatorToAnalyticsMethodMapping = indicatorToAnalyticsMethodMapping;
    }

    /**
     * @return The OLAPPortConfiguration between the Analytics Method and the Visualization of this Triad.
     */
    public OLAPPortConfiguration getAnalyticsMethodToVisualizationMapping() {
        return analyticsMethodToVisualizationMapping;
    }

    /**
     * @param analyticsMethodToVisualizationMapping The OLAPPortConfiguration to set between the the Analytics Method
     *                                              and the Visualization of this Triad.
     */
    public void setAnalyticsMethodToVisualizationMapping(OLAPPortConfiguration analyticsMethodToVisualizationMapping) {
        this.analyticsMethodToVisualizationMapping = analyticsMethodToVisualizationMapping;
    }

    /**
     * @return ID of this Triad.
     */
    public String getId() {
        return id;
    }

    /**
     * @param id The ID to be set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The AnalyticsMethodMetadata reference to the Analytis Method of this Triad.
     */
    public AnalyticsMethodMetadata getAnalyticsMethodReference() {
        return analyticsMethodReference;
    }

    /**
     * @param analyticsMethodReference The AnalyticsMethodMetadata to be set as the Analytics Method referene of this
     *                                 Triad.
     */
    public void setAnalyticsMethodReference(AnalyticsMethodMetadata analyticsMethodReference) {
        this.analyticsMethodReference = analyticsMethodReference;
    }

    /**
     * @return The IndicatorReference of this Triad.
     */
    public IndicatorReference getIndicatorReference() {
        return indicatorReference;
    }

    /**
     * @param indicatorReference The IndicatorReference to be set in this Triad.
     */
    public void setIndicatorReference(IndicatorReference indicatorReference) {
        this.indicatorReference = indicatorReference;
    }

    /**
     * Updates this object with the information of the Triad passed as a parameter
     *
     * @param triad with the information to be used to update this object.
     */
    public void updateWithTriad(Triad triad) {
        this.setAnalyticsMethodReference(triad.getAnalyticsMethodReference());
        this.setIndicatorReference(triad.getIndicatorReference());
        this.setVisualizationReference(triad.getVisualizationReference());
        this.setAnalyticsMethodToVisualizationMapping(triad.getAnalyticsMethodToVisualizationMapping());
        this.setIndicatorToAnalyticsMethodMapping(triad.getIndicatorToAnalyticsMethodMapping());
    }

    /**
     * Attempts to return a JSON human-readable form representation of this object. If it fails, it defaults to standard
     * JAVA string representation.
     *
     * @return A string representation of this Object.
     */
    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Triad{" +
                    "id='" + id + '\'' +
                    ", analyticsMethodReference='" + analyticsMethodReference + '\'' +
                    ", indicatorReference='" + indicatorReference + '\'' +
                    ", visualizationReference='" + visualizationReference + '\'' +
                    ", indicatorToAnalyticsMethodMapping='" + indicatorToAnalyticsMethodMapping + '\'' +
                    ", analyticsMethodToVisualizationMapping='" + analyticsMethodToVisualizationMapping + '\'' +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Triad)) return false;

        Triad triad = (Triad) o;

        if (!id.equals(triad.id)) return false;
        if (!analyticsMethodReference.equals(triad.analyticsMethodReference)) return false;
        if (!indicatorReference.equals(triad.indicatorReference)) return false;
        if (!getVisualizationReference().equals(triad.getVisualizationReference())) return false;
        if (getIndicatorToAnalyticsMethodMapping() != null ?
                !getIndicatorToAnalyticsMethodMapping().equals(triad.getIndicatorToAnalyticsMethodMapping())
                : triad.getIndicatorToAnalyticsMethodMapping() != null)
            return false;
        return !(getAnalyticsMethodToVisualizationMapping() != null ?
                !getAnalyticsMethodToVisualizationMapping().equals(triad.getAnalyticsMethodToVisualizationMapping())
                : triad.getAnalyticsMethodToVisualizationMapping() != null);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + analyticsMethodReference.hashCode();
        result = 31 * result + indicatorReference.hashCode();
        result = 31 * result + getVisualizationReference().hashCode();
        result = 31 * result + (getIndicatorToAnalyticsMethodMapping() != null ?
                getIndicatorToAnalyticsMethodMapping().hashCode() : 0);
        result = 31 * result + (getAnalyticsMethodToVisualizationMapping() != null ?
                getAnalyticsMethodToVisualizationMapping().hashCode() : 0);
        return result;
    }
}

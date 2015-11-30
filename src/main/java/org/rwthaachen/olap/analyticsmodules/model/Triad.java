package org.rwthaachen.olap.analyticsmodules.model;

import OLAPDataSet.OLAPPortConfiguration;
import OLAPDataSet.OLAPPortMapping;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.rwthaachen.olap.analyticsmethods.model.AnalyticsMethodMetadata;

import javax.persistence.*;

/**
 * A Triad is used to save the Indicator/Method/Visualization along with the particular configuration between the
 * Indicator to the Analytics Method as well as the one between the Analytics Method to the Visualization
 */
@Entity
public class Triad {

    @Id
    @GeneratedValue
    String id;

    @Column(nullable = false,columnDefinition="LONGVARCHAR")
    @Convert(converter = AnalyticsMethodMetadataConverter.class)
    AnalyticsMethodMetadata analyticsMethodReference;

    @Column(nullable = false,columnDefinition="LONGVARCHAR")
    @Convert(converter = IndicatorReferenceConverter.class)
    IndicatorReference indicatorReference;

    @Column(nullable = false,columnDefinition="LONGVARCHAR")
    @Convert(converter = VisualizerReferenceConverter.class)
    VisualizerReference visualizationReference;

    @Column(columnDefinition="LONGVARCHAR")
    @Convert(converter = OLAPPortConfigurationConverter.class)
    OLAPPortConfiguration indicatorToAnalyticsMethodMapping;

    @Column(columnDefinition="LONGVARCHAR")
    @Convert(converter = OLAPPortConfigurationConverter.class)
    OLAPPortConfiguration analyticsMethodToVisualizationMapping;

    public Triad() {
    }

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

    public VisualizerReference getVisualizationReference() {
        return visualizationReference;
    }

    public void setVisualizationReference(VisualizerReference visualizationReference) {
        this.visualizationReference = visualizationReference;
    }

    public OLAPPortConfiguration getIndicatorToAnalyticsMethodMapping() {
        return indicatorToAnalyticsMethodMapping;
    }

    public void setIndicatorToAnalyticsMethodMapping(OLAPPortConfiguration indicatorToAnalyticsMethodMapping) {
        this.indicatorToAnalyticsMethodMapping = indicatorToAnalyticsMethodMapping;
    }

    public OLAPPortConfiguration getAnalyticsMethodToVisualizationMapping() {
        return analyticsMethodToVisualizationMapping;
    }

    public void setAnalyticsMethodToVisualizationMapping(OLAPPortConfiguration analyticsMethodToVisualizationMapping) {
        this.analyticsMethodToVisualizationMapping = analyticsMethodToVisualizationMapping;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AnalyticsMethodMetadata getAnalyticsMethodReference() {
        return analyticsMethodReference;
    }

    public void setAnalyticsMethodReference(AnalyticsMethodMetadata analyticsMethodReference) {
        this.analyticsMethodReference = analyticsMethodReference;
    }

    public IndicatorReference getIndicatorReference() {
        return indicatorReference;
    }

    public void setIndicatorReference(IndicatorReference indicatorReference) {
        this.indicatorReference = indicatorReference;
    }

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
        if (getIndicatorToAnalyticsMethodMapping() != null ? !getIndicatorToAnalyticsMethodMapping().equals(triad.getIndicatorToAnalyticsMethodMapping()) : triad.getIndicatorToAnalyticsMethodMapping() != null)
            return false;
        return !(getAnalyticsMethodToVisualizationMapping() != null ? !getAnalyticsMethodToVisualizationMapping().equals(triad.getAnalyticsMethodToVisualizationMapping()) : triad.getAnalyticsMethodToVisualizationMapping() != null);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + analyticsMethodReference.hashCode();
        result = 31 * result + indicatorReference.hashCode();
        result = 31 * result + getVisualizationReference().hashCode();
        result = 31 * result + (getIndicatorToAnalyticsMethodMapping() != null ? getIndicatorToAnalyticsMethodMapping().hashCode() : 0);
        result = 31 * result + (getAnalyticsMethodToVisualizationMapping() != null ? getAnalyticsMethodToVisualizationMapping().hashCode() : 0);
        return result;
    }

    public void updateWithTriad(Triad triad) {
        this.setAnalyticsMethodReference(triad.getAnalyticsMethodReference());
        this.setIndicatorReference(triad.getIndicatorReference());
        this.setVisualizationReference(triad.getVisualizationReference());
        this.setAnalyticsMethodToVisualizationMapping(triad.getAnalyticsMethodToVisualizationMapping());
        this.setIndicatorToAnalyticsMethodMapping(triad.getIndicatorToAnalyticsMethodMapping());
    }
}

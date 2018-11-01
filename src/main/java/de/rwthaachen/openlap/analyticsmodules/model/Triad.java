package de.rwthaachen.openlap.analyticsmodules.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rwthaachen.openlap.analyticsengine.model.Question;
import de.rwthaachen.openlap.dataset.OpenLAPPortConfig;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

/**
 * A Triad is used to save the Indicator/Method/Visualization along with the particular configuration between the
 * Indicator to the Analytics Method as well as the one between the Analytics Method to the Visualization
 */
@Entity
@Table(name = "Triad")
public class Triad {

    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    @Column(name = "triad_id")
    long id;

    @Column(nullable = false)
    long goalId;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Convert(converter = IndicatorReferenceConverter.class)
    IndicatorReference indicatorReference;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Convert(converter = AnalyticsMethodReferenceConverter.class)
    AnalyticsMethodReference analyticsMethodReference;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Convert(converter = VisualizerReferenceConverter.class)
    VisualizerReference visualizationReference;

//    @Column(columnDefinition = "TEXT")
//    @Convert(converter = OpenLAPPortConfigConverter.class)
//    OpenLAPPortConfig indicatorToAnalyticsMethodMapping;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = OpenLAPPortConfigReferenceConverter.class)
    OpenLAPPortConfigReference indicatorToAnalyticsMethodMapping;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = OpenLAPPortConfigConverter.class)
    OpenLAPPortConfig analyticsMethodToVisualizationMapping;

    @ManyToMany(mappedBy = "triads", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Question> questions = new HashSet<Question>();

    @Column(columnDefinition = "TEXT")
    private String parameters;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private Timestamp createdOn;

    @Column(nullable = false)
    private Timestamp lastExecutedOn;

    @Column(nullable = false)
    private int timesExecuted;

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
     * @param indicatorToAnalyticsMethodMapping     The OpenLAPPortConfig between the Indicator and the Analytics Method
     *                                              of this Triad.
     * @param analyticsMethodToVisualizationMapping The OpenLAPPortConfig between the Analytics Method and the
     *                                              Visualization of this Triad.
     */
    public Triad(long id,
                 long goalId,
                 IndicatorReference indicatorReference,
                 AnalyticsMethodReference analyticsMethodReference,
                 VisualizerReference visualizationReference,
                 OpenLAPPortConfigReference indicatorToAnalyticsMethodMapping,
                 OpenLAPPortConfig analyticsMethodToVisualizationMapping) {
        //this.id = id;
        this.goalId = goalId;
        this.indicatorReference = indicatorReference;
        this.analyticsMethodReference = analyticsMethodReference;
        this.visualizationReference = visualizationReference;
        this.indicatorToAnalyticsMethodMapping = indicatorToAnalyticsMethodMapping;
        this.analyticsMethodToVisualizationMapping = analyticsMethodToVisualizationMapping;
        this.parameters = "[]";
        this.createdBy = "";
        this.createdOn = new Timestamp(System.currentTimeMillis());
        this.lastExecutedOn = new Timestamp(System.currentTimeMillis());
        this.timesExecuted = 0;
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
     * @param indicatorToAnalyticsMethodMapping     The OpenLAPPortConfig between the Indicator and the Analytics Method
     *                                              of this Triad.
     * @param analyticsMethodToVisualizationMapping The OpenLAPPortConfig between the Analytics Method and the
     *                                              Visualization of this Triad.
     */
    public Triad(IndicatorReference indicatorReference,
                 AnalyticsMethodReference analyticsMethodReference,
                 VisualizerReference visualizationReference,
                 OpenLAPPortConfigReference indicatorToAnalyticsMethodMapping,
                 OpenLAPPortConfig analyticsMethodToVisualizationMapping) {
        this.indicatorReference = indicatorReference;
        this.analyticsMethodReference = analyticsMethodReference;
        this.visualizationReference = visualizationReference;
        this.indicatorToAnalyticsMethodMapping = indicatorToAnalyticsMethodMapping;
        this.analyticsMethodToVisualizationMapping = analyticsMethodToVisualizationMapping;
        this.parameters = "[]";
        this.createdBy = "";
        this.createdOn = new Timestamp(System.currentTimeMillis());
        this.lastExecutedOn = new Timestamp(System.currentTimeMillis());
        this.timesExecuted = 0;
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
     * @param indicatorToAnalyticsMethodMapping     The OpenLAPPortConfig between the Indicator and the Analytics Method
     *                                              of this Triad.
     * @param analyticsMethodToVisualizationMapping The OpenLAPPortConfig between the Analytics Method and the
     *                                              Visualization of this Triad.
     */
    public Triad(long goalId,
                 IndicatorReference indicatorReference,
                 AnalyticsMethodReference analyticsMethodReference,
                 VisualizerReference visualizationReference,
                 OpenLAPPortConfigReference indicatorToAnalyticsMethodMapping,
                 OpenLAPPortConfig analyticsMethodToVisualizationMapping) {
        this.goalId = goalId;
        this.indicatorReference = indicatorReference;
        this.analyticsMethodReference = analyticsMethodReference;
        this.visualizationReference = visualizationReference;
        this.indicatorToAnalyticsMethodMapping = indicatorToAnalyticsMethodMapping;
        this.analyticsMethodToVisualizationMapping = analyticsMethodToVisualizationMapping;
        this.parameters = "[]";
        this.createdBy = "";
        this.createdOn = new Timestamp(System.currentTimeMillis());
        this.lastExecutedOn = new Timestamp(System.currentTimeMillis());
        this.timesExecuted = 0;
    }

    public Triad(long goalId, AnalyticsMethodReference analyticsMethodReference, IndicatorReference indicatorReference,
                 VisualizerReference visualizationReference, OpenLAPPortConfigReference indicatorToAnalyticsMethodMapping,
                 OpenLAPPortConfig analyticsMethodToVisualizationMapping, Set<Question> questions,
                 String parameters, String createdBy, Timestamp createdOn, Timestamp lastExecutedOn,
                 int timesExecuted) {
        this.goalId = goalId;
        this.analyticsMethodReference = analyticsMethodReference;
        this.indicatorReference = indicatorReference;
        this.visualizationReference = visualizationReference;
        this.indicatorToAnalyticsMethodMapping = indicatorToAnalyticsMethodMapping;
        this.analyticsMethodToVisualizationMapping = analyticsMethodToVisualizationMapping;
        this.questions = questions;
        this.parameters = parameters;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
        this.lastExecutedOn = lastExecutedOn;
        this.timesExecuted = timesExecuted;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getGoalId() {
        return goalId;
    }

    public void setGoalId(long goalId) {
        this.goalId = goalId;
    }

    public IndicatorReference getIndicatorReference() {
        return indicatorReference;
    }

    public void setIndicatorReference(IndicatorReference indicatorReference) {
        this.indicatorReference = indicatorReference;
    }

    public AnalyticsMethodReference getAnalyticsMethodReference() {
        return analyticsMethodReference;
    }

    public void setAnalyticsMethodReference(AnalyticsMethodReference analyticsMethodReference) {
        this.analyticsMethodReference = analyticsMethodReference;
    }

    public VisualizerReference getVisualizationReference() {
        return visualizationReference;
    }

    public void setVisualizationReference(VisualizerReference visualizationReference) {
        this.visualizationReference = visualizationReference;
    }

    public OpenLAPPortConfigReference getIndicatorToAnalyticsMethodMapping() {
        return indicatorToAnalyticsMethodMapping;
    }

    public void setIndicatorToAnalyticsMethodMapping(OpenLAPPortConfigReference indicatorToAnalyticsMethodMapping) {
        this.indicatorToAnalyticsMethodMapping = indicatorToAnalyticsMethodMapping;
    }

    public OpenLAPPortConfig getAnalyticsMethodToVisualizationMapping() {
        return analyticsMethodToVisualizationMapping;
    }

    public void setAnalyticsMethodToVisualizationMapping(OpenLAPPortConfig analyticsMethodToVisualizationMapping) {
        this.analyticsMethodToVisualizationMapping = analyticsMethodToVisualizationMapping;
    }

    public Set<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<Question> questions) {
        this.questions = questions;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Timestamp createdOn) {
        this.createdOn = createdOn;
    }

    public Timestamp getLastExecutedOn() {
        return lastExecutedOn;
    }

    public void setLastExecutedOn(Timestamp lastExecutedOn) {
        this.lastExecutedOn = lastExecutedOn;
    }

    public int getTimesExecuted() {
        return timesExecuted;
    }

    public void setTimesExecuted(int timesExecuted) {
        this.timesExecuted = timesExecuted;
    }

    /**
     * Updates this object with the information of the Triad passed as a parameter
     *
     * @param triad with the information to be used to update this object.
     */
    public void updateWithTriad(Triad triad) {
        this.setGoalId(triad.getGoalId());
        this.setAnalyticsMethodReference(triad.getAnalyticsMethodReference());
        this.setIndicatorReference(triad.getIndicatorReference());
        this.setVisualizationReference(triad.getVisualizationReference());
        this.setAnalyticsMethodToVisualizationMapping(triad.getAnalyticsMethodToVisualizationMapping());
        this.setIndicatorToAnalyticsMethodMapping(triad.getIndicatorToAnalyticsMethodMapping());
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Triad{" +
                    "id=" + id +
                    ", goalId=" + goalId +
                    ", analyticsMethodReference=" + analyticsMethodReference +
                    ", indicatorReference=" + indicatorReference +
                    ", visualizationReference=" + visualizationReference +
                    ", indicatorToAnalyticsMethodMapping=" + indicatorToAnalyticsMethodMapping +
                    ", analyticsMethodToVisualizationMapping=" + analyticsMethodToVisualizationMapping +
                    ", questions=" + questions +
                    ", parameters='" + parameters + '\'' +
                    ", createdBy='" + createdBy + '\'' +
                    ", createdOn=" + createdOn +
                    ", lastExecutedOn=" + lastExecutedOn +
                    ", timesExecuted=" + timesExecuted +
                    '}';
        }
    }

    /**
     * Attempts to return a JSON human-readable form representation of this object. If it fails, it defaults to standard
     * JAVA string representation.
     *
     * @return A string representation of this Object.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Triad)) return false;

        Triad triad = (Triad) o;

        if (getId() != triad.getId()) return false;
        if (getGoalId() != triad.getGoalId()) return false;
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
        int result = (int) (getId() ^ (getId() >>> 32));
        result = 31 * result + (int) (getGoalId() ^ (getGoalId() >>> 32));
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

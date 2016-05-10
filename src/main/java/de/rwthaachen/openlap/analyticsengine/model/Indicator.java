package de.rwthaachen.openlap.analyticsengine.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rwthaachen.openlap.analyticsmodules.model.AnalyticsGoal;

import javax.persistence.*;

/**
 * Created by Arham Muslim
 * on 11-Mar-16.
 */
@Entity
@Table(name = "Indicator")
public class Indicator {

    @Id
    @GeneratedValue
    @Column(name = "indicator_id")
    private long id;

    @Column(name = "query", columnDefinition = "TEXT")
    private String query;

    @Column(name = "name", columnDefinition = "LONGVARCHAR")
    private String name;

    @Column(name = "short_name", columnDefinition = "LONGVARCHAR")
    private String shortName;


    /**
     * Empty constructor
     */
    public Indicator() {
        this.name = "";
        this.query = "";
        this.shortName = "";
    }

    /**
     * Standard constructor
     *
     * @param name      Name of the Indicator
     * @param shortName Short name of the indicator
     * @param query     Query for the indicator
     */
    public Indicator(String name, String shortName, String query) {
        this.name = name;
        this.shortName = shortName;
        this.query = query;
    }

    /**
     * @return ID of the Indicator
     */
    public long getId() {
        return id;
    }

    /**
     * @param id ID to be set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return Name of the Indicator
     */
    public String getName() {
        return name;
    }

    /**
     * @param name Name to be set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Short Name of the indicator
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @param shortName Description to be set.
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * @return Query for the indicator
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query Query for the indicator.
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Update this object with the values from another Indicator.
     *
     * @param indicator containing the data to be updated.
     */
    public void updateWithAnalyticsGoal(Indicator indicator) {
        this.setName(indicator.getName());
        this.setShortName(indicator.getShortName());
        this.setQuery(indicator.getQuery());
    }

    /**
     * Attempts to return a JSON representation of the object, defaults to a string representation otherwise.
     *
     * @return A JSON text representation of the object.
     */
    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Indicator{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", shortName='" + shortName + '\'' +
                    ", query='" + query + '\'' +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnalyticsGoal)) return false;

        Indicator that = (Indicator) o;

        if (getId() != that.getId()) return false;
        if (!getName().equals(that.getName())) return false;
        if (!getShortName().equals(that.getShortName())) return false;
        return getQuery().equals(that.getQuery());
    }

    @Override
    public int hashCode() {
        int result = (getId() + "").hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + getShortName().hashCode();
        result = 31 * result + getQuery().hashCode();
        return result;
    }
}
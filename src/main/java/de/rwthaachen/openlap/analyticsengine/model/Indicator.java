package de.rwthaachen.openlap.analyticsengine.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.*;

/**
 * Created by Arham Muslim
 * on 11-Mar-16.
 */
@Entity
@Table(name = "Indicator")
public class Indicator {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "indicator_id")
    private long id;

    @Column(name = "query", columnDefinition = "TEXT", nullable = false)
    @Convert(converter = IndicatorQueryConverter.class)
    private IndicatorQuery query;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private boolean isComposite;

    /**
     * Empty constructor
     */
    public Indicator() {
        this.name = "";
        this.isComposite = false;
        this.query = new IndicatorQuery();
    }

    /**
     *
     * Partial Indicator
     *
     * @param name
     * @param query
     * @param isComposite
     */
    public Indicator(String name, IndicatorQuery query, boolean isComposite) {
        this.name = name;
        this.query = query;
        this.isComposite = isComposite;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public IndicatorQuery getQuery() {
        return query;
    }

    public void setQuery(IndicatorQuery query) {
        this.query = query;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isComposite() {
        return isComposite;
    }

    public void setComposite(boolean composite) {
        isComposite = composite;
    }

    /**
     * Update this object with the values from another Indicator.
     *
     * @param indicator containing the data to be updated.
     */
    public void updateWithIndicator(Indicator indicator) {
        this.setName(indicator.getName());
        this.setQuery(indicator.getQuery());
        this.setComposite(indicator.isComposite());
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
                    "id=" + id +
                    ", query=" + query +
                    ", name='" + name + '\'' +
                    ", isComposite=" + isComposite +
                    '}';
        }
    }




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Indicator)) return false;

        Indicator indicator = (Indicator) o;

        if (getId() != indicator.getId()) return false;
        if (!getQuery().equals(indicator.getQuery())) return false;
        return getName().equals(indicator.getName());

    }

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));
        result = 31 * result + getQuery().hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + (isComposite() ? 1 : 0);
        return result;
    }
}
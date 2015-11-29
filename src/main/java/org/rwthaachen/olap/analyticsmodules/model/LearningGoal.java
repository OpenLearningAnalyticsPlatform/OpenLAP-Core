package org.rwthaachen.olap.analyticsmodules.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.rwthaachen.olap.analyticsmethods.model.AnalyticsMethodMetadata;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lechip on 27/11/15.
 */
@Entity
public class LearningGoal {

    @Id
    @GeneratedValue
    @Column(name = "LGOAL_ID")
    private String id;

    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private boolean isActive;

    @Column(columnDefinition="LONGVARCHAR")
    @Convert(converter = AnalyticsMethodMetadataSetConverter.class)
    Set<AnalyticsMethodMetadata> analyticsMethods;

    public LearningGoal() {
        this.id = "";
        this.name = "";
        this.author = "";
        this.description = "";
        this.isActive = false;
        this.analyticsMethods = new LinkedHashSet<AnalyticsMethodMetadata>();
    }

    public LearningGoal(String name, String description, String author, boolean isActive) {
        this.name = name;
        this.description = description;
        this.author = author;
        this.isActive = isActive;
        this.analyticsMethods = new LinkedHashSet<AnalyticsMethodMetadata>();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Set<AnalyticsMethodMetadata> getAnalyticsMethods() {
        return analyticsMethods;
    }

    public void setAnalyticsMethods(Set<AnalyticsMethodMetadata> analyticsMethods) {
        this.analyticsMethods = analyticsMethods;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "LearningGoal{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", author='" + author + '\'' +
                    ", isActive=" + isActive +
                    ", analyticsMethods=" + analyticsMethods +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LearningGoal)) return false;

        LearningGoal that = (LearningGoal) o;

        if (isActive() != that.isActive()) return false;
        if (!getId().equals(that.getId())) return false;
        if (!getName().equals(that.getName())) return false;
        if (!getDescription().equals(that.getDescription())) return false;
        return getAuthor().equals(that.getAuthor());

    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + getDescription().hashCode();
        result = 31 * result + getAuthor().hashCode();
        result = 31 * result + (isActive() ? 1 : 0);
        return result;
    }
}

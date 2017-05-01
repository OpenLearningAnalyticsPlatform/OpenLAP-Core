package de.rwthaachen.openlap.analyticsengine.model;

import de.rwthaachen.openlap.analyticsmodules.model.AnalyticsGoal;
import de.rwthaachen.openlap.analyticsmodules.model.Triad;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Arham Muslim
 * on 14-Jun-16.
 */
@Entity
@Table(name = "Question")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "question_id")
    private long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int indicatorCount;

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
    @JoinTable(name = "Question_Triad", joinColumns = { @JoinColumn(name = "question_id") }, inverseJoinColumns = { @JoinColumn(name = "triad_id") })
    private Set<Triad> triads = new HashSet<Triad>();

    public Question() {
        this.name = "";
        this.indicatorCount = 0;
    }

    public Question(String name, int indicatorCount) {
        this.name = name;
        this.indicatorCount = indicatorCount;
    }

    public Question(String name, int indicatorCount, Set<Triad> triads) {
        this.name = name;
        this.indicatorCount = indicatorCount;
        this.triads = triads;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndicatorCount() {
        return indicatorCount;
    }

    public void setIndicatorCount(int indicatorCount) {
        this.indicatorCount = indicatorCount;
    }

    public Set<Triad> getTriads() {
        return triads;
    }

    public void setTriads(Set<Triad> triads) {
        this.triads = triads;
    }
}

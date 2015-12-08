package de.rwthaachen.openlap.analyticsmodules.model;

/**
 * Created by lechip on 29/11/15.
 */
public class IndicatorReference {
    long id;
    String shortName;
    String indicatorName;

    public IndicatorReference() {
    }

    public IndicatorReference(long id, String shortName, String indicatorName) {
        this.id = id;
        this.shortName = shortName;
        this.indicatorName = indicatorName;
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

    public String getIndicator_name() {
        return indicatorName;
    }

    public void setIndicator_name(String indicator_name) {
        this.indicatorName = indicator_name;
    }

    public String getShort_name() {
        return indicatorName;
    }

    public void setShort_name(String short_name) {
        this.indicatorName = short_name;
    }
}

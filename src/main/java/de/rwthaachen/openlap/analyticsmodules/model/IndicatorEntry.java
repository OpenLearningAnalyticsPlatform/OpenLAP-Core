package de.rwthaachen.openlap.analyticsmodules.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by Arham on 1/3/2017.
 */
public class IndicatorEntry {

    //HashMap to store the ids of the analyics method and json of HashMap for additional input parameters
    long id;
    String indicatorName;

    public IndicatorEntry(){}

    public IndicatorEntry(long id, String indicatorName) {
        this.id = id;
        this.indicatorName = indicatorName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIndicatorName() {
        return indicatorName;
    }

    public void setIndicatorName(String indicatorName) {
        this.indicatorName = indicatorName;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "IndicatorEntry{" +
                    "id=" + id +
                    ", indicatorName='" + indicatorName + '\'' +
                    '}';
        }
    }
}

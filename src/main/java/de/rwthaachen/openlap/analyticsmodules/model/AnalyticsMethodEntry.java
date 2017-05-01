package de.rwthaachen.openlap.analyticsmodules.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arham on 1/3/2017.
 */
public class AnalyticsMethodEntry {

    //HashMap to store the ids of the analyics method and json of HashMap for additional input parameters
    long id;
    Map<String, String> additionalParams;

    public AnalyticsMethodEntry(){
        additionalParams = new HashMap<String, String>();
    }

    public AnalyticsMethodEntry(long id, Map<String, String> additionalParams) {
        this.id = id;
        this.additionalParams = additionalParams;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Map<String, String> getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(Map<String, String> additionalParams) {
        this.additionalParams = additionalParams;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "AnalyticsMethodEntry{" +
                    "id=" + id +
                    ", additionalParams=" + additionalParams +
                    '}';
        }
    }
}

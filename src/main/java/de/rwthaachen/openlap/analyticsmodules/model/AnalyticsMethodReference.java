package de.rwthaachen.openlap.analyticsmodules.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arham on 1/3/2017.
 */
public class AnalyticsMethodReference {

    //HashMap to store the ids of the analyics method and json of HashMap for additional input parameters
    Map<String, AnalyticsMethodEntry> analyticsMethods;

    public AnalyticsMethodReference(){
        analyticsMethods = new HashMap<String, AnalyticsMethodEntry>();
    }

    public Map<String, AnalyticsMethodEntry> getAnalyticsMethods() {
        return analyticsMethods;
    }

    public void setAnalyticsMethods(Map<String, AnalyticsMethodEntry> analyticsMethods) {
        this.analyticsMethods = analyticsMethods;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "AnalyticsMethodReference{" +
                    "analyticsMethods=" + analyticsMethods +
                    '}';
        }
    }
}

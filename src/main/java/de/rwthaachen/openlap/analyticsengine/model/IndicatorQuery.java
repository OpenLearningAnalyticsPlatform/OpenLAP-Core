package de.rwthaachen.openlap.analyticsengine.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arham on 1/9/2017.
 */
public class IndicatorQuery {
    Map<String, String> queries;

    public IndicatorQuery(){
        queries = new HashMap<String, String>();
    }

    public Map<String, String> getQueries() {
        return queries;
    }

    public void setQueries(Map<String, String> queries) {
        this.queries = queries;
    }
}

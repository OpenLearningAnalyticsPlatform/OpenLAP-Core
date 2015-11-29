package org.rwthaachen.olap.analyticsmodules.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.rwthaachen.olap.analyticsmethods.model.AnalyticsMethodMetadata;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by lechip on 29/11/15.
 */
public class AnalyticsMethodMetadataSetConverter implements AttributeConverter<Set<AnalyticsMethodMetadata>, String> {

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Set<AnalyticsMethodMetadata> attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return attribute.toString();
        }
    }

    @Override
    public Set<AnalyticsMethodMetadata> convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, new TypeReference<Set<AnalyticsMethodMetadata>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new LinkedHashSet<AnalyticsMethodMetadata>();
        }
    }
}

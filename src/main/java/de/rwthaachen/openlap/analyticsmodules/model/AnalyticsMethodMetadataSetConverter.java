package de.rwthaachen.openlap.analyticsmodules.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rwthaachen.openlap.analyticsmethods.model.AnalyticsMethodMetadata;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An object Mapper for the DataAccessLayer to convert an AnalyticsMethodMetadata sets to a String during persistence
 * operations
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
            return mapper.readValue(dbData, new TypeReference<Set<AnalyticsMethodMetadata>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            return new LinkedHashSet<AnalyticsMethodMetadata>();
        }
    }
}

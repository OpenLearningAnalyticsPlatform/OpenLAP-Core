package de.rwthaachen.openlap.analyticsmodules.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rwthaachen.openlap.analyticsmethods.model.AnalyticsMethodMetadata;

import javax.persistence.AttributeConverter;
import java.io.IOException;

/**
 * An object Mapper for the DataAccessLayer to convert an AnalyticsMethodMetadata to a String during persistence
 * operations
 */
public class AnalyticsMethodMetadataConverter implements AttributeConverter<AnalyticsMethodMetadata, String> {

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(AnalyticsMethodMetadata attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return attribute.toString();
        }
    }

    @Override
    public AnalyticsMethodMetadata convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, AnalyticsMethodMetadata.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

package de.rwthaachen.openlap.analyticsengine.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import java.io.IOException;

/**
 * An object Mapper for the DataAccessLayer to convert an IndicatorQuery to a String during persistence
 * operations
 */
public class IndicatorQueryConverter implements AttributeConverter<IndicatorQuery, String> {

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(IndicatorQuery indicatorQuery) {
        try {
            return mapper.writeValueAsString(indicatorQuery);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return indicatorQuery.toString();
        }
    }

    @Override
    public IndicatorQuery convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, IndicatorQuery.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

package de.rwthaachen.openlap.analyticsmodules.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import java.io.IOException;

/**
 * An object Mapper for the DataAccessLayer to convert an IndicatorReference sets to a String during persistence
 * operations
 */
public class IndicatorReferenceConverter implements AttributeConverter<IndicatorReference, String> {

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(IndicatorReference attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return attribute.toString();
        }
    }

    @Override
    public IndicatorReference convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, IndicatorReference.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

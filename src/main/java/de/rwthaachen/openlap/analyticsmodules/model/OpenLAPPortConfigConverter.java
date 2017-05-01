package de.rwthaachen.openlap.analyticsmodules.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rwthaachen.openlap.dataset.OpenLAPPortConfig;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;

/**
 * An object Mapper for the DataAccessLayer to convert an OpenLAPPortConfig sets to a String during persistence
 * operations
 */
@Converter
public class OpenLAPPortConfigConverter implements AttributeConverter<OpenLAPPortConfig, String> {

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(OpenLAPPortConfig attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return attribute.toString();
        }
    }

    @Override
    public OpenLAPPortConfig convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, OpenLAPPortConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

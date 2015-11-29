package org.rwthaachen.olap.analyticsmodules.model;

import OLAPDataSet.OLAPPortConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;

/**
 * Created by lechip on 29/11/15.
 */
@Converter
public class OLAPPortConfigurationConverter implements AttributeConverter<OLAPPortConfiguration, String> {

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(OLAPPortConfiguration attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return attribute.toString();
        }
    }

    @Override
    public OLAPPortConfiguration convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, OLAPPortConfiguration.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

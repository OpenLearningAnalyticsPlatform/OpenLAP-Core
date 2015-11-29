package org.rwthaachen.olap.analyticsmodules.model;

import OLAPDataSet.OLAPPortConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.rwthaachen.olap.analyticsmethods.model.AnalyticsMethodMetadata;

import javax.persistence.AttributeConverter;
import java.io.IOException;

/**
 * Created by lechip on 29/11/15.
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

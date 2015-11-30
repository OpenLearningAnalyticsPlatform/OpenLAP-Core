package org.rwthaachen.olap.analyticsmethods.service;

import org.springframework.beans.factory.annotation.Value;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * TODO
 */
public class SimpleXmlSchemaValidator {

    /**
     * TODO
     * @param validationInformation
     * @param streamXmlToCheck
     * @param xsdUrl
     * @return
     */
    public static boolean validateXML(AnalyticsMethodsValidationInformation validationInformation,
                                      InputStream streamXmlToCheck, String xsdUrl)
    {
        URL schemaFile;
        Source xmlFile= new StreamSource(streamXmlToCheck);
        try {
            schemaFile = new URL(xsdUrl);
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.validate(xmlFile);
            return true;
        } catch (SAXException e) {
            validationInformation.appendMessage("Invalid PMML File: " + e.getLocalizedMessage());
            validationInformation.setValid(false);
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            validationInformation.setValid(false);
            return false;
        }
    }
}

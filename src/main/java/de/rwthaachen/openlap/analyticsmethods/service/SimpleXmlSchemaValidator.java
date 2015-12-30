package de.rwthaachen.openlap.analyticsmethods.service;

import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * An utility class to validate XML files against an XSD.
 */
public class SimpleXmlSchemaValidator {

    /**
     * Validates an XML in an InputStream against an XSD.
     * @param validationInformation Object to return the information of the validity of the XML.
     * @param streamXmlToCheck Stream with the XML to be validated.
     * @param xsdUrl A String with the URL of the XSD to validate the XML with.
     * @return True if the XML is valid according to the XSD, false otherwise.
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

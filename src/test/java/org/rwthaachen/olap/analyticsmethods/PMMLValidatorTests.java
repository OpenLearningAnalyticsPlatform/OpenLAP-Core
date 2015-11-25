package org.rwthaachen.olap.analyticsmethods;

import org.junit.Assert;
import org.junit.Test;
import org.rwthaachen.olap.analyticsmethods.service.AnalyticsMethodsValidationInformation;
import org.rwthaachen.olap.analyticsmethods.service.SimpleXmlSchemaValidator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 * Created by lechip on 24/11/15.
 */
public class PMMLValidatorTests {

    public static final String PMML_EXAMPLE_VALID = "/pmmlXmlSamples/single_audit_kmeans.xml";
    public static final String PMML_EXAMPLE_WRONG = "/pmmlXmlSamples/example_breakfast.xml";

    String schemaUrl = "http://dmg.org/pmml/v4-1/pmml-4-1.xsd";
    //Tests an example PMML
    @Test
    public void testPMMLValidation() throws URISyntaxException, IOException {
        AnalyticsMethodsValidationInformation validationInformation = new AnalyticsMethodsValidationInformation();
        // Prepare valid file of PMML XML
        //URL fileUrlValid  = getClass().getClassLoader().getResource(PMML_EXAMPLE_VALID);
        //File pmmlFileValid = new File(fileUrlValid.toURI());
        // Prepare not valid fil of XML
        InputStream pmmlFileValid = getClass().getResourceAsStream(PMML_EXAMPLE_VALID);
        //URL fileUrlWrong  = getClass().getClassLoader().getResource(PMML_EXAMPLE_WRONG);
        //File pmmlFileWrong = new File(fileUrlWrong.toURI());
        InputStream pmmlFileWrong = getClass().getResourceAsStream(PMML_EXAMPLE_WRONG);
        Assert.assertTrue(SimpleXmlSchemaValidator.validateXML(validationInformation, pmmlFileValid, schemaUrl));
        Assert.assertFalse(SimpleXmlSchemaValidator.validateXML(validationInformation, pmmlFileWrong, schemaUrl));
    }

}

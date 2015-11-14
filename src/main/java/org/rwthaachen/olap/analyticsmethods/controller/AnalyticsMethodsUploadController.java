package org.rwthaachen.olap.analyticsmethods.controller;

import OLAPDataSet.DataSetConfigurationValidationResult;
import OLAPDataSet.OLAPColumnConfigurationData;
import OLAPDataSet.OLAPPortConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodsBadRequestException;
import org.rwthaachen.olap.analyticsmethods.model.AnalyticsMethodMetadata;
import org.rwthaachen.olap.analyticsmethods.service.AnalyticsMethodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A spring Controller that acts as a facade, exposing an API for handling JSON requests to the Analyics Methods
 * macro component of the OLAP
 */
@Controller
public class AnalyticsMethodsUploadController {

    @Autowired
    AnalyticsMethodsService analyticsMethodsService;

    @RequestMapping(
            value = "/AnalyticsMethods",
            method = RequestMethod.GET
    )
    public @ResponseBody
    List<AnalyticsMethodMetadata> viewAllAnalyticsMethods()
    {
        return analyticsMethodsService.viewAllAnalyticsMethods();
    }

    @RequestMapping
            (
                    value = "/AnalyticsMethods/{id}",
                    method = RequestMethod.GET
            )
    public @ResponseBody AnalyticsMethodMetadata viewAnalyticsMethod(@PathVariable String id)
    {
        return analyticsMethodsService.viewAnalyticsMethod(id);
    }

    //TODO: Add the file handler
    @RequestMapping
            (
                    value = "/uploadAnalyticsMethod",
                    method = RequestMethod.POST
            )
    public @ResponseBody AnalyticsMethodMetadata uploadAnalyticsMethod
    (
            @RequestParam ("methodMetadata") String methodMetadataText,
            //@RequestParam("jarBundle") String base64EncodedJar
            @RequestParam ("jarBundle") MultipartFile jarBundle
    )
    {

        ObjectMapper mapper = new ObjectMapper();
        AnalyticsMethodMetadata methodMetadata = null;

        try {
            // Attempt to interpret the json to construct the metadata object. It has to be done like this because
            // the json is sent as a form request text (since the file is also part of the form),
            // which does not support directly JSON.
            methodMetadata = mapper.readValue(methodMetadataText,
                    AnalyticsMethodMetadata.class);
            return analyticsMethodsService.uploadAnalyticsMethod(methodMetadata, jarBundle);
        } catch (IOException e) {
            e.printStackTrace();
            throw new AnalyticsMethodsBadRequestException(e.getMessage());
        }
    }

    //TODO: Add the file handler
    @RequestMapping
            (
                    value = "/updateAnalyticsMethod/{id}",
                    method = RequestMethod.PUT
            )
    public @ResponseBody  AnalyticsMethodMetadata updateAnalyticsMethod
            (
                    @RequestBody AnalyticsMethodMetadata methodMetadata,
                    @RequestParam(value = "file", required = false) String file,
                    @PathVariable String id
            )
    {
        return new AnalyticsMethodMetadata(id,file,"",methodMetadata.getDescription(),null);
    }

    //TODO: Add the file handler
    @RequestMapping
            (
                    value = "/validateConfiguration/{id}",
                    method = RequestMethod.POST
            )
    public @ResponseBody DataSetConfigurationValidationResult validateConfiguration
            (
                    @RequestBody OLAPPortConfiguration configurationMapping,
                    @PathVariable String id
            )
    {

        return new DataSetConfigurationValidationResult(true, configurationMapping.toString() +
                " Validated by Analytics Method: " + id);
    }

    //TODO: Add the file handler
    @RequestMapping
            (
                    value = "getInputPorts/{id}",
                    method = RequestMethod.GET
            )
    public @ResponseBody List<OLAPColumnConfigurationData> getInputPorts
            (
                    @PathVariable String id
            )
    {
        // TODO this is just a placeholder method
        return new ArrayList<OLAPColumnConfigurationData>(Arrays.asList(new OLAPColumnConfigurationData()));
    }

    //TODO: Add the file handler
    @RequestMapping
            (
                    value = "getOutputPorts/{id}",
                    method = RequestMethod.GET
            )
    public @ResponseBody List<OLAPColumnConfigurationData> getOutputPorts
            (
                    @PathVariable String id
            )
    {
        return new ArrayList<OLAPColumnConfigurationData>(Arrays.asList(new OLAPColumnConfigurationData()));
    }

}

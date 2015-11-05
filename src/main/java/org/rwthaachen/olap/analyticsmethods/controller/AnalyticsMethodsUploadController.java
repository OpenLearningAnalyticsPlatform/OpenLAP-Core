package org.rwthaachen.olap.analyticsmethods.controller;

import OLAPDataSet.DataSetConfigurationValidationResult;
import OLAPDataSet.OLAPPortConfiguration;
import OLAPDataSet.OLAPPortMapping;
import org.rwthaachen.olap.analyticsmethods.model.AnalyticsMethodMetadata;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A spring Controller that acts as a facade, exposing an API for handling JSON requests to the Analyics Methods
 * macro component of the OLAP
 */
@Controller
public class AnalyticsMethodsUploadController {

    @RequestMapping(
            value = "/viewAllAnalyticsMethods",
            method = RequestMethod.GET
    )
    public @ResponseBody
    List<AnalyticsMethodMetadata> viewAllAnalyticsMethods()
    {
        return new ArrayList<AnalyticsMethodMetadata>(Arrays.asList(new AnalyticsMethodMetadata(), new AnalyticsMethodMetadata()));
    }


    @RequestMapping
            (
                    value = "/viewAnalyticsMethod/{id}",
                    method = RequestMethod.GET
            )
    public @ResponseBody AnalyticsMethodMetadata viewAnalyticsMethod(@PathVariable String id)
    {
        return  new AnalyticsMethodMetadata(id,"view","","",null);
    }

    //TODO: Add the file handler, use requestParam for the file
    @RequestMapping
            (
                    value = "/uploadAnalyticsMethod",
                    method = RequestMethod.POST
            )
    public @ResponseBody AnalyticsMethodMetadata uploadAnalyticsMethod
            (
                    @RequestBody AnalyticsMethodMetadata methodMetadata,
                    @RequestParam("file") String file
            )
    {
        methodMetadata.setDescription(file);
        return methodMetadata;
    }

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

}

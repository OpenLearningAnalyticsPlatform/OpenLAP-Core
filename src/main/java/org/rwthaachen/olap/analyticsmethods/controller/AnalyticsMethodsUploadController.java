package org.rwthaachen.olap.analyticsmethods.controller;

import OLAPDataSet.DataSetConfigurationValidationResult;
import OLAPDataSet.OLAPColumnConfigurationData;
import OLAPDataSet.OLAPPortConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodLoaderException;
import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodNotFoundException;
import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodsBadRequestException;
import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodsUploadErrorException;
import org.rwthaachen.olap.analyticsmethods.model.AnalyticsMethodMetadata;
import org.rwthaachen.olap.analyticsmethods.service.AnalyticsMethodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

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

    @RequestMapping
            (
                    value = "/AnalyticsMethods",
                    method = RequestMethod.POST
            )
    public @ResponseBody AnalyticsMethodMetadata uploadAnalyticsMethod
    (
            @RequestParam ("methodMetadata") String methodMetadataText,
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


    @RequestMapping
            (
                    value = "/AnalyticsMethods/{id}",
                    method = RequestMethod.POST
            )
    public @ResponseBody  AnalyticsMethodMetadata updateAnalyticsMethod
            (
                    @RequestParam("methodMetadata") String methodMetadataText,
                    @RequestParam("jarBundle") MultipartFile jarBundle,
                    @PathVariable String id
            )
    {
        //TODO Implement
        ObjectMapper mapper = new ObjectMapper();
        AnalyticsMethodMetadata methodMetadata = null;

        try {
            // Attempt to interpret the json to construct the metadata object. It has to be done like this because
            // the json is sent as a form request text (since the file is also part of the form),
            // which does not support directly JSON.
            methodMetadata = mapper.readValue(methodMetadataText,
                    AnalyticsMethodMetadata.class);
            return analyticsMethodsService.updateAnalyticsMethod(methodMetadata, id, jarBundle);
        } catch (IOException e) {
            e.printStackTrace();
            throw new AnalyticsMethodsBadRequestException(e.getMessage());
        }
    }

    @RequestMapping
            (
                    value = "/AnalyticsMethods/{id}/validateConfiguration",
                    method = RequestMethod.POST
            )
    public @ResponseBody DataSetConfigurationValidationResult validateConfiguration
            (
                    @RequestBody OLAPPortConfiguration configurationMapping,
                    @PathVariable String id
            )
    {
        return analyticsMethodsService.validateConfiguration(id, configurationMapping);
    }


    @RequestMapping
            (
                    value = "AnalyticsMethods/{id}/getInputPorts",
                    method = RequestMethod.GET
            )
    public @ResponseBody List<OLAPColumnConfigurationData> getInputPorts
            (
                    @PathVariable String id
            )
    {
        return analyticsMethodsService.GetInputPortsForMethod(id);
    }


    @RequestMapping
            (
                    value = "AnalyticsMethods/{id}/getOutputPorts",
                    method = RequestMethod.GET
            )
    public @ResponseBody List<OLAPColumnConfigurationData> getOutputPorts
            (
                    @PathVariable String id
            )
    {
        //TODO Implement
        return analyticsMethodsService.GetOutputPortsForMethod(id);
    }

    @ExceptionHandler(AnalyticsMethodNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody
    AnalyticsMethodsErrorHandlerDTO handleMethodNotFoundException(AnalyticsMethodNotFoundException e,
                                                                  HttpServletRequest request)
    {
        AnalyticsMethodsErrorHandlerDTO errorObject = new AnalyticsMethodsErrorHandlerDTO(
                HttpStatus.NOT_FOUND.value(),
                e.getClass().getName(),
                e.getMessage(),
                request.getServletPath()
        );

        return errorObject;
    }

    @ExceptionHandler({AnalyticsMethodsUploadErrorException.class, IOException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody
    AnalyticsMethodsErrorHandlerDTO handleMethodsUploadErrorException(Exception e,
                                                                      HttpServletRequest request)
    {
        AnalyticsMethodsErrorHandlerDTO errorObject = new AnalyticsMethodsErrorHandlerDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                e.getClass().getName(),
                e.getMessage(),
                request.getServletPath()
        );

        return errorObject;
    }

    @ExceptionHandler(AnalyticsMethodsBadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody
    AnalyticsMethodsErrorHandlerDTO handleMethodsUploadBadRequestException(AnalyticsMethodsBadRequestException e,
                                                                      HttpServletRequest request)
    {
        AnalyticsMethodsErrorHandlerDTO errorObject = new AnalyticsMethodsErrorHandlerDTO(
                HttpStatus.BAD_REQUEST.value(),
                e.getClass().getName(),
                e.getMessage(),
                request.getServletPath()
        );

        return errorObject;
    }

    @ExceptionHandler(AnalyticsMethodLoaderException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody
    AnalyticsMethodsErrorHandlerDTO handleMethodsUploadBadRequestException(AnalyticsMethodLoaderException e,
                                                                           HttpServletRequest request)
    {
        AnalyticsMethodsErrorHandlerDTO errorObject = new AnalyticsMethodsErrorHandlerDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                e.getClass().getName(),
                e.getMessage(),
                request.getServletPath()
        );

        return errorObject;
    }
}

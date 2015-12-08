package de.rwthaachen.openlap.analyticsmethods.controller;

import DataSet.OLAPColumnConfigurationData;
import DataSet.OLAPDataSetConfigurationValidationResult;
import DataSet.OLAPPortConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rwthaachen.openlap.analyticsmethods.exceptions.AnalyticsMethodLoaderException;
import de.rwthaachen.openlap.analyticsmethods.exceptions.AnalyticsMethodNotFoundException;
import de.rwthaachen.openlap.analyticsmethods.exceptions.AnalyticsMethodsBadRequestException;
import de.rwthaachen.openlap.analyticsmethods.exceptions.AnalyticsMethodsUploadErrorException;
import de.rwthaachen.openlap.analyticsmethods.model.AnalyticsMethodMetadata;
import de.rwthaachen.openlap.analyticsmethods.service.AnalyticsMethodsService;
import de.rwthaachen.openlap.common.controller.GenericResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * A spring Controller that acts as a facade, exposing an API for handling JSON requests to the Analytics Methods
 * macro component of the OpenLAP
 */
@Controller
public class AnalyticsMethodsUploadController {

    @Autowired
    AnalyticsMethodsService analyticsMethodsService;

    /**
     * TODO
     * @return
     */
    @RequestMapping(
            value = "/AnalyticsMethods",
            method = RequestMethod.GET
    )
    public @ResponseBody
    List<AnalyticsMethodMetadata> viewAllAnalyticsMethods()
    {
        return analyticsMethodsService.viewAllAnalyticsMethods();
    }

    /**
     * TODO
     * @param id
     * @return
     */
    @RequestMapping
            (
                    value = "/AnalyticsMethods/{id}",
                    method = RequestMethod.GET
            )
    public @ResponseBody AnalyticsMethodMetadata viewAnalyticsMethod(@PathVariable String id)
    {
        return analyticsMethodsService.viewAnalyticsMethod(id);
    }

    /**
     * TODO
     * @param jarBundle
     * @param methodMetadataText
     * @return
     */
    @RequestMapping
            (
                    value = "/AnalyticsMethods",
                    method = RequestMethod.POST
            )
    public @ResponseBody AnalyticsMethodMetadata uploadAnalyticsMethod
    (
            @RequestParam ("jarBundle") MultipartFile jarBundle,
            @RequestParam ("methodMetadata") String methodMetadataText
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

    /**
     * TODO
     * @param methodMetadataText
     * @param jarBundle
     * @param id
     * @return
     */
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

    /**
     * TODO
     * @param configurationMapping
     * @param id
     * @return
     */
    @RequestMapping
            (
                    value = "/AnalyticsMethods/{id}/validateConfiguration",
                    method = RequestMethod.PUT
            )
    public @ResponseBody OLAPDataSetConfigurationValidationResult validateConfiguration
            (
                    @RequestBody OLAPPortConfiguration configurationMapping,
                    @PathVariable String id
            )
    {
        return analyticsMethodsService.validateConfiguration(id, configurationMapping);
    }

    /**
     * TODO
     * @param id
     * @return
     */
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


    /**
     * TODO
     * @param id
     * @return
     */
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
        return analyticsMethodsService.GetOutputPortsForMethod(id);
    }

    /**
     * HTTP endpoint handler method for deleting AnalyticsMethod
     * @param id id of the AnalyticsMethod to be deleted
     * @return GenericResponseDTO with deletion confirmation
     */
    @RequestMapping(
            value = "/AnalyticsMethods/{id}",
            method = RequestMethod.DELETE
    )
    public @ResponseBody
    GenericResponseDTO deleteAnalyticsMethod(@PathVariable String id){
        analyticsMethodsService.deleteAnalyticsMethod(id);
        return new GenericResponseDTO(HttpStatus.OK.value(),
                "Analytics Method with id {" + id + "} deleted");
    }

    //region ExceptionHandlers
    @ExceptionHandler(AnalyticsMethodNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody
    GenericResponseDTO handleMethodNotFoundException(AnalyticsMethodNotFoundException e,
                                                     HttpServletRequest request)
    {
        GenericResponseDTO errorObject = new GenericResponseDTO(
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
    GenericResponseDTO handleMethodsUploadErrorException(Exception e,
                                                         HttpServletRequest request)
    {
        GenericResponseDTO errorObject = new GenericResponseDTO(
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
    GenericResponseDTO handleMethodsUploadBadRequestException(AnalyticsMethodsBadRequestException e,
                                                              HttpServletRequest request)
    {
        GenericResponseDTO errorObject = new GenericResponseDTO(
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
    GenericResponseDTO handleMethodsUploadBadRequestException(AnalyticsMethodLoaderException e,
                                                              HttpServletRequest request)
    {
        GenericResponseDTO errorObject = new GenericResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                e.getClass().getName(),
                e.getMessage(),
                request.getServletPath()
        );

        return errorObject;
    }
    //endregion
}

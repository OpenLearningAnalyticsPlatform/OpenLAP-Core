package de.rwthaachen.openlap.analyticsmethods.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.rwthaachen.openlap.analyticsmethods.exceptions.AnalyticsMethodLoaderException;
import de.rwthaachen.openlap.analyticsmethods.exceptions.AnalyticsMethodNotFoundException;
import de.rwthaachen.openlap.analyticsmethods.exceptions.AnalyticsMethodsBadRequestException;
import de.rwthaachen.openlap.analyticsmethods.exceptions.AnalyticsMethodsUploadErrorException;
import de.rwthaachen.openlap.analyticsmethods.model.AnalyticsMethodMetadata;
import de.rwthaachen.openlap.analyticsmethods.service.AnalyticsMethodsService;
import de.rwthaachen.openlap.common.controller.GenericResponseDTO;
import de.rwthaachen.openlap.dataset.OpenLAPColumnConfigData;
import de.rwthaachen.openlap.dataset.OpenLAPDataSetConfigValidationResult;
import de.rwthaachen.openlap.dataset.OpenLAPPortConfig;
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
     * HTTP endpoint handler method that lists all the Metadata of the  AnalyticsMethods available
     *
     * @return A List of the available AnalyticsMethods
     */
    @RequestMapping(
            value = "/AnalyticsMethods",
            method = RequestMethod.GET
    )
    public
    @ResponseBody
    List<AnalyticsMethodMetadata> viewAllAnalyticsMethods() {
        return analyticsMethodsService.viewAllAnalyticsMethods();
    }

    /**
     * HTTP endpoint handler method that returns the Metadata of the Analytics Method of the specified ID
     *
     * @param id ID of the AnalyticsMethod to view
     * @return The AnalyticsMethod with Metadata of the specified ID
     */
    @RequestMapping
            (
                    value = "/AnalyticsMethods/{id}",
                    method = RequestMethod.GET
            )
    public
    @ResponseBody
    AnalyticsMethodMetadata viewAnalyticsMethod(@PathVariable long id) {
        return analyticsMethodsService.viewAnalyticsMethod(id);
    }

    /**
     * HTTP endpoint handler method that enables to post an AnalyticsMethod to the Server to be validated and
     * made available for usage.
     *
     * @param jarBundle          The JAR file with the implementation of the AnalyticsMethod
     * @param methodMetadataText A string with the JSON of the metadata to upload as manifest of the AnalyticsMethod
     * @return The Metadata of the uploaded AnalyticsMethod if deemed valid by the OpenLAP
     */
    @RequestMapping
            (
                    value = "/AnalyticsMethods",
                    method = RequestMethod.POST
            )
    public
    @ResponseBody
    AnalyticsMethodMetadata uploadAnalyticsMethod
    (
            @RequestParam("jarBundle") MultipartFile jarBundle,
            @RequestParam("methodMetadata") String methodMetadataText
    ) {
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
     * HTTP endpoint handler method that allows to update an AnalyticsMethod to the Server to be validated and made
     * available for usage.
     *
     * @param methodMetadataText A string with the JSON of the metadata to upload as manifest of the AnalyticsMethod
     * @param jarBundle          The JAR file with the implementation of the AnalyticsMethod
     * @param id                 ID of the AnalyticsMethod Metadata that is to be updated.
     * @return The Metadata of the uploaded AnalyticsMethod if deemed valid by the OpenLAP
     */
    @RequestMapping
            (
                    value = "/AnalyticsMethods/{id}",
                    method = RequestMethod.POST
            )
    public
    @ResponseBody
    AnalyticsMethodMetadata updateAnalyticsMethod
    (
            @RequestParam("methodMetadata") String methodMetadataText,
            @RequestParam("jarBundle") MultipartFile jarBundle,
            @PathVariable long id
    ) {
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
     * HTTP endpoint handler method that allows to validate an OpenLAPPortConfig of a specific AnalyticsMethod.
     *
     * @param configurationMapping The OpenLAPPortConfig to be validated
     * @param id                   The ID of the AnalyticsMethod Metadata to be validated against the OpenLAPPortConfig.
     * @return An Object with the validation information of the OpenLAPPortConfig against the specified Analytics
     * Method.
     */
    @RequestMapping
            (
                    value = "/AnalyticsMethods/{id}/validateConfiguration",
                    method = RequestMethod.PUT
            )
    public
    @ResponseBody
    OpenLAPDataSetConfigValidationResult validateConfiguration
    (
            @RequestBody OpenLAPPortConfig configurationMapping,
            @PathVariable long id
    ) {
        return analyticsMethodsService.validateConfiguration(id, configurationMapping);
    }

    /**
     * HTTP endpoint handler method that returns the OpenLAPColumnConfigData of the input ports of a
     * specific AnalyticsMethod
     *
     * @param id ID of the AnalyticsMethod Metadata
     * @return A list of OpenLAPColumnConfigData corresponding to the input ports of the AnalyticsMethod
     */
    @RequestMapping
            (
                    value = "AnalyticsMethods/{id}/getInputPorts",
                    method = RequestMethod.GET
            )
    public
    @ResponseBody
    List<OpenLAPColumnConfigData> getInputPorts
    (
            @PathVariable long id
    ) {
        return analyticsMethodsService.GetInputPortsForMethod(id);
    }


    /**
     * HTTP endpoint handler method that returns the OpenLAPColumnConfigData of the output ports of a
     * specific AnalyticsMethod
     *
     * @param id ID of the AnalyticsMethod Metadata
     * @return A list of OpenLAPColumnConfigData corresponding to the output ports of the AnalyticsMethod
     */
    @RequestMapping
            (
                    value = "AnalyticsMethods/{id}/getOutputPorts",
                    method = RequestMethod.GET
            )
    public
    @ResponseBody
    List<OpenLAPColumnConfigData> getOutputPorts
    (
            @PathVariable long id
    ) {
        return analyticsMethodsService.GetOutputPortsForMethod(id);
    }

    /**
     * HTTP endpoint handler method for deleting AnalyticsMethod
     *
     * @param id id of the AnalyticsMethod to be deleted
     * @return GenericResponseDTO with deletion confirmation
     */
    @RequestMapping(
            value = "/AnalyticsMethods/{id}",
            method = RequestMethod.DELETE
    )
    public
    @ResponseBody
    GenericResponseDTO deleteAnalyticsMethod(@PathVariable long id) {
        analyticsMethodsService.deleteAnalyticsMethod(id);
        return new GenericResponseDTO(HttpStatus.OK.value(),
                "Analytics Method with id {" + id + "} deleted");
    }

    //region ExceptionHandlers

    /**
     * Handler for AnalyticsMethodNotFoundException.
     * It returns the appropriate HTTP Error code.
     *
     * @param e       exception
     * @param request HTTP request
     * @return A GenericResponseDTO with the information about the exception and its cause.
     */
    @ExceptionHandler(AnalyticsMethodNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public
    @ResponseBody
    GenericResponseDTO handleMethodNotFoundException(AnalyticsMethodNotFoundException e,
                                                     HttpServletRequest request) {
        GenericResponseDTO errorObject = new GenericResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                e.getClass().getName(),
                e.getMessage(),
                request.getServletPath()
        );

        return errorObject;
    }

    /**
     * Handler for AnalyticsMethodsUploadErrorException and IOException
     * It returns the appropriate HTTP Error code.
     *
     * @param e       exception
     * @param request HTTP request
     * @return A GenericResponseDTO with the information about the exception and its cause.
     */
    @ExceptionHandler({AnalyticsMethodsUploadErrorException.class, IOException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public
    @ResponseBody
    GenericResponseDTO handleMethodsUploadErrorException(Exception e,
                                                         HttpServletRequest request) {
        GenericResponseDTO errorObject = new GenericResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                e.getClass().getName(),
                e.getMessage(),
                request.getServletPath()
        );

        return errorObject;
    }

    /**
     * Handler for AnalyticsMethodsBadRequestException
     * It returns the appropriate HTTP Error code.
     *
     * @param e       exception
     * @param request HTTP request
     * @return A GenericResponseDTO with the information about the exception and its cause.
     */
    @ExceptionHandler(AnalyticsMethodsBadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public
    @ResponseBody
    GenericResponseDTO handleMethodsUploadBadRequestException(AnalyticsMethodsBadRequestException e,
                                                              HttpServletRequest request) {
        GenericResponseDTO errorObject = new GenericResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                e.getClass().getName(),
                e.getMessage(),
                request.getServletPath()
        );

        return errorObject;
    }

    /**
     * Handler for AnalyticsMethodLoaderException
     * It returns the appropriate HTTP Error code.
     *
     * @param e       exception
     * @param request HTTP request
     * @return A GenericResponseDTO with the information about the exception and its cause.
     */
    @ExceptionHandler(AnalyticsMethodLoaderException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public
    @ResponseBody
    GenericResponseDTO handleMethodsUploadBadRequestException(AnalyticsMethodLoaderException e,
                                                              HttpServletRequest request) {
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

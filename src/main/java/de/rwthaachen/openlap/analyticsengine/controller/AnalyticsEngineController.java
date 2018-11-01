package de.rwthaachen.openlap.analyticsengine.controller;

import de.rwthaachen.openlap.analyticsengine.core.dtos.request.IndicatorPreviewRequest;
import de.rwthaachen.openlap.analyticsengine.core.dtos.request.QuestionSaveRequest;
import de.rwthaachen.openlap.analyticsengine.core.dtos.response.*;
import de.rwthaachen.openlap.analyticsengine.exceptions.BadRequestException;
import de.rwthaachen.openlap.analyticsengine.exceptions.ItemNotFoundException;
import de.rwthaachen.openlap.analyticsengine.service.AnalyticsEngineService;
import de.rwthaachen.openlap.analyticsmethods.model.AnalyticsMethodMetadata;
import de.rwthaachen.openlap.analyticsmodules.model.AnalyticsGoal;
import de.rwthaachen.openlap.common.controller.GenericResponseDTO;
import de.rwthaachen.openlap.dataset.OpenLAPColumnConfigData;
import de.rwthaachen.openlap.dynamicparam.OpenLAPDynamicParam;
import de.rwthaachen.openlap.visualizer.core.dtos.response.VisualizationFrameworkDetailsResponse;
import de.rwthaachen.openlap.visualizer.core.dtos.response.VisualizationFrameworksDetailsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Arham Muslim
 * on 30-Dec-15.
 */
@Controller
@RequestMapping(value="/AnalyticsEngine")
public class AnalyticsEngineController {

    @Autowired
    AnalyticsEngineService analyticsEngineService;

    @RequestMapping(value = {"/GetIndicatorData/", "/GetIndicatorData"}, method = RequestMethod.GET)
    public
    @ResponseBody
    String GetIndicatorData(
            @RequestParam(value = "tid", required = true) String triadID,
            @RequestParam Map<String, String> allRequestParams,
            HttpServletRequest request) {

        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());

        try {
            //return analyticsEngineService.executeIndicator(allRequestParams, baseUrl);
            return "Outdated request call. Please get new indicator request code from OpenLAP.";
        } catch (Exception exc) {
            return exc.getMessage();
        }
    }

    @RequestMapping(value = {"/GetIndicatorDataHQL/", "/GetIndicatorDataHQL"}, method = RequestMethod.GET)
    public
    @ResponseBody
    String GetIndicatorDataHQL(
            @RequestParam(value = "tid", required = true) String triadID,
            @RequestParam Map<String, String> allRequestParams,
            HttpServletRequest request) {

        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());

        try {
            return analyticsEngineService.executeIndicatorHQL(allRequestParams, baseUrl);
        } catch (Exception exc) {
            return exc.getMessage();
        }
    }

    @RequestMapping(value = "/GetIndicatorPreview", method = RequestMethod.POST)
    public
    @ResponseBody
    IndicatorPreviewResponse GetIndicatorPreview(
            @RequestBody IndicatorPreviewRequest previewRequest,
            @RequestParam Map<String, String> allRequestParams,
            HttpServletRequest request) {

        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());

        return analyticsEngineService.getIndicatorPreview(previewRequest, baseUrl);
    }

    @RequestMapping(value = "/GetCompositeIndicatorPreview", method = RequestMethod.POST)
    public
    @ResponseBody
    IndicatorPreviewResponse GetCompositeIndicatorPreview(
            @RequestBody IndicatorPreviewRequest previewRequest,
            @RequestParam Map<String, String> allRequestParams,
            HttpServletRequest request) {

        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());

        return analyticsEngineService.getCompIndicatorPreview(previewRequest, baseUrl);
    }

    @RequestMapping(value = "/GetMLAIIndicatorPreview", method = RequestMethod.POST)
    public
    @ResponseBody
    IndicatorPreviewResponse GetMLAIIndicatorPreview(
            @RequestBody IndicatorPreviewRequest previewRequest,
            @RequestParam Map<String, String> allRequestParams,
            HttpServletRequest request) {

        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());

        return analyticsEngineService.getMLAIIndicatorPreview(previewRequest, baseUrl);
    }


    @RequestMapping(value = "/SaveQuestionAndIndicators", method = RequestMethod.POST)
    public
    @ResponseBody
    QuestionSaveResponse SaveQuestionAndIndicators(
            @RequestBody QuestionSaveRequest questionSaveRequest,
            HttpServletRequest request) {

        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());

        return analyticsEngineService.saveQuestionAndIndicators(questionSaveRequest, request);
    }


    @RequestMapping(value = "/SaveQuestionAndIndicatorsDummy", method = RequestMethod.POST)
    public
    @ResponseBody
    QuestionSaveResponse saveQuestionAndIndicatorsDummy(
            @RequestBody QuestionSaveRequest questionSaveRequest,
            HttpServletRequest request) {

        return analyticsEngineService.saveQuestionAndIndicatorsDummy(questionSaveRequest);
    }

    @RequestMapping(value = {"/GetGoals/", "/GetAllGoals"}, method = RequestMethod.GET)
    public
    @ResponseBody
    List<AnalyticsGoal> GetAllGoals(@RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
        return analyticsEngineService.getAllGoals(request);
    }

    @RequestMapping(value = {"/ValidateQuestionName/", "/ValidateQuestionName"}, method = RequestMethod.GET)
    public
    @ResponseBody
    Boolean ValidateQuestionName(@RequestParam String name) {
        return analyticsEngineService.validateQuestionName(name);
    }

    @RequestMapping(value = {"/ValidateIndicatorName/", "/ValidateIndicatorName"}, method = RequestMethod.GET)
    public
    @ResponseBody
    Boolean ValidateIndicatorName(@RequestParam String name) {
        return analyticsEngineService.validateIndicatorName(name);
    }

    @RequestMapping(value = {"/GetGoals/", "/GetActiveGoals"}, method = RequestMethod.GET)
    public
    @ResponseBody
    List<AnalyticsGoal> GetActiveGoals(@RequestParam(value = "uid", required = false) String uid,
                                       @RequestParam Map<String, String> allRequestParams,
                                       HttpServletRequest request) {
        return analyticsEngineService.getActiveGoals(uid, request);
    }

    @RequestMapping(value = {"/SaveGoal/", "/SaveGoal"}, method = RequestMethod.GET)
    public
    @ResponseBody
    AnalyticsGoal SaveGoal(@RequestParam String name,
                           @RequestParam String description,
                           @RequestParam String author,
                           @RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
        return analyticsEngineService.saveGoal(name, description, author, request);
    }

    @RequestMapping(value = {"/SetGoalStatus/", "/SetGoalStatus"}, method = RequestMethod.GET)
    public
    @ResponseBody
    AnalyticsGoal SetGoalStatus(@RequestParam long goalId,
                                @RequestParam boolean isActive,
                                @RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
        return analyticsEngineService.setGoalStatus(goalId, isActive, request);
    }

    @RequestMapping(value = {"/GetAnalyticsMethods/", "/GetAnalyticsMethods"}, method = RequestMethod.GET)
    public
    @ResponseBody
    List<AnalyticsMethodMetadata> GetAllMethods(@RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
        return analyticsEngineService.getAllAnalyticsMethods(request);
    }

    @RequestMapping(value = {"/GetVisualizations/", "/GetVisualizations"}, method = RequestMethod.GET)
    public
    @ResponseBody
    VisualizationFrameworksDetailsResponse GetAllVisualizations(@RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
        return analyticsEngineService.getAllVisualizations(request);
    }

    @RequestMapping(value = {"/GetVisualizationMethods/", "/GetVisualizationMethods"}, method = RequestMethod.GET)
    public
    @ResponseBody
    VisualizationFrameworkDetailsResponse GetVisualizationMethods(@RequestParam long frameworkId, @RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
        return analyticsEngineService.getVisualizationsMethods(frameworkId, request);
    }

    @RequestMapping(value = {"/GetQuestions/", "/GetQuestions"}, method = RequestMethod.GET)
    public
    @ResponseBody
    List<QuestionResponse> GetQuestions(@RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
        return analyticsEngineService.getQuestions(request);
    }

    @RequestMapping(value = {"/GetIndicators/", "/GetIndicators"}, method = RequestMethod.GET)
    public
    @ResponseBody
    List<IndicatorResponse> GetIndicators(@RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
        return analyticsEngineService.getIndicators(request);
    }

    @RequestMapping(value = {"/GetIndicatorsByQuestionId/", "/GetIndicatorsByQuestionId"}, method = RequestMethod.GET)
    public
    @ResponseBody
    List<IndicatorResponse> GetIndicatorsByQuestionId(@RequestParam long questionId, @RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
        return analyticsEngineService.getIndicatorsByQuestionId(questionId, request);
    }

    @RequestMapping(value = {"/GetIndicatorById/", "/GetIndicatorById"}, method = RequestMethod.GET)
    public
    @ResponseBody
    IndicatorResponse GetTriadById(@RequestParam long indicatorId, @RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
        return analyticsEngineService.getTriadById(indicatorId, request);
    }


    @RequestMapping(value = {"/SearchQuestions/", "/SearchQuestions"}, method = RequestMethod.GET)
    public
    @ResponseBody
    List<QuestionResponse> SearchQuestions(@RequestParam String searchParameter,
                                           @RequestParam boolean exactSearch,
                                           @RequestParam String colName,
                                           @RequestParam String sortDirection,
                                           @RequestParam boolean sort,
                                           @RequestParam(value = "uid", required = false) String uid,
                                           @RequestParam Map<String, String> allRequestParams,
                                           HttpServletRequest request) {
        return analyticsEngineService.searchQuestions(searchParameter, exactSearch, colName, sortDirection, sort, uid, request);
    }

    @RequestMapping(value = {"/GetSortedQuestions/", "/GetSortedQuestions"}, method = RequestMethod.GET)
    public
    @ResponseBody
    List<QuestionResponse> GetSortedQuestions(@RequestParam String colName,
                                           @RequestParam String sortDirection,
                                           @RequestParam boolean sort,
                                           @RequestParam Map<String, String> allRequestParams,
                                           HttpServletRequest request) {
        return analyticsEngineService.getSortedQuestions(colName, sortDirection, sort, request);
    }

    @RequestMapping(value = {"/SearchIndicators/", "/SearchIndicators"}, method = RequestMethod.GET)
    public
    @ResponseBody
    List<IndicatorResponse> SearchIndicators(@RequestParam String searchParameter,
                                           @RequestParam boolean exactSearch,
                                           @RequestParam String colName,
                                           @RequestParam String sortDirection,
                                           @RequestParam boolean sort,
                                           @RequestParam(required = false) String userName,
                                           @RequestParam Map<String, String> allRequestParams,
                                           HttpServletRequest request) {
        return analyticsEngineService.searchIndicators(searchParameter, exactSearch, colName, sortDirection, sort, userName, request);
    }

    @RequestMapping(value = {"/GetDataColumnsByCatID/", "/GetDataColumnsByCatID"}, method = RequestMethod.GET)
    public
    @ResponseBody
    List<OpenLAPColumnConfigData> GetDataColumnsByIDs(@RequestParam String categoryIDs,
                                                      @RequestParam(value = "source", required = false) String source,
                                                      @RequestParam(value = "platform", required = false) String platform,
                                                      @RequestParam(value = "action", required = false) String action,
                                                      HttpServletRequest request) {
        return analyticsEngineService.getDataColumnsByIDs(categoryIDs, source, platform, action, request);
    }

    @RequestMapping(value = {"/GetDataColumnsByCatName/", "/GetDataColumnsByCatName"}, method = RequestMethod.GET)
    public
    @ResponseBody
    List<OpenLAPColumnConfigData> GetDataColumnsByNames(@RequestParam String categoryNames, @RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
        return analyticsEngineService.getDataColumnsByNames(categoryNames, request);
    }

    @RequestMapping(value = {"/GetAttributesValues/", "/GetAttributesValues"}, method = RequestMethod.GET)
    public
    @ResponseBody List<String> GetAttributesValues(
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "platform", required = false) String platform,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam String categoryIds,
            @RequestParam String key,
            HttpServletRequest request) {
        return analyticsEngineService.getAttributesValues(source, platform, action, categoryIds, key);
    }

    @RequestMapping(value = {"/GetEventSources/", "/GetEventSources"}, method = RequestMethod.GET)
    public
    @ResponseBody String GetEventSources(
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "platform", required = false) String platform,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "session", required = false) String session,
            @RequestParam(value = "timestampBegin", required = false) Integer timestampBegin,
            @RequestParam(value = "timestampEnd", required = false) Integer timestampEnd,
            @RequestParam(value = "sortDirection", required = false) String sortDirection,
            HttpServletRequest request) {
        return analyticsEngineService.getEventSources(source, platform, action, session, timestampBegin, timestampEnd, sortDirection);
    }

    @RequestMapping(value = {"/GetEventPlatforms/", "/GetEventPlatforms"}, method = RequestMethod.GET)
    public
    @ResponseBody String GetEventPlatforms(
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "platform", required = false) String platform,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "session", required = false) String session,
            @RequestParam(value = "timestampBegin", required = false) Integer timestampBegin,
            @RequestParam(value = "timestampEnd", required = false) Integer timestampEnd,
            @RequestParam(value = "sortDirection", required = false) String sortDirection,
            HttpServletRequest request) {
        return analyticsEngineService.getEventPlatforms(source, platform, action, session, timestampBegin, timestampEnd, sortDirection);
    }

    @RequestMapping(value = {"/GetEventActions/", "/GetEventActions"}, method = RequestMethod.GET)
    public
    @ResponseBody String GetEventActions(
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "platform", required = false) String platform,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "session", required = false) String session,
            @RequestParam(value = "timestampBegin", required = false) Integer timestampBegin,
            @RequestParam(value = "timestampEnd", required = false) Integer timestampEnd,
            @RequestParam(value = "sortDirection", required = false) String sortDirection,
            HttpServletRequest request) {
        return analyticsEngineService.getEventActions(source, platform, action, session, timestampBegin, timestampEnd, sortDirection);
    }

    @RequestMapping(value = {"/GetEventSessions/", "/GetEventSessions"}, method = RequestMethod.GET)
    public
    @ResponseBody String GetEventSessions(
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "platform", required = false) String platform,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "session", required = false) String session,
            @RequestParam(value = "timestampBegin", required = false) Integer timestampBegin,
            @RequestParam(value = "timestampEnd", required = false) Integer timestampEnd,
            @RequestParam(value = "sortDirection", required = false) String sortDirection,
            HttpServletRequest request) {
        return analyticsEngineService.getEventSessions(source, platform, action, session, timestampBegin, timestampEnd, sortDirection);
    }

    @RequestMapping(value = {"/GetEventTimestamps/", "/GetEventTimestamps"}, method = RequestMethod.GET)
    public
    @ResponseBody String GetEventTimestamps(
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "platform", required = false) String platform,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "session", required = false) String session,
            @RequestParam(value = "timestampBegin", required = false) Integer timestampBegin,
            @RequestParam(value = "timestampEnd", required = false) Integer timestampEnd,
            @RequestParam(value = "sortDirection", required = false) String sortDirection,
            HttpServletRequest request) {
        return analyticsEngineService.getEventTimestamps(source, platform, action, session, timestampBegin, timestampEnd, sortDirection);
    }

    @RequestMapping(value = {"/GetDistinctCategories/", "/GetDistinctCategories"}, method = RequestMethod.GET)
    public
    @ResponseBody
    LinkedHashMap<String, String> GetDistinctCategories(
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "platform", required = false) String platform,
            @RequestParam(value = "action", required = false) String action,
            HttpServletRequest request) {
        return analyticsEngineService.getDistinctCategories(source, platform, action);
    }



    @RequestMapping(value = {"/GetAnalyticsMethodInputs/", "/GetAnalyticsMethodInputs"}, method = RequestMethod.GET)
    public
    @ResponseBody
    List<OpenLAPColumnConfigData> GetAnalyticsMethodInputs(@RequestParam long id, @RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
        return analyticsEngineService.getAnalyticsMethodInputs(id, request);
    }

    @RequestMapping(value = {"/GetAnalyticsMethodOutputs/", "/GetAnalyticsMethodOutputs"}, method = RequestMethod.GET)
    public
    @ResponseBody
    List<OpenLAPColumnConfigData> GetAnalyticsMethodOutputs(@RequestParam long id, @RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
        return analyticsEngineService.getAnalyticsMethodOutputs(id, request);
    }

    @RequestMapping(value = {"/GetAnalyticsMethodParams/", "/GetAnalyticsMethodParams"}, method = RequestMethod.GET)
    public
    @ResponseBody
    List<OpenLAPDynamicParam> GetAnalyticsMethodParams(@RequestParam long id, @RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
        return analyticsEngineService.getAnalyticsMethodDynamicParams(id, request);
    }

    @RequestMapping(value = {"/GetVisualizationMethodInputs/", "/GetVisualizationMethodInputs"}, method = RequestMethod.GET)
    public
    @ResponseBody
    List<OpenLAPColumnConfigData> GetVisualizationMethodInputs(@RequestParam long frameworkId, @RequestParam long methodId, @RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
        return analyticsEngineService.getVisualizationMethodInputs(frameworkId, methodId, request);
    }

    @RequestMapping(value = {"/GetIndicatorRequestCode/", "/GetIndicatorRequestCode"}, method = RequestMethod.GET)
    public
    @ResponseBody
    IndicatorSaveResponse GetIndicatorRequestCode(@RequestParam long indicatorId, HttpServletRequest request) {
        return analyticsEngineService.getIndicatorRequestCode(indicatorId, request);
    }

    @RequestMapping(value = {"/GetQuestionRequestCode/", "/GetQuestionRequestCode"}, method = RequestMethod.GET)
    public
    @ResponseBody
    QuestionSaveResponse GetQuestionRequestCode(@RequestParam long questionId, HttpServletRequest request) {
        return analyticsEngineService.getQuestionRequestCode(questionId, request);
    }


    @RequestMapping(value = {"/test/", "/test"}, method = RequestMethod.GET)
    public
    @ResponseBody String test(@RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
        return analyticsEngineService.testing(request);
    }

    //region Exception Handling
    @ExceptionHandler(ItemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody GenericResponseDTO handleItemNotFoundException(ItemNotFoundException e, HttpServletRequest request) {
        GenericResponseDTO errorObject = new GenericResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                e.getErrorCode(),
                e.getMessage(),
                request.getServletPath()
        );

        return errorObject;
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody GenericResponseDTO handleBadRequestException(BadRequestException e, HttpServletRequest request) {
        GenericResponseDTO errorObject = new GenericResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                e.getErrorCode(),
                e.getMessage(),
                request.getServletPath()
        );

        return errorObject;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody GenericResponseDTO handleException(Exception e, HttpServletRequest request) {
        GenericResponseDTO errorObject = new GenericResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "0",
                e.getMessage(),
                request.getServletPath()
        );

        return errorObject;
    }
    //endregion
}

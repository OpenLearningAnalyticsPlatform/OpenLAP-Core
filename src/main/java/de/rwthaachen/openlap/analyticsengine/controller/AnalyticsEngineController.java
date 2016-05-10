package de.rwthaachen.openlap.analyticsengine.controller;

import de.rwthaachen.openlap.analyticsengine.core.dtos.request.IndicatorPreviewRequest;
import de.rwthaachen.openlap.analyticsengine.core.dtos.response.IndicatorPreviewResponse;
import de.rwthaachen.openlap.analyticsengine.exceptions.IndicatorNotFoundException;
import de.rwthaachen.openlap.analyticsengine.service.AnalyticsEngineService;
import de.rwthaachen.openlap.analyticsmethods.model.AnalyticsMethodMetadata;
import de.rwthaachen.openlap.analyticsmodules.model.AnalyticsGoal;
import de.rwthaachen.openlap.common.controller.GenericResponseDTO;
import de.rwthaachen.openlap.visualizer.core.dtos.response.VisualizationFrameworksDetailsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


/**
 * Created by Arham Muslim
 * on 30-Dec-15.
 */
@Controller
public class AnalyticsEngineController {

    @Autowired
    AnalyticsEngineService analyticsEngineService;

    @RequestMapping(value = {"/AnalyticsEngine/GetIndicatorData/", "/AnalyticsEngine/GetIndicatorData"}, method = RequestMethod.GET)
    public
    @ResponseBody
    String GetIndicatorData(
            @RequestParam(value = "tid", required = true) String triadID,
            @RequestParam(value = "cid", required = true) String courseID,
            @RequestParam Map<String, String> allRequestParams,
            HttpServletRequest request) {

        /*@RequestParam(value = "mid", required = false) String moduleID,
        @RequestParam(value = "pid", required = false) String userID,
        @RequestParam(value = "width", required = false) int divWidth,
        @RequestParam(value = "height", required = false) int divHeight,
        @RequestParam(value = "start", required = false) String startDate,
        @RequestParam(value = "end", required = false) String endDate,*/

        //long unixTime = System.currentTimeMillis() / 1000L;
        //String rand = triadID + "" + unixTime;

        /*String code = "<script type='text/javascript'>" +
                "var data" + rand + " = google.visualization.arrayToDataTable([ ['Task', 'Hours per Day'], ['Work', 11], ['Eat', 2], ['Commute', 2], ['Watch TV', 2], ['Sleep', 7]]);" +
                "var options" + rand + " = { title: 'Java My Daily Activities', width: " + (divWidth - 10) + ", height: " + (divHeight - 10) + ", is3D: true, backgroundColor: { fill:'transparent' }};" +
                "var chart" + rand + " = new google.visualization.PieChart(document.getElementById('chartdiv" + rand + "'));" +
                "google.visualization.events.addListener(chart" + rand + ", 'ready', function (){$('#chartdiv" + rand + " > div:first-child > div:nth-child(2)').css({ top: '1px', left:'1px'});});" +
                "chart" + rand + ".draw(data" + rand + ", options" + rand + ");" +
                "</script><div id='chartdiv" + rand + "'></div>";*/

        //return encodeURIComponent(code);

        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());

        try {
            //return analyticsEngineService.performGetRequest(baseUrl + "/AnalyticsModules/AnalyticsGoals/");
            return analyticsEngineService.executeIndicator(allRequestParams, baseUrl);
        } catch (Exception exc) {
            return exc.getMessage();
        }
    }

    @RequestMapping(value = {"/AnalyticsEngine/SetupForDemo/", "/AnalyticsEngine/SetupForDemo"}, method = RequestMethod.GET)
    public
    @ResponseBody
    String SetupForDemo(@RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
        return analyticsEngineService.SetupForDemo(request);
    }

    @RequestMapping(value = "/AnalyticsEngine/GetIndicatorPreview", method = RequestMethod.POST)
    public
    @ResponseBody
    IndicatorPreviewResponse GetIndicatorPreview(
            @RequestBody IndicatorPreviewRequest previewRequest,
            @RequestParam Map<String, String> allRequestParams,
            HttpServletRequest request) {

        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());

        return analyticsEngineService.getIndicatorPreview(previewRequest, allRequestParams, baseUrl);
    }


    @RequestMapping(value = "/AnalyticsEngine/test", method = RequestMethod.GET)
    public
    @ResponseBody
    IndicatorPreviewRequest GetIndicatorPreview(
            @RequestParam Map<String, String> allRequestParams,
            HttpServletRequest request) {

        IndicatorPreviewRequest req = new IndicatorPreviewRequest();
        req.setQuery("select");
        req.setAnalyticsMethodId(1);
        req.setVisualizationMethodId(1);
        req.setVisualizationFrameworkId(1);

        return req;
    }


    @RequestMapping(value = {"/AnalyticsEngine/GetGoals/", "/AnalyticsEngine/GetGoals"}, method = RequestMethod.GET)
    public
    @ResponseBody
    List<AnalyticsGoal> GetAllGoals(@RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
        return analyticsEngineService.getAllGoals(request);
    }

    @RequestMapping(value = {"/AnalyticsEngine/GetAnalyticsMethods/", "/AnalyticsEngine/GetAnalyticsMethods"}, method = RequestMethod.GET)
    public
    @ResponseBody
    List<AnalyticsMethodMetadata> GetAllMethods(@RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
        return analyticsEngineService.getAllAnalyticsMethods(request);
    }

    @RequestMapping(value = {"/AnalyticsEngine/GetVisualizations/", "/AnalyticsEngine/GetVisualizations"}, method = RequestMethod.GET)
    public
    @ResponseBody
    VisualizationFrameworksDetailsResponse GetAllVisualizations(@RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
        return analyticsEngineService.getAllVisualizations(request);
    }


    //region Exception Handling
    @ExceptionHandler(IndicatorNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public
    @ResponseBody
    GenericResponseDTO handleMethodNotFoundException(IndicatorNotFoundException e,
                                                     HttpServletRequest request) {
        GenericResponseDTO errorObject = new GenericResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                e.getClass().getName(),
                e.getMessage(),
                request.getServletPath()
        );

        return errorObject;
    }
    //endregion
}

package org.rwthaachen.olap.analyticsmodules.controller;

import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodsBadRequestException;
import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodsUploadErrorException;
import org.rwthaachen.olap.analyticsmethods.model.AnalyticsMethodMetadata;
import org.rwthaachen.olap.analyticsmodules.exceptions.LearningGoalNotFoundException;
import org.rwthaachen.olap.analyticsmodules.exceptions.TriadNotFoundException;
import org.rwthaachen.olap.analyticsmodules.model.LearningGoal;
import org.rwthaachen.olap.analyticsmodules.model.Triad;
import org.rwthaachen.olap.analyticsmodules.service.AnalyticsModulesService;
import org.rwthaachen.olap.common.controller.ErrorHandlerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * A spring Controller that acts as a facade, exposing an API for handling JSON requests to the Analytics Modules
 * macro component of the OpenLAP
 */
@Controller
public class AnalyticsModulesController {

    private static final String LEARNING_GOAL_ACTION_ACTIVATE = "activate";
    private static final String LEARNING_GOAL_ACTION_DEACTIVATE = "deactivate";
    @Autowired
    AnalyticsModulesService modulesService;

    //region Triads

    /**
     * HTTP endpoint handler method that uses the AnalyticsModulesService to save a Triad.
     * @param triad to be saved
     * @return JSON representation of the saved Triad with an ID.
     */
    @RequestMapping(
            value = "/AnalyticsModules/triads/",
            method = RequestMethod.POST
    )
    public @ResponseBody Triad saveTriad(@RequestBody Triad triad)
    {
        return modulesService.saveTriad(triad);
    }

    /**
     * HTTP endpoint handler method that uses the AnalyticsModulesService to get a Triad by its ID.
     * @param id of the requested Triad
     * @return JSON representation of the Triad with the requested ID
     */
    @RequestMapping(
            value = "/AnalyticsModules/triads/{id}",
            method = RequestMethod.GET
    )
    public @ResponseBody Triad getTriadById(@PathVariable String id)
    {
        return modulesService.getTriadById(id);
    }

    /**
     * HTTP endpoint handler method that uses the AnalyticsModulesService to get all Triads
     * @return JSON representation of all the Triads
     */
    @RequestMapping(
            value = "/AnalyticsModules/triads",
            method = RequestMethod.GET
    )
    public @ResponseBody List<Triad> getAllTriads()
    {
        return modulesService.getAllTriads();
    }

    //TODO update triad
    //TODO delete triad
    //endregion

    //region LearningGoals

    /**
     * HTTP endpoint handler method that uses the AnalyticsModulesService to get a LearningGoal by its ID.
     * @param id of the requested LearningGoal
     * @return JSON representation of the LearningGoal with the requested ID
     */
    @RequestMapping(
            value = "/AnalyticsModules/LearningGoals/{id}",
            method = RequestMethod.GET
    )
    public @ResponseBody LearningGoal getLearningGoalById(@PathVariable String id)
    {
        return modulesService.getLearningGoalById(id);
    }

    /**
     * HTTP endpoint handler method that uses the AnalyticsModulesService to save a LearningGoal.
     * @param learningGoal to be saved
     * @return JSON representation of the saved LearningGoal with an ID.
     */
    @RequestMapping(
            value = "/AnalyticsModules/LearningGoals/",
            method = RequestMethod.POST
    )
    public @ResponseBody LearningGoal saveLearningGoal(@RequestBody LearningGoal learningGoal)
    {
        return modulesService.saveLearningGoal(learningGoal);
    }

    /**
     * TODO
     * @param id
     * @param action
     * @return
     */
    @RequestMapping(
            value = "/AnalyticsModules/LearningGoals/{id}/{action}",
            method = RequestMethod.GET
    )
    public @ResponseBody LearningGoal authorizeLearningGoal(@PathVariable String id, @PathVariable String action)
    {
        if (action.equals(LEARNING_GOAL_ACTION_ACTIVATE))
        {
            return modulesService.authorizeLearningGoal(id, true);
        }
        else if (action.equals(LEARNING_GOAL_ACTION_DEACTIVATE))
        {
            return modulesService.authorizeLearningGoal(id, false);
        }
        else throw new AnalyticsMethodsBadRequestException("Invalid request for Learning Goal");
    }

    /**
     * HTTP endpoint handler method that uses the AnalyticsModulesService to get all LearningGoals
     * @return JSON representation of all the LearningGoals
     */
    @RequestMapping(
            value = "/AnalyticsModules/LearningGoals/",
            method = RequestMethod.GET
    )
    public @ResponseBody List<LearningGoal> getAllLearningGoals()
    {
        return modulesService.getAllLearningGoals();
    }

    /**
     * TODO
     * @param learningGoal
     * @param analyticsMethodMetadata
     * @return
     */
    public @ResponseBody LearningGoal addAnalyticsMethodToLearningGoal(LearningGoal learningGoal,
                                                         AnalyticsMethodMetadata analyticsMethodMetadata)
    {
        //return modulesService.addAnalyticsMethodToLearningGoal(learningGoal, analyticsMethodMetadata);
        //TODO
        return null;
    }

    //TODO update learningGoal
    //TODO delete learningGoal
    //endregion

    //region ExceptionHandlers
    @ExceptionHandler({TriadNotFoundException.class,
            AnalyticsMethodsUploadErrorException.class,
            LearningGoalNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody
    ErrorHandlerDTO handleMethodNotFoundException(Exception e,
                                                  HttpServletRequest request)
    {
        ErrorHandlerDTO errorObject = new ErrorHandlerDTO(
                HttpStatus.NOT_FOUND.value(),
                e.getClass().getName(),
                e.getMessage(),
                request.getServletPath()
        );

        return errorObject;
    }
    //endregion
}

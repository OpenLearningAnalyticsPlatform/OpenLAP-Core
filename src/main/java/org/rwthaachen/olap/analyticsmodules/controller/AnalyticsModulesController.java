package org.rwthaachen.olap.analyticsmodules.controller;

import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodsBadRequestException;
import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodsUploadErrorException;
import org.rwthaachen.olap.analyticsmethods.model.AnalyticsMethodMetadata;
import org.rwthaachen.olap.analyticsmodules.exceptions.LearningGoalNotFoundException;
import org.rwthaachen.olap.analyticsmodules.exceptions.TriadNotFoundException;
import org.rwthaachen.olap.analyticsmodules.model.LearningGoal;
import org.rwthaachen.olap.analyticsmodules.model.Triad;
import org.rwthaachen.olap.analyticsmodules.service.AnalyticsModulesService;
import org.rwthaachen.olap.common.controller.GenericResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * A spring Controller that acts as a facade, exposing an API for handling JSON requests to the Analytics Modules
 * macro component of the OpenLAP
 */
@Controller
public class AnalyticsModulesController {

    public static final String LEARNING_GOAL_ACTION_ACTIVATE = "activate";
    public static final String LEARNING_GOAL_ACTION_DEACTIVATE = "deactivate";
    @Autowired
    AnalyticsModulesService modulesService;

    //region Triads

    /**
     * HTTP endpoint handler method to save a Triad.
     * @param triad to be saved
     * @return JSON representation of the saved Triad with an ID.
     */
    @RequestMapping(
            value = "/AnalyticsModules/Triads/",
            method = RequestMethod.POST
    )
    public @ResponseBody Triad saveTriad(@RequestBody Triad triad)
    {
        return modulesService.saveTriad(triad);
    }

    /**
     * HTTP endpoint handler method to get a Triad by its ID.
     * @param id of the requested Triad
     * @return JSON representation of the Triad with the requested ID
     */
    @RequestMapping(
            value = "/AnalyticsModules/Triads/{id}",
            method = RequestMethod.GET
    )
    public @ResponseBody Triad getTriadById(@PathVariable String id)
    {
        return modulesService.getTriadById(id);
    }

    /**
     * HTTP endpoint handler method to get all Triads
     * @return JSON representation of all the Triads
     */
    @RequestMapping(
            value = "/AnalyticsModules/Triads",
            method = RequestMethod.GET
    )
    public @ResponseBody List<Triad> getAllTriads()
    {
        return modulesService.getAllTriads();
    }

    /**
     * HTTP endpoint handler method for updating Triad
     * @param triad Data of the Triad to be updated.
     * @param id of the Triad to be updated
     * @return updated Triad
     */
    @RequestMapping(
            value = "/AnalyticsModules/Triads/{id}",
            method = RequestMethod.PUT
    )
    public @ResponseBody Triad updateLearningGoal(@RequestBody Triad triad,
                                                         @PathVariable String id){
        return modulesService.updateTriad(triad, id);
    }

    /**
     * HTTP endpoint handler method for deleting Triad
     * @param id id of the Triad to be deleted
     * @return GenericResponseDTO with deletion confirmation
     */
    @RequestMapping(
            value = "/AnalyticsModules/Triads/{id}",
            method = RequestMethod.DELETE
    )
    public @ResponseBody GenericResponseDTO deleteTriad(@PathVariable String id){
        modulesService.deleteTriad(id);
        return new GenericResponseDTO(HttpStatus.OK.value(),
                "Learning Goal with id {" + id + "} deleted");
    }

    //endregion

    //region LearningGoals

    /**
     * HTTP endpoint handler method to get a LearningGoal by its ID.
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
     * HTTP endpoint handler method to save a LearningGoal.
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
     * HTTP endpoint handler method for Activating/Deactivating a LearningGoal
     * @param id of the LearningGoal
     * @param action "activate" or "deactivate"
     * @return the updated LearningGoal with the sent status
     */
    @RequestMapping(
            value = "/AnalyticsModules/LearningGoals/{id}/{action}",
            method = RequestMethod.GET
    )
    public @ResponseBody LearningGoal authorizeLearningGoal(@PathVariable String id, @PathVariable String action)
    {
        if (action.equals(LEARNING_GOAL_ACTION_ACTIVATE))
        {
            return modulesService.setLearningGoalActive(id, true);
        }
        else if (action.equals(LEARNING_GOAL_ACTION_DEACTIVATE))
        {
            return modulesService.setLearningGoalActive(id, false);
        }
        else throw new AnalyticsMethodsBadRequestException("Invalid request for Learning Goal");
    }

    /**
     * HTTP endpoint handler method to get all LearningGoals
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
     * HTTP endpoint handler method for attaching an AnalyticsMethod to a LearningGoal
     * @param learningGoalId id of the LearningGoal
     * @param analyticsMethodMetadata of the AnalyticsMethod to be related with the Learninggoal
     * @return the LearningGoal with the attached analyticsMethodMetadata
     */
    @RequestMapping(
            value = "/AnalyticsModules/LearningGoals/{id}/addAnalyticsMethod",
            method = RequestMethod.POST
    )
    public @ResponseBody LearningGoal addAnalyticsMethodToLearningGoal(
            @PathVariable String learningGoalId,
            @RequestBody AnalyticsMethodMetadata analyticsMethodMetadata)
    {
        return modulesService.addAnalyticsMethodToLearningGoal(learningGoalId, analyticsMethodMetadata);
    }

    /**
     * HTTP endpoint handler method for updating LearningGoal
     * @param learningGoal Data of the LearningGoal to be updated. Note that the isActive, id and the AnalyticsMethods
     *                     will not be updated using this method.
     * @param id of the LearningGoal to be updated
     * @return updated LearningGoal
     */
    @RequestMapping(
            value = "/AnalyticsModules/LearningGoals/{id}",
            method = RequestMethod.PUT
    )
    public @ResponseBody LearningGoal updateLearningGoal(@RequestBody LearningGoal learningGoal,
                                                         @PathVariable String id){
        return modulesService.updateLearningGoal(learningGoal, id);
    }

    /**
     * HTTP endpoint handler method for deleting LearningGoal
     * @param id id of the LearningGoal to be deleted
     * @return GenericResponseDTO with deletion confirmation
     */
    @RequestMapping(
            value = "/AnalyticsModules/LearningGoals/{id}",
            method = RequestMethod.DELETE
    )
    public @ResponseBody GenericResponseDTO deleteLearningGoal(@PathVariable String id){
        modulesService.deleteLearningGoal(id);
        return new GenericResponseDTO(HttpStatus.OK.value(),
                "Learning goal with id {" + id + "} deleted");
    }
    //endregion

    //region ExceptionHandlers
    @ExceptionHandler({TriadNotFoundException.class,
            AnalyticsMethodsUploadErrorException.class,
            LearningGoalNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody
    GenericResponseDTO handleMethodNotFoundException(Exception e,
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
    //endregion
}

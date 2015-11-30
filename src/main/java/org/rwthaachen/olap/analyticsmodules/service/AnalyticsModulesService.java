package org.rwthaachen.olap.analyticsmodules.service;

import org.rwthaachen.olap.OpenLAPCoreApplication;
import org.rwthaachen.olap.analyticsmethods.dataAccess.AnalyticsMethodsRepository;
import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodNotFoundException;
import org.rwthaachen.olap.analyticsmethods.model.AnalyticsMethodMetadata;
import org.rwthaachen.olap.analyticsmodules.dataAccess.LearningGoalsRepository;
import org.rwthaachen.olap.analyticsmodules.dataAccess.TriadsRepository;
import org.rwthaachen.olap.analyticsmodules.exceptions.AnalyticsModulesBadRequestException;
import org.rwthaachen.olap.analyticsmodules.exceptions.LearningGoalNotFoundException;
import org.rwthaachen.olap.analyticsmodules.exceptions.TriadNotFoundException;
import org.rwthaachen.olap.analyticsmodules.model.LearningGoal;
import org.rwthaachen.olap.analyticsmodules.model.Triad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * This service handles the "business logic" of the macro component. It also works as a facade for other
 * macro components that happen to be running on the same server, i.e. the Analytics Engine and Analytics Methods
 */
@Service
public class AnalyticsModulesService {

    @Autowired
    TriadsRepository triadsRepository;

    @Autowired
    LearningGoalsRepository learningGoalRepository;

    @Autowired
    AnalyticsMethodsRepository analyticsMethodsRepository;

    private	static	final Logger log =
            LoggerFactory.getLogger(OpenLAPCoreApplication.class);

    //region Triads
    /**
     * Saves a Triad with the given configuration (if any) to the Analytics Modules
     * @param triad with reference to the Indicator, AnalyticsMethod and Visualization as well as the
     *              respective configurations. The AnalyticsMethod must exist.
     * @return the saved Triad
     */
    public Triad saveTriad(Triad triad) throws AnalyticsMethodNotFoundException{
        // Check that the Analytics Method exists
        if (analyticsMethodsRepository.exists(triad.getAnalyticsMethodReference().getId()))
        {
            try {
            return triadsRepository.save(triad);
            }catch (DataIntegrityViolationException sqlException){
                sqlException.printStackTrace();
                throw new AnalyticsModulesBadRequestException("Learning Goal already exists.");
            } catch (Exception e){
                e.printStackTrace();
                throw new AnalyticsModulesBadRequestException(e.getMessage());
            }
        }
        // If the AnalyticsMethod is not found then thrown an exception
        else{
            throw new AnalyticsMethodNotFoundException("Metod with id: {"
                    + triad.getAnalyticsMethodReference()
                    + "} not found.");
        }
    }

    /**
     * Get a Triad by it's ID
     * @param id of the Triad
     * @return the Triad with the specified ID
     */
    public Triad getTriadById(String id) throws TriadNotFoundException {
        Triad result = triadsRepository.findOne(id);
        if (result == null || id == null)
        {
            throw new TriadNotFoundException("Triad with id: {" + id + "} not found");
        }
        else
        {
            log.info("viewAnalyticsMethod returns " + result.toString());
            return result;
        }
    }

    /**
     * Gets all the existing Triads on the system
     * @return returns an ArrayList with all the existing Triads.
     */
    public List<Triad> getAllTriads() {
        ArrayList<Triad> result = new ArrayList<Triad>();
        // (A :: B) denotes A consumer execute B with the iterator given.
        triadsRepository.findAll().forEach(result :: add);
        return result;
    }

    /**
     * Delete the specified Triad
     * @param triadId id of the Triad to be deleted
     */
    public void deleteTriad(String triadId) {
        if(!triadsRepository.exists(triadId)){
            throw new TriadNotFoundException("Triad with id = {"
                    + triadId + "} not found.");
        }
        triadsRepository.delete(triadId);
    }

    /**
     * Update the specified Triad with the specified the data sent
     * @param triad Data of the Triad to be updated.
     * @param id of the Triad to be updated
     * @return updated Triad
     */
    public Triad updateTriad(Triad triad, String id) {
        Triad responseTriad = triadsRepository.findOne(id);
        if(responseTriad == null){
            throw new AnalyticsModulesBadRequestException("Learning Goal with id = {"
                    + id + "} not found.");
        }
        responseTriad.updateWithTriad(triad);
        return triadsRepository.save(responseTriad);
    }

    //endregion

    //region LearningGoals

    /**
     * Gets a LearningGoal by its ID
     * @param id of the LearningGoal
     * @return the LearningGoal with the specified ID
     */
    public LearningGoal getLearningGoalById(String id) {
        LearningGoal result = learningGoalRepository.findOne(id);
        if (result == null || id == null)
        {
            throw new LearningGoalNotFoundException("LearningGoal with id: {" + id + "} not found");
        }
        else
        {
            log.info("viewAnalyticsMethod returns " + result.toString());
            return result;
        }
    }

    /**
     * Creates an inactive LearningGoal with no AnalyticsMethods related to it.
     * @param learningGoal the LearningGoal to be saved
     * @return LearningGoal saved with an ID
     */
    public LearningGoal saveLearningGoal(LearningGoal learningGoal) {
        LearningGoal learningGoalToSave = new LearningGoal(learningGoal.getName(),learningGoal.getDescription(),
                learningGoal.getAuthor(),false);
        try {
            return learningGoalRepository.save(learningGoalToSave);
        }catch (DataIntegrityViolationException sqlException){
            sqlException.printStackTrace();
            throw new AnalyticsModulesBadRequestException("Learning Goal already exists.");
        } catch (Exception e){
            e.printStackTrace();
            throw new AnalyticsModulesBadRequestException(e.getMessage());
        }
    }

    /**
     * Gets all the existing LearningGoals on the system
     * @return returns an ArrayList with all the existing LearningGoals.
     */
    public List<LearningGoal> getAllLearningGoals() {
        ArrayList<LearningGoal> result = new ArrayList<LearningGoal>();
        // (A :: B) denotes A consumer execute B with the iterator given.
        learningGoalRepository.findAll().forEach(result :: add);
        return result;
    }

    /**
     * Switches the active field of the LearningGoal, only active LearningGoals can add new AnalyticsMethods
     * @param id of the LearningGoal to be switched
     * @return the saved LearningGoal with the set active status
     */
    public LearningGoal setLearningGoalActive(String id, boolean status) {
        LearningGoal learningGoal = getLearningGoalById(id);
        learningGoal.setActive(status);
        return updateLearningGoal(learningGoal, id);
    }

    /**
     * Attach an AnalyticsMethodMetadata to a LearningGoal
     * @param learningGoalId the id of the LearningGoal to attach the AnalyticsMethodMetadata to
     * @param analyticsMethodMetadata the AnalyticsMethodMetadata to be attached
     * @return the new LearningGoal with the attached AnalyticsMethodMetadata
     */
    public LearningGoal addAnalyticsMethodToLearningGoal(String learningGoalId,
                                                         AnalyticsMethodMetadata analyticsMethodMetadata) {
        //Check that LearningGoal exists
        //Check that AnalyticsMethod exists
        LearningGoal responseLearningGoal = learningGoalRepository.findOne(learningGoalId);
        AnalyticsMethodMetadata requestedAnalytisMethod =
                analyticsMethodsRepository.findOne(analyticsMethodMetadata.getId());
        if(responseLearningGoal == null){
            throw new AnalyticsModulesBadRequestException("Learning Goal with id = {"
                    + learningGoalId + "} not found.");
        }
        if ( requestedAnalytisMethod == null){
            throw new AnalyticsModulesBadRequestException("Analytics Method with id = {"
                    + analyticsMethodMetadata.getId() + "} not found.");
        }
        if (!responseLearningGoal.isActive()){
            throw new AnalyticsModulesBadRequestException("Learning Goal with id = {"
                    + analyticsMethodMetadata.getId() + "} must be active to attach Analytics Methods to it.");
        }
        //Attach analyticsMethodMetadata if it does not exist in the LearningGoal
        responseLearningGoal.getAnalyticsMethods().add(requestedAnalytisMethod);

        //Return the LearningGoal with the attached
        return learningGoalRepository.save(responseLearningGoal);
    }

    /**
     * Update the specified LearningGoal with the specified the data sent
     * @param learningGoal Data of the LearningGoal to be updated. Note that the isActive, id and the AnalyticsMethods
     *                     will not be updated using this method.
     * @param id of the LearningGoal to be updated
     * @return updated LearningGoal
     */
    public LearningGoal updateLearningGoal(LearningGoal learningGoal, String id) {
        LearningGoal responseLearningGoal = learningGoalRepository.findOne(id);
        if(responseLearningGoal == null){
            throw new AnalyticsModulesBadRequestException("Learning Goal with id = {"
                    + id + "} not found.");
        }
        responseLearningGoal.updateWithLearningGoal(learningGoal);
        return learningGoalRepository.save(responseLearningGoal);
    }

    /**
     * Delete the specified LearningGoal
     * @param learningGoalId id of the LearningGoal to be deleted
     */
    public void deleteLearningGoal(String learningGoalId) {
        if(!learningGoalRepository.exists(learningGoalId)){
            throw new LearningGoalNotFoundException("Learning Goal with id = {"
                    + learningGoalId + "} not found.");
        }
        learningGoalRepository.delete(learningGoalId);
    }

    //endregion
}

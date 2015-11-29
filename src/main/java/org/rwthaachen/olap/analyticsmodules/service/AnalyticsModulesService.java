package org.rwthaachen.olap.analyticsmodules.service;

import org.rwthaachen.olap.OpenLAPCoreApplication;
import org.rwthaachen.olap.analyticsmethods.dataAccess.AnalyticsMethodsRepository;
import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodNotFoundException;
import org.rwthaachen.olap.analyticsmethods.exceptions.AnalyticsMethodsBadRequestException;
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
    public LearningGoal authorizeLearningGoal(String id, boolean status) {
        LearningGoal learningGoal = getLearningGoalById(id);
        learningGoal.setActive(status);
        return saveLearningGoal(learningGoal);
    }

    //endregion
}

package de.rwthaachen.openlap.analyticsmodules.service;

import de.rwthaachen.openlap.OpenLAPCoreApplication;
import de.rwthaachen.openlap.analyticsmethods.dataaccess.AnalyticsMethodsRepository;
import de.rwthaachen.openlap.analyticsmethods.exceptions.AnalyticsMethodNotFoundException;
import de.rwthaachen.openlap.analyticsmethods.model.AnalyticsMethodMetadata;
import de.rwthaachen.openlap.analyticsmodules.dataaccess.AnalyticsGoalRepository;
import de.rwthaachen.openlap.analyticsmodules.dataaccess.TriadsRepository;
import de.rwthaachen.openlap.analyticsmodules.exceptions.AnalyticsGoalNotFoundException;
import de.rwthaachen.openlap.analyticsmodules.exceptions.AnalyticsModulesBadRequestException;
import de.rwthaachen.openlap.analyticsmodules.exceptions.TriadNotFoundException;
import de.rwthaachen.openlap.analyticsmodules.model.AnalyticsGoal;
import de.rwthaachen.openlap.analyticsmodules.model.Triad;
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
    AnalyticsGoalRepository analyticsGoalRepository;

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
                throw new AnalyticsModulesBadRequestException("Analytics Goal already exists.");
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
            throw new AnalyticsModulesBadRequestException("Triad with id = {"
                    + id + "} not found.");
        }
        if(triad.getAnalyticsMethodReference() == null
                || triad.getIndicatorReference() == null
                || triad.getVisualizationReference() == null)
        {
            throw new AnalyticsModulesBadRequestException("Input Triad for update is not a valid Triad");
        }

        responseTriad.updateWithTriad(triad);
        return triadsRepository.save(responseTriad);
    }

    //endregion

    //region AnalyticsGoals

    /**
     * Gets a AnalyticsGoal by its ID
     * @param id of the AnalyticsGoal
     * @return the AnalyticsGoal with the specified ID
     */
    public AnalyticsGoal getAnalyticsGoalById(String id) {
        AnalyticsGoal result = analyticsGoalRepository.findOne(id);
        if (result == null || id == null)
        {
            throw new AnalyticsGoalNotFoundException("AnalyticsGoal with id: {" + id + "} not found");
        }
        else
        {
            log.info("viewAnalyticsMethod returns " + result.toString());
            return result;
        }
    }

    /**
     * Creates an inactive AnalyticsGoal with no AnalyticsMethods related to it.
     * @param analyticsGoal the AnalyticsGoal to be saved
     * @return AnalyticsGoal saved with an ID
     */
    public AnalyticsGoal saveAnalyticsGoal(AnalyticsGoal analyticsGoal) {
        AnalyticsGoal analyticsGoalToSave = new AnalyticsGoal(analyticsGoal.getName(), analyticsGoal.getDescription(),
                analyticsGoal.getAuthor(),false);
        try {
            return analyticsGoalRepository.save(analyticsGoalToSave);
        }catch (DataIntegrityViolationException sqlException){
            sqlException.printStackTrace();
            throw new AnalyticsModulesBadRequestException("Analytics Goal already exists.");
        } catch (Exception e){
            e.printStackTrace();
            throw new AnalyticsModulesBadRequestException(e.getMessage());
        }
    }

    /**
     * Gets all the existing AnalyticsGoals on the system
     * @return returns an ArrayList with all the existing AnalyticsGoals.
     */
    public List<AnalyticsGoal> getAllAnalyticsGoals() {
        ArrayList<AnalyticsGoal> result = new ArrayList<AnalyticsGoal>();
        // (A :: B) denotes A consumer execute B with the iterator given.
        analyticsGoalRepository.findAll().forEach(result :: add);
        return result;
    }

    /**
     * Switches the active field of the AnalyticsGoal, only active AnalyticsGoals can add new AnalyticsMethods
     * @param id of the AnalyticsGoal to be switched
     * @return the saved AnalyticsGoal with the set active status
     */
    public AnalyticsGoal setAnalyticsGoalActive(String id, boolean status) {
        AnalyticsGoal analyticsGoal = getAnalyticsGoalById(id);
        analyticsGoal.setActive(status);
        return updateAnalyticsGoal(analyticsGoal, id);
    }

    /**
     * Attach an AnalyticsMethodMetadata to a AnalyticsGoal
     * @param AnalyticsGoalId the id of the AnalyticsGoal to attach the AnalyticsMethodMetadata to
     * @param analyticsMethodMetadata the AnalyticsMethodMetadata to be attached
     * @return the new AnalyticsGoal with the attached AnalyticsMethodMetadata
     */
    public AnalyticsGoal addAnalyticsMethodToAnalyticsGoal(String AnalyticsGoalId,
                                                          AnalyticsMethodMetadata analyticsMethodMetadata) {
        //Check that AnalyticsGoal exists
        //Check that AnalyticsMethod exists
        AnalyticsGoal responseAnalyticsGoal = analyticsGoalRepository.findOne(AnalyticsGoalId);
        AnalyticsMethodMetadata requestedAnalytisMethod =
                analyticsMethodsRepository.findOne(analyticsMethodMetadata.getId());
        if(responseAnalyticsGoal == null){
            throw new AnalyticsModulesBadRequestException("Analytics Goal with id = {"
                    + AnalyticsGoalId + "} not found.");
        }
        if ( requestedAnalytisMethod == null){
            throw new AnalyticsModulesBadRequestException("Analytics Method with id = {"
                    + analyticsMethodMetadata.getId() + "} not found.");
        }
        if (!responseAnalyticsGoal.isActive()){
            throw new AnalyticsModulesBadRequestException("Analytics Goal with id = {"
                    + analyticsMethodMetadata.getId() + "} must be active to attach Analytics Methods to it.");
        }
        //Attach analyticsMethodMetadata if it does not exist in the AnalyticsGoal
        responseAnalyticsGoal.getAnalyticsMethods().add(requestedAnalytisMethod);

        //Return the AnalyticsGoal with the attached
        return analyticsGoalRepository.save(responseAnalyticsGoal);
    }

    /**
     * Update the specified AnalyticsGoal with the specified the data sent
     * @param analyticsGoal Data of the AnalyticsGoal to be updated. Note that the isActive, id and the AnalyticsMethods
     *                     will not be updated using this method.
     * @param id of the AnalyticsGoal to be updated
     * @return updated AnalyticsGoal
     */
    public AnalyticsGoal updateAnalyticsGoal(AnalyticsGoal analyticsGoal, String id) {
        AnalyticsGoal responseAnalyticsGoal = analyticsGoalRepository.findOne(id);
        if(responseAnalyticsGoal == null){
            throw new AnalyticsGoalNotFoundException("Analytics Goal with id = {"
                    + id + "} not found.");
        }
        responseAnalyticsGoal.updateWithAnalyticsGoal(analyticsGoal);
        return analyticsGoalRepository.save(responseAnalyticsGoal);
    }

    /**
     * Delete the specified AnalyticsGoal
     * @param AnalyticsGoalId id of the AnalyticsGoal to be deleted
     */
    public void deleteAnalyticsGoal(String AnalyticsGoalId) {
        if(!analyticsGoalRepository.exists(AnalyticsGoalId)){
            throw new AnalyticsGoalNotFoundException("Analytics Goal with id = {"
                    + AnalyticsGoalId + "} not found.");
        }
        analyticsGoalRepository.delete(AnalyticsGoalId);
    }

    //endregion
}

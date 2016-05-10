package de.rwthaachen.openlap.analyticsengine.service;

import DataSet.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.AnalyticsMethod;
import core.exceptions.AnalyticsMethodInitializationException;
import de.rwthaachen.openlap.OpenLAPCoreApplication;
import de.rwthaachen.openlap.analyticsengine.core.dtos.request.IndicatorPreviewRequest;
import de.rwthaachen.openlap.analyticsengine.core.dtos.response.IndicatorPreviewResponse;
import de.rwthaachen.openlap.analyticsengine.dataaccess.IndicatorRepository;
import de.rwthaachen.openlap.analyticsengine.datamodel.OpenLAPCategory;
import de.rwthaachen.openlap.analyticsengine.datamodel.OpenLAPEntity;
import de.rwthaachen.openlap.analyticsengine.datamodel.OpenLAPEvent;
import de.rwthaachen.openlap.analyticsengine.datamodel.OpenLAPUsers;
import de.rwthaachen.openlap.analyticsengine.exceptions.IndicatorBadRequestException;
import de.rwthaachen.openlap.analyticsengine.exceptions.IndicatorNotFoundException;
import de.rwthaachen.openlap.analyticsengine.exceptions.OpenLAPDateSetMappingException;
import de.rwthaachen.openlap.analyticsengine.model.Indicator;
import de.rwthaachen.openlap.analyticsmethods.model.AnalyticsMethodMetadata;
import de.rwthaachen.openlap.analyticsmethods.service.AnalyticsMethodsService;
import de.rwthaachen.openlap.analyticsmodules.model.AnalyticsGoal;
import de.rwthaachen.openlap.analyticsmodules.model.IndicatorReference;
import de.rwthaachen.openlap.analyticsmodules.model.Triad;
import de.rwthaachen.openlap.analyticsmodules.model.VisualizerReference;
import de.rwthaachen.openlap.visualizer.core.dtos.request.GenerateVisualizationCodeRequest;
import de.rwthaachen.openlap.visualizer.core.dtos.response.*;
import exceptions.OLAPDataColumnException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

//import de.rwthaachen.openlap.visualizer.core.dtos.response.VisualizationMethodDetailsResponse;
//import de.rwthaachen.openlap.visualizer.core.dtos.response.VisualizationFrameworkDetailsResponse;

//import de.rwthaachen.openlap.visualizer.core.dtos.response.VisualizationMethodDetailsResponse;
//import de.rwthaachen.openlap.visualizer.core.dtos.response.VisualizationFrameworkDetailsResponse;

/**
 * Created by Arham Muslim
 * on 30-Dec-15.
 */
@Service
public class AnalyticsEngineService {

    private static final Logger log = LoggerFactory.getLogger(OpenLAPCoreApplication.class);
    @Autowired
    IndicatorRepository indicatorRepository;
    @Autowired
    AnalyticsMethodsService analyticsMethodsService;
    private SessionFactory factory;
    private String visualizationURL = "http://137.226.231.18:8080";

    public AnalyticsEngineService() {
        URL resourceURL = ClassLoader.getSystemClassLoader().getResource("LADB-Config.xml");
        Configuration configuration = new Configuration();
        configuration.configure(resourceURL);
        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
        factory = configuration.buildSessionFactory(builder.build());
    }

    public String executeIndicator(Map<String, String> params, String baseUrl) {
        //OLAPDataSet rawdata = executeQuery("select eve.eventId from OpenLAPEvent eve where eve.eventId > 40000 and eve.eventId < 44000");
        //OLAPDataSet rawdata = executeHQLQuery("select eve.eventId, cat.cId from OpenLAPEvent eve inner join OpenLAPCategory cat");

                /*SELECT C.Minor as Category,E.Timestamp As Date
        FROM Event E
        INNER JOIN Category C
        ON E.C_ID = C.C_Id
        INNER JOIN (SELECT * FROM Entity WHERE Value = '" + courseID + @"') T
        ON T.Event_fk = E.Event_Id
        WHERE  (C.Minor LIKE 'Discussion Forum' OR  C.Minor LIKE 'Wiki' OR C.Minor LIKE 'Shared Documents') AND E.Timestamp >= " + DateTimeToUnixTimestamp(BeginDate) + @" AND E.Timestamp <= 1s3513513*/


        System.out.println("Start executing the indicator with Triad id: " + params.getOrDefault("tid", ""));

        ObjectMapper mapper = new ObjectMapper();

        Date startDate, endDate;
        long uStartDate = 0, uEndDate = 0;


        try {
            if (params.containsKey("start") && !params.getOrDefault("start", "").equals("")) {
                startDate = new SimpleDateFormat("yyyyMMdd").parse(params.getOrDefault("start", ""));
                uStartDate = startDate.getTime() / 1000L;
            }
            if (params.containsKey("end") && !params.getOrDefault("end", "").equals("")) {
                endDate = new SimpleDateFormat("yyyyMMdd").parse(params.getOrDefault("end", ""));
                uEndDate = endDate.getTime() / 1000L;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


        Triad triad;

        try {
            //return analyticsEngineService.performGetRequest(baseUrl + "/AnalyticsModules/AnalyticsGoals/");
            String triadJSON = performGetRequest(baseUrl + "/AnalyticsModules/Triads/" + params.getOrDefault("tid", ""));
            triad = mapper.readValue(triadJSON, Triad.class);
        } catch (Exception exc) {
            throw new IndicatorNotFoundException("Indicator with triad id '" + params.getOrDefault("tid", "") + "' not found.");
        }


        if (triad == null)
            throw new IndicatorNotFoundException("Indicator with triad id '" + params.getOrDefault("tid", "") + "' not found.");

        try {
            System.out.println("Triad from database : " + mapper.writeValueAsString(triad));
        } catch (Exception exc) {
        }

        Indicator curInd = getIndicatorById(triad.getIndicatorReference().getId());

        if (curInd == null) {
            throw new IndicatorNotFoundException("Indicator with id '" + triad.getIndicatorReference().getId() + "' not found.");
        }

        //Replacing the courseid in the query with the actual coursenumber coming from the request code.
        String courseID = params.getOrDefault("cid", "");
        String curIndQuery = curInd.getQuery();
        curIndQuery = curIndQuery.replace("CourseRoomID", courseID);

        //Adding the time filter for start of data
        if (uStartDate > 0)
            curIndQuery += " AND E.Timestamp >= " + uStartDate;

        //Adding the time filter for end of data
        if (uEndDate > 0)
            curIndQuery += " AND E.Timestamp <= " + uEndDate;


        OLAPDataSet queryDataSet = executeSQLQuery(curIndQuery);

        if (queryDataSet == null) {
            throw new IndicatorNotFoundException("No data found for the indicator with id '" + triad.getIndicatorReference().getId() + "'.");
        }

        try {
            System.out.println("Raw Data as OpenLAP-DataSet");
            System.out.println(mapper.writeValueAsString(queryDataSet));
        } catch (Exception exc) {
        }
        //Validating and applying the analytics method

        OLAPDataSet analyzedDataSet = null;
        try {
/*
            String dataToMethodConfigJSON = triad.getIndicatorToAnalyticsMethodMapping().toString();
            String methodValidJSON = performPutRequest(baseUrl + "/AnalyticsMethods/"+triad.getAnalyticsMethodReference().getId()+"/validateConfiguration", dataToMethodConfigJSON);

            OLAPDataSetConfigurationValidationResult methodValid =  mapper.readValue(methodValidJSON, OLAPDataSetConfigurationValidationResult.class);

            if(methodValid.isValid()) {
*/

            try {
                AnalyticsMethod method = analyticsMethodsService.loadAnalyticsMethodInstance(triad.getAnalyticsMethodReference().getId());
                method.initialize(queryDataSet, triad.getIndicatorToAnalyticsMethodMapping());
                analyzedDataSet = method.execute();
            } catch (Exception ex) {
                throw new IndicatorNotFoundException(ex.getMessage());
            }

/*            }*/
        } catch (AnalyticsMethodInitializationException amexc) {
            throw new IndicatorNotFoundException(amexc.getMessage());
        } catch (Exception exc) {
            throw new IndicatorNotFoundException(exc.getMessage());
        }


        if (analyzedDataSet == null) {
            throw new IndicatorNotFoundException("No data returned from the analytics methods with id '" + triad.getAnalyticsMethodReference().getId() + "'.");
        }

        try {
            System.out.println("Analyzed data using Analytics Method '" + triad.getAnalyticsMethodReference().getName() + "' as OpenLAP-DataSet");
            System.out.println(mapper.writeValueAsString(analyzedDataSet));
        } catch (Exception exc) {
        }

        //visualizing the analyzed data

        String indicatorCode = "";

        try {

            Map<String, Object> additionalParams = new HashMap<String, Object>();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                additionalParams.put(entry.getKey(), entry.getValue());

            }

            GenerateVisualizationCodeRequest visualRequest = new GenerateVisualizationCodeRequest();
            visualRequest.setFrameworkId(triad.getVisualizationReference().getFrameworkId());
            visualRequest.setFrameworkName(triad.getVisualizationReference().getFrameworkName());
            visualRequest.setMethodId(triad.getVisualizationReference().getMethodId());
            visualRequest.setMethodName(triad.getVisualizationReference().getMethodName());
            visualRequest.setDataSet(analyzedDataSet);
            visualRequest.setAdditionalParameters(additionalParams);

            String visualRequestJSON = mapper.writeValueAsString(visualRequest);

            String visualResponseJSON = performJSONPostRequest(visualizationURL + "/generateVisualizationCode", visualRequestJSON);
            GenerateVisualizationCodeResponse visualResponse = mapper.readValue(visualResponseJSON, GenerateVisualizationCodeResponse.class);

            indicatorCode = visualResponse.getVisualizationCode();
        } catch (Exception exc) {
            return exc.getMessage();
        }

        try {
            System.out.println("Indicator Visualization Code send back to the client: ");
            System.out.println(indicatorCode);
        } catch (Exception exc) {
        }

        return encodeURIComponent(indicatorCode);
    }

    public IndicatorPreviewResponse getIndicatorPreview(IndicatorPreviewRequest previewRequest, Map<String, String> params, String baseUrl) {
        IndicatorPreviewResponse response = new IndicatorPreviewResponse();
        try {
            System.out.println("Start generating the indicator preview");

            ObjectMapper mapper = new ObjectMapper();

            String curIndQuery = previewRequest.getQuery();

            curIndQuery = "SELECT T.Title FROM Event E" +
                    " INNER JOIN Category C ON E.C_ID = C.C_Id" +
                    " INNER JOIN " +
                    " (SELECT E1.Event_fk, E1.Value as Title" +
                    " FROM Entity E1" +
                    " INNER JOIN Entity As E2 ON E1.Event_fk = E2.Event_fk" +
                    " WHERE E1.EntityKey = 'Title' and E1.Value Not LIKE ' ' and E1.Value LIKE '%.%' and E2.Value = '15ws-14118') T" +
                    " ON T.Event_fk = E.Event_Id" +
                    " WHERE  (C.Minor LIKE 'Learning Materials') AND E.Action = 'View'";

            OLAPDataSet queryDataSet = executeSQLQuery(curIndQuery);

            if (queryDataSet == null) {
                response.setSuccess(false);
                response.setErrorMessage("No data found for the requested query");
                return response;
            }

            try {
                System.out.println("Raw Data as OpenLAP-DataSet");
                System.out.println(mapper.writeValueAsString(queryDataSet));
            } catch (Exception exc) {
            }


            //Validating and applying the analytics method
            OLAPDataSet analyzedDataSet = null;
            OLAPPortConfiguration queryToMethodConfig = null;
            try {
                AnalyticsMethod method = analyticsMethodsService.loadAnalyticsMethodInstance(String.valueOf(previewRequest.getAnalyticsMethodId()));
                queryToMethodConfig = previewRequest.getQueryToMethodConfig();

                if (queryToMethodConfig == null) {
                    List<OLAPColumnConfigurationData> methodInputPort = method.getInputPorts();
                    queryToMethodConfig = generateDefaultPortConfiguration(queryDataSet.getColumnsConfigurationData(), methodInputPort);

                    String dataToMethodConfigJSON = "{\"mapping\" : [{\"outputPort\" : {\"type\" : \"STRING\",\"id\" : \"Title\",\"required\" : true},\"inputPort\" : {\"type\" : \"STRING\",\"id\" : \"item_name\",\"required\" : true}}]}";
                    queryToMethodConfig = mapper.readValue(dataToMethodConfigJSON, OLAPPortConfiguration.class);
                }

                method.initialize(queryDataSet, queryToMethodConfig);
                analyzedDataSet = method.execute();
            } catch (AnalyticsMethodInitializationException amexc) {
                response.setSuccess(false);
                response.setErrorMessage(amexc.getMessage());
                return response;
            } catch (Exception exc) {
                response.setSuccess(false);
                response.setErrorMessage(exc.getMessage());
                return response;
            }


            if (analyzedDataSet == null) {
                response.setSuccess(false);
                response.setErrorMessage("No analyzed data returned from the analytics method");
                return response;
            }

            try {
                System.out.println("Analyzed data using Analytics Method '" + previewRequest.getAnalyticsMethodId() + "' as OpenLAP-DataSet");
                System.out.println(mapper.writeValueAsString(analyzedDataSet));
            } catch (Exception exc) {
            }


            //Accessing Analytics method
            VisualizationMethodConfigurationResponse visMethodConfigResponse = null;

            try {
                String visMethodConfigResponseJSON = performGetRequest(visualizationURL + "/frameworks/" + previewRequest.getVisualizationFrameworkId() + "/methods/" + previewRequest.getVisualizationMethodId() + "/configuration");
                visMethodConfigResponse = mapper.readValue(visMethodConfigResponseJSON, VisualizationMethodConfigurationResponse.class);
            } catch (Exception exc) {
                //response.setSuccess(false);
                //response.setErrorMessage("No port configuration available for visualization method");
                //return response;
            }


            //visualizing the analyzed data
            String indicatorCode = "";
            OLAPPortConfiguration methodToVisConfig = null;
            try {

                Map<String, Object> additionalParams = new HashMap<String, Object>();

                for (Map.Entry<String, String> entry : params.entrySet()) {
                    additionalParams.put(entry.getKey(), entry.getValue());
                }

                methodToVisConfig = previewRequest.getMethodToVisualizationConfig();

                if (methodToVisConfig == null) {
                    List<OLAPColumnConfigurationData> visMethodInputPort = visMethodConfigResponse.getMethodConfiguration().getInput().getColumnsConfigurationData();
                    methodToVisConfig = generateDefaultPortConfiguration(analyzedDataSet.getColumnsConfigurationData(), visMethodInputPort);

                    String methodToVisualizerConfigJSON = "{\"mapping\" : [{\"outputPort\" : {\"type\" : \"STRING\",\"id\" : \"item_name\",\"required\" : true},\"inputPort\" : {\"type\" : \"STRING\",\"id\" : \"xAxisStrings\",\"required\" : true}},{\"outputPort\" : {\"type\" : \"INTEGER\",\"id\" : \"item_count\",\"required\" : true},\"inputPort\" : {\"type\" : \"INTEGER\",\"id\" : \"yAxisValues\",\"required\" : true}}]}";
                    methodToVisConfig = mapper.readValue(methodToVisualizerConfigJSON, OLAPPortConfiguration.class);
                }

                GenerateVisualizationCodeRequest visualRequest = new GenerateVisualizationCodeRequest();
                visualRequest.setFrameworkId(previewRequest.getVisualizationFrameworkId());
                visualRequest.setMethodId(previewRequest.getVisualizationMethodId());
                visualRequest.setDataSet(analyzedDataSet);
                //visualRequest.setPortConfiguration(methodToVis);
                visualRequest.setAdditionalParameters(additionalParams);

                String visualRequestJSON = mapper.writeValueAsString(visualRequest);

                String visualResponseJSON = performJSONPostRequest(visualizationURL + "/generateVisualizationCode", visualRequestJSON);
                GenerateVisualizationCodeResponse visualResponse = mapper.readValue(visualResponseJSON, GenerateVisualizationCodeResponse.class);

                indicatorCode = visualResponse.getVisualizationCode();
            } catch (Exception exc) {
                response.setSuccess(false);
                response.setErrorMessage(exc.getMessage());
                return response;
            }

            try {
                System.out.println("Indicator Visualization Code send back to the client: ");
                System.out.println(indicatorCode);
            } catch (Exception exc) {
            }


            response.setSuccess(true);
            response.setErrorMessage("");
            response.setQuery(previewRequest.getQuery());
            response.setAnalyticsMethodId(previewRequest.getAnalyticsMethodId());
            response.setVisualizationFrameworkId(previewRequest.getVisualizationFrameworkId());
            response.setVisualizationMethodId(previewRequest.getVisualizationMethodId());
            response.setVisualizationCode(encodeURIComponent(indicatorCode));
            response.setIndicatorToAnalyticsMethodMapping(queryToMethodConfig);
            response.setAnalyticsMethodToVisualizationMapping(methodToVisConfig);

            return response;
        } catch (Exception exc) {
            response.setSuccess(false);
            response.setErrorMessage(exc.getMessage());
            return response;
        }
    }

    private String convertTanmayaQueryToLADBQuery(String tQuery) {
        String lQuery = tQuery.replaceAll("GLAEntity", "Entity");

        return lQuery;
    }

    public String SetupForDemo(HttpServletRequest request) {
        ObjectMapper mapper = new ObjectMapper();

        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());

        /*//Adding new Analytics Goal to the platform
        AnalyticsGoal testAnalyticsGoal = new AnalyticsGoal("Statistics", "Statistical analysis of learning activities data.", "Arham Muslim", true);
        String analyticsGoalAsJsonString = testAnalyticsGoal.toString();

        AnalyticsGoal analyticsGoal;

        try {
            String goalJSON = performJSONPostRequest(baseUrl + "/AnalyticsModules/AnalyticsGoals/", analyticsGoalAsJsonString);

            analyticsGoal = mapper.readValue(goalJSON, AnalyticsGoal.class);
        }
        catch (Exception exc){return  exc.getMessage();}

        //Activating the added Analytics Goal
        try {
            performPutRequest(baseUrl + "/AnalyticsModules/AnalyticsGoals/"+analyticsGoal.getId()+"/activate", "");
        }
        catch (Exception exc){return  exc.getMessage();}
*/

        //Adding new Indicator
        Indicator indicator = null;
        IndicatorReference indicatorReference = null;

        try {
            Indicator ind = new Indicator("Most Viewed Learning Material", "Top10LearningMaterials", "");
            ind.setQuery("SELECT T.Title FROM Event E" +
                    " INNER JOIN Category C ON E.C_ID = C.C_Id" +
                    " INNER JOIN " +
                    " (SELECT E1.Event_fk, E1.Value as Title" +
                    " FROM Entity E1" +
                    " INNER JOIN Entity As E2 ON E1.Event_fk = E2.Event_fk" +
                    " WHERE E1.EntityKey = 'Title' and E1.Value Not LIKE ' ' and E1.Value LIKE '%.%' and E2.Value = 'CourseRoomID') T" +
                    " ON T.Event_fk = E.Event_Id" +
                    " WHERE  (C.Minor LIKE 'Learning Materials') AND E.Action = 'View'");

            indicator = saveIndicator(ind);
            indicatorReference = new IndicatorReference(indicator.getId(), indicator.getShortName(), indicator.getName());
        } catch (Exception exc) {
            return exc.getMessage();
        }


        //Accessing Analytics method
        AnalyticsMethodMetadata methodMetadata;

        try {
            String methodJSON = performGetRequest(baseUrl + "/AnalyticsMethods/1");
            methodMetadata = mapper.readValue(methodJSON, AnalyticsMethodMetadata.class);
        } catch (Exception exc) {
            return exc.getMessage();
        }


        //Accessing Visualization technique
        VisualizerReference visualizerReference;

        try {
            String frameworkJSON = performGetRequest(visualizationURL + "/frameworks/1");
            VisualizationFrameworkDetailsResponse frameworkResponse = mapper.readValue(frameworkJSON, VisualizationFrameworkDetailsResponse.class);

            String methodJSON = performGetRequest(visualizationURL + "/frameworks/1/methods/1");
            VisualizationMethodDetailsResponse methodResponse = mapper.readValue(methodJSON, VisualizationMethodDetailsResponse.class);

            visualizerReference = new VisualizerReference(
                    frameworkResponse.getVisualizationFramework().getId(),
                    methodResponse.getVisualizationMethod().getId(),
                    frameworkResponse.getVisualizationFramework().getName(),
                    methodResponse.getVisualizationMethod().getName());
        } catch (Exception exc) {
            return exc.getMessage();
        }


        //Generating the OpenLAP-PortConfiguration for QueryData and Analytics Method
        OLAPPortConfiguration dataToMethodConfig;
        try {
            String dataToMethodConfigJSON = "{\"mapping\" : [{\"outputPort\" : {\"type\" : \"STRING\",\"id\" : \"Title\",\"required\" : true},\"inputPort\" : {\"type\" : \"STRING\",\"id\" : \"item_name\",\"required\" : true}}]}";
            dataToMethodConfig = mapper.readValue(dataToMethodConfigJSON, OLAPPortConfiguration.class);
        } catch (Exception exc) {
            return exc.getMessage();
        }


        //Generating the OpenLAP-PortConfiguration for Analytics Method and Visualizer
        OLAPPortConfiguration methodToVisualizerConfig;
        try {
            String methodToVisualizerConfigJSON = "{\"mapping\" : [{\"outputPort\" : {\"type\" : \"STRING\",\"id\" : \"item_name\",\"required\" : true},\"inputPort\" : {\"type\" : \"STRING\",\"id\" : \"xAxisStrings\",\"required\" : true}},{\"outputPort\" : {\"type\" : \"INTEGER\",\"id\" : \"item_count\",\"required\" : true},\"inputPort\" : {\"type\" : \"INTEGER\",\"id\" : \"yAxisValues\",\"required\" : true}}]}";
            methodToVisualizerConfig = mapper.readValue(methodToVisualizerConfigJSON, OLAPPortConfiguration.class);
        } catch (Exception exc) {
            return exc.getMessage();
        }


        Triad triad = new Triad(indicatorReference, methodMetadata, visualizerReference, dataToMethodConfig, methodToVisualizerConfig);

        //Saving Triad
        Triad savedTriad = null;
        try {
            String triadJSON = triad.toString();
            String savedTriadJSON = performJSONPostRequest(baseUrl + "/AnalyticsModules/Triads/", triadJSON);
            savedTriad = mapper.readValue(savedTriadJSON, Triad.class);
        } catch (Exception exc) {
            return exc.getMessage();
        }

        return savedTriad.toString();
    }


    public List<AnalyticsGoal> getAllGoals(HttpServletRequest request) {
        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());

        ObjectMapper mapper = new ObjectMapper();

        List<AnalyticsGoal> allGoals;

        try {
            String goalsJSON = performGetRequest(baseUrl + "/AnalyticsModules/AnalyticsGoals/");
            allGoals = mapper.readValue(goalsJSON, mapper.getTypeFactory().constructCollectionType(List.class, AnalyticsGoal.class));
        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            return null;
        }

        return allGoals;
    }

    public List<AnalyticsMethodMetadata> getAllAnalyticsMethods(HttpServletRequest request) {
        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());

        ObjectMapper mapper = new ObjectMapper();

        List<AnalyticsMethodMetadata> allMethods;

        try {
            String methodsJSON = performGetRequest(baseUrl + "/AnalyticsMethods");
            allMethods = mapper.readValue(methodsJSON, mapper.getTypeFactory().constructCollectionType(List.class, AnalyticsMethodMetadata.class));
        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            return null;
        }

        return allMethods;
    }

    public VisualizationFrameworksDetailsResponse getAllVisualizations(HttpServletRequest request) {
        ObjectMapper mapper = new ObjectMapper();

        VisualizationFrameworksDetailsResponse allVis;

        try {
            String visualizationsJSON = performGetRequest(visualizationURL + "/frameworks/list");
            allVis = mapper.readValue(visualizationsJSON, VisualizationFrameworksDetailsResponse.class);
        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            return null;
        }

        return allVis;
    }


    public OLAPPortConfiguration generateDefaultPortConfiguration(List<OLAPColumnConfigurationData> senderColumns, List<OLAPColumnConfigurationData> receiverColumns) throws OpenLAPDateSetMappingException {
        ArrayList<OLAPPortMapping> mapping = new ArrayList<OLAPPortMapping>();

        for (OLAPColumnConfigurationData receiverColumn : receiverColumns) {
            OLAPColumnConfigurationData selectedSenderColumn = null;
            for (OLAPColumnConfigurationData senderColumn : senderColumns) {
                if (receiverColumn.getType() == senderColumn.getType()) {
                    selectedSenderColumn = senderColumn;

                    mapping.add(new OLAPPortMapping(senderColumn, receiverColumn));

                    senderColumns.remove(senderColumn);
                    break;
                }
            }

            if (selectedSenderColumn == null)
                throw new OpenLAPDateSetMappingException("No mapping possible for the '" + receiverColumn.getId() + "' column.");
        }

        OLAPPortConfiguration config = new OLAPPortConfiguration(mapping);
        return config;
    }

    public OLAPPortConfiguration generateDefaultPortConfiguration(OLAPDataSet sender, OLAPDataSet receiver) throws OpenLAPDateSetMappingException {
        List<OLAPColumnConfigurationData> senderColumns = sender.getColumnsConfigurationData(true);
        List<OLAPColumnConfigurationData> receiverColumns = receiver.getColumnsConfigurationData(true);

        return generateDefaultPortConfiguration(senderColumns, receiverColumns);
    }


    //region Indicator repository methods

    public Indicator getIndicatorById(long id) throws IndicatorNotFoundException {
        Indicator result = indicatorRepository.findOne(id);
        if (result == null || id <= 0) {
            throw new IndicatorNotFoundException("Indicator with id: {" + id + "} not found");
        } else {
            log.info("getIndicatorById returns " + result.toString());
            return result;
        }
    }

    public Indicator saveIndicator(Indicator indicator) {
        Indicator indicatorToSave = new Indicator(indicator.getName(), indicator.getShortName(), indicator.getQuery());
        try {
            return indicatorRepository.save(indicatorToSave);
        } catch (DataIntegrityViolationException sqlException) {
            sqlException.printStackTrace();
            throw new IndicatorBadRequestException("Indicator already exists.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new IndicatorBadRequestException(e.getMessage());
        }
    }

    public List<Indicator> getAllIndicators() {
        ArrayList<Indicator> result = new ArrayList<Indicator>();
        indicatorRepository.findAll().forEach(result::add);
        return result;
    }

    public void deleteIndicator(long indicatorId) {
        if (!indicatorRepository.exists(indicatorId)) {
            throw new IndicatorNotFoundException("Indicator with id = {" + indicatorId + "} not found.");
        }
        indicatorRepository.delete(indicatorId);
    }

    //endregion

    //region Query Execution and Data Transformation

    public OLAPDataSet executeHQLQuery(String queryString) {
        Session session = factory.openSession();

        Query query = session.createQuery(queryString);

        List<?> dataList = query.list();

        session.close();

        return transformHQLToOpenLAPDatSet(dataList, queryString);
    }

    public OLAPDataSet transformHQLToOpenLAPDatSet(List<?> dataList, String hqlQuery) {
        //Extracting column names from the query
        String[] fieldsName;
        int indexFrom = hqlQuery.indexOf("from");
        if (indexFrom > 0) {
            String cols = hqlQuery.substring(hqlQuery.indexOf("select") + 7, indexFrom);
            cols = cols.replaceAll("\\s", "");
            fieldsName = cols.split(",", -1);
        } else
            fieldsName = new String[]{};

        OLAPDataSet ds;

        if (dataList.size() > 0) {

            ds = new OLAPDataSet();

            Object dataFirst = dataList.get(0);

            //There are three cases, multiple coulmns in the query, single column in the query, no column

            if (dataFirst instanceof Object[]) // Multiple column case
            {
                Object[] firstRow = (Object[]) dataFirst;

                for (int i = 0; i < firstRow.length; i++) {
                    Class cls = firstRow[i].getClass();
                    String className = cls.getName();

                    try {

                        switch (className) {
                            case "java.lang.Integer":
                                ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(fieldsName[i], OLAPColumnDataType.INTEGER, true));
                                break;
                            case "java.lang.Boolean":
                                ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(fieldsName[i], OLAPColumnDataType.BOOLEAN, true));
                                break;
                            case "java.lang.Byte":
                                ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(fieldsName[i], OLAPColumnDataType.BYTE, true));
                                break;
                            case "java.lang.Character":
                                ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(fieldsName[i], OLAPColumnDataType.CHAR, true));
                                break;
                            case "java.lang.Float":
                                ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(fieldsName[i], OLAPColumnDataType.FLOAT, true));
                                break;
                            case "java.sql.Timestamp":
                                ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(fieldsName[i], OLAPColumnDataType.LOCAL_DATE_TIME, true));
                                break;
                            case "java.lang.Long":
                                ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(fieldsName[i], OLAPColumnDataType.LONG, true));
                                break;
                            case "java.lang.Short":
                                ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(fieldsName[i], OLAPColumnDataType.SHORT, true));
                                break;
                            case "java.lang.String":
                                ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(fieldsName[i], OLAPColumnDataType.STRING, true));
                                break;
                            default:
                                break;
                        }

                    } catch (OLAPDataColumnException e) {
                        e.printStackTrace();
                    }
                }

                for (Object row : dataList) {
                    Object[] col = (Object[]) row;
                    for (int i = 0; i < col.length; i++)
                        ds.getColumns().get(fieldsName[i]).getData().add(col[i]);
                }
            } else if (dataFirst instanceof OpenLAPCategory) {
                try {
                    ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType("cId", OLAPColumnDataType.INTEGER, true));
                    ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType("type", OLAPColumnDataType.STRING, true));
                    ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType("major", OLAPColumnDataType.STRING, true));
                    ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType("minor", OLAPColumnDataType.STRING, true));
                } catch (OLAPDataColumnException e) {
                    e.printStackTrace();
                }

                for (Object row : dataList) {
                    OpenLAPCategory obj = (OpenLAPCategory) row;

                    ds.getColumns().get("cId").getData().add(obj.getcId());
                    ds.getColumns().get("type").getData().add(obj.getType());
                    ds.getColumns().get("major").getData().add(obj.getMajor());
                    ds.getColumns().get("minor").getData().add(obj.getMinor());
                }
            } else if (dataFirst instanceof OpenLAPEntity) {
                try {
                    ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType("eId", OLAPColumnDataType.INTEGER, true));
                    ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType("eventFk", OLAPColumnDataType.INTEGER, true));
                    ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType("entityKey", OLAPColumnDataType.STRING, true));
                    ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType("entityValue", OLAPColumnDataType.STRING, true));
                } catch (OLAPDataColumnException e) {
                    e.printStackTrace();
                }

                for (Object row : dataList) {
                    OpenLAPEntity obj = (OpenLAPEntity) row;

                    ds.getColumns().get("eId").getData().add(obj.geteId());
                    ds.getColumns().get("eventFk").getData().add(obj.getEventFk());
                    ds.getColumns().get("entityKey").getData().add(obj.getEntityKey());
                    ds.getColumns().get("entityValue").getData().add(obj.getValue());
                }
            } else if (dataFirst instanceof OpenLAPEvent) {
                try {
                    ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType("eventId", OLAPColumnDataType.INTEGER, true));
                    ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType("uId", OLAPColumnDataType.INTEGER, true));
                    ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType("cId", OLAPColumnDataType.INTEGER, true));
                    ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType("timestamp", OLAPColumnDataType.INTEGER, true));
                    ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType("session", OLAPColumnDataType.STRING, true));
                    ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType("action", OLAPColumnDataType.STRING, true));
                    ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType("platform", OLAPColumnDataType.STRING, true));
                    ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType("source", OLAPColumnDataType.STRING, true));
                } catch (OLAPDataColumnException e) {
                    e.printStackTrace();
                }

                for (Object row : dataList) {
                    OpenLAPEvent obj = (OpenLAPEvent) row;

                    ds.getColumns().get("eventId").getData().add(obj.getEventId());
                    ds.getColumns().get("uId").getData().add(obj.getuId());
                    ds.getColumns().get("cId").getData().add(obj.getcId());
                    ds.getColumns().get("timestamp").getData().add(obj.getTimestamp());
                    ds.getColumns().get("session").getData().add(obj.getSession());
                    ds.getColumns().get("action").getData().add(obj.getAction());
                    ds.getColumns().get("platform").getData().add(obj.getPlatform());
                    ds.getColumns().get("source").getData().add(obj.getSource());
                }
            } else if (dataFirst instanceof OpenLAPUsers) {
                try {
                    ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType("uId", OLAPColumnDataType.INTEGER, true));
                    ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType("name", OLAPColumnDataType.STRING, true));
                } catch (OLAPDataColumnException e) {
                    e.printStackTrace();
                }

                for (Object row : dataList) {
                    OpenLAPUsers obj = (OpenLAPUsers) row;

                    ds.getColumns().get("uId").getData().add(obj.getuId());
                    ds.getColumns().get("name").getData().add(obj.getName());
                }
            } else //Single column case
            {
                String className = dataFirst.getClass().getName();

                try {
                    switch (className) {
                        case "java.lang.Integer":
                            ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(fieldsName[0], OLAPColumnDataType.INTEGER, true));
                            break;
                        case "java.lang.Boolean":
                            ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(fieldsName[0], OLAPColumnDataType.BOOLEAN, true));
                            break;
                        case "java.lang.Byte":
                            ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(fieldsName[0], OLAPColumnDataType.BYTE, true));
                            break;
                        case "java.lang.Character":
                            ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(fieldsName[0], OLAPColumnDataType.CHAR, true));
                            break;
                        case "java.lang.Float":
                            ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(fieldsName[0], OLAPColumnDataType.FLOAT, true));
                            break;
                        case "java.sql.Timestamp":
                            ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(fieldsName[0], OLAPColumnDataType.LOCAL_DATE_TIME, true));
                            break;
                        case "java.lang.Long":
                            ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(fieldsName[0], OLAPColumnDataType.LONG, true));
                            break;
                        case "java.lang.Short":
                            ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(fieldsName[0], OLAPColumnDataType.SHORT, true));
                            break;
                        case "java.lang.String":
                            ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(fieldsName[0], OLAPColumnDataType.STRING, true));
                            break;
                        default:
                            break;
                    }

                } catch (OLAPDataColumnException e) {
                    e.printStackTrace();
                }

                for (Object row : dataList) {
                    ds.getColumns().get(fieldsName[0]).getData().add(row);
                }
            }
        } else {
            ds = null;
        }

        return ds;
    }


    public OLAPDataSet executeSQLQuery(String queryString) {
        Session session = factory.openSession();

        Query query = session.createSQLQuery(queryString);
        query.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);

        List<Map<String, Object>> dataList = query.list();

        session.close();

        OLAPDataSet ds = transformSQLToOpenLAPDatSet(dataList);
        return ds;
    }

    public OLAPDataSet transformSQLToOpenLAPDatSet(List<Map<String, Object>> dataList) {
        OLAPDataSet ds;

        if (dataList.size() > 0) {

            ds = new OLAPDataSet();

            Map<String, Object> dataFirst = (Map<String, Object>) dataList.get(0);

            for (Map.Entry<String, Object> entry : dataFirst.entrySet()) {
                Class cls = entry.getValue().getClass();
                String className = cls.getName();

                try {

                    switch (className) {
                        case "java.lang.Integer":
                            ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(entry.getKey(), OLAPColumnDataType.INTEGER, true));
                            break;
                        case "java.lang.Boolean":
                            ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(entry.getKey(), OLAPColumnDataType.BOOLEAN, true));
                            break;
                        case "java.lang.Byte":
                            ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(entry.getKey(), OLAPColumnDataType.BYTE, true));
                            break;
                        case "java.lang.Character":
                            ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(entry.getKey(), OLAPColumnDataType.CHAR, true));
                            break;
                        case "java.lang.Float":
                            ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(entry.getKey(), OLAPColumnDataType.FLOAT, true));
                            break;
                        case "java.sql.Timestamp":
                            ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(entry.getKey(), OLAPColumnDataType.LOCAL_DATE_TIME, true));
                            break;
                        case "java.lang.Long":
                            ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(entry.getKey(), OLAPColumnDataType.LONG, true));
                            break;
                        case "java.lang.Short":
                            ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(entry.getKey(), OLAPColumnDataType.SHORT, true));
                            break;
                        case "java.lang.String":
                            ds.addOLAPDataColumn(OLAPDataColumnFactory.createOLAPDataColumnOfType(entry.getKey(), OLAPColumnDataType.STRING, true));
                            break;
                        default:
                            break;
                    }

                } catch (OLAPDataColumnException e) {
                    e.printStackTrace();
                }
            }

            for (Map<String, Object> map : dataList) {
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    ds.getColumns().get(entry.getKey()).getData().add(entry.getValue());
                }
            }
        } else {
            ds = null;
        }

        return ds;
    }

    //endregion

    //region Basic HTTP Requests and encoding

    public String performGetRequest(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } else {
            throw new Exception(con.getResponseMessage());
        }
    }

    public String performJSONPostRequest(String url, String jsonContent) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Content-Length", "" + Integer.toString(jsonContent.getBytes().length));

        con.setUseCaches(false);
        con.setDoInput(true);
        con.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(jsonContent);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } else {
            throw new Exception(con.getResponseMessage());
        }
    }

    public String performPutRequest(String url, String jsonContent) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Content-Length", "" + Integer.toString(jsonContent.getBytes().length));

        con.setUseCaches(false);
        con.setDoInput(true);
        con.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(jsonContent);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } else {
            throw new Exception(con.getResponseMessage());
        }
    }

    public String encodeURIComponent(String component) {
        String result = null;

        try {
            result = URLEncoder.encode(component, "UTF-8")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            result = component;
        }
        return result;
    }
    //endregion
}

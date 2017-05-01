package de.rwthaachen.openlap.analyticsengine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.AnalyticsMethod;
import core.exceptions.AnalyticsMethodInitializationException;
import de.rwthaachen.openlap.OpenLAPCoreApplication;
import de.rwthaachen.openlap.analyticsengine.core.dtos.request.IndicatorPreviewRequest;
import de.rwthaachen.openlap.analyticsengine.core.dtos.request.IndicatorSaveRequest;
import de.rwthaachen.openlap.analyticsengine.core.dtos.request.QuestionSaveRequest;
import de.rwthaachen.openlap.analyticsengine.core.dtos.response.*;
import de.rwthaachen.openlap.analyticsengine.dataaccess.IndicatorRepository;
import de.rwthaachen.openlap.analyticsengine.dataaccess.QuestionRepository;
import de.rwthaachen.openlap.analyticsengine.dataaccess.TriadCacheRepository;
import de.rwthaachen.openlap.analyticsengine.datamodel.OpenLAPCategory;
import de.rwthaachen.openlap.analyticsengine.datamodel.OpenLAPEntity;
import de.rwthaachen.openlap.analyticsengine.datamodel.OpenLAPEvent;
import de.rwthaachen.openlap.analyticsengine.datamodel.OpenLAPUsers;
import de.rwthaachen.openlap.analyticsengine.exceptions.BadRequestException;
import de.rwthaachen.openlap.analyticsengine.exceptions.ItemNotFoundException;
import de.rwthaachen.openlap.analyticsengine.exceptions.OpenLAPDateSetMappingException;
import de.rwthaachen.openlap.analyticsengine.model.Indicator;
import de.rwthaachen.openlap.analyticsengine.model.Question;
import de.rwthaachen.openlap.analyticsengine.model.TriadCache;
import de.rwthaachen.openlap.analyticsmethods.model.AnalyticsMethodMetadata;
import de.rwthaachen.openlap.analyticsmethods.service.AnalyticsMethodsService;
import de.rwthaachen.openlap.analyticsmodules.model.*;
import de.rwthaachen.openlap.dataset.*;
import de.rwthaachen.openlap.exceptions.OpenLAPDataColumnException;
import de.rwthaachen.openlap.visualizer.core.dtos.request.GenerateVisualizationCodeRequest;
import de.rwthaachen.openlap.visualizer.core.dtos.request.ValidateVisualizationMethodConfigurationRequest;
import de.rwthaachen.openlap.visualizer.core.dtos.response.*;
import org.apache.commons.lang3.StringUtils;
import org.crsh.console.jline.internal.Log;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Array;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Arham Muslim
 * on 30-Dec-15.
 */
@Service
public class AnalyticsEngineService {

    //region Declared Variables
    @Autowired
    IndicatorRepository indicatorRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    TriadCacheRepository triadCacheRepository;

    @Autowired
    AnalyticsMethodsService analyticsMethodsService;

    @Value("${indicatorExecutionURL}")
    private String indicatorExecutionURL;

    @Value("${visualizerURL}")
    private String visualizationURL;

    private static final Logger log = LoggerFactory.getLogger(OpenLAPCoreApplication.class);

    private SessionFactory factory;

    //endregion

    //region Indicator Execution
    public AnalyticsEngineService() {
        URL resourceURL = ClassLoader.getSystemClassLoader().getResource("LADB-Config.xml");
        Configuration configuration = new Configuration();
        configuration.configure(resourceURL);
        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
        factory = configuration.buildSessionFactory(builder.build());
    }

    public String executeIndicator(Map<String, String> params, String baseUrl) {
        //System.out.println("Start executing the indicator with Triad id: " + params.getOrDefault("tid", ""));
        log.info("Start executing the indicator with Triad id: " + params.getOrDefault("tid", ""));

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
            String triadJSON = performGetRequest(baseUrl + "/AnalyticsModules/Triads/" + params.getOrDefault("tid", ""));
            triad = mapper.readValue(triadJSON, Triad.class);
        } catch (Exception exc) {
            throw new ItemNotFoundException("Indicator with triad id '" + params.getOrDefault("tid", "") + "' not found.", "1");
        }


        if (triad == null)
            throw new ItemNotFoundException("Indicator with triad id '" + params.getOrDefault("tid", "") + "' not found.","1");

        //try {
        //    System.out.println("Triad from database : " + mapper.writeValueAsString(triad));
        //} catch (Exception exc) {}

        OpenLAPDataSet analyzedDataSet = null;

        //Indicator curInd = getIndicatorById(triad.getIndicatorReference().getId());
        Indicator curInd = getIndicatorById(triad.getIndicatorReference().getIndicators().get("0").getId());

        if (curInd == null) {
            throw new ItemNotFoundException("Indicator with id '" + triad.getIndicatorReference().getIndicators().get("0").getId() + "' not found.", "1");
        }

        //if(triad.getIndicatorReference().getIndicators().size()==1) {
        if(!curInd.isComposite()) {

            //Replacing the courseid in the query with the actual coursenumber coming from the request code.
            String courseID = params.getOrDefault("cid", "");
            String curIndQuery = curInd.getQuery().getQueries().get("0");
            curIndQuery = curIndQuery.replace("CourseRoomID", courseID);

            //Adding the time filter for start of data
            String uStartDateStr = "";
            if (uStartDate > 0)
                uStartDateStr = "AND E.Timestamp >= " + uStartDate;
            curIndQuery = curIndQuery.replace("uStartDate", uStartDateStr);

            //Adding the time filter for end of data
            String uEndDateStr = "";
            if (uEndDate > 0)
                uEndDateStr = "AND E.Timestamp <= " + uEndDate;
            curIndQuery = curIndQuery.replace("uEndDate", uEndDateStr);

            OpenLAPDataSet queryDataSet = executeSQLQuery(curIndQuery);

            if (queryDataSet == null) {
                throw new ItemNotFoundException("No data found for the indicator with id '" + triad.getIndicatorReference().getIndicators().get("0").getId() + "'.", "1");
            }

            //try {
            //    System.out.println("Raw Data as OpenLAP-DataSet");
            //    System.out.println(mapper.writeValueAsString(queryDataSet));
            //} catch (Exception exc) { }

            //Validating and applying the analytics method


            try {
/*
            // Skipping this validate configuration step while executing the indicator as the configuration is already validated in the indicator generation step

            String dataToMethodConfigJSON = triad.getIndicatorToAnalyticsMethodMapping().toString();
            String methodValidJSON = performPutRequest(baseUrl + "/AnalyticsMethods/"+triad.getAnalyticsMethodReference().getId()+"/validateConfiguration", dataToMethodConfigJSON);

            OpenLAPDataSetConfigValidationResult methodValid =  mapper.readValue(methodValidJSON, OpenLAPDataSetConfigValidationResult.class);

            if(methodValid.isValid()) {
*/

                try {
                    AnalyticsMethod method = analyticsMethodsService.loadAnalyticsMethodInstance(triad.getAnalyticsMethodReference().getAnalyticsMethods().get("0").getId());
                    method.initialize(queryDataSet, triad.getIndicatorToAnalyticsMethodMapping().getPortConfigs().get("0"));
                    analyzedDataSet = method.execute();
                } catch (Exception ex) {
                    throw new ItemNotFoundException(ex.getMessage(), "1");
                }
            } catch (AnalyticsMethodInitializationException amexc) {
                throw new ItemNotFoundException(amexc.getMessage(), "1");
            } catch (Exception exc) {
                throw new ItemNotFoundException(exc.getMessage(), "1");
            }


            if (analyzedDataSet == null) {
                throw new ItemNotFoundException("No data returned from the analytics methods with id '" + triad.getAnalyticsMethodReference().getAnalyticsMethods().get("0").getId() + "'.", "1");
            }

        }
        else {}

        //try {
        //    System.out.println("Analyzed data using Analytics Method '" + triad.getAnalyticsMethodReference().getName() + "' as OpenLAP-DataSet");
        //    System.out.println(mapper.writeValueAsString(analyzedDataSet));
        //} catch (Exception exc) { }

        //Visualizing the analyzed data
        String indicatorCode = "";
        try {

            Map<String, Object> additionalParams = new HashMap<String, Object>();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                additionalParams.put(entry.getKey(), entry.getValue());
            }

            GenerateVisualizationCodeRequest visualRequest = new GenerateVisualizationCodeRequest();
            visualRequest.setFrameworkId(triad.getVisualizationReference().getFrameworkId());
            //visualRequest.setFrameworkName(triad.getVisualizationReference().getFrameworkName());
            visualRequest.setMethodId(triad.getVisualizationReference().getMethodId());
            //visualRequest.setMethodName(triad.getVisualizationReference().getMethodName());
            visualRequest.setDataSet(analyzedDataSet);
            visualRequest.setPortConfiguration(triad.getAnalyticsMethodToVisualizationMapping());
            visualRequest.setParams(additionalParams);

            String visualRequestJSON = mapper.writeValueAsString(visualRequest);

//            String visualResponseJSON = performJSONPostRequest(visualizationURL + "/generateVisualizationCode", visualRequestJSON);
//            GenerateVisualizationCodeResponse visualResponse = mapper.readValue(visualResponseJSON, GenerateVisualizationCodeResponse.class);

            GenerateVisualizationCodeResponse visualResponse = performJSONPostRequest(visualizationURL + "/generateVisualizationCode", visualRequestJSON, GenerateVisualizationCodeResponse.class);

            indicatorCode = visualResponse.getVisualizationCode();
        } catch (Exception exc) {
            throw new ItemNotFoundException(exc.getMessage(), "1");
        }

        //try {
        //    System.out.println("Indicator Visualization Code send back to the client: ");
        //    System.out.println(indicatorCode);
        //} catch (Exception exc) {}

        return encodeURIComponent(indicatorCode);
    }

    public String executeIndicatorHQL(Map<String, String> params, String baseUrl) {
        log.info("Start executing the indicator with Triad id: " + params.getOrDefault("tid", ""));

        Long triadId = Long.parseLong(params.get("tid"));

        String divWidth;

        if(params.containsKey("width"))
            divWidth = params.get("width");
        else
            divWidth = "500";
        params.put("width", "xxxwidthxxx");


        String divHeight;

        if(params.containsKey("height"))
            divHeight = params.get("height");
        else
            divHeight = "350";
        params.put("height", "xxxheightxxx");


        boolean performCache = false;

        TriadCache triadCache = performCache ? getCacheByTriadId(triadId) : null;

        if(triadCache == null) {

            ObjectMapper mapper = new ObjectMapper();

//            Date startDate, endDate;
//            long uStartDate = 0, uEndDate = 0;
//
//            try {
//                if (params.containsKey("start")) {
//                    startDate = new SimpleDateFormat("yyyyMMdd").parse(params.get("start"));
//                    uStartDate = startDate.getTime() / 1000L;
//                }
//                if (params.containsKey("end")) {
//                    endDate = new SimpleDateFormat("yyyyMMdd").parse(params.get("end"));
//                    uEndDate = endDate.getTime() / 1000L;
//                }
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }

            Triad triad;

            try {
                String triadJSON = performGetRequest(baseUrl + "/AnalyticsModules/Triads/" + params.getOrDefault("tid", ""));
                triad = mapper.readValue(triadJSON, Triad.class);
            } catch (Exception exc) {
                throw new ItemNotFoundException("Indicator with triad id '" + params.getOrDefault("tid", "") + "' not found.", "1");
            }


            if (triad == null)
                throw new ItemNotFoundException("Indicator with triad id '" + params.getOrDefault("tid", "") + "' not found.", "1");


            OpenLAPDataSet analyzedDataSet = null;

            //if(triad.getIndicatorReference().getIndicators().size()==1) {

            if(triad.getIndicatorReference().getIndicatorType().equals("simple")) {

                Indicator curInd = getIndicatorById(triad.getIndicatorReference().getIndicators().get("0").getId());

                if (curInd == null) {
                    throw new ItemNotFoundException("Indicator with id '" + triad.getIndicatorReference().getIndicators().get("0").getId() + "' not found.", "1");
                }

                //Replacing the courseid in the query with the actual coursenumber coming from the request code.
                String courseID = params.getOrDefault("cid", "");
                String curIndQuery = curInd.getQuery().getQueries().get("0");
                curIndQuery = curIndQuery.replace("CourseRoomID", courseID);

                //Adding the time filter for start of data
//                String uStartDateStr = "";
//                if (uStartDate > 0)
//                    uStartDateStr = "AND E.Timestamp >= " + uStartDate;
//                curIndQuery = curIndQuery.replace("uStartDate", uStartDateStr);

                //Adding the time filter for end of data
//                String uEndDateStr = "";
//                if (uEndDate > 0)
//                    uEndDateStr = "AND E.Timestamp <= " + uEndDate;
//                curIndQuery = curIndQuery.replace("uEndDate", uEndDateStr);

                //OpenLAPDataSet queryDataSet = executeSQLQuery(curIndQuery);

                OpenLAPDataSet queryDataSet = executeIndicatorQuery(curIndQuery, triad.getIndicatorToAnalyticsMethodMapping().getPortConfigs().get("0"), 0);

                if (queryDataSet == null) {
                    throw new ItemNotFoundException("No data found for the indicator with id '" + triad.getIndicatorReference().getIndicators().get("0").getId() + "'.", "1");
                }


                //Applying the analytics method
                try {
                    try {
                        AnalyticsMethod method = analyticsMethodsService.loadAnalyticsMethodInstance(triad.getAnalyticsMethodReference().getAnalyticsMethods().get("0").getId());
                        Map<String, String> methodParams = triad.getAnalyticsMethodReference().getAnalyticsMethods().get("0").getAdditionalParams();

                        method.initialize(queryDataSet, triad.getIndicatorToAnalyticsMethodMapping().getPortConfigs().get("0"), methodParams);
                        analyzedDataSet = method.execute();
                    } catch (Exception ex) {
                        throw new ItemNotFoundException(ex.getMessage(), "1");
                    }
                } catch (AnalyticsMethodInitializationException amexc) {
                    throw new ItemNotFoundException(amexc.getMessage(), "1");
                } catch (Exception exc) {
                    throw new ItemNotFoundException(exc.getMessage(), "1");
                }


                if (analyzedDataSet == null) {
                    throw new ItemNotFoundException("No data returned from the analytics methods with id '" + triad.getAnalyticsMethodReference().getAnalyticsMethods().get("0").getId() + "'.", "1");
                }
            } else if(triad.getIndicatorReference().getIndicatorType().equals("composite")){

                Indicator curInd = getIndicatorById(triad.getIndicatorReference().getIndicators().get("0").getId());

                if (curInd == null) {
                    throw new ItemNotFoundException("Indicator with id '" + triad.getIndicatorReference().getIndicators().get("0").getId() + "' not found.", "1");
                }

                //Replacing the courseid in the query with the actual coursenumber coming from the request code.
                String courseID = params.getOrDefault("cid", "");

                Set<String> indicatorNames = curInd.getQuery().getQueries().keySet();
                OpenLAPPortConfig methodToVisConfig = triad.getAnalyticsMethodToVisualizationMapping();

                boolean addIndicatorNameColumn = false;
                String columnId = null;
                for(OpenLAPColumnConfigData outputConfig : methodToVisConfig.getOutputColumnConfigurationData()){
                    if(outputConfig.getId().equals("indicator_names"))
                        addIndicatorNameColumn = true;
                    else
                        columnId = outputConfig.getId();
                }

                for(String indicatorName: indicatorNames){

                    String curIndQuery = curInd.getQuery().getQueries().get(indicatorName);
                    OpenLAPPortConfig queryToMethodConfig = triad.getIndicatorToAnalyticsMethodMapping().getPortConfigs().get(indicatorName);

                    curIndQuery = curIndQuery.replace("CourseRoomID", courseID);

                    OpenLAPDataSet queryDataSet = executeIndicatorQuery(curIndQuery, queryToMethodConfig, 0);

                    if (queryDataSet == null) {
                        throw new ItemNotFoundException("No data found for the indicator with id '" + triad.getIndicatorReference().getIndicators().get("0").getId() + "'.", "1");
                    }

//                try {
//                    log.info("Query data: " + mapper.writeValueAsString(queryDataSet));
//                } catch (Exception exc) {}

                    //Applying the analytics method
                    OpenLAPDataSet singleAnalyzedDataSet = null;
                    try {
                        AnalyticsMethod method = analyticsMethodsService.loadAnalyticsMethodInstance(triad.getAnalyticsMethodReference().getAnalyticsMethods().get(indicatorName).getId());
                        Map<String, String> methodParams = triad.getAnalyticsMethodReference().getAnalyticsMethods().get(indicatorName).getAdditionalParams();

                        method.initialize(queryDataSet, queryToMethodConfig, methodParams);
                        singleAnalyzedDataSet = method.execute();
                    } catch (AnalyticsMethodInitializationException amexc) {
                        throw new ItemNotFoundException(amexc.getMessage(), "1");
                    } catch (Exception exc) {
                        throw new ItemNotFoundException(exc.getMessage(), "1");
                    }


                    if (singleAnalyzedDataSet == null) {
                        throw new ItemNotFoundException("No data returned from the analytics methods with id '" + triad.getAnalyticsMethodReference().getAnalyticsMethods().get("0").getId() + "'.", "1");
                    }

//                    try {
//                        log.info("Analyzed data: " + mapper.writeValueAsString(singleAnalyzedDataSet));
//                    } catch (Exception exc) {}

                    //Merging analyzed dataset
                    if(analyzedDataSet == null) {
                        analyzedDataSet = singleAnalyzedDataSet;

                        if(addIndicatorNameColumn)
                            if(!analyzedDataSet.getColumns().containsKey("indicator_names"))
                                try {
                                    analyzedDataSet.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("indicator_names", OpenLAPColumnDataType.STRING, true, "Indicator Names", "Names of the indicators combines together to form the composite."));
                                } catch (OpenLAPDataColumnException e) { e.printStackTrace(); }
                    }
                    else {
                        List<OpenLAPColumnConfigData> columnConfigDatas = singleAnalyzedDataSet.getColumnsConfigurationData();

                        for(OpenLAPColumnConfigData columnConfigData : columnConfigDatas)
                            analyzedDataSet.getColumns().get(columnConfigData.getId()).getData().addAll(singleAnalyzedDataSet.getColumns().get(columnConfigData.getId()).getData());
                    }

                    if(addIndicatorNameColumn) {
                        int dataSize = singleAnalyzedDataSet.getColumns().get(columnId).getData().size();

                        for(int i=0;i<dataSize;i++)
                            analyzedDataSet.getColumns().get("indicator_names").getData().add(indicatorName);
                    }
                }

//                try {
//                    log.info("Combined Analyzed data: " + mapper.writeValueAsString(analyzedDataSet));
//                } catch (Exception exc) {}

                } else if(triad.getIndicatorReference().getIndicatorType().equals("multianalysis")){

                Indicator curInd = getIndicatorById(triad.getIndicatorReference().getIndicators().get("0").getId());

                if (curInd == null) {
                    throw new ItemNotFoundException("Indicator with id '" + triad.getIndicatorReference().getIndicators().get("0").getId() + "' not found.", "1");
                }

                //Replacing the courseid in the query with the actual coursenumber coming from the request code.
                String courseID = params.getOrDefault("cid", "");

                Set<String> indicatorIds = curInd.getQuery().getQueries().keySet();

                Map<String, OpenLAPDataSet> analyzedDatasetMap = new HashMap<>();


                for(String indicatorId: indicatorIds){

                    if(indicatorId.equals("0")) // skipping 0 since it is the id for the 2nd level analytics method and it does not have a query
                        continue;

                    String curIndQuery = curInd.getQuery().getQueries().get(indicatorId);
                    OpenLAPPortConfig queryToMethodConfig = triad.getIndicatorToAnalyticsMethodMapping().getPortConfigs().get(indicatorId);

                    curIndQuery = curIndQuery.replace("CourseRoomID", courseID);

                    OpenLAPDataSet queryDataSet = executeIndicatorQuery(curIndQuery, queryToMethodConfig, 0);

                    if (queryDataSet == null) {
                        throw new ItemNotFoundException("No data found for the indicator with id '" + triad.getIndicatorReference().getIndicators().get("0").getId() + "'.", "1");
                    }

//                try {
//                    log.info("Query data: " + mapper.writeValueAsString(queryDataSet));
//                } catch (Exception exc) {}

                    //Applying the analytics method
                    OpenLAPDataSet singleAnalyzedDataSet = null;
                    try {
                        AnalyticsMethod method = analyticsMethodsService.loadAnalyticsMethodInstance(triad.getAnalyticsMethodReference().getAnalyticsMethods().get(indicatorId).getId());
                        Map<String, String> methodParams = triad.getAnalyticsMethodReference().getAnalyticsMethods().get(indicatorId).getAdditionalParams();
                        method.initialize(queryDataSet, queryToMethodConfig, methodParams);
                        singleAnalyzedDataSet = method.execute();
                    } catch (AnalyticsMethodInitializationException amexc) {
                        throw new ItemNotFoundException(amexc.getMessage(), "1");
                    } catch (Exception exc) {
                        throw new ItemNotFoundException(exc.getMessage(), "1");
                    }

                    if (singleAnalyzedDataSet == null) {
                        throw new ItemNotFoundException("No data returned from the analytics methods with id '" + triad.getAnalyticsMethodReference().getAnalyticsMethods().get("0").getId() + "'.", "1");
                    }

                    analyzedDatasetMap.put(indicatorId, singleAnalyzedDataSet);

//                    try {
//                        log.info("Analyzed data: " + mapper.writeValueAsString(singleAnalyzedDataSet));
//                    } catch (Exception exc) {}
                }


                //mergning dataset
                List<OpenLAPDataSetMergeMapping> mergeMappings = triad.getIndicatorReference().getDataSetMergeMappingList();
                OpenLAPDataSet mergedDataset = null;
                String mergeStatus = "";

                while(mergeMappings.size()>0){

                    OpenLAPDataSet firstDataset = null;
                    OpenLAPDataSet secondDataset = null;

                    for(OpenLAPDataSetMergeMapping mergeMapping: mergeMappings){

                        //OpenLAPDataSetMergeMapping mergeMapping = mergeMappings.get(0);

                        String key1 = mergeMapping.getIndRefKey1();
                        String key2 = mergeMapping.getIndRefKey2();

                        int dashCountKey1 = StringUtils.countMatches(key1, "-"); //Merged keys will always be in key1
                        //int dashCountKey2 = StringUtils.countMatches(key2, "-");

                        if(dashCountKey1 == 0) {
                            firstDataset = analyzedDatasetMap.get(key1);
                            secondDataset = analyzedDatasetMap.get(key2);
                        } else {
                            if(!mergeStatus.isEmpty() && key1.equals(mergeStatus)){
                                firstDataset = mergedDataset;
                                secondDataset = analyzedDatasetMap.get(key2);
                                key1 = "(" + key1 + ")";
                            } else
                                continue;
                        }


                        OpenLAPDataSet processedDataset = mergeOpenLAPDataSets(firstDataset, secondDataset, mergeMapping);
                        if(processedDataset != null) {
                            mergedDataset = processedDataset;
                            mergeStatus = key1 + "-" + key2;
                            mergeMappings.remove(mergeMapping);
                        }

                        break;
                    }

                }

                try {
                    log.info("Merged Analyzed data: " + mapper.writeValueAsString(mergedDataset));
                } catch (Exception exc) {}


                //Applying the final analysis whose configuration is stored always with id "0"
                OpenLAPPortConfig finalQueryToMethodConfig = triad.getIndicatorToAnalyticsMethodMapping().getPortConfigs().get("0");
                try {
                    AnalyticsMethod method = analyticsMethodsService.loadAnalyticsMethodInstance(triad.getAnalyticsMethodReference().getAnalyticsMethods().get("0").getId());
                    Map<String, String> methodParams = triad.getAnalyticsMethodReference().getAnalyticsMethods().get("0").getAdditionalParams();
                    method.initialize(mergedDataset, finalQueryToMethodConfig, methodParams);
                    analyzedDataSet = method.execute();
                } catch (AnalyticsMethodInitializationException amexc) {
                    throw new ItemNotFoundException(amexc.getMessage(), "1");
                } catch (Exception exc) {
                    throw new ItemNotFoundException(exc.getMessage(), "1");
                }

                if (analyzedDataSet == null) {
                    throw new ItemNotFoundException("No data returned from the analytics methods with id '" + triad.getAnalyticsMethodReference().getAnalyticsMethods().get("0").getId() + "'.", "1");
                }
            }

            //Visualizing the analyzed data
            String indicatorCode = "";
            try {

                Map<String, Object> additionalParams = new HashMap<String, Object>();

                for (Map.Entry<String, String> entry : params.entrySet()) {
                    additionalParams.put(entry.getKey(), entry.getValue());
                }

                GenerateVisualizationCodeRequest visualRequest = new GenerateVisualizationCodeRequest();
                visualRequest.setFrameworkId(triad.getVisualizationReference().getFrameworkId());
                //visualRequest.setFrameworkName(triad.getVisualizationReference().getFrameworkName());
                visualRequest.setMethodId(triad.getVisualizationReference().getMethodId());
                //visualRequest.setMethodName(triad.getVisualizationReference().getMethodName());
                visualRequest.setDataSet(analyzedDataSet);
                visualRequest.setPortConfiguration(triad.getAnalyticsMethodToVisualizationMapping());
                visualRequest.setParams(additionalParams);

                String visualRequestJSON = mapper.writeValueAsString(visualRequest);

//                String visualResponseJSON = performJSONPostRequest(visualizationURL + "/generateVisualizationCode", visualRequestJSON);
//                GenerateVisualizationCodeResponse visualResponse = mapper.readValue(visualResponseJSON, GenerateVisualizationCodeResponse.class);

                GenerateVisualizationCodeResponse visualResponse = performJSONPostRequest(visualizationURL + "/generateVisualizationCode", visualRequestJSON, GenerateVisualizationCodeResponse.class);

                indicatorCode = visualResponse.getVisualizationCode();
            } catch (Exception exc) {
                throw new ItemNotFoundException(exc.getMessage(), "1");
            }

            String encodedCode = encodeURIComponent(indicatorCode);

            if(performCache)
                saveTriadCache(triadId, encodedCode);

            encodedCode = encodedCode.replace("xxxwidthxxx", divWidth);
            encodedCode = encodedCode.replace("xxxheightxxx", divHeight);

            log.info("Returning new code for Triad id: " + params.get("tid"));

            return encodedCode;
        }
        else{
            log.info("Returning cached code for Triad id: " + params.get("tid"));

            String encodedCode = triadCache.getCode();

            encodedCode = encodedCode.replace("xxxwidthxxx", divWidth);
            encodedCode = encodedCode.replace("xxxheightxxx", divHeight);

            return encodedCode;
        }
    }

    public IndicatorPreviewResponse getIndicatorPreview(IndicatorPreviewRequest previewRequest, Map<String, String> params, String baseUrl) {
        IndicatorPreviewResponse response = new IndicatorPreviewResponse();
        try {
            ObjectMapper mapper = new ObjectMapper();

            try {
                log.info("Start generating the indicator preview for:" +  mapper.writeValueAsString(previewRequest));
            } catch (Exception exc) { }

            if(previewRequest.getIndicatorType().equals("simple")) {
                String curIndQuery = previewRequest.getQuery().get("0");
                OpenLAPPortConfig queryToMethodConfig = previewRequest.getQueryToMethodConfig().get("0");

                //OpenLAPDataSet queryDataSet = executeSQLQuery(curIndQuery);

                OpenLAPDataSet queryDataSet = executeIndicatorQuery(curIndQuery, queryToMethodConfig, 0);

                if (queryDataSet == null) {
                    response.setSuccess(false);
                    response.setErrorMessage("No data found for the requested query");
                    log.info("No data found for the requested query");
                    return response;
                }

//                try {
//                    log.info("Query data: " + mapper.writeValueAsString(queryDataSet));
//                } catch (Exception exc) {}


                //Validating the analytics method
                try {
                    String methodValidJSON = performPutRequest(baseUrl + "/AnalyticsMethods/" + previewRequest.getAnalyticsMethodId().get("0") + "/validateConfiguration", queryToMethodConfig.toString());

                    OpenLAPDataSetConfigValidationResult methodValid = mapper.readValue(methodValidJSON, OpenLAPDataSetConfigValidationResult.class);

                    if (!methodValid.isValid())
                        throw new Exception("Mapping between the data column and the input to analysis method is not valid.");

                } catch (Exception exc) {
                    response.setSuccess(false);
                    response.setErrorMessage(exc.getMessage());
                    log.info(exc.getMessage());
                    return response;
                }

                //Applying the analytics method
                OpenLAPDataSet analyzedDataSet = null;
                try {
                    AnalyticsMethod method = analyticsMethodsService.loadAnalyticsMethodInstance(previewRequest.getAnalyticsMethodId().get("0"));
                    Map<String, String> methodParams = previewRequest.getMethodInputParams();

                    method.initialize(queryDataSet, queryToMethodConfig, methodParams);
                    analyzedDataSet = method.execute();
                } catch (AnalyticsMethodInitializationException amexc) {
                    response.setSuccess(false);
                    response.setErrorMessage(amexc.getMessage());
                    log.info(amexc.getMessage());
                    return response;
                } catch (Exception exc) {
                    response.setSuccess(false);
                    response.setErrorMessage(exc.getMessage());
                    log.info(exc.getMessage());
                    return response;
                }


                if (analyzedDataSet == null) {
                    response.setSuccess(false);
                    response.setErrorMessage("No analyzed data returned from the analytics method");
                    log.info("No analyzed data returned from the analytics method");
                    return response;
                }

                try {
                    log.info("Analyzed data: " + mapper.writeValueAsString(analyzedDataSet));
                } catch (Exception exc) { }


                //Accessing Analytics method
                VisualizationMethodConfigurationResponse visMethodConfigResponse = null;

                //visualizing the analyzed data
                String indicatorCode = "";
                OpenLAPPortConfig methodToVisConfig = previewRequest.getMethodToVisualizationConfig();


                //Validating the visualization technique
                try {

                    ValidateVisualizationMethodConfigurationRequest visRequest = new ValidateVisualizationMethodConfigurationRequest();
                    visRequest.setConfigurationMapping(methodToVisConfig);

                    String visRequestJSON = mapper.writeValueAsString(visRequest);

//                String visValidResponseJSON = performJSONPostRequest(visualizationURL + "/frameworks/" + previewRequest.getVisualizationFrameworkId() + "/methods/" + previewRequest.getVisualizationMethodId() + "/validateConfiguration",visRequestJSON);
//                ValidateVisualizationMethodConfigurationResponse visValid =  mapper.readValue(visValidResponseJSON, ValidateVisualizationMethodConfigurationResponse.class);

                    ValidateVisualizationMethodConfigurationResponse visValid = performJSONPostRequest(visualizationURL + "/frameworks/" + previewRequest.getVisualizationFrameworkId() + "/methods/" + previewRequest.getVisualizationMethodId() + "/validateConfiguration", visRequestJSON, ValidateVisualizationMethodConfigurationResponse.class);

                    if (!visValid.isConfigurationValid())
                        throw new Exception("Mapping between the output of method and input of visualization is not valid.");

                } catch (Exception exc) {
                    response.setSuccess(false);
                    response.setErrorMessage(exc.getMessage());
                    log.info(exc.getMessage());
                    return response;
                }


                try {
                    GenerateVisualizationCodeRequest visualRequest = new GenerateVisualizationCodeRequest();
                    visualRequest.setFrameworkId(previewRequest.getVisualizationFrameworkId());
                    visualRequest.setMethodId(previewRequest.getVisualizationMethodId());
                    visualRequest.setDataSet(analyzedDataSet);
                    visualRequest.setPortConfiguration(methodToVisConfig);
                    visualRequest.setParams(previewRequest.getAdditionalParams());

                    String visualRequestJSON = mapper.writeValueAsString(visualRequest);

                    //String visualResponseJSON = performJSONPostRequest(visualizationURL + "/generateVisualizationCode", visualRequestJSON);
                    //GenerateVisualizationCodeResponse visualResponse = mapper.readValue(visualResponseJSON, GenerateVisualizationCodeResponse.class);

                    GenerateVisualizationCodeResponse visualResponse = performJSONPostRequest(visualizationURL + "/generateVisualizationCode", visualRequestJSON, GenerateVisualizationCodeResponse.class);

                    indicatorCode = visualResponse.getVisualizationCode();
                } catch (Exception exc) {
                    response.setSuccess(false);
                    response.setErrorMessage(exc.getMessage());
                    log.info(exc.getMessage());
                    return response;
                }

                response.setSuccess(true);
                response.setErrorMessage("");
                //response.setQuery(previewRequest.getQuery());
                //response.setAnalyticsMethodId(previewRequest.getAnalyticsMethodId());
                //response.setVisualizationFrameworkId(previewRequest.getVisualizationFrameworkId());
                //response.setVisualizationMethodId(previewRequest.getVisualizationMethodId());
                response.setVisualizationCode(encodeURIComponent(indicatorCode));
                //response.setIndicatorToAnalyticsMethodMapping(queryToMethodConfig);
                //response.setAnalyticsMethodToVisualizationMapping(methodToVisConfig);
            }
            return response;
        } catch (Exception exc) {
            response.setSuccess(false);
            response.setErrorMessage(exc.getMessage());
            log.info(exc.getMessage());
            return response;
        }
    }

    public IndicatorPreviewResponse getCompIndicatorPreview(IndicatorPreviewRequest previewRequest, Map<String, String> params, String baseUrl) {
        IndicatorPreviewResponse response = new IndicatorPreviewResponse();
        try {
            ObjectMapper mapper = new ObjectMapper();

            try {
                log.info("Start generating the composite indicator preview for:" +  mapper.writeValueAsString(previewRequest));
            } catch (Exception exc) { }

            if(previewRequest.getIndicatorType().equals("composite")) {

                Set<String> indicatorNames = previewRequest.getQuery().keySet();
                OpenLAPPortConfig methodToVisConfig = previewRequest.getMethodToVisualizationConfig();

                boolean addIndicatorNameColumn = false;
                String columnId = null;
                for(OpenLAPColumnConfigData outputConfig : methodToVisConfig.getOutputColumnConfigurationData()){
                    if(outputConfig.getId().equals("indicator_names"))
                        addIndicatorNameColumn = true;
                    else
                        columnId = outputConfig.getId();
                }

                OpenLAPDataSet combinedAnalyzedDataSet = null;

                for(String indicatorName: indicatorNames){

                    String curIndQuery = previewRequest.getQuery().get(indicatorName);
                    OpenLAPPortConfig queryToMethodConfig = previewRequest.getQueryToMethodConfig().get(indicatorName);

                    OpenLAPDataSet queryDataSet = executeIndicatorQuery(curIndQuery, queryToMethodConfig, 0);

                    if (queryDataSet == null) {
                        response.setSuccess(false);
                        response.setErrorMessage("No data found for indicator '" + indicatorName + "'");
                        log.info("No data found for indicator '" + indicatorName + "'");
                        return response;
                    }

//                try {
//                    log.info("Query data: " + mapper.writeValueAsString(queryDataSet));
//                } catch (Exception exc) {}


                    //Validating the analytics method
                    try {
                        String methodValidJSON = performPutRequest(baseUrl + "/AnalyticsMethods/" + previewRequest.getAnalyticsMethodId().get(indicatorName) + "/validateConfiguration", queryToMethodConfig.toString());

                        OpenLAPDataSetConfigValidationResult methodValid = mapper.readValue(methodValidJSON, OpenLAPDataSetConfigValidationResult.class);

                        if (!methodValid.isValid())
                            throw new Exception("Mapping between the data column and the input to analysis method is not valid for indicator '" + indicatorName + "'.");

                    } catch (Exception exc) {
                        response.setSuccess(false);
                        response.setErrorMessage(exc.getMessage());
                        log.info(exc.getMessage());
                        return response;
                    }

                    //Applying the analytics method
                    OpenLAPDataSet analyzedDataSet = null;
                    try {
                        AnalyticsMethod method = analyticsMethodsService.loadAnalyticsMethodInstance(previewRequest.getAnalyticsMethodId().get(indicatorName));
                        Map<String, String> methodParams = previewRequest.getMethodInputParams();

                        method.initialize(queryDataSet, queryToMethodConfig, methodParams);
                        //method.initialize(queryDataSet, queryToMethodConfig);
                        analyzedDataSet = method.execute();
                    } catch (AnalyticsMethodInitializationException amexc) {
                        response.setSuccess(false);
                        response.setErrorMessage(amexc.getMessage());
                        log.info(amexc.getMessage());
                        return response;
                    } catch (Exception exc) {
                        response.setSuccess(false);
                        response.setErrorMessage(exc.getMessage());
                        log.info(exc.getMessage());
                        return response;
                    }


                    if (analyzedDataSet == null) {
                        response.setSuccess(false);
                        response.setErrorMessage("No analyzed data returned from the analytics method");
                        log.info("No analyzed data returned from the analytics method");
                        return response;
                    }

//                    try {
//                        log.info("Analyzed data: " + mapper.writeValueAsString(analyzedDataSet));
//                    } catch (Exception exc) {}

                    //Merging analyzed dataset
                    if(combinedAnalyzedDataSet == null) {
                        combinedAnalyzedDataSet = analyzedDataSet;

                        if(addIndicatorNameColumn)
                            if(!combinedAnalyzedDataSet.getColumns().containsKey("indicator_names"))
                                combinedAnalyzedDataSet.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("indicator_names", OpenLAPColumnDataType.STRING, true, "Indicator Names", "Names of the indicators combines together to form the composite."));
                    }
                    else {
                        List<OpenLAPColumnConfigData> columnConfigDatas = analyzedDataSet.getColumnsConfigurationData();

                        for(OpenLAPColumnConfigData columnConfigData : columnConfigDatas)
                            combinedAnalyzedDataSet.getColumns().get(columnConfigData.getId()).getData().addAll(analyzedDataSet.getColumns().get(columnConfigData.getId()).getData());
                    }

                    if(addIndicatorNameColumn) {
                        int dataSize = analyzedDataSet.getColumns().get(columnId).getData().size();

                        for(int i=0;i<dataSize;i++)
                            combinedAnalyzedDataSet.getColumns().get("indicator_names").getData().add(indicatorName);
                    }
                }

//                try {
//                    log.info("Combined Analyzed data: " + mapper.writeValueAsString(combinedAnalyzedDataSet));
//                } catch (Exception exc) {}



//                try {
//                    log.info("Analyzed data: " + mapper.writeValueAsString(analyzedDataSet));
//                } catch (Exception exc) { }


                //Accessing Analytics method
                //VisualizationMethodConfigurationResponse visMethodConfigResponse = null;

                //visualizing the analyzed data
                String indicatorCode = "";


                //Validating the visualization technique
                try {

                    ValidateVisualizationMethodConfigurationRequest visRequest = new ValidateVisualizationMethodConfigurationRequest();
                    visRequest.setConfigurationMapping(methodToVisConfig);

                    String visRequestJSON = mapper.writeValueAsString(visRequest);

//                String visValidResponseJSON = performJSONPostRequest(visualizationURL + "/frameworks/" + previewRequest.getVisualizationFrameworkId() + "/methods/" + previewRequest.getVisualizationMethodId() + "/validateConfiguration",visRequestJSON);
//                ValidateVisualizationMethodConfigurationResponse visValid =  mapper.readValue(visValidResponseJSON, ValidateVisualizationMethodConfigurationResponse.class);

                    ValidateVisualizationMethodConfigurationResponse visValid = performJSONPostRequest(visualizationURL + "/frameworks/" + previewRequest.getVisualizationFrameworkId() + "/methods/" + previewRequest.getVisualizationMethodId() + "/validateConfiguration", visRequestJSON, ValidateVisualizationMethodConfigurationResponse.class);

                    if (!visValid.isConfigurationValid())
                        throw new Exception("Mapping between the output of method and input of visualization is not valid.");

                } catch (Exception exc) {
                    response.setSuccess(false);
                    response.setErrorMessage(exc.getMessage());
                    log.info(exc.getMessage());
                    return response;
                }


                try {
                    GenerateVisualizationCodeRequest visualRequest = new GenerateVisualizationCodeRequest();
                    visualRequest.setFrameworkId(previewRequest.getVisualizationFrameworkId());
                    visualRequest.setMethodId(previewRequest.getVisualizationMethodId());
                    visualRequest.setDataSet(combinedAnalyzedDataSet);
                    visualRequest.setPortConfiguration(methodToVisConfig);
                    visualRequest.setParams(previewRequest.getAdditionalParams());

                    String visualRequestJSON = mapper.writeValueAsString(visualRequest);

                    //String visualResponseJSON = performJSONPostRequest(visualizationURL + "/generateVisualizationCode", visualRequestJSON);
                    //GenerateVisualizationCodeResponse visualResponse = mapper.readValue(visualResponseJSON, GenerateVisualizationCodeResponse.class);

                    GenerateVisualizationCodeResponse visualResponse = performJSONPostRequest(visualizationURL + "/generateVisualizationCode", visualRequestJSON, GenerateVisualizationCodeResponse.class);

                    indicatorCode = visualResponse.getVisualizationCode();
                } catch (Exception exc) {
                    response.setSuccess(false);
                    response.setErrorMessage(exc.getMessage());
                    log.info(exc.getMessage());
                    return response;
                }

                response.setSuccess(true);
                response.setErrorMessage("");
                response.setVisualizationCode(encodeURIComponent(indicatorCode));
            }
            return response;
        } catch (Exception exc) {
            response.setSuccess(false);
            response.setErrorMessage(exc.getMessage());
            log.info(exc.getMessage());
            return response;
        }
    }

    public IndicatorPreviewResponse getMLAIIndicatorPreview(IndicatorPreviewRequest previewRequest, Map<String, String> params, String baseUrl) {
        IndicatorPreviewResponse response = new IndicatorPreviewResponse();
        try {
            ObjectMapper mapper = new ObjectMapper();

            try {
                log.info("Start generating the composite indicator preview for:" +  mapper.writeValueAsString(previewRequest));
            } catch (Exception exc) { }

            if(previewRequest.getIndicatorType().equals("composite")) {

                Set<String> indicatorNames = previewRequest.getQuery().keySet();
                OpenLAPPortConfig methodToVisConfig = previewRequest.getMethodToVisualizationConfig();

                boolean addIndicatorNameColumn = false;
                String columnId = null;
                for(OpenLAPColumnConfigData outputConfig : methodToVisConfig.getOutputColumnConfigurationData()){
                    if(outputConfig.getId().equals("indicator_names"))
                        addIndicatorNameColumn = true;
                    else
                        columnId = outputConfig.getId();
                }

                OpenLAPDataSet combinedAnalyzedDataSet = null;

                for(String indicatorName: indicatorNames){

                    String curIndQuery = previewRequest.getQuery().get(indicatorName);
                    OpenLAPPortConfig queryToMethodConfig = previewRequest.getQueryToMethodConfig().get(indicatorName);

                    OpenLAPDataSet queryDataSet = executeIndicatorQuery(curIndQuery, queryToMethodConfig, 0);

                    if (queryDataSet == null) {
                        response.setSuccess(false);
                        response.setErrorMessage("No data found for indicator '" + indicatorName + "'");
                        log.info("No data found for indicator '" + indicatorName + "'");
                        return response;
                    }

//                try {
//                    log.info("Query data: " + mapper.writeValueAsString(queryDataSet));
//                } catch (Exception exc) {}


                    //Validating the analytics method
                    try {
                        String methodValidJSON = performPutRequest(baseUrl + "/AnalyticsMethods/" + previewRequest.getAnalyticsMethodId().get(indicatorName) + "/validateConfiguration", queryToMethodConfig.toString());

                        OpenLAPDataSetConfigValidationResult methodValid = mapper.readValue(methodValidJSON, OpenLAPDataSetConfigValidationResult.class);

                        if (!methodValid.isValid())
                            throw new Exception("Mapping between the data column and the input to analysis method is not valid for indicator '" + indicatorName + "'.");

                    } catch (Exception exc) {
                        response.setSuccess(false);
                        response.setErrorMessage(exc.getMessage());
                        log.info(exc.getMessage());
                        return response;
                    }

                    //Applying the analytics method
                    OpenLAPDataSet analyzedDataSet = null;
                    try {
                        AnalyticsMethod method = analyticsMethodsService.loadAnalyticsMethodInstance(previewRequest.getAnalyticsMethodId().get(indicatorName));
                        Map<String, String> methodParams = previewRequest.getMethodInputParams();

                        method.initialize(queryDataSet, queryToMethodConfig, methodParams);
                        //method.initialize(queryDataSet, queryToMethodConfig);
                        analyzedDataSet = method.execute();
                    } catch (AnalyticsMethodInitializationException amexc) {
                        response.setSuccess(false);
                        response.setErrorMessage(amexc.getMessage());
                        log.info(amexc.getMessage());
                        return response;
                    } catch (Exception exc) {
                        response.setSuccess(false);
                        response.setErrorMessage(exc.getMessage());
                        log.info(exc.getMessage());
                        return response;
                    }


                    if (analyzedDataSet == null) {
                        response.setSuccess(false);
                        response.setErrorMessage("No analyzed data returned from the analytics method");
                        log.info("No analyzed data returned from the analytics method");
                        return response;
                    }

//                    try {
//                        log.info("Analyzed data: " + mapper.writeValueAsString(analyzedDataSet));
//                    } catch (Exception exc) {}

                    //Merging analyzed dataset
                    if(combinedAnalyzedDataSet == null) {
                        combinedAnalyzedDataSet = analyzedDataSet;

                        if(addIndicatorNameColumn)
                            if(!combinedAnalyzedDataSet.getColumns().containsKey("indicator_names"))
                                combinedAnalyzedDataSet.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("indicator_names", OpenLAPColumnDataType.STRING, true, "Indicator Names", "Names of the indicators combines together to form the composite."));
                    }
                    else {
                        List<OpenLAPColumnConfigData> columnConfigDatas = analyzedDataSet.getColumnsConfigurationData();

                        for(OpenLAPColumnConfigData columnConfigData : columnConfigDatas)
                            combinedAnalyzedDataSet.getColumns().get(columnConfigData.getId()).getData().addAll(analyzedDataSet.getColumns().get(columnConfigData.getId()).getData());
                    }

                    if(addIndicatorNameColumn) {
                        int dataSize = analyzedDataSet.getColumns().get(columnId).getData().size();

                        for(int i=0;i<dataSize;i++)
                            combinedAnalyzedDataSet.getColumns().get("indicator_names").getData().add(indicatorName);
                    }
                }

//                try {
//                    log.info("Combined Analyzed data: " + mapper.writeValueAsString(combinedAnalyzedDataSet));
//                } catch (Exception exc) {}



//                try {
//                    log.info("Analyzed data: " + mapper.writeValueAsString(analyzedDataSet));
//                } catch (Exception exc) { }


                //Accessing Analytics method
                //VisualizationMethodConfigurationResponse visMethodConfigResponse = null;

                //visualizing the analyzed data
                String indicatorCode = "";


                //Validating the visualization technique
                try {

                    ValidateVisualizationMethodConfigurationRequest visRequest = new ValidateVisualizationMethodConfigurationRequest();
                    visRequest.setConfigurationMapping(methodToVisConfig);

                    String visRequestJSON = mapper.writeValueAsString(visRequest);

//                String visValidResponseJSON = performJSONPostRequest(visualizationURL + "/frameworks/" + previewRequest.getVisualizationFrameworkId() + "/methods/" + previewRequest.getVisualizationMethodId() + "/validateConfiguration",visRequestJSON);
//                ValidateVisualizationMethodConfigurationResponse visValid =  mapper.readValue(visValidResponseJSON, ValidateVisualizationMethodConfigurationResponse.class);

                    ValidateVisualizationMethodConfigurationResponse visValid = performJSONPostRequest(visualizationURL + "/frameworks/" + previewRequest.getVisualizationFrameworkId() + "/methods/" + previewRequest.getVisualizationMethodId() + "/validateConfiguration", visRequestJSON, ValidateVisualizationMethodConfigurationResponse.class);

                    if (!visValid.isConfigurationValid())
                        throw new Exception("Mapping between the output of method and input of visualization is not valid.");

                } catch (Exception exc) {
                    response.setSuccess(false);
                    response.setErrorMessage(exc.getMessage());
                    log.info(exc.getMessage());
                    return response;
                }


                try {
                    GenerateVisualizationCodeRequest visualRequest = new GenerateVisualizationCodeRequest();
                    visualRequest.setFrameworkId(previewRequest.getVisualizationFrameworkId());
                    visualRequest.setMethodId(previewRequest.getVisualizationMethodId());
                    visualRequest.setDataSet(combinedAnalyzedDataSet);
                    visualRequest.setPortConfiguration(methodToVisConfig);
                    visualRequest.setParams(previewRequest.getAdditionalParams());

                    String visualRequestJSON = mapper.writeValueAsString(visualRequest);

                    //String visualResponseJSON = performJSONPostRequest(visualizationURL + "/generateVisualizationCode", visualRequestJSON);
                    //GenerateVisualizationCodeResponse visualResponse = mapper.readValue(visualResponseJSON, GenerateVisualizationCodeResponse.class);

                    GenerateVisualizationCodeResponse visualResponse = performJSONPostRequest(visualizationURL + "/generateVisualizationCode", visualRequestJSON, GenerateVisualizationCodeResponse.class);

                    indicatorCode = visualResponse.getVisualizationCode();
                } catch (Exception exc) {
                    response.setSuccess(false);
                    response.setErrorMessage(exc.getMessage());
                    log.info(exc.getMessage());
                    return response;
                }

                response.setSuccess(true);
                response.setErrorMessage("");
                response.setVisualizationCode(encodeURIComponent(indicatorCode));
            }
            return response;
        } catch (Exception exc) {
            response.setSuccess(false);
            response.setErrorMessage(exc.getMessage());
            log.info(exc.getMessage());
            return response;
        }
    }

    public OpenLAPDataSet mergeOpenLAPDataSets(OpenLAPDataSet firstDataset, OpenLAPDataSet secondDataSet, OpenLAPDataSetMergeMapping mapping){

        int firstDataSize, secondDataSize;

        int intDefaultValue = -1;
        String strDefaultValue = "-";
        char charDefaultValue = '-';

        List<OpenLAPDataColumn> firstColumns = firstDataset.getColumnsAsList(false);
        HashMap<String, ArrayList> firstData = new HashMap<>();
        firstDataSize = firstColumns.get(0).getData().size();

        for(OpenLAPDataColumn column : firstColumns)
            firstData.put(column.getConfigurationData().getId(), column.getData());

        List<OpenLAPDataColumn> firstColumnsBase = firstDataset.getColumnsAsList(false);

        List<OpenLAPDataColumn> secondColumns = secondDataSet.getColumnsAsList(false);
        HashMap<String, ArrayList> secondData = new HashMap<>();
        secondDataSize = secondColumns.get(0).getData().size();

        for(OpenLAPDataColumn column : secondColumns) {
            secondData.put(column.getConfigurationData().getId(), column.getData());

            //Adding the second dataset columns with default values to first dataset
            if(!mapping.getIndRefField2().equals(column.getConfigurationData().getId())){

                OpenLAPDataColumn newColumn;
                ArrayList newData;
                switch (column.getConfigurationData().getType())
                {
                    case BYTE:
                        newColumn = new OpenLAPDataColumn<Byte>(column.getConfigurationData().getId(), OpenLAPColumnDataType.BYTE, column.getConfigurationData().isRequired(), column.getConfigurationData().getTitle(), column.getConfigurationData().getDescription());
                        newData = new ArrayList<Byte>(Collections.nCopies(firstDataSize, (byte)intDefaultValue));
                        break;
                    case SHORT:
                        newColumn = new OpenLAPDataColumn<Short>(column.getConfigurationData().getId(), OpenLAPColumnDataType.SHORT, column.getConfigurationData().isRequired(), column.getConfigurationData().getTitle(), column.getConfigurationData().getDescription());
                        newData = new ArrayList<Short>(Collections.nCopies(firstDataSize, (short)intDefaultValue));
                        break;
                    case STRING:
                        newColumn = new OpenLAPDataColumn<String>(column.getConfigurationData().getId(), OpenLAPColumnDataType.STRING, column.getConfigurationData().isRequired(), column.getConfigurationData().getTitle(), column.getConfigurationData().getDescription());
                        newData = new ArrayList<String>(Collections.nCopies(firstDataSize, strDefaultValue));
                        break;
                    case INTEGER:
                        newColumn = new OpenLAPDataColumn<Integer>(column.getConfigurationData().getId(), OpenLAPColumnDataType.INTEGER, column.getConfigurationData().isRequired(), column.getConfigurationData().getTitle(), column.getConfigurationData().getDescription());
                        newData = new ArrayList<Integer>(Collections.nCopies(firstDataSize, intDefaultValue));
                        break;
                    case BOOLEAN:
                        newColumn = new OpenLAPDataColumn<Boolean>(column.getConfigurationData().getId(), OpenLAPColumnDataType.BOOLEAN, column.getConfigurationData().isRequired(), column.getConfigurationData().getTitle(), column.getConfigurationData().getDescription());
                        newData = new ArrayList<Boolean>(Collections.nCopies(firstDataSize, false));
                        break;
                    case LONG:
                        newColumn = new OpenLAPDataColumn<Long>(column.getConfigurationData().getId(), OpenLAPColumnDataType.LONG, column.getConfigurationData().isRequired(), column.getConfigurationData().getTitle(), column.getConfigurationData().getDescription());
                        newData = new ArrayList<Long>(Collections.nCopies(firstDataSize, (long)intDefaultValue));
                        break;
                    case FLOAT:
                        newColumn = new OpenLAPDataColumn<Float>(column.getConfigurationData().getId(), OpenLAPColumnDataType.FLOAT, column.getConfigurationData().isRequired(), column.getConfigurationData().getTitle(), column.getConfigurationData().getDescription());
                        newData = new ArrayList<Float>(Collections.nCopies(firstDataSize, (float)intDefaultValue));
                        break;
                    case LOCAL_DATE_TIME:
                        newColumn = new OpenLAPDataColumn<LocalDateTime>(column.getConfigurationData().getId(), OpenLAPColumnDataType.LOCAL_DATE_TIME, column.getConfigurationData().isRequired(), column.getConfigurationData().getTitle(), column.getConfigurationData().getDescription());
                        newData = new ArrayList<LocalDateTime>(Collections.nCopies(firstDataSize, LocalDateTime.MIN));
                        break;
                    case CHAR:
                        newColumn = new OpenLAPDataColumn<Character>(column.getConfigurationData().getId(), OpenLAPColumnDataType.CHAR, column.getConfigurationData().isRequired(), column.getConfigurationData().getTitle(), column.getConfigurationData().getDescription());
                        newData = new ArrayList<Character>(Collections.nCopies(firstDataSize, charDefaultValue));
                        break;
                    default:
                        newColumn = new OpenLAPDataColumn<String>(column.getConfigurationData().getId(), OpenLAPColumnDataType.STRING, column.getConfigurationData().isRequired(), column.getConfigurationData().getTitle(), column.getConfigurationData().getDescription());
                        newData = new ArrayList<String>(Collections.nCopies(firstDataSize, strDefaultValue));
                        break;
                }
                //newColumn.setData(newData);

                firstData.put(newColumn.getConfigurationData().getId(), newData);
                firstColumns.add(newColumn);
            }
        }


        //merging data from second dataset into first dataset

        for(int i=0;i<secondDataSize;i++){
            Object commonFieldValue = secondData.get(mapping.getIndRefField2()).get(i);

            boolean valFound = false;
            for(int j=0;j<firstDataSize;j++) {
                if(firstData.get(mapping.getIndRefField1()).get(j).equals(commonFieldValue)){

                    for(String key : secondData.keySet()) {
                        if(!key.equals(mapping.getIndRefField2()))
                            firstData.get(key).set(j, secondData.get(key).get(i));
                    }

                    valFound = true;
                    break;
                }
            }

            if(!valFound){
                for(String key : secondData.keySet()) {
                    if(!key.equals(mapping.getIndRefField2()))
                        firstData.get(key).add(secondData.get(key).get(i));
                }

                //Adding the default values to the first dataset data array
                for(OpenLAPDataColumn column : firstColumnsBase){
                    if(column.getConfigurationData().getId().equals(mapping.getIndRefField1()))
                        firstData.get(column.getConfigurationData().getId()).add(commonFieldValue);
                    else{
                        switch (column.getConfigurationData().getType())
                        {
                            case BYTE:
                                firstData.get(column.getConfigurationData().getId()).add((byte)intDefaultValue);
                                break;
                            case SHORT:
                                firstData.get(column.getConfigurationData().getId()).add((short)intDefaultValue);
                                break;
                            case STRING:
                                firstData.get(column.getConfigurationData().getId()).add(strDefaultValue);
                                break;
                            case INTEGER:
                                firstData.get(column.getConfigurationData().getId()).add(intDefaultValue);
                                break;
                            case BOOLEAN:
                                firstData.get(column.getConfigurationData().getId()).add(false);
                                break;
                            case LONG:
                                firstData.get(column.getConfigurationData().getId()).add((long)intDefaultValue);
                                break;
                            case FLOAT:
                                firstData.get(column.getConfigurationData().getId()).add((float)intDefaultValue);
                                break;
                            case LOCAL_DATE_TIME:
                                firstData.get(column.getConfigurationData().getId()).add(LocalDateTime.MIN);
                                break;
                            case CHAR:
                                firstData.get(column.getConfigurationData().getId()).add(charDefaultValue);
                                break;
                            default:
                                firstData.get(column.getConfigurationData().getId()).add(strDefaultValue);
                                break;
                        }
                    }
                }
            }
        }

        OpenLAPDataSet mergedDataset = new OpenLAPDataSet();
        //Generating merged dataset using the columns and data
        for(OpenLAPDataColumn column : firstColumns){
            column.setData(firstData.get(column.getConfigurationData().getId()));
            try {
                mergedDataset.addOpenLAPDataColumn(column);
            } catch (OpenLAPDataColumnException e) {
                e.printStackTrace();
            }
        }

        return mergedDataset;
    }

    //endregion

    //region commented code

    /*public String executeIndicator(Map<String, String> params, String baseUrl) {
        //System.out.println("Start executing the indicator with Triad id: " + params.getOrDefault("tid", ""));
        log.info("Start executing the indicator with Triad id: " + params.getOrDefault("tid", ""));

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
            String triadJSON = performGetRequest(baseUrl + "/AnalyticsModules/Triads/" + params.getOrDefault("tid", ""));
            triad = mapper.readValue(triadJSON, Triad.class);
        } catch (Exception exc) {
            throw new ItemNotFoundException("Indicator with triad id '" + params.getOrDefault("tid", "") + "' not found.", "1");
        }


        if (triad == null)
            throw new ItemNotFoundException("Indicator with triad id '" + params.getOrDefault("tid", "") + "' not found.","1");

        //try {
        //    System.out.println("Triad from database : " + mapper.writeValueAsString(triad));
        //} catch (Exception exc) {}

        Indicator curInd = getIndicatorById(triad.getIndicatorReference().getId());

        if (curInd == null) {
            throw new ItemNotFoundException("Indicator with id '" + triad.getIndicatorReference().getId() + "' not found.","1");
        }

        //Replacing the courseid in the query with the actual coursenumber coming from the request code.
        String courseID = params.getOrDefault("cid", "");
        String curIndQuery = curInd.getQuery();
        curIndQuery = curIndQuery.replace("CourseRoomID", courseID);

        //Adding the time filter for start of data
        String uStartDateStr = "";
        if (uStartDate > 0)
            uStartDateStr = "AND E.Timestamp >= " + uStartDate;
        curIndQuery = curIndQuery.replace("uStartDate", uStartDateStr);

        //Adding the time filter for end of data
        String uEndDateStr = "";
        if (uEndDate > 0)
            uEndDateStr = "AND E.Timestamp <= " + uEndDate;
        curIndQuery = curIndQuery.replace("uEndDate", uEndDateStr);

        OpenLAPDataSet queryDataSet = executeSQLQuery(curIndQuery);

        if (queryDataSet == null) {
            throw new ItemNotFoundException("No data found for the indicator with id '" + triad.getIndicatorReference().getId() + "'.","1");
        }

        //try {
        //    System.out.println("Raw Data as OpenLAP-DataSet");
        //    System.out.println(mapper.writeValueAsString(queryDataSet));
        //} catch (Exception exc) { }

        //Validating and applying the analytics method

        OpenLAPDataSet analyzedDataSet = null;
        try {
*//*
            // Skipping this validate configuration step while executing the indicator as the configuration is already validated in the indicator generation step

            String dataToMethodConfigJSON = triad.getIndicatorToAnalyticsMethodMapping().toString();
            String methodValidJSON = performPutRequest(baseUrl + "/AnalyticsMethods/"+triad.getAnalyticsMethodReference().getId()+"/validateConfiguration", dataToMethodConfigJSON);

            OpenLAPDataSetConfigValidationResult methodValid =  mapper.readValue(methodValidJSON, OpenLAPDataSetConfigValidationResult.class);

            if(methodValid.isValid()) {
*//*

            try {
                AnalyticsMethod method = analyticsMethodsService.loadAnalyticsMethodInstance(triad.getAnalyticsMethodReference().getId());
                method.initialize(queryDataSet, triad.getIndicatorToAnalyticsMethodMapping());
                analyzedDataSet = method.execute();
            } catch (Exception ex) {
                throw new ItemNotFoundException(ex.getMessage(),"1");
            }
        } catch (AnalyticsMethodInitializationException amexc) {
            throw new ItemNotFoundException(amexc.getMessage(),"1");
        } catch (Exception exc) {
            throw new ItemNotFoundException(exc.getMessage(),"1");
        }


        if (analyzedDataSet == null) {
            throw new ItemNotFoundException("No data returned from the analytics methods with id '" + triad.getAnalyticsMethodReference().getId() + "'.","1");
        }

        //try {
        //    System.out.println("Analyzed data using Analytics Method '" + triad.getAnalyticsMethodReference().getName() + "' as OpenLAP-DataSet");
        //    System.out.println(mapper.writeValueAsString(analyzedDataSet));
        //} catch (Exception exc) { }

        //Visualizing the analyzed data
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
            visualRequest.setPortConfiguration(triad.getAnalyticsMethodToVisualizationMapping());
            visualRequest.setAdditionalParameters(additionalParams);

            String visualRequestJSON = mapper.writeValueAsString(visualRequest);

//            String visualResponseJSON = performJSONPostRequest(visualizationURL + "/generateVisualizationCode", visualRequestJSON);
//            GenerateVisualizationCodeResponse visualResponse = mapper.readValue(visualResponseJSON, GenerateVisualizationCodeResponse.class);

            GenerateVisualizationCodeResponse visualResponse = performJSONPostRequest(visualizationURL + "/generateVisualizationCode", visualRequestJSON, GenerateVisualizationCodeResponse.class);

            indicatorCode = visualResponse.getVisualizationCode();
        } catch (Exception exc) {
            throw new ItemNotFoundException(exc.getMessage(),"1");
        }

        //try {
        //    System.out.println("Indicator Visualization Code send back to the client: ");
        //    System.out.println(indicatorCode);
        //} catch (Exception exc) {}

        return encodeURIComponent(indicatorCode);
    }
*/


    /*public String executeIndicatorHQL(Map<String, String> params, String baseUrl) {
        log.info("Start executing the indicator with Triad id: " + params.getOrDefault("tid", ""));

        Long triadId = Long.parseLong(params.get("tid"));


        String divWidth;

        if(params.containsKey("width"))
            divWidth = params.get("width");
        else
            divWidth = "500";
        params.put("width", "xxxwidthxxx");


        String divHeight;

        if(params.containsKey("height"))
            divHeight = params.get("height");
        else
            divHeight = "350";
        params.put("height", "xxxheightxxx");


        boolean performCache = true;

        TriadCache triadCache = performCache ? getCacheByTriadId(triadId) : null;

        if(triadCache == null) {

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
                String triadJSON = performGetRequest(baseUrl + "/AnalyticsModules/Triads/" + params.getOrDefault("tid", ""));
                triad = mapper.readValue(triadJSON, Triad.class);
            } catch (Exception exc) {
                throw new ItemNotFoundException("Indicator with triad id '" + params.getOrDefault("tid", "") + "' not found.", "1");
            }


            if (triad == null)
                throw new ItemNotFoundException("Indicator with triad id '" + params.getOrDefault("tid", "") + "' not found.", "1");


            Indicator curInd = getIndicatorById(triad.getIndicatorReference().getId());

            if (curInd == null) {
                throw new ItemNotFoundException("Indicator with id '" + triad.getIndicatorReference().getId() + "' not found.", "1");
            }

            //Replacing the courseid in the query with the actual coursenumber coming from the request code.
            String courseID = params.getOrDefault("cid", "");
            String curIndQuery = curInd.getQuery();
            curIndQuery = curIndQuery.replace("CourseRoomID", courseID);

            //Adding the time filter for start of data
            String uStartDateStr = "";
            if (uStartDate > 0)
                uStartDateStr = "AND E.Timestamp >= " + uStartDate;
            curIndQuery = curIndQuery.replace("uStartDate", uStartDateStr);

            //Adding the time filter for end of data
            String uEndDateStr = "";
            if (uEndDate > 0)
                uEndDateStr = "AND E.Timestamp <= " + uEndDate;
            curIndQuery = curIndQuery.replace("uEndDate", uEndDateStr);

            //OpenLAPDataSet queryDataSet = executeSQLQuery(curIndQuery);

            OpenLAPDataSet queryDataSet = executeIndicatorQuery(curIndQuery, triad.getIndicatorToAnalyticsMethodMapping(), 0);

            if (queryDataSet == null) {
                throw new ItemNotFoundException("No data found for the indicator with id '" + triad.getIndicatorReference().getId() + "'.", "1");
            }


            //Validating and applying the analytics method
            OpenLAPDataSet analyzedDataSet = null;
            try {
                try {
                    AnalyticsMethod method = analyticsMethodsService.loadAnalyticsMethodInstance(triad.getAnalyticsMethodReference().getId());
                    method.initialize(queryDataSet, triad.getIndicatorToAnalyticsMethodMapping());
                    analyzedDataSet = method.execute();
                } catch (Exception ex) {
                    throw new ItemNotFoundException(ex.getMessage(), "1");
                }
            } catch (AnalyticsMethodInitializationException amexc) {
                throw new ItemNotFoundException(amexc.getMessage(), "1");
            } catch (Exception exc) {
                throw new ItemNotFoundException(exc.getMessage(), "1");
            }


            if (analyzedDataSet == null) {
                throw new ItemNotFoundException("No data returned from the analytics methods with id '" + triad.getAnalyticsMethodReference().getId() + "'.", "1");
            }

            //Visualizing the analyzed data
            String indicatorCode = "";
            try {

                Map<String, Object> additionalParams = new HashMap<String, Object>();

                for (Map.Entry<String, String> entry : params.entrySet()) {
                    additionalParams.put(entry.getKey(), entry.getValue());
                }

                GenerateVisualizationCodeRequest visualRequest = new GenerateVisualizationCodeRequest();
                visualRequest.setFrameworkId(triad.getVisualizationReference().getFrameworkId());
                //visualRequest.setFrameworkName(triad.getVisualizationReference().getFrameworkName());
                visualRequest.setMethodId(triad.getVisualizationReference().getMethodId());
                //visualRequest.setMethodName(triad.getVisualizationReference().getMethodName());
                visualRequest.setDataSet(analyzedDataSet);
                visualRequest.setPortConfiguration(triad.getAnalyticsMethodToVisualizationMapping());
                visualRequest.setAdditionalParameters(additionalParams);

                String visualRequestJSON = mapper.writeValueAsString(visualRequest);

//                String visualResponseJSON = performJSONPostRequest(visualizationURL + "/generateVisualizationCode", visualRequestJSON);
//                GenerateVisualizationCodeResponse visualResponse = mapper.readValue(visualResponseJSON, GenerateVisualizationCodeResponse.class);

                GenerateVisualizationCodeResponse visualResponse = performJSONPostRequest(visualizationURL + "/generateVisualizationCode", visualRequestJSON, GenerateVisualizationCodeResponse.class);

                indicatorCode = visualResponse.getVisualizationCode();
            } catch (Exception exc) {
                throw new ItemNotFoundException(exc.getMessage(), "1");
            }

            String encodedCode = encodeURIComponent(indicatorCode);

            if(performCache)
                saveTriadCache(triadId, encodedCode);

            encodedCode = encodedCode.replace("xxxwidthxxx", divWidth);
            encodedCode = encodedCode.replace("xxxheightxxx", divHeight);

            log.info("Returning new code for Triad id: " + params.get("tid"));

            return encodedCode;
        }
        else{
            log.info("Returning cached code for Triad id: " + params.get("tid"));

            String encodedCode = triadCache.getCode();

            encodedCode = encodedCode.replace("xxxwidthxxx", divWidth);
            encodedCode = encodedCode.replace("xxxheightxxx", divHeight);

            return encodedCode;
        }
    }*/

    /*public IndicatorPreviewResponse getIndicatorPreview(IndicatorPreviewRequest previewRequest, Map<String, String> params, String baseUrl) {
        IndicatorPreviewResponse response = new IndicatorPreviewResponse();
        try {
            ObjectMapper mapper = new ObjectMapper();

            try {
                log.info("Start generating the indicator preview for:" +  mapper.writeValueAsString(previewRequest));
            } catch (Exception exc) { }

            String curIndQuery = previewRequest.getQuery();
            OpenLAPPortConfig queryToMethodConfig = previewRequest.getQueryToMethodConfig();

            //OpenLAPDataSet queryDataSet = executeSQLQuery(curIndQuery);

            OpenLAPDataSet queryDataSet = executeIndicatorQuery(curIndQuery, queryToMethodConfig, 0);

            if (queryDataSet == null) {
                response.setSuccess(false);
                response.setErrorMessage("No data found for the requested query");
                log.info("No data found for the requested query");
                return response;
            }

            try {
                log.info("Query data: " + mapper.writeValueAsString(queryDataSet));
            } catch (Exception exc) { }


            //Validating the analytics method
            try {
                String methodValidJSON = performPutRequest(baseUrl + "/AnalyticsMethods/"+previewRequest.getAnalyticsMethodId()+"/validateConfiguration", queryToMethodConfig.toString());

                OpenLAPDataSetConfigValidationResult methodValid =  mapper.readValue(methodValidJSON, OpenLAPDataSetConfigValidationResult.class);

                if(!methodValid.isValid())
                    throw new Exception("Mapping between the data column and the input to analysis method is not valid.");

            } catch (Exception exc) {
                response.setSuccess(false);
                response.setErrorMessage(exc.getMessage());
                log.info(exc.getMessage());
                return response;
            }

            //Applying the analytics method
            OpenLAPDataSet analyzedDataSet = null;
            try {
                AnalyticsMethod method = analyticsMethodsService.loadAnalyticsMethodInstance(previewRequest.getAnalyticsMethodId());

                method.initialize(queryDataSet, queryToMethodConfig);
                analyzedDataSet = method.execute();
            } catch (AnalyticsMethodInitializationException amexc) {
                response.setSuccess(false);
                response.setErrorMessage(amexc.getMessage());
                log.info(amexc.getMessage());
                return response;
            } catch (Exception exc) {
                response.setSuccess(false);
                response.setErrorMessage(exc.getMessage());
                log.info(exc.getMessage());
                return response;
            }


            if (analyzedDataSet == null) {
                response.setSuccess(false);
                response.setErrorMessage("No analyzed data returned from the analytics method");
                log.info("No analyzed data returned from the analytics method");
                return response;
            }

            try {
                log.info("Analyzed data: " + mapper.writeValueAsString(analyzedDataSet));
            } catch (Exception exc) { }


            //Accessing Analytics method
            VisualizationMethodConfigurationResponse visMethodConfigResponse = null;

            //visualizing the analyzed data
            String indicatorCode = "";
            OpenLAPPortConfig methodToVisConfig = previewRequest.getMethodToVisualizationConfig();


            //Validating the visualization technique
            try {

                ValidateVisualizationMethodConfigurationRequest visRequest = new ValidateVisualizationMethodConfigurationRequest();
                visRequest.setConfigurationMapping(methodToVisConfig);

                String visRequestJSON =  mapper.writeValueAsString(visRequest);

//                String visValidResponseJSON = performJSONPostRequest(visualizationURL + "/frameworks/" + previewRequest.getVisualizationFrameworkId() + "/methods/" + previewRequest.getVisualizationMethodId() + "/validateConfiguration",visRequestJSON);
//                ValidateVisualizationMethodConfigurationResponse visValid =  mapper.readValue(visValidResponseJSON, ValidateVisualizationMethodConfigurationResponse.class);

                ValidateVisualizationMethodConfigurationResponse visValid = performJSONPostRequest(visualizationURL + "/frameworks/" + previewRequest.getVisualizationFrameworkId() + "/methods/" + previewRequest.getVisualizationMethodId() + "/validateConfiguration", visRequestJSON, ValidateVisualizationMethodConfigurationResponse.class);

                if(!visValid.isConfigurationValid())
                    throw new Exception("Mapping between the output of method and input of visualization is not valid.");

            } catch (Exception exc) {
                response.setSuccess(false);
                response.setErrorMessage(exc.getMessage());
                log.info(exc.getMessage());
                return response;
            }


            try {
                GenerateVisualizationCodeRequest visualRequest = new GenerateVisualizationCodeRequest();
                visualRequest.setFrameworkId(previewRequest.getVisualizationFrameworkId());
                visualRequest.setMethodId(previewRequest.getVisualizationMethodId());
                visualRequest.setDataSet(analyzedDataSet);
                visualRequest.setPortConfiguration(methodToVisConfig);
                visualRequest.setAdditionalParameters(previewRequest.getAdditionalParams());

                String visualRequestJSON = mapper.writeValueAsString(visualRequest);

                //String visualResponseJSON = performJSONPostRequest(visualizationURL + "/generateVisualizationCode", visualRequestJSON);
                //GenerateVisualizationCodeResponse visualResponse = mapper.readValue(visualResponseJSON, GenerateVisualizationCodeResponse.class);

                GenerateVisualizationCodeResponse visualResponse = performJSONPostRequest(visualizationURL + "/generateVisualizationCode", visualRequestJSON, GenerateVisualizationCodeResponse.class);

                indicatorCode = visualResponse.getVisualizationCode();
            } catch (Exception exc) {
                response.setSuccess(false);
                response.setErrorMessage(exc.getMessage());
                log.info(exc.getMessage());
                return response;
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
            log.info(exc.getMessage());
            return response;
        }
    }*/

    //endregion

    //region Indicator Engine methods

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

    public List<AnalyticsGoal> getActiveGoals(HttpServletRequest request) {
        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());

        ObjectMapper mapper = new ObjectMapper();

        List<AnalyticsGoal> allGoals;

        try {
            String goalsJSON = performGetRequest(baseUrl + "/AnalyticsModules/ActiveAnalyticsGoals/");
            allGoals = mapper.readValue(goalsJSON, mapper.getTypeFactory().constructCollectionType(List.class, AnalyticsGoal.class));
        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            return null;
        }

        return allGoals;
    }

    public AnalyticsGoal saveGoal(String goalName, String goalDesc, String goalAuthor, HttpServletRequest request) {
        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());

        ObjectMapper mapper = new ObjectMapper();

        try {
            AnalyticsGoal newGoal = new AnalyticsGoal();
            newGoal.setName(goalName);
            newGoal.setDescription(goalDesc);
            newGoal.setAuthor(goalAuthor);

            String saveGoalRequestJSON = mapper.writeValueAsString(newGoal);

//            String saveGoalResponseJSON = performJSONPostRequest(baseUrl + "/AnalyticsModules/AnalyticsGoals/", saveGoalRequestJSON);
//            AnalyticsGoal savedGoal = mapper.readValue(saveGoalResponseJSON, AnalyticsGoal.class);

            AnalyticsGoal savedGoal = performJSONPostRequest(baseUrl + "/AnalyticsModules/AnalyticsGoals/", saveGoalRequestJSON, AnalyticsGoal.class);

            return savedGoal;
        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            return null;
        }
    }

    public AnalyticsGoal setGoalStatus(long goalId, boolean isActive, HttpServletRequest request) {
        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());

        ObjectMapper mapper = new ObjectMapper();

        String setStatus;

        if(isActive)
            setStatus = "activate";
        else
            setStatus = "deactivate";

        try {

            String saveGoalResponseJSON = performPutRequest(baseUrl + "/AnalyticsModules/AnalyticsGoals/"+goalId+"/"+setStatus, null);
            AnalyticsGoal returnedGoal = mapper.readValue(saveGoalResponseJSON, AnalyticsGoal.class);

            return returnedGoal;
        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            return null;
        }
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

    public VisualizationFrameworkDetailsResponse getVisualizationsMethods(long frameworkId, HttpServletRequest request) {
        ObjectMapper mapper = new ObjectMapper();

        VisualizationFrameworkDetailsResponse visMethods;

        try {
            String visualizationsJSON = performGetRequest(visualizationURL + "/frameworks/" + frameworkId);
            visMethods = mapper.readValue(visualizationsJSON, VisualizationFrameworkDetailsResponse.class);
        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            return null;
        }

        return visMethods;
    }

    public LinkedHashMap<String, String> getDistinctCategories(String source, String platform, String action){
        LinkedHashMap<String, String> categories = new LinkedHashMap<String, String>();

        try {
            ObjectMapper mapper = new ObjectMapper();

            String whereQuery = "";

            if (!isStringEmpty(source)) {
                String[] sources = source.split(",");

                if(sources.length > 1)
                    whereQuery = " where source in ('" + StringUtils.join(sources,"','") + "')";
                else
                    whereQuery = " where source = '" + source + "'";
            }

            if (!isStringEmpty(platform)) {
                String combiningClause = isStringEmpty(whereQuery) ? " where" : " and";

                String[] platforms = platform.split(",");

                if(platforms.length > 1)
                    whereQuery += combiningClause + " platform in ('" + StringUtils.join(platforms,"','") + "')";
                else
                    whereQuery += combiningClause + " platform = '" + platform + "'";
            }

            if (!isStringEmpty(action)) {
                String combiningClause = isStringEmpty(whereQuery) ? " where" : " and";

                String[] actions = action.split(",");

                if(actions.length > 1)
                    whereQuery += combiningClause + " action in ('" + StringUtils.join(actions,"','") + "')";
                else
                    whereQuery += combiningClause + " action = '" + action + "'";
            }

            String catIDsQuery = "select distinct categoryByCId.cId from OpenLAPEvent" + whereQuery;

            List<Integer> catIDs = (List<Integer>)executeHQLQueryRaw(catIDsQuery);
            List<String> sCatIDs = catIDs.stream().map(Object::toString).collect(Collectors.toList());

            if(catIDs.size()>0)
            {
                String query = "from OpenLAPCategory where cId in (" + String.join(",", sCatIDs) + ") order by minor";

                List<OpenLAPCategory> dataList = (List<OpenLAPCategory>)executeHQLQueryRaw(query);

                for (OpenLAPCategory category : dataList) {
                    categories.put(category.getMinor(),category.getcId()+"");
                }
            }
        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            return null;
        }

        return categories;
    }

    public List<String> getAttributesValues(String source, String platform, String action, String cateogryIDs, String key){
        try {
            ObjectMapper mapper = new ObjectMapper();

            String whereQuery = "";

            if (!isStringEmpty(source)) {
                String[] sources = source.split(",");

                if(sources.length > 1)
                    whereQuery = " where eventByEventFk.source in ('" + StringUtils.join(sources,"','") + "')";
                else
                    whereQuery = " where eventByEventFk.source = '" + source + "'";
            }

            if (!isStringEmpty(platform)) {
                String combiningClause = isStringEmpty(whereQuery) ? " where" : " and";

                String[] platforms = platform.split(",");

                if(platforms.length > 1)
                    whereQuery += combiningClause + " eventByEventFk.platform in ('" + StringUtils.join(platforms,"','") + "')";
                else
                    whereQuery += combiningClause + " eventByEventFk.platform = '" + platform + "'";
            }

            if (!isStringEmpty(action)) {
                String combiningClause = isStringEmpty(whereQuery) ? " where" : " and";

                String[] actions = action.split(",");

                if(actions.length > 1)
                    whereQuery += combiningClause + " eventByEventFk.action in ('" + StringUtils.join(actions,"','") + "')";
                else
                    whereQuery += combiningClause + " eventByEventFk.action = '" + action + "'";
            }

            if (!isStringEmpty(key)) {
                String combiningClause = isStringEmpty(whereQuery) ? " where" : " and";
                whereQuery += combiningClause + " entityKey = '" + key + "'";
            }

            if (!isStringEmpty(cateogryIDs)) {
                String combiningClause = isStringEmpty(whereQuery) ? " where" : " and";

                String[] catIDs = cateogryIDs.split(",");

                if(catIDs.length > 1)
                    whereQuery += combiningClause + " eventByEventFk.categoryByCId.cId in (" + StringUtils.join(catIDs,",") + ")";
                else
                    whereQuery += combiningClause + " eventByEventFk.categoryByCId.cId = " + cateogryIDs;
            }

            String query = "select distinct value from OpenLAPEntity" + whereQuery + " order by value";

            List<String> dataList = (List<String>) executeHQLQueryRaw(query);

            return dataList;

        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            return null;
        }
    }

    public String getEventSources(String source, String platform, String action, String session, Integer timestampBegin, Integer timestampEnd, String sortDirection){
        return getEvents("source", source, platform, action, session, timestampBegin, timestampEnd, sortDirection);
    }

    public String getEventPlatforms(String source, String platform, String action, String session, Integer timestampBegin, Integer timestampEnd, String sortDirection){
        return getEvents("platform", source, platform, action, session, timestampBegin, timestampEnd, sortDirection);
    }

    public String getEventActions(String source, String platform, String action, String session, Integer timestampBegin, Integer timestampEnd, String sortDirection){
        return getEvents("action", source, platform, action, session, timestampBegin, timestampEnd, sortDirection);
    }

    public String getEventSessions(String source, String platform, String action, String session, Integer timestampBegin, Integer timestampEnd, String sortDirection){
        return getEvents("session", source, platform, action, session, timestampBegin, timestampEnd, sortDirection);
    }

    public String getEventTimestamps(String source, String platform, String action, String session, Integer timestampBegin, Integer timestampEnd, String sortDirection){
        return getEvents("timestamp", source, platform, action, session, timestampBegin, timestampEnd, sortDirection);
    }

    public List<OpenLAPColumnConfigData> getDataColumnsByIDs(String categoryIDs, HttpServletRequest request) {
        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());
        List<OpenLAPColumnConfigData> methodInputs = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            methodInputs =  new ArrayList<OpenLAPColumnConfigData>();

            // Manually adding columns related to the Event table (ALL EVENT COLUMNS SHOULD ALWAYS BE SEND BEFORE ENTITIES COLUMNS)
            methodInputs.add(new OpenLAPColumnConfigData("timestamp", OpenLAPColumnDataType.INTEGER, true, "Timestamp", "Time when the learning event occurred in unix timestamp format"));
            methodInputs.add(new OpenLAPColumnConfigData("source", OpenLAPColumnDataType.STRING, true, "Source", "Source of the learning event."));
            methodInputs.add(new OpenLAPColumnConfigData("platform", OpenLAPColumnDataType.STRING, true, "Platform", "Platform where the learning event occurred."));
            //methodInputs.add(new OpenLAPColumnConfigData("session", OpenLAPColumnDataType.STRING, true, "Session", "Session in which the learning event occured."));
            methodInputs.add(new OpenLAPColumnConfigData("action", OpenLAPColumnDataType.STRING, true, "Action", "Type of action performed in the learning event."));
            methodInputs.add(new OpenLAPColumnConfigData("category", OpenLAPColumnDataType.STRING, true, "Category", "Category of the learning event."));

            methodInputs.add(new OpenLAPColumnConfigData("user", OpenLAPColumnDataType.STRING, true, "User", "Anonymized user who performed the learning event."));

            String[] catIDs = categoryIDs.split(",");
            String subQuery = "";

            for (String catID : catIDs)
                subQuery += "select [K_ID] from Category_Keys_Mapping where C_ID = " + catID + " intersect ";

            subQuery = subQuery.substring(0, subQuery.lastIndexOf(" intersect "));

            String query = "select s.[Key], s.[Description], s.Title, s.[Type] FROM Keys_Specifications s " +
                    "WHERE s.[ID] in (" + subQuery + ")";

            // Adding columns related to the Entity table
            //String query = "SELECT S.[Key], S.[Description], S.Title, S.[Type] FROM Keys_Specifications S " +
            //        "INNER JOIN Category_Keys_Mapping M ON M.K_ID = S.ID " +
            //        "WHERE M.C_ID = " + categoryID;

            List<Map<String, Object>> dataList = executeSQLQueryRaw(query);

            if (dataList != null && dataList.size() > 0) {
                for (Map<String, Object> map : dataList) {
                    OpenLAPColumnDataType colType = null;

                    switch (map.get("Type").toString()) {
                        case "INTEGER":
                            colType = OpenLAPColumnDataType.INTEGER;
                            break;
                        case "BOOLEAN":
                            colType = OpenLAPColumnDataType.BOOLEAN;
                            break;
                        case "BYTE":
                            colType = OpenLAPColumnDataType.BYTE;
                            break;
                        case "CHAR":
                            colType = OpenLAPColumnDataType.CHAR;
                            break;
                        case "FLOAT":
                            colType = OpenLAPColumnDataType.FLOAT;
                            break;
                        case "LOCAL_DATE_TIME":
                            colType = OpenLAPColumnDataType.LOCAL_DATE_TIME;
                            break;
                        case "LONG":
                            colType = OpenLAPColumnDataType.LONG;
                            break;
                        case "SHORT":
                            colType = OpenLAPColumnDataType.SHORT;
                            break;
                        case "STRING":
                            colType = OpenLAPColumnDataType.STRING;
                            break;
                        default:
                            colType = OpenLAPColumnDataType.STRING;
                            break;
                    }
                    String colId = map.get("Key").toString();
                    String colTitle = map.get("Title").toString();
                    String colDesc = map.get("Description").toString();

                    methodInputs.add(new OpenLAPColumnConfigData(colId, colType, false, colTitle, colDesc));
                }
            }

        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            return null;
        }

        return methodInputs;
    }

    public List<OpenLAPColumnConfigData> getDataColumnsByNames(String categoryNames, HttpServletRequest request) {
        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());
        List<OpenLAPColumnConfigData> methodInputs = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            methodInputs =  new ArrayList<OpenLAPColumnConfigData>();

            // Manually adding columns related to the Event table (ALL EVENT COLUMNS SHOULD ALWAYS BE SEND BEFORE ENTITIES COLUMNS)
            methodInputs.add(new OpenLAPColumnConfigData("timestamp", OpenLAPColumnDataType.INTEGER, true, "Timestamp", "Time when the learning event occurred in unix timestamp format"));
            methodInputs.add(new OpenLAPColumnConfigData("session", OpenLAPColumnDataType.STRING, true, "Session", "Session in which the learning event occured."));
            methodInputs.add(new OpenLAPColumnConfigData("action", OpenLAPColumnDataType.STRING, true, "Action", "Type of action performed in the learning event."));
            methodInputs.add(new OpenLAPColumnConfigData("platform", OpenLAPColumnDataType.STRING, true, "Platform", "Platform where the learning event occurred."));
            methodInputs.add(new OpenLAPColumnConfigData("category", OpenLAPColumnDataType.STRING, true, "Category", "Category of the learning event."));
            methodInputs.add(new OpenLAPColumnConfigData("source", OpenLAPColumnDataType.STRING, true, "Source", "Source of the learning event."));
            methodInputs.add(new OpenLAPColumnConfigData("user", OpenLAPColumnDataType.STRING, true, "User", "Anonymized user who performed the learning event."));


            String[] catNames = categoryNames.split(",");
            String subQuery = "";

            for (String catName : catNames)
                subQuery += "select [K_ID] from Category_Keys_Mapping m inner join Category c on m.C_ID = c.C_Id where C.Minor = '" + catName + "' intersect ";

            subQuery = subQuery.substring(0, subQuery.lastIndexOf(" intersect "));

            String query = "select s.[Key], s.[Description], s.Title, s.[Type] FROM Keys_Specifications s " +
                    "WHERE s.[ID] in (" + subQuery + ")";


//            String query = "SELECT S.[Key], S.[Description], S.Title, S.[Type] FROM Category C " +
//                    "INNER JOIN Category_Keys_Mapping M ON C.C_Id = M.C_ID " +
//                    "INNER JOIN Keys_Specifications S ON M.K_ID = S.ID " +
//                    "WHERE C.Minor = '" + categoryName + "'";

            List<Map<String, Object>> dataList = executeSQLQueryRaw(query);

            if (dataList != null && dataList.size() > 0) {
                for (Map<String, Object> map : dataList) {
                    OpenLAPColumnDataType colType = null;

                    switch (map.get("Type").toString()) {
                        case "INTEGER":
                            colType = OpenLAPColumnDataType.INTEGER;
                            break;
                        case "BOOLEAN":
                            colType = OpenLAPColumnDataType.BOOLEAN;
                            break;
                        case "BYTE":
                            colType = OpenLAPColumnDataType.BYTE;
                            break;
                        case "CHAR":
                            colType = OpenLAPColumnDataType.CHAR;
                            break;
                        case "FLOAT":
                            colType = OpenLAPColumnDataType.FLOAT;
                            break;
                        case "LOCAL_DATE_TIME":
                            colType = OpenLAPColumnDataType.LOCAL_DATE_TIME;
                            break;
                        case "LONG":
                            colType = OpenLAPColumnDataType.LONG;
                            break;
                        case "SHORT":
                            colType = OpenLAPColumnDataType.SHORT;
                            break;
                        case "STRING":
                            colType = OpenLAPColumnDataType.STRING;
                            break;
                        default:
                            colType = OpenLAPColumnDataType.STRING;
                            break;
                    }
                    String colId = map.get("Key").toString();
                    String colTitle = map.get("Title").toString();
                    String colDesc = map.get("Description").toString();

                    methodInputs.add(new OpenLAPColumnConfigData(colId, colType, false, colTitle, colDesc));
                }
            }
        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            return null;
        }

        return methodInputs;
    }

    public List<OpenLAPColumnConfigData> getAnalyticsMethodInputs(long id, HttpServletRequest request) {
        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());
        List<OpenLAPColumnConfigData> methodInputs = null;

        try {
            ObjectMapper mapper = new ObjectMapper();

            String methodInputsJSON = performGetRequest(baseUrl + "/AnalyticsMethods/"+id+"/getInputPorts");
            methodInputs = mapper.readValue(methodInputsJSON, mapper.getTypeFactory().constructCollectionType(List.class, OpenLAPColumnConfigData.class));
        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            return null;
        }

        return methodInputs;
    }

    public List<OpenLAPColumnConfigData> getAnalyticsMethodOutputs(long id, HttpServletRequest request) {
        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());
        List<OpenLAPColumnConfigData> methodInputs = null;

        try {
            ObjectMapper mapper = new ObjectMapper();

            String methodInputsJSON = performGetRequest(baseUrl + "/AnalyticsMethods/"+id+"/getOutputPorts");
            methodInputs = mapper.readValue(methodInputsJSON, mapper.getTypeFactory().constructCollectionType(List.class, OpenLAPColumnConfigData.class));
        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            return null;
        }

        return methodInputs;
    }

    public List<OpenLAPColumnConfigData> getVisualizationMethodInputs(long frameworkId, long methodId, HttpServletRequest request) {
        List<OpenLAPColumnConfigData> methodInputs = null;

        try {
            ObjectMapper mapper = new ObjectMapper();


            String methodInputsJSON = performGetRequest(visualizationURL + "/frameworks/"+frameworkId+"/methods/"+methodId+"/configuration");
            VisualizationMethodConfigurationResponse visResponse = mapper.readValue(methodInputsJSON, VisualizationMethodConfigurationResponse.class);

            methodInputs = visResponse.getMethodConfiguration().getInput().getColumnsConfigurationData();
        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            return null;
        }

        return methodInputs;
    }

    public List<QuestionResponse> getQuestions(HttpServletRequest request) {
        List<QuestionResponse> questionsResponse = new ArrayList<QuestionResponse>();

        List<Question> allQuestions = getAllQuestions();

        for (Question question : allQuestions) {
            questionsResponse.add(new QuestionResponse(question.getId(), question.getName(), question.getIndicatorCount()));
        }

        return questionsResponse;
    }

    public List<QuestionResponse> searchQuestions(String searchParameter, boolean exactSearch, String colName, String sortDirection, boolean sort, HttpServletRequest request){
        List<QuestionResponse> questionsResponse = new ArrayList<QuestionResponse>();

        List<Question> allQuestions = searchQuestions(searchParameter, exactSearch, colName, sortDirection, sort);

        for (Question question : allQuestions) {
            questionsResponse.add(new QuestionResponse(question.getId(), question.getName(), question.getIndicatorCount()));
        }

        return questionsResponse;
    }

    public List<QuestionResponse> getSortedQuestions(String colName, String sortDirection, boolean sort, HttpServletRequest request){
        List<QuestionResponse> questionsResponse = new ArrayList<QuestionResponse>();

        List<Question> allQuestions = getSortedQuestions(colName, sortDirection, sort);

        for (Question question : allQuestions) {
            questionsResponse.add(new QuestionResponse(question.getId(), question.getName(), question.getIndicatorCount()));
        }

        return questionsResponse;
    }

    public List<IndicatorResponse> getIndicatorsByQuestionId(long questionId, HttpServletRequest request) {
        List<IndicatorResponse> indicators = new ArrayList<IndicatorResponse>();

        Question question = getQuestionById(questionId);

        for (Triad triad : question.getTriads()) {
            Indicator indicator = getIndicatorById(triad.getIndicatorReference().getIndicators().get("0").getId());

            IndicatorResponse indicatorResponse = new IndicatorResponse();
            indicatorResponse.setId(triad.getId());
            //indicatorResponse.setQuery(indicator.getQuery());
            indicatorResponse.setIndicatorReference(triad.getIndicatorReference());
            //indicatorResponse.setAnalyticsMethodReference(triad.getAnalyticsMethodReference().getAnalyticsMethods().get("0"));
            indicatorResponse.setAnalyticsMethodReference(triad.getAnalyticsMethodReference());
            indicatorResponse.setVisualizationReference(triad.getVisualizationReference());
            //indicatorResponse.setQueryToMethodConfig(triad.getIndicatorToAnalyticsMethodMapping());
            indicatorResponse.setQueryToMethodConfig(new HashMap<>());
            indicatorResponse.getQueryToMethodConfig().put("0",triad.getIndicatorToAnalyticsMethodMapping().getPortConfigs().get("0"));


            indicatorResponse.setMethodToVisualizationConfig(triad.getAnalyticsMethodToVisualizationMapping());
            indicatorResponse.setName(indicator.getName());
            indicatorResponse.setParameters(triad.getParameters());
            //indicatorResponse.setComposite(indicator.isComposite());
            indicatorResponse.setIndicatorType(triad.getIndicatorReference().getIndicatorType());
            indicatorResponse.setCreatedBy(triad.getCreatedBy());

            indicators.add(indicatorResponse);
        }

        return indicators;
    }

    public List<IndicatorResponse> getIndicators(HttpServletRequest request) {
        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());
        ObjectMapper mapper = new ObjectMapper();

        List<IndicatorResponse> indicatorResponses = new ArrayList<IndicatorResponse>();

        List<Triad> triads = null;
        try {
            String triadsJSON = performGetRequest(baseUrl + "/AnalyticsModules/Triads/");
            triads = mapper.readValue(triadsJSON, mapper.getTypeFactory().constructCollectionType(List.class, Triad.class));
        } catch (Exception exc) {
            throw new ItemNotFoundException("No indicator found","1");
        }

        for (Triad triad : triads) {
            Indicator indicator = getIndicatorById(triad.getIndicatorReference().getIndicators().get("0").getId());

            IndicatorResponse indicatorResponse = new IndicatorResponse();
            indicatorResponse.setId(triad.getId());
            //indicatorResponse.setQuery(indicator.getQuery());
            indicatorResponse.setIndicatorReference(triad.getIndicatorReference());
            indicatorResponse.setAnalyticsMethodReference(triad.getAnalyticsMethodReference());
            indicatorResponse.setVisualizationReference(triad.getVisualizationReference());

            indicatorResponse.setQueryToMethodConfig(new HashMap<>());
            indicatorResponse.getQueryToMethodConfig().put("0", triad.getIndicatorToAnalyticsMethodMapping().getPortConfigs().get("0"));
            indicatorResponse.setMethodToVisualizationConfig(triad.getAnalyticsMethodToVisualizationMapping());
            indicatorResponse.setName(indicator.getName());
            indicatorResponse.setParameters(triad.getParameters());
            //indicatorResponse.setComposite(indicator.isComposite());
            indicatorResponse.setIndicatorType(triad.getIndicatorReference().getIndicatorType());
            indicatorResponse.setCreatedBy(triad.getCreatedBy());

            indicatorResponses.add(indicatorResponse);
        }

        return indicatorResponses;
    }

    public IndicatorResponse getTriadById(long triadId, HttpServletRequest request) {
        IndicatorResponse indicatorResponse = new IndicatorResponse();
        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());
        ObjectMapper mapper = new ObjectMapper();

        Triad triad;

        try {
            String triadJSON = performGetRequest(baseUrl + "/AnalyticsModules/Triads/" + triadId);
            triad = mapper.readValue(triadJSON, Triad.class);
        } catch (Exception exc) {
            throw new ItemNotFoundException("Indicator with triad id '" + triadId + "' not found.", "1");
        }


        if (triad == null)
            throw new ItemNotFoundException("Indicator with triad id '" + triadId + "' not found.","1");


        Indicator indicator = getIndicatorById(triad.getIndicatorReference().getIndicators().get("0").getId());

        indicatorResponse.setId(triad.getId());
        //indicatorResponse.setQuery(indicator.getQuery());
        indicatorResponse.setIndicatorReference(triad.getIndicatorReference());
        indicatorResponse.setAnalyticsMethodReference(triad.getAnalyticsMethodReference());
        indicatorResponse.setVisualizationReference(triad.getVisualizationReference());
        //indicatorResponse.setQueryToMethodConfig(triad.getIndicatorToAnalyticsMethodMapping().getPortConfigs().get("0"));
        //indicatorResponse.setMethodToVisualizationConfig(triad.getAnalyticsMethodToVisualizationMapping());
        indicatorResponse.setName(indicator.getName());
        indicatorResponse.setParameters(triad.getParameters());
        //indicatorResponse.setComposite(indicator.isComposite());
        indicatorResponse.setIndicatorType(triad.getIndicatorReference().getIndicatorType());
        indicatorResponse.setCreatedBy(triad.getCreatedBy());

        return indicatorResponse;
    }

    public List<IndicatorResponse> searchIndicators(String searchParameter, boolean exactSearch, String colName, String sortDirection, boolean sort, String userName, HttpServletRequest request){

        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());
        ObjectMapper mapper = new ObjectMapper();

        List<IndicatorResponse> indicatorResponses = new ArrayList<IndicatorResponse>();

        //List<Indicator> allIndicators = searchIndicators(searchParameter, exactSearch, colName, sortDirection, sort);

        List<Triad> triads = null;
        try {

            String url;

            if (!isStringEmpty(userName))
                url = baseUrl + "/AnalyticsModules/TriadsByUser?userName="+userName;
            else
                url = baseUrl + "/AnalyticsModules/Triads/";

            String triadsJSON = performGetRequest(url);
            triads = mapper.readValue(triadsJSON, mapper.getTypeFactory().constructCollectionType(List.class, Triad.class));
        } catch (Exception exc) {
            throw new ItemNotFoundException("No indicator found","1");
        }

        for (Triad triad : triads) {
            //Indicator indicator = getIndicatorById(triad.getIndicatorReference().getId());

            //Only returning the indicators which are not composite and which have parameters walue
            if(triad.getParameters().length()>5) {

                boolean shouldAdd = true;

                //Applying filtering
                if(!isStringEmpty(searchParameter))
                    if(exactSearch)
                        shouldAdd = searchParameter.toLowerCase().equals(triad.getIndicatorReference().getIndicators().get("0").getIndicatorName().toLowerCase());
                    else
                        shouldAdd = triad.getIndicatorReference().getIndicators().get("0").getIndicatorName().matches("(?i).*"+searchParameter+".*");

                if(shouldAdd) {
                    IndicatorResponse indicatorResponse = new IndicatorResponse();
                    indicatorResponse.setId(triad.getId());
                    //indicatorResponse.setQuery(indicator.getQuery());
                    //indicatorResponse.setIndicatorReference(triad.getIndicatorReference());
                    //indicatorResponse.setAnalyticsMethodReference(triad.getAnalyticsMethodReference());
                    //indicatorResponse.setVisualizationReference(triad.getVisualizationReference());
                    //indicatorResponse.setQueryToMethodConfig(triad.getIndicatorToAnalyticsMethodMapping());
                    //indicatorResponse.setMethodToVisualizationConfig(triad.getAnalyticsMethodToVisualizationMapping());
                    indicatorResponse.setName(triad.getIndicatorReference().getIndicators().get("0").getIndicatorName());
                    indicatorResponse.setParameters(triad.getParameters());
                    indicatorResponse.setIndicatorType(triad.getIndicatorReference().getIndicatorType());
                    //indicatorResponse.setComposite(indicator.isComposite());
                    //indicatorResponse.setCreatedBy(triad.getCreatedBy());0000

                    indicatorResponses.add(indicatorResponse);
                }
            }
        }

        if(sort) {

            if(sortDirection.toLowerCase().equals("desc"))
                switch (colName) {
                    case "id":
                        Collections.sort(indicatorResponses, Collections.reverseOrder((IndicatorResponse r1, IndicatorResponse r2) -> compareIntegers(r1.getId(), r2.getId())));
                        break;
                    case "name":
                        default:
                            Collections.sort(indicatorResponses, Collections.reverseOrder((IndicatorResponse r1, IndicatorResponse r2) -> r1.getName().compareTo(r2.getName())));
                            break;
                }

            else
                switch (colName) {
                    case "id":
                        Collections.sort(indicatorResponses, (IndicatorResponse r1, IndicatorResponse r2) -> compareIntegers(r1.getId(), r2.getId()));
                        break;
                    case "name":
                    default:
                        Collections.sort(indicatorResponses, (IndicatorResponse r1, IndicatorResponse r2) -> r1.getName().compareTo(r2.getName()));
                        break;
                }
        }

        return indicatorResponses;
    }

    /*public QuestionSaveResponse saveQuestionAndIndicators(QuestionSaveRequest saveRequest, HttpServletRequest request) {
        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());

        ObjectMapper mapper = new ObjectMapper();

        QuestionSaveResponse questionSaveResponse = new QuestionSaveResponse();

        Set<Triad> triads = new HashSet<Triad>();

        List<IndicatorSaveResponse> indicatorResponses = new ArrayList<IndicatorSaveResponse>();
        AnalyticsGoal goal;

        try {
            String goalsJSON = performGetRequest(baseUrl + "/AnalyticsModules/AnalyticsGoals/" + saveRequest.getGoalID());
            goal = mapper.readValue(goalsJSON, AnalyticsGoal.class);
        } catch (Exception exc) {
            questionSaveResponse.setQuestionSaved(false);
            questionSaveResponse.setErrorMessage("Problem accessing Analytics Goal: " + exc.getMessage());
            Log.info("Problem accessing Analytics Goal: " + exc.getMessage());
            return questionSaveResponse;
        }

        Question question = new Question();
        question.setName(saveRequest.getQuestion());
        //question.setTriads(triads);
        question.setIndicatorCount(saveRequest.getIndicators().size());

        Question savedQuestion = saveQuestion(question);

        for (IndicatorSaveRequest indicatorRequest : saveRequest.getIndicators()) {

            IndicatorSaveResponse indicatorResponse = new IndicatorSaveResponse();
            indicatorResponse.setIndicatorClientID(indicatorRequest.getIndicatorClientID());

            try {
                //Saving the indicator query
                Indicator ind = new Indicator();
                ind.setName(indicatorRequest.getName());
                ind.setQuery(indicatorRequest.getQuery());
                ind.setComposite(indicatorRequest.isComposite());

                IndicatorReference indicatorReference = null;

                if (indicatorRequest.getServerID() > 0) {
                    Indicator loadedIndicator = getIndicatorById(indicatorRequest.getServerID());

                    if(loadedIndicator.equals(ind))
                        indicatorReference = new IndicatorReference(loadedIndicator.getId(), loadedIndicator.getName());
                    else {
                        Indicator savedInd = saveIndicator(ind);
                        indicatorReference = new IndicatorReference(savedInd.getId(), savedInd.getName());
                    }
                }
                else {
                    Indicator savedInd = saveIndicator(ind);
                    indicatorReference = new IndicatorReference(savedInd.getId(), savedInd.getName());
                }

                //Getting Analytics Method metadata
                AnalyticsMethodMetadata methodMetadata;
                String analyticsMethodJSON = performGetRequest(baseUrl + "/AnalyticsMethods/" + indicatorRequest.getAnalyticsMethodId());
                methodMetadata = mapper.readValue(analyticsMethodJSON, AnalyticsMethodMetadata.class);


                //Getting Visualization technique metadata
                VisualizerReference visualizerReference;
                String visFrameworkJSON = performGetRequest(visualizationURL + "/frameworks/" + indicatorRequest.getVisualizationFrameworkId());
                VisualizationFrameworkDetailsResponse frameworkResponse = mapper.readValue(visFrameworkJSON, VisualizationFrameworkDetailsResponse.class);
                String visMethodJSON = performGetRequest(visualizationURL + "/frameworks/"+ indicatorRequest.getVisualizationFrameworkId() + "/methods/"+ indicatorRequest.getVisualizationMethodId());
                VisualizationMethodDetailsResponse methodResponse = mapper.readValue(visMethodJSON, VisualizationMethodDetailsResponse.class);
                visualizerReference = new VisualizerReference(
                            frameworkResponse.getVisualizationFramework().getId(),
                            methodResponse.getVisualizationMethod().getId(),
                            frameworkResponse.getVisualizationFramework().getName(),
                            methodResponse.getVisualizationMethod().getName());


                //Validating the data to method port configuration
                String queryToMethodConfigJSON = indicatorRequest.getQueryToMethodConfig().toString();
                String queryToMethodConfigValidJSON = performPutRequest(baseUrl + "/AnalyticsMethods/"+indicatorRequest.getAnalyticsMethodId()+"/validateConfiguration", queryToMethodConfigJSON);
                OpenLAPDataSetConfigValidationResult queryToMethodConfigValid =  mapper.readValue(queryToMethodConfigValidJSON, OpenLAPDataSetConfigValidationResult.class);
                if(!queryToMethodConfigValid.isValid()){
                    indicatorResponse.setIndicatorSaved(false);
                    indicatorResponse.setErrorMessage("Query to Method Port-Configuration is not valid: " + queryToMethodConfigValid.getValidationMessage());
                    indicatorResponses.add(indicatorResponse);
                    continue;
                }


                //Validating the method to visualization port configuration
                ValidateVisualizationMethodConfigurationRequest methodToVisConfigRequest = new  ValidateVisualizationMethodConfigurationRequest();
                methodToVisConfigRequest.setConfigurationMapping(indicatorRequest.getMethodToVisualizationConfig());

//                String methodToVisConfigValidJSON = performJSONPostRequest(visualizationURL + "/frameworks/"+ indicatorRequest.getVisualizationFrameworkId() + "/methods/"+ indicatorRequest.getVisualizationMethodId()+"/validateConfiguration", mapper.writeValueAsString(methodToVisConfigRequest));
//                ValidateVisualizationMethodConfigurationResponse methodToVisConfigValid =  mapper.readValue(methodToVisConfigValidJSON, ValidateVisualizationMethodConfigurationResponse.class);

                ValidateVisualizationMethodConfigurationResponse methodToVisConfigValid = performJSONPostRequest(visualizationURL + "/frameworks/"+ indicatorRequest.getVisualizationFrameworkId() + "/methods/"+ indicatorRequest.getVisualizationMethodId()+"/validateConfiguration", mapper.writeValueAsString(methodToVisConfigRequest), ValidateVisualizationMethodConfigurationResponse.class);

                if(!methodToVisConfigValid.isConfigurationValid()){
                    indicatorResponse.setIndicatorSaved(false);
                    indicatorResponse.setErrorMessage("Method to Visualization Port-Configuration is not valid: " + methodToVisConfigValid.getValidationMessage());
                    indicatorResponses.add(indicatorResponse);
                    continue;
                }

                //Saving the triad
                Triad triad = new Triad(saveRequest.getGoalID(), indicatorReference, methodMetadata, visualizerReference, indicatorRequest.getQueryToMethodConfig(), indicatorRequest.getMethodToVisualizationConfig());
                triad.setCreatedBy(indicatorRequest.getCreatedBy());
                triad.setParameters(indicatorRequest.getParameters());
                String triadJSON = triad.toString();
//                String savedTriadJSON = performJSONPostRequest(baseUrl + "/AnalyticsModules/Triads/", triadJSON);
//                Triad savedTriad = mapper.readValue(savedTriadJSON, Triad.class);

                Triad savedTriad = performJSONPostRequest(baseUrl + "/AnalyticsModules/Triads/", triadJSON, Triad.class);

                triads.add(savedTriad);

                setQuestionTriadMapping(savedQuestion.getId(), savedTriad.getId());

                indicatorResponse.setIndicatorSaved(true);
                indicatorResponse.setIndicatorRequestCode(getIndicatorRequestCode(savedTriad));

                indicatorResponses.add(indicatorResponse);
            }
            catch (Exception exc) {
                indicatorResponse.setIndicatorSaved(false);
                indicatorResponse.setErrorMessage(exc.getMessage());
            }
        }

        questionSaveResponse.setIndicatorSaveResponses(indicatorResponses);
        questionSaveResponse.setQuestionRequestCode(getQuestionRequestCode(triads));
        questionSaveResponse.setQuestionSaved(true);

        return questionSaveResponse;
    }*/

    public QuestionSaveResponse saveQuestionAndIndicators(QuestionSaveRequest saveRequest, HttpServletRequest request) {
        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());

        log.info("Saving question:" + saveRequest.getQuestion());

        ObjectMapper mapper = new ObjectMapper();

        QuestionSaveResponse questionSaveResponse = new QuestionSaveResponse();

        Set<Triad> triads = new HashSet<Triad>();

        List<IndicatorSaveResponse> indicatorResponses = new ArrayList<IndicatorSaveResponse>();
        AnalyticsGoal goal;

        try {
            String goalsJSON = performGetRequest(baseUrl + "/AnalyticsModules/AnalyticsGoals/" + saveRequest.getGoalID());
            goal = mapper.readValue(goalsJSON, AnalyticsGoal.class);
        } catch (Exception exc) {
            questionSaveResponse.setQuestionSaved(false);
            questionSaveResponse.setErrorMessage("Problem accessing Analytics Goal: " + exc.getMessage());
            Log.info("Problem accessing Analytics Goal: " + exc.getMessage());
            return questionSaveResponse;
        }

        Question question = new Question();
        question.setName(saveRequest.getQuestion());
        //question.setTriads(triads);
        question.setIndicatorCount(saveRequest.getIndicators().size());

        Question savedQuestion = saveQuestion(question);

        for (IndicatorSaveRequest indicatorRequest : saveRequest.getIndicators()) {

            IndicatorSaveResponse indicatorResponse = new IndicatorSaveResponse();
            indicatorResponse.setIndicatorClientID(indicatorRequest.getIndicatorClientID());

            try {
                //Saving the indicator query
                Indicator ind = new Indicator();
                ind.setName(indicatorRequest.getName());

                IndicatorReference indicatorReference = new IndicatorReference();
                indicatorReference.setIndicatorType(indicatorRequest.getIndicatorType());

                Set<Map.Entry<String, String>> querySet = indicatorRequest.getQuery().entrySet();

                for (Map.Entry<String, String> indQuery : querySet)
                    ind.getQuery().getQueries().put(indQuery.getKey(), indQuery.getValue());

                Indicator savedInd = saveIndicator(ind);
                indicatorReference.getIndicators().put("0", new IndicatorEntry(savedInd.getId(), savedInd.getName()));

                //ind.setComposite(false);
//                for (Map.Entry<String, String> indQuery : querySet) {
//                    if (indicatorRequest.getServerID().get(indQuery.getKey()) > 0) {
//                        Indicator loadedIndicator = getIndicatorById(indicatorRequest.getServerID().get(indQuery.getKey()));
//
//                        if (loadedIndicator.equals(ind)) {
//                            indicatorReference.getIndicators().put(indQuery.getKey(), new IndicatorEntry(loadedIndicator.getId(), loadedIndicator.getName()));
//                        } else {
//                            Indicator savedInd = saveIndicator(ind);
//                            indicatorReference.getIndicators().put(indQuery.getKey(), new IndicatorEntry(savedInd.getId(), savedInd.getName()));
//                        }
//                    } else {
//                        Indicator savedInd = saveIndicator(ind);
//                        indicatorReference.getIndicators().put(indQuery.getKey(), new IndicatorEntry(savedInd.getId(), savedInd.getName()));
//                    }
//                }


                //Getting Analytics Method metadata
                // AnalyticsMethodMetadata methodMetadata;
                // String analyticsMethodJSON = performGetRequest(baseUrl + "/AnalyticsMethods/" + indicatorRequest.getAnalyticsMethodId().get("0"));
                // methodMetadata = mapper.readValue(analyticsMethodJSON, AnalyticsMethodMetadata.class);

                AnalyticsMethodReference methodReference = new AnalyticsMethodReference();
                for (Map.Entry<String, Long> methodId : indicatorRequest.getAnalyticsMethodId().entrySet())
                    methodReference.getAnalyticsMethods().put(methodId.getKey(), new AnalyticsMethodEntry(methodId.getValue(), indicatorRequest.getMethodInputParams()));
                // TODO: Additional parameters need to be implemented. currently every method receive the same set of parameters.

                OpenLAPPortConfigReference queryToMethodReference = new OpenLAPPortConfigReference();
                for (Map.Entry<String, OpenLAPPortConfig> methodConfig : indicatorRequest.getQueryToMethodConfig().entrySet()) {

                    //Validating the data to method port configuration
                    String queryToMethodConfigJSON = methodConfig.getValue().toString();
                    String queryToMethodConfigValidJSON = performPutRequest(baseUrl + "/AnalyticsMethods/"+indicatorRequest.getAnalyticsMethodId().get(methodConfig.getKey())+"/validateConfiguration", queryToMethodConfigJSON);
                    OpenLAPDataSetConfigValidationResult queryToMethodConfigValid =  mapper.readValue(queryToMethodConfigValidJSON, OpenLAPDataSetConfigValidationResult.class);
                    if(!queryToMethodConfigValid.isValid()){
                        indicatorResponse.setIndicatorSaved(false);
                        indicatorResponse.setErrorMessage("Query to Method Port-Configuration is not valid: " + queryToMethodConfigValid.getValidationMessage());
                        indicatorResponses.add(indicatorResponse);
                        continue;
                    }

                    queryToMethodReference.getPortConfigs().put(methodConfig.getKey(), methodConfig.getValue());
                }
//                if(indicatorRequest.getIndicatorType().equals("simple")) {
//                    ind.getQuery().getQueries().put("0", indicatorRequest.getQuery().get("0"));
//                    ind.setComposite(false);
//
//                    indicatorReference.setIndicatorType("simple");
//
//                    if (indicatorRequest.getServerID().get("0") > 0) {
//                        Indicator loadedIndicator = getIndicatorById(indicatorRequest.getServerID().get("0"));
//
//                        if (loadedIndicator.equals(ind)) {
//                            indicatorReference.getIndicators().put("0", new IndicatorEntry(loadedIndicator.getId(), loadedIndicator.getName()));
//                        } else {
//                            Indicator savedInd = saveIndicator(ind);
//                            indicatorReference.getIndicators().put("0", new IndicatorEntry(savedInd.getId(), savedInd.getName()));
//                        }
//                    } else {
//                        Indicator savedInd = saveIndicator(ind);
//                        indicatorReference.getIndicators().put("0", new IndicatorEntry(savedInd.getId(), savedInd.getName()));
//                    }
//
//                    //Getting Analytics Method metadata
//                    AnalyticsMethodMetadata methodMetadata;
//                    String analyticsMethodJSON = performGetRequest(baseUrl + "/AnalyticsMethods/" + indicatorRequest.getAnalyticsMethodId().get("0"));
//                    methodMetadata = mapper.readValue(analyticsMethodJSON, AnalyticsMethodMetadata.class);
//
//                    methodReference = new AnalyticsMethodReference();
//                    methodReference.getAnalyticsMethods().put("0", new AnalyticsMethodEntry(indicatorRequest.getAnalyticsMethodId().get("0"), indicatorRequest.getMethodInputParams()));
//
//                    queryToMethodReference = new OpenLAPPortConfigReference();
//                    queryToMethodReference.getPortConfigs().put("0", indicatorRequest.getQueryToMethodConfig().get("0"));
//                }

                //Getting Visualization technique metadata
                VisualizerReference visualizerReference;
                String visFrameworkJSON = performGetRequest(visualizationURL + "/frameworks/" + indicatorRequest.getVisualizationFrameworkId());
                VisualizationFrameworkDetailsResponse frameworkResponse = mapper.readValue(visFrameworkJSON, VisualizationFrameworkDetailsResponse.class);
                String visMethodJSON = performGetRequest(visualizationURL + "/frameworks/"+ indicatorRequest.getVisualizationFrameworkId() + "/methods/"+ indicatorRequest.getVisualizationMethodId());
                VisualizationMethodDetailsResponse methodResponse = mapper.readValue(visMethodJSON, VisualizationMethodDetailsResponse.class);
                visualizerReference = new VisualizerReference(
                        frameworkResponse.getVisualizationFramework().getId(),
                        methodResponse.getVisualizationMethod().getId(),
                        indicatorRequest.getVisualizationInputParams());

                //Validating the method to visualization port configuration
                ValidateVisualizationMethodConfigurationRequest methodToVisConfigRequest = new  ValidateVisualizationMethodConfigurationRequest();
                methodToVisConfigRequest.setConfigurationMapping(indicatorRequest.getMethodToVisualizationConfig());

//                String methodToVisConfigValidJSON = performJSONPostRequest(visualizationURL + "/frameworks/"+ indicatorRequest.getVisualizationFrameworkId() + "/methods/"+ indicatorRequest.getVisualizationMethodId()+"/validateConfiguration", mapper.writeValueAsString(methodToVisConfigRequest));
//                ValidateVisualizationMethodConfigurationResponse methodToVisConfigValid =  mapper.readValue(methodToVisConfigValidJSON, ValidateVisualizationMethodConfigurationResponse.class);

                ValidateVisualizationMethodConfigurationResponse methodToVisConfigValid = performJSONPostRequest(visualizationURL + "/frameworks/"+ indicatorRequest.getVisualizationFrameworkId() + "/methods/"+ indicatorRequest.getVisualizationMethodId()+"/validateConfiguration", mapper.writeValueAsString(methodToVisConfigRequest), ValidateVisualizationMethodConfigurationResponse.class);

                if(!methodToVisConfigValid.isConfigurationValid()){
                    indicatorResponse.setIndicatorSaved(false);
                    indicatorResponse.setErrorMessage("Method to Visualization Port-Configuration is not valid: " + methodToVisConfigValid.getValidationMessage());
                    indicatorResponses.add(indicatorResponse);
                    continue;
                }

                //Saving the triad
                Triad triad = new Triad(saveRequest.getGoalID(), indicatorReference, methodReference, visualizerReference, queryToMethodReference, indicatorRequest.getMethodToVisualizationConfig());
                triad.setCreatedBy(indicatorRequest.getCreatedBy());
                triad.setParameters(indicatorRequest.getParameters());
                String triadJSON = triad.toString();
//                String savedTriadJSON = performJSONPostRequest(baseUrl + "/AnalyticsModules/Triads/", triadJSON);
//                Triad savedTriad = mapper.readValue(savedTriadJSON, Triad.class);

                Triad savedTriad = performJSONPostRequest(baseUrl + "/AnalyticsModules/Triads/", triadJSON, Triad.class);

                triads.add(savedTriad);

                setQuestionTriadMapping(savedQuestion.getId(), savedTriad.getId());

                indicatorResponse.setIndicatorSaved(true);
                indicatorResponse.setIndicatorRequestCode(getIndicatorRequestCode(savedTriad));

                indicatorResponses.add(indicatorResponse);
            }
            catch (Exception exc) {
                indicatorResponse.setIndicatorSaved(false);
                indicatorResponse.setErrorMessage(exc.getMessage());
            }
        }

        questionSaveResponse.setIndicatorSaveResponses(indicatorResponses);
        questionSaveResponse.setQuestionRequestCode(getQuestionRequestCode(triads));
        questionSaveResponse.setQuestionSaved(true);

        return questionSaveResponse;
    }

    public QuestionSaveResponse saveQuestionAndIndicatorsDummy(QuestionSaveRequest questionSaveRequest) {
        QuestionSaveResponse questionSaveResponse = new QuestionSaveResponse();

        Question question = getQuestionById(51);

        Object[] qTriads = question.getTriads().toArray();
        Triad triad = (Triad)qTriads[0];

        List<IndicatorSaveResponse> indicatorResponses = new ArrayList<IndicatorSaveResponse>();

        Set<Triad> triads = new HashSet<Triad>();

        for (IndicatorSaveRequest indicatorRequest : questionSaveRequest.getIndicators()) {
            IndicatorSaveResponse indicatorResponse = new IndicatorSaveResponse();
            indicatorResponse.setIndicatorClientID(indicatorRequest.getIndicatorClientID());
            indicatorResponse.setIndicatorSaved(true);
            indicatorResponse.setIndicatorRequestCode(getIndicatorRequestCode(triad));
            indicatorResponses.add(indicatorResponse);

            triads.add(triad);
        }


        questionSaveResponse.setIndicatorSaveResponses(indicatorResponses);
        questionSaveResponse.setQuestionRequestCode(getQuestionRequestCode(triads));
        questionSaveResponse.setQuestionSaved(true);

        return questionSaveResponse;
    }

    public IndicatorSaveResponse getIndicatorRequestCode(long triadId, HttpServletRequest request) {

        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());
        ObjectMapper mapper = new ObjectMapper();

        Triad triad = null;
        IndicatorSaveResponse indicatorResponse = new IndicatorSaveResponse();

        try {
            String triadJSON = performGetRequest(baseUrl + "/AnalyticsModules/Triads/" + triadId);
            triad = mapper.readValue(triadJSON, Triad.class);
        } catch (Exception exc) {
            indicatorResponse.setIndicatorSaved(false);
            indicatorResponse.setErrorMessage("Indicator with triad id '" + triadId + "' not found.");
            return indicatorResponse;
        }

        if (triad == null) {
            indicatorResponse.setIndicatorSaved(false);
            indicatorResponse.setErrorMessage("Indicator with triad id '" + triadId + "' not found.");
        }
        else {
            indicatorResponse.setIndicatorSaved(true);
            indicatorResponse.setIndicatorRequestCode(getIndicatorRequestCode(triad));
            indicatorResponse.setErrorMessage(triad.getIndicatorReference().getIndicators().get("0").getIndicatorName());
        }

        return indicatorResponse;
    }

    public QuestionSaveResponse getQuestionRequestCode(long questionId, HttpServletRequest request) {

        QuestionSaveResponse questionSaveResponse = new QuestionSaveResponse();
        Question question = getQuestionById(questionId);

        if (question == null) {
            questionSaveResponse.setQuestionSaved(false);
            questionSaveResponse.setErrorMessage("Question with id '" + questionId + "' not found.");
            return questionSaveResponse;
        }

        List<IndicatorSaveResponse> indicatorSaveResponses = new ArrayList<IndicatorSaveResponse>();

        for (Triad triad : question.getTriads()) {
            IndicatorSaveResponse indicatorSaveResponse = new IndicatorSaveResponse();

            indicatorSaveResponse.setIndicatorSaved(true);
            indicatorSaveResponse.setIndicatorRequestCode(getIndicatorRequestCode(triad));
            indicatorSaveResponse.setErrorMessage(triad.getIndicatorReference().getIndicators().get("0").getIndicatorName());

            indicatorSaveResponses.add(indicatorSaveResponse);
        }

        questionSaveResponse.setIndicatorSaveResponses(indicatorSaveResponses);
        questionSaveResponse.setQuestionRequestCode(getQuestionRequestCode(question.getTriads()));
        questionSaveResponse.setQuestionSaved(true);
        questionSaveResponse.setErrorMessage(question.getName());

        return questionSaveResponse;
    }

    public String testing(String categoryID, HttpServletRequest request){
        String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());
        String returnValue = "";

        try {
            ObjectMapper mapper = new ObjectMapper();

//            String query = "select eventByEventFk.usersByUId.name, value, eventByEventFk.categoryByCId.minor, count(e1) from OpenLAPEntity e1 where " +
//                    "eventByEventFk.action in ('Update') and " +
//                    "eventByEventFk.platform in ('WEB') and " +
//                    "eventByEventFk.source in ('L2P') and " +
//                    "eventByEventFk.categoryByCId.cId in (" + categoryID + ") and " +
//                    "entityKey = 'CourseId' " +
//                    "group by eventByEventFk.usersByUId.name, eventByEventFk.categoryByCId.minor, value " +
//                    "order by eventByEventFk.usersByUId.name";

//            String query = "select eventByEventFk.usersByUId.name, value, eventByEventFk.categoryByCId.minor, count(e1) from OpenLAPEntity e1, OpenLAPEntity e2 where " +
//                    "eventByEventFk.action in ('Update') and " +
//                    "eventByEventFk.platform in ('WEB') and " +
//                    "eventByEventFk.source in ('L2P') and " +
//                    "eventByEventFk.categoryByCId.cId in (" + categoryID + ") and " +
//                    "entityKey = 'CourseId' " +
//                    "group by eventByEventFk.usersByUId.name, eventByEventFk.categoryByCId.minor, value " +
//                    "order by eventByEventFk.usersByUId.name";
//
//            List<?> dataList = executeHQLQueryRaw(query);
//
//            OpenLAPDataSet dataSet = transformHQLToOpenLAPDatSet(dataList, query);

            List<OpenLAPDataSetMergeMapping> mergeMappings = new ArrayList<>();
            OpenLAPDataSetMergeMapping first = new OpenLAPDataSetMergeMapping();
            first.setIndRefKey1("1");
            first.setIndRefField1("item_count");
            first.setIndRefKey2("2");
            first.setIndRefField2("average_marks");

            mergeMappings.add(first);
            try {
                returnValue =  mapper.writeValueAsString(mergeMappings);
            } catch (Exception exc) {}

        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            return null;
        }

        return returnValue;
    }

    public Boolean validateQuestionName(String name) {
        return isQuestionNameAvailable(name);
    }

    public Boolean validateIndicatorName(String name) {
        return isIndicatorNameAvailable(name);
    }



    private String getEvents(String selectAttribute, String source, String platform, String action, String session, Integer timestampBegin, Integer timestampEnd, String sortDirection){
        String returnValue;

        try {
            ObjectMapper mapper = new ObjectMapper();

            String whereQuery = "";
            if (source != null && !isStringEmpty(source)) {
                String[] sources = source.split(",");

                if(sources.length > 1)
                    whereQuery = " where source in ('" + StringUtils.join(sources,"','") + "')";
                else
                    whereQuery = " where source = '" + source + "'";
            }

            if (platform != null && !isStringEmpty(platform)) {
                String combiningClause = isStringEmpty(whereQuery) ? " where" : " and";

                String[] platforms = platform.split(",");

                if(platforms.length > 1)
                    whereQuery += combiningClause + " platform in ('" + StringUtils.join(platforms,"','") + "')";
                else
                    whereQuery += combiningClause + " platform = '" + platform + "'";
            }

            if (action != null && !isStringEmpty(action)) {
                String combiningClause = isStringEmpty(whereQuery) ? " where" : " and";

                String[] actions = action.split(",");

                if(actions.length > 1)
                    whereQuery += combiningClause + " action in ('" + StringUtils.join(actions,"','") + "')";
                else
                    whereQuery += combiningClause + " action = '" + action + "'";
            }

            if (session != null && !isStringEmpty(session)) {
                String combiningClause = isStringEmpty(whereQuery) ? " where" : " and";

                String[] sessions = session.split(",");

                if(sessions.length > 1)
                    whereQuery += combiningClause + " session in ('" + StringUtils.join(sessions,"','") + "')";
                else
                    whereQuery += combiningClause + " session = '" + session + "'";
            }

            if (timestampBegin != null && timestampBegin > 0) {
                String combiningClause = isStringEmpty(whereQuery) ? " where" : " and";
                whereQuery +=  combiningClause + " timestamp >= " + timestampBegin;
            }

            if (timestampEnd != null && timestampEnd > 0) {
                String combiningClause = isStringEmpty(whereQuery) ? " where" : " and";
                whereQuery +=  combiningClause + " timestamp <= " + timestampEnd;
            }

            String sortQuery = "";
            if (sortDirection != null && !isStringEmpty(sortDirection))
                sortQuery = " order by " + selectAttribute + " " + sortDirection;

            String query = "select distinct " + selectAttribute + " from OpenLAPEvent" + whereQuery + sortQuery;

            List<String> platformList = (List<String>)executeHQLQueryRaw(query);

            returnValue = mapper.writeValueAsString(platformList);

        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            return null;
        }

        return returnValue;
    }

    private String getIndicatorRequestCode(Triad triad) {
        String visFrameworkScript = "";
        try{
            visFrameworkScript = performGetRequest(visualizationURL + "/frameworks/" + triad.getVisualizationReference().getFrameworkId() + "/methods/"+ triad.getVisualizationReference().getMethodId() + "/frameworkScript");
            visFrameworkScript = decodeURIComponent(visFrameworkScript);
        } catch (Exception exc) {
            throw new ItemNotFoundException(exc.getMessage(),"1");
        }


        String indicatorRequestCode = "<table id='wait_"+triad.getId()+"' style='width: 96%;height: 96%;text-align: center;'><tr><td><img style='-webkit-user-select: none' src='https://www.microsoft.com/about/corporatecitizenship/en-us/youthspark/computerscience/teals/images/loading.gif'> " +
                " <br>Please wait the indicator is being processed</td></tr></table> " +
                visFrameworkScript +
                "<script type=\"text/javascript\"> " +
//                " google.setOnLoadCallback(draw_"+triad.getId()+"); " +
//                "   function draw_"+triad.getId()+"() { " +
                "$(document).ready(function() {var xmlhttp_"+triad.getId()+"; " +
                "  if (window.XMLHttpRequest) { xmlhttp_"+triad.getId()+" = new XMLHttpRequest(); } else { xmlhttp_"+triad.getId()+" = new ActiveXObject('Microsoft.XMLHTTP'); } " +
                "  xmlhttp_"+triad.getId()+".onreadystatechange = function (xmlhttp_"+triad.getId()+") {  " +
                "   if (xmlhttp_"+triad.getId()+".currentTarget.readyState == 4) {  " +
                "    $('#wait_"+triad.getId()+"').hide(); " +
                "    $('#main_"+triad.getId()+"').parent().parent().css('overflow','hidden'); " +
                "    $('#main_"+triad.getId()+"').show(); " +
                "    var decResult_"+triad.getId()+" = decodeURIComponent(xmlhttp_"+triad.getId()+".currentTarget.responseText); " +
                "    $('#td_"+triad.getId()+"').append(decResult_"+triad.getId()+"); " +
                "   } " +
                "  }; " +
                "  xmlhttp_"+triad.getId()+".open('GET', '" + indicatorExecutionURL + "?tid="+triad.getId()+"&width='+$('#main_"+triad.getId()+"').parent().width()+'&height='+$('#main_"+triad.getId()+"').parent().height(), true);  " +
                "  xmlhttp_"+triad.getId()+".timeout = 300000; " +
                "  xmlhttp_"+triad.getId()+".send(); });" +
//                "   } " +
                "</script> " +
                "<table id='main_"+triad.getId()+"'><tbody><tr> " +
                " <td id='td_"+triad.getId()+"' style='text-align:-webkit-center;text-align:-moz-center;text-align:-o-center;text-align:-ms-center;'></td></tr></tbody></table> ";

        return  indicatorRequestCode;
    }

    private String getQuestionRequestCode(Set<Triad> triads){

        StringBuilder requestCode = new StringBuilder();

        if(triads.size()>0) {
            requestCode.append("<table style='width:100%;height:100%'>");
            int count = 1;

            for (Triad triad : triads) {

                if(count%2 == 1) requestCode.append("<tr class='questionRows'>");

                requestCode.append("<td>");
                requestCode.append(getIndicatorRequestCode(triad));
                requestCode.append("</td>");

                if(count%2 == 0) requestCode.append("</tr>");

                count++;
            }

            //adding  the closing tr if the number of triads are odd
            if(count%2 == 0) requestCode.append("</tr>");

            requestCode.append("</table>");
        }
        else{
            requestCode.append("No indicators assoicated with the question.");
        }


        return requestCode.toString();
    }

    private boolean setQuestionTriadMapping(long questionId, long triadId){
        boolean isSuccess = true;

        String queryString = "INSERT INTO question_triad (question_id, triad_id)" +
                " VALUES ("+ questionId + "," + triadId + ")";

        try {
            insertSQLQueryRaw(queryString);
        }
        catch (Exception exc){
            isSuccess = false;
        }

        return isSuccess;
    }

    //endregion

    //region Automatic Port Configuration generation
    public OpenLAPPortConfig generateDefaultPortConfiguration(List<OpenLAPColumnConfigData> senderColumns, List<OpenLAPColumnConfigData> receiverColumns) throws OpenLAPDateSetMappingException {
        ArrayList<OpenLAPPortMapping> mapping = new ArrayList<OpenLAPPortMapping>();

        for (OpenLAPColumnConfigData receiverColumn : receiverColumns) {
            OpenLAPColumnConfigData selectedSenderColumn = null;
            for (OpenLAPColumnConfigData senderColumn : senderColumns) {
                if (receiverColumn.getType() == senderColumn.getType()) {
                    selectedSenderColumn = senderColumn;

                    mapping.add(new OpenLAPPortMapping(senderColumn, receiverColumn));

                    senderColumns.remove(senderColumn);
                    break;
                }
            }

            if (selectedSenderColumn == null)
                throw new OpenLAPDateSetMappingException("No mapping possible for the '" + receiverColumn.getId() + "' column.");
        }

        OpenLAPPortConfig config = new OpenLAPPortConfig(mapping);
        return config;
    }

    public OpenLAPPortConfig generateDefaultPortConfiguration(OpenLAPDataSet sender, OpenLAPDataSet receiver) throws OpenLAPDateSetMappingException {
        List<OpenLAPColumnConfigData> senderColumns = sender.getColumnsConfigurationData(true);
        List<OpenLAPColumnConfigData> receiverColumns = receiver.getColumnsConfigurationData(true);

        return generateDefaultPortConfiguration(senderColumns, receiverColumns);
    }
    //endregion

    //region Question repository methods

    public Question getQuestionById(long id) throws ItemNotFoundException {
        Question result = questionRepository.findOne(id);
        if (result == null || id <= 0) {
            throw new ItemNotFoundException("Question with id: {" + id + "} not found","2");
        } else {
            //log.info("getQuestionById returns " + result.toString());
            return result;
        }
    }

    public Question saveQuestion(Question question) {

        //Question questionToSave = new Question(question.getName(), question.getIndicatorCount(), question.getGoal(), question.getIndicators());
        try {
            return questionRepository.save(question);
        } catch (DataIntegrityViolationException sqlException) {
            sqlException.printStackTrace();
            throw new BadRequestException("Question already exists.", "1");
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException(e.getMessage(),  "1");
        }
    }

    public List<Question> getAllQuestions() {
        ArrayList<Question> result = new ArrayList<Question>();
        questionRepository.findAll().forEach(result::add);
        return result;
    }

    public List<Question> searchQuestions(String searchParameter, boolean exactSearch,
                                          String colName, String sortDirection, boolean sort) {
        ArrayList<Question> result = new ArrayList<Question>();

        Sort.Direction querySortDirection;
        switch(sortDirection.toLowerCase()){
            case "asc":
                querySortDirection = Sort.Direction.ASC;
                break;
            case "desc":
                querySortDirection = Sort.Direction.DESC;
                break;
            default:
                querySortDirection = Sort.Direction.ASC;
        }

        if(exactSearch) {
            if(sort)
                questionRepository.findByName(searchParameter, new Sort(querySortDirection, colName)).forEach(result::add);
            else
                questionRepository.findByName(searchParameter).forEach(result::add);
        }
        else {
            if(sort)
                questionRepository.findByNameContaining(searchParameter, new Sort(querySortDirection, colName)).forEach(result::add);
            else
                questionRepository.findByNameContaining(searchParameter).forEach(result::add);
        }
        return result;
    }

    public List<Question> getSortedQuestions(String colName, String sortDirection, boolean sort) {
        ArrayList<Question> result = new ArrayList<Question>();

        Sort.Direction querySortDirection;
        switch(sortDirection.toLowerCase()){
            case "asc":
                querySortDirection = Sort.Direction.ASC;
                break;
            case "desc":
                querySortDirection = Sort.Direction.DESC;
                break;
            default:
                querySortDirection = Sort.Direction.ASC;
        }

            if(sort)
                questionRepository.findAll(new Sort(querySortDirection, colName)).forEach(result::add);
            else
                questionRepository.findAll().forEach(result::add);

        return result;
    }

    public Boolean isQuestionNameAvailable(String name) throws ItemNotFoundException {
        List<Question> result = questionRepository.findByName(name);

        if (result == null) {
            return true;
        } else {
            if(result.size()>0)
                return false;
            else
                return true;
        }
    }

    /*public void deleteQuestion(long questionId) {
        if (!questionRepository.exists(questionId)) {
            throw new ItemNotFoundException("Question with id = {" + questionId + "} not found.","2");
        }
        questionRepository.delete(questionId);
    }*/

    //endregion

    //region Indicator repository methods

    public Indicator getIndicatorById(long id) throws ItemNotFoundException {
        Indicator result = indicatorRepository.findOne(id);
        if (result == null || id <= 0) {
            throw new ItemNotFoundException("Indicator with id: {" + id + "} not found","1");
        } else {
            //log.info("getIndicatorById returns " + result.toString());
            return result;
        }
    }

    public Boolean isIndicatorNameAvailable(String name) throws ItemNotFoundException {
        List<Indicator> result = indicatorRepository.findByName(name);
        if (result == null) {
            return true;
        } else {
            if(result.size()>0)
                return false;
            else
                return true;
        }
    }

    public Indicator saveIndicator(Indicator indicator) {
        try {
            return indicatorRepository.save(indicator);
        } catch (DataIntegrityViolationException sqlException) {
            sqlException.printStackTrace();
            throw new BadRequestException("Indicator already exists.", "2");
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException(e.getMessage(), "2");
        }
    }

    /*public List<Indicator> getAllIndicators() {
        ArrayList<Indicator> result = new ArrayList<Indicator>();
        indicatorRepository.findAll().forEach(result::add);
        return result;
    }

    public List<Indicator> searchIndicators(String searchParameter, boolean exactSearch,
                                          String colName, String sortDirection, boolean sort) {
        ArrayList<Indicator> result = new ArrayList<Indicator>();

        Sort.Direction querySortDirection;
        switch(sortDirection.toLowerCase()){
            case "asc":
                querySortDirection = Sort.Direction.ASC;
                break;
            case "desc":
                querySortDirection = Sort.Direction.DESC;
                break;
            default:
                querySortDirection = Sort.Direction.ASC;
        }

        if(exactSearch) {
            if(sort)
                indicatorRepository.findByName(searchParameter, new Sort(querySortDirection, colName)).forEach(result::add);
            else
                indicatorRepository.findByName(searchParameter).forEach(result::add);
        }
        else {
            if(sort)
                indicatorRepository.findByNameContaining(searchParameter, new Sort(querySortDirection, colName)).forEach(result::add);
            else
                indicatorRepository.findByNameContaining(searchParameter).forEach(result::add);
        }
        return result;
    }

    public void deleteIndicator(long indicatorId) {
        if (!indicatorRepository.exists(indicatorId)) {
            throw new ItemNotFoundException("Indicator with id = {" + indicatorId + "} not found.","2");
        }
        indicatorRepository.delete(indicatorId);
    }*/

    //endregion

    //region TriadCache repository methods

    public TriadCache getCacheByTriadId(long triadId) throws ItemNotFoundException {
        List<TriadCache> result = triadCacheRepository.findByTriadId(triadId);

        if (result == null) {
            return null;
        } else {
            if(result.size()>0)
                return result.get(0);
            else
                return null;
        }
    }

    public TriadCache saveTriadCache(long triadId, String code) {

        TriadCache triadCache = new TriadCache(triadId, code);
        try {
            return triadCacheRepository.save(triadCache);
        } catch (DataIntegrityViolationException sqlException) {
            sqlException.printStackTrace();
            throw new BadRequestException("Cache already exists.", "1");
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException(e.getMessage(),  "1");
        }
    }

    //endregion

    //region Query Execution and Data Transformation

    public OpenLAPDataSet executeHQLQuery(String queryString) {
        List<?> dataList = executeHQLQueryRaw(queryString);

        OpenLAPDataSet ds = transformHQLToOpenLAPDatSet(dataList, queryString);

        return ds;
    }

    public List<?> executeHQLQueryRaw(String queryString) {
        Session session = null;
        Transaction transaction = null;
        List<?> dataList;

        try {

            session = factory.openSession();

            transaction = session.beginTransaction();

            Query query = session.createQuery(queryString);

            dataList = query.list();

            transaction.commit();
        }
        catch (Exception exc){
            transaction.rollback();
            dataList = null;
        }
        finally {
            if (session != null)
                session.close();
        }

        return dataList;
    }

    public OpenLAPDataSet transformHQLToOpenLAPDatSet(List<?> dataList, String hqlQuery) {
        //Extracting column names from the query
        String[] fieldsName;
        int indexFrom = hqlQuery.indexOf("from");
        if (indexFrom > 0) {
            String cols = hqlQuery.substring(hqlQuery.indexOf("select") + 7, indexFrom);
            cols = cols.replaceAll("\\s", "");
            fieldsName = cols.split(",", -1);
        } else
            fieldsName = new String[]{};

        OpenLAPDataSet ds;

        if (dataList.size() > 0) {

            ds = new OpenLAPDataSet();

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
                                ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(fieldsName[i], OpenLAPColumnDataType.INTEGER, true));
                                break;
                            case "java.lang.Boolean":
                                ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(fieldsName[i], OpenLAPColumnDataType.BOOLEAN, true));
                                break;
                            case "java.lang.Byte":
                                ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(fieldsName[i], OpenLAPColumnDataType.BYTE, true));
                                break;
                            case "java.lang.Character":
                                ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(fieldsName[i], OpenLAPColumnDataType.CHAR, true));
                                break;
                            case "java.lang.Float":
                                ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(fieldsName[i], OpenLAPColumnDataType.FLOAT, true));
                                break;
                            case "java.sql.Timestamp":
                                ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(fieldsName[i], OpenLAPColumnDataType.LOCAL_DATE_TIME, true));
                                break;
                            case "java.lang.Long":
                                ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(fieldsName[i], OpenLAPColumnDataType.LONG, true));
                                break;
                            case "java.lang.Short":
                                ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(fieldsName[i], OpenLAPColumnDataType.SHORT, true));
                                break;
                            case "java.lang.String":
                                ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(fieldsName[i], OpenLAPColumnDataType.STRING, true));
                                break;
                            default:
                                break;
                        }

                    } catch (OpenLAPDataColumnException e) {
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
                    ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("cId", OpenLAPColumnDataType.INTEGER, true));
                    ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("type", OpenLAPColumnDataType.STRING, true));
                    ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("major", OpenLAPColumnDataType.STRING, true));
                    ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("minor", OpenLAPColumnDataType.STRING, true));
                } catch (OpenLAPDataColumnException e) {
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
                    ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("eId", OpenLAPColumnDataType.INTEGER, true));
                    ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("eventFk", OpenLAPColumnDataType.INTEGER, true));
                    ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("entityKey", OpenLAPColumnDataType.STRING, true));
                    ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("entityValue", OpenLAPColumnDataType.STRING, true));
                } catch (OpenLAPDataColumnException e) {
                    e.printStackTrace();
                }

                for (Object row : dataList) {
                    OpenLAPEntity obj = (OpenLAPEntity) row;

                    ds.getColumns().get("eId").getData().add(obj.geteId());
                    ds.getColumns().get("eventFk").getData().add(obj.getEventByEventFk().getEventId());
                    ds.getColumns().get("entityKey").getData().add(obj.getEntityKey());
                    ds.getColumns().get("entityValue").getData().add(obj.getValue());
                }
            } else if (dataFirst instanceof OpenLAPEvent) {
                try {
                    ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("eventId", OpenLAPColumnDataType.INTEGER, true));
                    ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("uId", OpenLAPColumnDataType.INTEGER, true));
                    ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("cId", OpenLAPColumnDataType.INTEGER, true));
                    ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("timestamp", OpenLAPColumnDataType.INTEGER, true));
                    ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("session", OpenLAPColumnDataType.STRING, true));
                    ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("action", OpenLAPColumnDataType.STRING, true));
                    ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("platform", OpenLAPColumnDataType.STRING, true));
                    ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("source", OpenLAPColumnDataType.STRING, true));
                } catch (OpenLAPDataColumnException e) {
                    e.printStackTrace();
                }

                for (Object row : dataList) {
                    OpenLAPEvent obj = (OpenLAPEvent) row;

                    ds.getColumns().get("eventId").getData().add(obj.getEventId());
                    ds.getColumns().get("uId").getData().add(obj.getUsersByUId().getuId());
                    ds.getColumns().get("cId").getData().add(obj.getCategoryByCId().getcId());
                    ds.getColumns().get("timestamp").getData().add(obj.getTimestamp());
                    ds.getColumns().get("session").getData().add(obj.getSession());
                    ds.getColumns().get("action").getData().add(obj.getAction());
                    ds.getColumns().get("platform").getData().add(obj.getPlatform());
                    ds.getColumns().get("source").getData().add(obj.getSource());
                }
            } else if (dataFirst instanceof OpenLAPUsers) {
                try {
                    ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("uId", OpenLAPColumnDataType.INTEGER, true));
                    ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("name", OpenLAPColumnDataType.STRING, true));
                } catch (OpenLAPDataColumnException e) {
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
                            ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(fieldsName[0], OpenLAPColumnDataType.INTEGER, true));
                            break;
                        case "java.lang.Boolean":
                            ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(fieldsName[0], OpenLAPColumnDataType.BOOLEAN, true));
                            break;
                        case "java.lang.Byte":
                            ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(fieldsName[0], OpenLAPColumnDataType.BYTE, true));
                            break;
                        case "java.lang.Character":
                            ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(fieldsName[0], OpenLAPColumnDataType.CHAR, true));
                            break;
                        case "java.lang.Float":
                            ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(fieldsName[0], OpenLAPColumnDataType.FLOAT, true));
                            break;
                        case "java.sql.Timestamp":
                            ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(fieldsName[0], OpenLAPColumnDataType.LOCAL_DATE_TIME, true));
                            break;
                        case "java.lang.Long":
                            ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(fieldsName[0], OpenLAPColumnDataType.LONG, true));
                            break;
                        case "java.lang.Short":
                            ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(fieldsName[0], OpenLAPColumnDataType.SHORT, true));
                            break;
                        case "java.lang.String":
                            ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(fieldsName[0], OpenLAPColumnDataType.STRING, true));
                            break;
                        default:
                            break;
                    }

                } catch (OpenLAPDataColumnException e) {
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



    public OpenLAPDataSet executeIndicatorQuery(String queryString, OpenLAPPortConfig methodMapping, int rowCount) {
        Session session = null;
        Transaction transaction = null;
        OpenLAPDataSet ds;

        try {

            session = factory.openSession();

            transaction = session.beginTransaction();

            Query query = session.createQuery(queryString);

            if(rowCount>0)
                query.setMaxResults(rowCount);

            List<?>  dataList = query.list();

            ds = transformIndicatorQueryToOpenLAPDatSet(dataList, methodMapping);

            transaction.commit();
        }
        catch (Exception exc){
            transaction.rollback();
            ds = null;
        }
        finally {
            if (session != null)
                session.close();
        }

        return ds;
    }

    public OpenLAPDataSet transformIndicatorQueryToOpenLAPDatSet(List<?> dataList, OpenLAPPortConfig methodMapping) {
        OpenLAPDataSet ds;

        if (dataList.size() > 0) {

            ds = new OpenLAPDataSet();

            boolean containEntity = false;

            // Creating columns based on the query to method configuration
            try {
                for (OpenLAPColumnConfigData column : methodMapping.getOutputColumnConfigurationData()) {
                    ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(column.getId(), column.getType(), false, column.getTitle(), column.getDescription()));

                    //checking if any mapping have false which means a column needs data from Entity table
                    if(!column.isRequired())
                        containEntity = true;
                }
            } catch (OpenLAPDataColumnException e) {
                e.printStackTrace();
            }

            OpenLAPEvent preEvent = null;
            OpenLAPEvent curEvent = null;

            Map<String, String> preEntities = new HashMap<String, String>();
            OpenLAPEntity curEntity = null;

            for (Object row : dataList) {
                if(containEntity) {
                    Object[] rowArray = (Object[]) row;

                    curEvent = (OpenLAPEvent) rowArray[0];
                    curEntity = (OpenLAPEntity) rowArray[1];

                    if(preEvent==null || preEvent.getEventId() == curEvent.getEventId()){
                        preEvent = curEvent;
                        preEntities.put(curEntity.getEntityKey(),curEntity.getValue());
                        continue;
                    }
                }
                else{
                    preEvent = (OpenLAPEvent) row;
                }

                try {
                    for (OpenLAPColumnConfigData column : methodMapping.getOutputColumnConfigurationData()) {
                        if (column.isRequired()) {
                            //if the isRequired is true than it means that column is related to Event class
                            switch (column.getId()) {
                                case "eventId":
                                    ds.getColumns().get("eventId").getData().add(preEvent.getEventId());
                                    break;
                                case "timestamp":
                                    ds.getColumns().get("timestamp").getData().add(preEvent.getTimestamp());
                                    break;
                                case "session":
                                    ds.getColumns().get("session").getData().add(preEvent.getSession());
                                    break;
                                case "action":
                                    ds.getColumns().get("action").getData().add(preEvent.getAction());
                                    break;
                                case "platform":
                                    ds.getColumns().get("platform").getData().add(preEvent.getPlatform());
                                    break;
                                case "source":
                                    ds.getColumns().get("source").getData().add(preEvent.getSource());
                                    break;
                                case "category":
                                    ds.getColumns().get("category").getData().add(preEvent.getCategoryByCId().getMinor());
                                    break;
                                case "user":
                                    ds.getColumns().get("user").getData().add(preEvent.getUsersByUId().getName());
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            //if the isRequired is false than it means that column is related to Entity class
                            //ds.getColumns().get(column.getId()).getData().add(preEntities.get(column.getId()));

                            switch (column.getType()) {
                                case BOOLEAN:
                                    try{ds.getColumns().get(column.getId()).getData().add(Boolean.parseBoolean(preEntities.get(column.getId()).toLowerCase()));}
                                    catch (Exception exc) {ds.getColumns().get(column.getId()).getData().add(false);}
                                    break;
                                case BYTE:
                                    try{ds.getColumns().get(column.getId()).getData().add(Byte.parseByte(preEntities.get(column.getId())));}
                                    catch (Exception exc) {ds.getColumns().get(column.getId()).getData().add(-128);}
                                    break;
                                case LOCAL_DATE_TIME:
                                    try{ds.getColumns().get(column.getId()).getData().add(LocalDateTime.parse(preEntities.get(column.getId())));}
                                    catch (Exception exc) {ds.getColumns().get(column.getId()).getData().add(LocalDateTime.MIN);}
                                    break;
                                case FLOAT:
                                    try{ds.getColumns().get(column.getId()).getData().add(Float.parseFloat(preEntities.get(column.getId())));}
                                    catch (Exception exc) {ds.getColumns().get(column.getId()).getData().add(-3.40282347E+38F);}
                                    break;
                                case LONG:
                                    try{ds.getColumns().get(column.getId()).getData().add(Long.parseLong(preEntities.get(column.getId())));}
                                    catch (Exception exc) {ds.getColumns().get(column.getId()).getData().add(-2147483648);}
                                    break;
                                case INTEGER:
                                    try{ds.getColumns().get(column.getId()).getData().add(Integer.parseInt(preEntities.get(column.getId())));}
                                    catch (Exception exc) {ds.getColumns().get(column.getId()).getData().add(-2147483648);}
                                    break;
                                case SHORT:
                                    try{ds.getColumns().get(column.getId()).getData().add(Short.parseShort(preEntities.get(column.getId())));}
                                    catch (Exception exc) {ds.getColumns().get(column.getId()).getData().add(false);}
                                    break;
                                case CHAR:
                                    try{ds.getColumns().get(column.getId()).getData().add(preEntities.get(column.getId()).charAt(0));}
                                    catch (Exception exc) {ds.getColumns().get(column.getId()).getData().add("");}
                                    break;
                                case STRING:
                                    ds.getColumns().get(column.getId()).getData().add(preEntities.get(column.getId()));
                                    break;
                                default:
                                    ds.getColumns().get(column.getId()).getData().add(preEntities.get(column.getId()));
                                    break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                if(containEntity) {
                    preEvent = curEvent;
                    preEntities.clear();
                    preEntities.put(curEntity.getEntityKey(),curEntity.getValue());
                }
            }
        } else {
            ds = null;
        }

        return ds;
    }



    public OpenLAPDataSet executeSQLQuery(String queryString) {

        List<Map<String, Object>> dataList = executeSQLQueryRaw(queryString);

        OpenLAPDataSet ds = transformSQLToOpenLAPDatSet(dataList);

        return ds;
    }

    public List<Map<String, Object>> executeSQLQueryRaw(String queryString) {
        Session session = null;
        Transaction transaction = null;
        List<Map<String, Object>> dataList;

        try {

            session = factory.openSession();

            transaction = session.beginTransaction();

            Query query = session.createSQLQuery(queryString);
            query.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);

            dataList = query.list();

            transaction.commit();
        }
        catch (Exception exc){
            transaction.rollback();
            dataList = null;
        }
        finally {
            if (session != null)
                session.close();
        }

        return dataList;
    }

    public boolean insertSQLQueryRaw(String queryString) {
        Session session = null;
        Transaction transaction = null;

        boolean isSuccess = true;

        try {

            session = factory.openSession();

            transaction = session.beginTransaction();

            Query query = session.createSQLQuery(queryString);

            query.executeUpdate();

            transaction.commit();
        }
        catch (Exception exc){
            transaction.rollback();
            isSuccess = false;
        }
        finally {
            if (session != null)
                session.close();
        }

        return isSuccess;
    }

    public OpenLAPDataSet transformSQLToOpenLAPDatSet(List<Map<String, Object>> dataList) {
        OpenLAPDataSet ds;

        if (dataList != null && dataList.size() > 0) {

            ds = new OpenLAPDataSet();

            Map<String, Object> dataFirst = (Map<String, Object>) dataList.get(0);

            for (Map.Entry<String, Object> entry : dataFirst.entrySet()) {
                Class cls = entry.getValue().getClass();
                String className = cls.getName();

                try {

                    switch (className) {
                        case "java.lang.Integer":
                            ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(entry.getKey(), OpenLAPColumnDataType.INTEGER, true));
                            break;
                        case "java.lang.Boolean":
                            ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(entry.getKey(), OpenLAPColumnDataType.BOOLEAN, true));
                            break;
                        case "java.lang.Byte":
                            ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(entry.getKey(), OpenLAPColumnDataType.BYTE, true));
                            break;
                        case "java.lang.Character":
                            ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(entry.getKey(), OpenLAPColumnDataType.CHAR, true));
                            break;
                        case "java.lang.Float":
                            ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(entry.getKey(), OpenLAPColumnDataType.FLOAT, true));
                            break;
                        case "java.sql.Timestamp":
                            ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(entry.getKey(), OpenLAPColumnDataType.LOCAL_DATE_TIME, true));
                            break;
                        case "java.lang.Long":
                            ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(entry.getKey(), OpenLAPColumnDataType.LONG, true));
                            break;
                        case "java.lang.Short":
                            ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(entry.getKey(), OpenLAPColumnDataType.SHORT, true));
                            break;
                        case "java.lang.String":
                            ds.addOpenLAPDataColumn(OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(entry.getKey(), OpenLAPColumnDataType.STRING, true));
                            break;
                        default:
                            break;
                    }

                } catch (OpenLAPDataColumnException e) {
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

    //region Basic HTTP Requests and encoding/decoding

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

    public <T> T performJSONPostRequest(String url, String jsonContent, Class<T> type) throws Exception{
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Accept","application/json;charset=utf-8");

        HttpEntity<String> entity = new HttpEntity<String>(jsonContent,headers);

        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));

        T result = restTemplate.postForObject(url, entity, type);

        return result;
    }

    /*public String performJSONPostRequest(String url, String jsonContent) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
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
    }*/

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

    public String decodeURIComponent(String s) {
        if (s == null) {
            return null;
        }

        String result = null;

        try {
            result = URLDecoder.decode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }

    public String encodeURIComponent(String s) {
        String result = null;

        try {
            result = URLEncoder.encode(s, "UTF-8")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            result = s;
        }
        return result;
    }
    //endregion

    //region Static methods
    public static boolean isStringEmpty( final String s ) {
        return s == null || s.trim().isEmpty();
    }
    public static int compareIntegers(long x, long y){
        if (x == y)
            return 0;
        else if (x > y)
            return 1;
        else
            return -1;
    }
    //endregion
}
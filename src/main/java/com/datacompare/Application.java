/**
 * Startup classs for this application
 *
 *
 * @author      Harnath Valeti
 * @author      Madhu Athinarapu
 * @version     1.0
 * @since       1.0
 */
package com.datacompare;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.datacompare.util.AWSUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.datacompare.model.AppProperties;
import com.datacompare.service.CompareService;
import com.datacompare.util.FormatUtil;
import org.springframework.core.env.Environment;

/**
 * Application Start!
 *
 */
@SpringBootApplication
public class Application implements ApplicationRunner {
	
	private Logger logger = LoggerFactory.getLogger(Application.class);

	@Autowired
	private Environment env;

    public static void main( String[] args ) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String timeStampStr=timestamp.toString();
		System.setProperty("logtimestamp", timeStampStr);
		SpringApplication app = new SpringApplication(Application.class);
		app.run(args);
    }
 
    @Override
    public void run(ApplicationArguments args) throws Exception {
    	long startTime=System.currentTimeMillis();
    	logger.debug("Command-line arguments: {}", Arrays.toString(args.getSourceArgs()));
        logger.debug("Non Option Args: {}", args.getNonOptionArgs());
        logger.info("Option Names: {}", args.getOptionNames());

        if(!args.getOptionNames().isEmpty()) {
        	
        	try {

				int subStringlength=4;
				if(args.getSourceArgs() !=null && args.getSourceArgs().length>0)
					subStringlength=Arrays.toString(args.getSourceArgs()).length();
                String sourceArgs = StringUtils.substring(Arrays.toString(args.getSourceArgs()), 3, (subStringlength-1));
                String[] sargs = sourceArgs.split(", --");
                Map<String, String> arguments = new HashMap<String, String>();
                for (int i = 0; i < sargs.length; i++) {
        			String[] keyVal = sargs[i].split("=", 2);
        			arguments.put(keyVal[0], keyVal[1]);
        		}
                AppProperties appProperties = new AppProperties();
        		String connectionType = arguments.get("connectionType");
        		connectionType = (connectionType != null && !connectionType.isEmpty()) ? connectionType : "All";
        		appProperties.setConnectionType(connectionType);
        		String reportType = arguments.get("reportType");
        		reportType = (reportType != null && !reportType.isEmpty()) ? reportType : "Detail";
        		appProperties.setReportType(reportType);
        		appProperties.setSourceDBType(arguments.get("sourceDBType").toUpperCase());
				appProperties.setRegion(arguments.get("secMgrRegion"));
				appProperties.setSrcDBSecretManagerEndPoint(arguments.get("srcDBSecretMgrEndPoint"));
				appProperties.setSrcDBSecretName(arguments.get("srcDBSecretName"));
				appProperties.setTgtDBSecretManagerEndPoint(arguments.get("tgtDBSecretMgrEndPoint"));
				appProperties.setTgtDBSecretName(arguments.get("tgtDBSecretName"));
				
				if(!StringUtils.isEmpty(arguments.get("connectionPoolMaxSize"))) {
					appProperties.setConnectionPoolMaxSize(Integer.parseInt(arguments.get("connectionPoolMaxSize")));
				}
				if(!StringUtils.isEmpty(arguments.get("connectionPoolMinSize"))) {
					appProperties.setConnectionPoolMinSize(Integer.parseInt(arguments.get("connectionPoolMinSize")));
				}
				
				logger.info("Properties: "+ appProperties);
        		if("All".equals(appProperties.getConnectionType())) {
        			setDatabaseProperties(arguments, appProperties);
        		} else if("JDBC".equals(appProperties.getConnectionType())) {
        			setJdbcDbProperties(arguments, appProperties);
        		}
        		setSchemaTableProperties(arguments, appProperties);
        		setOtherProperties(arguments, appProperties);
				setPrimaryKeys(appProperties);
        		//Filter
        		String filterType = arguments.get("filterType");
        		filterType = (filterType != null && !filterType.isEmpty()) ? filterType : "Sql";
        		appProperties.setFilterType(filterType);
        		appProperties.setFilter(arguments.get("filter"));
				appProperties.setTrustStorePath(env.getProperty("jdbc.truststore.password"));
				appProperties.setTrsutStorePassword(env.getProperty("jdbc.truststore.password"));
        		CompareService compareService = new CompareService();
        		compareService.startService(appProperties); 
 				
			} catch (Exception e) {
				logger.error("Error", e);  
			}
        	logger.info("######Total Execution Time is####### "+(System.currentTimeMillis()-startTime));
        	//Exit the process normally.
        	System.exit(0); 
        }
    }

	private void setPrimaryKeys(AppProperties appProperties) {
		if (appProperties != null) {
			String keyDetails = appProperties.getPrimaryKeys();
			if (keyDetails != null) {
				HashMap<String, String> map = new HashMap<String, String>();
				String[] tableKeys = keyDetails.split(";");
				if (tableKeys != null && tableKeys.length > 0) {
					for (int i = 0; i < tableKeys.length; i++) {
						String tableKey = tableKeys[0];
						String[] pKeys = tableKey.split(":");
						if (pKeys != null && pKeys.length > 0) {
							map.put(pKeys[0], pKeys[1]);
						}
					}
				}
				if (!map.isEmpty()) {
					appProperties.setPrimaryKeyMap(map);
				}
			}
		}
	}
	/**
     * This will set individual database details like host, port, database, user and password
     * 
     * @param arguments
     * @param appProperties
     */
    private void setDatabaseProperties(Map<String, String> arguments, AppProperties appProperties) {
    	
        //Source DB Details
		appProperties.setSourceIP(arguments.get("sourceHost"));
		if(arguments.get("sourcePort")!=null && arguments.get("sourcePort").length()>0)
		appProperties.setSourcePort(Integer.parseInt(arguments.get("sourcePort")));
		appProperties.setSourceDBName(arguments.get("sourceDBName"));
		appProperties.setSourceUserPassword(arguments.get("sourcePassword"));
		appProperties.setSourceUserName(arguments.get("sourceUsername"));
		String sourceDBService = arguments.get("sourceDBService");
		sourceDBService = (sourceDBService != null && sourceDBService.trim().length() > 0) ? sourceDBService : "Service";
		appProperties.setSourceSSLRequire((FormatUtil.getIntValue(arguments.get("sourceSSLRequire"), 0, 0) == 1) ? true : false);
		appProperties.setSourceDBService(sourceDBService);

		//Target DB Details
		appProperties.setTargetIP(arguments.get("targetHost"));
		if(arguments.get("targetPort")!=null && arguments.get("targetPort").length()>0)
		appProperties.setTargetPort(Integer.parseInt(arguments.get("targetPort")));
		appProperties.setTargetDBName(arguments.get("targetDBName"));
		appProperties.setTargetSSLRequire((FormatUtil.getIntValue(arguments.get("targetSSLRequire"), 0, 0) == 1) ? true : false);
		appProperties.setTargetUserName(arguments.get("targetUsername"));
		appProperties.setTargetUserPassword(arguments.get("targetPassword"));
		if(appProperties.getSrcDBSecretName()!=null && appProperties.getSrcDBSecretManagerEndPoint()!=null && appProperties.getRegion()!=null)
		{
			appProperties.setSourceDB(true);
			appProperties = new AWSUtil().getSecrets(appProperties);
		}
		if(appProperties.getTgtDBSecretName()!=null && appProperties.getTgtDBSecretManagerEndPoint()!=null && appProperties.getRegion()!=null)
		{
			appProperties.setSourceDB(false);
			appProperties = new AWSUtil().getSecrets(appProperties);
		}
	}
    
    /**
     * This will set JDBC details, user and password.
     * 
     * @param arguments
     * @param appProperties
     */
    private void setJdbcDbProperties(Map<String, String> arguments, AppProperties appProperties) {
    	
        //Source DB Details
		appProperties.setSourceJdbcUrl(arguments.get("sourceJdbcUrl"));
		appProperties.setSourceUserName(arguments.get("sourceUsername"));
		appProperties.setSourceUserPassword(arguments.get("sourcePassword"));
		
		//Target DB Details
		appProperties.setTargetJdbcUrl(arguments.get("targetJdbcUrl"));
		appProperties.setTargetUserName(arguments.get("targetUsername"));
		appProperties.setTargetUserPassword(arguments.get("targetPassword"));
    }
    
    /**
     * This will set schema, table and column details.
     * 
     * @param arguments
     * @param appProperties
     */
    private void setSchemaTableProperties(Map<String, String> arguments, AppProperties appProperties) {
    	
		//Table Details
		appProperties.setSchemaName(arguments.get("schemaName"));
		appProperties.setTableName(arguments.get("tableName"));
		appProperties.setIgnoreTables((FormatUtil.getIntValue(arguments.get("ignoreTables"), 0, 0) == 1) ? true : false);
		appProperties.setColumns(arguments.get("columns"));
		appProperties.setPrimaryKeys(arguments.get("primaryKey"));
		appProperties.setIgnoreColumns((FormatUtil.getIntValue(arguments.get("ignoreColumns"), 0, 0) == 1) ? true : false);
    }
    
    /**
     * This will set settings related to execution.
     * 
     * @param arguments
     * @param appProperties
     */
    private void setOtherProperties(Map<String, String> arguments, AppProperties appProperties) {
    	
		//Other Details
		appProperties.setFetchSize(FormatUtil.getIntValue(arguments.get("chunkSize"), 10000, 1000000)); 
		appProperties.setMaxDecimals(FormatUtil.getIntValue(arguments.get("maxDecimals"), 5, 10));
		appProperties.setMaxTextSize(FormatUtil.getIntValue(arguments.get("maxTextSize"), 500, 5000));
		appProperties.setMaxNoofThreads(FormatUtil.getIntValue(arguments.get("noofParallelChunks"), 1, 10));
		appProperties.setCompareOnlyDate((FormatUtil.getIntValue(arguments.get("compareOnlyDate"), 0, 0) == 1) ? true : false);
		appProperties.setDisplayCompleteData((FormatUtil.getIntValue(arguments.get("displayCompleteData"), 0, 0) == 1) ? true : false);

		String jobName = arguments.get("jobName");
		jobName = (jobName != null && !jobName.isEmpty()) ? jobName : "data_comparison_result";
		appProperties.setJobName(jobName);
		appProperties.setOutputFolderPath(arguments.get("outputFolderPath")); 
    }
}

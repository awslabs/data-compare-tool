package com.datacompare.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datacompare.model.AppProperties;
import com.datacompare.model.DatabaseInfo.dbType;

public class PropertiesUtil {

	public static Logger logger = LoggerFactory.getLogger("PropertiesUtil");
	
	/**
	 * 
	 * @return
	 */
	public static AppProperties readProperties() {
		
	    String sourceDBType = "ORACLE";
		
        if( sourceDBType.equalsIgnoreCase("ORACLE") ) {
        	
        	dbType.valueOf(sourceDBType.toUpperCase());
        	
        } else {
        	
        	logger.info("Please enter the correct source DB type");
        	
        	return null;
        }
	    
        /** Source */
	    String sourceIP = null;
	    int sourcePort = 0;
	    String sourceDBName = null;
	    String sourceUserName = null;
	    String sourceUserPassword = null;
	    
	    /** Target */
	    String targetIP = null;
	    int targetPort = 0;
	    String targetDBName = null;
	    String targetUserName = null;
	    String targetUserPassword = null;
	    
		String schemaName = null;
		String tableName = null;
		String ignoreColumns = null;
		
		int fetchSize = 0;
		int maxDecimals = 0;
		int maxNoofThreads = 0;
		String sqlFilter = null;
		
		Properties prop = new Properties();
		InputStream input = null;
	
		try {

			input = new FileInputStream("config.properties");

			// load a properties file
			prop.load(input);

			// get the property value
			sourceIP = prop.getProperty("source.oracle.ip.address").trim();
			sourcePort = Integer.parseInt(prop.getProperty("source.oracle.port").trim());
			sourceDBName = prop.getProperty("source.oracle.service.name").trim();
			sourceUserName = prop.getProperty("source.oracle.user").trim();
			sourceUserPassword = prop.getProperty("source.oracle.password").trim();
			
			targetIP = prop.getProperty("source.postgres.ip.address").trim();
			targetPort = Integer.parseInt(prop.getProperty("source.postgres.port").trim());
			targetDBName = prop.getProperty("source.postgres.service.name").trim();
			targetUserName = prop.getProperty("source.postgres.user").trim();
			targetUserPassword = prop.getProperty("source.postgres.password").trim();
			
			schemaName = prop.getProperty("schema.name").trim();
			tableName = prop.getProperty("table.name").trim();
			ignoreColumns = prop.getProperty("ignore.table.columns").trim();
			
			fetchSize = FormatUtil.getIntValue(prop.getProperty("database.fetch.size").trim(), 10, 0);
			maxDecimals = FormatUtil.getIntValue(prop.getProperty("decimals.comparison.size").trim(), 5, 0);
			maxNoofThreads = FormatUtil.getIntValue(prop.getProperty("max.noof.threads").trim(), 5, 0);
			sqlFilter = prop.getProperty("sql.filter").trim();

		} catch (IOException ex) {
			
			logger.error("Unable to load/read config.properties file", ex);
			
			return null; 
			
		} finally {
			
			if (input != null) {
				
				try {
					
					input.close();
					
				} catch (IOException e) {
					
					logger.error(e.getMessage(), e);
				}
			}
		}	
		
		AppProperties appProperties = new AppProperties();
		
		appProperties.setSourceDBType(sourceDBType);
		appProperties.setSchemaName(schemaName);
		appProperties.setSourceDBName(sourceDBName);
		appProperties.setSourceIP(sourceIP);
		appProperties.setSourcePort(sourcePort);
		appProperties.setSourceUserName(sourceUserName);
		appProperties.setSourceUserPassword(sourceUserPassword);
		appProperties.setTableName(tableName);
		appProperties.setTargetDBName(targetDBName);
		appProperties.setTargetIP(targetIP);
		appProperties.setTargetPort(targetPort);
		appProperties.setTargetUserName(targetUserName);
		appProperties.setTargetUserPassword(targetUserPassword);
		appProperties.setFetchSize(fetchSize); 
		appProperties.setMaxDecimals(maxDecimals);
		appProperties.setMaxNoofThreads(maxNoofThreads); 
		appProperties.setColumns(ignoreColumns); 
		appProperties.setFilter(sqlFilter); 
		
		return appProperties;
	}
}

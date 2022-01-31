/**
 * Model class for Application and database connection properties .
 *
 *
 * @author      Harnath Valeti
 * @author      Madhu Athinarapu
 * @version     1.0
 * @since       1.0
 */

package com.datacompare.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

public class AppProperties {
	@Autowired
	private Environment env;

	private boolean isSourceDB;
	private String region;
	private String srcDBSecretManagerEndPoint;
	private String srcDBSecretName;
	private String tgtDBSecretManagerEndPoint;
	private String tgtDBSecretName;

	public boolean isSourceDB() {
		return isSourceDB;
	}

	public void setSourceDB(boolean sourceDB) {
		isSourceDB = sourceDB;
	}

	private int fetchSize;

	private int maxDecimals;
	
	private int maxTextSize;
	
	private int maxNoofThreads;

	private String schemaName;
	
	private String sourceDBService;

	private String sourceDBName;

	private String sourceDBType;

	private String sourceIP;

	private int sourcePort;

	private String sourceUserName;

	private String sourceUserPassword;

	private boolean fssl;

	private boolean ignoreTables;
	
	private String tableName;

	private String targetDBName;

	private String targetIP;

	private int targetPort;

	private String targetUserName;

	private String targetUserPassword;
	
	private boolean targetSSLRequire;
	
	private boolean ignoreColumns;
	
	private String columns;
	
	private String filterType;
	
	private String filter;
	
	private boolean compareOnlyDate;
	
	private boolean displayCompleteData;
	
	private String outputFolderPath;
	
	private String jobName;
	
	private String connectionType;
	
	private String sourceJdbcUrl;
	
	private String targetJdbcUrl;
	
	private String reportType;

	private boolean sourceSSLRequire;

	//@Value("${jdbc.truststore.path}")
	private String trustStorePath;
	//@Value("${jdbc.truststore.password}")
	public String trsutStorePassword;




	/**
	 * @return the fetchSize
	 */
	public int getFetchSize() {
		return fetchSize;
	}

	/**
	 * @return the maxDecimals
	 */
	public int getMaxDecimals() {
		return maxDecimals;
	}

	/**
	 * @return the schemaName
	 */
	public String getSchemaName() {
		return schemaName;
	}

	/**
	 * @return the sourceDBName
	 */
	public String getSourceDBName() {
		return sourceDBName;
	}

	/**
	 * @return the sourceDBType
	 */
	public String getSourceDBType() {
		return sourceDBType;
	}

	/**
	 * @return the sourceIP
	 */
	public String getSourceIP() {
		return sourceIP;
	}

	/**
	 * @return the sourcePort
	 */
	public int getSourcePort() {
		return sourcePort;
	}

	/**
	 * @return the sourceUserName
	 */
	public String getSourceUserName() {
		return sourceUserName;
	}

	/**
	 * @return the sourceUserPassword
	 */
	public String getSourceUserPassword() {
		return sourceUserPassword;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @return the targetDBName
	 */
	public String getTargetDBName() {
		return targetDBName;
	}

	/**
	 * @return the targetIP
	 */
	public String getTargetIP() {
		return targetIP;
	}

	/**
	 * @return the targetPort
	 */
	public int getTargetPort() {
		return targetPort;
	}

	/**
	 * @return the targetUserName
	 */
	public String getTargetUserName() {
		return targetUserName;
	}

	/**
	 * @return the targetUserPassword
	 */
	public String getTargetUserPassword() {
		return targetUserPassword;
	}

	/**
	 * @param fetchSize
	 *            the fetchSize to set
	 */
	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}

	/**
	 * @param maxDecimals
	 *            the maxDecimals to set
	 */
	public void setMaxDecimals(int maxDecimals) {
		this.maxDecimals = maxDecimals;
	}

	/**
	 * @param schemaName
	 *            the schemaName to set
	 */
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	/**
	 * @param sourceDBName
	 *            the sourceDBName to set
	 */
	public void setSourceDBName(String sourceDBName) {
		this.sourceDBName = sourceDBName;
	}

	/**
	 * @param sourceDBType
	 *            the sourceDBType to set
	 */
	public void setSourceDBType(String sourceDBType) {
		this.sourceDBType = sourceDBType;
	}

	/**
	 * @param sourceIP
	 *            the sourceIP to set
	 */
	public void setSourceIP(String sourceIP) {
		this.sourceIP = sourceIP;
	}

	/**
	 * @param sourcePort
	 *            the sourcePort to set
	 */
	public void setSourcePort(int sourcePort) {
		this.sourcePort = sourcePort;
	}

	/**
	 * @param sourceUserName
	 *            the sourceUserName to set
	 */
	public void setSourceUserName(String sourceUserName) {
		this.sourceUserName = sourceUserName;
	}

	/**
	 * @param sourceUserPassword
	 *            the sourceUserPassword to set
	 */
	public void setSourceUserPassword(String sourceUserPassword) {
		this.sourceUserPassword = sourceUserPassword;
	}

	/**
	 * @param tableName
	 *            the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * @param targetDBName
	 *            the targetDBName to set
	 */
	public void setTargetDBName(String targetDBName) {
		this.targetDBName = targetDBName;
	}

	/**
	 * @param targetIP
	 *            the targetIP to set
	 */
	public void setTargetIP(String targetIP) {
		this.targetIP = targetIP;
	}

	/**
	 * @param targetPort
	 *            the targetPort to set
	 */
	public void setTargetPort(int targetPort) {
		this.targetPort = targetPort;
	}

	/**
	 * @param targetUserName
	 *            the targetUserName to set
	 */
	public void setTargetUserName(String targetUserName) {
		this.targetUserName = targetUserName;
	}

	/**
	 * @param targetUserPassword
	 *            the targetUserPassword to set
	 */
	public void setTargetUserPassword(String targetUserPassword) {
		this.targetUserPassword = targetUserPassword;
	}

	/**
	 * @return the maxNoofThreads
	 */
	public int getMaxNoofThreads() {
		return maxNoofThreads;
	}

	/**
	 * @param maxNoofThreads the maxNoofThreads to set
	 */
	public void setMaxNoofThreads(int maxNoofThreads) {
		this.maxNoofThreads = maxNoofThreads;
	}

	/**
	 * @return the columns
	 */
	public String getColumns() {
		return columns;
	}

	/**
	 * @param columns the columns to set
	 */
	public void setColumns(String columns) {
		this.columns = columns;
	}

	/**
	 * @return the filterType
	 */
	public String getFilterType() {
		return filterType;
	}

	/**
	 * @param filterType the filterType to set
	 */
	public void setFilterType(String filterType) {
		this.filterType = filterType;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	/**
	 * @return the compareOnlyDate
	 */
	public boolean isCompareOnlyDate() {
		return compareOnlyDate;
	}

	/**
	 * @param compareOnlyDate the compareOnlyDate to set
	 */
	public void setCompareOnlyDate(boolean compareOnlyDate) {
		this.compareOnlyDate = compareOnlyDate;
	}

	public boolean isDisplayCompleteData() {
		return displayCompleteData;
	}

	public void setDisplayCompleteData(boolean displayCompleteData) {
		this.displayCompleteData = displayCompleteData;
	}

	/**
	 * @return the sourceDBService
	 */
	public String getSourceDBService() {
		return sourceDBService;
	}

	/**
	 * @param sourceDBService the sourceDBService to set
	 */
	public void setSourceDBService(String sourceDBService) {
		this.sourceDBService = sourceDBService;
	}

	/**
	 * @return the targetSSLRequire
	 */
	public boolean isTargetSSLRequire() {
		return targetSSLRequire;
	}

	/**
	 * @param targetSSLRequire the targetSSLRequire to set
	 */
	public void setTargetSSLRequire(boolean targetSSLRequire) {
		this.targetSSLRequire = targetSSLRequire;
	}

	/**
	 * @return the outputFolderPath
	 */
	public String getOutputFolderPath() {
		return outputFolderPath;
	}

	/**
	 * @param outputFolderPath the outputFolderPath to set
	 */
	public void setOutputFolderPath(String outputFolderPath) {
		this.outputFolderPath = outputFolderPath;
	}

	/**
	 * @return the jobName
	 */
	public String getJobName() {
		return jobName;
	}

	/**
	 * @param jobName the jobName to set
	 */
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	/**
	 * @return the connectionType
	 */
	public String getConnectionType() {
		return connectionType;
	}

	/**
	 * @param connectionType the connectionType to set
	 */
	public void setConnectionType(String connectionType) {
		this.connectionType = connectionType;
	}

	/**
	 * @return the sourceJdbcUrl
	 */
	public String getSourceJdbcUrl() {
		return sourceJdbcUrl;
	}

	/**
	 * @param sourceJdbcUrl the sourceJdbcUrl to set
	 */
	public void setSourceJdbcUrl(String sourceJdbcUrl) {
		this.sourceJdbcUrl = sourceJdbcUrl;
	}

	/**
	 * @return the targetJdbcUrl
	 */
	public String getTargetJdbcUrl() {
		return targetJdbcUrl;
	}

	/**
	 * @param targetJdbcUrl the targetJdbcUrl to set
	 */
	public void setTargetJdbcUrl(String targetJdbcUrl) {
		this.targetJdbcUrl = targetJdbcUrl;
	}

	/**
	 * @return the reportType
	 */
	public String getReportType() {
		return reportType;
	}

	/**
	 * @param reportType the reportType to set
	 */
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	/**
	 * @return the ignoreTables
	 */
	public boolean isIgnoreTables() {
		return ignoreTables;
	}

	/**
	 * @param ignoreTables the ignoreTables to set
	 */
	public void setIgnoreTables(boolean ignoreTables) {
		this.ignoreTables = ignoreTables;
	}

	/**
	 * @return the ignoreColumns
	 */
	public boolean isIgnoreColumns() {
		return ignoreColumns;
	}

	/**
	 * @param ignoreColumns the ignoreColumns to set
	 */
	public void setIgnoreColumns(boolean ignoreColumns) {
		this.ignoreColumns = ignoreColumns;
	}

	/**
	 * @return the maxTextSize
	 */
	public int getMaxTextSize() {
		return maxTextSize;
	}

	/**
	 * @param maxTextSize the maxTextSize to set
	 */
	public void setMaxTextSize(int maxTextSize) {
		this.maxTextSize = maxTextSize;
	}


	public boolean isSourceSSLRequire() {
		return sourceSSLRequire;
	}

	public void setSourceSSLRequire(boolean sourceSSLRequire) {
		this.sourceSSLRequire = sourceSSLRequire;
	}

	public String getTrustStorePath() {
		return trustStorePath;
	}

	public void setTrustStorePath(String trustStorePath) {
		this.trustStorePath = trustStorePath;
	}

	public String getTrsutStorePassword() {
		return trsutStorePassword;
	}

	public void setTrsutStorePassword(String trsutStorePassword) {
		this.trsutStorePassword = trsutStorePassword;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AppProperties [fetchSize=");
		builder.append(fetchSize);
		builder.append(", maxDecimals=");
		builder.append(maxDecimals);
		builder.append(", maxTextSize=");
		builder.append(maxTextSize);
		builder.append(", maxNoofThreads=");
		builder.append(maxNoofThreads);
		builder.append(", schemaName=");
		builder.append(schemaName);
		builder.append(", sourceDBService=");
		builder.append(sourceDBService);
		builder.append(", sourceDBName=");
		builder.append(sourceDBName);
		builder.append(", sourceDBType=");
		builder.append(sourceDBType);
		builder.append(", sourceIP=");
		builder.append(sourceIP);
		builder.append(", sourcePort=");
		builder.append(sourcePort);
		builder.append(", sourceUserName=");
		builder.append(sourceUserName);
		builder.append(", sourceSSLRequire=");
		builder.append(sourceSSLRequire);
		builder.append(", ignoreTables=");
		builder.append(ignoreTables);
		builder.append(", tableName=");
		builder.append(tableName);
		builder.append(", targetDBName=");
		builder.append(targetDBName);
		builder.append(", targetIP=");
		builder.append(targetIP);
		builder.append(", targetPort=");
		builder.append(targetPort);
		builder.append(", targetUserName=");
		builder.append(targetUserName);
		builder.append(", targetSSLRequire=");
		builder.append(targetSSLRequire);
		builder.append(", ignoreColumns=");
		builder.append(ignoreColumns);
		builder.append(", columns=");
		builder.append(columns);
		builder.append(", filterType=");
		builder.append(filterType);
		builder.append(", filter=");
		builder.append(filter);
		builder.append(", compareOnlyDate=");
		builder.append(compareOnlyDate);
		builder.append(", displayCompleteData=");
		builder.append(displayCompleteData);
		builder.append(", outputFolderPath=");
		builder.append(outputFolderPath);
		builder.append(", jobName=");
		builder.append(jobName);
		builder.append(", connectionType=");
		builder.append(connectionType);
		builder.append(", sourceJdbcUrl=");
		builder.append(sourceJdbcUrl);
		builder.append(", targetJdbcUrl=");
		builder.append(targetJdbcUrl);
		builder.append(", reportType=");
		builder.append(reportType);
		builder.append("]");
		return builder.toString();
	}



	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public void setSrcDBSecretManagerEndPoint(String srcDBSecretManagerEndPoint) {
		this.srcDBSecretManagerEndPoint = srcDBSecretManagerEndPoint;
	}

	public String getSrcDBSecretManagerEndPoint() {
		return srcDBSecretManagerEndPoint;
	}

	public void setSrcDBSecretName(String srcDBSecretName) {
		this.srcDBSecretName = srcDBSecretName;
	}

	public String getSrcDBSecretName() {
		return srcDBSecretName;
	}

	public void setTgtDBSecretManagerEndPoint(String tgtDBSecretManagerEndPoint) {
		this.tgtDBSecretManagerEndPoint = tgtDBSecretManagerEndPoint;
	}

	public String getTgtDBSecretManagerEndPoint() {
		return tgtDBSecretManagerEndPoint;
	}

	public void setTgtDBSecretName(String tgtDBSecretName) {
		this.tgtDBSecretName = tgtDBSecretName;
	}

	public String getTgtDBSecretName() {
		return tgtDBSecretName;
	}
}
/**
 * Service class to compare the database table data between Source( Oracle) and Traget Database( like Postgres)
 *
 *
 * @author      Harnath Valeti
 * @author      Madhu Athinarapu
 * @version     1.0
 * @since       1.0
 */

package com.datacompare.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datacompare.controller.CompareController;
import com.datacompare.model.AppProperties;
import com.datacompare.model.CompareResult;
import com.datacompare.model.DatabaseInfo;
import com.datacompare.model.DatabaseInfo.dbType;
import com.datacompare.util.DateUtil;
import com.datacompare.util.FileUtil;
import com.datacompare.util.JdbcUtil;

public class CompareService {

	public Logger logger = LoggerFactory.getLogger("CompareService");
	
	private Connection sourceConn = null;
	private Connection targetConn = null;
	
	/**
	 * @return the sourceConn
	 */
	public Connection getSourceConn() {
		return sourceConn;
	}

	/**
	 * @param sourceConn the sourceConn to set
	 */
	public void setSourceConn(Connection sourceConn) {
		this.sourceConn = sourceConn;
	}

	/**
	 * @return the targetConn
	 */
	public Connection getTargetConn() {
		return targetConn;
	}

	/**
	 * @param targetConn the targetConn to set
	 */
	public void setTargetConn(Connection targetConn) {
		this.targetConn = targetConn;
	}

	/**
	 * 
	 * @param appProperties
	 */
	public void startService(AppProperties appProperties) {
		 
		executeComparision(appProperties);
	}

	/**
	 * 
	 * @param appProperties
	 */
	public void executeComparision(AppProperties appProperties) {
		
	  	try {
			
	  		setConnections(appProperties); 
  			
  			if(getSourceConn() != null && getTargetConn() != null) { 
  				
  				long rowNo = 1;
  				Date date = new Date();
  				DateUtil dateUtil = new DateUtil();
  				String executionStartedAt = dateUtil.getExecutionDate(date);
  				String appendDateToFileNames = dateUtil.getAppendDateToFileName(date);
  				CompareController.reportOutputFolder = appProperties.getOutputFolderPath(); 
  				
  				StringBuilder data = new StringBuilder();
  				
  	            data.append("<html><head><title>Data Compare Results</title><style>table{border: 1px solid #ddd; border-radius: 13px; border-collapse: collapse;} th, td {border-bottom: 1px solid #ddd;border-collapse: collapse; font-family: Arial; font-size: 10pt;} th, td {padding: 10px;}	th {text-align: left;} table {border-spacing: 5px; width: 100%; background-color: #f1f1c1;	border-color: gray;} table tr:nth-child(even) {background-color: #eee;} table tr:nth-child(odd) {background-color: #fff;} table th {color: white; background-color: black;}	</style></head><body>")
				.append("<table><thead><tr><th>S.NO</th><th>TABLE</th><th>DETAILS</th><th>EXECUTION</th></tr></thead><tbody>");
  	            
				List<String> columnList = getColumnList(appProperties.getColumns());

				if (appProperties.getTableName() != null && !appProperties.getTableName().isEmpty()
						&& !appProperties.isIgnoreTables()) {
  					
  					String[] tableNameParts = appProperties.getTableName().split(",");
  					
  					for (String tableName : tableNameParts) { 

						CompareResult dto = compare(appProperties, getSourceConn(), getTargetConn(),
								appProperties.getSchemaName(), tableName, columnList);

			  			String executedTableName = appProperties.getSchemaName() + "." + tableName;
			  			
			  			summary(executedTableName, data, executionStartedAt, rowNo++, columnList, dto, appProperties);
  					}
		  			
  				} else {
  					
  					String[] schemaParts = appProperties.getSchemaName().split(",");
  					
  					for (String schemaName : schemaParts) { 
  						
						List<CompareResult> dtos = compareSchema(appProperties, getSourceConn(), getTargetConn(), schemaName, columnList);
	  					
	  					for (CompareResult dto : dtos) {
							
				  			String executedTableName = schemaName + "." + dto.getTableName();
				  			
	  						summary(executedTableName, data, executionStartedAt, rowNo++, columnList, dto, appProperties);
	  					}
					}
  				}
  				
				data.append("</tbody>")
				.append("</table>") 
  				.append("</body>")
  				.append("</html>");
				
        		String jobName = appProperties.getJobName();
        		jobName = (jobName != null && !jobName.isEmpty()) ? jobName : "data_comparison_result";
        		
  	        	String fileName = jobName + "_" + appendDateToFileNames + ".html";
  	        	
  	        	FileUtil fileWrite = new FileUtil();
  				
  	        	fileWrite.writeDataToFile(data, fileName, CompareController.reportOutputFolder); 
  	        	
  	        	CompareController.reportFileName = fileName;
	  			
  			} else {
  				
  				logger.info("Either Source or Target DB connection is not established."); 
  			}
	  			
	  	} catch (Exception ex) {
	  		
	  		logger.error(ex.getMessage(), ex);
	  		
	  	} finally {
			
	  		JdbcUtil jdbcUtil = new JdbcUtil();
	  		
	  		jdbcUtil.closeConnection(getSourceConn());
	  		jdbcUtil.closeConnection(getTargetConn());
		}
	}

	/**
	 * 
	 * @param executedTableName
	 * @param data
	 * @param executionStartedAt
	 * @param rowNo
	 * @param columnList
	 * @param dto
	 * @param appProperties
	 */
	private void summary(String executedTableName, StringBuilder data, String executionStartedAt, long rowNo,
			List<String> columnList, CompareResult dto, AppProperties appProperties) {

		String result = (dto.getResult() != null) ? dto.getResult() : "NA";
		String timeTaken = new DateUtil().timeDiffFormatted(dto.getTimeTaken());
		timeTaken = (timeTaken != null) ? timeTaken : "";
		long sourceTableCount = dto.getRowCountSource();
		long targetTableCount = dto.getRowCountTarget();
		long matchedRows = 0;
		long missingRows = dto.getSourceFailedRowCount();
		long unmatchedRows = dto.getValueMismatchCount();
		long invalidRows = dto.getTargetFailedRowCount();
		String reason = null;
		
		if("Detail".equals(appProperties.getReportType())) {
			
			matchedRows = sourceTableCount - unmatchedRows - missingRows;
			reason = (dto.getReason() != null) ? dto.getReason()
					: ((sourceTableCount == matchedRows && sourceTableCount > 0 && !"Failed".equals(dto.getResult()))
							? "Data Matched" : "");
			dto.setReason(reason);
			dto.setMatchedRowCount(matchedRows);
			
		} else if("Basic".equals(appProperties.getReportType())) {	
			
			reason = (dto.getReason() != null) ? dto.getReason()
					: ((sourceTableCount == targetTableCount) ? "Counts Matched" : "Counts Mismatch");
			dto.setReason(reason);
		}

		logSummary(dto, appProperties.getReportType());

		prepareSummaryReport(data, appProperties, columnList, dto, rowNo, sourceTableCount, targetTableCount,
				matchedRows, missingRows, unmatchedRows, invalidRows, executedTableName, result, reason, timeTaken,
				executionStartedAt);
	}
	
	/**
	 * 
	 * @param appProperties
	 * @throws Exception
	 */
	private void setConnections(AppProperties appProperties) throws Exception { 
		
	  	Connection sourceConn = null;
	  	Connection targetConn = null;
	  	
	  	JdbcUtil jdbcUtil = new JdbcUtil();
	  	
		if("All".equals(appProperties.getConnectionType())) {
			
			DatabaseInfo sourceDb = new DatabaseInfo(appProperties.getSourceIP(), appProperties.getSourcePort(),
					appProperties.getSourceDBName(), appProperties.getSourceDBService(),
					appProperties.getSourceUserName(), appProperties.getSourceUserPassword(), appProperties.isSourceSSLRequire(),
					dbType.valueOf(appProperties.getSourceDBType().toUpperCase()),false,appProperties.getTrustStorePath(),appProperties.getTrsutStorePassword());
			
			DatabaseInfo targetDb = new DatabaseInfo(appProperties.getTargetIP(), appProperties.getTargetPort(),
					appProperties.getTargetDBName(), null, appProperties.getTargetUserName(),
					appProperties.getTargetUserPassword(), appProperties.isTargetSSLRequire(), dbType.POSTGRESQL,
					true, appProperties.getTrustStorePath(),  appProperties.getTrsutStorePassword());

  			sourceConn = getConnection(sourceDb);
  			logger.info("Source DB Connection Details: " + sourceConn);
  			
  			targetConn = getConnection(targetDb);
  			logger.info("Target DB Connection Details: " + targetConn);
  			
		} else if("JDBC".equals(appProperties.getConnectionType())) {
			
			sourceConn = getConnection(appProperties.getSourceJdbcUrl(),
					jdbcUtil.getDriverClass(appProperties.getSourceDBType().toUpperCase()),
					appProperties.getSourceUserName(), appProperties.getSourceUserPassword());
  			logger.info("Source DB Connection Details: " + sourceConn);
  			
			targetConn = getConnection(appProperties.getTargetJdbcUrl(), jdbcUtil.getDriverClass("POSTGRESQL"),
					appProperties.getTargetUserName(), appProperties.getTargetUserPassword());
  			logger.info("Target DB Connection Details: " + targetConn);
		}
		
		setSourceConn(sourceConn);
		setTargetConn(targetConn); 
	}
	
	/**
	 * 
	 * @param data
	 * @param appProperties
	 * @param columnList
	 * @param dto
	 * @param rowNo
	 * @param sourceTableCount
	 * @param targetTableCount
	 * @param matchedRows
	 * @param missingRows
	 * @param unmatchedRows
	 * @param invalidRows
	 * @param executedTableName
	 * @param result
	 * @param reason
	 * @param timeTaken
	 * @param executionStartedAt
	 */
	private void prepareSummaryReport(StringBuilder data, AppProperties appProperties, List<String> columnList,
			CompareResult dto, long rowNo, long sourceTableCount, long targetTableCount, long matchedRows,
			long missingRows, long unmatchedRows, long invalidRows, String executedTableName, String result, String reason,
			String timeTaken, String executionStartedAt) {
		
		data.append("<tr>")
		.append("<td style='vertical-align: top;'><b>").append(Long.toString(rowNo)).append("</b></td>")
		.append("<td style='vertical-align: top;'>")
		.append("<table>")
		.append("<tr><td><b>Name</b></td><td>").append(executedTableName).append("</td></tr>");
		
		if(appProperties.isIgnoreColumns() && !columnList.isEmpty()) {
			
			data.append("<tr><td><b>Columns Excluded from Comparison</b></td><td>")
			.append(columnList.toString()).append("</td></tr>");
			
		} else if(!columnList.isEmpty()) {
			
			data.append("<tr><td><b>Columns Included for Comparison</b></td><td>")
			.append(columnList.toString()).append("</td></tr>");
			
		} else {
			
			data.append("<tr><td><b>Columns</b></td><td>All</td></tr>");
		}
		
		data.append("<tr><td><b>Max Decimals Compared</b></td><td>").append(appProperties.getMaxDecimals()).append("</td></tr>");
		data.append("<tr><td><b>Max Text Size Compared</b></td><td>").append(appProperties.getMaxTextSize()).append("</td></tr>");

		if (appProperties.isCompareOnlyDate()) {

			data.append("<tr><td><b>Compared Only Date</b></td><td>Yes</td></tr>");
		}

		data.append("</table>")
		.append("</td>")
		.append("<td style='vertical-align: top;'>")
		.append("<table>")
		.append("<tr><td><b>Source Table Count</b></td><td>").append(sourceTableCount).append("</td></tr>")
		.append("<tr><td><b>Target Table Count</b></td><td>").append(targetTableCount).append("</td></tr>");
		
		if("Detail".equals(appProperties.getReportType())) {
			
			data.append("<tr><td><b>Matched Rows</b></td><td>").append(matchedRows).append("</td></tr>");
			
			if(unmatchedRows > 0) {
				data.append("<tr><td><b>Unmatched Rows</b></td><td>").append(unmatchedRows).append("</td></tr>");
			}
			
			if(missingRows > 0) {
				data.append("<tr><td><b>Missing Rows</b></td><td>").append(missingRows).append("</td></tr>");
			}
			
			if(invalidRows > 0) {
				data.append("<tr><td><b>Additional Rows in Target</b></td><td>").append(invalidRows).append("</td></tr>");
			}
		}
		
		data.append("</table>")
		.append("</td>")
		.append("<td style='vertical-align: top;'>")
		.append("<table>")
		.append("<tr><td><b>Started at</b></td><td>").append(executionStartedAt).append("</td></tr>")
		.append("<tr><td><b>Time Taken</b></td><td>").append(timeTaken).append("</td></tr>")
		.append("<tr><td><b>Status</b></td><td>").append(result).append("</td></tr>")
		.append("<tr><td><b>Message</b></td><td>").append(reason).append("</td></tr>")
		;

		if (dto.getFilename() != null && dto.getFilename().trim().endsWith(".html")) {

			data.append("<tr><td><b>Report</b></td><td>").append("<a href='").append(dto.getFilename())
					.append("'>View Details</a></td></tr>");
		}

		data.append("</table>")
		.append("</td>")
		.append("</tr>");

		if (appProperties.getFilter() != null && !appProperties.getFilter().isEmpty()) {

			data.append("<tr><td colspan='7'><b>").append(appProperties.getFilterType())
					.append(" Filter Used</b>&nbsp;&nbsp;").append(appProperties.getFilter()).append("</td></tr>");
		}
	}
	
	/**
	 * 
	 * @param columns
	 * @return
	 */
	private List<String> getColumnList(String columns) { 
		
		List<String> columnsList = new ArrayList<String>();
		
		if(columns != null && !columns.isEmpty()) {
				
			String[] columnParts = columns.split(",");
			
			for (String column : columnParts) {
				
				columnsList.add(column.trim());
			}
		}
		
		return columnsList;
	}
	
	/**
	 * 
	 * @param dto
	 * @param reportType
	 */
	private void logSummary(CompareResult dto, String reportType) {
		
		StringBuilder info = new StringBuilder();
		
		String timeTaken = new DateUtil().timeDiffFormatted(dto.getTimeTaken());
		
		info.append("\nSummary of table data comparision");
		info.append("\n----------------------------------------------------------\n");
		info.append("\nTotal time taken = "); 
		info.append(timeTaken);
		info.append("\nTotal row count in source table = ");
		info.append(dto.getRowCountSource());
		info.append("\nTotal row count in target table = ");
		info.append(dto.getRowCountTarget());
		info.append("\nResult = ");
		info.append(dto.getResult());
		info.append("\nMessage = "); 
		info.append(dto.getReason());
		
		if("Detail".equals(reportType)) {
			
			info.append("\nMatched Rows Count = ");
			info.append(dto.getMatchedRowCount());
			info.append("\nFailed Row Count = ");
			info.append(dto.getFailedRowNumber());
		}
		
		info.append("\n----------------------------------------------------------\n");
		
		logger.info(info.toString());
	}
	
	/**
	 * 
	 * @param db
	 * @return
	 * @throws Exception
	 */
	public Connection getConnection(DatabaseInfo db) throws Exception {

		return getConnection(db.getURL(), db.getDriverClass(), db.getUserName(), db.getPassword(),db.getType().name(),db.isSslRequire(),db.getTrustStorePath(),db.getTrsutStorePassword());
	}
	
	/**
	 * 
	 * @param url
	 * @param driverClass
	 * @param user
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public Connection getConnection(String url, String driverClass, String user, String password, String dbType,boolean isSslRequire,String trustStorePath, String trsutStorePassword) throws Exception {
		
		try {
			
			Class.forName(driverClass);
			
		} catch(ClassNotFoundException ex) {
			
			ex.printStackTrace();
			throw new Exception(ex.getMessage());
		}

		Properties props = new Properties();
		props.setProperty("user", user);
		props.setProperty("password", password);
		if (isSslRequire) {
			//props.setProperty("oracle.net.ssl_cipher_suites", "(TLS_ SA_WITH_AES_128_CBC_SHA, TLS_ SA_WITH_AES_256_CBC_SHA, SSL_ SA_WITH_3DES_EDE_CBC_SHA ,SSL_ SA_WITH_ C4_128_SHA,SSL_ SA_WITH_ C4_128_MD5 ,SSL_ SA_WITH_DES_CBC_SHA ,SSL_DH_anon_WITH_3DES_EDE_CBC_SHA,SSL_DH_anon_WITH_ C4_128_MD5,SSL_DH_anon_WITH_DES_CBC_SHA,SSL_ SA_EXPO T_WITH_ C4_40_MD5 ,SSL_ SA_EXPO T_WITH_DES40_CBC_SHA ,TLS_ SA_WITH_AES_128_CBC_SHA,TLS_ SA_WITH_AES_256_CBC_SHA)");
			setSslProperties(props,trustStorePath, trsutStorePassword);
		}
		logger.info("\n" + url);
		Connection conn = DriverManager.getConnection(url, props);
		
		return conn;
	}
	/**
	 *
	 * @param url
	 * @param driverClass
	 * @param user
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public Connection getConnection(String url, String driverClass, String user, String password) throws Exception {

		try {

			Class.forName(driverClass);

		} catch(ClassNotFoundException ex) {

			ex.printStackTrace();
			throw new Exception(ex.getMessage());
		}

		Properties props = new Properties();
		props.setProperty("user", user);
		props.setProperty("password", password);
		logger.info("\n" + url);
		Connection conn = DriverManager.getConnection(url, props);

		return conn;
	}

	/**
	 * Method for configuring SSL connection properties.
	 */
	public void setSslProperties(Properties props,String trustStorePath,String trsutStorePassword) {

		props.setProperty("javax.net.ssl.trustStore",
				trustStorePath);
		props.setProperty("javax.net.ssl.trustStoreType","JKS");
		props.setProperty("javax.net.ssl.trustStorePassword",trsutStorePassword);

	}
	/**
	 * 
	 * @param appProperties
	 * @param sourceConn
	 * @param targetConn
	 * @param schemaName
	 * @param tableName
	 * @param columnList
	 * @return
	 */
	public CompareResult compare(AppProperties appProperties, Connection sourceConn, Connection targetConn,
			String schemaName, String tableName, List<String> columnList) {
		if("Detail".equals(appProperties.getReportType())) {
			return compareDetailData(appProperties, sourceConn, targetConn, schemaName, tableName, columnList);
		} else if("Basic".equals(appProperties.getReportType())) {
			return compareBasicData(appProperties, sourceConn, targetConn, schemaName, tableName);
		}
		return null;
	}
	
	/**
	 * 
	 * @param appProperties
	 * @param sourceConn
	 * @param targetConn
	 * @param schemaName
	 * @param tableName
	 * @param columnList
	 * @return
	 */
	private CompareResult compareDetailData(AppProperties appProperties, Connection sourceConn, Connection targetConn,
			String schemaName, String tableName, List<String> columnList) {
		
		String sourceDBType = appProperties.getSourceDBType().toUpperCase();
		int maxNoofThreads = appProperties.getMaxNoofThreads();
		boolean displayCompleteData = appProperties.isDisplayCompleteData();
		long additionalrows =0;
		CompareResult dto = new CompareResult();
		long start = System.currentTimeMillis();
		StringBuilder info = new StringBuilder();
		// Get the Java runtime
       // Runtime runtime = Runtime.getRuntime();

        long usedMemory = 0;
		try {
			checkIfTableExistsInPg(schemaName.toLowerCase(), tableName.toLowerCase(), "POSTGRESQL", targetConn);
            long rowCount=0;
			long sourceRowCount= new FetchMetadata().getTotalRecords(sourceConn,schemaName.toUpperCase(), tableName.toUpperCase(),null);
			long targetRowCount= new FetchMetadata().getTotalRecords(targetConn,schemaName.toUpperCase(), tableName.toUpperCase(),null);
            if(sourceRowCount>targetRowCount)
               rowCount=sourceRowCount;
			else {
				rowCount = targetRowCount;
				additionalrows = targetRowCount-sourceRowCount;
			}
			FetchMetadata fetchSourceMetadata = new FetchMetadata(sourceDBType, null, sourceConn,
					schemaName.toUpperCase(), tableName.toUpperCase(), rowCount, null, null, false, null, columnList, appProperties,true,additionalrows);
			FetchMetadata fetchTargetMetadata = new FetchMetadata("POSTGRESQL", sourceDBType, targetConn,
					schemaName.toLowerCase(), tableName.toLowerCase(), rowCount,
					fetchSourceMetadata.getSortKey(), fetchSourceMetadata.getPrimaryKey(),
					fetchSourceMetadata.isHasNoUniqueKey(),
					fetchSourceMetadata.getTableMetadataMap(), columnList, appProperties,false,additionalrows);
		  	        fetchTargetMetadata.setTargetRowCount(targetRowCount);
			        fetchSourceMetadata.setTargetRowCount(targetRowCount);
 			List<String> sourceChunks = fetchSourceMetadata.getChunks();
			List<String> targetChunks = fetchTargetMetadata.getChunks();
			info.append("Schema: ");
			info.append(schemaName);
			info.append(" , Table: ");
			info.append(tableName);
			info.append(" , No Of Chunks: ");
			info.append(sourceChunks.size());
			logger.info(info.toString());
			int numChunks = sourceChunks.size();
			int i;
			info = new StringBuilder();
			info.append("No Of Chunks to run: ");
			info.append(numChunks);
			info.append("\n###############################################################\n");
			logger.info(info.toString());
			Map<String, String> mismatchSourceData = new ConcurrentHashMap<String, String>();
			Map<String, String> mismatchTargetData = new ConcurrentHashMap<String, String>();
			List<String> failTuple = Collections.synchronizedList(new ArrayList<String>());
			String result = "Completed";
			List<Long> sourceCountList = Collections.synchronizedList(new ArrayList<Long>());
			List<Long> targetCountList = Collections.synchronizedList(new ArrayList<Long>());
			List<Long> sourceTimeTaken = Collections.synchronizedList(new ArrayList<Long>());
			List<Long> targetTimeTaken = Collections.synchronizedList(new ArrayList<Long>());
			ExecutorService executor = Executors.newFixedThreadPool(maxNoofThreads);
			for (i = 0; i < numChunks; i++) {
				String targetChunk = fetchSourceMetadata.isHasNoUniqueKey()
						? getTargetChunkWhenNoUniqueKey(sourceChunks.get(i)) : sourceChunks.get(i);
				ExecuteChunk executeChunk = new ExecuteChunk(sourceDBType, "POSTGRESQL", sourceChunks.get(i),
						targetChunk, fetchSourceMetadata.getSql(), fetchTargetMetadata.getSql(), i, numChunks,
						sourceConn, targetConn, fetchSourceMetadata.getTableMetadataMap(),
						fetchTargetMetadata.getTableMetadataMap(), appProperties,fetchSourceMetadata.isPrimeryKeySupplied(appProperties,tableName));
				executeChunk.setSourceData(mismatchSourceData);
				executeChunk.setTargetData(mismatchTargetData);
				executeChunk.setFailTuple(failTuple);
				executeChunk.setResult(result);
				executeChunk.setSourceCount(sourceCountList);
				executeChunk.setTargetCount(targetCountList);
				executeChunk.setSourceTimeTaken(sourceTimeTaken);
				executeChunk.setTargetTimeTaken(targetTimeTaken);
				executeChunk.setHasNoUniqueKey(fetchSourceMetadata.isHasNoUniqueKey());
				executor.execute(executeChunk);
			}
			executor.shutdown();
			while (!executor.isTerminated()) {
	        }
			logger.info("Finished all chunks");
			logTimeTaken(sourceTimeTaken, targetTimeTaken);
			dto.setResult(result);
			dto.setFailTuple(failTuple);
			info = new StringBuilder();
			info.append("\n----------------------------------------------------\n");
			info.append("Finished analyzing the data for ");
			info.append(schemaName);
			info.append(".");
			info.append(tableName);
			logger.info(info.toString());
			long sourceCount = getCount(sourceCountList);
			long targetCount = getCount(targetCountList);
			logger.info("Size of the source mismatch data before final validation"+ mismatchSourceData.size());
			logger.info("Size of the target mismatch data before final validation "+ mismatchTargetData.size());

			ExecutorService validationExecutor = Executors.newFixedThreadPool(1);
			ValidateChunk executeChunk = new ValidateChunk(mismatchSourceData,mismatchSourceData, mismatchTargetData,fetchSourceMetadata.isHasNoUniqueKey(),fetchSourceMetadata.isPrimeryKeySupplied(appProperties,tableName),numChunks+1);
			validationExecutor.execute(executeChunk);
			validationExecutor.shutdown();
			while (!validationExecutor.isTerminated()) {
			}
			dto.setRowCountSource(sourceCount);
			dto.setRowCountTarget(targetCount);
			dto.setTableName(tableName);
			writeDataToFile(fetchSourceMetadata, mismatchSourceData, fetchTargetMetadata, mismatchTargetData, dto,
					schemaName, displayCompleteData,targetRowCount,appProperties,tableName);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			if (dto.getReason() == null) {
				dto.setReason(ex.getMessage());
			}
			dto.setTableName(tableName);
			dto.setResult("Failed");
		}
		long end = System.currentTimeMillis();
		long timeTaken = end - start;
		dto.setTimeTaken(timeTaken/1000 );
		dto.setUsedMemory(usedMemory);
		info = new StringBuilder();
		info.append("\n----------------------------------------------------\n");
		info.append("Finished writing comparison results for ");
		info.append(schemaName);
		info.append(".");
		info.append(tableName);
		logger.info(info.toString());
		return dto;
	}
		public List<Map<String, String>> splitMap( Map<String, String> original, long max) {
			int counter = 0;
			int lcounter = 0;
			List<Map<String, String>> listOfSplitMaps = new ArrayList<Map<String, String>> ();
			Map<String, String> splitMap = new HashMap<> ();

			for (Map.Entry<String, String> m : original.entrySet()) {
				if (counter < max) {
					splitMap.put(m.getKey(), m.getValue());
					counter++;
					lcounter++;

					if (counter == max || lcounter == original.size()) {
						counter = 0;
						listOfSplitMaps.add(splitMap);
						splitMap = new HashMap<> ();
					}
				}
			}
			return listOfSplitMaps;
		}

	/**
	 * 
	 * @param targetChunk
	 * @return
	 */
	private String getTargetChunkWhenNoUniqueKey(String targetChunk) {
		
//		int beginIndex = targetChunk.lastIndexOf("where");
//		int endIndex = targetChunk.lastIndexOf("order by 1");
//		
//		String temp = targetChunk.substring(beginIndex, endIndex);
//		temp = temp.replaceAll("where", "").replaceAll("key1", "").replaceAll(">=", "").replaceAll("<=", "").trim();
//		temp = StringUtils.normalizeSpace(temp);
//		int index = temp.indexOf("and");
//		
//		long startRange = Long.parseLong(temp.substring(0, index).trim());
//		long endRange = Long.parseLong(temp.substring(index + 3, temp.length()).trim()); 
//		long limit = endRange - startRange;
//		startRange = startRange - 1;
//	
//		StringBuilder condition = new StringBuilder();
//		
//		condition.append(" order by 1 limit ").append(limit).append(" offset ").append(startRange);
//		
//		return condition.toString();
		return targetChunk;
	}
	
	/**
	 * 
	 * @param sourceTimeTaken
	 * @param targetTimeTaken
	 */
	private void logTimeTaken(List<Long> sourceTimeTaken, List<Long> targetTimeTaken) {
		try {
			DateUtil dateUtil = new DateUtil();
			StringBuilder info = new StringBuilder();
			info.append("Least, Highest, Total, Average Times Taken for executing chunk.");
			info.append("\n----------------------------------------------------------\n");
			Collections.sort(sourceTimeTaken);
			long sourceLeast = sourceTimeTaken.get(0);
			long sourceHighest = sourceTimeTaken.get(sourceTimeTaken.size() - 1);
			long sourceTimeSum = 0L;
			for (Long sourceTime : sourceTimeTaken) {
				sourceTimeSum = sourceTimeSum + sourceTime;
			}
			long sourceTimeAvg = (sourceTimeSum/sourceTimeTaken.size());
			info.append("Source - Least Time Taken to Fetch Data " + dateUtil.timeDiffFormatted(sourceLeast));
			info.append("\nSource - Highest Time Taken to Fetch Data " + dateUtil.timeDiffFormatted(sourceHighest));
			info.append("\nSource - Tota Time Taken to Fetch Data " + dateUtil.timeDiffFormatted(sourceTimeSum));
			info.append("\nSource - Average Time Taken to Fetch Data " + dateUtil.timeDiffFormatted(sourceTimeAvg));
			info.append("\n----------------------------------------------------------");
			Collections.sort(targetTimeTaken);
			long targetLeast = targetTimeTaken.get(0);
			long targetHighest = targetTimeTaken.get(targetTimeTaken.size() - 1);
			long targetTimeSum = 0L;
			for (Long targetTime : targetTimeTaken) {
				targetTimeSum = targetTimeSum + targetTime;
			}
			long targetTimeAvg = (targetTimeSum/targetTimeTaken.size());
			info.append("\nTarget - Least Time Taken to Fetch Data " + dateUtil.timeDiffFormatted(targetLeast));
			info.append("\nTarget - Highest Time Taken to Fetch Data " + dateUtil.timeDiffFormatted(targetHighest));
			info.append("\nTarget - Total Time Taken to Fetch Data " + dateUtil.timeDiffFormatted(targetTimeSum));
			info.append("\nTarget - Average Time Taken to Fetch Data " + dateUtil.timeDiffFormatted(targetTimeAvg));
			info.append("\n");
			logger.info(info.toString());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * 
	 * @param countList
	 * @return
	 */
	private Long getCount(List<Long> countList) {
		long count = 0;
		for (Long cnt : countList) {
			count = count + cnt;
		}
		return count;
	}
	
	/**
	 * 
	 * @param appProperties
	 * @param sourceConn
	 * @param targetConn
	 * @param schemaName
	 * @param tableName
	 * @return
	 */
	private CompareResult compareBasicData(AppProperties appProperties, Connection sourceConn, Connection targetConn,
			String schemaName, String tableName ){
		String sourceDBType = appProperties.getSourceDBType().toUpperCase();
		CompareResult result = new CompareResult();
		long start = System.currentTimeMillis();
		StringBuilder info = new StringBuilder();
		// Get the Java runtime
       // Runtime runtime = Runtime.getRuntime();
        long usedMemory = 0;
		try {
			checkIfTableExistsInPg(schemaName.toLowerCase(), tableName.toLowerCase(), "POSTGRESQL", targetConn);
			FetchMetadata fetchSourceMetadata = new FetchMetadata(sourceDBType, null, sourceConn,
					schemaName.toUpperCase(), tableName.toUpperCase(), 0, null, null, false, null, null, appProperties,true,0);
			FetchMetadata fetchTargetMetadata = new FetchMetadata("POSTGRESQL", null, targetConn,
					schemaName.toLowerCase(), tableName.toLowerCase(), 0, null, null, false, null, null, appProperties,false,0);
			info = new StringBuilder();
			info.append("\n----------------------------------------------------\n");
			info.append("Finished fetching basic the data for ");
			info.append(schemaName);
			info.append(".");
			info.append(tableName);
			logger.info(info.toString());
			long sourceTotalRowCount = fetchSourceMetadata.getSourceRowCount();
			result.setRowCountSource(sourceTotalRowCount);
			long targetTotalRowCount = fetchTargetMetadata.getTargetRowCount();
			result.setRowCountTarget(targetTotalRowCount);
			result.setTableName(tableName);
			result.setResult("Completed");
		} catch (Exception ex) {
			
			logger.error(ex.getMessage(), ex);
			
			if (result.getReason() == null) {
				
				result.setReason(ex.getMessage());
			}
			
			result.setTableName(tableName);
			result.setResult("Failed");
		} 

		long end = System.currentTimeMillis();
		long timeTaken = end - start;
		result.setTimeTaken(timeTaken/1000 );
		result.setUsedMemory(usedMemory); 
		
		return result;
	}
	
	/**
	 * 
	 * @param schemaName
	 * @param tableName
	 * @param dbType
	 * @param conn
	 * @throws Exception
	 */
	private void checkIfTableExistsInPg(String schemaName, String tableName, String dbType, Connection conn) throws Exception {
		
		ResultSet rs = null;
		
		try {
			
			rs = conn.getMetaData().getTables(null, schemaName, tableName , null);
			
			if (!rs.next()) {
				throw new Exception("Table " + schemaName + "." + tableName + " not found for "+ dbType + " DATABASE");
			}
			
		} catch(SQLException ex) {
			
			logger.error(ex.getMessage(), ex);
			
		} finally {
			
			new JdbcUtil().closeResultSet(rs);
		} 
	}

	/**
	 * 
	 * @param fetchSourceMetadata
	 * @param mismatchSourceData
	 * @param fetchTargetMetadata
	 * @param mismatchTargetData
	 * @param dto
	 * @param schemaName
	 * @param displayCompleteData
	 */
	public void writeDataToFile(FetchMetadata fetchSourceMetadata, Map<String, String> mismatchSourceData,
			FetchMetadata fetchTargetMetadata, Map<String, String> mismatchTargetData, CompareResult dto,
			String schemaName, boolean displayCompleteData,long targetCount,AppProperties appProperties, String tableName) {
		
		StringBuilder info = new StringBuilder();
		
		info.append("\nSource Table size = ");
		info.append(fetchSourceMetadata.getSourceRowCount());
		info.append("\nTarget Table size = ");
		//info.append(targetRowCount);
		info.append(targetCount);
		info.append("\n");
		logger.info(info.toString());
		if((mismatchSourceData.size() != 0 ) || (mismatchTargetData.size()!=0)) {
			info = new StringBuilder();
			info.append(schemaName.toLowerCase());
			info.append("_");
			info.append(dto.getTableName().toLowerCase());
			info.append("_table_comparison_result_");
			info.append(new DateUtil().getAppendDateToFileName(new Date()));
			info.append(".html");
			String fileName = info.toString();
			try {
				long totalFailedRowCount = 0;
				StringBuilder bw = new StringBuilder();
				writeHeader(fetchSourceMetadata, fetchTargetMetadata, displayCompleteData, bw,getSuppliedPrimaryKey(appProperties));
				if ((!fetchSourceMetadata.isHasNoUniqueKey()) || (fetchSourceMetadata.isHasNoUniqueKey() && fetchSourceMetadata.isPrimeryKeySupplied(appProperties, tableName))){
					writeMismatchData(mismatchSourceData, mismatchTargetData, displayCompleteData, dto, bw);
			     }
				writeMismatchSourceData(mismatchSourceData, displayCompleteData, dto, bw);
				writeMismatchTargetData(mismatchTargetData, displayCompleteData, dto, bw);
				totalFailedRowCount = dto.getValueMismatchCount() + dto.getTargetFailedRowCount()
						+ dto.getSourceFailedRowCount();
 				String reasonOfFailure = "";
 				if( dto.getTargetFailedRowCount() > 0 ) {
 					reasonOfFailure = "Additional rows found in Target";
 				}
 				if( dto.getSourceFailedRowCount() > 0 ) {
 					reasonOfFailure = reasonOfFailure + (!reasonOfFailure.isEmpty() ? " / " : "") + "Rows did not migrated from source";
 				}
 				if( dto.getValueMismatchCount() > 0 ) {
 					reasonOfFailure = reasonOfFailure + (!reasonOfFailure.isEmpty() ? " / " : "") + "Tuple value mismatched";
 				}
 				if( dto.getTargetFailedRowCount() == 0 && dto.getSourceFailedRowCount() == 0 && dto.getValueMismatchCount() == 0 ) {
 					reasonOfFailure = "Data Matched";
 				}
 				dto.setFailedRowNumber(totalFailedRowCount);
 				dto.setReason(reasonOfFailure);
 				dto.setFilename(fileName);
 				bw.append("</tbody></table></body></html>");
 				FileUtil fileWrite = new FileUtil();
 				fileWrite.writeDataToFile(bw, fileName, CompareController.reportOutputFolder);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	/**
	 * 
	 * @param fetchSourceMetadata
	 * @param fetchTargetMetadata
	 * @param displayCompleteData
	 * @param bw
	 */
	private void writeHeader(FetchMetadata fetchSourceMetadata, FetchMetadata fetchTargetMetadata,
			boolean displayCompleteData, StringBuilder bw, String uniqueKeys) {

		bw.append(
				"<html><head><title>Data Compare Details</title><style>table{border: 1px solid #ddd; border-radius: 13px; border-collapse: collapse;} th, td {border-bottom: 1px solid #ddd;border-collapse: collapse; font-family: Arial; font-size: 10pt;} th, td {padding: 15px;}	th {text-align: left;} table {border-spacing: 5px; width: 100%; background-color: #f1f1c1;	border-color: gray;} table tr:nth-child(even) {background-color: #eee;} table tr:nth-child(odd) {background-color: #fff;} table th {color: white; background-color: black;}	</style></head><body>");

		if (displayCompleteData) {

			bw.append(
					"<table><thead><tr><th>REASON OF FAILURE</th><th>SOURCE TUPLE</th><th>TARGET TUPLE</th></tr></thead><tbody>");

			bw.append("<tr><td style='width: 10%;'>&nbsp;</td>");

			String sourceCols = fetchSourceMetadata.getTableMetadataMap().keySet().toString();

			sourceCols = " || " + sourceCols.replaceAll(", ", " || ").replace("[", "").replace("]", "") + " || ";

			bw.append("<td style='width: 45%;vertical-align: top;'>").append(sourceCols).append("</td>");

			String targetCols = fetchTargetMetadata.getTableMetadataMap().keySet().toString();
			
			targetCols = " || " + targetCols.replaceAll(", ", " || ").replace("[", "").replace("]", "") + " || ";

			bw.append("<td style='width: 45%;vertical-align: top;'>").append(targetCols).append("</td></tr>");

		} else {

			bw.append("<table><thead><tr><th>REASON OF FAILURE</th><th>UNIQUE KEYS</th></tr></thead><tbody>");

			bw.append("<tr><td>&nbsp;</td>");

			String sortColumns = fetchSourceMetadata.getSortKey();
            if(uniqueKeys!=null && !uniqueKeys.isEmpty())
				sortColumns=uniqueKeys;
			bw.append("<td>").append(sortColumns).append("</td></tr>");
		}
	}
	
	/**
	 * 
	 * @param mismatchSourceData
	 * @param mismatchTargetData
	 * @param displayCompleteData
	 * @param dto
	 * @param bw
	 */
	private void writeMismatchData(Map<String, String> mismatchSourceData, Map<String, String> mismatchTargetData,
								   boolean displayCompleteData, CompareResult dto, StringBuilder bw) {

		boolean mismatchDataFound = (mismatchSourceData.size() > 0);
		long mismatchRowCount = 0;

		if (!displayCompleteData && mismatchDataFound) {

			bw.append("<tr><td style='vertical-align: top;'><b>Row value mismatched</b></td><td>");
		}

		List<String> keys = new ArrayList<String>(mismatchSourceData.keySet());

		for (final String key : keys) {

			if (mismatchTargetData.containsKey(key)) {

				if (displayCompleteData) {

					bw.append("<tr><td>Row value mismatched</td><td style='vertical-align: top;'>");

					bw.append(mismatchSourceData.get(key))
							.append("</td><td style='vertical-align: top;'>")
							.append(mismatchTargetData.get(key));

					bw.append("</td></tr>");

				} else {

					bw.append(key).append(" || ");
				}

				mismatchRowCount++;

				mismatchSourceData.remove(key);
				mismatchTargetData.remove(key);
			}
		}

		if (!displayCompleteData && mismatchDataFound) {

			bw.append("</td></tr>");
		}

		dto.setValueMismatchCount(mismatchRowCount);
	}
	/**
	 * 
	 * @param mismatchSourceData
	 * @param displayCompleteData
	 * @param dto
	 * @param bw
	 */
	private void writeMismatchSourceData(Map<String, String> mismatchSourceData,
			boolean displayCompleteData, CompareResult dto, StringBuilder bw) {

		long sourceFailedRowCount = 0;
		
		if (mismatchSourceData.size() != 0) {

			if (!displayCompleteData) {

				bw.append("<tr><td style='vertical-align: top;'><b>Rows did not migrated from Source</b></td><td>");
			}

			for (final String key : mismatchSourceData.keySet()) {

				if (displayCompleteData) {

					bw.append("<tr><td>Row did not migrated from Source</td><td style='vertical-align: top;'>");
					bw.append(mismatchSourceData.get(key));
					bw.append("</td><td>&nbsp;</td></tr>");

				} else {

					bw.append(key).append(" || ");
				}

				sourceFailedRowCount++;
			}

			if (!displayCompleteData) {

				bw.append("</td></tr>");
			}
		}

		dto.setSourceFailedRowCount(sourceFailedRowCount);
	}
	
	/**
	 * 
	 * @param mismatchTargetData
	 * @param displayCompleteData
	 * @param dto
	 * @param bw
	 */
	private void writeMismatchTargetData(Map<String, String> mismatchTargetData,
			boolean displayCompleteData, CompareResult dto, StringBuilder bw) {

		long targetFailedRowCount = 0;
		
		if (mismatchTargetData.size() != 0) {

			if (!displayCompleteData) {

				bw.append("<tr><td style='vertical-align: top;'><b>Additional Rows found in Target</b></td><td>");
			}

			for (final String key : mismatchTargetData.keySet()) {

				if (displayCompleteData) {

					bw.append(
							"<tr><td>Additional Row found in Target</td><td>&nbsp;</td><td style='vertical-align: top;'>");
					bw.append(mismatchTargetData.get(key));
					bw.append("</td></tr>");

				} else {

					bw.append(key).append(" || ");
				}

				targetFailedRowCount++;
			}

			if (!displayCompleteData) {

				bw.append("</td></tr>");
			}
		}

		dto.setTargetFailedRowCount(targetFailedRowCount);
	}

	/**
	 * 
	 * @param appProperties
	 * @param sourceConn
	 * @param targetConn
	 * @param schemaName
	 * @param columnList
	 * @return
	 */
	public List<CompareResult> compareSchema(AppProperties appProperties, Connection sourceConn, Connection targetConn,
			String schemaName, List<String> columnList) {

		List<CompareResult> tableList = new ArrayList<CompareResult>();
		List<String> tableNames = new ArrayList<String>();

		ResultSet rs = null;

		try {

			List<String> ignoreTables = (appProperties.getTableName() != null && !appProperties.getTableName().isEmpty()
					&& appProperties.isIgnoreTables()) ? Arrays.asList(appProperties.getTableName().split(","))
					: new ArrayList<String>();

			String[] types = {"TABLE"};
			rs = sourceConn.getMetaData().getTables(null, schemaName.toUpperCase(), null, types);

			while (rs.next()) {

				String tableName = rs.getString("TABLE_NAME");

				if (ignoreTable(ignoreTables, tableName)) continue;

				tableNames.add(tableName);
			}

		} catch (SQLException ex) {

			logger.error(ex.getMessage(), ex);

		} finally {

			new JdbcUtil().closeResultSet(rs);
		}

	//	ExecutorService executor = Executors.newFixedThreadPool(10);
	//	Map<String, CompareResult> hashMap = new ConcurrentHashMap<String, CompareResult>();
		if (!tableNames.isEmpty()) {

			for (String tableName : tableNames) {
				StringBuilder info = new StringBuilder();
				info.append("\n----------------------------------------------------\n");
				info.append("Started Comparing Table Name: ");
				info.append(tableName);
				info.append(" in Schema: ");
				info.append(schemaName);
				logger.info(info.toString());
			/*	CompareResult dto = null;
				CompareTableResults compareTableResults = new CompareTableResults(dto, appProperties, sourceConn, targetConn,
						schemaName, tableName, columnList, hashMap);
				executor.execute(compareTableResults);
			}
			executor.shutdown();
			while (!executor.isTerminated()) {
			}
			for (final String key : hashMap.keySet()) {
				CompareResult dto = hashMap.get(key);*/
				CompareResult dto = compare(appProperties, sourceConn, targetConn, schemaName, tableName, columnList);
				//if (dto.getReason() == null && !(dto.getResult() != null && "Completed".equals(dto.getResult())) ) {
					//dto.setTableName(tableName);
				if (dto.getReason() == null && !(dto.getResult() != null && "Completed".equals(dto.getResult()))) {
					dto.setTableName(tableName);
					dto.setReason("Table " + schemaName + "." + tableName + " unable to compare.");
					dto.setResult("Failed");
				}

				tableList.add(dto);

	/*			StringBuilder info = new StringBuilder();

				info.append("\n----------------------------------------------------\n");
				info.append("Finished Comparing Table Name: ");
				info.append(key);
				info.append(" in Schema: ");
				info.append(schemaName);*/

				logger.info(info.toString());
			}
		}

		return tableList;
	}
	
	/**
	 * 
	 * @param tablesList
	 * @param table
	 * @return
	 */
	private boolean ignoreTable(List<String> tablesList, String table) {

		for (String tab : tablesList) {
			
	        if (tab.equalsIgnoreCase(table)) {
	        	
	            return true;
	         }
	    }
		
	    return false;		
	}
	private String getSuppliedPrimaryKey(AppProperties appProperties) {
		String pkeys =null;
		if(appProperties!=null) {
			String tableName=appProperties.getTableName();
			if (appProperties.getPrimaryKeyMap() != null
					&& !appProperties.getPrimaryKeyMap().isEmpty()
					&& appProperties.getPrimaryKeyMap().get(tableName.toUpperCase()) != null) {
				pkeys = appProperties.getPrimaryKeyMap().get(tableName.toUpperCase());
			}
		}
		return pkeys;
	}
}

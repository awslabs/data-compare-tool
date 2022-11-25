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

import java.io.FileWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import com.ds.DataSource;

public class CompareService {

	public Logger logger = LoggerFactory.getLogger("CompareService");
	
	//private Connection sourceConn = null;
	//private Connection targetConn = null;
	
	/**
	 * @return the sourceConn
	 */
	/*public Connection getSourceConn() {
		return sourceConn;
	}*/

	/**
	 * @param sourceConn the sourceConn to set
	 */
	/*public void setSourceConn(Connection sourceConn) {
		this.sourceConn = sourceConn;
	}*/

	/**
	 * @return the targetConn
	 */
	/*public Connection getTargetConn() {
		return targetConn;
	}*/

	/**
	 * @param targetConn the targetConn to set
	 */
	/*public void setTargetConn(Connection targetConn) {
		this.targetConn = targetConn;
	}*/

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
			if(!DataSource.getInstance().isPoolInitialized())
	  		  setConnections(appProperties);
  			
  			if(DataSource.getInstance().isPoolInitialized()) { 
	  			long rowNo = 1;
  				Date date = new Date();
  				DateUtil dateUtil = new DateUtil();
  				String executionStartedAt = dateUtil.getExecutionDate(date);
  				String appendDateToFileNames = dateUtil.getAppendDateToFileName(date);
  				CompareController.reportOutputFolder = appProperties.getOutputFolderPath();

				List<String> columnList = getColumnList(appProperties.getColumns());

				if (appProperties.getTableName() != null && !appProperties.getTableName().isEmpty()
						&& !appProperties.isIgnoreTables()) {
  					String[] tableNameParts = appProperties.getTableName().split(",");
  					for (String tableName : tableNameParts) {
						CompareResult dto = compare(appProperties, /*getSourceConn(), getTargetConn(),*/
								appProperties.getSchemaName(), tableName, columnList);

  					}
		  			
  				} else {
  					
  					String[] schemaParts = appProperties.getSchemaName().split(",");
  					
  					for (String schemaName : schemaParts) {
						String[] schemas = schemaName.split(":");
						if(schemas.length>0)
						appProperties.setSchemaName(schemas[0]);
						if(schemas.length>1)
						appProperties.setTargetSchemaName(schemas[1].toLowerCase());
						List<CompareResult> dtos = compareSchema(appProperties, /*getSourceConn(), getTargetConn(),*/  columnList);

					}
  				}

			} else {
  				
  				logger.info("Either Source or Target DB connection is not established."); 
  			}
	  			
	  	} catch (Exception ex) {
	  		
	  		logger.error(ex.getMessage(), ex);
	  		
	  	} finally {
			
	  		JdbcUtil jdbcUtil = new JdbcUtil();
	  		
	  		//jdbcUtil.closeConnection(getSourceConn());
	  		//jdbcUtil.closeConnection(getTargetConn());
		}
	}


	
	/**
	 * 
	 * @param appProperties
	 * @throws Exception
	 */
	private void setConnections(AppProperties appProperties) throws Exception { 
		
	  	Connection sourceConn = null;
	  	Connection targetConn = null;
	  	
	  	//JdbcUtil jdbcUtil = new JdbcUtil();
	  	
		if("All".equals(appProperties.getConnectionType())) {
			
			DatabaseInfo sourceDb = new DatabaseInfo(appProperties.getSourceIP(), appProperties.getSourcePort(),
					appProperties.getSourceDBName(), appProperties.getSourceDBService(),
					appProperties.getSourceUserName(), appProperties.getSourceUserPassword(), appProperties.isSourceSSLRequire(),
					dbType.valueOf(appProperties.getSourceDBType().toUpperCase()),false,appProperties.getTrustStorePath(),appProperties.getTrsutStorePassword());
			sourceDb.setConnectionPoolMinSize(appProperties.getConnectionPoolMinSize());
			sourceDb.setConnectionPoolMaxSize(appProperties.getConnectionPoolMaxSize());
			
			DatabaseInfo targetDb = new DatabaseInfo(appProperties.getTargetIP(), appProperties.getTargetPort(),
					appProperties.getTargetDBName(), null, appProperties.getTargetUserName(),
					appProperties.getTargetUserPassword(), appProperties.isTargetSSLRequire(), dbType.POSTGRESQL,
					true, appProperties.getTrustStorePath(),  appProperties.getTrsutStorePassword());
			targetDb.setConnectionPoolMinSize(appProperties.getConnectionPoolMinSize());
			targetDb.setConnectionPoolMaxSize(appProperties.getConnectionPoolMaxSize());

  			//sourceConn = getConnection(sourceDb);
  			//logger.info("Source DB Connection Details: " + sourceConn);
  			
  			//targetConn = getConnection(targetDb);
  			//logger.info("Target DB Connection Details: " + targetConn);
  			
  			//try the pool start
			 DataSource.getInstance().initializePool(sourceDb, targetDb);
	  		/*Connection ssscon=DataSource.getInstance().getSourceDBConnection();
	  		logger.info("Successfully Retrieved a connection " + ssscon);
	  		Connection ssscon2=DataSource.getInstance().getSourceDBConnection();
	  		logger.info("Successfully Retrieved a connection2 " + ssscon2);
	  		Connection ssscon3=DataSource.getInstance().getSourceDBConnection();
	  		logger.info("Successfully Retrieved a connection3 " + ssscon3);
	  		Connection ssscon4=DataSource.getInstance().getSourceDBConnection();
	  		logger.info("Successfully Retrieved a connection4 " + ssscon4);
	  		
	  		ssscon.close();
	  		ssscon2.close();
	  		ssscon3.close();
	  		ssscon4.close();*/
	  		logger.info("Successfully initialized the pool");
	  		//try the pool end
	  		
  			
		} else if("JDBC".equals(appProperties.getConnectionType())) {
			
			/*sourceConn = getConnection(appProperties.getSourceJdbcUrl(),
					jdbcUtil.getDriverClass(appProperties.getSourceDBType().toUpperCase()),
					appProperties.getSourceUserName(), appProperties.getSourceUserPassword());
  			logger.info("Source DB Connection Details: " + sourceConn);
  			
			targetConn = getConnection(appProperties.getTargetJdbcUrl(), jdbcUtil.getDriverClass("POSTGRESQL"),
					appProperties.getTargetUserName(), appProperties.getTargetUserPassword());*/
			
			DataSource.getInstance().initializePool(appProperties.getSourceJdbcUrl(),
					JdbcUtil.getDriverClass(appProperties.getSourceDBType().toUpperCase()),
					appProperties.getSourceUserName(), appProperties.getSourceUserPassword(),
					appProperties.getTargetJdbcUrl(), JdbcUtil.getDriverClass("POSTGRESQL"),
					appProperties.getTargetUserName(), appProperties.getSourceUserPassword(),
					appProperties.getConnectionPoolMinSize(), appProperties.getConnectionPoolMaxSize());
  			logger.info("Target DB Connection Details: " + targetConn);
		}
		
		//setSourceConn(sourceConn);
		//setTargetConn(targetConn); 
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
	 * @param schemaName
	 * @param tableName
	 * @param columnList
	 * @return
	 */
	public CompareResult compare(AppProperties appProperties,
			String schemaName,String tableName,List<String> columnList) throws Exception {
			return compareDetailData(appProperties, schemaName, tableName, columnList);

	}
	
	/**
	 * 
	 * @param appProperties
	 * @param schemaName
	 * @param tableName
	 * @param columnList
	 * @return
	 */
	private CompareResult compareDetailData(AppProperties appProperties,
			String schemaName, String tableName, List<String> columnList) throws Exception {

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
			checkIfTableExistsInPg(appProperties.getTargetSchemaName().toLowerCase(), tableName.toLowerCase(), "POSTGRESQL"/*, targetConn*/);
            long rowCount=0;


			FetchMetadata fetchSourceMetadata = new FetchMetadata(  tableName,null,null,
					 columnList,
					 appProperties);

			info.append("Schema: ");
			info.append(schemaName);
			info.append(" , Table: ");
			info.append(tableName);


			int i;
			info = new StringBuilder();
		   info.append("\n###############################################################\n");
			logger.info(info.toString());
			ExecutorService executor = Executors.newFixedThreadPool(maxNoofThreads);

			Statement stmt = null;
			ResultSet rs = null;
			Connection con=null;
			try {
				con =  DataSource.getInstance().getTargetDBConnection() ;

				//stmt = getConnection().createStatement();
				stmt = con.createStatement();
				 start = System.currentTimeMillis();
				long keySize = 0;
				long valSize = 0;
				String query = appProperties.getSql();
				//logger.debug(query);
				if(query!=null && !query.trim().equals("")) {
					rs = stmt.executeQuery(query);
				}
			} catch (SQLException ex) {

				ex.printStackTrace();
				logger.error("DB", ex);

			} finally {

				JdbcUtil jdbcUtil = new JdbcUtil();

				JdbcUtil.closeResultSet(rs);
				JdbcUtil.closeStatement(stmt);
				JdbcUtil.closeConnection(con);
				//logger.info("Statement execution completed"+chunk );
			}
		} catch (SQLException ex) {

			ex.printStackTrace();
			logger.error("db", ex);

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
	 * @param schemaName
	 * @param tableName
	 * @param dbType
	 * @throws Exception
	 */
	private void checkIfTableExistsInPg(String schemaName, String tableName, String dbType) throws Exception {
		
		ResultSet rs = null;
		Connection conn=null;
		try {
			conn=DataSource.getInstance().getTargetDBConnection();
			rs = conn.getMetaData().getTables(null, schemaName, tableName , null);
			
			if (!rs.next()) {
				throw new Exception("Table " + schemaName + "." + tableName + " not found for "+ dbType + " DATABASE");
			}
			
		} catch(SQLException ex) {
			
			logger.error(ex.getMessage(), ex);
			
		} finally {
			
			new JdbcUtil().closeResultSet(rs);
			JdbcUtil.closeConnection(conn);
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

				String detailedReportCsvFileNameWithPath= getDetailedReportCsvFileNameWithPath(schemaName,dto.getTableName(), CompareController.reportOutputFolder);
				convertDetailedReportHtmlFileToCsvFile(bw,detailedReportCsvFileNameWithPath,displayCompleteData) ;

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}


	/**
	 * +
	 *
	 * @param schemaName
	 * @param tableName
	 * @return detailedReportCsvFileName
	 */
	private String getDetailedReportCsvFileName(String schemaName, String tableName) {

		String detailedReportCsvFileName = "";
		try {
			StringBuilder detailedReportCsvFileNameSbr = new StringBuilder();
			detailedReportCsvFileNameSbr.append(schemaName.toLowerCase());
			detailedReportCsvFileNameSbr.append("_");
			detailedReportCsvFileNameSbr.append(tableName.toLowerCase());
			detailedReportCsvFileNameSbr.append("_table_comparison_result_");
			detailedReportCsvFileNameSbr.append(new DateUtil().getAppendDateToFileName(new Date()));
			detailedReportCsvFileNameSbr.append(".csv");

			detailedReportCsvFileName = detailedReportCsvFileNameSbr.toString();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return detailedReportCsvFileName;

	}

	/**
	 * +
	 *
	 * @param schemaName
	 * @param tableName
	 * @param reportOutputFolder
	 * @return detailedReportCsvFileNameWithPath
	 */
	private String getDetailedReportCsvFileNameWithPath(String schemaName, String tableName, String reportOutputFolder) {

		String detailedReportCsvFileNameWithPath = "";
		try {
			StringBuilder detailedReportCsvFileName = new StringBuilder();
			detailedReportCsvFileName.append(schemaName.toLowerCase());
			detailedReportCsvFileName.append("_");
			detailedReportCsvFileName.append(tableName.toLowerCase());
			detailedReportCsvFileName.append("_table_comparison_result_");
			detailedReportCsvFileName.append(new DateUtil().getAppendDateToFileName(new Date()));
			detailedReportCsvFileName.append(".csv");
			detailedReportCsvFileNameWithPath = detailedReportCsvFileName.toString();
			if (null != reportOutputFolder && !reportOutputFolder.trim().isEmpty()) {
				detailedReportCsvFileNameWithPath = reportOutputFolder + "/" + detailedReportCsvFileNameWithPath;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return detailedReportCsvFileNameWithPath;
	}

	/**
	 * +
	 *
	 * @param compareResultInHtmlFormat
	 * @param detailedReportCsvFileNameWithPath
	 * @param displayCompleteData
	 */
	public void convertDetailedReportHtmlFileToCsvFile(StringBuilder compareResultInHtmlFormat, String detailedReportCsvFileNameWithPath, boolean displayCompleteData) {
		try {

			FileWriter detailedReportCsvFileWriter = new FileWriter(detailedReportCsvFileNameWithPath);
			Document htmlDocument = Jsoup.parseBodyFragment(compareResultInHtmlFormat.toString());

			String docHeader = htmlDocument.getElementsByTag("th").toString();
			if (docHeader.contains("REASON OF FAILURE") && docHeader.contains("SOURCE TUPLE") && docHeader.contains("TARGET TUPLE")) {

				//add headers in csv
				if (!htmlDocument.getElementsByTag("th").isEmpty()) {
					Elements headers = htmlDocument.getElementsByTag("th");
					for (Element header : headers) {
						detailedReportCsvFileWriter.write("\"" + header.text() + "\",");
					}
					detailedReportCsvFileWriter.write("\n");
				}

				Elements rows = htmlDocument.getElementsByTag("tr");
				for (Element row : rows) {

					//add row data in csv
					if (!row.getElementsByTag("td").isEmpty()) {

						//add column0 data in row
						Element column0 = row.getElementsByTag("td").get(0);
						if (!column0.text().isEmpty()) {
							detailedReportCsvFileWriter.write("\"" + column0.text() + "\"");
						}

						//add column1 data in row
						Element column1 = row.getElementsByTag("td").get(1);
						if (column1.text().isEmpty()) {
							detailedReportCsvFileWriter.write("," + ",");
						} else if (column1.text().contains("||")) {
							String cellTextAsSingleRow = column1.text().replaceAll("\\s\\|\\|\\s", ",");
							cellTextAsSingleRow = cellTextAsSingleRow.replaceAll("\\|\\|", "");
							if (cellTextAsSingleRow.contains(" , , , ")) {
								String cellTextAsNewRow = cellTextAsSingleRow.replaceAll("\\s\\,\\s\\,\\s\\,\\s", "\",\n,\"");
								detailedReportCsvFileWriter.write("," + "\"" + cellTextAsNewRow + "\"" + ",");
							} else {
								detailedReportCsvFileWriter.write("," + "\"" + cellTextAsSingleRow + "\"" + ",");
							}

						} else {
							detailedReportCsvFileWriter.write("," + "\"" + column1.text() + "\"" + ",");
						}

						//add column2 data in row
						Element column2 = row.getElementsByTag("td").get(2);
						if (column2.text().isEmpty()) {
							detailedReportCsvFileWriter.write(",");
						} else if (column2.text().contains("||")) {
							String cellTextAsSingleRow = column2.text().replaceAll("\\s\\|\\|\\s", ",");
							cellTextAsSingleRow = cellTextAsSingleRow.replaceAll("\\|\\|", "");
							if (cellTextAsSingleRow.contains(" , , , ")) {
								String cellTextAsNewRow = cellTextAsSingleRow.replaceAll("\\s\\,\\s\\,\\s\\,\\s", "\",\n,\"");
								detailedReportCsvFileWriter.write("\"" + cellTextAsNewRow + "\"" + ",");
							} else {
								detailedReportCsvFileWriter.write("\"" + cellTextAsSingleRow + "\"" + ",");
							}
						} else {

							detailedReportCsvFileWriter.write("\"" + column2.text() + "\"" + ",");
						}

					}
					detailedReportCsvFileWriter.write("\n");
				}
			} else {

				//add headers in csv
				if (!htmlDocument.getElementsByTag("th").isEmpty()) {
					Elements headers = htmlDocument.getElementsByTag("th");
					for (Element header : headers) {
						detailedReportCsvFileWriter.write("\"" + header.text() + "\",");
					}
					detailedReportCsvFileWriter.write("\n");
				}

				Elements rows = htmlDocument.getElementsByTag("tr");
				for (Element row : rows) {
					//add row data in csv
					Elements columns = row.getElementsByTag("td");
					for (Element column : columns) {
						String columnText = column.text();
						if (columnText.contains("||")) {
							if (columnText.contains(" || || || ")) {
								String columnTextAsMultiRow = columnText.replaceAll("\\s\\|\\|\\s\\|\\|\\s\\|\\|\\s", " \",\n,\" ");
								columnTextAsMultiRow = columnTextAsMultiRow.replaceAll("\\s\\|\\|\\s", ",");
								columnTextAsMultiRow = columnTextAsMultiRow.replaceAll("\\|\\|", "");
								detailedReportCsvFileWriter.write("\"" + columnTextAsMultiRow + "\"");
							} else if (displayCompleteData) {
								String columnTextAsSingleRow = columnText.replaceAll("\\s\\|\\|\\s", ",");
								columnTextAsSingleRow = columnTextAsSingleRow.replaceAll("\\|\\|", "");
								detailedReportCsvFileWriter.write("\"" + columnTextAsSingleRow + "\"");
							} else if (!displayCompleteData) {
								String columnTextAsMultiRow = columnText.replaceAll("\\s\\|\\|\\s", "\",\n,\"");
								columnTextAsMultiRow = columnTextAsMultiRow.replaceAll("\\|\\|", "");
								detailedReportCsvFileWriter.write("\"" + columnTextAsMultiRow + "\"");
							} else {
								detailedReportCsvFileWriter.write("\"" + columnText + "\"");
							}
						} else if (columnText.isEmpty()) {
							detailedReportCsvFileWriter.write(columnText.concat(","));
						} else {
							detailedReportCsvFileWriter.write("\"" + columnText + "\",");
						}
					}
					detailedReportCsvFileWriter.write("\n");
				}
			}
			detailedReportCsvFileWriter.close();
			logger.info(detailedReportCsvFileNameWithPath + " written successfully");
		} catch (Exception e) {
			logger.error("Error while creating detailed report csv file");
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * +
	 *
	 * @param appendDateToFileNames
	 * @param jobName
	 * @param reportOutputFolder
	 * @return summaryReportCsvFileNameWithPath
	 */
	private String getSummaryReportCsvFileNameWithPath(String appendDateToFileNames, String jobName, String reportOutputFolder) {
		String summaryReportCsvFileName = jobName + "_" + appendDateToFileNames + ".csv";
		String summaryReportCsvFileNameWithPath = summaryReportCsvFileName;
		if (null != reportOutputFolder && !reportOutputFolder.trim().isEmpty()) {
			summaryReportCsvFileNameWithPath = reportOutputFolder + "/" + summaryReportCsvFileName;
		}
		return summaryReportCsvFileNameWithPath;
	}

	/**
	 * +
	 *
	 * @param compareResultInHtmlFormat
	 * @param summaryReportCsvFileNameWithPath
	 */
	public void convertSummaryReportHtmlFileToCsvFile(StringBuilder compareResultInHtmlFormat, String summaryReportCsvFileNameWithPath) {
		try {
			FileWriter summaryReportCsvFileWriter = new FileWriter(summaryReportCsvFileNameWithPath);
			Document htmlDocument = Jsoup.parseBodyFragment(compareResultInHtmlFormat.toString());
			Elements rows = htmlDocument.getElementsByTag("tr");

			for (Element row : rows) {

				//if (row.getElementsByTag("table").isEmpty()) {
				//if (row.toString().contains("Detailed Report") || row.toString().contains("Summary Report")) {

				if (!row.getElementsByTag("table").isEmpty() || row.toString().contains("Detailed Report") || row.toString().contains("Summary Report")) {
					continue;
				}
				Elements columns = row.getElementsByTag("td");
				for (Element column : columns) {
					summaryReportCsvFileWriter.write(column.text().concat(", "));
				}
				summaryReportCsvFileWriter.write("\n");
				//}
				if (!row.getElementsContainingText("Sql Filter Used").isEmpty()) {
					summaryReportCsvFileWriter.write("\n");
				}
			}
			summaryReportCsvFileWriter.close();
			logger.info(summaryReportCsvFileNameWithPath + " written successfully");
		} catch (Exception e) {
			logger.error("Error while creating summary report csv file");
			logger.error(e.getMessage(), e);
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
				bw.append(" || ");
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
				bw.append(" || ");
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
	 * @param columnList
	 * @return
	 */
	public List<CompareResult> compareSchema(AppProperties appProperties,
			List<String> columnList) throws Exception {

		List<CompareResult> tableList = new ArrayList<CompareResult>();
		List<String> tableNames = new ArrayList<String>();

		ResultSet rs = null;
		Connection sourceConn=null;
		
		try {
			sourceConn =DataSource.getInstance().getTargetDBConnection();
			
			List<String> ignoreTables = (appProperties.getTableName() != null && !appProperties.getTableName().isEmpty()
					&& appProperties.isIgnoreTables()) ? Arrays.asList(appProperties.getTableName().split(","))
					: new ArrayList<String>();

			String[] types = {"FOREIGN"};
			rs = sourceConn.getMetaData().getTables(appProperties.getTargetDBName().toLowerCase(), appProperties.getSchemaName().toLowerCase(), null, null);

			while (rs.next()) {
				String tableName = rs.getString("TABLE_NAME");

				if (ignoreTable(ignoreTables, tableName) || tableName.toLowerCase().contains("awsdms") || tableName.trim().equals("") || !tableName.trim().startsWith("ppt")) continue;
				tableNames.add(tableName);
			}

		} catch (SQLException ex) {

			logger.error(ex.getMessage(), ex);

		} finally {

			new JdbcUtil().closeResultSet(rs);
			JdbcUtil.closeConnection(sourceConn);
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
				info.append(appProperties.getSchemaName());
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
				CompareResult dto = compare(appProperties, /*sourceConn, targetConn,*/ appProperties.getSchemaName(), tableName, columnList);
				//if (dto.getReason() == null && !(dto.getResult() != null && "Completed".equals(dto.getResult())) ) {
					//dto.setTableName(tableName);
				if (dto.getReason() == null && !(dto.getResult() != null && "Completed".equals(dto.getResult()))) {
					dto.setTableName(tableName);
					dto.setReason("Table " + appProperties.getSchemaName() + "." + tableName + " unable to compare.");
					dto.setResult("Failed");
				}

				tableList.add(dto);

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

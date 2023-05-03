/**
 * Service class to compare the database table data between Source( Oracle) and Traget Database( like Postgres)
 *
 *
 * @author      Harnath Valeti
 * @author      Madhu Athinarapu
 * @version     1.0
 * @since       1.0
 */

package com.datavalidationtool.service;


import com.datavalidationtool.dao.DataSource;
import com.datavalidationtool.model.DatabaseInfo;
import com.datavalidationtool.model.RunDetails;
import com.datavalidationtool.model.request.ValidationRequest;
import com.datavalidationtool.model.response.LastRunDetails;
import com.datavalidationtool.model.response.RecommendationResponse;
import com.datavalidationtool.model.response.RecommendationRow;
import com.datavalidationtool.model.response.RunInfo;
import com.datavalidationtool.util.JdbcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ValidationService {
	public Logger logger = LoggerFactory.getLogger("CompareService");
	@Autowired
	public DataSource dataSource;
	@Autowired
	public RecommendationService recommendationService;

	/**
	 *
	 * @param validationRequest
	 */
	public String validateData(ValidationRequest validationRequest) {
		String runIds="";
		String runId="";
		try {
			String[] schemaParts=null;
            boolean isSchemaLevel=false;
			if(validationRequest.getSourceSchemaName()!=null) {
				schemaParts = validationRequest.getSourceSchemaName().split(",");
				for (String schemaName : schemaParts) {
					String[] schemas = schemaName.split(":");
					if (schemas.length > 0)
						validationRequest.setSourceSchemaName(schemas[0]);
					if (schemas.length > 1)
						validationRequest.setTargetSchemaName(schemas[1].toLowerCase());
				}
			}
	  			long rowNo = 1;
				if (validationRequest.getTableNames() != null && validationRequest.getTableNames().length>0
						&& !validationRequest.isIgnoreTables()) {
					//getCurrentSchemaRunInfo(validationRequest);
					validationRequest.setSchemaRunNumber(0);
					String[] tableNameParts = validationRequest.getTableNames();
					ExecutorService executor = Executors.newFixedThreadPool(tableNameParts.length);
					CompareData compareData=null;
					for (String tableName : tableNameParts) {
						validationRequest.setTableName(tableName);
						validationRequest.setRunId(UUID.randomUUID().toString());
						if (validationRequest.getChunkSize() == 0){
							compareData = new CompareData(validationRequest, dataSource, tableName,null);
						   executor.execute(compareData);
					}else{
						generateChunks(validationRequest);
					}
						addRunDetails(validationRequest);
					}
					executor.shutdown();
					while (!executor.isTerminated()) {
						//appProperties.setTableName(tableName);
					}
					//get last one
					if (compareData != null) {
						String runIdTmp = compareData.getRunId();
						runId = runId + "\n" + runIdTmp;
					}
  				} else {
					getCurrentSchemaRunInfo(validationRequest);
  					for (String schemaName : schemaParts) {
						 runId= compareSchema(validationRequest, /*getSourceConn(), getTargetConn(),*/  null);
					}
  				}
	  	} catch (Exception ex) {
	  		logger.error(ex.getMessage(), ex);
	  	} finally {
	  		JdbcUtil jdbcUtil = new JdbcUtil();
		}
    return runId;
	}

	private void addRunDetails(ValidationRequest validationRequest) {

			RunDetails runDetails = null;
			try {
			runDetails = getCurrentTableRunInfo(validationRequest);
			runDetails.setSchemaRun(validationRequest.getSchemaRunNumber());
			runDetails.setRunId(validationRequest.getRunId());
			runDetails.setSourceHostName(validationRequest.getTargetHost());
			runDetails.setTargetHostName(validationRequest.getTargetHost());
			runDetails.setDatabaseName(validationRequest.getTargetDBName());
			runDetails.setSchemaName(validationRequest.getSourceSchemaName());
			runDetails.setTableName(validationRequest.getTableName());
			runDetails.setUniqueColumns(validationRequest.getUniqueCols());
			runDetails.setDataFilters(validationRequest.getDataFilters());
			runDetails.setChunkColumn(validationRequest.getChunkColumns());
			runDetails.setChunkSize(validationRequest.getChunkSize());
			runDetails.setIncremental(validationRequest.isIncremental());
			runDetails.setUniqueColumns(validationRequest.getUniqueCols());
			runDetails.setDataFilters(validationRequest.getDataFilters());
			runDetails.setChunkColumn(validationRequest.getChunkColumns());
			runDetails.setChunkSize(validationRequest.getChunkSize());
			runDetails.setIncremental(validationRequest.isIncremental());
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			runDetails.setExecutionTime(timestamp);
				addRunDetailsForSelection(runDetails);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


	private void generateChunks(ValidationRequest valRequest) throws SQLException {
		Long totalRecords = getTableCount(valRequest);
		Connection dbConn=null;
		getTablePrimaryKeys(valRequest);
		int ntileSize =  valRequest.getChunkSize();
		ntileSize = (ntileSize <= 0) ? 1 : ntileSize;
		StringBuilder sql = new StringBuilder();
		String pKey=valRequest.getPrimaryKeys()!=null ? valRequest.getPrimaryKeys():valRequest.getChunkColumns();
		/*if(isHasNoUniqueKey()) {
			sql.append("SELECT row_number()  OVER (ORDER BY ").append(cols).append(" ) nt FROM ").append(schemaName)
					.append(".").append(tableName);
		} else {*/
		String filterCondition= valRequest.getDataFilters()!=null && !valRequest.getDataFilters().isEmpty()?("  where "+valRequest.getDataFilters()):"";
			sql.append("SELECT min(").append(pKey).append(") AS startRange, max(").append(pKey)
					.append(") AS endRange,count(*) AS chunkSize, nt FROM (SELECT ").append(pKey).append(" ,ntile(")
					.append(ntileSize).append(") OVER (ORDER BY ").append(pKey).append(" ) nt FROM ").append(valRequest.getSourceSchemaName())
					.append(".").append(valRequest.getTableName()).append( filterCondition).append(" ) as a GROUP BY nt ORDER BY nt");
		//}

		logger.info("Fetch Chunks SQL Query: " + sql.toString());
		dbConn= dataSource.getDBConnection();
		Statement stmt = dbConn.createStatement();
		ResultSet rs = stmt.executeQuery(sql.toString());
		ExecutorService executor = Executors.newFixedThreadPool(ntileSize);

		String runId="";
		int count=0;
		while (rs.next()) {
			CompareData compareData=null;
			int columnType= rs.getMetaData().getColumnType(1);
			long startRange=0;
			long endRange=0;
			startRange = rs.getLong("startRange");
			endRange = rs.getLong("endRange");
			long chunkSize = rs.getLong("chunkSize");
			StringBuilder condition = new StringBuilder();
			condition.append(pKey).append(" >=").append(startRange).append(" and ").append(pKey).append(" <= ").append(endRange);
			//valRequest.setDataFilters(condition.toString());
			compareData=new CompareData(valRequest,dataSource,valRequest.getTableName(),condition.toString());
			executor.execute(compareData);
			String runIdTmp= compareData.getRunId();
			runId=runId+"\n"+runIdTmp;
			}
			executor.shutdown();
			while (!executor.isTerminated()) {
				//appProperties.setTableName(tableName);
			}

	}


	public RunDetails getCurrentSchemaRunInfo(ValidationRequest appProperties) throws Exception {
		RunDetails runDetails = new RunDetails();
		String query = "SELECT max(schema_run) FROM public.run_details where target_host_name=? and database_name=? and schema_name=?";
		Connection dbConn=null;
		PreparedStatement pst =null;
		try {
			dbConn= dataSource.getDBConnection();
			pst = dbConn.prepareStatement(query);
			pst.setString(1,appProperties.getTargetHost());
			pst.setString(2,appProperties.getTargetDBName());
			pst.setString(3,appProperties.getSourceSchemaName());
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				// If it is a schema run
				if(appProperties.getTableName()==null || appProperties.getTableName().isBlank()) {
					appProperties.setSchemaRunNumber(rs.getInt(1)+1);
				}else{
					appProperties.setSchemaRunNumber(0);
				}
			}

		} catch (SQLException ex) {
			logger.error("Exception while fetching table details");
			logger.error(ex.getMessage());
		}
		finally {
			pst.close();
			if(dbConn!=null)
			dbConn.close();
		}
		return runDetails;
	}

	public RunDetails getCurrentTableRunInfo(ValidationRequest appProperties) throws Exception {
		RunDetails runDetails = new RunDetails();
		String query = "SELECT max(table_run) FROM public.run_details where target_host_name=? and database_name=? and schema_name=? and table_name=?";
		Connection dbConn=null;
		PreparedStatement pst =null;
		try {
			dbConn= dataSource.getDBConnection();
			pst = dbConn.prepareStatement(query);
			pst.setString(1,appProperties.getTargetHost());
			pst.setString(2,appProperties.getTargetDBName());
			pst.setString(3,appProperties.getSourceSchemaName());
			pst.setString(4,appProperties.getTableName());
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				// If it is a schema run
				if(appProperties.getTableName()==null || appProperties.getTableName().isBlank()) {
					runDetails.setTableRun(rs.getInt(1) + 1);
				}else{
					runDetails.setTableRun(rs.getInt(1) + 1);
				}
			}

		} catch (SQLException ex) {
			logger.error("Exception while fetching table details");
			logger.error(ex.getMessage());
		}
		finally {
			pst.close();
			if(dbConn != null)
				dbConn.close();
		}
		return runDetails;
	}

	public List<RunDetails> addRunDetailsForSelection(RunDetails runDetails) throws Exception {
		Connection dbConn=null;
		PreparedStatement pst =null;
		List<RunDetails> outputRunDetailsList = new ArrayList<>();
		String query = "insert into public.run_details(source_host_name,target_host_name,database_name,schema_name,table_name,schema_run,table_run,run_id,execution_date, data_filter, unique_columns, chunk_column, chunk_size,incremental) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			dbConn= dataSource.getDBConnection();
			pst = dbConn.prepareStatement(query);
			pst.setString(1,runDetails.getSourceHostName());
			pst.setString(2,runDetails.getTargetHostName());
			pst.setString(3,runDetails.getDatabaseName());
			pst.setString(4,runDetails.getSchemaName());
			pst.setString(5,runDetails.getTableName());
			pst.setInt(6,runDetails.getSchemaRun());
			pst.setInt(7,runDetails.getTableRun());
			pst.setString(8,runDetails.getRunId());
			pst.setTimestamp(9,runDetails.getExecutionTime());
			pst.setString(10, runDetails.getDataFilters());
			pst.setString(11,runDetails.getUniqueColumns());
			pst.setString(12,runDetails.getChunkColumn());
			pst.setInt(13,runDetails.getChunkSize());
			pst.setBoolean(14,runDetails.isIncremental());
			int count= pst.executeUpdate();
            logger.info("added run details count",count);
		} catch (SQLException ex) {
			logger.error("Exception while adding table details");
			logger.error(ex.getMessage());
		}finally {
			pst.close();
			if(dbConn!=null)
			dbConn.close();
		}
		return outputRunDetailsList;
	}
	
	/**
	 * 
	 * @param appProperties
	 * @throws Exception
	 */
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
			//setSslProperties(props,trustStorePath, trsutStorePassword);
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
			conn=dataSource.getDBConnection();
			rs = conn.getMetaData().getTables(null, schemaName, tableName , null);
			if (!rs.next()) {
				throw new Exception("Table " + schemaName + "." + tableName + " not found for "+ dbType + " DATABASE");
			}
		} catch(SQLException ex) {
			logger.error(ex.getMessage(), ex);
		} finally {
			if(rs!=null)
				rs.close();
			if(conn!=null)
				conn.close();
		} 
	}
	/**
	 * 
	 * @param appProperties
	 * @param columnList
	 * @return
	 */
	public String compareSchema(ValidationRequest appProperties,
			List<String> columnList) throws Exception {
		List<String> tableNames = new ArrayList<String>();
		ResultSet rs = null;
		Connection sourceConn=null;
		String runId="";
		try {
			sourceConn =dataSource.getDBConnection();
			List<String> ignoreTables = (appProperties.getTableNames() != null && appProperties.getTableNames().length>0
					&& appProperties.isIgnoreTables()) ? Arrays.asList(appProperties.getTableNames())
					: new ArrayList<String>();
			rs = sourceConn.getMetaData().getTables(appProperties.getTargetDBName().toLowerCase(), appProperties.getSourceSchemaName().toLowerCase(), null, null);
			while (rs.next()) {
				String tableName = rs.getString("TABLE_NAME");
				if (ignoreTable(ignoreTables, tableName) || tableName.toLowerCase().contains("awsdms") || tableName.trim().equals("") ) continue;
				tableNames.add(tableName);
			}
		} catch (SQLException ex) {
			logger.error(ex.getMessage(), ex);
		} finally {

			if(rs!=null)
				rs.close();
			if(sourceConn!=null)
			sourceConn.close();
		}
		if (!tableNames.isEmpty()) {
			ExecutorService executor = Executors.newFixedThreadPool(tableNames.size());
			CompareData compareData=null;
			for (String tableName : tableNames) {
				compareData = new CompareData(appProperties,dataSource,tableName,null);
			}
			executor.shutdown();
			while (!executor.isTerminated()) {
			}
			//get last one
			String runIdTmp= compareData.getRunId();
			runId=runId+"\n"+runIdTmp;
		}
		return runId;
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

	public RunInfo getRunInfo(ValidationRequest inputRunDetails) throws Exception {
		long rowCount= getTableCount(inputRunDetails);
		RunDetails runDetails= RunDetails.builder().schemaName(inputRunDetails.getSourceSchemaName()).tableName(inputRunDetails.getTableName()).validationRequest(true).build();
		RecommendationResponse recommendationResponse = recommendationService.getRecommendationResponseV2(runDetails);
		inputRunDetails.setRunDetailsLimit(1);
		RunInfo runInfo=getLastRunDetails(inputRunDetails).getRuns().get(0);//buildRunInfo(recommendationResponse,rowCount);
		return runInfo;
	}

	public LastRunDetails getLastRunDetails(ValidationRequest inputRunDetails) throws Exception {
		ArrayList<RunInfo> list= new ArrayList<RunInfo>();
		LastRunDetails lastRunDetails=new LastRunDetails();
		RunDetails runDetail= RunDetails.builder().schemaName(inputRunDetails.getSourceSchemaName()).tableName(inputRunDetails.getTableName()).validationRequest(true).build();
		List<RunDetails> rundetails=getLastRunResults(inputRunDetails.getRunDetailsLimit()!=0?inputRunDetails.getRunDetailsLimit():10);
		HashMap<String,Long> tableList=new HashMap<String,Long>();
		for(RunDetails runId:rundetails ) {
			Long rowCount= 0L;
			runId.setValidationRequest(true);
			RecommendationResponse recommendationResponse = recommendationService.getRecommendationResponseV2(runId);
			inputRunDetails.setTableName(runId.getTableName());
			if(tableList.get(runId.getTableName())!=null){
				rowCount=tableList.get(runId.getTableName());
			}else
			 rowCount= getTableCount(inputRunDetails);
			RunInfo runInfo = buildRunInfo(recommendationResponse, rowCount);
			runInfo.setTable(runId.getTableName());
			runInfo.setLastRunDate(runId.getExecutionDate());
			runInfo.setUniqueColumns(runId.getUniqueColumns());
			runInfo.setDataFilters(runId.getDataFilters());
			runInfo.setChunkColumns(runId.getChunkColumn());
			runInfo.setChunkSize(runId.getChunkSize());
			runInfo.setIncremental(runId.isIncremental());
			tableList.put(runId.getTableName(),rowCount);
			list.add(runInfo);
		}
		lastRunDetails.setRuns(list);
		lastRunDetails.setSchemaName(runDetail.getSchemaName());
		return lastRunDetails;

	}

	private List<RunDetails> getLastRunResults(int limit) throws SQLException {
		List<RunDetails> outputRunDetailsList = new ArrayList<>();
		String query = "select * FROM public.run_details order by execution_date desc limit "+limit;
		Connection dbConn =null;
		PreparedStatement pst =null;
		try { dbConn =dataSource.getDBConnection();
			pst = dbConn.prepareStatement(query);
			ResultSet rs = pst.executeQuery();

			while (rs.next()) {

				RunDetails runDetails = new RunDetails();
				runDetails.setSourceHostName(rs.getString("source_host_name"));
				runDetails.setTargetHostName(rs.getString("target_host_name"));
				runDetails.setDatabaseName(rs.getString("database_name"));
				runDetails.setSchemaName(rs.getString("schema_name"));
				runDetails.setTableName(rs.getString("table_name"));
				runDetails.setSchemaRun(rs.getInt("schema_run"));
				runDetails.setTableRun(rs.getInt("table_run"));
				runDetails.setRunId(rs.getString("run_id"));
				runDetails.setExecutionDate(rs.getString("execution_date"));
				runDetails.setUniqueColumns(rs.getString("unique_columns"));
				runDetails.setDataFilters(rs.getString("data_filter"));
				runDetails.setChunkColumn(rs.getString("chunk_column"));
			    runDetails.setChunkSize(rs.getInt("chunk_size"));
				runDetails.setIncremental(rs.getBoolean("incremental"));
				outputRunDetailsList.add(runDetails);
			}

		} catch (SQLException ex) {
			logger.error("Exception while fetching table details");
			logger.error(ex.getMessage());
		}finally {
			pst.close();
			dbConn.close();
		}
		return outputRunDetailsList;
	}



	private RunInfo buildRunInfo(RecommendationResponse recommendationResponse, long rowCount) {
		RunInfo runInfo=RunInfo.builder().totalRecords(rowCount).build();
		long missing=0;
		long mismatch=0;
		for(RecommendationRow row : recommendationResponse.getRows())
		{
          if((Integer) row.getRecommendationCode()==1)
			  missing++;
		  else if((Integer) row.getRecommendationCode()==2)
			  mismatch++;
		  else if((Integer) row.getRecommendationCode()==4)
			runInfo.setDuration(row.getDurationText());
		}
		runInfo.setMismatchRows(mismatch);
		runInfo.setMissingRows(missing);
		return runInfo;
	}

	private long getTableCount(ValidationRequest inputRunDetails) throws SQLException {
		String query = "SELECT count(*) from "+ inputRunDetails.getSourceSchemaName()+"."+inputRunDetails.getTableName();
		long count=0;
		Connection dbConn=null;
		try {
			dbConn= dataSource.getDBConnection();
			Statement pst = dbConn.createStatement();
			ResultSet rs = pst.executeQuery(query);
			while (rs.next()) {
				// If it is a schema run
				count=rs.getLong(1);
			}

		} catch (SQLException ex) {
			logger.error("Exception while fetching table details");
			logger.error(ex.getMessage());
		}
		finally {
			if(dbConn!=null)
			dbConn.close();
		}
		return count;

	}
private void getTablePrimaryKeys(ValidationRequest valRequest) throws SQLException {
	Map<Integer, String> primaryKeyMap= new HashMap<Integer, String>();
	StringBuffer primaryKey=new StringBuffer();
	Connection dbConn=null;
	ResultSet rs =null;
	try {
		dbConn= dataSource.getDBConnection();
		rs = dbConn.getMetaData().getPrimaryKeys(null, valRequest.getSourceSchemaName().toLowerCase(), valRequest.getTableName());
		while (rs.next()) {
			String colName = rs.getString("COLUMN_NAME");
			Integer pkPosition = rs.getInt("KEY_SEQ");
			primaryKey.append(colName).append(",");
		}
		//primaryKey.deleteCharAt(primaryKey.length()-1);
		if(primaryKey.length()>0) {
			valRequest.setPrimaryKeys(primaryKey.substring(0, (primaryKey.length() - 2)));
		}else{
			valRequest.setPrimaryKeys(valRequest.getUniqueCols());
		}

} catch (SQLException ex) {
		logger.error(ex.getMessage(), ex);
	} finally {
		if(rs!=null)
			rs.close();
		if(dbConn!=null)
			dbConn.close();
	}
}

}

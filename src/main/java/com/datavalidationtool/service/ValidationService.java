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
import com.datavalidationtool.model.DatabaseInfo.dbType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.Date;
import java.util.*;
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
		String runId="";
		try {
            boolean isSchemaLevel=false;
			String[] schemaParts = validationRequest.getSourceSchemaName().split(",");
			for (String schemaName : schemaParts) {
				String[] schemas = schemaName.split(":");
				if (schemas.length > 0)
					validationRequest.setSourceSchemaName(schemas[0]);
				if (schemas.length > 1)
					validationRequest.setTargetSchemaName(schemas[1].toLowerCase());
			}

  		//	if(dataSource.isPoolInitialized()) {
	  			long rowNo = 1;
  				Date date = new Date();
  				//DateUtil dateUtil = new DateUtil();
				if (validationRequest.getTableName() != null && !validationRequest.getTableName().isEmpty()
						&& !validationRequest.isIgnoreTables()) {
					getCurrentSchemaRunInfo(validationRequest);
  					String[] tableNameParts = validationRequest.getTableName().split(",");
  					for (String tableName : tableNameParts) {
						validationRequest.setTableName(tableName);
						runId=validate(validationRequest,tableName, null);
  					}
		  			
  				} else {
					getCurrentSchemaRunInfo(validationRequest);
  					for (String schemaName : schemaParts) {
						 runId= compareSchema(validationRequest, /*getSourceConn(), getTargetConn(),*/  null);
					}
  				}
		//	} else {
  				//logger.info("Either Source or Target DB connection is not established.");
  			//}
	  	} catch (Exception ex) {
	  		logger.error(ex.getMessage(), ex);
	  	} finally {
	  		JdbcUtil jdbcUtil = new JdbcUtil();
		}
    return runId;
	}
	public RunDetails getCurrentSchemaRunInfo(ValidationRequest appProperties) throws Exception {
		RunDetails runDetails = new RunDetails();
		String query = "SELECT max(schema_run) FROM public.run_details where target_host_name=? and database_name=? and schema_name=?";
		Connection dbConn=null;
		try {
			dbConn= dataSource.getDBConnection();
			PreparedStatement pst = dbConn.prepareStatement(query);
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
			if(dbConn!=null)
			dbConn.close();
		}
		return runDetails;
	}

	public RunDetails getCurrentTableRunInfo(ValidationRequest appProperties) throws Exception {
		RunDetails runDetails = new RunDetails();
		String query = "SELECT max(table_run) FROM public.run_details where target_host_name=? and database_name=? and schema_name=? and table_name=?";
		Connection dbConn=null;
		try {
			dbConn= dataSource.getDBConnection();
			PreparedStatement pst = dbConn.prepareStatement(query);
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
			if(dbConn!=null)
				dbConn.close();
		}
		return runDetails;
	}

	public List<RunDetails> addRunDetailsForSelection(RunDetails runDetails) throws Exception {
		Connection dbConn=null;
		List<RunDetails> outputRunDetailsList = new ArrayList<>();
		String query = "insert into public.run_details(source_host_name,target_host_name,database_name,schema_name,table_name,schema_run,table_run,run_id,execution_date) values(?,?,?,?,?,?,?,?,?)";
		try {
			dbConn= dataSource.getDBConnection();
			PreparedStatement pst = dbConn.prepareStatement(query);
			pst.setString(1,runDetails.getSourceHostName());
			pst.setString(2,runDetails.getTargetHostName());
			pst.setString(3,runDetails.getDatabaseName());
			pst.setString(4,runDetails.getSchemaName());
			pst.setString(5,runDetails.getTableName());
			pst.setInt(6,runDetails.getSchemaRun());
			pst.setInt(7,runDetails.getTableRun());
			pst.setString(8,runDetails.getRunId());
			pst.setTimestamp(9,runDetails.getExecutionTime());
			int count= pst.executeUpdate();
            logger.info("added run details count",count);
		} catch (SQLException ex) {
			logger.error("Exception while adding table details");
			logger.error(ex.getMessage());
		}finally {
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
	 * @param appProperties
	 * @param tableName
	 * @param columnList
	 * @return
	 */
	private String validate(ValidationRequest appProperties,
			 String tableName, List<String> columnList) throws Exception {

		long start = System.currentTimeMillis();
		StringBuilder info = new StringBuilder();
		String runId="";
        long usedMemory = 0;
		try {
			//checkIfTableExistsInPg(appProperties.getTargetSchemaName().toLowerCase(), tableName.toLowerCase(), "POSTGRESQL"/*, targetConn*/);
            long rowCount=0;
			info.append("Schema: ");
			info.append(appProperties.getTargetSchemaName());
			info.append(" , Table: ");
			info.append(tableName);
			int i;
			info = new StringBuilder();
		   info.append("\n###############################################################\n");
			//logger.info(info.toString());
			Statement stmt = null;
			ResultSet rs = null;
			Connection con=null;

			try {
				con =  dataSource.getDBConnection() ;
				//stmt = getConnection().createStatement();
				stmt = con.createStatement();
				 start = System.currentTimeMillis();
				long keySize = 0;
				long valSize = 0;
				//fn_post_mig_data_validation_dvt2_include_exclude('ops$ora','crtdms','grade','grade','id','','comments',true,false)
				String dbFunction = "{ call fn_post_mig_data_validation_dvt2_include_exclude(?,?,?,?,?,?,?,?,?) }";
				CallableStatement cst = null ;
				try {
					cst = con.prepareCall(dbFunction);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				{
					cst.setString(1, appProperties.getSourceSchemaName());
					cst.setString(2, appProperties.getTargetSchemaName());
					cst.setString(3, appProperties.getTableName().equals("")?tableName:appProperties.getTableName());
					cst.setString(4, appProperties.getTableName().equals("")?tableName:appProperties.getTableName());
					//cst.setString(5, appProperties.getColumns());
					//cst.setString(6, appProperties.getFilter());
					//cst.setString(7, appProperties.getFilter());
					cst.setString(5, appProperties.getColumns()!=null?appProperties.getColumns():"");
					cst.setString(6, appProperties.getDataFilters()!=null?appProperties.getDataFilters():"");
					cst.setString(7, appProperties.getFilterType()!=null?appProperties.getFilterType():"");
					cst.setBoolean(8, appProperties.isIgnoreColumns());
					cst.setBoolean(9, appProperties.isCheckAdditionalRows());
					//cst.registerOutParameter(1, Types.VARCHAR);
				}
				rs= cst.executeQuery();
				while(rs.next()) {
					String result = rs.getString(1);
					logger.info("Table "+tableName+" Validation Status", result);
					if(result.contains("Validation complete for Run Id")) {
						runId = result.substring(31, 63);
					}
				}
				if(!runId.isBlank()) {
					if(appProperties.getTableName().equals(""))
					appProperties.setTableName(tableName);
					RunDetails runDetails = getCurrentTableRunInfo(appProperties);
					runDetails.setSchemaRun(appProperties.getSchemaRunNumber());
					runDetails.setRunId(runId);
					runDetails.setSourceHostName(appProperties.getTargetHost());
					runDetails.setTargetHostName(appProperties.getTargetHost());
					runDetails.setDatabaseName(appProperties.getTargetDBName());
					runDetails.setSchemaName(appProperties.getSourceSchemaName());
					runDetails.setTableName(appProperties.getTableName().equals("")?tableName:appProperties.getTableName());
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					runDetails.setExecutionTime(timestamp);
					addRunDetailsForSelection(runDetails);
				}
			} catch (SQLException ex) {

				ex.printStackTrace();
				logger.error("DB", ex);

			} finally {
				rs.close();
				con.close();
			}
		} catch (SQLException ex) {

			ex.printStackTrace();
			logger.error("db", ex);

		}


		long end = System.currentTimeMillis();
		long timeTaken = end - start;
		info = new StringBuilder();
		info.append("\n----------------------------------------------------\n");
		info.append("Finished writing comparison results for "+timeTaken);
		info.append(appProperties.getSourceSchemaName());
		info.append(".");
		info.append(tableName);
		logger.info(info.toString());
		return runId;
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
		rs.close();
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
			List<String> ignoreTables = (appProperties.getTableName() != null && !appProperties.getTableName().isEmpty()
					&& appProperties.isIgnoreTables()) ? Arrays.asList(appProperties.getTableName().split(","))
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
			rs.close();
			sourceConn.close();
		}
		if (!tableNames.isEmpty()) {
			for (String tableName : tableNames) {
				StringBuilder info = new StringBuilder();
				appProperties.setTableName(tableName);
				runId= validate(appProperties,  tableName, columnList);
				//logger.info(info.toString());
			}
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
		RunDetails runDetails= RunDetails.builder().schemaName(inputRunDetails.getSourceSchemaName()).tableName(inputRunDetails.getTableName()).validationrequest(true).build();
		RecommendationResponse recommendationResponse = recommendationService.getRecommendationResponseV2(runDetails);
		RunInfo runInfo=buildRunInfo(recommendationResponse,rowCount);
		return runInfo;
	}

	public LastRunDetails getLastRunDetails(ValidationRequest inputRunDetails) throws Exception {
		ArrayList<RunInfo> list= new ArrayList<RunInfo>();
		LastRunDetails lastRunDetails=new LastRunDetails();
		RunDetails runDetail= RunDetails.builder().schemaName(inputRunDetails.getSourceSchemaName()).tableName(inputRunDetails.getTableName()).validationrequest(true).build();
		List<RunDetails> rundetails=getLastRunResults(runDetail);
		HashMap<String,Long> tableList=new HashMap<String,Long>();
		for(RunDetails runId:rundetails ) {
			Long rowCount= 0L;
			runId.setValidationrequest(true);
			RecommendationResponse recommendationResponse = recommendationService.getRecommendationResponseV2(runId);
			inputRunDetails.setTableName(runId.getTableName());
			if(tableList.get(runId.getTableName())!=null){
				rowCount=tableList.get(runId.getTableName());
			}else
			 rowCount= getTableCount(inputRunDetails);
			RunInfo runInfo = buildRunInfo(recommendationResponse, rowCount);
			runInfo.setTable(runId.getTableName());
			runInfo.setLastRunDate(runId.getExecutionDate());
			tableList.put(runId.getTableName(),rowCount);
			list.add(runInfo);
		}
		lastRunDetails.setRuns(list);
		lastRunDetails.setSchemaName(runDetail.getSchemaName());
		return lastRunDetails;

	}

	private List<RunDetails> getLastRunResults(RunDetails runDetail) throws SQLException {
		List<RunDetails> outputRunDetailsList = new ArrayList<>();
		String query = "select * FROM public.run_details order by execution_date desc limit 10";
		Connection dbConn =null;
		try { dbConn =dataSource.getDBConnection();
			PreparedStatement pst = dbConn.prepareStatement(query);
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
				outputRunDetailsList.add(runDetails);
			}

		} catch (SQLException ex) {
			logger.error("Exception while fetching table details");
			logger.error(ex.getMessage());
		}finally {
			if(dbConn!=null)
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
}

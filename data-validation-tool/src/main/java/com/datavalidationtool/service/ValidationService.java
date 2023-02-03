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


import com.datavalidationtool.ds.DataSource;
import com.datavalidationtool.model.DatabaseInfo;
import com.datavalidationtool.model.request.ValidationRequest;
import com.datavalidationtool.util.JdbcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.datavalidationtool.model.DatabaseInfo.dbType;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.Date;
import java.util.*;
@Service
public class ValidationService {
	public Logger logger = LoggerFactory.getLogger("CompareService");
	/**
	 * 
	 * @param validationRequest
	 */
	public void validateData(ValidationRequest validationRequest) {
	  	try {
			if(!DataSource.getInstance().isPoolInitialized())
	  		  setConnections(validationRequest);
  			if(DataSource.getInstance().isPoolInitialized()) { 
	  			long rowNo = 1;
  				Date date = new Date();
  				//DateUtil dateUtil = new DateUtil();
				if (validationRequest.getTableName() != null && !validationRequest.getTableName().isEmpty()
						&& !validationRequest.isIgnoreTables()) {
  					String[] tableNameParts = validationRequest.getTableName().split(",");
  					for (String tableName : tableNameParts) {
						validate(validationRequest,tableName, null);
  					}
		  			
  				} else {
  					
  					String[] schemaParts = validationRequest.getSourceSchemaName().split(",");
  					for (String schemaName : schemaParts) {
						String[] schemas = schemaName.split(":");
						if(schemas.length>0)
							validationRequest.setSourceSchemaName(schemas[0]);
						if(schemas.length>1)
							validationRequest.setTargetSchemaName(schemas[1].toLowerCase());
						compareSchema(validationRequest, /*getSourceConn(), getTargetConn(),*/  null);
					}
  				}
			} else {
  				logger.info("Either Source or Target DB connection is not established."); 
  			}
	  	} catch (Exception ex) {
	  		logger.error(ex.getMessage(), ex);
	  	} finally {
	  		JdbcUtil jdbcUtil = new JdbcUtil();
		}
	}


	
	/**
	 * 
	 * @param appProperties
	 * @throws Exception
	 */
	private void setConnections(ValidationRequest appProperties) throws Exception {
		
	  	Connection sourceConn = null;
	  	Connection targetConn = null;
		DatabaseInfo targetDb = new DatabaseInfo(appProperties.getTargetHost(), appProperties.getTargetPort(),
					appProperties.getTargetDBName(), null, appProperties.getTargetUserName(),
					appProperties.getTargetUserPassword(), false, dbType.POSTGRESQL,
					true, null,  null);
			targetDb.setConnectionPoolMinSize(appProperties.getConnectionPoolMinSize());
			targetDb.setConnectionPoolMaxSize(appProperties.getConnectionPoolMaxSize());
            //POOL INITIALIEZED
            DataSource.getInstance().initializePool(null, targetDb);
	  		logger.info("Successfully initialized the pool");
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
	private void validate(ValidationRequest appProperties,
			 String tableName, List<String> columnList) throws Exception {

		long start = System.currentTimeMillis();
		StringBuilder info = new StringBuilder();
        long usedMemory = 0;
		try {
			checkIfTableExistsInPg(appProperties.getTargetSchemaName().toLowerCase(), tableName.toLowerCase(), "POSTGRESQL"/*, targetConn*/);
            long rowCount=0;
			info.append("Schema: ");
			info.append(appProperties.getTargetSchemaName());
			info.append(" , Table: ");
			info.append(tableName);
			int i;
			info = new StringBuilder();
		   info.append("\n###############################################################\n");
			logger.info(info.toString());
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
				String dbFunction = "{ call fn_post_mig_data_validation_dvt2(?,?,?,?,?,?,?,?) }";
				CallableStatement cst = null ;
				try {
					cst = con.prepareCall(dbFunction);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				{
					cst.setString(1, appProperties.getSourceSchemaName());
					cst.setString(2, appProperties.getTargetSchemaName());
					cst.setString(3, appProperties.getTableName());
					cst.setString(4, appProperties.getTableName());
					//cst.setString(5, appProperties.getColumns());
					//cst.setString(6, appProperties.getFilter());
					//cst.setString(7, appProperties.getFilter());
					cst.setString(5, "");
					cst.setString(6, "");
					cst.setString(7, "");
					cst.setBoolean(8, appProperties.isCheckAdditionalRows());
					//cst.registerOutParameter(1, Types.VARCHAR);
				}
				rs= cst.executeQuery();
				logger.error("DB", rs.getRow());
				//String result=rs.getString(1);
				//logger.info("Result.." +result);
				//logger.debug(query);
			} catch (SQLException ex) {

				ex.printStackTrace();
				logger.error("DB", ex);

			} finally {
				JdbcUtil jdbcUtil = new JdbcUtil();
				JdbcUtil.closeResultSet(rs);
				JdbcUtil.closeStatement(stmt);
				//JdbcUtil.closeConnection(con);
			}
		} catch (SQLException ex) {

			ex.printStackTrace();
			logger.error("db", ex);

		}


		long end = System.currentTimeMillis();
		long timeTaken = end - start;
		info = new StringBuilder();
		info.append("\n----------------------------------------------------\n");
		info.append("Finished writing comparison results for ");
		info.append(appProperties.getSourceSchemaName());
		info.append(".");
		info.append(tableName);
		logger.info(info.toString());

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
	 * @param appProperties
	 * @param columnList
	 * @return
	 */
	public void compareSchema(ValidationRequest appProperties,
			List<String> columnList) throws Exception {
		List<String> tableNames = new ArrayList<String>();
		ResultSet rs = null;
		Connection sourceConn=null;
		try {
			sourceConn =DataSource.getInstance().getTargetDBConnection();
			List<String> ignoreTables = (appProperties.getTableName() != null && !appProperties.getTableName().isEmpty()
					&& appProperties.isIgnoreTables()) ? Arrays.asList(appProperties.getTableName().split(","))
					: new ArrayList<String>();
			String[] types = {"FOREIGN"};
			rs = sourceConn.getMetaData().getTables(appProperties.getTargetDBName().toLowerCase(), appProperties.getSourceSchemaName().toLowerCase(), null, null);
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
		if (!tableNames.isEmpty()) {
			for (String tableName : tableNames) {
				StringBuilder info = new StringBuilder();
				validate(appProperties,  tableName, columnList);
				logger.info(info.toString());
			}
		}
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

}

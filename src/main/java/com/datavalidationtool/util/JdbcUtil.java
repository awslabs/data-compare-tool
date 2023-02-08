package com.datavalidationtool.util;

import com.datavalidationtool.ds.DataSource;
import com.datavalidationtool.model.DatabaseInfo;
import com.datavalidationtool.model.request.ValidationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.Properties;

@Component
public class JdbcUtil {

	public static Logger logger = LoggerFactory.getLogger("JdbcUtil");
	
	/**
	 * 
	 * @param dbType
	 * @return
	 */
	public static String getDriverClass(String dbType) {
		
		if("ORACLE".equals(dbType)) {
			
			return "oracle.jdbc.driver.OracleDriver";
			
		} else if("SQLSERVER".equals(dbType)) {
			
			return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
			
		} else if("POSTGRESQL".equals(dbType)) {
			
			return "org.postgresql.Driver";
		}
		
		return "";
	}
	
	/**
	 * 
	 * @param connection
	 */
	public static void closeConnection(Connection connection) {
		
		if (connection != null) {
			
			try {
				
				connection.close();
				
			} catch (SQLException e) {
				
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	/**
	 * 
	 * @param statement
	 */
	public static void closeStatement(Statement statement) {
		if(statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	/**
	 * 
	 * @param resultSet
	 */
	public static void closeResultSet(ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	/**
	 *
	 * @param appProperties
	 * @throws Exception
	 */
	public static void setConnections(ValidationRequest appProperties) throws Exception {
		Connection sourceConn = null;
		Connection targetConn = null;
		DatabaseInfo targetDb = new DatabaseInfo(appProperties.getTargetHost(), appProperties.getTargetPort(),
				appProperties.getTargetDBName(), null, appProperties.getTargetUserName(),
				appProperties.getTargetUserPassword(), false, DatabaseInfo.dbType.POSTGRESQL,
				true, null,  null);
		targetDb.setConnectionPoolMinSize(appProperties.getConnectionPoolMinSize());
		targetDb.setConnectionPoolMaxSize(appProperties.getConnectionPoolMaxSize());
		//POOL INITIALIEZED
		DataSource.getInstance().initializePool(null, targetDb);
		logger.info("Successfully initialized the pool");
	}
	/**
	 *
	 * @param db
	 * @return
	 * @throws Exception
	 */
	public Connection getConnection(DatabaseInfo db) throws Exception {

		return getConnection(db.getURL(), db.getDriverClass(), db.getUserName(), db.getPassword(), db.getType().name(), db.isSslRequire(), db.getTrustStorePath(), db.getTrsutStorePassword());
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
	public Connection getConnection(String url, String driverClass, String user, String password, String dbType, boolean isSslRequire, String trustStorePath, String trsutStorePassword) throws Exception {

		try {
			Class.forName(driverClass);
		} catch (ClassNotFoundException ex) {
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

}
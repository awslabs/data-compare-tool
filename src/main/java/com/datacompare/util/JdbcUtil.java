package com.datacompare.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcUtil {

	public static Logger logger = LoggerFactory.getLogger("JdbcUtil");
	
	/**
	 * 
	 * @param dbType
	 * @return
	 */
	public String getDriverClass(String dbType) {
		
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
	public void closeConnection(Connection connection) {
		
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
	public void closeStatement(Statement statement) {
		
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
	public void closeResultSet(ResultSet resultSet) {
		
		if (resultSet != null) {
			
			try {
				
				resultSet.close();
				
			} catch (SQLException e) {
				
				logger.error(e.getMessage(), e);
			}
		}
	}
}
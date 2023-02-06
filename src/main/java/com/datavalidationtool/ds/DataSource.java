package com.datavalidationtool.ds;


import com.datavalidationtool.model.DatabaseInfo;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DataSource {

	public Logger logger = LoggerFactory.getLogger("DataSource");

	private HikariDataSource sourceDS;
	private HikariDataSource targetDS;

	private static DataSource instance = new DataSource();
	private boolean isPoolInitialized = false;

	public boolean isPoolInitialized() {
		return isPoolInitialized;
	}

	private DataSource() {
	}

	public static DataSource getInstance() {
		return instance;
	}

	public Connection getSourceDBConnection() throws SQLException {
		logger.info("Source DB Pool Size===" + sourceDS.getMaximumPoolSize());
		logger.info("Target DB Pool Size===" + targetDS.getMaximumPoolSize());
		return sourceDS.getConnection();
	}

	public Connection getTargetDBConnection() throws SQLException {
		return targetDS.getConnection();
	}

	public synchronized void initializePool(DatabaseInfo srcDatabasebInfo, DatabaseInfo targetDatabasebInfo) {
		if (!isPoolInitialized) {
			if(srcDatabasebInfo!=null) {
				sourceDS = new HikariDataSource();
				sourceDS.setJdbcUrl(srcDatabasebInfo.getURL());
				sourceDS.setDriverClassName(srcDatabasebInfo.getDriverClass());
				sourceDS.setUsername(srcDatabasebInfo.getUserName());
				sourceDS.setPassword(srcDatabasebInfo.getPassword());
				sourceDS.setConnectionTestQuery("select 1 from dual");

				logger.info("sourceDS setMinimumIdle ====" + srcDatabasebInfo.getConnectionPoolMinSize());
				logger.info("sourceDS setMaximumPoolSize ====" + srcDatabasebInfo.getConnectionPoolMaxSize());

				if (srcDatabasebInfo.getConnectionPoolMinSize() != 0) {
					sourceDS.setMinimumIdle(srcDatabasebInfo.getConnectionPoolMinSize());

				}
				if (srcDatabasebInfo.getConnectionPoolMaxSize() != 0) {
					sourceDS.setMaximumPoolSize(srcDatabasebInfo.getConnectionPoolMaxSize());
				}


				logger.info("Sucessfully created Source DB Connection Pool...");
				logger.info("Source DB Pool Min Size=" + sourceDS.getMinimumIdle() + ", MaxSize=" + sourceDS.getMaximumPoolSize());
			}
			targetDS = new HikariDataSource();
			targetDS.setJdbcUrl(targetDatabasebInfo.getURL());
			targetDS.setDriverClassName(targetDatabasebInfo.getDriverClass());
			targetDS.setUsername(targetDatabasebInfo.getUserName());
			targetDS.setPassword(targetDatabasebInfo.getPassword());
			targetDS.setConnectionTestQuery("select 1");
			
			logger.info("targetDS setMinimumIdle ===="+targetDatabasebInfo.getConnectionPoolMinSize());
			logger.info("targetDS setMaximumPoolSize ===="+targetDatabasebInfo.getConnectionPoolMaxSize());
			
			if(targetDatabasebInfo.getConnectionPoolMinSize()!=0) {
				targetDS.setMinimumIdle(targetDatabasebInfo.getConnectionPoolMinSize());
			}
			if(targetDatabasebInfo.getConnectionPoolMaxSize()!=0) {
				targetDS.setMaximumPoolSize(targetDatabasebInfo.getConnectionPoolMaxSize());
			}
			logger.info("Target DB Pool Min Size="+targetDS.getMinimumIdle()+", MaxSize="+targetDS.getMaximumPoolSize());

			this.isPoolInitialized = true;
			logger.info("Sucessfully created Target DB Connection Pool... ");
		} else {
			throw new RuntimeException("Pool Already Initialized.");
		}
	}

	public synchronized void initializePool(String sourceJdbcUrl, String sourceDriverClassName, String sourceUserName,
			String souurceUserPwd, String targetJdbcUrl, String targetDriverClassName, String targetUserName,
			String targetUserPwd, int connectionPoolMinSize, int connectionPoolMaxSize) {
		if (!isPoolInitialized) {
			sourceDS = new HikariDataSource();
			sourceDS.setJdbcUrl(sourceJdbcUrl);
			sourceDS.setDriverClassName(sourceDriverClassName);
			sourceDS.setUsername(sourceUserName);
			sourceDS.setPassword(souurceUserPwd);
			if(connectionPoolMinSize!=0) {
				sourceDS.setMinimumIdle(connectionPoolMinSize);
			}
			if(connectionPoolMaxSize!=0) {
				sourceDS.setMaximumPoolSize(connectionPoolMinSize);
			}
			
			logger.info("Sucessfully created Source DB Connection Pool...");
			
			targetDS = new HikariDataSource();
			targetDS.setJdbcUrl(targetJdbcUrl);
			targetDS.setDriverClassName(targetDriverClassName);
			targetDS.setUsername(targetUserName);
			targetDS.setPassword(targetUserPwd);
			if(connectionPoolMinSize!=0) {
				targetDS.setMinimumIdle(connectionPoolMinSize);
			}
			if(connectionPoolMaxSize!=0) {
				targetDS.setMaximumPoolSize(connectionPoolMinSize);
			}
			
			this.isPoolInitialized = true;
			logger.info("Sucessfully created Target DB Connection Pool... ");
		} else {
			throw new RuntimeException("Pool Already Initialized.");
		}

	}

}

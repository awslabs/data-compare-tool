
/**
 * Model class for database information.
 *
 *
 * @author      Harnath Valeti
 * @author      Madhu Athinarapu
 * @version     1.0
 * @since       1.0
 */

package com.datavalidationtool.model;

public class DatabaseInfo {

	public static final String ORACLE_JDBC_DRIVER_ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
	public static final String PORTSTRING = ")(PORT=";
	public static final String JDBC_ORACLE_THIN_DESCRIPTION_ADDRESS_PROTOCOL_TCPS_HOST = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcps)(HOST=";
	public static final String CONNECT_DATA_SERVICE_NAME = "))(CONNECT_DATA=(SERVICE_NAME=";
	public static final String JDBC_ORACLE_THIN = "jdbc:oracle:thin:@";

	public enum dbType {
		POSTGRESQL, POSTGRESQL_SSL, ORACLE, SQLSERVER, ORACLE_SID, ORACLE_SERVICE
	}
	
	private final String	hostName;
	private final int		port;
	private final String	database;
	private final String 	service;
	private final String	userName;
	private final String	password;
	private final dbType	type;
	private final boolean   destination;
	private final boolean   sslRequire;
	private final String	trustStorePath;
	private final String	trsutStorePassword;
    
	private int connectionPoolMinSize;
	private int connectionPoolMaxSize;
	
	/**
	 * 
	 * @param hostName
	 * @param port
	 * @param database
	 * @param service
	 * @param userName
	 * @param password
	 * @param sslRequire
	 * @param type
	 * @param destination
	 */
	public DatabaseInfo(String hostName, int port, String database, String service, String userName, String password, boolean sslRequire, dbType type, boolean destination,String trustStorePath, String trsutStorePassword) {
		
		this.hostName = hostName;
		this.port = port;
		this.database = database;
		this.service = service;
		this.userName = userName;
		this.password = password;
		this.sslRequire = sslRequire;
		this.trustStorePath=trustStorePath;
		this.trsutStorePassword=trsutStorePassword;
		
		if(type.name().equals("ORACLE") && this.service != null && ("Service".equals(this.service) || "SID".equals(this.service))) {
			
			this.type = dbType.valueOf(dbType.ORACLE.name() + "_" + this.service);
			
		} else if(type.name().equals("POSTGRESQL") && this.sslRequire) {
			
			this.type = dbType.valueOf(dbType.POSTGRESQL.name() + "_SSL"); 
			
		} else {
			
			this.type = type;
		}
		
		this.destination = destination;
	}

	/**
	 * 
	 * @return
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * 
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * 
	 * @return
	 */
	public dbType getType() {
		return type;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isDestination() {
		return destination;
	}

	public boolean isSslRequire() {
		return sslRequire;
	}


	public String getTrustStorePath() {
		return trustStorePath;
	}

	public String getTrsutStorePassword() {
		return trsutStorePassword;
	}
	
	
	public int getConnectionPoolMinSize() {
		return connectionPoolMinSize;
	}

	public void setConnectionPoolMinSize(int connectionPoolMinSize) {
		this.connectionPoolMinSize = connectionPoolMinSize;
	}

	public int getConnectionPoolMaxSize() {
		return connectionPoolMaxSize;
	}

	public void setConnectionPoolMaxSize(int connectionPoolMaxSize) {
		this.connectionPoolMaxSize = connectionPoolMaxSize;
	}

	/**
	 * 
	 * @return
	 */
	public String getDriverClass() {
		
		switch (type) {
		
			case POSTGRESQL:
				return "org.postgresql.Driver";
			case POSTGRESQL_SSL:
				return "org.postgresql.Driver";
			case ORACLE:
				return ORACLE_JDBC_DRIVER_ORACLE_DRIVER;
			case ORACLE_SID:
				return ORACLE_JDBC_DRIVER_ORACLE_DRIVER;
			case ORACLE_SERVICE:
				return ORACLE_JDBC_DRIVER_ORACLE_DRIVER;
			case SQLSERVER:
				return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		}
		
		return "";
	}
	
	/**
	 * 
	 * @return
	 *
	 */
	public String getURL() {
		
		String url = "";
		
		switch (type) {

			case POSTGRESQL:
				url = "jdbc:postgresql://" + hostName + ":" + port + "/" + database + "?tcpKeepAlive=true";
				break;
			case POSTGRESQL_SSL:
				url = "jdbc:postgresql://" + hostName + ":" + port + "/" + database + "?sslmode=require&tcpKeepAlive=true";
				break;
			case ORACLE:
				if (sslRequire)
					url = JDBC_ORACLE_THIN_DESCRIPTION_ADDRESS_PROTOCOL_TCPS_HOST + hostName + PORTSTRING + port + CONNECT_DATA_SERVICE_NAME + database + ")))";
				else
					url = JDBC_ORACLE_THIN + hostName + ":" + port + ":" + database;
				break;
			case ORACLE_SID:
				if (sslRequire)
					url = JDBC_ORACLE_THIN_DESCRIPTION_ADDRESS_PROTOCOL_TCPS_HOST + hostName + PORTSTRING + port + CONNECT_DATA_SERVICE_NAME + database + ")))";
				else
					url = JDBC_ORACLE_THIN + hostName + ":" + port + ":" + database;
				break;
			case ORACLE_SERVICE:
				if (sslRequire)
					url = JDBC_ORACLE_THIN_DESCRIPTION_ADDRESS_PROTOCOL_TCPS_HOST + hostName + PORTSTRING + port + CONNECT_DATA_SERVICE_NAME + database + ")))";
				else
					url = JDBC_ORACLE_THIN + hostName + ":" + port + "/" + database;
				break;
			case SQLSERVER:
				url = "jdbc:sqlserver://" + hostName + ":" + port + ";databaseName=" + database;
				break;
		}
		
		return url;
	}
}
package com.datacompare.util;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

public class PostgreSQLJDBCExample {
	

	public static void main(String[] args) {
		
		new PostgreSQLJDBCExample().connectionTest();
	}

	public void connectionTest() {

		System.out.println("-------- PostgreSQL JDBC Connection Testing ------");

		try {

			Class.forName("org.postgresql.Driver");

		} catch (ClassNotFoundException e) {

			System.out.println("Where is your PosgtreSQL JDBC Driver?");
			e.printStackTrace();
			return;
		}

		System.out.println("PosgtreSQL JDBC Driver Registered!");
 
		Connection connection = null;

		try {

			Properties prop = new Properties();

			//prop.setProperty("javax.net.ssl.trustStore","D:\\rds-combined-ca-bundle.pem");
			prop.setProperty("user","munnavi");
			prop.setProperty("password","Fp5MYUPkbvcF");	
			//-Djavax.net.debug=SSL
			//?useSSL=true&requireSSL=true&sslmode=verify-full&ssl=true
			String url = "jdbc:postgresql://vzw-euiv-myinfo-rdsdev-2018-10-09-auroradbcluster-12347mevdqnf2.cluster-ccpoyzezuqrd.us-east-1.rds.amazonaws.com:5432/OPS?sslmode=require&tcpKeepAlive=true";
			connection = DriverManager.getConnection(url,prop);
			System.out.println("\n"+url);
			ResultSet rs = connection.getMetaData().getColumns(null, "dbtoolsecurity_config", "tblApprovalChain", null);
			rs.setFetchSize(1000);

			while (rs.next()) {
				
				String columnType = rs.getString("TYPE_NAME");

				String columnName = rs.getString("COLUMN_NAME");
				
				String columnSize = rs.getString("COLUMN_SIZE");
				
				boolean isNullable = rs.getString("IS_NULLABLE").toUpperCase().equals("YES");

				String decimalDigits = rs.getString("DECIMAL_DIGITS");
				
				System.out.println("columnType: " + columnType);
				System.out.println("columnName: " + columnName);
				System.out.println("columnSize: " + columnSize);
				System.out.println("isNullable: " + isNullable);
				System.out.println("decimalDigits: " + decimalDigits); 
			}
		} catch (SQLException e) {

			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}

		if (connection != null) {
			System.out.println("You made it, take control your database now!");
//		} else {
//			System.out.println("Failed to make connection!");
		}
	}
}
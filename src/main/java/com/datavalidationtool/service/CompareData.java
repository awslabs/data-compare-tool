package com.datavalidationtool.service;

import com.datavalidationtool.dao.DataSource;
import com.datavalidationtool.model.RunDetails;
import com.datavalidationtool.model.request.ValidationRequest;
import org.apache.poi.ss.usermodel.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

public class CompareData implements Runnable {


    public DataSource dataSource;
    public Logger logger = LoggerFactory.getLogger("CompareService");
    public ValidationRequest getValidationRequest() {
        return this.validationRequest;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String tableName;
    public void setValidationRequest(ValidationRequest validationRequest) {
        this.validationRequest = validationRequest;
    }
    public ValidationRequest validationRequest;

    public String getRunId() {
        return this.runId;
    }
    public void setRunId(String runId) {
        this.runId = runId;
    }
    private String runId;

    public String getChunkFilter() {
        return chunkFilter;
    }

    public void setChunkFilter(String chunkFilter) {
        this.chunkFilter = chunkFilter;
    }

    private String chunkFilter;

    public CompareData(ValidationRequest validationRequest, DataSource dataSource,String tableName,String chunkFilter) {
        this.validationRequest = validationRequest;
        this.runId=new String();
        this.dataSource=dataSource;
        this.tableName=tableName;
        this.chunkFilter=chunkFilter;
    }
    @Override
    public void run() {
        Thread.currentThread().setName("CompareData for Table"+tableName);
        this.validationRequest.setTableName(tableName);
        String filter=getChunkFilter();
        StringBuilder info =new StringBuilder();
        logger.info("Filter...."+validationRequest.getDataFilters());;
        long start = System.currentTimeMillis();
            setRunId(runId);
            Statement stmt = null;
            ResultSet rs = null;
            Connection con=null;

        try {
                con =  dataSource.getDBConnection() ;
               // System.out.println("con metadata"+ con.getMetaData());
                stmt = con.createStatement();
                long keySize = 0;
                long valSize = 0;
                String dbFunction = "{ call fn_post_mig_data_validation_dvt2_include_exclude(?,?,?,?,?,?,?,?,?,?) }";
                CallableStatement cst = null ;
                try {
                    cst = con.prepareCall(dbFunction);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                cst.setString(1, validationRequest.getSourceSchemaName());
                cst.setString(2, validationRequest.getTargetSchemaName());
                cst.setString(3, tableName);
                cst.setString(4, tableName);
                cst.setString(5, validationRequest.getUniqueCols()!=null?validationRequest.getUniqueCols():"");
                cst.setString(6, filter!=null?filter:"");
                cst.setString(7, validationRequest.getColumns()!=null?validationRequest.getColumns():"");
                cst.setBoolean(8, validationRequest.isIgnoreColumns());
                cst.setBoolean(9, validationRequest.isCheckAdditionalRows());
                cst.setString(10, validationRequest.getRunId());
            logger.info(" Filter...in thread."+filter);
                rs= cst.executeQuery();
                while(rs.next()) {
                    String result = rs.getString(1);
                    //logger.info("Table "+validationRequest.getTableName(), result);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                logger.error("DB", ex);

            } finally {
                if(rs!=null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if(con!=null) {
                    try {
                        con.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        long end = System.currentTimeMillis();
        long timeTaken = end - start;
        long diffInSeconds = (end - start) / 1000;
        info.append("Schema: ");
        info.append(validationRequest.getTargetSchemaName());
        info.append(" , Table: ");
        info.append(tableName);
        info.append(" , Total time taken: ");
        info.append(diffInSeconds);
        logger.info(info.toString());
        int i;
        info = new StringBuilder();
        info.append("\n###############################################################\n");
        logger.info(info.toString());
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
            logger.error("Exception while fetching table details in getCurrentTableRunInfo");
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
        String query = "insert into public.run_details(source_host_name,target_host_name,database_name,schema_name,table_name,schema_run,table_run,run_id,execution_date) values(?,?,?,?,?,?,?,?,?)";
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
            int count= pst.executeUpdate();
            logger.info("added run details for table",runDetails.getTableName());
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
}

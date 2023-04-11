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

    public CompareData(ValidationRequest validationRequest, DataSource dataSource,String tableName) {
        this.validationRequest = validationRequest;
        this.runId=new String();
        this.dataSource=dataSource;
        this.tableName=tableName;
    }
    @Override
    public void run() {
        Thread.currentThread().setName("CompareData for Table"+tableName);
        long start = System.currentTimeMillis();
        Map<String, String> tempSource = new HashMap<String, String>();
        Map<String, String> tempTarget = new HashMap<String, String>();
        List<String> tempSourceFailTuple = new ArrayList<String>();
        List<String> tempTargetFailTuple = new ArrayList<String>();
        long end = System.currentTimeMillis();
        long timeTaken = end - start;
        long diffInSeconds = (end - start) / 1000;
        try {
            validationRequest.setTableName(tableName);
           String runId= validate(validationRequest,tableName);
            try {
                Thread.sleep(2000); //wait 2 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
           System.out.println("runId...."+runId);
            System.out.println("table name ..."+tableName);
            setRunId(runId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder info = new StringBuilder();
        info.append("\n----------------------------------------------------------"+diffInSeconds);
    }

    private String validate(ValidationRequest validationRequest,String tableName) throws Exception {
        long start = System.currentTimeMillis();
        StringBuilder info = new StringBuilder();
        String runId="";
        long usedMemory = 0;
        try {
            //checkIfTableExistsInPg(appProperties.getTargetSchemaName().toLowerCase(), tableName.toLowerCase(), "POSTGRESQL"/*, targetConn*/);
            long rowCount=0;
            info.append("Schema: ");
            info.append(validationRequest.getTargetSchemaName());
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
                String dbFunction = "{ call fn_post_mig_data_validation_dvt2_include_exclude(?,?,?,?,?,?,?,?,?) }";
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
                cst.setString(6, validationRequest.getDataFilters()!=null?validationRequest.getDataFilters():"");
                cst.setString(7, validationRequest.getColumns()!=null?validationRequest.getColumns():"");
                cst.setBoolean(8, validationRequest.isIgnoreColumns());
                cst.setBoolean(9, validationRequest.isCheckAdditionalRows());
                rs= cst.executeQuery();
                while(rs.next()) {
                    String result = rs.getString(1);
                    logger.info("Table "+validationRequest.getTableName(), result);
                    if(result.contains("Validation complete for Run Id")) {
                        runId = result.substring(31, 63);
                        logger.info("Run Id " ,runId);
                    }
                }
                if(!runId.isBlank()) {
                    if(validationRequest.getTableName().equals(""))
                        validationRequest.setTableName(tableName);
                    RunDetails runDetails = getCurrentTableRunInfo(validationRequest);
                    runDetails.setSchemaRun(validationRequest.getSchemaRunNumber());
                    runDetails.setRunId(runId);
                    runDetails.setSourceHostName(validationRequest.getTargetHost());
                    runDetails.setTargetHostName(validationRequest.getTargetHost());
                    runDetails.setDatabaseName(validationRequest.getTargetDBName());
                    runDetails.setSchemaName(validationRequest.getSourceSchemaName());
                    runDetails.setTableName(tableName);
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    runDetails.setExecutionTime(timestamp);
                    addRunDetailsForSelection(runDetails);
                }
            } catch (SQLException ex) {

                ex.printStackTrace();
                logger.error("DB", ex);

            } finally {
                if(rs!=null)
                    rs.close();
                if(con!=null)
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
        info.append(validationRequest.getSourceSchemaName());
        info.append(".");
        info.append(tableName);
        logger.info(info.toString());
        return runId;
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

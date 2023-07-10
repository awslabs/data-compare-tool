/**
 * Implementation class for recommendation APIs.
 *
 * @author Rajeshkumar Kagathara
 * @version 1.0
 */

package com.datavalidationtool.service;

import com.datavalidationtool.dao.DataSource;
import com.datavalidationtool.model.DatabaseInfo;
import com.datavalidationtool.model.ExcelDataRequest;
import com.datavalidationtool.model.RunDetails;
import com.datavalidationtool.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    public static final String WHERE = " WHERE ";
    public static final String SOURCE_HOST_NAME = "source_host_name='";
    public static final String AND = " and ";
    public static final String SOURCE_HOST_NAME1 = "source_host_name";
    public static final String TARGET_HOST_NAME = "target_host_name";
    public static final String DATABASE_NAME = "database_name";
    public static final String SCHEMA_NAME = "schema_name";
    public static final String TABLE_NAME = "table_name";
    public static final String SCHEMA_RUN = "schema_run";
    public static final String TABLE_RUN = "table_run";
    public static final String RUN_ID = "run_id";
    public static final String EXECUTION_DATE = "execution_date";
    public static final String RUN_ID1 = "run_id='";
    public static final String VAL_TYPE = "val_type";
    public static final String EXCEPTION_STATUS = "exception_status";
    public static final String MISSING = "Missing";
    public static final String MISMATCH_SRC = "Mismatch_src";
    public static final String MISMATCH_TRG = "Mismatch_trg";
    @Autowired
    public DataSource dataSource;
    public Logger logger = LoggerFactory.getLogger("RecommendationServiceImpl");
    public String recommendationApiTest() {
        logger.info("Recommendation Api Test Successful");
        return "Recommendation Api Test Successful";
    }


    public List<String> getDbSchemaDetails(DatabaseInfo databaseInfo)  {
        List<String> schemaList = new ArrayList<>();
        String query = "SELECT schema_name FROM information_schema.schemata where schema_owner ='postgres'";
        Connection dbConn =null;
        PreparedStatement pst=null;
        try { dbConn = dataSource.getDBConnection();
             pst = dbConn.prepareStatement(query);
             ResultSet rs = pst.executeQuery();
            while (rs.next()){
                schemaList.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            logger.error("SQL Exception while fetching schema details");
            logger.error(ex.getMessage());
        } catch (Exception e) {
            logger.error("Generic Exception while fetching schema details");
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
        finally {
            try {
                 if(pst!=null)
                     pst.close();
                if(dbConn!=null)
                    dbConn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return schemaList;

    }

    public List<String> getSchemaTableDetails(DatabaseInfo databaseInfo, String schemaName) {

        List<String> tableList = new ArrayList<>();
        String preparedQuery = "SELECT table_name FROM information_schema.tables WHERE table_schema=?";

        try (Connection con = dataSource.getDBConnection();
             PreparedStatement pst = con.prepareStatement(preparedQuery)) {
            pst.setString(1, schemaName);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    tableList.add(rs.getString(1));
                }
            }

        } catch (SQLException ex) {
            logger.error("SQL Exception while fetching table details in getSchemaTableDetails");
            logger.error(ex.getMessage());
        } catch (Exception e) {
            logger.error("Generic Exception while fetching table details in getSchemaTableDetails");
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return tableList;
    }

    public List<RunDetails> getRunDetailsWithOptional(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo) throws Exception {

        List<RunDetails> outputRunDetailsList = new ArrayList<>();
        String query = null;
        String whereQueryCondition = "";
        String queryWithOptionalParam = "SELECT * FROM public.run_details";
        if (inputRunDetails_1.getSourceHostName() != null) {
            whereQueryCondition = whereQueryCondition + WHERE;
            whereQueryCondition = whereQueryCondition + SOURCE_HOST_NAME + inputRunDetails_1.getSourceHostName() + "' ";
        }
        if (inputRunDetails_1.getTargetHostName() != null) {
            whereQueryCondition = whereQueryCondition + AND + "target_host_name='" + inputRunDetails_1.getTargetHostName() + "' ";
        }
        if (inputRunDetails_1.getDatabaseName() != null) {
            whereQueryCondition = whereQueryCondition + AND + "database_name='" + inputRunDetails_1.getDatabaseName() + "' ";
        }

        if (inputRunDetails_1.getSchemaName() != null) {
            whereQueryCondition = whereQueryCondition + AND + "schema_name='" + inputRunDetails_1.getSchemaName() + "' ";
        }

        if (inputRunDetails_1.getTableName() != null) {
            whereQueryCondition = whereQueryCondition + AND + "table_name='" + inputRunDetails_1.getTableName() + "' ";
        }

        if (inputRunDetails_1.getSchemaRun() != 0) {
            whereQueryCondition = whereQueryCondition + AND + "schema_run='" + inputRunDetails_1.getSchemaRun() + "' ";
        }

        if (inputRunDetails_1.getTableRun() != 0) {
            whereQueryCondition = whereQueryCondition + AND + "table_run='" + inputRunDetails_1.getTableRun() + "' ";
        }

        query = queryWithOptionalParam + whereQueryCondition;

        try (Connection dbConn =dataSource.getDBConnection();
        PreparedStatement pst = dbConn.prepareStatement(query);){
             ResultSet rs = pst.executeQuery();

            while (rs.next()) {

                RunDetails runDetails = new RunDetails();
                runDetails.setSourceHostName(rs.getString(SOURCE_HOST_NAME1));
                runDetails.setTargetHostName(rs.getString(TARGET_HOST_NAME));
                runDetails.setDatabaseName(rs.getString(DATABASE_NAME));
                runDetails.setSchemaName(rs.getString(SCHEMA_NAME));
                runDetails.setTableName(rs.getString(TABLE_NAME));
                runDetails.setSchemaRun(rs.getInt(SCHEMA_RUN));
                runDetails.setTableRun(rs.getInt(TABLE_RUN));
                runDetails.setRunId(rs.getString(RUN_ID));
                runDetails.setExecutionDate(rs.getString(EXECUTION_DATE));

                outputRunDetailsList.add(runDetails);
            }

        } catch (SQLException ex) {
            logger.error("Exception while fetching table details in getRunDetailsWithOptional");
            logger.error(ex.getMessage());
        }
        return outputRunDetailsList;
    }

    public List<RunDetails> getHostRunDetails(String hostName, DatabaseInfo databaseInfo) throws Exception {

        List<RunDetails> outputRunDetailsList = new ArrayList<>();
        String query = "SELECT * FROM public.run_details WHERE "
                + SOURCE_HOST_NAME + hostName + "' ";

        try (Connection dbConn =dataSource.getDBConnection();
             PreparedStatement pst = dbConn.prepareStatement(query);){
             ResultSet rs = pst.executeQuery();

            while (rs.next()) {

                RunDetails runDetails = new RunDetails();
                runDetails.setSourceHostName(rs.getString(SOURCE_HOST_NAME1));
                runDetails.setTargetHostName(rs.getString(TARGET_HOST_NAME));
                runDetails.setDatabaseName(rs.getString(DATABASE_NAME));
                runDetails.setSchemaName(rs.getString(SCHEMA_NAME));
                runDetails.setTableName(rs.getString(TABLE_NAME));
                runDetails.setSchemaRun(rs.getInt(SCHEMA_RUN));
                runDetails.setTableRun(rs.getInt(TABLE_RUN));
                runDetails.setRunId(rs.getString(RUN_ID));
                runDetails.setExecutionDate(rs.getString(EXECUTION_DATE));

                outputRunDetailsList.add(runDetails);
            }

        } catch (SQLException ex) {
            logger.error("Exception while fetching table details in getHostRunDetails");
            logger.error(ex.getMessage());
        }
        return outputRunDetailsList;
    }

    @Override
    public List<RunDetails> getHostRunDetailsForSelection() throws Exception {

        List<RunDetails> outputRunDetailsList = new ArrayList<>();
        String query = "SELECT * FROM public.run_details";

        try ( Connection dbConn =dataSource.getDBConnection();
             PreparedStatement pst = dbConn.prepareStatement(query);){
             ResultSet rs = pst.executeQuery();

            while (rs.next()) {

                RunDetails runDetails = new RunDetails();
                runDetails.setSourceHostName(rs.getString(SOURCE_HOST_NAME1));
                runDetails.setTargetHostName(rs.getString(TARGET_HOST_NAME));
                runDetails.setDatabaseName(rs.getString(DATABASE_NAME));
                runDetails.setSchemaName(rs.getString(SCHEMA_NAME));
                runDetails.setTableName(rs.getString(TABLE_NAME));
                runDetails.setSchemaRun(rs.getInt(SCHEMA_RUN));
                runDetails.setTableRun(rs.getInt(TABLE_RUN));
                runDetails.setRunId(rs.getString(RUN_ID));
                runDetails.setExecutionDate(rs.getString(EXECUTION_DATE));
                outputRunDetailsList.add(runDetails);
            }

        } catch (SQLException ex) {
            logger.error("Exception while fetching table details in getHostRunDetailsForSelection");
            logger.error(ex.getMessage());
        }
        return outputRunDetailsList;
    }

    public RunDetails getCurrentRunInfo() throws Exception {
        RunDetails runDetails = new RunDetails();
        String query = "SELECT max(schema_run), max(table_run) FROM public.run_details";
        Connection dbConn =null;
        PreparedStatement pst =null;
        try { dbConn = dataSource.getDBConnection();
            pst=dbConn.prepareStatement(query);
             ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                runDetails.setSchemaRun(rs.getInt(1));
                runDetails.setTableRun(rs.getInt(2));
            }

        } catch (SQLException ex) {
            logger.error("Exception while fetching table details in getCurrentRunInfo");
            logger.error(ex.getMessage());
        }finally {
            if(pst!=null)
              pst.close();
            if(dbConn!=null)
                dbConn.close();
    }
        return runDetails;
    }

     public RunDetailsSelectionResponse getRunDetailsSelectionResponse(List<RunDetails> runDetails) {
        RunDetailsSelectionResponse runDetailsSelectionResponse = new RunDetailsSelectionResponse();
        List<HostDetails> hostDetailsList = new ArrayList<>();
        Set<String> uniqueSrcHostNm = runDetails.stream().map(run -> run.getSourceHostName()).collect(Collectors.toSet());
        for (String hostNm : uniqueSrcHostNm) {
            HostDetails hostDetails = new HostDetails();
            hostDetails.setHostName(hostNm);
            List<DatabaseDetails> databaseDetailsList = new ArrayList<>();
            Set<String> uniqueDbNm = runDetails.stream().filter(rd -> rd.getSourceHostName().equals(hostNm)).map(rd -> rd.getDatabaseName()).collect(Collectors.toSet());
            for (String dbNm : uniqueDbNm) {
                Set<String> uniqueSchemaNm = runDetails.stream().filter(rd -> rd.getSourceHostName().equals(hostNm) && rd.getDatabaseName().equals(dbNm)).map(rd -> rd.getSchemaName()).collect(Collectors.toSet());
                DatabaseDetails databaseDetails = new DatabaseDetails();
                databaseDetails.setDatabaseName(dbNm);
                List<SchemaDetails> schemaDetailsList = new ArrayList<>();
                for (String schemaNm : uniqueSchemaNm) {
                    Set<RunDetails> uniqueSchemaRunDetails = runDetails.stream().filter(rd -> rd.getSourceHostName().equals(hostNm) && rd.getDatabaseName().equals(dbNm) && rd.getSchemaName().equals(schemaNm)).collect(Collectors.toSet());
                    Set<RunWithDate> schemaRun = getRunWithDates(uniqueSchemaRunDetails);
                    SchemaDetails schemaDetails = new SchemaDetails();
                    schemaDetails.setSchemaName(schemaNm);
                    schemaDetails.setSchemaRun(schemaRun);
                    List<TableDetails> tableDetailsList = new ArrayList<>();
                    Set<String> uniqueTableNm = runDetails.stream().filter(rd -> rd.getSourceHostName().equals(hostNm) && rd.getDatabaseName().equals(dbNm) && rd.getSchemaName().equals(schemaNm)).map(rd -> rd.getTableName()).collect(Collectors.toSet());
                    for (String tableNm : uniqueTableNm) {
                        Set<RunDetails> uniqueTableRunDetails = runDetails.stream().filter(rd -> rd.getSourceHostName().equals(hostNm) && rd.getDatabaseName().equals(dbNm) && rd.getSchemaName().equals(schemaNm) && rd.getTableName().equals(tableNm)).collect(Collectors.toSet());
                        Set<RunWithDate> tableRun = getRunWithDates(uniqueTableRunDetails);
                        TableDetails tableDetails = new TableDetails();
                        tableDetails.setTableName(tableNm);
                        tableDetails.setTableRun(tableRun);
                        tableDetailsList.add(tableDetails);
                    }
                    schemaDetails.setTableList(tableDetailsList);
                    schemaDetailsList.add(schemaDetails);
                }
                databaseDetails.setSchemaList(schemaDetailsList);
                databaseDetailsList.add(databaseDetails);
            }
            hostDetails.setDatabaseList(databaseDetailsList);
            hostDetailsList.add(hostDetails);
        }
        runDetailsSelectionResponse.setHostDetailsList(hostDetailsList);
        return runDetailsSelectionResponse;
    }

    private static Set<RunWithDate> getRunWithDates(Set<RunDetails> uniqueSchemaRunDetails) {
        Set<RunWithDate> schemaRun = new TreeSet<>();
        for(RunDetails uTableRd: uniqueSchemaRunDetails) {
            RunWithDate runWithDate = new RunWithDate();
            Date executionDate = convertStrDateToDate(uTableRd.getExecutionDate());
            runWithDate.setRun(uTableRd.getTableRun());
            runWithDate.setExecutionDate(executionDate);
            schemaRun.add(runWithDate);
            runWithDate.setRunId(uTableRd.getRunId());
            runWithDate.setTableName(uTableRd.getTableName());
            runWithDate.setSchemaName(uTableRd.getSchemaName());
        }
        return schemaRun;
    }


    private static Date convertStrDateToDate(String strDate) {
        Date executionDate;
        try {
            final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
            final SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
            executionDate = sdf.parse(strDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return executionDate;
    }
    public List<Integer> getValIdFromValidationTable(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo, String ValidationTableName) throws Exception {

        List<Integer> valIdList = new ArrayList<>();
        String selectQuery = "SELECT val_id FROM " + inputRunDetails_1.getSchemaName() + "." + ValidationTableName + WHERE +
                RUN_ID1 + inputRunDetails_1.getRunId() + "' "
                + "ORDER BY val_id ASC ";

        //+"ORDER BY val_id ASC LIMIT 100";
        try (Connection dbConn =dataSource.getDBConnection();
             PreparedStatement pst = dbConn.prepareStatement(selectQuery);){
             ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                valIdList.add(rs.getInt("val_id"));
            }
        } catch (SQLException ex) {
            logger.error("Exception while fetching table details in getValIdFromValidationTable");
            logger.error(ex.getMessage());
        }
        return valIdList;
    }


    public List<Map<String, Object>> getEntireValidationTable(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo, String ValidationTableName) throws Exception {

        String selectQuery = "SELECT * FROM " + inputRunDetails_1.getSchemaName() + "." + ValidationTableName + WHERE +
                RUN_ID1 + inputRunDetails_1.getRunId() + "' "
         //       + "ORDER BY val_id ASC ";

        +"ORDER BY val_id ASC LIMIT 10";


        String tableJoinQuery = getTableJoinQueryForSourceTarget( inputRunDetails_1,  databaseInfo,  ValidationTableName);
        System.out.println(" tableJoinQuery ->"+tableJoinQuery );

        List<Map<String,Object>> rsKeyValMapList = new ArrayList<>();
        Connection dbConn =null;
        PreparedStatement pst=null;
        try { dbConn = dataSource.getDBConnection();
            //use below table join query to get source and target records.
             pst = dbConn.prepareStatement(tableJoinQuery);
             ResultSet rs = pst.executeQuery();
            if (rs != null) {
                // get the resultset metadata
                ResultSetMetaData rsmd = rs.getMetaData();
                while (rs.next()) {
                    Map<String,Object> rsKeyValMap = new HashMap<>();
                    for (int _iterator = 0; _iterator < rsmd.getColumnCount(); _iterator++) {
                        // get the SQL column name
                        String columnName = rsmd.getColumnName(_iterator + 1);
                        // get the value of the SQL column
                        Object columnValue = rs.getObject(_iterator + 1);
                        setrsKeyValue(rsKeyValMap, columnName, columnValue);
                    }
                    rsKeyValMapList.add(rsKeyValMap);
                }
            }
        } catch (SQLException ex) {
            logger.error("Exception while fetching table details in getEntireValidationTable");
            logger.error(ex.getMessage());
        }finally {
             if(pst!=null){
                 pst.close();
             }
            if(dbConn!=null){
                dbConn.close();
            }
        }
        return rsKeyValMapList;
    }

    public List<String> getAdminColumnNameOfValTable(){
        List<String> adminColumnNameOfValTable = new ArrayList<String>();
        adminColumnNameOfValTable.add("val_id");
        adminColumnNameOfValTable.add("val_ts");
        adminColumnNameOfValTable.add(RUN_ID);
        adminColumnNameOfValTable.add("val_log");
        adminColumnNameOfValTable.add(VAL_TYPE);
        adminColumnNameOfValTable.add("exception_rank");
        adminColumnNameOfValTable.add(EXCEPTION_STATUS);
        return adminColumnNameOfValTable;
    }

    public RecommendationResponse getRecommendationResponse(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo, String ValidationTableName) throws Exception {

        String tableJoinQuery = getTableJoinQueryForSourceTarget( inputRunDetails_1,  databaseInfo,  ValidationTableName);
        List<Map<String,Object>> rsKeyValMapList = new ArrayList<>();
        RecommendationResponse recommendationResponse = new RecommendationResponse();
        recommendationResponse.setTable(inputRunDetails_1.getSchemaName() + "." + ValidationTableName);
        recommendationResponse.setCurrentPage(1);
        recommendationResponse.setPageSize(5);
        recommendationResponse.setTotalRecords(20);
        String uniqueColumnFromValTable= getUniqueColumnFromValTable(inputRunDetails_1, databaseInfo, ValidationTableName);
        if(!uniqueColumnFromValTable.isEmpty()){
            recommendationResponse.setUniqueColumns(Arrays.asList(uniqueColumnFromValTable.split(",")));
        }
        else{
            recommendationResponse.setUniqueColumns(new ArrayList<>());
        }
        List<RecommendationRow> recommendationRowList = new ArrayList<>();

        Connection dbConn =null;
        try { dbConn =dataSource.getDBConnection();
             //PreparedStatement pst = dbConn.prepareStatement(selectQuery);){

             //use below table join query to get source and target records.
            PreparedStatement pst = dbConn.prepareStatement(tableJoinQuery);
            ResultSet rs = pst.executeQuery();
            if (rs != null) {
                // get the resultset metadata
                ResultSetMetaData rsmd = rs.getMetaData();
                while (rs.next()) {
                    Map<String,Object> rsKeyValMap = new HashMap<>();
                    List<RecommendationColumn> recommendationColumns = new ArrayList<>();
                    for (int _iterator = 0; _iterator < rsmd.getColumnCount(); _iterator++) {
                        // get the SQL column name
                        String columnName = rsmd.getColumnName(_iterator + 1);
                        // get the value of the SQL column
                        Object columnValue = rs.getObject(_iterator + 1);
                        setrsKeyValue(rsKeyValMap, columnName, columnValue);
                        List<String> AdminColumnNameOfValTable=getAdminColumnNameOfValTable();
                        if(!AdminColumnNameOfValTable.contains(columnName)){
                            if(rsKeyValMap.containsKey(columnName)){
                                recommendationColumns.add(new RecommendationColumn(columnName, rsKeyValMap.get(columnName),columnValue));
                            }
                        }
                    }
                    Object recommendationCode = null;
                    recommendationCode = setRecommendationCode(rsKeyValMap, recommendationCode);
                    recommendationRowList.add(new RecommendationRow(recommendationCode,recommendationColumns,rs.getInt(2),null));
                    rsKeyValMapList.add(rsKeyValMap);
                }
            }
        } catch (SQLException ex) {
            logger.error("Exception while fetching table details in getRecommendationResponse");
            logger.error(ex.getMessage());
        }finally {
            if(dbConn!=null){
                dbConn.close();
            }

    }
        recommendationResponse.setRows(recommendationRowList);
        return recommendationResponse;
    }

    private static Object setRecommendationCode(Map<String, Object> rsKeyValMap, Object recommendationCode) {
        if("Mismatch".equalsIgnoreCase(rsKeyValMap.get(VAL_TYPE).toString())){
            recommendationCode =2;
        }
        else if(MISSING.equalsIgnoreCase(rsKeyValMap.get(VAL_TYPE).toString())){
            recommendationCode =1;
        }
        else if("SrcMissing".equalsIgnoreCase(rsKeyValMap.get(VAL_TYPE).toString())){
            recommendationCode =3;
        }
        return recommendationCode;
    }

    private static void setrsKeyValue(Map<String, Object> rsKeyValMap, String columnName, Object columnValue) {
        if(rsKeyValMap.containsKey(columnName)){
            rsKeyValMap.put("target_"+ columnName, columnValue);
        }else{
            rsKeyValMap.put(columnName, columnValue);
        }
    }

    public String getTableJoinQueryForSourceTarget(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo, String ValidationTableName) throws Exception {

        String logColsList = getUniqueColumnFromValTable(inputRunDetails_1, databaseInfo, ValidationTableName);

        String tableJoinQuery = "";

        if (!logColsList.isEmpty()) {
            String tableJoinClause = " ON ";
            String selectColumn = "";
            String[] logColsListSplit = logColsList.split(",");
            for (int index = 0; index < logColsListSplit.length; index++) {
                tableJoinClause = tableJoinClause + " val." + logColsListSplit[index] + " = target." + logColsListSplit[index];


                selectColumn = selectColumn + " val." + logColsListSplit[index] + " as " + " val_" + logColsListSplit[index];
                selectColumn = selectColumn + " , ";
                selectColumn = selectColumn + " target." + logColsListSplit[index] + " as " + " target_" + logColsListSplit[index];

                if (index < logColsListSplit.length - 1) {
                    tableJoinClause = tableJoinClause + AND;

                    selectColumn = selectColumn + " , ";
                }


            }

            /*tableJoinQuery= "SELECT * FROM " + inputRunDetails_1.getSchemaName() + "." + ValidationTableName + " val JOIN " +
                    inputRunDetails_1.getSchemaName() + "." + inputRunDetails_1.getTableName() +" target "+ tableJoinClause+
                    " where val.run_id='" + inputRunDetails_1.getRunId() + "' ";*/

            //tableJoinQuery = "SELECT *, " + selectColumn + " FROM " + inputRunDetails_1.getSchemaName() + "." + ValidationTableName + " val JOIN " + inputRunDetails_1.getSchemaName() + "." + inputRunDetails_1.getTableName() + " target " + tableJoinClause + " where val.run_id='" + inputRunDetails_1.getRunId() + "' ";
            tableJoinQuery = "SELECT * FROM " + inputRunDetails_1.getSchemaName() + "." + ValidationTableName + " val JOIN " + inputRunDetails_1.getSchemaName() + "." + inputRunDetails_1.getTableName() + " target " + tableJoinClause + " where val.run_id='" + inputRunDetails_1.getRunId() + "' ";
        }

        return tableJoinQuery;
    }

    private String getUniqueColumnFromValTable(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo, String ValidationTableName) throws Exception {
        String selectColumnsQuery = "SELECT val_log FROM " + inputRunDetails_1.getSchemaName() + "." + ValidationTableName + WHERE + RUN_ID1 + inputRunDetails_1.getRunId() + "' " + " and val_type='Log-Cols-List'";
        String logColsList="";
        try (Connection dbConn =dataSource.getDBConnection();
             PreparedStatement pst = dbConn.prepareStatement(selectColumnsQuery);){
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                logColsList = rs.getString("val_log");
            }
        } catch (SQLException ex) {
            logger.error("Exception while fetching table details in getUniqueColumnFromValTable");
            logger.error(ex.getMessage());
        }
        return logColsList;
    }

    public List<RunDetails> getRunDetails(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo) throws Exception {
        List<RunDetails> outputRunDetailsList = new ArrayList<>();
        String query = null;
        String queryWithOptionalParam = "SELECT * FROM public.run_details WHERE " +
                SOURCE_HOST_NAME + inputRunDetails_1.getSourceHostName() + "' " +
                "and target_host_name='" + inputRunDetails_1.getTargetHostName() + "' " +
                "and database_name='" + inputRunDetails_1.getDatabaseName() + "' " +
                "and schema_name='" + inputRunDetails_1.getSchemaName() + "' ";
        if (inputRunDetails_1.getTableName() != null) {
            queryWithOptionalParam = queryWithOptionalParam + "and table_name='" + inputRunDetails_1.getTableName() + "' ";
        }
        if (inputRunDetails_1.getSchemaRun() != 0) {
            queryWithOptionalParam = queryWithOptionalParam + "and schema_run='" + inputRunDetails_1.getSchemaRun() + "' ";
        }
        if (inputRunDetails_1.getTableRun() != 0) {
            queryWithOptionalParam = queryWithOptionalParam + " and table_run='" + inputRunDetails_1.getTableRun() + "' ";
        }
        query = queryWithOptionalParam;
        try (Connection dbConn =dataSource.getDBConnection();
             PreparedStatement pst = dbConn.prepareStatement(query);){
             ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                RunDetails runDetails = new RunDetails();
                runDetails.setSourceHostName(rs.getString(SOURCE_HOST_NAME1));
                runDetails.setTargetHostName(rs.getString(TARGET_HOST_NAME));
                runDetails.setDatabaseName(rs.getString(DATABASE_NAME));
                runDetails.setSchemaName(rs.getString(SCHEMA_NAME));
                runDetails.setTableName(rs.getString(TABLE_NAME));
                runDetails.setSchemaRun(rs.getInt(SCHEMA_RUN));
                runDetails.setTableRun(rs.getInt(TABLE_RUN));
                runDetails.setRunId(rs.getString(RUN_ID));
                runDetails.setExecutionDate(rs.getString(EXECUTION_DATE));
                outputRunDetailsList.add(runDetails);
            }
        } catch (SQLException ex) {
            logger.error("Exception while fetching table details in getRunDetails");
            logger.error(ex.getMessage());
        }
        return outputRunDetailsList;
    }

    public boolean executeDbProcedure(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo) throws Exception {
        boolean executeDbProcedureResult = false;

        try (Connection dbConn =dataSource.getDBConnection();
             PreparedStatement pst = dbConn.prepareStatement("call helloworld()");){
            pst.execute();
            executeDbProcedureResult = true;
        } catch (SQLException ex) {
            logger.error("Exception in execute Db Procedure");
            logger.error(ex.getMessage());
        }
        return executeDbProcedureResult;
    }

    public int insertRunDetailsRecord(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo) throws Exception {
        int updateResult = 0;
        String insertQuery = "Insert into public.run_details ( source_host_name,target_host_name,database_name,schema_name, table_name,schema_run, execution_date)";

        insertQuery = insertQuery + " values (" +
                "'" + inputRunDetails_1.getSourceHostName() + "'," +
                "'" + inputRunDetails_1.getTargetHostName() + "'," +
                "'" + inputRunDetails_1.getDatabaseName() + "'," +
                "'" + inputRunDetails_1.getSchemaName() + "'," +
                "'" + inputRunDetails_1.getTableName() + "'," +
                "'" + inputRunDetails_1.getSchemaRun() + "'," +
                "current_timestamp)";

        try (
                Connection dbConn = dataSource.getDBConnection();
            PreparedStatement pst = dbConn.prepareStatement(insertQuery);){
            updateResult = pst.executeUpdate();
        } catch (SQLException ex) {
            logger.error("Exception while fetching table details insertRunDetailsRecord");
            logger.error(ex.getMessage());
        }

        return updateResult;
    }

    public RecommendationResponse getRecommendationResponseV2(RunDetails runDetails) throws Exception {
        RecommendationResponse recommendationResponse = new RecommendationResponse();
        ExcelDataRequest excelDataRequest = ExcelDataRequest.builder().runId(runDetails.getRunId()).schemaName(runDetails.getSchemaName()).tableName(runDetails.getTableName()).validationRequest(runDetails.isValidationRequest()).build();
        Connection con=null;
       try{  con=dataSource.getDBConnection();
        ResultSet resultSet = getResultSet(excelDataRequest, recommendationResponse,con);
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        String tableName = resultSetMetaData.getTableName(1);
        String schemaName = excelDataRequest.getSchemaName();
        recommendationResponse.setTable(schemaName + "." + tableName);
        recommendationResponse.setCurrentPage(1);
        recommendationResponse.setPageSize(10);
        int rowCount = 0;
        List<RecommendationRow> recommendationRowList = new ArrayList<>();
        if (resultSet != null) {
            ResultSetMetaData rsmd = resultSet.getMetaData();
            Map<Object, Map<String, Object>> srcValueMap = new HashMap<>();
            Map<Object, Map<String, Object>> tgtValueMap = new HashMap<>();
            Map<Object, Integer> valIdMap = new HashMap<>();
            int valId=0;
            String durationText="";
            while (resultSet.next()) {
                Map<String, Object> rsKeyValMap = new HashMap<>();
                for (int index = 0; index < rsmd.getColumnCount(); index++) {
                    // get the SQL column name
                    String columnName = rsmd.getColumnName(index + 1);
                    // get the value of the SQL column
                    Object columnValue = resultSet.getObject(index + 1);
                  // if(columnName!=null && !columnName.equalsIgnoreCase("exception_status"))
                    rsKeyValMap.put(columnName, columnValue);
                }
                valIdMap.put(resultSet.getObject(4),Integer.valueOf(resultSet.getString(2)));
                if (MISMATCH_SRC.equalsIgnoreCase(resultSet.getObject(3).toString()) ||
                        MISSING.equalsIgnoreCase(resultSet.getObject(3).toString())) {
                    srcValueMap.put(resultSet.getObject(4), rsKeyValMap);
                } else if (MISMATCH_TRG.equalsIgnoreCase(resultSet.getObject(3).toString())||
                        "EXTRA_RECORD".equalsIgnoreCase(resultSet.getObject(3).toString())) {
                    tgtValueMap.put(resultSet.getObject(4), rsKeyValMap);
                }

            }
            List<String> AdminColumnNameOfValTable = getAdminColumnNameOfValTable();
            for (Object srcKey : srcValueMap.keySet()) {
                rowCount++;
                List<RecommendationColumn> recommendationColumns = new ArrayList<>();
                Object recommendationCode = 1;
                Map<String, Object> srcValue = srcValueMap.get(srcKey);
                Map<String, Object> tgtValue = new HashMap<>();
                if (tgtValueMap.containsKey(srcKey)) {
                    tgtValue = tgtValueMap.get(srcKey);
                }
                for (Object srcColumns : srcValue.keySet()) {
                    if (!AdminColumnNameOfValTable.contains(srcColumns.toString())) {
                        if (tgtValue.containsKey(srcColumns)) {
                            recommendationColumns.add(new RecommendationColumn(srcColumns.toString(), srcValue.get(srcColumns), tgtValue.get(srcColumns)));
                        } else {
                            recommendationColumns.add(new RecommendationColumn(srcColumns.toString(), srcValue.get(srcColumns), ""));
                        }
                    }
                    if (EXCEPTION_STATUS.equalsIgnoreCase(srcColumns.toString())) {
                        if (MISMATCH_SRC.equalsIgnoreCase(srcValue.get(srcColumns).toString())) {
                            recommendationCode = 2;
                        } else if (MISSING.equalsIgnoreCase(srcValue.get(srcColumns).toString())) {
                            recommendationCode = 1;
                        } else if (MISMATCH_TRG.equalsIgnoreCase(srcValue.get(srcColumns).toString())) {
                            recommendationCode = 3;
                        }else{
                            recommendationCode = 4;
                            durationText=resultSet.getString(4);
                        }
                    }
                }
                recommendationRowList.add(new RecommendationRow(recommendationCode, recommendationColumns,valIdMap.get(srcKey),durationText));
            }
            for (Object tgtKey : tgtValueMap.keySet()) {
                rowCount++;
                List<RecommendationColumn> recommendationColumns = new ArrayList<>();
                Object recommendationCode = 1;
                Map<String, Object> tgtValue = tgtValueMap.get(tgtKey);
                Map<String, Object> srcValue = new HashMap<>();
                if (srcValueMap.containsKey(tgtKey)) {
                    srcValue = srcValueMap.get(tgtKey);
                }
                for (Object tgtColumns : tgtValue.keySet()) {
                    if (!AdminColumnNameOfValTable.contains(tgtColumns.toString())) {
//                        if (srcValue.containsKey(tgtColumns)) {
//                            recommendationColumns.add(new RecommendationColumn(tgtColumns.toString(), srcValue.get(tgtColumns), tgtValue.get(tgtColumns)));
//                        } else {
//                            recommendationColumns.add(new RecommendationColumn(tgtColumns.toString(), srcValue.get(tgtColumns), tgtValue.get(tgtColumns)));
//                        }
                        recommendationColumns.add(new RecommendationColumn(tgtColumns.toString(), srcValue.get(tgtColumns), tgtValue.get(tgtColumns)));
                    }
                    if (EXCEPTION_STATUS.equalsIgnoreCase(tgtColumns.toString())) {
                        if (MISMATCH_SRC.equalsIgnoreCase(tgtValue.get(tgtColumns).toString())) {
                            recommendationCode = 2;
                        } else if (MISSING.equalsIgnoreCase(tgtValue.get(tgtColumns).toString())) {
                            recommendationCode = 1;
                        } else if (MISMATCH_TRG.equalsIgnoreCase(tgtValue.get(tgtColumns).toString())) {
                            recommendationCode = 3;
                        }else{
                            recommendationCode = 4;
                            //durationText=resultSet.getString(4);
                        }
                    }
                }
                recommendationRowList.add(new RecommendationRow(recommendationCode, recommendationColumns,valIdMap.get(tgtKey),durationText));
            }
        }

        recommendationResponse.setTotalRecords(rowCount);
        recommendationResponse.setRows(recommendationRowList);
       } catch (SQLException e) {
           e.printStackTrace();
       }
       finally {
            if(con!=null){
                con.close();
            }
       }
        return recommendationResponse;
    }

    private ResultSet getResultSet(ExcelDataRequest excelDataRequest, RecommendationResponse recommendationResponse,Connection con) throws Exception {
        ResultSet rs=null;

        try {

            String pk =null;
            StringBuilder stb=new StringBuilder();
            boolean firstCol=true;
            DatabaseMetaData meta = con.getMetaData();
            rs = meta.getPrimaryKeys(null, excelDataRequest.getSchemaName(), excelDataRequest.getTableName());
            ResultSet rs1=meta.getColumns(null,excelDataRequest.getSchemaName(),excelDataRequest.getTableName(),null);
            ArrayList list = new ArrayList<String>();
            while (rs.next()) {
                pk = rs.getString(4);
                if(!pk.isEmpty()){
                    recommendationResponse.setUniqueColumns(Arrays.asList(pk.split(",")));
                }
                else{
                    recommendationResponse.setUniqueColumns(new ArrayList<>());
                }
            }
            while (rs1.next()) {
                String col = rs1.getString("COLUMN_NAME");
                if(!firstCol)
                    stb.append(",");
                stb.append(col);
                firstCol=false;
                list.add(col);
            }
            PreparedStatement pst = null ;
            excelDataRequest.setColList(list);
            String preparedQuery="";
            if(!excelDataRequest.isValidationRequest()){

                preparedQuery=  "SELECT SRC.*, DENSE_RANK () OVER ( ORDER BY SRC.val_id  ASC) EXCEPTION_RANK FROM \n" +
                    "(SELECT RUN_ID, VAL_ID, UPPER(VAL_TYPE) AS EXCEPTION_STATUS,"+stb.toString()+" FROM "+excelDataRequest.getSchemaName()+"."+excelDataRequest.getTableName()+"_val \n" +
                    "WHERE RUN_ID = ? \n" +
                    "AND UPPER(VAL_TYPE) IN ('MISMATCH_SRC','MISMATCH_TRG','MISSING','EXTRA_RECORD') \n" +
                    ") SRC ORDER BY EXCEPTION_RANK ASC,VAL_ID ASC;\n";
                try {
                        pst = con.prepareStatement(preparedQuery,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                pst.setString(1, excelDataRequest.getRunId());
            }
            else {
                preparedQuery = "SELECT SRC.*, DENSE_RANK () OVER ( ORDER BY SRC.val_id  ASC) EXCEPTION_RANK FROM \n" +
                        "(SELECT RUN_ID, VAL_ID, UPPER(VAL_TYPE) AS EXCEPTION_STATUS,"+stb.toString()+" FROM "+excelDataRequest.getSchemaName()+"."+excelDataRequest.getTableName()+"_val \n" +
                        "WHERE RUN_ID IN ( SELECT RUN_ID FROM "+excelDataRequest.getSchemaName()+"."+excelDataRequest.getTableName()+"_val ORDER BY VAL_ID DESC LIMIT 1 ) \n" +
                        "AND UPPER(VAL_TYPE) IN ('MISMATCH_SRC','MISMATCH_TRG','MISSING','LOG-END','EXTRA_RECORD')  \n" +
                        ") SRC ORDER BY EXCEPTION_RANK ASC,VAL_ID ASC";
                try {
                    pst = con.prepareStatement(preparedQuery,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            rs= pst.executeQuery();
            excelDataRequest.setResultSet(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

}

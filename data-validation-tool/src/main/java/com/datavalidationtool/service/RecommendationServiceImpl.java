/**
 * Implementation class for recommendation APIs.
 *
 * @author Rajeshkumar Kagathara
 * @version 1.0
 */

package com.datavalidationtool.service;

import com.datavalidationtool.model.DatabaseInfo;
import com.datavalidationtool.model.RunDetails;
import com.datavalidationtool.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    public Logger logger = LoggerFactory.getLogger("RecommendationServiceImpl");

    public String recommendationApiTest() {
        logger.info("Recommendation Api Test Successful");
        return "Recommendation Api Test Successful";
    }


    public List<String> getDbSchemaDetails(DatabaseInfo databaseInfo) {
        List<String> schemaList = new ArrayList<>();
        String query = "SELECT schema_name FROM information_schema.schemata where schema_owner ='postgres'";
        //String query = "SELECT schema_name FROM information_schema.schemata";

        try (Connection dbConn = getConnection(databaseInfo);
             PreparedStatement pst = dbConn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
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
        return schemaList;

    }

    public List<String> getSchemaTableDetails(DatabaseInfo databaseInfo, String schemaName) {

        List<String> tableList = new ArrayList<>();
        String preparedQuery = "SELECT table_name FROM information_schema.tables WHERE table_schema=?";

        try (Connection con = getConnection(databaseInfo);
             PreparedStatement pst = con.prepareStatement(preparedQuery)) {
            pst.setString(1, schemaName);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    tableList.add(rs.getString(1));
                }
            }

        } catch (SQLException ex) {
            logger.error("SQL Exception while fetching table details");
            logger.error(ex.getMessage());
        } catch (Exception e) {
            logger.error("Generic Exception while fetching table details");
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
            whereQueryCondition = whereQueryCondition + " WHERE ";
            whereQueryCondition = whereQueryCondition + "source_host_name='" + inputRunDetails_1.getSourceHostName() + "' ";
        }

        if (inputRunDetails_1.getTargetHostName() != null) {
            whereQueryCondition = whereQueryCondition + " and " + "target_host_name='" + inputRunDetails_1.getTargetHostName() + "' ";
        }

        if (inputRunDetails_1.getDatabaseName() != null) {
            whereQueryCondition = whereQueryCondition + " and " + "database_name='" + inputRunDetails_1.getDatabaseName() + "' ";
        }

        if (inputRunDetails_1.getSchemaName() != null) {
            whereQueryCondition = whereQueryCondition + " and " + "schema_name='" + inputRunDetails_1.getSchemaName() + "' ";
        }

        if (inputRunDetails_1.getTableName() != null) {
            whereQueryCondition = whereQueryCondition + " and " + "table_name='" + inputRunDetails_1.getTableName() + "' ";
        }

        if (inputRunDetails_1.getSchemaRun() != 0) {
            whereQueryCondition = whereQueryCondition + " and " + "schema_run='" + inputRunDetails_1.getSchemaRun() + "' ";
        }

        if (inputRunDetails_1.getTableRun() != 0) {
            whereQueryCondition = whereQueryCondition + " and " + "table_run='" + inputRunDetails_1.getTableRun() + "' ";
        }

        query = queryWithOptionalParam + whereQueryCondition;

        try (Connection dbConn = getConnection(databaseInfo);
             PreparedStatement pst = dbConn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

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
        }
        return outputRunDetailsList;
    }

    public List<RunDetails> getHostRunDetails(String hostName, DatabaseInfo databaseInfo) throws Exception {

        List<RunDetails> outputRunDetailsList = new ArrayList<>();
        String query = "SELECT * FROM public.run_details WHERE "
                + "source_host_name='" + hostName + "' ";

        try (Connection dbConn = getConnection(databaseInfo);
             PreparedStatement pst = dbConn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

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
        }
        return outputRunDetailsList;
    }

    @Override
    public List<RunDetails> getHostRunDetailsForSelection(DatabaseInfo databaseInfo) throws Exception {

        List<RunDetails> outputRunDetailsList = new ArrayList<>();
        String query = "SELECT * FROM public.run_details";

        try (Connection dbConn = getConnection(databaseInfo);
             PreparedStatement pst = dbConn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

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
        }
        return outputRunDetailsList;
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
                    Set<RunWithDate> schemaRun = new TreeSet<>();
                    for(RunDetails uTableRd:uniqueSchemaRunDetails) {
                        RunWithDate runWithDate = new RunWithDate();
                        java.util.Date executionDate = convertStrDateToDate(uTableRd.getExecutionDate());
                        runWithDate.setRun(uTableRd.getTableRun());
                        runWithDate.setExecutionDate(executionDate);
                        schemaRun.add(runWithDate);
                    }
                    SchemaDetails schemaDetails = new SchemaDetails();
                    schemaDetails.setSchemaName(schemaNm);
                    schemaDetails.setSchemaRun(schemaRun);


                    List<TableDetails> tableDetailsList = new ArrayList<>();
                    Set<String> uniqueTableNm = runDetails.stream().filter(rd -> rd.getSourceHostName().equals(hostNm) && rd.getDatabaseName().equals(dbNm) && rd.getSchemaName().equals(schemaNm)).map(rd -> rd.getTableName()).collect(Collectors.toSet());
                    for (String tableNm : uniqueTableNm) {
                        Set<RunDetails> uniqueTableRunDetails = runDetails.stream().filter(rd -> rd.getSourceHostName().equals(hostNm) && rd.getDatabaseName().equals(dbNm) && rd.getSchemaName().equals(schemaNm) && rd.getTableName().equals(tableNm)).collect(Collectors.toSet());
                        Set<RunWithDate> tableRun = new TreeSet<>();
                        for(RunDetails uTableRd:uniqueTableRunDetails) {
                            RunWithDate runWithDate = new RunWithDate();
                            Date executionDate = convertStrDateToDate(uTableRd.getExecutionDate());
                            runWithDate.setRun(uTableRd.getTableRun());
                            runWithDate.setExecutionDate(executionDate);
                            tableRun.add(runWithDate);
                        }
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
        String selectQuery = "SELECT val_id FROM " + inputRunDetails_1.getSchemaName() + "." + ValidationTableName + " WHERE " +
                "run_id='" + inputRunDetails_1.getRunId() + "' "
                + "ORDER BY val_id ASC ";

        //+"ORDER BY val_id ASC LIMIT 100";

        try (Connection dbConn = getConnection(databaseInfo);
             PreparedStatement pst = dbConn.prepareStatement(selectQuery);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                valIdList.add(rs.getInt("val_id"));
            }
        } catch (SQLException ex) {
            logger.error("Exception while fetching table details");
            logger.error(ex.getMessage());
        }
        return valIdList;
    }


    public List<Map<String, Object>> getEntireValidationTable(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo, String ValidationTableName) throws Exception {

        String selectQuery = "SELECT * FROM " + inputRunDetails_1.getSchemaName() + "." + ValidationTableName + " WHERE " +
                "run_id='" + inputRunDetails_1.getRunId() + "' "
         //       + "ORDER BY val_id ASC ";

        +"ORDER BY val_id ASC LIMIT 10";


        String tableJoinQuery = getTableJoinQueryForSourceTarget( inputRunDetails_1,  databaseInfo,  ValidationTableName);
        System.out.println(" tableJoinQuery ->"+tableJoinQuery );

        List<Map<String,Object>> rsKeyValMapList = new ArrayList<>();

        try (Connection dbConn = getConnection(databaseInfo);
             //PreparedStatement pst = dbConn.prepareStatement(selectQuery);){

            //use below table join query to get source and target records.
             PreparedStatement pst = dbConn.prepareStatement(tableJoinQuery);){

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

                        if(rsKeyValMap.containsKey(columnName)){
                            rsKeyValMap.put("target_"+columnName, columnValue);

                        }else{
                            rsKeyValMap.put(columnName, columnValue);

                        }
                        //rsKeyValMap.put(columnName, columnValue);

                        //System.out.println(columnName +" :"+columnValue);
                    }
                    //System.out.println("-------");
                    /*
                    if(null == rsKeyValMap.get("val_log")){
                        rsKeyValMapList.add(rsKeyValMap);
                    }*/
                    rsKeyValMapList.add(rsKeyValMap);
                }

            }

        } catch (SQLException ex) {
            logger.error("Exception while fetching table details");
            logger.error(ex.getMessage());
        }
        return rsKeyValMapList;
    }

    public List<String> getAdminColumnNameOfValTable(){
        List<String> adminColumnNameOfValTable = new ArrayList<String>();
        adminColumnNameOfValTable.add("val_id");
        adminColumnNameOfValTable.add("val_ts");
        adminColumnNameOfValTable.add("run_id");
        adminColumnNameOfValTable.add("val_log");
        adminColumnNameOfValTable.add("val_type");
        return adminColumnNameOfValTable;
    }

    public RecommendationResponse getRecommendationResponse(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo, String ValidationTableName) throws Exception {

        String tableJoinQuery = getTableJoinQueryForSourceTarget( inputRunDetails_1,  databaseInfo,  ValidationTableName);
        System.out.println(" tableJoinQuery ->"+tableJoinQuery );

        List<Map<String,Object>> rsKeyValMapList = new ArrayList<>();

        RecommendationResponse recommendationResponse = new RecommendationResponse();
        recommendationResponse.setTable(inputRunDetails_1.getSchemaName() + "." + ValidationTableName);
        recommendationResponse.setCurrentPage(0);
        recommendationResponse.setPageSize(0);
        recommendationResponse.setTotalRecords(0);
        String uniqueColumnFromValTable= getUniqueColumnFromValTable(inputRunDetails_1, databaseInfo, ValidationTableName);
        if(!uniqueColumnFromValTable.isEmpty()){
            recommendationResponse.setUniqueColumns(Arrays.asList(uniqueColumnFromValTable.split(",")));
        }
        else{
            recommendationResponse.setUniqueColumns(new ArrayList<>());
        }
        List<RecommendationRow> recommendationRowList = new ArrayList<>();


        try (Connection dbConn = getConnection(databaseInfo);
             //PreparedStatement pst = dbConn.prepareStatement(selectQuery);){

             //use below table join query to get source and target records.
             PreparedStatement pst = dbConn.prepareStatement(tableJoinQuery);){

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

                        if(rsKeyValMap.containsKey(columnName)){
                            rsKeyValMap.put("target_"+columnName, columnValue);

                        }else{
                            rsKeyValMap.put(columnName, columnValue);
                        }
                        List<String> AdminColumnNameOfValTable=getAdminColumnNameOfValTable();
                        if(!AdminColumnNameOfValTable.contains(columnName)){
                            if(rsKeyValMap.containsKey(columnName)){
                                recommendationRowList.add(new RecommendationRow(columnName, rsKeyValMap.get(columnName),columnValue, rsKeyValMap.get("val_type")));
                            }
                        }
                    }
                    rsKeyValMapList.add(rsKeyValMap);
                }

            }

        } catch (SQLException ex) {
            logger.error("Exception while fetching table details");
            logger.error(ex.getMessage());
        }

        recommendationResponse.setRows(recommendationRowList);

        return recommendationResponse;
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
                    tableJoinClause = tableJoinClause + " and ";

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
        String selectColumnsQuery = "SELECT val_log FROM " + inputRunDetails_1.getSchemaName() + "." + ValidationTableName + " WHERE " + "run_id='" + inputRunDetails_1.getRunId() + "' " + " and val_type='Log-Cols-List'";

        System.out.println("selectColumnsQuery -> " + selectColumnsQuery);
        String logColsList="";
        try (Connection dbConn = getConnection(databaseInfo); PreparedStatement pst = dbConn.prepareStatement(selectColumnsQuery); ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                logColsList = rs.getString("val_log");
            }
        } catch (SQLException ex) {
            logger.error("Exception while fetching table details");
            logger.error(ex.getMessage());
        }
        return logColsList;
    }

    public List<RunDetails> getRunDetails(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo) throws Exception {

        List<RunDetails> outputRunDetailsList = new ArrayList<>();
        String query = null;

        String queryWithOptionalParam = "SELECT * FROM public.run_details WHERE " +
                "source_host_name='" + inputRunDetails_1.getSourceHostName() + "' " +
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

        try (Connection dbConn = getConnection(databaseInfo);
             PreparedStatement pst = dbConn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

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
        }
        return outputRunDetailsList;
    }

    public boolean executeDbProcedure(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo) throws Exception {

        boolean executeDbProcedureResult = false;

        try (Connection dbConn = getConnection(databaseInfo);
             PreparedStatement pst = dbConn.prepareStatement("call helloworld()");) {
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

        try {
            Connection dbConn = getConnection(databaseInfo);
            PreparedStatement pst = dbConn.prepareStatement(insertQuery);
            updateResult = pst.executeUpdate();


        } catch (SQLException ex) {
            logger.error("Exception while fetching table details");
            logger.error(ex.getMessage());
        }


        return updateResult;
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
        if (isSslRequire) {
            //props.setProperty("oracle.net.ssl_cipher_suites", "(TLS_ SA_WITH_AES_128_CBC_SHA, TLS_ SA_WITH_AES_256_CBC_SHA, SSL_ SA_WITH_3DES_EDE_CBC_SHA ,SSL_ SA_WITH_ C4_128_SHA,SSL_ SA_WITH_ C4_128_MD5 ,SSL_ SA_WITH_DES_CBC_SHA ,SSL_DH_anon_WITH_3DES_EDE_CBC_SHA,SSL_DH_anon_WITH_ C4_128_MD5,SSL_DH_anon_WITH_DES_CBC_SHA,SSL_ SA_EXPO T_WITH_ C4_40_MD5 ,SSL_ SA_EXPO T_WITH_DES40_CBC_SHA ,TLS_ SA_WITH_AES_128_CBC_SHA,TLS_ SA_WITH_AES_256_CBC_SHA)");
            setSslProperties(props, trustStorePath, trsutStorePassword);
        }
        logger.info("\n" + url);
        Connection conn = DriverManager.getConnection(url, props);

        return conn;
    }


    /**
     * Method for configuring SSL connection properties.
     */
    public void setSslProperties(Properties props, String trustStorePath, String trsutStorePassword) {

        props.setProperty("javax.net.ssl.trustStore",
                trustStorePath);
        props.setProperty("javax.net.ssl.trustStoreType", "JKS");
        props.setProperty("javax.net.ssl.trustStorePassword", trsutStorePassword);

    }

}
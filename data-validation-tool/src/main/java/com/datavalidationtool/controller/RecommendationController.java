/**
 * Controller class for data validation recommendation APIs.
 *
 * @author Rajeshkumar Kagathara
 * @version 1.0
 */

package com.datavalidationtool.controller;

import com.datavalidationtool.model.DatabaseInfo;
import com.datavalidationtool.model.RunDetails;
import com.datavalidationtool.model.response.*;
import com.datavalidationtool.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/recommendation/api")
public class RecommendationController {

    @Autowired
    RecommendationService recommendationService;

    //http://localhost:8080/recommendation/api/test
    @GetMapping("/test")
    public String recommendationApiTest() {

        return recommendationService.recommendationApiTest();
    }

    //http://localhost:8080/recommendation/api/schema
    @GetMapping("/schema")
    public List<String> getDbSchemaDetails() {

        DatabaseInfo databaseInfo = new DatabaseInfo("localhost", 5432, "postgres",
                null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                true, "/certs/", "changeit");

        return recommendationService.getDbSchemaDetails(databaseInfo);
    }


    //http://localhost:8080/recommendation/api/table?schemaName=test_schema_2
    @GetMapping("/table")
    public List<String> getSchemaTableDetails(@RequestParam String schemaName) throws Exception {
        DatabaseInfo databaseInfo = new DatabaseInfo("localhost", 5432, "postgres",
                null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                true, "/certs/", "changeit");

        //String schemaName="test_schema_2";
        return recommendationService.getSchemaTableDetails(databaseInfo, schemaName);
    }

    //http://localhost:8080/recommendation/api/run-details/optional?sourceHostName=localhost&targetHostName=localhost&databaseName=postgres
    //http://localhost:8080/recommendation/api/run-details/optional?sourceHostName=localhost&targetHostName=localhost&databaseName=ttp
    @GetMapping("/run-details/optional")
    public List<RunDetails> getRunDetailsWithOptional(
            @RequestParam Optional<String> sourceHostName,
            @RequestParam Optional<String> targetHostName,
            @RequestParam Optional<String> databaseName,
            @RequestParam Optional<String> schemaName,
            @RequestParam Optional<String> tableName,
            @RequestParam Optional<Integer> schemaRun,
            @RequestParam Optional<Integer> tableRun
    ) throws Exception {

        RunDetails inputRunDetails = new RunDetails();
        if (sourceHostName.isPresent()) {
            inputRunDetails.setSourceHostName(sourceHostName.get());
        }
        if (targetHostName.isPresent()) {
            inputRunDetails.setTargetHostName(targetHostName.get());
        }
        if (databaseName.isPresent()) {
            inputRunDetails.setDatabaseName(databaseName.get());
        }
        if (schemaName.isPresent()) {
            inputRunDetails.setSchemaName(schemaName.get());
        }
        if (tableName.isPresent()) {
            inputRunDetails.setTableName(tableName.get());
        }
        if (schemaRun.isPresent()) {
            inputRunDetails.setSchemaRun(schemaRun.get());
        }
        if (tableRun.isPresent()) {
            inputRunDetails.setTableRun(tableRun.get());
        }

        DatabaseInfo databaseInfo = new DatabaseInfo("localhost", 5432,
                "postgres", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                true, "/certs/", "changeit");

        return recommendationService.getRunDetailsWithOptional(inputRunDetails, databaseInfo);
    }


    //http://localhost:8080/recommendation/api/host-run-details?sourceHostName=localhost

    @GetMapping("/host-run-details")
    public List<RunDetails> getHostRunDetails(@RequestParam String sourceHostName) throws Exception {

        DatabaseInfo databaseInfo = new DatabaseInfo("localhost", 5432,
                "postgres", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                true, "/certs/", "changeit");

        List<RunDetails> runDetails = recommendationService.getHostRunDetails(sourceHostName, databaseInfo);

        return runDetails;
    }


    //http://localhost:8080/recommendation/api/host-run-details/custom?sourceHostName=localhost
    @GetMapping("/host-run-details/custom")
    public Object getHostRunDetailsCustomResponse(@RequestParam String sourceHostName) throws Exception {

        DatabaseInfo databaseInfo = new DatabaseInfo("localhost", 5432,
                "postgres", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                true, "/certs/", "changeit");

        List<RunDetails> runDetails = recommendationService.getHostRunDetails(sourceHostName, databaseInfo);

        if (!runDetails.isEmpty()) {
            return getRunDetailsCustomResponse(runDetails);
        }
        return runDetails;
    }

    private static RunDetailsResponse getRunDetailsCustomResponse(List<RunDetails> runDetails) {
        RunDetailsResponse runDetailsResponse = new RunDetailsResponse();

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
                    SchemaDetails schemaDetails = new SchemaDetails();
                    schemaDetails.setSchemaName(schemaNm);
                    Set<Integer> uniqueSchemaRun = runDetails.stream().filter(rd -> rd.getSourceHostName().equals(hostNm) && rd.getDatabaseName().equals(dbNm) && rd.getSchemaName().equals(schemaNm)).map(rd -> rd.getSchemaRun()).collect(Collectors.toSet());
                    schemaDetails.setSchemaRun(uniqueSchemaRun.stream().collect(Collectors.toList()));

                    List<TableDetails> tableDetailsList = new ArrayList<>();
                    Set<String> uniqueTableNm = runDetails.stream().filter(rd -> rd.getSourceHostName().equals(hostNm) && rd.getDatabaseName().equals(dbNm) && rd.getSchemaName().equals(schemaNm)).map(rd -> rd.getTableName()).collect(Collectors.toSet());
                    for (String tableNm : uniqueTableNm) {
                        Set<Integer> uniqueTableRun = runDetails.stream().filter(rd -> rd.getSourceHostName().equals(hostNm) && rd.getDatabaseName().equals(dbNm) && rd.getSchemaName().equals(schemaNm) && rd.getTableName().equals(tableNm)).map(rd -> rd.getTableRun()).collect(Collectors.toSet());

                        TableDetails tableDetails = new TableDetails();
                        tableDetails.setTableName(tableNm);
                        tableDetails.setTableRun(uniqueTableRun.stream().collect(Collectors.toList()));
                        tableDetailsList.add(tableDetails);
                    }
                    schemaDetails.setTableList(tableDetailsList);
                    schemaDetailsList.add(schemaDetails);

                }
                databaseDetails.setSchemaList(schemaDetailsList);
                databaseDetailsList.add(databaseDetails);
            }
            hostDetails.setDatabaseList(databaseDetailsList);
            runDetailsResponse.setHostDetails(hostDetails);
        }
        return runDetailsResponse;
    }

    //http://localhost:8080/recommendation/api/run-details/source/val-id?sourceHostName=localhost&targetHostName=localhost&databaseName=ttp
    @GetMapping("/run-details/source/val-id")
    public List<Integer> getValIdFromValidationTable(
            @RequestParam Optional<String> sourceHostName,
            @RequestParam Optional<String> targetHostName,
            @RequestParam Optional<String> databaseName,
            @RequestParam Optional<String> schemaName,
            @RequestParam Optional<String> tableName,
            @RequestParam Optional<Integer> schemaRun,
            @RequestParam Optional<Integer> tableRun
    ) throws Exception {

        RunDetails inputRunDetails = new RunDetails();
        if (sourceHostName.isPresent()) {
            inputRunDetails.setSourceHostName(sourceHostName.get());
        }
        if (targetHostName.isPresent()) {
            inputRunDetails.setTargetHostName(targetHostName.get());
        }
        if (databaseName.isPresent()) {
            inputRunDetails.setDatabaseName(databaseName.get());
        }
        if (schemaName.isPresent()) {
            inputRunDetails.setSchemaName(schemaName.get());
        }
        if (tableName.isPresent()) {
            inputRunDetails.setTableName(tableName.get());
        }
        if (schemaRun.isPresent()) {
            inputRunDetails.setSchemaRun(schemaRun.get());
        }
        if (tableRun.isPresent()) {
            inputRunDetails.setTableRun(tableRun.get());
        }

        DatabaseInfo runTableDbInfo = new DatabaseInfo("localhost", 5432,
                "postgres", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                true, "/certs/", "changeit");

        List<RunDetails> runDetailsList = recommendationService.getRunDetailsWithOptional(inputRunDetails, runTableDbInfo);
        Optional<RunDetails> runDetails = runDetailsList.stream().findFirst();


        List<Integer> sourceValIdFromValidationTable = new ArrayList<>();
        if (runDetails.isPresent()) {
            DatabaseInfo valTableDbInfo = new DatabaseInfo("localhost", 5432,
                    "ttp", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                    true, "/certs/", "changeit");

            String validationTableName = runDetails.get().getTableName() + "_val";
            sourceValIdFromValidationTable = recommendationService.getValIdFromValidationTable(runDetails.get(), valTableDbInfo, validationTableName);
        }
        return sourceValIdFromValidationTable;

    }


    //http://localhost:8080/recommendation/api/run-details
    @GetMapping("/run-details-old")
    public List<RunDetails> getRunDetails() throws Exception {

        RunDetails inputRunDetails_1 = new RunDetails("localhost", "localhost", "postgres", "public", "company");

        DatabaseInfo databaseInfo = new DatabaseInfo("localhost", 5432,
                "postgres", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                true, "/certs/", "changeit");

        return recommendationService.getRunDetails(inputRunDetails_1, databaseInfo);
    }


    @GetMapping("/execute-db-procedure")
    public boolean executeDbProcedure() throws Exception {

        //RunDetails inputRunDetails_1 = new RunDetails("localhost","localhost","postgres","public","company",3);
        RunDetails inputRunDetails_1 = new RunDetails("localhost", "localhost", "postgres", "public", "company");
        //RunDetails inputRunDetails_1 = new RunDetails("localhost","localhost","postgres","public");

        DatabaseInfo databaseInfo = new DatabaseInfo("localhost", 5432,
                "postgres", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                true, "/certs/", "changeit");

        return recommendationService.executeDbProcedure(inputRunDetails_1, databaseInfo);
    }

    @GetMapping("/insert-run-details-record")
    public int insertRunDetailsRecord() throws Exception {
        RunDetails inputRunDetails_1 = new RunDetails("localhost", "localhost", "postgres", "public", "company", 5);
        DatabaseInfo databaseInfo = new DatabaseInfo("localhost", 5432,
                "postgres", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                true, "/certs/", "changeit");

        return recommendationService.insertRunDetailsRecord(inputRunDetails_1, databaseInfo);
    }

}
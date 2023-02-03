/**
 * Controller class for data validation recommendation APIs.
 *
 * @author Rajeshkumar Kagathara
 * @version 1.0
 */

package com.datavalidationtool.controller;

import com.datavalidationtool.model.DatabaseInfo;
import com.datavalidationtool.model.RunDetails;
import com.datavalidationtool.model.response.RecommendationResponse;
import com.datavalidationtool.service.ExcelDataService;
import com.datavalidationtool.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

@RestController
@RequestMapping("/recommendation/api")
public class RecommendationController {

    @Autowired
    RecommendationService recommendationService;
    @Autowired
    private ExcelDataService excelDataService;

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


    //http://localhost:8080/recommendation/api/host-run-details/selection
    @GetMapping("/host-run-details/selection")
    public Object getHostRunDetailsSelectionResponse() throws Exception {

        DatabaseInfo databaseInfo = new DatabaseInfo("localhost", 5432,
                "postgres", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                true, "/certs/", "changeit");

        List<RunDetails> runDetailBeans = recommendationService.getHostRunDetailsForSelection(databaseInfo);

        if (!runDetailBeans.isEmpty()) {
            return recommendationService.getRunDetailsSelectionResponse(runDetailBeans);
        }
        return runDetailBeans;
    }

    //http://localhost:8080/recommendation/api/host-run-details/custom?sourceHostName=localhost
    @GetMapping("/host-run-details/custom")
    public Object getHostRunDetailsCustomResponse(@RequestParam String sourceHostName) throws Exception {

        DatabaseInfo databaseInfo = new DatabaseInfo("localhost", 5432,
                "postgres", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                true, "/certs/", "changeit");

        List<RunDetails> runDetails = recommendationService.getHostRunDetails(sourceHostName, databaseInfo);

        if (!runDetails.isEmpty()) {
            return recommendationService.getRunDetailsSelectionResponse(runDetails);
        }
        return runDetails;
    }

    //http://localhost:8080/recommendation/api/source-target/records?sourceHostName=localhost&targetHostName=localhost&databaseName=ttp&schemaName=ops$ora&tableName=ppt12&schemaRun=1&tableRun=2
    @PostMapping(path = "/source-target/records",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Integer> getValIdFromValidationTable(@RequestBody RunDetails inputRunDetails) throws Exception {


        DatabaseInfo runTableDbInfo = new DatabaseInfo("localhost", 5432,
                "postgres", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                true, "/certs/", "changeit");

        List<RunDetails> runDetailsList = recommendationService.getRunDetailsWithOptional(inputRunDetails, runTableDbInfo);
        Optional<RunDetails> runDetails = runDetailsList.stream().findFirst();


        List<Integer> sourceValIdFromValidationTable = new ArrayList<>();
        if(runDetails.isPresent()) {
            DatabaseInfo valTableDbInfo = new DatabaseInfo("localhost", 5432,
                    "ttp", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                    true, "/certs/", "changeit");

            String validationTableName = runDetails.get().getTableName() + "_val";
            sourceValIdFromValidationTable = recommendationService.getValIdFromValidationTable(runDetails.get(), valTableDbInfo, validationTableName);
        }
        return sourceValIdFromValidationTable;

    }



    //http://localhost:8080/recommendation/api/source-target/entire-records
    // Pass below as post request body
    // ?sourceHostName=localhost&targetHostName=localhost&databaseName=ttp&schemaName=ops$ora&tableName=ppt12&schemaRun=1&tableRun=2
    @PostMapping(path = "/source-target/entire-records",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> getEntireValidationTable(@RequestBody RunDetails inputRunDetails) throws Exception {


        DatabaseInfo runTableDbInfo = new DatabaseInfo("localhost", 5432,
                "postgres", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                true, "/certs/", "changeit");

        List<RunDetails> runDetailsList = recommendationService.getRunDetailsWithOptional(inputRunDetails, runTableDbInfo);
        Optional<RunDetails> runDetails = runDetailsList.stream().findFirst();


        List<Map<String, Object>> entireValidationTable = new ArrayList<>();
        if(runDetails.isPresent()) {
            DatabaseInfo valTableDbInfo = new DatabaseInfo("localhost", 5432,
                    "ttp", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                    true, "/certs/", "changeit");

            String validationTableName = runDetails.get().getTableName() + "_val";
            entireValidationTable = recommendationService.getEntireValidationTable(runDetails.get(), valTableDbInfo, validationTableName);
        }
        return entireValidationTable;

    }

    //http://localhost:8080/recommendation/api/source-target/recommendation
    // Pass below as post request body
    // ?sourceHostName=localhost&targetHostName=localhost&databaseName=ttp&schemaName=ops$ora&tableName=ppt12&schemaRun=1&tableRun=2

    @PostMapping(path = "/source-target/recommendation",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
        RecommendationResponse getRecommendationResponse(@RequestBody RunDetails inputRunDetails) throws Exception {


            DatabaseInfo runTableDbInfo = new DatabaseInfo("localhost", 5432,
                    "postgres", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                    true, "/certs/", "changeit");

            List<RunDetails> runDetailsList = recommendationService.getRunDetailsWithOptional(inputRunDetails, runTableDbInfo);
            Optional<RunDetails> runDetails = runDetailsList.stream().findFirst();


            RecommendationResponse  recommendationResponse= new RecommendationResponse();
            if(runDetails.isPresent()) {
                DatabaseInfo valTableDbInfo = new DatabaseInfo("localhost", 5432,
                        "ttp", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                        true, "/certs/", "changeit");

                String validationTableName = runDetails.get().getTableName() + "_val";
                recommendationResponse = recommendationService.getRecommendationResponse(runDetails.get(), valTableDbInfo, validationTableName);
            }
            return recommendationResponse;

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
        if(runDetails.isPresent()) {
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

    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file ) throws IOException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String timeStampStr=timestamp.toString();
        String fileName = "/Users/amsudan/Desktop/Projects/DataValidation/upload/"+file.getOriginalFilename();
        String home = System.getProperty("user.home");
        try {
            file.transferTo( new File(home + File.separator + "Desktop" + File.separator + "Projects" + File.separator + "DataValidation" + File.separator + "upload" + File.separator + file.getOriginalFilename()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        excelDataService.readExcel(fileName);
        return ResponseEntity.ok("File uploaded successfully.");
    }


}
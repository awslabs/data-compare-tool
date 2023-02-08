/**
 * Controller class for data validation recommendation APIs.
 *
 * @author Rajeshkumar Kagathara
 * @version 1.0
 */

package com.datavalidationtool.controller;

import com.datavalidationtool.model.DatabaseInfo;
import com.datavalidationtool.model.ExcelDataRequest;
import com.datavalidationtool.model.RunDetails;
import com.datavalidationtool.model.response.RecommendationResponse;
import com.datavalidationtool.service.ExcelDataService;
import com.datavalidationtool.service.RecommendationService;
import com.datavalidationtool.service.RemediateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;
@RestController
@RequestMapping("/dvt")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class RecommendationController {

    @Autowired
    RecommendationService recommendationService;
    @Autowired
    private ExcelDataService excelDataService;
    @Autowired
    ServletContext context;
    @Autowired
    RemediateService remediateService;

    //http://localhost:8080/recommendation/test
    @GetMapping("/recommendation/test")
    public String recommendationApiTest() {

        return recommendationService.recommendationApiTest();
    }

    //http://localhost:8080/dvt/recommendation/recommendation-selection?sourceHostName=localhost
    @GetMapping("/recommendation/recommendation-selection")
    public Object getHostRunDetailsSelectionResponse() throws Exception {

        DatabaseInfo databaseInfo = new DatabaseInfo("ukpg-instance-1.cl7uqmhlcmfi.eu-west-2.rds.amazonaws.com", 5432,
                "ttp", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                true, "/certs/", "changeit");

        List<RunDetails> runDetailBeans = recommendationService.getHostRunDetailsForSelection(databaseInfo);

        if (!runDetailBeans.isEmpty()) {
            return recommendationService.getRunDetailsSelectionResponse(runDetailBeans);
        }
        return runDetailBeans;
    }


    @GetMapping(path = "/recommendation/recommendation-data/v1")
    RecommendationResponse getRecommendationResponseV1(@RequestParam Optional<String> schemaName,
                                                       @RequestParam Optional<String> tableName,
                                                       @RequestParam Optional<Integer> schemaRun,
                                                       @RequestParam Optional<Integer> tableRun,
                                                       @RequestParam Optional<Integer> page) throws Exception {


        DatabaseInfo runTableDbInfo = new DatabaseInfo("localhost", 5432,
                "postgres", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                true, "/certs/", "changeit");

        RunDetails inputRunDetails = new RunDetails("ukpg-instance-1.cl7uqmhlcmfi.eu-west-2.rds.amazonaws.com",
                "ukpg-instance-1.cl7uqmhlcmfi.eu-west-2.rds.amazonaws.com",
                "ttp",  "ops$ora", "ppt_3", 1, 1);

                //"ttp",  schemaName.get(), tableName.get(), schemaRun.get(), tableRun.get());

            List<RunDetails> runDetailsList = recommendationService.getRunDetailsWithOptional(inputRunDetails, runTableDbInfo);
        Optional<RunDetails> runDetails = runDetailsList.stream().findFirst();


        RecommendationResponse  recommendationResponse= new RecommendationResponse();
        if(runDetails.isPresent()) {
            DatabaseInfo valTableDbInfo = new DatabaseInfo("ukpg-instance-1.cl7uqmhlcmfi.eu-west-2.rds.amazonaws.com", 5432,
                    "ttp", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                    true, "/certs/", "changeit");

            String validationTableName = runDetails.get().getTableName() + "_val";
            recommendationResponse = recommendationService.getRecommendationResponse(runDetails.get(), valTableDbInfo, validationTableName);
        }
        return recommendationResponse;

    }

    //http://localhost:8080/recommendation/api/source-target/recommendation
    // Pass below as post request body
    // ?sourceHostName=localhost&targetHostName=localhost&databaseName=ttp&schemaName=ops$ora&tableName=ppt12&schemaRun=1&tableRun=2

    @PostMapping(path = "/recommendation/recommendation-data",
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
                DatabaseInfo valTableDbInfo = new DatabaseInfo("ukpg-instance-1.cl7uqmhlcmfi.eu-west-2.rds.amazonaws.com", 5432,
                        "ttp", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                        true, "/certs/", "changeit");

                String validationTableName = runDetails.get().getTableName() + "_val";
                recommendationResponse = recommendationService.getRecommendationResponse(runDetails.get(), valTableDbInfo, validationTableName);
            }
            return recommendationResponse;

        }

    @GetMapping(path = "/recommendation/recommendation-data/v2")
    RecommendationResponse getRecommendationResponseV2(@RequestParam Optional<String> schemaName,
                                                       @RequestParam Optional<String> table,
                                                       @RequestParam Optional<Integer> schemaRun,
                                                       @RequestParam Optional<Integer> tableRun,
                                                       @RequestParam Optional<Integer> page) throws Exception {


        DatabaseInfo runTableDbInfo = new DatabaseInfo("localhost", 5432,
                "postgres", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                true, "/certs/", "changeit");

        RunDetails inputRunDetails = new RunDetails("ukpg-instance-1.cl7uqmhlcmfi.eu-west-2.rds.amazonaws.com",
                "ukpg-instance-1.cl7uqmhlcmfi.eu-west-2.rds.amazonaws.com",
                "ttp",  "ops$ora", table.get().toString(), 1, 1);

        List<RunDetails> runDetailsList = recommendationService.getRunDetailsWithOptional(inputRunDetails, runTableDbInfo);
        Optional<RunDetails> runDetails = runDetailsList.stream().findFirst();

        RecommendationResponse  recommendationResponse= new RecommendationResponse();
        if(runDetails.isPresent()) {
            recommendationResponse = recommendationService.getRecommendationResponseV2(runDetails);
        }
        return recommendationResponse;
    }


    @GetMapping("/remediation/remediate-data")
    public int insertRunDetailsRecord() throws Exception {
        RunDetails inputRunDetails_1 = new RunDetails("localhost", "localhost", "postgres", "public", "company", 5);
        DatabaseInfo databaseInfo = new DatabaseInfo("localhost", 5432,
                "postgres", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                true, "/certs/", "changeit");

        return remediateService.remediateData(inputRunDetails_1);
    }

    @PostMapping("/recommendation/upload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file ) throws Exception {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String timeStampStr=timestamp.toString();
        String fileName = context.getRealPath("upload") + file.getOriginalFilename();
        String home = System.getProperty("user.home");
        try {
       // byte[] bytes = file.getBytes();
      //  Path path = Paths.get(fileName);
      //  Files.write(path, bytes);
         //  file.transferTo( new File(home + File.separator + "Desktop" + File.separator + "Projects" + File.separator + "DataValidation" + File.separator + "upload" + File.separator + file.getOriginalFilename()));
            file.transferTo( new File(  fileName));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        excelDataService.processDBUpdates(fileName);
        return ResponseEntity.ok("File uploaded successfully.");
    }


}
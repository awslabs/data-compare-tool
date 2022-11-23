/**
 * Controller class for data validation recommendation APIs.
 *
 *
 * @author      Rajeshkumar Kagathara
 * @version     1.0
 */

package com.datavalidationtool.controller;

import com.datavalidationtool.model.DatabaseInfo;
import com.datavalidationtool.model.RunDetails;
import com.datavalidationtool.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    //http://localhost:8080/recommendation/api/rundetails
    @GetMapping("/rundetails")
    public List<RunDetails> getRunDetails() throws Exception {

        //RunDetails inputRunDetails_1 = new RunDetails("localhost","localhost","postgres","public","company",3);
        RunDetails inputRunDetails_1 = new RunDetails("localhost", "localhost", "postgres", "public", "company");
        //RunDetails inputRunDetails_1 = new RunDetails("localhost","localhost","postgres","public");

        DatabaseInfo databaseInfo = new DatabaseInfo("localhost", 5432,
                "postgres", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                true, "/certs/", "changeit");

        return recommendationService.getRunDetails(inputRunDetails_1, databaseInfo);
    }

    @GetMapping("/executedbprocedure")
    public boolean executeDbProcedure() throws Exception {

        //RunDetails inputRunDetails_1 = new RunDetails("localhost","localhost","postgres","public","company",3);
        RunDetails inputRunDetails_1 = new RunDetails("localhost", "localhost", "postgres", "public", "company");
        //RunDetails inputRunDetails_1 = new RunDetails("localhost","localhost","postgres","public");

        DatabaseInfo databaseInfo = new DatabaseInfo("localhost", 5432,
                "postgres", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                true, "/certs/", "changeit");

        return recommendationService.executeDbProcedure(inputRunDetails_1, databaseInfo);
    }

    @GetMapping("/insertrundetailsrecord")
    public int insertRunDetailsRecord() throws Exception {
        RunDetails inputRunDetails_1 = new RunDetails("localhost", "localhost", "postgres", "public", "company", 5);
        DatabaseInfo databaseInfo = new DatabaseInfo("localhost", 5432,
                "postgres", null, "postgres", "postgres", false, DatabaseInfo.dbType.POSTGRESQL,
                true, "/certs/", "changeit");

        return recommendationService.insertRunDetailsRecord(inputRunDetails_1, databaseInfo);
    }

}
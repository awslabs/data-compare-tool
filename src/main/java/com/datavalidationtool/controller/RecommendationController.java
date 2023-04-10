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
import com.datavalidationtool.model.request.RecommendationRequest;
import com.datavalidationtool.model.request.RemediateRequest;
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
    @GetMapping("recommendation/test")
    public String recommendationApiTest() {
        return recommendationService.recommendationApiTest();
    }

    //http://localhost:8080/dvt/recommendation/recommendation-selection?sourceHostName=localhost
    @GetMapping("recommendation/recommendation-selection")
    public Object getHostRunDetailsSelectionResponse() throws Exception {
        List<RunDetails> runDetailBeans = recommendationService.getHostRunDetailsForSelection();
        if (!runDetailBeans.isEmpty()) {
            return recommendationService.getRunDetailsSelectionResponse(runDetailBeans);
        }
        return runDetailBeans;
    }


    @PostMapping(path = "recommendation/recommendation-data/v2")
    RecommendationResponse getRecommendationResponseV2(@RequestBody RecommendationRequest recRequest) throws Exception {
        RunDetails runDetails=RunDetails.builder().runId(recRequest.getRunId()).tableName(recRequest.getTableName()).schemaName(recRequest.getSchemaName()).build();
        RecommendationResponse  recommendationResponse= new RecommendationResponse();
        if(runDetails!=null) {
            recommendationResponse = recommendationService.getRecommendationResponseV2(runDetails);
        }
        return recommendationResponse;
    }


    @PostMapping("remediation/remediate-data")
    public int insertRunDetailsRecord(@RequestBody RemediateRequest remediateRequest) throws Exception {
        return remediateService.remediateData(remediateRequest);
    }

    @PostMapping("recommendation/upload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file ) throws Exception {
        String fileName = file.getOriginalFilename() ;
        try {
            byte[] bytes = file.getBytes();
              Path path = Paths.get(fileName);
              Files.write(path, bytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        excelDataService.processDBUpdates(fileName);
        return ResponseEntity.ok("File uploaded successfully.");
    }


}
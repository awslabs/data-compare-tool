/**
 * Interface for recommendation APIs.
 *
 * @author Rajeshkumar Kagathara
 * @version 1.0
 */

package com.datavalidationtool.service;

import com.datavalidationtool.model.DatabaseInfo;
import com.datavalidationtool.model.RunDetails;
import com.datavalidationtool.model.response.RecommendationResponse;
import com.datavalidationtool.model.response.RunDetailsSelectionResponse;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RecommendationService {

    public String recommendationApiTest();

    public List<String> getDbSchemaDetails(DatabaseInfo databaseInfo);

    public List<String> getSchemaTableDetails(DatabaseInfo databaseInfo, String schemaName);

    public List<RunDetails> getRunDetails(RunDetails inputRunDetails, DatabaseInfo db) throws Exception;

    public List<RunDetails> getRunDetailsWithOptional(RunDetails inputRunDetails, DatabaseInfo db) throws Exception;

    public List<Integer> getValIdFromValidationTable(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo, String ValidationTableName) throws Exception;

    public List<Map<String, Object>> getEntireValidationTable(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo, String ValidationTableName) throws Exception;


        public boolean executeDbProcedure(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo) throws Exception;

    public int insertRunDetailsRecord(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo) throws Exception;

    List<RunDetails> getHostRunDetails(String hostName, DatabaseInfo databaseInfo) throws Exception;

    List<RunDetails> getHostRunDetailsForSelection() throws Exception;

    RunDetailsSelectionResponse getRunDetailsSelectionResponse(List<RunDetails> runDetails) ;

    RecommendationResponse getRecommendationResponse(RunDetails runDetails, DatabaseInfo valTableDbInfo, String validationTableName) throws Exception;
    RecommendationResponse getRecommendationResponseV2(RunDetails runDetails) throws Exception;

}

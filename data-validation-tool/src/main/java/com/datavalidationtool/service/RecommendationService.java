/**
 * Interface for recommendation APIs.
 *
 * @author Rajeshkumar Kagathara
 * @version 1.0
 */

package com.datavalidationtool.service;

import com.datavalidationtool.model.DatabaseInfo;
import com.datavalidationtool.model.RunDetails;

import java.util.List;

public interface RecommendationService {

    public String recommendationApiTest();

    public List<String> getDbSchemaDetails(DatabaseInfo databaseInfo);

    public List<String> getSchemaTableDetails(DatabaseInfo databaseInfo, String schemaName);

    public List<RunDetails> getRunDetails(RunDetails inputRunDetails, DatabaseInfo db) throws Exception;

    public List<RunDetails> getRunDetailsWithOptional(RunDetails inputRunDetails, DatabaseInfo db) throws Exception;

    public List<Integer> getValIdFromValidationTable(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo, String ValidationTableName) throws Exception;

    public boolean executeDbProcedure(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo) throws Exception;

    public int insertRunDetailsRecord(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo) throws Exception;

    List<RunDetails> getHostRunDetails(String hostName, DatabaseInfo databaseInfo) throws Exception;

    List<RunDetails> getHostRunDetailsForSelection(DatabaseInfo databaseInfo) throws Exception;

}

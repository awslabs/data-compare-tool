/**
 * Interface for recommendation APIs.
 *
 *
 * @author      Rajeshkumar Kagathara
 * @version     1.0
 */

package com.datavalidationtool.service;

import com.datavalidationtool.model.DatabaseInfo;
import com.datavalidationtool.model.RunDetails;
import java.util.List;

public interface RecommendationService {

    public String recommendationApiTest();

    public List<String> getDbSchemaDetails(DatabaseInfo databaseInfo);

    public List<String> getSchemaTableDetails(DatabaseInfo databaseInfo,String schemaName);

    public List<RunDetails> getRunDetails(RunDetails inputRunDetails, DatabaseInfo db) throws Exception;
    public boolean executeDbProcedure(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo) throws Exception;
    public int insertRunDetailsRecord(RunDetails inputRunDetails_1, DatabaseInfo databaseInfo) throws Exception;

}

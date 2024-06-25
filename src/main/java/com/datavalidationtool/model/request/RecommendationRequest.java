package com.datavalidationtool.model.request;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RecommendationRequest {
    String schemaName;
    String tableName;
    String runId;
    String targetSchemaName;
}

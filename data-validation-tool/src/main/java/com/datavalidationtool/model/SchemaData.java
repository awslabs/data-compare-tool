package com.datavalidationtool.model;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SchemaData {
    private String sourceSchemaName;
    private String targetSchemaName;
    private String tableName;
    private String runId;
    private String dataUpdateStr;
    private String columnNames;
    private boolean missingPresent;
    private boolean mismatchPresent;
}

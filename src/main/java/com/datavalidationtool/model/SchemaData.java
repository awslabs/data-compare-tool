package com.datavalidationtool.model;

import lombok.*;

import java.util.HashMap;

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
    private String dataInsertStr;
    private String columnNames;
    private boolean missingPresent;
    private boolean mismatchPresent;
    private HashMap<String, HashMap<String,String>> tableValues;
}

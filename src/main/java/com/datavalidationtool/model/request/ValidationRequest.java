package com.datavalidationtool.model.request;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ValidationRequest {
    private String primaryKeys;
    private String sql;
    private int connectionPoolMinSize;
    private int connectionPoolMaxSize;
    private String sourceSchemaName;
    private String targetSchemaName;
    private String tableName;
    private String targetDBName;
    private String targetHost;
    private int targetPort;
    private String targetUserName;
    private String targetUserPassword;
    private boolean ignoreColumns;
    private String columns;
    private String filterType;
    private String filter;
    private boolean compareOnlyDate;
    private String connectionType;
    private boolean ignoreTables;
    private boolean checkAdditionalRows;
    private int schemaRunNumber;
}

package com.datavalidationtool.model.request;

import lombok.*;
import org.springframework.stereotype.Component;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Component
public class ValidationRequest {
    private String primaryKeys;
    private String sql;
    private int connectionPoolMinSize;
    private int connectionPoolMaxSize;
    private String sourceSchemaName;
    private String targetSchemaName;
    private String[] tableNames;
    private String tableName;
    private String targetDBName;
    private String targetHost;
    private int targetPort;
    private String targetUserName;
    private String targetUserPassword;
    private boolean ignoreColumns;
    private String columns;
    private String filterType;
    private String dataFilters;
    private boolean compareOnlyDate;
    private String connectionType;
    private boolean ignoreTables;
    private boolean checkAdditionalRows;
    private int schemaRunNumber;
    private String uniqueCols;
}

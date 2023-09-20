package com.datavalidationtool.model;

import lombok.*;

import java.sql.ResultSet;
import java.util.ArrayList;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ExcelDataRequest {
    ResultSet resultSet;
    long startIndex;
    long endIndex;
    String fileName;
    String tableName;
    String schemaName;
    String runId;
    ArrayList colList;
    boolean validationRequest;
}

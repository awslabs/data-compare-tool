package com.datavalidationtool.model.request;

import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RemediateRequest {

    private String runId;
    private String tableName;
    private String schemaName;
    private List<ColumnDetails> columnDetails;
    private String exceptionType;
}
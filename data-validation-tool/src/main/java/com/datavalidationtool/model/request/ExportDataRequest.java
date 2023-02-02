package com.datavalidationtool.model.request;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ExportDataRequest {
    String schemaName;
    String tableName;
    String runId;
}

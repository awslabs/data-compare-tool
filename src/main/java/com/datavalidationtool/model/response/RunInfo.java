package com.datavalidationtool.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RunInfo {
    long totalRecords;
    String table;
    long missingRows;
    long mismatchRows;
    String duration;
    String lastRunDate;
    String uniqueColumns;
    String chunkColumns;
    String dataFilters;
    int chunkSize;
    boolean incremental;


}

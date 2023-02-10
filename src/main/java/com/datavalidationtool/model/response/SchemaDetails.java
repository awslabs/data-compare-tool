package com.datavalidationtool.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;
import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SchemaDetails {
    String schemaName;
    Set<RunWithDate> schemaRun;
    List<TableDetails> tableList;
}

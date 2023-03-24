package com.datavalidationtool.model.response;

import lombok.*;

import java.util.List;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LastRunDetails {
    String schemaName;
    List<RunInfo> runs;
}

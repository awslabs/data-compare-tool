package com.datavalidationtool.model.response;

import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RecommendationResponse {

    int totalRecords;
    int currentPage;
    int pageSize;
    String table;
    List<String> uniqueColumns;
    List<RecommendationRow> rows;
}

package com.datavalidationtool.model.response;

import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RecommendationRow {

    int recommendationCode;
    List<RecommendationColumn> columns;
    int valId;
    String durationText;
}

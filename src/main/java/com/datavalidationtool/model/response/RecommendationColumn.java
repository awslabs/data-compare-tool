package com.datavalidationtool.model.response;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RecommendationColumn {

    String colName;
    Object srcValue;
    Object targetValue;
}

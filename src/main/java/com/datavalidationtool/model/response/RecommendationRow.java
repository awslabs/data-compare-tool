package com.datavalidationtool.model.response;

import java.util.List;

public
class RecommendationRow {

    Object recommendationCode;
    List<RecommendationColumn> columns;

    public RecommendationRow() {
    }

    public RecommendationRow(Object recommendationCode, List<RecommendationColumn> columns) {
        this.recommendationCode = recommendationCode;
        this.columns = columns;
    }

    public List<RecommendationColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<RecommendationColumn> columns) {
        this.columns = columns;
    }

    public Object getRecommendationCode() {
        return recommendationCode;
    }

    public void setRecommendationCode(Object recommendationCode) {
        this.recommendationCode = recommendationCode;
    }
}

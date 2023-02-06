package com.datavalidationtool.model.response;

public
class RecommendationRow {

    String colName;
    Object srcValue;
    Object targetValue;

    Object recommendationCode;

    public RecommendationRow() {
    }

    public RecommendationRow(String colName, Object srcValue, Object targetValue, Object recommendationCode) {
        this.colName = colName;
        this.srcValue = srcValue;
        this.targetValue = targetValue;
        this.recommendationCode=recommendationCode;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public Object getSrcValue() {
        return srcValue;
    }

    public void setSrcValue(Object srcValue) {
        this.srcValue = srcValue;
    }

    public Object getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(Object targetValue) {
        this.targetValue = targetValue;
    }

    public Object getRecommendationCode() {
        return recommendationCode;
    }

    public void setRecommendationCode(Object recommendationCode) {
        this.recommendationCode = recommendationCode;
    }
}

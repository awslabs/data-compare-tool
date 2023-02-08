package com.datavalidationtool.model.response;

public
class RecommendationColumn {

    String colName;
    Object srcValue;
    Object targetValue;

    //Object recommendationCode;

    public RecommendationColumn() {
    }

    public RecommendationColumn(String colName, Object srcValue, Object targetValue) {
        this.colName = colName;
        this.srcValue = srcValue;
        this.targetValue = targetValue;
        //this.recommendationCode=recommendationCode;
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


}

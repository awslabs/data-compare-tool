package com.datavalidationtool.model.response;

import java.util.List;

public class RecommendationResponse {

    int totalRecords;
    int currentPage;
    int pageSize;
    String table;
    List<String> uniqueColumns;
    List<RecommendationRow> rows;

    public RecommendationResponse() {
    }

    public RecommendationResponse(int totalRecords, int currentPage, int pageSize, String table, List<String> uniqueColumns, List<RecommendationRow> rows) {
        this.totalRecords = totalRecords;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.table = table;
        this.uniqueColumns = uniqueColumns;
        this.rows = rows;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public List<String> getUniqueColumns() {
        return uniqueColumns;
    }

    public void setUniqueColumns(List<String> uniqueColumns) {
        this.uniqueColumns = uniqueColumns;
    }

    public List<RecommendationRow> getRows() {
        return rows;
    }

    public void setRows(List<RecommendationRow> rows) {
        this.rows = rows;
    }
}

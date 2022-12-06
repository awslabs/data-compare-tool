package com.datavalidationtool.model.response;

import java.util.List;

public class TableDetails {

    String tableName;

    List<Integer> tableRun;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Integer> getTableRun() {
        return tableRun;
    }

    public void setTableRun(List<Integer> tableRun) {
        this.tableRun = tableRun;
    }

    public TableDetails(String tableName, List<Integer> tableRun) {
        this.tableName = tableName;
        this.tableRun = tableRun;
    }

    public TableDetails() {
    }
}

package com.datavalidationtool.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TableDetails {

    String tableName;
    Set<RunWithDate> tableRun;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Set<RunWithDate> getTableRun() {
        return tableRun;
    }

    public void setTableRun(Set<RunWithDate> tableRun) {
        this.tableRun = tableRun;
    }

    public TableDetails() {
    }
}

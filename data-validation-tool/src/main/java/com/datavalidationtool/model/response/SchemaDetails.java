package com.datavalidationtool.model.response;

import java.util.List;

public class SchemaDetails {

    String schemaName;

    List<Integer> schemaRun;

    List<TableDetails> tableList;


    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public List<Integer> getSchemaRun() {
        return schemaRun;
    }

    public void setSchemaRun(List<Integer> schemaRun) {
        this.schemaRun = schemaRun;
    }

    public List<TableDetails> getTableList() {
        return tableList;
    }

    public void setTableList(List<TableDetails> tableList) {
        this.tableList = tableList;
    }

    public SchemaDetails(String schemaName, List<Integer> schemaRun, List<TableDetails> tableList) {
        this.schemaName = schemaName;
        this.schemaRun = schemaRun;
        this.tableList = tableList;
    }

    public SchemaDetails() {
    }
}

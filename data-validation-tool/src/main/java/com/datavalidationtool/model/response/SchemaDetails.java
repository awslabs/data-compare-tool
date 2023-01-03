package com.datavalidationtool.model.response;

import java.util.List;
import java.util.Set;

public class SchemaDetails {

    String schemaName;
    Set<RunWithDate> schemaRun;

    List<TableDetails> tableList;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public List<TableDetails> getTableList() {
        return tableList;
    }

    public void setTableList(List<TableDetails> tableList) {
        this.tableList = tableList;
    }

    public Set<RunWithDate> getSchemaRun() {
        return schemaRun;
    }

    public void setSchemaRun(Set<RunWithDate> schemaRun) {
        this.schemaRun = schemaRun;
    }

    public SchemaDetails() {
    }
}

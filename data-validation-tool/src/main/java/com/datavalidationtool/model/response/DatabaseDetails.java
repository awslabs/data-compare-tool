package com.datavalidationtool.model.response;

import java.util.List;

public class DatabaseDetails {

    String databaseName;

    List<SchemaDetails> schemaList;

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public List<SchemaDetails> getSchemaList() {
        return schemaList;
    }

    public void setSchemaList(List<SchemaDetails> schemaList) {
        this.schemaList = schemaList;
    }

    public DatabaseDetails(String databaseName, List<SchemaDetails> schemaList) {
        this.databaseName = databaseName;
        this.schemaList = schemaList;
    }

    public DatabaseDetails() {
    }
}

package com.datavalidationtool.model;

import java.sql.Date;

public class RunDetails {

    private String sourceHostName;
    private String targetHostName;
    private String databaseName;
    private String schemaName;
    private String tableName;
    private int run;
    private Date executionDate;

    public RunDetails() {
    }

    public RunDetails(String sourceHostName, String targetHostName, String databaseName, String schemaName) {
        this.sourceHostName = sourceHostName;
        this.targetHostName = targetHostName;
        this.databaseName = databaseName;
        this.schemaName = schemaName;
    }

    public RunDetails(String sourceHostName, String targetHostName, String databaseName, String schemaName, String tableName) {
        this.sourceHostName = sourceHostName;
        this.targetHostName = targetHostName;
        this.databaseName = databaseName;
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    public RunDetails(String sourceHostName, String targetHostName, String databaseName, String schemaName, String tableName, int run) {
        this.sourceHostName = sourceHostName;
        this.targetHostName = targetHostName;
        this.databaseName = databaseName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.run = run;
    }

    @Override
    public String toString() {
        return "RunDetails{" +
                "sourceHostName='" + sourceHostName + '\'' +
                ", targetHostName='" + targetHostName + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", schemaName='" + schemaName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", run=" + run +
                ", executionDate=" + executionDate +
                '}';
    }

    public String getSourceHostName() {
        return sourceHostName;
    }

    public void setSourceHostName(String sourceHostName) {
        this.sourceHostName = sourceHostName;
    }

    public String getTargetHostName() {
        return targetHostName;
    }

    public void setTargetHostName(String targetHostName) {
        this.targetHostName = targetHostName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getRun() {
        return run;
    }

    public void setRun(int run) {
        this.run = run;
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(Date executionDate) {
        this.executionDate = executionDate;
    }
}

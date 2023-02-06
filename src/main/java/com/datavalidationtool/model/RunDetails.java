package com.datavalidationtool.model;


import java.util.Date;

public class RunDetails {

    private String sourceHostName;
    private String targetHostName;
    private String databaseName;
    private String schemaName;
    private String tableName;
    private int schemaRun;

    private int tableRun;
    private String runId;
    private String executionDate;

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

    public RunDetails(String sourceHostName, String targetHostName, String databaseName, String schemaName, String tableName, int schemaRun, int tableRun) {
        this.sourceHostName = sourceHostName;
        this.targetHostName = targetHostName;
        this.databaseName = databaseName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.schemaRun = schemaRun;
        this.tableRun=tableRun;
    }
    public RunDetails(String sourceHostName, String targetHostName, String databaseName, String schemaName, String tableName, int schemaRun) {
        this.sourceHostName = sourceHostName;
        this.targetHostName = targetHostName;
        this.databaseName = databaseName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.tableRun=tableRun;
    }

    @Override
    public String toString() {
        return "RunDetails{" +
                "sourceHostName='" + sourceHostName + '\'' +
                ", targetHostName='" + targetHostName + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", schemaName='" + schemaName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", schemaRun=" + schemaRun +
                ", tableRun=" + tableRun +
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

    public int getSchemaRun() {
        return schemaRun;
    }

    public void setSchemaRun(int schemaRun) {
        this.schemaRun = schemaRun;
    }

    public int getTableRun() {
        return tableRun;
    }

    public void setTableRun(int tableRun) {
        this.tableRun = tableRun;
    }

    public String getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(String executionDate) {
        this.executionDate = executionDate;
    }

    public String getRunId() {
        return runId;
    }
    public void setRunId(String runId) {
        this.runId = runId;
    }
}

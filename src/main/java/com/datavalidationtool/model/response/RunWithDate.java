package com.datavalidationtool.model.response;

import java.util.Date;
import java.util.Objects;

public class RunWithDate implements Comparable<Object>{
    int run;
    Date executionDate;

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    String runId;
    String schemaName;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    String tableName;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunWithDate that = (RunWithDate) o;
        return run == that.run && Objects.equals(executionDate, that.executionDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(run, executionDate);
    }

    @Override
    public int compareTo(Object o) {

        RunWithDate that = (RunWithDate) o;
        if(run == that.run){
            return 0;
        }else if(run <that.run){
            return 1;
        }
        else{
            return -1;
        }
    }
}

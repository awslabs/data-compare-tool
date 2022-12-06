package com.datavalidationtool.model.response;

import java.util.List;

public class HostDetails {

    String hostName;
    List<DatabaseDetails> databaseList;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public List<DatabaseDetails> getDatabaseList() {
        return databaseList;
    }

    public void setDatabaseList(List<DatabaseDetails> databaseList) {
        this.databaseList = databaseList;
    }

    public HostDetails(String hostName, List<DatabaseDetails> databaseList) {
        this.hostName = hostName;
        this.databaseList = databaseList;
    }

    public HostDetails() {
    }
}

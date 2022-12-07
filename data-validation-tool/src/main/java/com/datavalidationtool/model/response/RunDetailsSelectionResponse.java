package com.datavalidationtool.model.response;

import java.util.List;

public class RunDetailsSelectionResponse {

    List<HostDetails> hostDetailsList;

    public List<HostDetails> getHostDetailsList() {
        return hostDetailsList;
    }

    public void setHostDetailsList(List<HostDetails> hostDetailsList) {
        this.hostDetailsList = hostDetailsList;
    }

    public RunDetailsSelectionResponse(List<HostDetails> hostDetailsList) {
        this.hostDetailsList = hostDetailsList;
    }

    public RunDetailsSelectionResponse() {
    }
}
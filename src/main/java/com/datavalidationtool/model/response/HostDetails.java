package com.datavalidationtool.model.response;

import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class HostDetails {

    String hostName;
    List<DatabaseDetails> databaseList;
}

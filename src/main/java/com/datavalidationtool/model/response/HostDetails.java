package com.datavalidationtool.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HostDetails {

    String hostName;
    Integer port;
    String username;
    String password;
    List<DatabaseDetails> databaseList;
}

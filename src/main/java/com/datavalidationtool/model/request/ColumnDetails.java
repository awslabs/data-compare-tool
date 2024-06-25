package com.datavalidationtool.model.request;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ColumnDetails {
    private int valId;
    private String column;
    private String value;
    private String exceptionType;
}

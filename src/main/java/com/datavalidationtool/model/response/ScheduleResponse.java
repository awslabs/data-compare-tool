package com.datavalidationtool.model.response;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ScheduleResponse {
    private int scheduleId;
    private int count;
}

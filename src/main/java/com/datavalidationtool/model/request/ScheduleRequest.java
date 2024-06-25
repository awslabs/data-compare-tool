package com.datavalidationtool.model.request;

import lombok.*;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Component
public class ScheduleRequest extends ValidationRequest {
    private Timestamp scheduleTime;
    private int duration;
    private long scheduleId;
    private String status;
    private Timestamp endDate;
    private boolean reoccurrence;
    private String dayFrequency ;
    private Timestamp scheduleEndDate;
    private int timeOccurrence;
    private String timeFrequency;
    private int numOccurrence ;
}

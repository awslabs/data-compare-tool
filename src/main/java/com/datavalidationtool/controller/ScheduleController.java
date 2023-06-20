package com.datavalidationtool.controller;

import com.datavalidationtool.model.request.ScheduleRequest;
import com.datavalidationtool.model.request.ScheduleRequest;
import com.datavalidationtool.model.request.ValidationRequest;
import com.datavalidationtool.model.response.LastRunDetails;
import com.datavalidationtool.model.response.RunInfo;
import com.datavalidationtool.model.response.ScheduleResponse;
import com.datavalidationtool.service.ExcelDataService;
import com.datavalidationtool.service.FetchValidationDetailsService;
import com.datavalidationtool.service.SchedulerService;
import com.datavalidationtool.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/dvt")
public class ScheduleController {
    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private ExcelDataService excelDataService;

    @Autowired
    private ValidationService validationService;

    @PostMapping(value = "schedule/addSchedule")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public Object addRunSchedules(@RequestBody ScheduleRequest scheduleRequest) throws Exception {
        return schedulerService.addRunSchedules(scheduleRequest);
    }
    @PostMapping(value = "schedule/getSchedule")
    public Object getRunInfoDetails(@RequestBody ScheduleRequest scheduleRequest) throws Exception {
        return schedulerService.getScheduleInfo();
    }

    @PostMapping(value = "schedule/runScheduleJob")
    public Object runScheduleJob(@RequestBody ScheduleRequest scheduleRequest) throws Exception {
        return schedulerService.getScheduleInfo();
    }

    @GetMapping(value = "validation/getScheduleJobRuns")
    public LastRunDetails getScheduleJobRuns(@RequestBody ScheduleRequest scheduleRequest) throws Exception {
        return schedulerService.getScheduleJobRuns();
    }

}

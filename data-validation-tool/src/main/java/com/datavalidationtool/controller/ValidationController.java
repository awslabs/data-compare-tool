package com.datavalidationtool.controller;

import com.datavalidationtool.service.FetchValidationDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ValidationController {

    @Autowired
    private FetchValidationDetailsService service;

    @GetMapping(value = "/validateDetails")
    public ResponseEntity getValidationScreenDetails() throws Exception {
        var s = service.getValidationDetails();
        return new ResponseEntity(s, HttpStatus.ACCEPTED);
    }
}



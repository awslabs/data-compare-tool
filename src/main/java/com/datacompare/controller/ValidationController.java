package com.datacompare.controller;

import com.datacompare.service.FetchValidationDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ValidationController {

    @Autowired
    private FetchValidationDetailsService service;

    @GetMapping(value = "/validateDetails")
    public ResponseEntity getValidationScreenDetails(){
        service.fetchValidationDetails();
        return null;
    }
}



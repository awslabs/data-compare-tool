package com.datavalidationtool.controller;

import com.datavalidationtool.model.request.ExportDataRequest;
import com.datavalidationtool.model.request.ValidationRequest;
import com.datavalidationtool.service.ExcelDataService;
import com.datavalidationtool.service.FetchValidationDetailsService;
import com.datavalidationtool.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/dvt")
public class ValidationController {
    private Boolean toolRunning = Boolean.FALSE;
    public static String reportFileName = "XXX";
    public static String reportOutputFolder = null;
    @Autowired
    private FetchValidationDetailsService service;

    @Autowired
    private  ExcelDataService excelDataService;

    @Autowired
    private ValidationService validationService;

    @GetMapping(value = "validation/dbDetails")
    public ResponseEntity getValidationScreenDetails() throws Exception {
        var s = service.getValidationDetails();
        return new ResponseEntity(s, HttpStatus.ACCEPTED);
    }
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("validation/compareData")
    public String compareData(@RequestBody ValidationRequest inputRunDetails) throws Exception {
        String runId=validationService.validateData(inputRunDetails);
        toolRunning = Boolean.FALSE;
        return runId;
    }
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping("validation/exportData")
    public void exportData(@RequestParam String runId, @RequestParam String tableName, @RequestParam String schemaName, HttpServletResponse response) throws Exception {
       // ExcelDataService excelDataService = new ExcelDataService();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        ExportDataRequest exportDataRequest=ExportDataRequest.builder().runId(runId).schemaName(schemaName).tableName(tableName).build();
       // boolean fileCreated=
          excelDataService.createExcel(exportDataRequest,response);
        toolRunning = Boolean.FALSE;
        //if(fileCreated){
          //  return "Success";
     //   }
       // return  "failure";
    }
    @GetMapping("/")
    public String index(Model model) {
        if(toolRunning.booleanValue()) {
            model.addAttribute("msg", "Data Compare is in progress.");
            model.addAttribute("fileName", reportFileName);
            return "result";
        } else {
            reportFileName = "XXX";
            model.addAttribute("datacompare", null);
            return "datacompare";
        }
    }
}



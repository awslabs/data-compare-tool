package com.datavalidationtool.service;

import com.datavalidationtool.model.RunDetails;
import com.datavalidationtool.model.SchemaData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RemediateService {

    @Autowired
    private ExcelDataService excelDataService;


    public int remediateData(RunDetails inputRunDetails_1) {
        String strUpdateValue = "";
        String strInsertValue = "";
        int rowsUpdated=0;
        SchemaData details = SchemaData.builder().build();
        if (inputRunDetails_1.getMismatchType().startsWith("MISSING")) {
            details.setMissingPresent(true);
            details.setDataInsertStr(inputRunDetails_1.getValId());
        }
        if (inputRunDetails_1.getMismatchType().startsWith("MISMATCH")) {
            details.setMismatchPresent(true);
            details.setDataUpdateStr(inputRunDetails_1.getValId()+","+inputRunDetails_1);
        }
        details.setTableName(inputRunDetails_1.getTableName());
        details.setRunId(inputRunDetails_1.getRunId());
        details.setRunId(inputRunDetails_1.getRunId());
        details.setSourceSchemaName(inputRunDetails_1.getSourceHostName());
        details.setTargetSchemaName(inputRunDetails_1.getTargetHostName());
        if (details.isMismatchPresent()) {
            rowsUpdated=  excelDataService.executeDBCall(details);
        }
        if (details.isMissingPresent()) {
            rowsUpdated= excelDataService.executeDBInsertCall(details);
        }
        return rowsUpdated;
    }
}


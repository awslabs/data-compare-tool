package com.datavalidationtool.service;

import com.datavalidationtool.model.DatabaseInfo;
import com.datavalidationtool.model.RunDetails;
import com.datavalidationtool.model.SchemaData;
import com.datavalidationtool.model.request.ColumnDetails;
import com.datavalidationtool.model.request.RemediateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RemediateService {

    @Autowired
    private ExcelDataService excelDataService;

    public int remediateData(RemediateRequest remediateRequest) throws SQLException {
        String strUpdateValue = "";
        String strInsertValue = "";
        int rowsUpdated = 0;
        SchemaData details = buildUpdatRequest(remediateRequest);
        details.setSourceSchemaName(remediateRequest.getSchemaName());
        //TODO: need to remove this
       // details.setTargetSchemaName("ops$brdb");
        if (details.isMismatchPresent()) {
            rowsUpdated = excelDataService.executeDBCall(details);
        }
        if (details.isMissingPresent()) {
            rowsUpdated = excelDataService.executeDBInsertCall(details);
        }
        return rowsUpdated;
    }

    private SchemaData buildUpdatRequest(RemediateRequest remediateRequest) {

        int index = 0;
        boolean newRow = true;
        int valId = 0;
        int rowChangeValid = 0;
        String strUpdateValue = "";
        String strInsertValue = "";
        SchemaData details = SchemaData.builder().build();
        details.setTableName(remediateRequest.getTableName());
        details.setRunId(remediateRequest.getRunId());
        details.setSourceSchemaName(remediateRequest.getSchemaName());
        details.setTargetSchemaName(remediateRequest.getSchemaName());
        for (ColumnDetails remediateReq : remediateRequest.getColumnDetails()) {
            int dataRowNumber = 0;

            //324,COL1,COL2;325,COL1,COL2,COL3;326,COL1,COL2,COL3,COL4
            if (newRow)
                valId = remediateReq.getValId();

            //_remediate_missing_exceptions(‘ops$ora’,‘crtdms’,‘ppt_100’,‘ppt_100’,‘433;434;435’)
            //{\"column\":\"id\",\"value\":\"\",\"valId\":377},{\"column\":\"transaction\",\"value\":\"\",\"valId\":377}],[{\"column\":\"country\",\"value\":\"\",\"valId\":377},{\"column\":\"exception_status\",\"value\":\"\",\"valId\":377},{\"column\":\"id\",\"value\":\"\",\"valId\":377},{\"column\":\"transaction\",\"value\":\"\",\"valId\":377}]]"}
            if (remediateReq.getExceptionType().toUpperCase().startsWith("MISSING")) {
                if (rowChangeValid != valId) {
                    if (!strInsertValue.equals("")) {
                        strInsertValue = strInsertValue +";"+ remediateReq.getValId();
                    } else {
                        strInsertValue = strInsertValue + remediateReq.getValId() ;
                    }
                    rowChangeValid = valId;
                }
                details.setMissingPresent(true);
           }
            //fn_remediate_mismatch_exceptions_dvt2(‘a888c0794d9aba2991ecf5d0830a26af’,’440,id,transaction,country;439,id,transaction,country;438,id,transaction,country;437,id,transaction,country;’,’ops$ora’,‘crtdms’,‘ppt_100’,‘ppt_100’)
          else if (remediateReq.getExceptionType().toUpperCase().startsWith("MISMATCH")) {
                if (rowChangeValid != valId) {
                    // add semicolon
                    if (index != 0 && index < (remediateRequest.getColumnDetails().size()-1)) {
                        strUpdateValue =  strUpdateValue+";";
                        strUpdateValue =  strUpdateValue.replace(",;", ";");
                    }
                    strUpdateValue = strUpdateValue + remediateReq.getValId() + "," + remediateReq.getColumn();
                    // add val id and column name
                    if (index < (remediateRequest.getColumnDetails().size()-1)) {
                        strUpdateValue = strUpdateValue + ",";
                    }
                } else {
                    if (index == (remediateRequest.getColumnDetails().size()-1)) {
                        strUpdateValue = strUpdateValue + remediateReq.getColumn();
                    } else
                        strUpdateValue = strUpdateValue + remediateReq.getColumn() + ",";
                }
                rowChangeValid = valId;
                details.setMismatchPresent(true);
            }

            index++;
        }
        //to avoid any , or ; at the end
        if(strUpdateValue!=null && !strUpdateValue.equals("") &&(strUpdateValue.lastIndexOf(',')==(strUpdateValue.length()-1) || strUpdateValue.lastIndexOf(';')==(strUpdateValue.length()-1)))
            strUpdateValue= strUpdateValue.substring(0,strUpdateValue.length()-1);
        details.setDataUpdateStr(strUpdateValue);
        details.setDataInsertStr(strInsertValue);
    return details;
    }

}


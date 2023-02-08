package com.datavalidationtool.service;

import com.datavalidationtool.ds.DataSource;
import com.datavalidationtool.model.ExcelDataRequest;
import com.datavalidationtool.model.SchemaData;
import com.datavalidationtool.model.request.ExportDataRequest;
import com.datavalidationtool.model.request.ValidationRequest;
import com.datavalidationtool.model.response.HostDetails;
import com.datavalidationtool.model.response.SchemaDetails;
import com.datavalidationtool.util.JdbcUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.postgresql.core.ResultCursor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.ArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExcelDataService {
    private static final long MAX_ROWS_IN_SHEET = 100000;
    @Autowired
    private FetchValidationDetailsService service;
    public String processDBUpdates(String filePath) throws Exception {
        SchemaData dbUpdateData = readExcel(filePath);
        if (!DataSource.getInstance().isPoolInitialized()){
            JdbcUtil.setConnections(ValidationRequest.builder().targetHost("ukpg-instance-1.cl7uqmhlcmfi.eu-west-2.rds.amazonaws.com").targetPort(5432).targetDBName("ttp").targetUserName("postgres").targetUserPassword("postgres").connectionPoolMaxSize(3).connectionPoolMinSize(1).build());
        }
        if (DataSource.getInstance().isPoolInitialized()) {
            if(dbUpdateData.isMismatchPresent()) {
                executeDBCall(dbUpdateData);
            }
            if(dbUpdateData.isMissingPresent()) {
                executeDBInsertCall(dbUpdateData);
            }
        }
        return "Success";    }
    private void executeDBCall(SchemaData dbUpdateData) {
        ResultSet rs = null;
        Connection con=null;
        try {
            con =  DataSource.getInstance().getTargetDBConnection() ;
            String dbFunction = "{ call fn_remediate_mismatch_exceptions_dvt2(?,?,?,?,?,?) }";
            CallableStatement cst = null ;
            try {
                cst = con.prepareCall(dbFunction);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            cst.setString(1, dbUpdateData.getRunId());
            cst.setString(2, dbUpdateData.getDataUpdateStr());
            cst.setString(3, dbUpdateData.getSourceSchemaName());
            cst.setString(4, dbUpdateData.getTargetSchemaName());
            cst.setString(5, dbUpdateData.getTableName().replace("_val",""));
            cst.setString(6, dbUpdateData.getTableName().replace("_val",""));
            rs= cst.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JdbcUtil jdbcUtil = new JdbcUtil();
            JdbcUtil.closeResultSet(rs);;
        }

    }
    private void executeDBInsertCall(SchemaData dbUpdateData) {

        ResultSet rs = null;
        Connection con=null;
        try {
            con =  DataSource.getInstance().getTargetDBConnection() ;
            String dbFunction = "{ call fn_remediate_missing_exceptions(?,?,?,?,?) }";
            CallableStatement cst = null ;
            try {
                cst = con.prepareCall(dbFunction);
            } catch (SQLException e) {
                e.printStackTrace();
            }

                cst.setString(1, dbUpdateData.getSourceSchemaName());
                cst.setString(2, dbUpdateData.getTargetSchemaName());
                cst.setString(3, dbUpdateData.getTableName().replace("_val",""));
                cst.setString(4, dbUpdateData.getTableName().replace("_val",""));
                cst.setString(5, dbUpdateData.getDataInsertStr());
                rs= cst.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JdbcUtil jdbcUtil = new JdbcUtil();
            JdbcUtil.closeResultSet(rs);;
        }

    }

    public SchemaData readExcel(String filePath) throws IOException {
        File file = new File(filePath);
        FileInputStream inputStream = null;
        SchemaData details = SchemaData.builder().build();
        try {
            inputStream = new FileInputStream(file);
            Workbook recDataExcel = new XSSFWorkbook(inputStream);
            StringBuilder updatedData=new StringBuilder();
            int i = 0;

            for (Sheet sheet : recDataExcel) {

                int firstRow = sheet.getFirstRowNum();
                int lastRow = sheet.getLastRowNum();
                if (i == 0) {
                    for (int index = firstRow ; index <= lastRow; index++) {
                        Row row = sheet.getRow(index);
                        int cellIndex = 0;
                        for (Cell cell : row) {
                            if(cellIndex==1) {
                                String strValue = cell.getStringCellValue();
                                if (index == 0)
                                    details.setSourceSchemaName(strValue);
                                else if (index == 1)
                                    details.setTargetSchemaName(strValue);
                                else if (index == 2)
                                    details.setTableName(strValue);
                                else if (index == 3)
                                    details.setRunId(strValue);
                                else if (index == 4)
                                    details.setColumnNames(strValue);

                            }
                            cellIndex++;
                        }
                    }
                } else if(i==2){
                    int dataRowNumber=0;
                    String strUpdateValue="";
                    String strInsertValue="";
                    //324,COL1,COL2;325,COL1,COL2,COL3;326,COL1,COL2,COL3,COL4
                    for (int index = firstRow+2; index <= lastRow; index++) {
                        Row row = sheet.getRow(index);
                        int cellIndex=0;
                        String cellValue0="";
                            for (Cell cell : row) {
                                if(cellIndex==0) {
                                     cellValue0 = cell.getStringCellValue();
                                    }
                                if(cellIndex==1) {
                                    String cellValue = cell.getStringCellValue();
                                    if(cellValue.startsWith("MISSING")) {
                                        strInsertValue =strInsertValue+cellValue0 ;
                                        if(index<lastRow) {
                                            strInsertValue = strInsertValue + ";";
                                        }
                                        details.setMissingPresent(true);
                                    }
                                    else if(cellValue.startsWith("MISMATCH")) {
                                        strUpdateValue =strUpdateValue+cellValue0 + ","+details.getColumnNames();
                                        if(index<lastRow) {
                                            strUpdateValue = strUpdateValue + ";";
                                        }
                                        details.setMismatchPresent(true);
                                    }
                                    break;
                                }
                                cellIndex++;
                                }

                            }
                    details.setDataUpdateStr(strUpdateValue);
                    details.setDataInsertStr(strInsertValue);
                        }
                   i++;
                    }
            inputStream.close();
            recDataExcel.close();
        } catch (IOException e) {
            throw e;
        }
        return details;
    }

    public void createExcel(ExportDataRequest exportDataRequest) throws Exception {

        if (!DataSource.getInstance().isPoolInitialized()){
            JdbcUtil.setConnections(ValidationRequest.builder().targetHost("ukpg-instance-1.cl7uqmhlcmfi.eu-west-2.rds.amazonaws.com").targetPort(5432).targetDBName("ttp").targetUserName("postgres").targetUserPassword("postgres").connectionPoolMaxSize(3).connectionPoolMinSize(1).build());
        }
        if (DataSource.getInstance().isPoolInitialized()) {
            ExcelDataRequest excelDataRequest =ExcelDataRequest.builder().runId(exportDataRequest.getRunId()).schemaName(exportDataRequest.getSchemaName()).tableName(exportDataRequest.getTableName()).build();
            ResultSet resultSet = getResultSet(excelDataRequest);
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            String tableName = resultSetMetaData.getTableName(1);
            String schemaName = excelDataRequest.getSchemaName();
            int noOfColumns = resultSetMetaData.getColumnCount();
            int rowCount = 0;
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet0 = workbook.createSheet("Info");
            workbook.setSheetVisibility(0, SheetVisibility.HIDDEN);
            Row firstRow = sheet0.createRow(0);
            Row secoundRow = sheet0.createRow(1);
            Row thirdRow = sheet0.createRow(2);
            Row fourtRow = sheet0.createRow(3);
            Row fifthRow = sheet0.createRow(4);
            Cell fRCell0 = firstRow.createCell(0);
            fRCell0.setCellValue("Schema_Name");
            Cell fRCell1 = firstRow.createCell(1);
            fRCell1.setCellValue(schemaName);

            Cell sRCell0 = secoundRow.createCell(0);
            sRCell0.setCellValue("Target_Schema_Name");
            Cell sRCell1 = secoundRow.createCell(1);
            sRCell1.setCellValue(exportDataRequest.getTargetSchemaName());

            Cell tRCell0 = thirdRow.createCell(0);
            tRCell0.setCellValue("Table_Name");
            Cell tRCell1 = thirdRow.createCell(1);
            tRCell1.setCellValue(tableName);

            Cell fRtCell0 = fourtRow.createCell(0);
            fRtCell0.setCellValue("Run_Id");
            Cell fRtCell1 = fourtRow.createCell(1);
            fRtCell1.setCellValue(exportDataRequest.getRunId());

            Cell ffCell0 = fifthRow.createCell(0);
            ffCell0.setCellValue("Columns");
            Cell ffCell1 = fifthRow.createCell(1);
            ffCell1.setCellValue(getColumnNames(resultSetMetaData,noOfColumns));
            createExcelSheet(workbook,rowCount,noOfColumns,excelDataRequest,resultSetMetaData,"Mismatch Data",1);
            excelDataRequest.getResultSet().beforeFirst();
            createExcelSheet(workbook,rowCount,noOfColumns,excelDataRequest,resultSetMetaData,"Recommendation Data",2);
            //Current record details.
            try (POIFSFileSystem fs = new POIFSFileSystem()) {
                EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
                Encryptor encryptor = info.getEncryptor();
                encryptor.confirmPassword("Password1@");
                // Write out the encrypted version
                try (FileOutputStream outputStream = new FileOutputStream("reports/"+tableName + ".xlsx")) {
                //try (FileOutputStream outputStream = new FileOutputStream("/Users/amsudan/Desktop/Projects/DataValidation/awslab/"+tableName + ".xlsx")) {
                    workbook.write(outputStream);
                    outputStream.flush();
                    outputStream.close();
                    try (OPCPackage opc = OPCPackage.open("reports/"+tableName + ".xlsx", PackageAccess.READ_WRITE);
                  //  try (OPCPackage opc = OPCPackage.open("/Users/amsudan/Desktop/Projects/DataValidation/awslab/"+tableName + ".xlsx", PackageAccess.READ_WRITE);
                         OutputStream os = encryptor.getDataStream(fs)) {
                        opc.save(os);
                    } catch (IOException | InvalidFormatException e) {
                        e.printStackTrace();
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private String getColumnNames(ResultSetMetaData resultSetMetaData,int noOfColumns) throws SQLException {
       StringBuffer colNames=new StringBuffer();
        for (int i = 4; i < noOfColumns; i++) {
            colNames= colNames.append(resultSetMetaData.getColumnName(i));
            if(i < (noOfColumns-1))
              colNames.append(",") ;
        }
        return colNames.toString();
    }

    private ResultSet getResultSet(ExcelDataRequest excelDataRequest) {
        ResultSet rs=null;
        try {
            Connection con= DataSource.getInstance().getTargetDBConnection();
            String pk =null;
            StringBuilder stb=new StringBuilder();
            boolean firstCol=true;
            DatabaseMetaData meta = con.getMetaData();
            rs = meta.getPrimaryKeys(null, excelDataRequest.getSchemaName(), excelDataRequest.getTableName());
            ResultSet rs1=meta.getColumns(null,excelDataRequest.getSchemaName(),excelDataRequest.getTableName(),null);
            java.util.ArrayList list = new java.util.ArrayList<String>();
            while (rs.next()) {
                //pk = rs.getString("COLUMN_NAME");
                pk = rs.getString(4);
                System.out.println("getPrimaryKeys(): columnName=" + pk);
            }
            while (rs1.next()) {
                String col = rs1.getString("COLUMN_NAME");
                if(!firstCol)
                    stb.append(",");
                stb.append(col);
                firstCol=false;
                list.add(col);
            }
            excelDataRequest.setColList(list);
            String preparedQuery="SELECT SRC.*, DENSE_RANK () OVER ( ORDER BY SRC.id  ASC) EXCEPTION_RANK FROM \n" +
                "(SELECT RUN_ID, VAL_ID, UPPER(VAL_TYPE) AS EXCEPTION_STATUS,"+stb.toString()+" FROM "+excelDataRequest.getSchemaName()+"."+excelDataRequest.getTableName()+"_val \n" +
                "WHERE RUN_ID = ? \n" +
                "AND UPPER(VAL_TYPE) IN ('MISMATCH_SRC','MISMATCH_TRG','MISSING','EXTRA_RECORD') \n" +
                ") SRC ORDER BY EXCEPTION_RANK ASC,VAL_ID ASC;\n";
            System.out.println("preparedQuery=" + preparedQuery);
            PreparedStatement pst = null ;
            try {
                pst = con.prepareStatement(preparedQuery,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            {
                pst.setString(1, excelDataRequest.getRunId());
            }
            rs= pst.executeQuery();
            excelDataRequest.setResultSet(rs);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return rs;
    }
    public void createExcelSheet(XSSFWorkbook workbook,int rowCount,int noOfColumns,ExcelDataRequest excelDataRequest,ResultSetMetaData resultSetMetaData,String sheetName,int sheetNum) throws SQLException {

        XSSFSheet sheet = workbook.createSheet(sheetName);
        Row topHeader = sheet.createRow(++rowCount);

        Row header = sheet.createRow(++rowCount);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 14);
        font.setBold(true);
        headerStyle.setFont(font);

        CellStyle mismatchStyle = workbook.createCellStyle();
        mismatchStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        mismatchStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont afont = ((XSSFWorkbook) workbook).createFont();
        afont.setFontName("Arial");
        afont.setFontHeightInPoints((short) 10);
        afont.setBold(true);
        //afont.setColor(XSSFFont.COLOR_RED);
        mismatchStyle.setFont(afont);
        ArrayList<String> colList = excelDataRequest.getColList();

        Cell headerCell0 = topHeader.createCell(1);
        String colName = "Source Data";
        headerCell0.setCellValue(colName);
        headerCell0.setCellStyle(headerStyle);

        Cell headerCell1 = topHeader.createCell(noOfColumns - 2);
        colName = "Target Data";
        headerCell1.setCellValue(colName);
        headerCell1.setCellStyle(headerStyle);

        // sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));
        // sheet.addMergedRegion(new CellRangeAddress(1, 1, 5, 9));
        // exclude first col run id and last col timestamp
        for (int i = 2; i < noOfColumns; i++) {
            Cell headerCell = header.createCell(i - 2);
            colName = resultSetMetaData.getColumnName(i);
            headerCell.setCellValue(colName);
            headerCell.setCellStyle(headerStyle);
        }
        // First 2 columns are valId and run Id
        for (int i = 4; i < noOfColumns; i++) {
            Cell headerCell = header.createCell(noOfColumns - 6 + i);
            colName = resultSetMetaData.getColumnName(i);
            headerCell.setCellValue(colName);
            headerCell.setCellStyle(headerStyle);
        }
        while (excelDataRequest.getResultSet().next() && rowCount < MAX_ROWS_IN_SHEET) {
            Row row = sheet.createRow(++rowCount);
            String valType = null;
            int rank = 1;
            //Source data
            for (int i = 2; i < noOfColumns; i++) {
                Object field = excelDataRequest.getResultSet().getString(i);
                Cell cell = row.createCell(i - 2);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                } else if (field instanceof Long) {
                    cell.setCellValue((Long) field);
                } else if (field instanceof Boolean) {
                    cell.setCellValue((Boolean) field);
                } else if (field instanceof Character) {
                    cell.setCellValue((Character) field);
                } else if (field instanceof BigInteger) {
                    cell.setCellValue(String.valueOf((BigInteger) field));
                } else
                    cell.setCellValue((String) field);
                if (i == 3)
                    valType = (String) field;
            }
            //Target data
            if (valType != null && valType.startsWith("MISMATCH_")) {
                if (sheetNum == 1) {
                    excelDataRequest.getResultSet().next();
                }
                for (int i = 4; i < noOfColumns; i++) {
                    Object field = excelDataRequest.getResultSet().getString(i);
                    Cell cell = row.createCell(noOfColumns - 6 + i);
                    cell.setCellStyle(mismatchStyle);
                    if (field instanceof String) {
                        cell.setCellValue((String) field);
                    } else if (field instanceof Integer) {
                        cell.setCellValue((Integer) field);
                    } else if (field instanceof Long) {
                        cell.setCellValue((Long) field);
                    } else if (field instanceof Boolean) {
                        cell.setCellValue((Boolean) field);
                    } else if (field instanceof Character) {
                        cell.setCellValue((Character) field);
                    } else if (field instanceof BigInteger) {
                        cell.setCellValue(String.valueOf((BigInteger) field));
                    } else
                        cell.setCellValue((String) field);
                }
                if (sheetNum == 2) {
                    excelDataRequest.getResultSet().next();
                }

            } else {

                for (int i = 4; i < noOfColumns; i++) {
                    Object field = excelDataRequest.getResultSet().getString(i);
                    Cell cell = row.createCell(noOfColumns - 6 + i);
                    cell.setCellStyle(mismatchStyle);
                    if (field instanceof String) {
                        cell.setCellValue((String) field);
                    } else if (field instanceof Integer) {
                        cell.setCellValue((Integer) field);
                    } else if (field instanceof Long) {
                        cell.setCellValue((Long) field);
                    } else if (field instanceof Boolean) {
                        cell.setCellValue((Boolean) field);
                    } else if (field instanceof Character) {
                        cell.setCellValue((Character) field);
                    } else if (field instanceof BigInteger) {
                        cell.setCellValue(String.valueOf((BigInteger) field));
                    } else
                        cell.setCellValue((String) field);
                }
            }
        }
    }
}


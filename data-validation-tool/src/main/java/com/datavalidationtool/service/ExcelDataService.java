package com.datavalidationtool.service;

import com.datavalidationtool.ds.DataSource;
import com.datavalidationtool.model.ExcelDataRequest;
import com.datavalidationtool.model.request.ExportDataRequest;
import com.datavalidationtool.model.request.ValidationRequest;
import com.datavalidationtool.model.response.HostDetails;
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
    public String readExcel(String filePath) throws IOException {
        File file = new File(filePath);
        FileInputStream inputStream = null;
        StringBuilder toReturn = new StringBuilder();
        try {
            inputStream = new FileInputStream(file);
            Workbook baeuldungWorkBook = new XSSFWorkbook(inputStream);
            for (Sheet sheet : baeuldungWorkBook) {
                toReturn.append("--------------------------------------------------------------------")
                        .append("~");
                toReturn.append("Worksheet :")
                        .append(sheet.getSheetName())
                        .append("~");
                toReturn.append("--------------------------------------------------------------------")
                        .append("~");
                int firstRow = sheet.getFirstRowNum();
                int lastRow = sheet.getLastRowNum();
                for (int index = firstRow + 1; index <= lastRow; index++) {
                    Row row = sheet.getRow(index);
                    toReturn.append("|| ");
                }
            }
            inputStream.close();
            baeuldungWorkBook.close();

        } catch (IOException e) {
            throw e;
        }
        return toReturn.toString();
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
            Cell fRCell0 = firstRow.createCell(0);
            fRCell0.setCellValue("Schema_Name");
            Cell fRCell1 = firstRow.createCell(1);
            fRCell1.setCellValue(schemaName);

            Cell sRCell0 = secoundRow.createCell(0);
            sRCell0.setCellValue("Table_Name");
            Cell sRCell1 = secoundRow.createCell(1);
            sRCell1.setCellValue(tableName);

            Cell tRCell0 = thirdRow.createCell(0);
            tRCell0.setCellValue("Run_Id");
            Cell tRCell1 = thirdRow.createCell(1);
            tRCell1.setCellValue(exportDataRequest.getRunId());

            //Current record details.
            XSSFSheet sheet = workbook.createSheet("Details");
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
            ArrayList<String> colList=excelDataRequest.getColList();

            Cell headerCell0 = topHeader.createCell(1);
            String colName = "Source Data";
            headerCell0.setCellValue(colName);
            headerCell0.setCellStyle(headerStyle);

            Cell headerCell1 = topHeader.createCell(noOfColumns-2);
            colName = "Target Data";
            headerCell1.setCellValue(colName);
            headerCell1.setCellStyle(headerStyle);

           // sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));
           // sheet.addMergedRegion(new CellRangeAddress(1, 1, 5, 9));
            // First 2 columns are valId and run Id
            for (int i = 3; i < noOfColumns; i++) {
                Cell headerCell = header.createCell(i-3);
                colName = resultSetMetaData.getColumnName(i);
                headerCell.setCellValue(colName);
                headerCell.setCellStyle(headerStyle);
            }
           // First 2 columns are valId and run Id
            for (int i = 4; i < noOfColumns; i++) {
                Cell headerCell = header.createCell(noOfColumns-6+i);
                colName = resultSetMetaData.getColumnName(i);
                headerCell.setCellValue(colName);
                headerCell.setCellStyle(headerStyle);
            }

            while (excelDataRequest.getResultSet().next() && rowCount < MAX_ROWS_IN_SHEET) {
                Row row = sheet.createRow(++rowCount);
                for (int i = 3; i < noOfColumns; i++) {
                    Object field = excelDataRequest.getResultSet().getString(i);
                    Cell cell = row.createCell(i-3);
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
                for (int i = 4; i < noOfColumns; i++) {
                    Object field = excelDataRequest.getResultSet().getString(i);
                    Cell cell = row.createCell(noOfColumns-6+i);
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
            try (POIFSFileSystem fs = new POIFSFileSystem()) {

                EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
                Encryptor encryptor = info.getEncryptor();
                encryptor.confirmPassword("Password1@");
                /* Read in an existing OOXML file and write to encrypted output stream
                 * don't forget to close the output stream otherwise the padding bytes
                 * aren't added

                try (OPCPackage opc = OPCPackage.open(tableName + ".xlsx", PackageAccess.READ_WRITE);
                     OutputStream os = encryptor.getDataStream(fs)) {
                    opc.save(os);
                } catch (IOException | InvalidFormatException e) {
                    e.printStackTrace();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
*/
                // Write out the encrypted version
                try (FileOutputStream outputStream = new FileOutputStream("/Users/amsudan/Desktop/Projects/DataValidation/awslab/"+tableName + ".xlsx")) {
                    workbook.write(outputStream);
                    outputStream.flush();
                    outputStream.close();
                    try (OPCPackage opc = OPCPackage.open("/Users/amsudan/Desktop/Projects/DataValidation/awslab/"+tableName + ".xlsx", PackageAccess.READ_WRITE);
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
                pst = con.prepareStatement(preparedQuery);
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
}



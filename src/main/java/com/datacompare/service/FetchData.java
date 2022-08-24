/**
 * Service class to Fatch the data from the databse using JDBC resultset. C.
 * This runs a thread for multiple databases( source and target)
 *
 *
 * @author      Harnath Valeti
 * @author      Madhu Athinarapu
 * @version     1.0
 * @since       1.0
 */
package com.datacompare.service;

import com.datacompare.model.AppProperties;
import com.datacompare.model.DatabaseInfo;
import com.datacompare.model.TableColumnMetadata;
import com.datacompare.util.DateUtil;
import com.datacompare.util.JdbcUtil;
import com.datacompare.util.MemoryUtil;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

public class FetchData implements Runnable {

    /**
     *
     */
    public Logger logger = LoggerFactory.getLogger("FetchDataLog");

    private String PIPE_SEPARATOR = " || ";

    private String chunk = null;
    private String dbType = null;
    private String sourceDBType = null;
    private String sortKey = null;
    private String sql = null;

    private Connection connection = null;

    private int fetchSize;
    private long rowCount;
    private boolean compareOnlyDate;

    public String getPrimaryKeys() {
        return primaryKeys;
    }

    public void setPrimaryKeys(String primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    private String primaryKeys;

    private Map<String, String> hashMap = null;
    private Map<String, TableColumnMetadata> tableMetadataMap = null;
    private Map<String, TableColumnMetadata> sourceTableMetadataMap = null;

    private List<Long> timeTaken;

    /**
     * @param dbType
     * @param sourceDBType
     * @param sql
     * @param chunk
     * @param connection
     * @param tableMetadata
     * @param sourceTableMetadata
     * @param appProperties
     * @throws Exception
     */
    public FetchData(String dbType, String sourceDBType, String sql, String chunk, Connection connection,
                     Map<String, TableColumnMetadata> tableMetadata, Map<String, TableColumnMetadata> sourceTableMetadata,
                     AppProperties appProperties) throws Exception {

        if (!EnumUtils.isValidEnum(DatabaseInfo.dbType.class, dbType))
            throw new Exception(dbType + " not supported.");

        setConnection(connection);
        setDbType(dbType);
        setSourceDBType(sourceDBType);
        setSql(sql);
        setChunk(chunk);
        setFetchSize(appProperties.getFetchSize());
        setCompareOnlyDate(appProperties.isCompareOnlyDate());
        setTableMetadataMap(tableMetadata);
        setSourceTableMetadataMap(sourceTableMetadata);
        setHashMap(new ConcurrentHashMap<String, String>());
        setPrimaryKeys(getSuppliedPrimaryKey(appProperties));
        Thread.currentThread().setName(getDbType() + " " + getChunk());

        //logger.info("\n" + getDbType() + ": SQL Query without chunk: " + getSql());
    }

    @Override
    public void run() {
        //logger.info("Starting sql statement execution.."+chunk);
        Thread.currentThread().setName(getDbType() + " " + chunk);

        StringBuilder info = new StringBuilder();

        info.append("Started executing chunk for DB: ");
        info.append(getDbType());
        info.append(" ");
        info.append(getSql());
        info.append(" ");
        info.append(getChunk());

        logger.info(info.toString());

        Statement stmt = null;
        ResultSet rs = null;

        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            getHashMap().clear();
            stmt = getConnection().createStatement();
            long start = System.currentTimeMillis();
            long keySize = 0;
            long valSize = 0;
            String query = getSql() + " " + getChunk();
            //logger.debug(query);
            rs = stmt.executeQuery(query);
            rs.setFetchSize(getFetchSize());
            while (rs.next()) {
                try {
                    String key = rs.getString("key1");
                    StringBuilder value = new StringBuilder();
                    StringBuilder pKeys = new StringBuilder();
                    List<String> colNames = new ArrayList<String>(
                            getSourceTableMetadataMap() != null && !getSourceTableMetadataMap().isEmpty()
                                    ? getSourceTableMetadataMap().keySet() : getTableMetadataMap().keySet());
                    for (String colName : colNames) {
                        //Check if it is a provided pumiquekey column
                        boolean IsAKey= isAnUniqueKey(colName);
                        colName = "POSTGRESQL".equals(getDbType()) ? colName.toLowerCase() : colName.toUpperCase();

                        TableColumnMetadata metadata = getTableMetadataMap().get(colName);
                        TableColumnMetadata sourceMetadata = null;
                        String columnType = metadata.getColumnType();
                        String columnName=metadata.getColumnName();
                        int noOfDecimals = metadata.getNoOfDecimals();
                        int sourceNoOfDecimals = 0;
                        if ("POSTGRESQL".equals(getDbType())) {
                            sourceMetadata = getSourceTableMetadataMap().get(metadata.getColumnName().toUpperCase());
                            if (sourceMetadata != null) {
                                sourceNoOfDecimals = sourceMetadata.getNoOfDecimals();
                            }
                            // To handle the column names with #
                            if(columnName.contains("#")) {
                                if(!columnName.contains("#\"")) {
                                //    columnName = "\"" + columnName + "\"";
                                }
                            }
                        }
                        if (columnType.contains("int") || columnType.contains("INT")) {

                            long rValue = rs.getLong(columnName);
                            value.append(PIPE_SEPARATOR);
                            value.append(rValue);
                            if(IsAKey){
                                pKeys.append(PIPE_SEPARATOR);
                                pKeys.append(rValue);
                            }

                        } else if (columnType.contains("boolean") || columnType.contains("BOOLEAN")) {

                            boolean bValue = rs.getBoolean(columnName);
                            value.append(PIPE_SEPARATOR);
                            value.append(bValue);
                            if(IsAKey){
                                pKeys.append(PIPE_SEPARATOR);
                                pKeys.append(bValue);
                            }
                        } else if (columnType.compareTo("DECIMAL") == 0 || columnType.compareTo("NUMBER") == 0
                                || columnType.compareTo("numeric") == 0) {

                            noOfDecimals = (sourceNoOfDecimals > 0) ? sourceNoOfDecimals : noOfDecimals;

                            if (noOfDecimals > 0) {

                                String decimalFormat = (sourceMetadata != null) ? sourceMetadata.getDecimalFormat()
                                        : metadata.getDecimalFormat();

                                DecimalFormat df2 = new DecimalFormat(decimalFormat);
                                df2.setGroupingUsed(false);
                                double doub = 0;

                                if (getDbType().equals("ORACLE") && columnType.compareTo("NUMBER") == 0) {

                                    doub = rs.getDouble(columnName);

                                } else {

                                    BigDecimal bValue = rs.getBigDecimal(columnName);
                                    doub = (bValue != null) ? bValue.doubleValue() : 0;
                                }

                                value.append(PIPE_SEPARATOR);
                                value.append(df2.format(doub));
                                if(IsAKey){
                                    pKeys.append(PIPE_SEPARATOR);
                                    pKeys.append(doub);
                                }

                            } else {

                                if (getDbType().equals("ORACLE") && columnType.compareTo("NUMBER") == 0) {

                                    long lValue = rs.getLong(columnName);
                                    value.append(PIPE_SEPARATOR);
                                    value.append(Long.valueOf(lValue).toString());
                                    if(IsAKey){
                                        pKeys.append(PIPE_SEPARATOR);
                                        pKeys.append(Long.valueOf(lValue).toString());
                                    }

                                } else {

                                    String dValue = rs.getString(columnName);
                                    String nValue = (dValue != null && !dValue.equals("null")
                                            && dValue.trim().length() > 0) ? dValue : "0";
                                    value.append(PIPE_SEPARATOR);
                                    value.append(nValue);
                                    if(IsAKey){
                                        pKeys.append(PIPE_SEPARATOR);
                                        pKeys.append(nValue);
                                    }
                                }
                            }

                        } else if (columnType.contains("float") || columnType.contains("FLOAT") || columnType.compareTo("DOUBLE") == 0) {

                            noOfDecimals = (sourceNoOfDecimals > 0) ? sourceNoOfDecimals : noOfDecimals;

                            if (noOfDecimals > 0) {

                                String decimalFormat = (sourceMetadata != null) ? sourceMetadata.getDecimalFormat()
                                        : metadata.getDecimalFormat();

                                DecimalFormat df2 = new DecimalFormat(decimalFormat);
                                df2.setGroupingUsed(false);
                                double doub = rs.getDouble(columnName);
                                value.append(PIPE_SEPARATOR);
                                value.append(df2.format(doub));
                                if(IsAKey){
                                    pKeys.append(PIPE_SEPARATOR);
                                    pKeys.append(df2.format(doub));
                                }

                            } else {

                                String dValue = rs.getString(columnName);
                                String fValue = (dValue != null && !dValue.equals("null") && dValue.trim().length() > 0)
                                        ? dValue
                                        : "0";
                                value.append(PIPE_SEPARATOR);
                                value.append(fValue);
                                if(IsAKey){
                                    pKeys.append(PIPE_SEPARATOR);
                                    pKeys.append(fValue);
                                }
                            }

                        } else if (columnType.contains("char") || columnType.contains("CHAR")) {

                            String cValue = rs.getString(columnName);
                            String cVal = (cValue != null && !cValue.equals("null") && cValue.trim().length() > 0)
                                    ? cValue
                                    : "";
                            value.append(PIPE_SEPARATOR);
                            value.append(cVal);
                            if(IsAKey){
                                pKeys.append(PIPE_SEPARATOR);
                                pKeys.append(cVal);
                            }

                        } else if (columnType.contains("timestamp") || columnType.contains("TIMESTAMP")) {

                            try {

                                Timestamp timestamp = rs.getTimestamp(columnName);

                                String dtStr = (timestamp != null) ? ((isCompareOnlyDate())
                                        ? dateFormat.format(timestamp) : dateTimeFormat.format(timestamp)) : "";

                                value.append(PIPE_SEPARATOR);
                                value.append(dtStr);
                                if(IsAKey){
                                    pKeys.append(PIPE_SEPARATOR);
                                    pKeys.append(dtStr);
                                }

                            } catch (Exception e) {

                                logger.error(getDbType(), e);
                                value.append(PIPE_SEPARATOR);
                                value.append("");
                                if(IsAKey){
                                    pKeys.append(PIPE_SEPARATOR);
                                    pKeys.append("");
                                }
                            }

                        } else if (columnType.contains("DATE") || columnType.contains("date")) {

                            try {

                                java.sql.Timestamp date = rs.getTimestamp(columnName);

                                String dtStr = (date != null) ? ((isCompareOnlyDate())
                                        ? dateFormat.format(date) : dateTimeFormat.format(date)) : "";

                                value.append(PIPE_SEPARATOR);
                                value.append(dtStr);
                                if(IsAKey){
                                    pKeys.append(PIPE_SEPARATOR);
                                    pKeys.append(dtStr);
                                }

                            } catch (Exception e) {

                                logger.error(getDbType(), e);

                                value.append(PIPE_SEPARATOR);
                                value.append("");
                                if(IsAKey){
                                    pKeys.append(PIPE_SEPARATOR);
                                    pKeys.append("");
                                }
                            }

                        } else if (
                                   columnType.equalsIgnoreCase("TEXT")
                                           || columnType.equalsIgnoreCase("BYTEA")
                                           || columnType.equalsIgnoreCase("BLOB")){

                            // Binary data as MD5 hash value
                            byte blobVal[] = rs.getBytes(columnName);
                            String hValue = (blobVal != null && !blobVal.equals("null") && blobVal.length > 0)
                                    ? getHash(blobVal)
                                    : "";
                            value.append(PIPE_SEPARATOR);
                            value.append(hValue);
                            if(IsAKey){
                                pKeys.append(PIPE_SEPARATOR);
                                pKeys.append(hValue);
                            }

                        } else if (columnType.equalsIgnoreCase("CLOB")) {
                            // Clob data
							Clob clob  = rs.getClob(columnName);
                            String cValue = (clob != null && !clob.equals("null"))
                                    ? getHash(clob.getSubString(1,(int)clob.length()).getBytes())
                                    : "";
                            value.append(PIPE_SEPARATOR);
                            value.append(cValue);
                            if(IsAKey){
                                pKeys.append(PIPE_SEPARATOR);
                                pKeys.append(cValue);
                            }

                        } else {

                            String oValue = rs.getString(columnName);
                            String oVal = (oValue != null && !oValue.equals("null") && oValue.trim().length() > 0)
                                    ? oValue
                                    : "";
                            value.append(PIPE_SEPARATOR);
                            value.append(oVal);
                            if(IsAKey){
                                pKeys.append(PIPE_SEPARATOR);
                                pKeys.append(oVal);
                            }
                        }
                    }

                    value.append(PIPE_SEPARATOR);
                    pKeys.append(PIPE_SEPARATOR);

                    String val = StringUtils.normalizeSpace(value.toString()).trim();
                    String uKeys = StringUtils.normalizeSpace(pKeys.toString()).trim();
                    if(uKeys!=null && uKeys.length()>0){
                        key=uKeys;
                    }
                    if (val != null && val.trim().length() > 0) {
                        valSize = valSize + val.getBytes().length;
                        keySize = keySize + key.getBytes().length;
                    }
                    //TODO------
                   if(getPrimaryKeys()!=null && getPrimaryKeys().length()>0){
                       if (getHashMap().containsKey(uKeys.trim())) {
                           int dupNumber = getDuplicateNumber(getHashMap(), uKeys.trim());
                          // int dupNumber = Collections.frequency(getHashMap().keySet(), uKeys.trim());
                           String dupKey = uKeys.trim() + "DUP" + dupNumber;
                           getHashMap().put(dupKey, val);
                       } else {
                           getHashMap().put(uKeys.trim(), val);
                       }

                   }else {
                       if (getHashMap().containsKey(key.trim())) {
                           int dupNumber = getDuplicateNumber(getHashMap(), key.trim());
                           //int dupNumber = Collections.frequency(getHashMap().keySet(), key.trim());
                           String dupKey = key.trim() + "DUP" + dupNumber;
                           getHashMap().put(dupKey, val);
                       } else {
                           getHashMap().put(key.trim(), val);
                       }
                   }



                } catch (Exception e) {

                    logger.error(getDbType(), e);
                }
            }

            logger.debug(getDbType() + " Map Size in bytes " + (keySize + valSize) + " ,Value Size in Bytes "
                    + valSize + " , Key Size in Bytes " + keySize);
            new MemoryUtil().displayMemoryInfo();

            long end = System.currentTimeMillis();

            long diffInSeconds = (end - start) / 1000;

            getTimeTaken().add(Long.valueOf(diffInSeconds));

            String timeTaken = new DateUtil().timeDiffFormatted(diffInSeconds);

            info = new StringBuilder();

            info.append(getDbType());
            info.append(" CHUNK: ");
            //info.append(getSql());
            //info.append(" ");
            info.append(getChunk());
            info.append("\n");
            info.append("Time Taken to fetch this chunk = ");
            info.append(timeTaken);

            logger.info(info.toString());

        } catch (SQLException ex) {

            ex.printStackTrace();
            logger.error(getDbType(), ex);

        } finally {

            JdbcUtil jdbcUtil = new JdbcUtil();

            jdbcUtil.closeResultSet(rs);
            jdbcUtil.closeStatement(stmt);
            //logger.info("Statement execution completed"+chunk );
        }
    }

    private int getDuplicateNumber(Map<String, String> hashMap, String key) {
        int cnt=1;
        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            String dupKey=key.trim()+"DUP"+cnt;
            if(!hashMap.containsKey(dupKey)){
                return cnt;
            }
            cnt++;
        }
        return cnt;
    }

    /**
     * @return MD5 hash to compare the large objects
     */
    public String getHash(byte[] data) {
        String hashString = null;
        byte[] md5Hex = DigestUtils.md5Digest(data);
        if (md5Hex != null && md5Hex.length > 0)
            hashString = new String(md5Hex);
        return hashString;
    }
    /**
     * @return the chunk
     */
    public String getChunk() {
        return chunk;
    }

    /**
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * @return
     */
    public String getDbType() {
        return dbType;
    }

    /**
     * @return the sourceDBType
     */
    public String getSourceDBType() {
        return sourceDBType;
    }

    /**
     * @return the fetchSize
     */
    public int getFetchSize() {
        return fetchSize;
    }

    /**
     * @return
     */
    public Map<String, String> getHashMap() {
        return hashMap;
    }

    /**
     * @return
     */
    public long getRowCount() {
        return rowCount;
    }

    /**
     * @return the compareOnlyDate
     */
    public boolean isCompareOnlyDate() {
        return compareOnlyDate;
    }

    /**
     * @return
     */
    public String getSortKey() {
        return sortKey;
    }

    /**
     * @return the sql
     */
    public String getSql() {
        return sql;
    }

    /**
     * @return the tableMetadataMap
     */
    public Map<String, TableColumnMetadata> getTableMetadataMap() {
        return tableMetadataMap;
    }

    /**
     * @return the sourceTableMetadataMap
     */
    public Map<String, TableColumnMetadata> getSourceTableMetadataMap() {
        return sourceTableMetadataMap;
    }

    /**
     * @param chunk
     */
    public void setChunk(String chunk) {
        this.chunk = chunk;
    }

    /**
     * @param connection the connection to set
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * @param dbType
     */
    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    /**
     * @param sourceDBType the sourceDBType to set
     */
    public void setSourceDBType(String sourceDBType) {
        this.sourceDBType = sourceDBType;
    }

    /**
     * @param fetchSize the fetchSize to set
     */
    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    /**
     * @param hashMap the hashMap to set
     */
    public void setHashMap(Map<String, String> hashMap) {
        this.hashMap = hashMap;
    }

    /**
     * @param rowCount
     */
    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }

    /**
     * @param compareOnlyDate the compareOnlyDate to set
     */
    public void setCompareOnlyDate(boolean compareOnlyDate) {
        this.compareOnlyDate = compareOnlyDate;
    }

    /**
     * @param sortKey
     */
    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    /**
     * @param sql the sql to set
     */
    public void setSql(String sql) {
        this.sql = sql;
    }

    /**
     * @param tableMetadataMap the tableMetadataMap to set
     */
    public void setTableMetadataMap(Map<String, TableColumnMetadata> tableMetadataMap) {
        this.tableMetadataMap = tableMetadataMap;
    }

    /**
     * @param sourceTableMetadataMap the sourceTableMetadataMap to set
     */
    public void setSourceTableMetadataMap(Map<String, TableColumnMetadata> sourceTableMetadataMap) {
        this.sourceTableMetadataMap = sourceTableMetadataMap;
    }

    /**
     * @return the timeTaken
     */
    public List<Long> getTimeTaken() {
        return timeTaken;
    }

    /**
     * @param timeTaken the timeTaken to set
     */
    public void setTimeTaken(List<Long> timeTaken) {
        this.timeTaken = timeTaken;
    }

    /*********************************************************************************************
     * From CLOB to String
     * @return string representation of clob
     *********************************************************************************************/
    private String clobToString(java.sql.Clob data) {
        final StringBuilder sb = new StringBuilder();
        try {
            final Reader reader = data.getCharacterStream();
            final BufferedReader br = new BufferedReader(reader);
            int b;
            while (-1 != (b = br.read())) {
                sb.append((char) b);
            }
            br.close();
        } catch (SQLException e) {
            logger.error("SQL. Could not convert CLOB to string", e);
            return e.toString();
        } catch (IOException e) {
            logger.error("IO. Could not convert CLOB to string", e);
            return e.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    /*********************************************************************************************
     * From Blob to String for POSTGRESQL Only
     * @return string representation of LOB
     *********************************************************************************************/
    private String blobToString(long oid) {
        String blobString = null;
        LargeObject obj =null;
        try {
            getConnection().setAutoCommit(false);
            String temp;
            LargeObjectManager lobj = ((org.postgresql.PGConnection) getConnection())
                    .getLargeObjectAPI();
            obj = lobj.open(oid, LargeObjectManager.READ);
            // Read the data
            byte[] buf = new byte[obj.size()];
            obj.read(buf, 0, obj.size());
            blobString= new String(buf);

        } catch (SQLException e) {
            logger.error("SQL. Could not convert Blob to string", e);
            return e.toString();
        }  catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                getConnection().commit();
                obj.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return blobString;
    }

    private String getSuppliedPrimaryKey(AppProperties appProperties) {
        String pkeys =null;
        if(appProperties!=null) {
            String tableName=appProperties.getTableName();
            if (appProperties.getPrimaryKeyMap() != null
                    && !appProperties.getPrimaryKeyMap().isEmpty()
                    && appProperties.getPrimaryKeyMap().get(tableName.toUpperCase()) != null) {
                pkeys = appProperties.getPrimaryKeyMap().get(tableName.toUpperCase());
            }
        }
        return pkeys;
    }

    private boolean isAnUniqueKey( String column) {
        String id =null;
        String suppliedPrimaryKey=getPrimaryKeys();
        if(suppliedPrimaryKey!=null && suppliedPrimaryKey.trim().length()>0){
            String cols[]=suppliedPrimaryKey.split(",");
            if(cols!=null &&cols.length>0 ) {
                for(int i=0; i< cols.length; i++){
                    if(column!=null && column.equalsIgnoreCase(cols[i].trim()))
                        return true;
                }
            }
        }
        return false;
    }
   }

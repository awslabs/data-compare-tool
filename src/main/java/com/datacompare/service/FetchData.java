
package com.datacompare.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datacompare.model.AppProperties;
import com.datacompare.model.DatabaseInfo;
import com.datacompare.model.TableColumnMetadata;
import com.datacompare.util.DateUtil;
import com.datacompare.util.JdbcUtil;
import com.datacompare.util.MemoryUtil;

public class FetchData implements Runnable {

	/** */
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

	private Map<String, String> hashMap = null;
	private Map<String, TableColumnMetadata> tableMetadataMap = null;
	private Map<String, TableColumnMetadata> sourceTableMetadataMap = null;

	private List<Long> timeTaken;

	/**
	 * 
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
		
		Thread.currentThread().setName(getDbType() + " " + getChunk()); 

		//logger.info("\n" + getDbType() + ": SQL Query without chunk: " + getSql());
	}

	@Override
	public void run() {

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
					
					List<String> colNames = new ArrayList<String>(
							getSourceTableMetadataMap() != null && !getSourceTableMetadataMap().isEmpty()
									? getSourceTableMetadataMap().keySet() : getTableMetadataMap().keySet());
					
					for (String colName : colNames) {
						
						colName = "POSTGRESQL".equals(getDbType()) ? colName.toLowerCase() : colName.toUpperCase();
						
						TableColumnMetadata metadata = getTableMetadataMap().get(colName);
						TableColumnMetadata sourceMetadata = null;

						String columnType = metadata.getColumnType();
						int noOfDecimals = metadata.getNoOfDecimals();
						int sourceNoOfDecimals = 0;
						
						if("POSTGRESQL".equals(getDbType())) {
							
							sourceMetadata = getSourceTableMetadataMap().get(metadata.getColumnName().toUpperCase());
							
							if(sourceMetadata != null) {
								
								sourceNoOfDecimals = sourceMetadata.getNoOfDecimals();
							}
						}

						if (columnType.contains("int") || columnType.contains("INT")) {

							long rValue = rs.getLong(metadata.getColumnName());
							value.append(PIPE_SEPARATOR);
							value.append(rValue);

						} else if (columnType.contains("boolean") || columnType.contains("BOOLEAN")) {

							boolean bValue = rs.getBoolean(metadata.getColumnName());
							value.append(PIPE_SEPARATOR);
							value.append(bValue);

						} else if (columnType.compareTo("DECIMAL") == 0 || columnType.compareTo("NUMBER") == 0
								|| columnType.compareTo("numeric") == 0) {

							noOfDecimals = (sourceNoOfDecimals > 0) ? sourceNoOfDecimals : noOfDecimals;
							
							if (noOfDecimals > 0) {
								
								String decimalFormat = (sourceMetadata != null) ? sourceMetadata.getDecimalFormat()
										: metadata.getDecimalFormat();
								
								DecimalFormat df2 = new DecimalFormat(decimalFormat);

								double doub = 0;

								if (getDbType().equals("ORACLE") && columnType.compareTo("NUMBER") == 0) {

									doub = rs.getDouble(metadata.getColumnName());

								} else {

									BigDecimal bValue = rs.getBigDecimal(metadata.getColumnName());
									doub = (bValue != null) ? bValue.doubleValue() : 0;
								}

								value.append(PIPE_SEPARATOR);
								value.append(df2.format(doub));

							} else {

								if (getDbType().equals("ORACLE") && columnType.compareTo("NUMBER") == 0) {

									long lValue = rs.getLong(metadata.getColumnName());
									value.append(PIPE_SEPARATOR);
									value.append(Long.valueOf(lValue).toString());
									
								} else {

									String dValue = rs.getString(metadata.getColumnName());
									String nValue = (dValue != null && !dValue.equals("null")
											&& dValue.trim().length() > 0) ? dValue : "0";
									value.append(PIPE_SEPARATOR);
									value.append(nValue);
								}
							}

						} else if (columnType.contains("float") || columnType.contains("FLOAT") || columnType.compareTo("DOUBLE") == 0) {

							noOfDecimals = (sourceNoOfDecimals > 0) ? sourceNoOfDecimals : noOfDecimals;
							
							if (noOfDecimals > 0) {

								String decimalFormat = (sourceMetadata != null) ? sourceMetadata.getDecimalFormat()
										: metadata.getDecimalFormat();
								
								DecimalFormat df2 = new DecimalFormat(decimalFormat);

								double doub = rs.getDouble(metadata.getColumnName());

								value.append(PIPE_SEPARATOR);
								value.append(df2.format(doub));

							} else {

								String dValue = rs.getString(metadata.getColumnName());
								String fValue = (dValue != null && !dValue.equals("null") && dValue.trim().length() > 0)
										? dValue
										: "0";
								value.append(PIPE_SEPARATOR);
								value.append(fValue);
							}

						} else if (columnType.contains("char") || columnType.contains("CHAR")) {

							String cValue = rs.getString(metadata.getColumnName());
							String cVal = (cValue != null && !cValue.equals("null") && cValue.trim().length() > 0)
									? cValue
									: "";
							value.append(PIPE_SEPARATOR);
							value.append(cVal);

						} else if (columnType.contains("timestamp") || columnType.contains("TIMESTAMP")) {

							try {

								Timestamp timestamp = rs.getTimestamp(metadata.getColumnName());
								
								String dtStr = (timestamp != null) ? ((isCompareOnlyDate())
										? dateFormat.format(timestamp) : dateTimeFormat.format(timestamp)) : "";

								value.append(PIPE_SEPARATOR);
								value.append(dtStr);
								
							} catch (Exception e) {

								logger.error(getDbType(), e);
								value.append(PIPE_SEPARATOR);
								value.append("");
							}

						} else if (columnType.contains("DATE") || columnType.contains("date")) {

							try {

								java.sql.Date date = rs.getDate(metadata.getColumnName());

								String dtStr = (date != null) ? ((isCompareOnlyDate())
										? dateFormat.format(date) : dateTimeFormat.format(date)) : "";

								value.append(PIPE_SEPARATOR);
								value.append(dtStr);

							} catch (Exception e) {

								logger.error(getDbType(), e);

								value.append(PIPE_SEPARATOR);
								value.append("");
							}

						} else if (columnType.contains("LOB") || columnType.contains("lob") 
								|| columnType.contains("bytea")
								|| columnType.contains("TEXT") || columnType.contains("text")) {
							
							// Binary data as MD5 hash value
							String hashValue = rs.getString(metadata.getColumnName());
							String hValue = (hashValue != null && !hashValue.equals("null") && hashValue.trim().length() > 0)
									? hashValue
									: "";
							value.append(PIPE_SEPARATOR);
							value.append(hValue);
							
						} else {

							String oValue = rs.getString(metadata.getColumnName());
							String oVal = (oValue != null && !oValue.equals("null") && oValue.trim().length() > 0)
									? oValue
									: "";
							value.append(PIPE_SEPARATOR);
							value.append(oVal);
						}
					}

					value.append(PIPE_SEPARATOR);

					String val = StringUtils.normalizeSpace(value.toString()).trim();
					
					if(val != null && val.trim().length() > 0) {
						
						valSize = valSize + val.getBytes().length;
						keySize = keySize + key.getBytes().length;
					}

					getHashMap().put(key.trim(), val);

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
		}
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
	 * 
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
	 * 
	 * @return
	 */
	public Map<String, String> getHashMap() {
		return hashMap;
	}

	/**
	 * 
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
	 * 
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
	 * 
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
	 * 
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
	 * 
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
	 * 
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
}

package com.datacompare.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datacompare.model.AppProperties;
import com.datacompare.model.DatabaseInfo;
import com.datacompare.model.TableColumnMetadata;
import com.datacompare.util.JdbcUtil;

public class FetchMetadata {

	/** */
	public Logger logger = LoggerFactory.getLogger("FetchMetadataLog");

	private List<String> chunks = new ArrayList<String>();

	public List<String> columns = new ArrayList<String>();

	private String dbType = null;

	private int fetchSize;

	private int maxDecimals;
	
	private long rowCount;
	
	private boolean hasNoUniqueKey; 
	
	private String sortKey;
	
	private String primaryKey;

	private String sql = null;

	private Map<String, TableColumnMetadata> tableMetadataMap = new LinkedHashMap<String, TableColumnMetadata>();

	/**
	 * Function reading the table meta data checking the each columns value
	 * 
	 * @param dbType
	 * @param sourceDBType
	 * @param connection
	 * @param schemaName
	 * @param tableName
	 * @param rowCount
	 * @param sourceSortKey
	 * @param sourcePrimaryKey
	 * @param sourceTableMetadataMap
	 * @param columnList
	 * @param appProperties
	 * @throws Exception
	 */
	public FetchMetadata(String dbType, String sourceDBType, Connection connection, String schemaName, String tableName,
			long rowCount, String sourceSortKey, String sourcePrimaryKey,
			Map<String, TableColumnMetadata> sourceTableMetadataMap, List<String> columnList,
			AppProperties appProperties) throws Exception {
		
		if (!EnumUtils.isValidEnum(DatabaseInfo.dbType.class, dbType)) throw new Exception(dbType + " not supported.");
		
		if("Detail".equals(appProperties.getReportType())) {
			
			fetchDetailData(dbType, sourceDBType, connection, schemaName, tableName, rowCount, sourceSortKey,
					sourcePrimaryKey, sourceTableMetadataMap, columnList, appProperties);
			
		} else if("Basic".equals(appProperties.getReportType())) {
			
			fetchBasicData(connection, schemaName, tableName, appProperties);
		}
	}
	
	/**
	 * 
	 * @param dbType
	 * @param sourceDBType
	 * @param connection
	 * @param schemaName
	 * @param tableName
	 * @param rowCount
	 * @param sourceSortKey
	 * @param sourcePrimaryKey
	 * @param sourceTableMetadataMap
	 * @param columnList
	 * @param appProperties
	 * @throws Exception
	 */
	private void fetchDetailData(String dbType, String sourceDBType, Connection connection, String schemaName,
			String tableName, long rowCount, String sourceSortKey, String sourcePrimaryKey,
			Map<String, TableColumnMetadata> sourceTableMetadataMap, List<String> columnList,
			AppProperties appProperties) throws Exception {
		
		StringBuilder sortKey = new StringBuilder();
		StringBuilder primaryKey = new StringBuilder();
		StringBuilder uniqueKeyCol = new StringBuilder();

		setDbType(dbType);
		setFetchSize(appProperties.getFetchSize());
		setMaxDecimals(appProperties.getMaxDecimals());

		Map<Integer, String> primaryKeyMap = new TreeMap<Integer, String>();

		fetchTableColumns(sourceDBType, connection, schemaName, tableName, columnList, appProperties.isIgnoreColumns()); 
		fetchPrimaryColumns(connection, schemaName, tableName, primaryKeyMap, sourceSortKey, sourcePrimaryKey,
				sortKey, primaryKey, uniqueKeyCol);
		prepareQuery(connection, schemaName, tableName, sortKey.toString(), primaryKey.toString(),
				uniqueKeyCol.toString(), appProperties.getFilter(), appProperties.getFilterType(), rowCount, sourceTableMetadataMap);
	}
	
	/**
	 * 
	 * @param connection
	 * @param schemaName
	 * @param tableName
	 * @param appProperties
	 * @throws SQLException
	 */
	private void fetchBasicData(Connection connection, String schemaName, String tableName,
			AppProperties appProperties) throws SQLException { 
		
		Long totalRecords = getTotalRecords(connection, schemaName, tableName, appProperties.getFilter()); 

		setRowCount(totalRecords);
	}

	/**
	 * 
	 * @param connection
	 * @param schemaName
	 * @param tableName
	 * @param sortKey
	 * @param primaryKey
	 * @param uniqueKeyCol
	 * @param filter
	 * @param filterType
	 * @param rowCount
	 * @param sourceTableMetadataMap
	 * @throws SQLException
	 */
	private void prepareQuery(Connection connection, String schemaName, String tableName, String sortKey,
			String primaryKey, String uniqueKeyCol, String filter, String filterType, long rowCount,
			Map<String, TableColumnMetadata> sourceTableMetadataMap) throws SQLException {
		
		String query = null;
		String cols = null;
		
		switch (getDbType()) {

		case "POSTGRESQL":

			cols = getTargetColumns(sourceTableMetadataMap);
			
			query = "SELECT " + uniqueKeyCol + cols + " FROM " + schemaName + "." + tableName;

			if(isHasNoUniqueKey()) {
				
				query = "SELECT t.* FROM (" + query + " order by " + cols + ") t";
			}
			
			generateChunksPostgresql(connection, schemaName, tableName, rowCount, filter);

			break;

		case "ORACLE":

			cols = getSourceColumns();
			
			query = "SELECT " + uniqueKeyCol + cols + " FROM " + schemaName + "." + tableName;
			
			if(isHasNoUniqueKey()) {
				
				query = "SELECT * FROM (" + query + " order by " + cols + ")";
			}
			
			generateSourceChunks(connection, schemaName, tableName, sortKey, primaryKey, filter, filterType, cols);

			break;

		case "SQLSERVER":

			cols = getSourceColumns();
			
			query = "SELECT " + uniqueKeyCol + cols + " FROM " + schemaName + "." + tableName;
			
			//TODO
			if(isHasNoUniqueKey()) {
				
				query = "SELECT t.* FROM (" + query + " order by " + cols + ") t";
			}
			
			generateSourceChunks(connection, schemaName, tableName, sortKey, primaryKey, filter, filterType, cols);

			break;
		}
		
		setSql(query); 
		
		logger.info("\n" + getDbType() + ": SQL Query without chunk: " + getSql());
	}
	
	/**
	 * 
	 * @return
	 */
	private String getSourceColumns() {
		
		return getColumns(getTableMetadataMap(), getTableMetadataMap(), false);
	}
	
	/**
	 * 
	 * @param sourceTableMetadataMap
	 * @return
	 */
	private String getTargetColumns(Map<String, TableColumnMetadata> sourceTableMetadataMap) {
		
		return getColumns(sourceTableMetadataMap, getTableMetadataMap(), true);
	}

	/**
	 * 
	 * @param sourceTableMetadataMap
	 * @param targetTableMetadataMap
	 * @param isTarget
	 * @return
	 */
	private String getColumns(Map<String, TableColumnMetadata> sourceTableMetadataMap,
			Map<String, TableColumnMetadata> targetTableMetadataMap, boolean isTarget) {
		
		StringBuilder cols = new StringBuilder();
		List<String> colNames = new ArrayList<String>(sourceTableMetadataMap.keySet());
		
		for (String colName : colNames) {
		
			colName = isTarget ? colName.toLowerCase() : colName.toUpperCase();
			
			String colAs = targetTableMetadataMap.get(colName).getColumnAs();
			
			cols.append(colAs + ",");
			
			columns.add(colName);
		}
		
		if (!cols.toString().isEmpty()) {

			cols.deleteCharAt(cols.length() - 1);
		}
		
		return cols.toString();
	}
	
	/**
	 * 
	 * @param connection
	 * @param schemaName
	 * @param tableName
	 * @param primaryKeyMap
	 * @param sourceSortKey
	 * @param sourcePrimaryKey
	 * @param sortKey
	 * @param primaryKey
	 * @param uniqueKeyCol
	 * @throws Exception
	 */
	private void fetchPrimaryColumns(Connection connection, String schemaName, String tableName,
			Map<Integer, String> primaryKeyMap, String sourceSortKey, String sourcePrimaryKey, StringBuilder sortKey,
			StringBuilder primaryKey, StringBuilder uniqueKeyCol) throws Exception {
		
		ResultSet rs = null;
		
		try {

			rs = connection.getMetaData().getPrimaryKeys(null, schemaName, tableName);

			while (rs.next()) {

				String colName = rs.getString("COLUMN_NAME");
				Integer pkPosition = rs.getInt("KEY_SEQ");
				primaryKeyMap.put(pkPosition, colName);
			}
			
			if (sourceSortKey != null && sourceSortKey.trim().length() > 0) {

				sortKey.append(sourceSortKey.toLowerCase());
				primaryKey.append(sourcePrimaryKey.toLowerCase());

			} else if (primaryKeyMap != null && primaryKeyMap.size() > 0) {

				sortKey.append(StringUtils.join(primaryKeyMap.values(),","));
				primaryKey.append(primaryKeyMap.get(1));

				this.setSortKey(sortKey.toString());
				this.setPrimaryKey(primaryKey.toString()); 
			}

		} catch (Exception e) {

			logger.error(getDbType(), e);

		} finally {

			new JdbcUtil().closeResultSet(rs);
		}

		if (sortKey.toString().isEmpty()) {

			setHasNoUniqueKey(true); 
			//throw new Exception(getDbType() + " Database Table " + schemaName + "." + tableName + " PRIMARY Key not found.");
		}

		if (!sortKey.toString().isEmpty() && sortKey.toString().contains(",")) {

			String primaryKeyColsTemp = sortKey.toString().replaceAll(",", "),").replaceFirst("\\)\\,", ",") + ")";

			String primaryKeyColsTempArray[] = primaryKeyColsTemp.split("\\)\\,");

			for (int i = 0; i < primaryKeyColsTempArray.length; i++) {

				primaryKeyColsTemp = "concat(" + primaryKeyColsTemp;
			}

			uniqueKeyCol.append(primaryKeyColsTemp + " AS key1 ,");

		} else if(!primaryKey.toString().isEmpty()) {

			uniqueKeyCol.append(primaryKey + " AS key1 ,");
			
		} else if(isHasNoUniqueKey()) {
			
			if("ORACLE".equals(getDbType())) {
				
				uniqueKeyCol.append("ROWNUM AS key1,");
				
			} else if("POSTGRESQL".equals(getDbType())) {
				
				uniqueKeyCol.append("row_number() over() as key1,");
				
			} else if("SQLSERVER".equals(getDbType())) {
				
				//TODO
				uniqueKeyCol.append("ROWNUM AS key1,");
			}
		}
	}
	
	/**
	 * 
	 * @param sourceDBType
	 * @param connection
	 * @param schemaName
	 * @param tableName
	 * @param columnList
	 */
	private void fetchTableColumns(String sourceDBType, Connection connection, String schemaName,
			String tableName, List<String> columnList, boolean ignoreColumns) {
		
		ResultSet rs = null;
		
		try {

			rs = connection.getMetaData().getColumns(null, schemaName, tableName, null);
			rs.setFetchSize(getFetchSize());

			while (rs.next()) {
				
				String col = null;

				String columnType = rs.getString("TYPE_NAME");

				String columnName = rs.getString("COLUMN_NAME");
				
				String columnSize = rs.getString("COLUMN_SIZE");

				int colSize = (columnSize != null && !columnSize.equals("null")) ? Integer.parseInt(columnSize) : 0;

				if(column(columnList, columnName, ignoreColumns)) continue;
				
				if (binaryColumnType(columnType) || varcharLargeSize(columnType, colSize)) { 
					
					/** In case if source db is Oracle, it will check length for binary column types. 
					 *  If source db is SQLServer, it will check MD5 hash string for binary column types.*/
					if("ORACLE".equals(getDbType())) {
						
						if(varcharLargeSize(columnType, colSize)) {   
							
							col = "LOWER(STANDARD_HASH(NVL(" + columnName
									+ ",'NULL'), 'MD5')) AS " + columnName;
						} else {
							
							col = "DBMS_LOB.GETLENGTH(" + columnName + ") AS " + columnName;
						}
						
					} else if("POSTGRESQL".equals(getDbType()) && "ORACLE".equals(sourceDBType) && !varcharLargeSize(columnType, colSize)) { 
						
						col = "LENGTH(" + columnName + ") as " + columnName;
						
					} else if("SQLSERVER".equals(getDbType())) {
						
						col = "HashBytes('MD5', COALESCE(" + columnName + ",'NULL')) as " + columnName;
						
					} else if("POSTGRESQL".equals(getDbType()) && ("SQLSERVER".equals(sourceDBType) || varcharLargeSize(columnType, colSize))) { 
						
						col = "MD5(COALESCE (" + columnName + ",'NULL')) as " + columnName;
						
					} else {
						
						continue;
					}
					
				} else {
					
					col = columnName;
				}

				boolean isNullable = rs.getString("IS_NULLABLE").toUpperCase().equals("YES");

				String decimalDigits = rs.getString("DECIMAL_DIGITS");

				int noOfDecimals = (decimalDigits != null && !decimalDigits.equals("null"))
						? Integer.parseInt(decimalDigits)
						: 0;

				String decimalFormat = "";
				
				if (columnType.compareTo("DECIMAL") == 0 || columnType.compareTo("NUMBER") == 0
						|| columnType.compareTo("numeric") == 0 || columnType.contains("float")
						|| columnType.contains("FLOAT") || columnType.compareTo("DOUBLE") == 0) {

					for (int i = 0; i < colSize; i++) {

						decimalFormat = decimalFormat + "#";
					}
				}
				
				if (noOfDecimals > getMaxDecimals() && getMaxDecimals() > 0) {
					
					decimalFormat = (decimalFormat.trim().length() > 0) ? decimalFormat + "." : "#.";

					for (int j = 0; j < getMaxDecimals(); j++) {

						decimalFormat = decimalFormat + "0";
					}
				} else if (noOfDecimals > 0) {
					
					decimalFormat = (decimalFormat.trim().length() > 0) ? decimalFormat + "." : "#.";

					for (int j = 0; j < noOfDecimals; j++) {

						decimalFormat = decimalFormat + "0";
					}
				}

				decimalFormat = decimalFormat.replace("#.", "0.");

				TableColumnMetadata tableColumnMetadata = new TableColumnMetadata();

				tableColumnMetadata.setColumnType(columnType);
				tableColumnMetadata.setColumnName(columnName);
				tableColumnMetadata.setNullable(isNullable);
				tableColumnMetadata.setNoOfDecimals(noOfDecimals);
				tableColumnMetadata.setColSize(colSize);
				tableColumnMetadata.setDecimalFormat(decimalFormat);
				tableColumnMetadata.setColumnAs(col); 

				tableMetadataMap.put(columnName, tableColumnMetadata);
			}

			if (tableMetadataMap.isEmpty()) {

				throw new Exception(getDbType() + " Database Table " + schemaName + "." + tableName + " has no columns.");
			}

		} catch (Exception e) {

			logger.error(getDbType(), e);

		} finally {

			new JdbcUtil().closeResultSet(rs);
		}
	}
	
	/**
	 * 
	 * @param connection
	 * @param schemaName
	 * @param tableName
	 * @param sortKey
	 * @param primaryKey
	 * @param filter
	 * @param filterType
	 * @param cols
	 * @throws SQLException
	 */
	private void generateSourceChunks(Connection connection, String schemaName, String tableName, String sortKey,
			String primaryKey, String filter, String filterType, String cols) throws SQLException {

		logger.info("Started preparing chunks");

		chunks.clear();
		
		Long totalRecords = getTotalRecords(connection, schemaName, tableName, filter); 

		setRowCount(totalRecords);

		long ntileSize = (long)Math.ceil(totalRecords / getFetchSize());
		
		ntileSize = (ntileSize <= 0) ? 1 : ntileSize;
		
		StringBuilder sql = new StringBuilder();
		
		if(isHasNoUniqueKey()) {
			
			sql.append("SELECT min(ROWNUM) AS startRange, max(ROWNUM) AS endRange,count(*) AS chunkSize, nt FROM (SELECT ROWNUM")
			.append(" ,ntile(").append(ntileSize).append(") OVER (ORDER BY ").append(cols).append(" ) nt FROM ").append(schemaName)
			.append(".").append(tableName);
			
		} else {
			
			sql.append("SELECT min(").append(primaryKey).append(") AS startRange, max(").append(primaryKey)
			.append(") AS endRange,count(*) AS chunkSize, nt FROM (SELECT ").append(primaryKey).append(" ,ntile(")
			.append(ntileSize).append(") OVER (ORDER BY ").append(primaryKey).append(" ) nt FROM ").append(schemaName)
			.append(".").append(tableName);
	
			if(filter != null && !filter.isEmpty()) {
				
				sql.append(" WHERE ").append(filter);
			}
		}
		
		if("ORACLE".equals(getDbType())) {
			
			sql.append(") GROUP BY nt ORDER BY nt");
			
		} else {
			
			sql.append(") as a GROUP BY nt ORDER BY nt");
		}
		
		logger.info("Fetch Chunks SQL Query: " + sql.toString()); 

		Statement stmt = connection.createStatement();

		ResultSet rs = stmt.executeQuery(sql.toString());

		while (rs.next()) {
			
			long startRange = rs.getLong("startRange");
			long endRange = rs.getLong("endRange");
			long chunkSize = rs.getLong("chunkSize");

			StringBuilder condition = new StringBuilder();
			
			condition.append("where ");

			if(isHasNoUniqueKey()) {
				
				condition.append("key1").append(" >= ").append(startRange).append(" and ").append("key1")
				.append(" <= ").append(endRange).append(" order by 1");
				
			} else {

				if(filter != null && !filter.isEmpty() && !"Sample".equals(filterType)) {
					
					condition.append(filter).append(" and ");
				}
				
				condition.append(primaryKey).append(" >= ").append(startRange).append(" and ").append(primaryKey)
						.append(" <= ").append(endRange).append(" order by ").append(sortKey);
			}

			logger.debug("Chunk Range, Min: " + startRange + ", Max: " + endRange + ", Size: " + chunkSize); 
			
			chunks.add(condition.toString());
		}
		
		JdbcUtil jdbcUtil = new JdbcUtil();
		
		jdbcUtil.closeResultSet(rs);
		jdbcUtil.closeStatement(stmt); 

		logger.info("Completed preparing chunks");
	}
	
	/**
	 * 
	 * @param connection
	 * @param schemaName
	 * @param tableName
	 * @param filter
	 * @return
	 * @throws SQLException
	 */
	private Long getTotalRecords(Connection connection, String schemaName, String tableName,
			String filter) throws SQLException {
		
		Long totalRecords = Long.valueOf(0); 

		StringBuilder sql = new StringBuilder();

		sql.append("SELECT count(*) as totalrec FROM ").append(schemaName).append(".").append(tableName);
		
		if(filter != null && filter.trim().length() > 0) {
			
			sql.append(" WHERE ").append(filter);
		}

		Statement stmt = connection.createStatement();

		ResultSet rs = stmt.executeQuery(sql.toString());

		if (rs.next()) {
			
			totalRecords = rs.getLong("totalrec");
		}

		JdbcUtil jdbcUtil = new JdbcUtil();
		
		jdbcUtil.closeResultSet(rs);
		jdbcUtil.closeStatement(stmt); 
		
		
		return totalRecords;
	}

	/**
	 * 
	 * @param connection
	 * @param schemaName
	 * @param tableName
	 * @param rowCount
	 * @param pkCols
	 * @param pKey
	 * @param filter
	 * @throws SQLException
	 */
	private void generateChunksPostgresql(Connection connection, String schemaName, String tableName, long rowCount,
			String filter) throws SQLException {

		logger.info("Started preparing chunks for Postgresql");

		chunks.clear();

		//Long totalRecords = getTotalRecords(connection, schemaName, tableName, filter); 

		if (rowCount == 0) {

			throw new SQLException("There is no records in Postgresql for " + schemaName + "." + tableName);
		}

//		if (totalRecords == 0) {
//
//			throw new SQLException("There is no records in Postgresql for " + schemaName + "." + tableName);
//		}

//		setRowCount(totalRecords);
		setRowCount(0);

		logger.info("Completed preparing chunks for Postgresql");
	}

	/**
	 * 
	 * @param columnType
	 * @return
	 */
	private boolean binaryColumnType(String columnType) {

		return (columnType.contains("LOB") || columnType.contains("lob") || columnType.contains("BLOB")
				|| columnType.contains("blob") || columnType.contains("bytea") || columnType.contains("CLOB")
				|| columnType.contains("clob") || columnType.contains("TEXT") || columnType.contains("text"));
	}
	
	/**
	 * 
	 * @param columnType
	 * @param colSize
	 * @return
	 */
	private boolean varcharLargeSize(String columnType, int colSize) {
	
		return ( (columnType.contains("VARCHAR") || columnType.contains("varchar")) && colSize > 500);
	}

	/**
	 * 
	 * @param columnsList
	 * @param column
	 * @param ignoreColumn
	 * @return
	 */
	private boolean column(List<String> columnsList, String column, boolean ignoreColumn) {

		for (String col : columnsList) {
			
			if (col.equalsIgnoreCase(column)) {

				return ignoreColumn;
			}
	    }
		
	    return (!columnsList.isEmpty()) ? !ignoreColumn : false;	
	}

	/**
	 * 
	 * @return
	 */
	public List<String> getChunks() {
		return chunks;
	}

	/**
	 * 
	 * @return
	 */
	public String getDbType() {
		return dbType;
	}

	/**
	 * @return the fetchSize
	 */
	public int getFetchSize() {
		return fetchSize;
	}

	/**
	 * @return the maxDecimals
	 */
	public int getMaxDecimals() {
		return maxDecimals;
	}

	/**
	 * 
	 * @return
	 */
	public long getRowCount() {
		return rowCount;
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
	 * 
	 * @param dbType
	 */
	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	/**
	 * @param fetchSize the fetchSize to set
	 */
	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}

	/**
	 * @param maxDecimals the maxDecimals to set
	 */
	public void setMaxDecimals(int maxDecimals) {
		this.maxDecimals = maxDecimals;
	}

	/**
	 * 
	 * @param rowCount
	 */
	public void setRowCount(long rowCount) {
		this.rowCount = rowCount;
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
	 * @return the tableMetadataMap
	 */
	public Map<String, TableColumnMetadata> getTableMetadataMap() {
		return tableMetadataMap;
	}

	/**
	 * @param tableMetadataMap the tableMetadataMap to set
	 */
	public void setTableMetadataMap(Map<String, TableColumnMetadata> tableMetadataMap) {
		this.tableMetadataMap = tableMetadataMap;
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	/**
	 * @return the hasNoUniqueKey
	 */
	public boolean isHasNoUniqueKey() {
		return hasNoUniqueKey;
	}

	/**
	 * @param hasNoUniqueKey the hasNoUniqueKey to set
	 */
	public void setHasNoUniqueKey(boolean hasNoUniqueKey) {
		this.hasNoUniqueKey = hasNoUniqueKey;
	}
}
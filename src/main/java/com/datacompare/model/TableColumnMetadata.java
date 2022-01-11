/**
 * Model class for database table meta data.
 *
 *
 * @author      Harnath Valeti
 * @author      Madhu Athinarapu
 * @version     1.0
 * @since       1.0
 */

package com.datacompare.model;

public class TableColumnMetadata {

	private String columnName;

	private boolean isNullable;

	private int noOfDecimals;
	
	private int maxTextSize;

	private int colSize;

	private String columnType;

	private String decimalFormat;
	
	private String columnAs;

	public int getColSize() {
		return colSize;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getColumnType() {
		return columnType;
	}

	public String getDecimalFormat() {
		return decimalFormat;
	}

	public int getNoOfDecimals() {
		return noOfDecimals;
	}

	public boolean isNullable() {
		return isNullable;
	}

	public void setColSize(int colSize) {
		this.colSize = colSize;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}

	public void setDecimalFormat(String decimalFormat) {
		this.decimalFormat = decimalFormat;
	}

	public void setNoOfDecimals(int noOfDecimals) {
		this.noOfDecimals = noOfDecimals;
	}

	public void setNullable(boolean isNullable) {
		this.isNullable = isNullable;
	}

	public String getColumnAs() {
		return columnAs;
	}

	public void setColumnAs(String columnAs) {
		this.columnAs = columnAs;
	}

	public int getMaxTextSize() {
		return maxTextSize;
	}

	public void setMaxTextSize(int maxTextSize) {
		this.maxTextSize = maxTextSize;
	}

	@Override
	public String toString() {
		return "TableColumnMetadata [columnName=" + columnName + ", isNullable=" + isNullable + ", noOfDecimals="
				+ noOfDecimals + ", maxTextSize=" + maxTextSize + ", colSize=" + colSize + ", columnType=" + columnType
				+ ", decimalFormat=" + decimalFormat + ", columnAs=" + columnAs + "]";
	}
}
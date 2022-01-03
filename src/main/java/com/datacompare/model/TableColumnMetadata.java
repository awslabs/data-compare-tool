package com.datacompare.model;

public class TableColumnMetadata {

	private String columnName;

	private boolean isNullable;

	private int noOfDecimals;

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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TableColumnMetadata [columnName=");
		builder.append(columnName);
		builder.append(", isNullable=");
		builder.append(isNullable);
		builder.append(", noOfDecimals=");
		builder.append(noOfDecimals);
		builder.append(", colSize=");
		builder.append(colSize);
		builder.append(", columnType=");
		builder.append(columnType);
		builder.append(", decimalFormat=");
		builder.append(decimalFormat);
		builder.append(", columnAs=");
		builder.append(columnAs);
		builder.append("]");
		return builder.toString();
	}
}
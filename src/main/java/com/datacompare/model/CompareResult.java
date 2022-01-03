package com.datacompare.model;

import java.util.ArrayList;
import java.util.List;

public class CompareResult {

	private String result;
	
	private String reason; //failure reason like record count or data mismatch
	
	private String tableName;
	
	private long rowCountSource;
	
	private long rowCountTarget;
	
	private long rowCount;
	
	private long matchedRowCount;
	
	private long mismatchRowCount;
	
	private long sourceFailedRowCount;
	
	private long targetFailedRowCount;
	
	private long timeTaken; //seconds
	
	private long failedRowNumber;
	
	private long usedMemory;
	
	private String filename;
	
	private List<String> failTuple = new ArrayList<String>();

	
	public void setFailTuple(List<String> failTuple) {
		this.failTuple = failTuple;
	}

	public List<String> getFailTuple() {
		return failTuple; 
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableName() {
		return tableName;
	}
	

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public long getRowCountTarget() {
		return rowCountTarget;
	}

	public long getRowCountSource() {
		return rowCountSource;
	}

	public void setRowCountSource(long rowCount) {
		this.rowCountSource = rowCount;
	}
	
	public void setRowCountTarget(long rowCount) {
		this.rowCountTarget = rowCount;
	}
	
	public void setValueMismatchCount(long rowCount){
		this.mismatchRowCount = rowCount;
	}
	
	public long getValueMismatchCount(){
		return this.mismatchRowCount;
	}
	
	public void setSourceFailedRowCount(long rowCount) {
		 this.sourceFailedRowCount = rowCount;
	}
	
	public long getTargetFailedRowCount() {
		return this.targetFailedRowCount;
	}
	
	public void setTargetFailedRowCount(long rowCount) {
		 this.targetFailedRowCount = rowCount;
	}
	
	public long getSourceFailedRowCount() {
		return this.sourceFailedRowCount;
	}
	public void setFailedRowNumber(long rowNumber) {
		this.failedRowNumber = rowNumber;
	}
	
	
	public long getFailedRowNumber() {
		return failedRowNumber;
	}
	
	public long getTimeTaken() {
		return timeTaken;
	}

	public void setTimeTaken(long timeTaken) {
		this.timeTaken = timeTaken;
	}

	public long getRowCount() {
		return rowCount;
	}

	public void setRowCount(long rowCount) {
		this.rowCount = rowCount;
	}

	public long getUsedMemory() {
		return usedMemory;
	}

	public void setUsedMemory(long usedMemory) {
		this.usedMemory = usedMemory;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public long getMatchedRowCount() {
		return matchedRowCount;
	}

	public void setMatchedRowCount(long matchedRowCount) {
		this.matchedRowCount = matchedRowCount;
	}
}
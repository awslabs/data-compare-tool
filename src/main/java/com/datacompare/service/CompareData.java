/**
 * Service class to compare the database table data between Source( Oracle) and Traget Database( like Postgres)
 *
 *
 * @author      Harnath Valeti
 * @author      Madhu Athinarapu
 * @version     1.0
 * @since       1.0
 */


package com.datacompare.service;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datacompare.util.DateUtil;

/**
 * 
 * Compare Data Class
 *
 */
public class CompareData implements Runnable {

	/** */
	public Logger logger = LoggerFactory.getLogger("CompareDataLog");
	
	private int chunkNo;

	private long failedRowNumber = 0;

	private List<String> failTuple = new ArrayList<String>();

	private long additionalDuplicateRowsNumber = 0;

	private Map<String,String> additionalDuplicateRows ;

	private Map<String,String> missingInTarget ;

	private Map<String,String> additionalInTarget ;

	private int numberOfChunks;

	private String result;
	
	private Map<String, String> sourceData;

	private Map<String, String> targetData;

	private long tempRowNumber = 0;
	
	private static final String DEFAULT_RESULT = "Completed";
	private boolean hasNoUniqueKey;

	public boolean isHasNoUniqueKey() {
		return hasNoUniqueKey;
	}

	public void setHasNoUniqueKey(boolean hasNoUniqueKey) {
		this.hasNoUniqueKey = hasNoUniqueKey;
	}

	/**
	 * 
	 * @param source
	 * @param target
	 * @param chunkNo
	 * @param numberOfChunks
	 */
	public CompareData(Map<String, String> source, Map<String, String> target, int chunkNo, int numberOfChunks,boolean hasNoUniqueKey) {

		this.sourceData = source;
		this.targetData = target;
		this.chunkNo = chunkNo;
		this.numberOfChunks = numberOfChunks;
		this.failedRowNumber = 0;
		this.tempRowNumber = 0;
		this.result = DEFAULT_RESULT;
		this.hasNoUniqueKey=hasNoUniqueKey;
		
		Thread.currentThread().setName("CompareData for ChunkNo " + chunkNo+1); 
	}

	public long getfailedRowCount() {
		return failedRowNumber;
	}

	public List<String> getFailTuple() {
		return failTuple;
	}

	public String getResult() {
		return result;
	}

	/**
	 * @return the sourceData
	 */
	public Map<String, String> getSourceData() {
		return sourceData;
	}

	/**
	 * @param sourceData the sourceData to set
	 */
	public void setSourceData(Map<String, String> sourceData) {
		this.sourceData = sourceData;
	}

	/**
	 * @return the targetData
	 */
	public Map<String, String> getTargetData() {
		return targetData;
	}

	/**
	 * @param targetData the targetData to set
	 */
	public void setTargetData(Map<String, String> targetData) {
		this.targetData = targetData;
	}


	public Map<String, String> getAdditionalInTarget() {
		return additionalInTarget;
	}

	public void setAdditionalInTarget(Map<String, String> additionalInTarget) {
		this.additionalInTarget = additionalInTarget;
	}

	public Map<String, String> getMissingInTarget() {
		return missingInTarget;
	}

	public void setMissingInTarget(Map<String, String> missingInTarget) {
		this.missingInTarget = missingInTarget;
	}

	public void setAdditionalDuplicateRows(Map<String, String> additionalDuplicateRows) {
		this.additionalDuplicateRows = additionalDuplicateRows;
	}
	public void run() {

		Thread.currentThread().setName("CompareData for ChunkNo " + chunkNo+1); 
		
		long start = System.currentTimeMillis();

		Map<String, String> tempSource = new HashMap<String, String>();
		Map<String, String> tempTarget = new HashMap<String, String>();

		List<String> tempSourceFailTuple = new ArrayList<String>();
		List<String> tempTargetFailTuple = new ArrayList<String>();

		compare(sourceData, targetData, tempSourceFailTuple, tempSource,hasNoUniqueKey);

		compareTaget(targetData, sourceData, tempTargetFailTuple, tempTarget ,hasNoUniqueKey);

		//this.failTuple.addAll(tempSourceFailTuple);
		//this.failTuple.addAll(tempTargetFailTuple);

		long end = System.currentTimeMillis();

		long timeTaken = end - start;

		String timeTakenStr = new DateUtil().timeDiffFormatted(timeTaken);
		timeTakenStr = (timeTakenStr != null) ? timeTakenStr : "";

		sourceData.clear();
		targetData.clear();

		sourceData.putAll(tempSource);
		targetData.putAll(tempTarget);

		StringBuilder info = new StringBuilder();
		
		info.append("Time taken for data comparision = ");
		info.append(timeTakenStr);
		info.append("\n");
		info.append("Finished analyzing chunkNo ");
		info.append(chunkNo+1);
		info.append(" out of ");
		info.append(numberOfChunks);
		info.append(" chunks");
		info.append("\n----------------------------------------------------------");
		
		logger.info(info.toString());
	}

	/**
	 * 
	 * @param data
	 * @param dataToCompare
	 * @param failTuple
	 * @param failedEntry
	 */
	private void compare(Map<String, String> data, Map<String, String> dataToCompare, List<String> failTuple,
			Map<String, String> failedEntry,boolean hasNoUniqueKey) {

		for (Map.Entry<String, String> entry : data.entrySet()) {

		boolean newRecord=false;
		this.tempRowNumber++;
		String key = entry.getKey();
		try {
			if(!hasNoUniqueKey){
			if (key != null && !failedEntry.containsKey(key)) {
				String content = entry.getValue();
				String dataToCompareContent = dataToCompare.get(key);
				int sourceCount = Collections.frequency(data.values(), content);
				int targetCount = Collections.frequency(dataToCompare.values(), content);
				//if it is mismatch
						if(sourceCount>targetCount ){
							//if(Collections.frequency(failedEntry.values(), content)<(sourceCount-targetCount)){
								for(int cnt=0; cnt<(sourceCount-targetCount) ; cnt++) {
									String failedContent = (content != null) ? content : "";
									this.result = "Failed";
									//failTuple.add(failedContent);
									this.failedRowNumber = this.tempRowNumber;
									if (failedEntry.containsKey(key) && newRecord) {
									 	key = key + "-DUP"+cnt;
									}
									failedEntry.put(key, failedContent);
									newRecord=true;
								}
							newRecord=false;
							}
						}
					}
			if(hasNoUniqueKey){
				String content = entry.getValue();
				String dataToCompareContent = dataToCompare.get(key);
				if (key != null && !failedEntry.containsValue(content)) {
					int sourceCount = Collections.frequency(data.values(), content);
					int targetCount = Collections.frequency(dataToCompare.values(), content);
					//if it is mismatch
					if(sourceCount>targetCount ){
						//if(Collections.frequency(failedEntry.values(), content)<(sourceCount-targetCount)){
						for(int cnt=0; cnt<(sourceCount-targetCount) ; cnt++) {
							String failedContent = (content != null) ? content : "";
							this.result = "Failed";
							//failTuple.add(failedContent);
							this.failedRowNumber = this.tempRowNumber;
							if (failedEntry.containsKey(key) && newRecord) {
								key = key + "-DUP"+cnt;
							}
							failedEntry.put(key, failedContent);
							newRecord=true;
						}
						newRecord=false;
					}
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}

	/**
	 *
	 * @param data
	 * @param dataToCompare
	 * @param failTuple
	 * @param failedEntry
	 */
	private void compareTaget(Map<String, String> data, Map<String, String> dataToCompare, List<String> failTuple,
						 Map<String, String> failedEntry,boolean hasNoUniqueKey) {

		for (Map.Entry<String, String> entry : data.entrySet()) {
			this.tempRowNumber++;
			boolean newRecord=false;
			String key = entry.getKey();
			String content = entry.getValue();
			try {
				if(!hasNoUniqueKey) {
					if (key != null && !failedEntry.containsKey(key) ) {
						String dataToCompareContent = dataToCompare.get(key);
						int sourceCount = Collections.frequency(data.values(), content);
						int targetCount = Collections.frequency(dataToCompare.values(), content);
						for (int cnt = 0; cnt < (sourceCount - targetCount); cnt++) {
							String failedContent = (content != null) ? content : "";
							this.result = "Failed";
							//failTuple.add(failedContent);
							this.failedRowNumber = this.tempRowNumber;
							if (failedEntry.containsKey(key) && newRecord) {
								//key=key+"-DUP"+cnt;
							}
							failedEntry.put(key, failedContent);
							newRecord = true;
						}
					}
				}
					else if(hasNoUniqueKey){
					if (key != null && !failedEntry.containsValue(content) ) {
						String dataToCompareContent = dataToCompare.get(key);
						int sourceCount = Collections.frequency(data.values(), content);
						int targetCount = Collections.frequency(dataToCompare.values(), content);
						for(int cnt=0; cnt<(sourceCount-targetCount) ; cnt++) {
							String failedContent = (content != null) ? content : "";
							this.result = "Failed";
							//failTuple.add(failedContent);
							this.failedRowNumber = this.tempRowNumber;
							if(failedEntry.containsKey(key) && newRecord ){
								key=key+"-DUP"+cnt;
							}
							failedEntry.put(key, failedContent);
							newRecord=true;
						}
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			newRecord=false;
		}
	}
}

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
	
	private int numberOfChunks;

	private String result;
	
	private Map<String, String> sourceData;

	private Map<String, String> targetData;

	private long tempRowNumber = 0;
	
	private static final String DEFAULT_RESULT = "Completed";

	/**
	 * 
	 * @param source
	 * @param target
	 * @param chunkNo
	 * @param numberOfChunks
	 */
	public CompareData(Map<String, String> source, Map<String, String> target, int chunkNo, int numberOfChunks) {

		this.sourceData = source;
		this.targetData = target;
		this.chunkNo = chunkNo;
		this.numberOfChunks = numberOfChunks;
		this.failedRowNumber = 0;
		this.tempRowNumber = 0;
		this.result = DEFAULT_RESULT;
		
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

	public void run() {

		Thread.currentThread().setName("CompareData for ChunkNo " + chunkNo+1); 
		
		long start = System.currentTimeMillis();

		Map<String, String> tempSource = new HashMap<String, String>();
		Map<String, String> tempTarget = new HashMap<String, String>();

		List<String> tempSourceFailTuple = new ArrayList<String>();
		List<String> tempTargetFailTuple = new ArrayList<String>();
		
		compare(sourceData, targetData, tempSourceFailTuple, tempSource);

		compare(targetData, sourceData, tempTargetFailTuple, tempTarget);

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
			Map<String, String> failedEntry) {

		for (Map.Entry<String, String> entry : data.entrySet()) {

			this.tempRowNumber++;

			String key = entry.getKey();

			try {

				if (key != null) {

					String content = entry.getValue();
					String dataToCompareContent = dataToCompare.get(key);

					//if it is mismatch
					if (!(content != null && dataToCompareContent != null && content.equals(dataToCompareContent))) {
                     // if target has the data
						if(dataToCompare.containsValue(content))
						{
							int sourceCount = Collections.frequency(data.values(), content);
							int targetCount = Collections.frequency(dataToCompare.values(), content);

							if(sourceCount>targetCount){

								 if(Collections.frequency(failedEntry.values(), content)<(sourceCount-targetCount)){

									 String failedContent = (content != null) ? content : "";

									 this.result = "Failed";

									 //failTuple.add(failedContent);

									 this.failedRowNumber = this.tempRowNumber;

									 failedEntry.put(key, failedContent);
								 }
							}
						}
						else {
							String failedContent = (content != null) ? content : "";

							this.result = "Failed";

							//failTuple.add(failedContent);

							this.failedRowNumber = this.tempRowNumber;

							failedEntry.put(key, failedContent);
						}
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
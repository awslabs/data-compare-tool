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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

	private Map<String, String> sourceFailedData;

	private Map<String, String> targetFailedData;

	private long tempRowNumber = 0;
	
	private static final String DEFAULT_RESULT = "Completed";
	private boolean hasNoUniqueKey;

	public boolean isHasNoUniqueKey() {
		return hasNoUniqueKey;
	}

	public void setHasNoUniqueKey(boolean hasNoUniqueKey) {
		this.hasNoUniqueKey = hasNoUniqueKey;
	}

	public boolean hasProvidedUniqueKey;

	public boolean isHasProvidedUniqueKey() {
		return hasProvidedUniqueKey;
	}

	public void setHasProvidedUniqueKey(boolean hasProvidedUniqueKey) {
		this.hasProvidedUniqueKey = hasProvidedUniqueKey;
	}

	/**
	 * 
	 * @param source
	 * @param target
	 * @param chunkNo
	 * @param numberOfChunks
	 */
	public CompareData(Map<String, String> source, Map<String, String> target, int chunkNo, int numberOfChunks,boolean hasNoUniqueKey,Map<String, String> sourceFailedData, Map<String, String> targetFailedData, boolean hasProvidedUniqueKey) {

		this.sourceData = source;
		this.targetData = target;
		this.chunkNo = chunkNo;
		this.numberOfChunks = numberOfChunks;
		this.failedRowNumber = 0;
		this.tempRowNumber = 0;
		this.result = DEFAULT_RESULT;
		this.hasNoUniqueKey=hasNoUniqueKey;
		this.sourceFailedData=sourceFailedData;
		this.targetFailedData=targetFailedData;
		this.hasProvidedUniqueKey=hasProvidedUniqueKey;
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

		compare(sourceData, targetData, tempSourceFailTuple, tempSource,hasNoUniqueKey,sourceFailedData,targetFailedData,chunkNo,isHasProvidedUniqueKey());

		compareTaget(targetData, sourceData, tempTargetFailTuple, tempTarget ,hasNoUniqueKey,sourceFailedData,targetFailedData,chunkNo,isHasProvidedUniqueKey());

		//this.failTuple.addAll(tempSourceFailTuple);
		//this.failTuple.addAll(tempTargetFailTuple);

		long end = System.currentTimeMillis();

		long timeTaken = end - start;
		long diffInSeconds = (end - start) / 1000;
		String timeTakenStr = new DateUtil().timeDiffFormatted(diffInSeconds);
		timeTakenStr = (timeTakenStr != null) ? timeTakenStr : "";

		//logger.info("Compare data- Missing Rows "+tempSource.size());
		//logger.info("Compare data- Add. Rows "+tempTarget.size());

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
			Map<String, String> failedEntry,boolean hasNoUniqueKey,Map<String, String> sourceFailedData, Map<String, String> targetFailedData,int chnCnt,boolean hasProvidedUniqueKey) {
		int cntt=0;
		//logger.info("Started compare source Chunk:"+chnCnt+"--Failed count-"+targetFailedData.size());
		for (Map.Entry<String, String> entry : data.entrySet()) {

		boolean newRecord=false;
		this.tempRowNumber++;
		String key = entry.getKey();
		try {
			if(!hasNoUniqueKey || hasProvidedUniqueKey){
			if (key != null && !failedEntry.containsKey(key)) {
				String content = entry.getValue();
				String dataToCompareContent = dataToCompare.get(key);
			    int sourceCount = Collections.frequency(data.keySet(), key);
				int targetCount = Collections.frequency(dataToCompare.keySet(), key);
				int targetMismatchCount = Collections.frequency(dataToCompare.keySet(), content);
				//if it is mismatch
						if(sourceCount>targetCount || !content.equalsIgnoreCase(dataToCompareContent)){
							//if(Collections.frequency(failedEntry.values(), content)<(sourceCount-targetCount)){
							//	for(int cnt=0; cnt<(sourceCount-targetCount) ; cnt++) {
									String failedContent = (content != null) ? content : "";
									this.result = "Failed";
									//failTuple.add(failedContent);
									this.failedRowNumber = this.tempRowNumber;
									failedEntry.put(key, failedContent);
									cntt++;
									newRecord=true;
								}
						else{ //clean up the verified data. this is already verified
							sourceData.remove(key);
							targetData.remove(key);
						}
						}
			}else if(hasNoUniqueKey){
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
							cntt++;
							newRecord=true;
						}
						newRecord=false;
					}
					else{
						sourceData.remove(key);
						targetData.remove(key);
					}
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
		logger.info("Compare- Missing Rows " +cntt);

}

	/**
	 *
	 * @param data
	 * @param dataToCompare
	 * @param failTuple
	 * @param failedEntry
	 */
	private void compareTaget(Map<String, String> data, Map<String, String> dataToCompare, List<String> failTuple,
						 Map<String, String> failedEntry,boolean hasNoUniqueKey, Map<String, String> sourceFailedData, Map<String, String> targetFailedData,int chnCnt,boolean hasProvidedUniqueKey) {
        int cntt=0;

		//.info("Started compare source Chunk:"+chnCnt+"--Failed count-"+targetFailedData.size());
		for (Map.Entry<String, String> entry : data.entrySet()) {
			this.tempRowNumber++;
				boolean newRecord=false;
				String key = entry.getKey();
				String content = entry.getValue();
			try{
			if (!hasNoUniqueKey || hasProvidedUniqueKey) {
				if (key != null && !failedEntry.containsKey(key)) {
					String dataToCompareContent = dataToCompare.get(key);
					int sourceCount = Collections.frequency(data.keySet(), key);
					int targetCount = Collections.frequency(dataToCompare.keySet(), key);
					if (sourceCount > targetCount) {
						//logger.info("Target----->Comoare info SRC content: " + content + " TGT content: +" + dataToCompareContent + "+ SRC CNT : " + sourceCount + "TGT CNT :" + targetCount );
						//	for (int cnt = 0; cnt < (sourceCount - targetCount); cnt++) {
						String failedContent = (content != null) ? content : "";
						this.result = "Failed";
						//failTuple.add(failedContent);
						this.failedRowNumber = this.tempRowNumber;
						failedEntry.put(key, failedContent);
						cntt++;
						newRecord = true;
					}
				}
			} else if (hasNoUniqueKey) {
				if (key != null && !failedEntry.containsValue(content)) {
					String dataToCompareContent = dataToCompare.get(key);
					int sourceCount = Collections.frequency(data.values(), content);
					int targetCount = Collections.frequency(dataToCompare.values(), content);
					for (int cnt = 0; cnt < (sourceCount - targetCount); cnt++) {
						String failedContent = (content != null) ? content : "";
						this.result = "Failed";
						//failTuple.add(failedContent);
						this.failedRowNumber = this.tempRowNumber;
						if (failedEntry.containsKey(key) && newRecord) {
							key = key + "-DUP" + cnt;
						}
						failedEntry.put(key, failedContent);
						cntt++;
						newRecord = true;
					}
				}
			}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			newRecord=false;
		}
		logger.info(" Compare- Add. Rows" +cntt);
	}


	private void finalValidation(Map<String, String> data, Map<String, String> targetCountList, boolean hasNoUniqueKey) {
		//logger.info("Started the source chunk mismatch");
		ArrayList list= new ArrayList();
		for (Map.Entry<String, String> entry : data.entrySet()) {

			boolean newRecord=false;
			String key = entry.getKey();
			try {
				if(!hasNoUniqueKey || hasProvidedUniqueKey){
					if (key != null && targetCountList.containsKey(key)) {
						String content = entry.getValue();
						String dataToCompareContent = targetCountList.get(key);
						int sourceCount = Collections.frequency(data.values(), content);
						int targetCount = Collections.frequency(targetCountList.values(), content);
						//if it is mismatch
						if(sourceCount>0 && targetCount>0 ){
							//if(Collections.frequency(failedEntry.values(), content)<(sourceCount-targetCount)){
							list.add(key);
							String removeKey=getKeyForValue( targetCountList,content);
							if(removeKey!=null)
								targetCountList.remove(removeKey);
						}
					}

				}
				if(hasNoUniqueKey){
					String content = entry.getValue();
					String dataToCompareContent = targetCountList.get(key);
					int sourceCount = Collections.frequency(data.values(), content);
					int targetCount = Collections.frequency(targetCountList.values(), content);
					//if it is mismatch
					if(sourceCount>0 && targetCount>0 ){
						//if(Collections.frequency(failedEntry.values(), content)<(sourceCount-targetCount)){
						list.add(key);
						String removeKey=getKeyForValue( targetCountList,content);
						if(removeKey!=null)
							targetCountList.remove(removeKey);
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		//logger.info("Processed the source chunk mismatch");
		removeData(list,data);
	}

	private void removeData(ArrayList list, Map<String, String> data) {
		//.info("started data removal");
		if(list.size()>0){
			for(int i=0; i< list.size(); i++)
			{
				data.remove(list.get(i));
			}
		}
		//.info("Processed data removal");
	}

	private String getKeyForValue(Map<String, String> targetCountList, String content) {
		for (Map.Entry<String, String> entry : targetCountList.entrySet()) {

			boolean newRecord = false;
			String key = entry.getKey();
			String value = entry.getValue();
			if (value.equalsIgnoreCase(content))
				return key;
		}
		return null;
	}
}

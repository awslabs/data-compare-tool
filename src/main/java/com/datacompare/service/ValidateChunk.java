package com.datacompare.service;

import com.datacompare.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ValidateChunk implements Runnable {

    public Logger logger = LoggerFactory.getLogger("CompareDataLog");

    private Map<String, String> sourceFailedData;

    private Map<String, String> targetFailedData;

    public boolean isHasProvidedUniqueKey() {
        return hasProvidedUniqueKey;
    }

    public void setHasProvidedUniqueKey(boolean hasProvidedUniqueKey) {
        this.hasProvidedUniqueKey = hasProvidedUniqueKey;
    }

    private boolean hasProvidedUniqueKey;

    public Map<String, String> getProcessMap() {
        return processMap;
    }

    private Map<String, String> processMap;

    private static final String DEFAULT_RESULT = "Completed";
    private boolean hasNoUniqueKey;

    public boolean isHasNoUniqueKey() {
        return hasNoUniqueKey;
    }

    public void setHasNoUniqueKey(boolean hasNoUniqueKey) {
        this.hasNoUniqueKey = hasNoUniqueKey;
    }

    public int chunkNum;

    /**
     * @return the sourceData
     */
    public Map<String, String> getSourceFailedData() {
        return sourceFailedData;
    }

    /**
     * @param sourceData the sourceData to set
     */
    public void setSourceFailedData(Map<String, String> sourceData) {
        this.sourceFailedData = sourceFailedData;
    }

    /**
     * @return the targetData
     */
    public Map<String, String> setTargetFailedData() {
        return targetFailedData;
    }

    /**
     * @param targetData the targetData to set
     */
    public void setTargetFailedData(Map<String, String> targetData) {
        this.targetFailedData = targetFailedData;
    }



    public ValidateChunk( Map<String, String> processMap, Map<String, String> sourceFailedData, Map<String, String> targetFailedData,  boolean hasNoUniqueKey,boolean hasProvidedUniqueKey,int chunkNum) {

        this.processMap = processMap;
        this.hasNoUniqueKey = hasNoUniqueKey;
        this.sourceFailedData = sourceFailedData;
        this.targetFailedData = targetFailedData;
        this.hasProvidedUniqueKey=hasProvidedUniqueKey;
        this.chunkNum=chunkNum;

        Thread.currentThread().setName("validate the  ChunkNo ");
    }

    public void run() {

        Thread.currentThread().setName("validate the  ChunkNo "+chunkNum);

        long start = System.currentTimeMillis();

        Map<String, String> tempSource = new HashMap<String, String>();
        Map<String, String> tempTarget = new HashMap<String, String>();

        finalValidation();
        //finalValidation(targetFailedData, sourceFailedData, hasNoUniqueKey,tempTarget);

        long end = System.currentTimeMillis();

        long timeTaken = end - start;

        String timeTakenStr = new DateUtil().timeDiffFormatted(timeTaken);
        timeTakenStr = (timeTakenStr != null) ? timeTakenStr : "";

        sourceFailedData.putAll(tempSource);
        sourceFailedData.putAll(tempTarget);

        tempSource.clear();
        tempSource.clear();


    }

    private void finalValidation( ) {
        //logger.info("Started the chunk mismatch validation "+chunkNum);
        int removedCount=0;
            try {
                if (!hasNoUniqueKey || hasProvidedUniqueKey ) {
                    for (Map.Entry<String, String> entry : processMap.entrySet()) {
                        String key = entry.getKey();
                        String content = entry.getValue();
                        if (key != null) {
                            String dataToCompareContent = targetFailedData.get(key);
                            if (content.equalsIgnoreCase(dataToCompareContent)) {
                                targetFailedData.remove(key);
                                sourceFailedData.remove(key);
                                removedCount++;
                            }
                        }
                    }
                }else  if (hasNoUniqueKey) {
                    for (Map.Entry<String, String> entry : processMap.entrySet()) {
                        String key = entry.getKey();
                        String content = entry.getValue();
                        String dataToCompareContent = targetFailedData.get(key);
                        if (targetFailedData!=null && targetFailedData.containsValue(content)) {
                            //if(Collections.frequency(failedEntry.values(), content)<(sourceCount-targetCount)){
                            // list.add(key);
                            if( !content.equals(dataToCompareContent)) {
                                String removeKey = getKeyForValue(targetFailedData, content);
                                if(removeKey!=null)
                                targetFailedData.remove(removeKey);
                            }else
                            {
                                targetFailedData.remove(key);
                            }
                                sourceFailedData.remove(key);
                                removedCount++;
                            }
                        }
                    }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        //logger.info("Processed the source chunk-"+chunkNum+" removed count " +removedCount);
        //removeData(list, data);
    }


    private String getKeyForValue(Map<String, String> targetCountList, String content) {
        if(content!=null) {
            for (Map.Entry<String, String> entry : targetCountList.entrySet()) {

                boolean newRecord = false;
                String key = entry.getKey();
                String value = entry.getValue();
                if (value != null && value.equalsIgnoreCase(content))
                    return key;
            }
        }
        return null;
    }
}

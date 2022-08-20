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



    public ValidateChunk( Map<String, String> processMap, Map<String, String> sourceFailedData, Map<String, String> targetFailedData,  boolean hasNoUniqueKey,boolean hasProvidedUniqueKey) {

        this.processMap = processMap;
        this.hasNoUniqueKey = hasNoUniqueKey;
        this.sourceFailedData = sourceFailedData;
        this.targetFailedData = targetFailedData;
        this.hasProvidedUniqueKey=hasProvidedUniqueKey;

        Thread.currentThread().setName("validate the  ChunkNo ");
    }

    public void run() {

        Thread.currentThread().setName("validate the  ChunkNo ");

        long start = System.currentTimeMillis();

        Map<String, String> tempSource = new HashMap<String, String>();
        Map<String, String> tempTarget = new HashMap<String, String>();

        List<String> tempSourceFailTuple = new ArrayList<String>();
        List<String> tempTargetFailTuple = new ArrayList<String>();

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


        StringBuilder info = new StringBuilder();

    }

    private void finalValidation( ) {
        logger.info("Started the chunk mismatch validation");
        int removedCount=0;
        ArrayList list = new ArrayList();
        for (Map.Entry<String, String> entry : processMap.entrySet()) {

            boolean newRecord = false;
            String key = entry.getKey();
            String content = entry.getValue();
        //    logger.info("FINAL REMOVEAL------KEY--o--" + key);
         //   logger.info("FINAL REMOVEAL------content--o--" + content);
            try {
                if (!hasNoUniqueKey || hasProvidedUniqueKey ) {
                    if (key != null && targetFailedData.containsKey(key)) {

                        String dataToCompareContent = targetFailedData.get(key);
                        int sourceCount = Collections.frequency(processMap.keySet(), key);
                        int targetCount = Collections.frequency(targetFailedData.keySet(), key);
                        //if it is mismatch
                        if (sourceCount > 0 && targetCount > 0) {
                                targetFailedData.remove(key);
                                sourceFailedData.remove(key);
                                removedCount++;
                            }
                        }
                }else  if (hasNoUniqueKey) {
                    content = entry.getValue();
                    int sourceCount = Collections.frequency(processMap.values(), content);
                    int targetCount = Collections.frequency(targetFailedData.values(), content);
               //     logger.info("FINAL REMOVEAL------FEQ----" + sourceCount);
                //    logger.info("FINAL REMOVEAL------FEQ----" + targetCount);
                    //if it is mismatch
                    if (sourceCount > 0 && targetCount > 0) {
                        //if(Collections.frequency(failedEntry.values(), content)<(sourceCount-targetCount)){
                       // list.add(key);
                        String removeKey = getKeyForValue(targetFailedData, content);
                   //     logger.info("FINAL REMOVEAL------KEY----" + removeKey);
                        if (removeKey != null) {
                            targetFailedData.remove(removeKey);
                            sourceFailedData.remove(key);
                            removedCount++;
                           // logger.info("FINAL REMOVEAL----------" + content);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.info("Processed the source chunk mismatch- src filed count" +targetFailedData.size() + "Target Failed Cnt "+ targetFailedData.size()+ " removed count " +removedCount);
        //removeData(list, data);
    }

    private void removeData(ArrayList list, Map<String, String> data) {
        //logger.info("started data removal");
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
               // logger.info("FINAL REMOVEAL------KEY----" + list.get(i));
                data.remove(list.get(i));
               // logger.info("FINAL REMOVEAL----------" + data.get(list.get(i)));
            }
        }
        logger.info("Processed data removal");
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

    public void setProcessMap(Map<String, String> stringStringMap) {
    }
}

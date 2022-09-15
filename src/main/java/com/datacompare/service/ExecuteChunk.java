/**
 * Service class to execute the chunk. Chunk is a set of DB records to compare.
 * This runs a thread for multiple chunks
 *
 *
 * @author      Harnath Valeti
 * @author      Madhu Athinarapu
 * @version     1.0
 * @since       1.0
 */
package com.datacompare.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datacompare.model.AppProperties;
import com.datacompare.model.TableColumnMetadata;
import com.datacompare.util.MemoryUtil;

public class ExecuteChunk implements Runnable {

	/** */
	public Logger logger = LoggerFactory.getLogger("ExecuteChunkLog");

	private int chunkNo;
	private List<String> failTuple;
	private int numberOfChunks;
	private String result;
	private String sourceChunk;
	//private Connection sourceConnection = null;

	private String sourceDBType;
	private String sourceSql;

	private Map<String, TableColumnMetadata> sourceTableMetadata = null;
	private Map<String, TableColumnMetadata> targetTableMetadata = null;
	private String targetChunk;

	//private Connection targetConnection = null;

	private String targetDBType;
	private String targetSql;
	
	private Map<String, String> sourceData;
	private Map<String, String> targetData;
	
	private List<Long> sourceCount;
	private List<Long> targetCount;
	
	private List<Long> sourceTimeTaken;
	private List<Long> targetTimeTaken;
	
	private AppProperties appProperties;

	private boolean hasNoUniqueKey;

	public boolean isHasProvidedUniqueKey() {
		return hasProvidedUniqueKey;
	}

	public void setHasProvidedUniqueKey(boolean hasProvidedUniqueKey) {
		this.hasProvidedUniqueKey = hasProvidedUniqueKey;
	}

	private boolean hasProvidedUniqueKey;
	/**
	 * 
	 * @param sourceDBType
	 * @param targetDBType
	 * @param sourceChunk
	 * @param targetChunk
	 * @param sourceSql
	 * @param targetSql
	 * @param chunkNo
	 * @param numberOfChunks
	 * @param sourceConnection
	 * @param targetConnection
	 * @param sourceTableMetadata
	 * @param targetTableMetadata
	 * @param appProperties
	 */
	public ExecuteChunk(String sourceDBType, String targetDBType, String sourceChunk, String targetChunk,
			String sourceSql, String targetSql, int chunkNo, int numberOfChunks, /*Connection sourceConnection,
			Connection targetConnection,*/ Map<String, TableColumnMetadata> sourceTableMetadata,
			Map<String, TableColumnMetadata> targetTableMetadata, AppProperties appProperties,boolean hasProvidedUniqueKey) {

		setSourceDBType(sourceDBType);
		setTargetDBType(targetDBType);
		setSourceChunk(sourceChunk);
		setTargetChunk(targetChunk);
		setSourceSql(sourceSql);
		setTargetSql(targetSql);
		setAppProperties(appProperties);
		setChunkNo(chunkNo);
		setNumberOfChunks(numberOfChunks);
		//setSourceConnection(sourceConnection);
		//setTargetConnection(targetConnection);
		setSourceTableMetadata(sourceTableMetadata);
		setTargetTableMetadata(targetTableMetadata);
		setHasProvidedUniqueKey(hasProvidedUniqueKey);
		
		Thread.currentThread().setName("Executing Chunk No " + getChunkNo()+1); 

		StringBuilder info = new StringBuilder();
		
		info.append("SOURCE Chunk No = ");
		info.append((getChunkNo()+1));
		info.append(" ::: ");
		info.append(getSourceChunk());
		
		logger.info(info.toString());

		info = new StringBuilder();
		
		info.append("TARGET Chunk No = ");
		info.append((getChunkNo()+1));
		info.append(" ::: ");
		info.append(getTargetChunk());
		
		logger.info(info.toString());
	}

	/**
	 * @return the chunkNo
	 */
	public int getChunkNo() {
		return chunkNo;
	}

	/**
	 * @return the failTuple
	 */
	public List<String> getFailTuple() {
		return failTuple;
	}

	/**
	 * @return the numberOfChunks
	 */
	public int getNumberOfChunks() {
		return numberOfChunks;
	}

	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * @return the sourceChunk
	 */
	public String getSourceChunk() {
		return sourceChunk;
	}

	/**
	 * @return the sourceConnection
	 */
	/*public Connection getSourceConnection() {
		return sourceConnection;
	}*/

	/**
	 * @return the sourceDBType
	 */
	public String getSourceDBType() {
		return sourceDBType;
	}

	/**
	 * @return the sourceSql
	 */
	public String getSourceSql() {
		return sourceSql;
	}

	/**
	 * @return the sourceTableMetadata
	 */
	public Map<String, TableColumnMetadata> getSourceTableMetadata() {
		return sourceTableMetadata;
	}

	/**
	 * @return the targetTableMetadata
	 */
	public Map<String, TableColumnMetadata> getTargetTableMetadata() {
		return targetTableMetadata;
	}

	/**
	 * @return the targetChunk
	 */
	public String getTargetChunk() {
		return targetChunk;
	}

	/**
	 * @return the targetConnection
	 */
	/*public Connection getTargetConnection() {
		return targetConnection;
	}*/

	/**
	 * @return the targetDBType
	 */
	public String getTargetDBType() {
		return targetDBType;
	}

	/**
	 * @return the targetSql
	 */
	public String getTargetSql() {
		return targetSql;
	}

	/**
	 * @param chunkNo the chunkNo to set
	 */
	public void setChunkNo(int chunkNo) {
		this.chunkNo = chunkNo;
	}

	/**
	 * @param failTuple the failTuple to set
	 */
	public void setFailTuple(List<String> failTuple) {
		this.failTuple = failTuple;
	}

	/**
	 * @param numberOfChunks the numberOfChunks to set
	 */
	public void setNumberOfChunks(int numberOfChunks) {
		this.numberOfChunks = numberOfChunks;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * @param sourceChunk the sourceChunk to set
	 */
	public void setSourceChunk(String sourceChunk) {
		this.sourceChunk = sourceChunk;
	}

	/**
	 * @param sourceConnection the sourceConnection to set
	 */
	/*public void setSourceConnection(Connection sourceConnection) {
		this.sourceConnection = sourceConnection;
	}*/

	/**
	 * @param sourceDBType the sourceDBType to set
	 */
	public void setSourceDBType(String sourceDBType) {
		this.sourceDBType = sourceDBType;
	}

	/**
	 * @param sourceSql the sourceSql to set
	 */
	public void setSourceSql(String sourceSql) {
		this.sourceSql = sourceSql;
	}

	/**
	 * @param sourceTableMetadata the sourceTableMetadata to set
	 */
	public void setSourceTableMetadata(Map<String, TableColumnMetadata> sourceTableMetadata) {
		this.sourceTableMetadata = sourceTableMetadata;
	}

	/**
	 * @param targetTableMetadata the targetTableMetadata to set
	 */
	public void setTargetTableMetadata(Map<String, TableColumnMetadata> targetTableMetadata) {
		this.targetTableMetadata = targetTableMetadata;
	}

	/**
	 * @param targetChunk the targetChunk to set
	 */
	public void setTargetChunk(String targetChunk) {
		this.targetChunk = targetChunk;
	}

	/**
	 * @param targetConnection the targetConnection to set
	 */
	/*public void setTargetConnection(Connection targetConnection) {
		this.targetConnection = targetConnection;
	}*/

	/**
	 * @param targetDBType the targetDBType to set
	 */
	public void setTargetDBType(String targetDBType) {
		this.targetDBType = targetDBType;
	}

	/**
	 * @param targetSql the targetSql to set
	 */
	public void setTargetSql(String targetSql) {
		this.targetSql = targetSql;
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

	/**
	 * @return the sourceCount
	 */
	public List<Long> getSourceCount() {
		return sourceCount;
	}

	/**
	 * @param sourceCount the sourceCount to set
	 */
	public void setSourceCount(List<Long> sourceCount) {
		this.sourceCount = sourceCount;
	}

	/**
	 * @return the targetCount
	 */
	public List<Long> getTargetCount() {
		return targetCount;
	}

	/**
	 * @param targetCount the targetCount to set
	 */
	public void setTargetCount(List<Long> targetCount) {
		this.targetCount = targetCount;
	}

	/**
	 * @return the sourceTimeTaken
	 */
	public List<Long> getSourceTimeTaken() {
		return sourceTimeTaken;
	}

	/**
	 * @param sourceTimeTaken the sourceTimeTaken to set
	 */
	public void setSourceTimeTaken(List<Long> sourceTimeTaken) {
		this.sourceTimeTaken = sourceTimeTaken;
	}

	/**
	 * @return the targetTimeTaken
	 */
	public List<Long> getTargetTimeTaken() {
		return targetTimeTaken;
	}

	/**
	 * @param targetTimeTaken the targetTimeTaken to set
	 */
	public void setTargetTimeTaken(List<Long> targetTimeTaken) {
		this.targetTimeTaken = targetTimeTaken;
	}

	/**
	 * @return the appProperties
	 */
	public AppProperties getAppProperties() {
		return appProperties;
	}

	/**
	 * @param appProperties the appProperties to set
	 */
	public void setAppProperties(AppProperties appProperties) {
		this.appProperties = appProperties;
	}

	public boolean isHasNoUniqueKey() {
		return hasNoUniqueKey;
	}

	public void setHasNoUniqueKey(boolean hasNoUniqueKey) {
		this.hasNoUniqueKey = hasNoUniqueKey;
	}

	@Override
	public void run() {
		
		try {

			new MemoryUtil().displayMemoryInfo();
			
			Thread.currentThread().setName("Executing Chunk No " + getChunkNo()+1);
			FetchData fetchSourceData = new FetchData(getSourceDBType(), null, getSourceSql(), getSourceChunk(),
					/*getSourceConnection(),*/ true, getSourceTableMetadata(), null, getAppProperties(),getChunkNo());
			fetchSourceData.setTimeTaken(getSourceTimeTaken());
			FetchData fetchTargetData = new FetchData(getTargetDBType(), getSourceDBType(), getTargetSql(),
					getTargetChunk(), /*getTargetConnection(),*/ false, getTargetTableMetadata(), getSourceTableMetadata(),
					getAppProperties(),getChunkNo());
			fetchTargetData.setTimeTaken(getTargetTimeTaken());
			ExecutorService executor = Executors.newFixedThreadPool(2);
			executor.execute(fetchSourceData); 
			executor.execute(fetchTargetData);
			executor.shutdown();
			while (!executor.isTerminated()) {
	        }
			Long srcCnt = Long.valueOf(fetchSourceData.getHashMap().size());
			getSourceCount().add(srcCnt);
			Long tarCnt = Long.valueOf(fetchTargetData.getHashMap().size());
			getTargetCount().add(tarCnt);
			CompareData compareData = new CompareData(fetchSourceData.getHashMap(), fetchTargetData.getHashMap(),
					getChunkNo(), getChunkNo(),isHasNoUniqueKey(),getSourceData(),getTargetData(),isHasProvidedUniqueKey());
			
			executor = Executors.newFixedThreadPool(1);
			executor.execute(compareData);
			executor.shutdown();
			while (!executor.isTerminated()) {
	        }
			String result = compareData.getResult();
			
			if(!"Completed".equals(result)) {
				setResult(result);
			}
			List<String> failTuple = compareData.getFailTuple();
			getFailTuple().addAll(failTuple);

			Map<String, String> sourceData = compareData.getSourceData();
			getSourceData().putAll(sourceData);
			Map<String, String> targetData = compareData.getTargetData();
			getTargetData().putAll(targetData);

			ExecutorService validationExecutor = Executors.newFixedThreadPool(1);
			ValidateChunk executeChunk = new ValidateChunk(getSourceData(),getSourceData(), getTargetData(), hasNoUniqueKey,isHasProvidedUniqueKey(),getChunkNo());
			validationExecutor.execute(executeChunk);
			validationExecutor.shutdown();
			while (!validationExecutor.isTerminated()) {
			}

			//logger.info("Execute Chunk- New Missing "+sourceData.size());
			//logger.info("Execute Chunk- Add. Rows "+targetData.size());
			//logger.info("Execute Chunk- New Missing Total"+getSourceData().size());
			//logger.info("Execute Chunk- Add. Rows Total"+getTargetData().size());

			fetchSourceData.getHashMap().clear();
			fetchTargetData.getHashMap().clear();
			fetchSourceData = null;
			fetchTargetData = null;
			executor = null;

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
		}
	}
}

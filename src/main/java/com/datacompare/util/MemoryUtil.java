package com.datacompare.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryUtil {

	public static Logger logger = LoggerFactory.getLogger("MemoryUtil");
	
	public void displayMemoryInfo() {

		int mb = 1024 * 1024;

		// get Runtime instance
		Runtime instance = Runtime.getRuntime();
		
		StringBuilder info = new StringBuilder();

		//info.append("***** Heap utilization statistics [MB] *****");

		// available memory
		//info.append("\nTotal Memory: " + instance.totalMemory() / mb);

		// free memory
		info.append("Free Memory: " + instance.freeMemory() / mb);

		// used memory
		//info.append("\nUsed Memory: " + (instance.totalMemory() - instance.freeMemory()) / mb);

		// Maximum available memory
		//info.append("\nMax Memory: " + instance.maxMemory() / mb);
		
		logger.info(info.toString()); 
	}
}

package com.datacompare.util;

public class FormatUtil {

	public static final long MEGABYTE = 1024L * 1024L;
	
	/**
	 * 
	 * @param bytes
	 * @return
	 */
	public static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }
	
	/**
	 * 
	 * @param propVal
	 * @param defaultVal
	 * @param maxVal
	 * @return
	 */
	public static int getIntValue(String propVal, int defaultVal, int maxVal) {

		try {
			
			int pInt = Integer.parseInt(propVal);
			
			return (maxVal > 0 && pInt > maxVal) ? maxVal : pInt;
			
		} catch (NumberFormatException e) {
			
			return defaultVal;
		}
	}
}
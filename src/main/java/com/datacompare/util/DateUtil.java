package com.datacompare.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

	public static SimpleDateFormat formatter1 = new SimpleDateFormat("YYYY-MM-dd_hh-mm");
	
	public static SimpleDateFormat formatter2 = new SimpleDateFormat("dd MMM YYYY hh:mm");
	
	/**
	 * 
	 * @param date
	 * @return
	 */
	public String getAppendDateToFileName(Date date) {
		
		return formatter1.format(date);
	}
	
	/**
	 * 
	 * @param date
	 * @return
	 */
	public String getExecutionDate(Date date) {
		
		return formatter2.format(date);
	}
	/**
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public String timeDiffFormatted(long start, long end) {
		
		if(start > 0 && end > 0 && end > start) {

		    long diffInSeconds = (end - start) / 1000;
		    
		    return timeDiffFormatted(diffInSeconds);
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param diffInSeconds
	 * @return
	 */
	public String timeDiffFormatted(long diffInSeconds) {

		String timeFormatted = null;
		
		if(diffInSeconds > 0) {
			
		    long diff[] = new long[] { 0, 0, 0, 0 };
		    
		    diff[3] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds); /* sec */
		    diff[2] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds; /* min */
		    diff[1] = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds; /* hours */
		    diff[0] = (diffInSeconds = (diffInSeconds / 24)); /* days */

		    timeFormatted = String.format(
		        "%d day%s, %d hour%s, %d minute%s, %d second%s",
		        diff[0],
		        diff[0] > 1 ? "s" : "",
		        diff[1],
		        diff[1] > 1 ? "s" : "",
		        diff[2],
		        diff[2] > 1 ? "s" : "",
		        diff[3],
		        diff[3] > 1 ? "s" : "");	
			
		} else if(diffInSeconds == 0) {
			
			timeFormatted = "0 seconds";
		}
		
		return timeFormatted;
	}
}
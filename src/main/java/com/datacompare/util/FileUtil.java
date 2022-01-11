package com.datacompare.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {

	public static Logger logger = LoggerFactory.getLogger("FileUtil");
	
	/**
	 * 
	 * @param data
	 * @param fileName
	 * @param outputFolderPath
	 */
	public void writeDataToFile(StringBuilder data, String fileName, String outputFolderPath) {
		
        PrintWriter pw = null;
         
        try {
        	
			File file = (outputFolderPath != null && !outputFolderPath.isEmpty()) ? new File(outputFolderPath, fileName)
					: new File(fileName);
  	        	
        	pw = new PrintWriter(file);

        	pw.write(data.toString());
            
            logger.info(fileName + " written successfully");
            
        } catch (Exception e) {
        	
        	logger.error(e.getMessage(), e);
            
        } finally {
			
        	if(pw != null) {
        		
    			pw.close();
        	}
		}		
	}
	
	/**
	 * 
	 * @param directoryName
	 * @return
	 */
	public String getLatestResultFile(String directoryName) {
		
		File directory = new File(directoryName);
		
		File [] files = directory.listFiles(new FilenameFilter() {
			
		    @Override
		    public boolean accept(File file, String name) {
		        return name.startsWith("data_comparison_result_") && name.endsWith(".html");
		    }
		});	
		
		if (files == null || files.length == 0) {
	        return null;
	    }
		
		File lastModifiedFile = files[0];
		
	    for (int i = 1; i < files.length; i++) {
	    	
	       if (lastModifiedFile.lastModified() < files[i].lastModified()) {
	           lastModifiedFile = files[i];
	       }
	    }	
	    
	    return lastModifiedFile.getName();
	}
}
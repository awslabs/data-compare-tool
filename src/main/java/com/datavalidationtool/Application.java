
package com.datavalidationtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.sql.Timestamp;
import java.util.Arrays;

@EnableSwagger2
@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class Application implements ApplicationRunner {
	
	private Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main( String[] args ) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String timeStampStr=timestamp.toString();
		System.setProperty("logtimestamp", timeStampStr);
		SpringApplication app = new SpringApplication(Application.class);
		app.run(args);

	}
 
    @Override
    public void run(ApplicationArguments args) throws Exception {
    	long startTime=System.currentTimeMillis();
    	logger.debug("Command-line arguments: {}", Arrays.toString(args.getSourceArgs()));
        logger.debug("Non Option Args: {}", args.getNonOptionArgs());
        logger.debug("Option Names: {}", args.getOptionNames());


    }

}

package com.datavalidationtool.dao;

import com.datavalidationtool.model.AwsSecret;
import com.datavalidationtool.util.AWSUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.SQLException;

@Configuration
@Component
public class DataSource {

    @Autowired
    private AWSUtil awsUtil;
    public Logger logger = LoggerFactory.getLogger("DataSource");
    private HikariDataSource hkDataSource;
    private boolean isPoolInitialized = false;
    public boolean isPoolInitialized() {
        return this.isPoolInitialized;
    }
    public DataSource() {
    }
    public Connection getDBConnection() throws SQLException {
        logger.info("Source DB Pool Size===" + hkDataSource.getMaximumPoolSize());
        return hkDataSource.getConnection();
    }
    @PostConstruct
    public synchronized void initializePool() throws JsonProcessingException {
        if (!isPoolInitialized) {
            String secret = awsUtil.fetchValidationDetailsFromSSM();
            ObjectMapper mapper = new ObjectMapper();
            AwsSecret awsSecret = mapper.readValue(secret, AwsSecret.class);
            hkDataSource = new HikariDataSource();
            hkDataSource.setJdbcUrl("jdbc:postgresql://"+awsSecret.getHost()+":"+awsSecret.getPort()+"/"+awsSecret.getDbname()+"?tcpKeepAlive=true");
            hkDataSource.setDriverClassName("org.postgresql.Driver");
            hkDataSource.setUsername(awsSecret.getUsername());
            hkDataSource.setPassword(awsSecret.getPassword());
            hkDataSource.setConnectionTestQuery("select 1");
            hkDataSource.setMinimumIdle(30);
            hkDataSource.setMaximumPoolSize(30);
            logger.info("Pool initialized in DS===" + hkDataSource.getMaximumPoolSize());
            this.isPoolInitialized=true;
        }
        else{
            throw new RuntimeException("Pool Already Initialized.");
        }
    }
}

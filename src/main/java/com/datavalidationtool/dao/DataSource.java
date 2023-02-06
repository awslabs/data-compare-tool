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

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.SQLException;

@Configuration
@Component
public class DataSource {

    @Autowired
    private AWSUtil awsUtil;

    public Logger logger = LoggerFactory.getLogger("DataSource");

    private HikariDataSource dataSource;

    private boolean isPoolInitialized = false;

    public boolean isPoolInitialized() {
        return isPoolInitialized;
    }

    public DataSource() {
    }



    public Connection getDBConnection() throws SQLException {
        logger.info("Source DB Pool Size===" + dataSource.getMaximumPoolSize());
        return dataSource.getConnection();
    }

    @PostConstruct
    public synchronized void initializePool() throws JsonProcessingException {
        if (!isPoolInitialized) {
            String secret = awsUtil.fetchValidationDetailsFromSSM();
            ObjectMapper mapper = new ObjectMapper();
            AwsSecret awsSecret = mapper.readValue(secret, AwsSecret.class);

            dataSource = new HikariDataSource();
            dataSource.setJdbcUrl("jdbc:postgresql://"+awsSecret.getHost()+":"+awsSecret.getPort()+"/"+awsSecret.getDbname()+"?tcpKeepAlive=true");
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setUsername(awsSecret.getUsername());
            dataSource.setPassword(awsSecret.getPassword());
            dataSource.setConnectionTestQuery("select 1");
            dataSource.setMinimumIdle(30);
            dataSource.setMaximumPoolSize(30);

            this.isPoolInitialized=true;
        }
        else{
            throw new RuntimeException("Pool Already Initialized.");
        }
    }
}

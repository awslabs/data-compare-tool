package com.datavalidationtool.util;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class AWSUtil {

    @Autowired
    private Environment env;

    private Logger logger = LoggerFactory.getLogger(AWSUtil.class);

    public  String fetchValidationDetailsFromSSM(){

        String secretName = env.getProperty("secretname");
        String region = env.getProperty("region");

        String endpoint =("secretsmanager." + region + ".amazonaws.com");
        AwsClientBuilder.EndpointConfiguration config = new AwsClientBuilder.EndpointConfiguration(endpoint, region);
        AWSSecretsManagerClientBuilder clientBuilder = AWSSecretsManagerClientBuilder.standard();
        clientBuilder.setEndpointConfiguration(config);
        AWSSecretsManager client = clientBuilder.build();
        String secret = null;
        ByteBuffer binarySecretData;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
        GetSecretValueResult getSecretValueResponse = null;
        try {
            getSecretValueResponse = client.getSecretValue(getSecretValueRequest);

        } catch(ResourceNotFoundException e) {
            logger.error("Could not found the secret {}",secretName);
        } catch (InvalidRequestException e) {
            logger.error("Request was invalid : ",e.getMessage());
        } catch (InvalidParameterException e) {
            logger.error("The request had invalid params: ",e.getMessage());
        }

        if(getSecretValueResponse != null) {
            if(getSecretValueResponse.getSecretString() != null) {
                secret = getSecretValueResponse.getSecretString();
            }
            else {
                binarySecretData = getSecretValueResponse.getSecretBinary();
            }
        }

        logger.debug("Secret Name : {} \t Secret Value : {} \n",secretName,secret);
        return secret;
    }

    public  String fetchSourceDBDetailsFromSSM(){
        String secretName = env.getProperty("srcsecretname");
        String region = env.getProperty("region");

        String endpoint =("secretsmanager." + region + ".amazonaws.com");
        AwsClientBuilder.EndpointConfiguration config = new AwsClientBuilder.EndpointConfiguration(endpoint, region);
        AWSSecretsManagerClientBuilder clientBuilder = AWSSecretsManagerClientBuilder.standard();
        clientBuilder.setEndpointConfiguration(config);
        AWSSecretsManager client = clientBuilder.build();
        String secret = null;
        ByteBuffer binarySecretData;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
        GetSecretValueResult getSecretValueResponse = null;
        try {
            getSecretValueResponse = client.getSecretValue(getSecretValueRequest);

        } catch(ResourceNotFoundException e) {
            logger.error("Could not found the secret {}",secretName);
        } catch (InvalidRequestException e) {
            logger.error("Request was invalid : ",e.getMessage());
        } catch (InvalidParameterException e) {
            logger.error("The request had invalid params: ",e.getMessage());
        }

        if(getSecretValueResponse != null) {
            if(getSecretValueResponse.getSecretString() != null) {
                secret = getSecretValueResponse.getSecretString();
            }
//            else {
//                binarySecretData = getSecretValueResponse.getSecretBinary();
//            }
        }

        logger.debug("Secret Name : {} \t Secret Value : {} \n",secretName,secret);
        return secret;
    }
}

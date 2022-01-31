package com.datacompare.util;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import com.datacompare.model.AppProperties;
import com.datacompare.model.DatabaseInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.tools.javac.util.ByteBuffer;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;

public class AWSUtil {

    public static Logger logger = LoggerFactory.getLogger("AWSUtil");

    public AppProperties getSecrets(AppProperties appProp) {

        String secretName = null;
        String endpoints = null;
        String AWS_Region = null;

        AWS_Region = appProp.getRegion();
        if(appProp.isSourceDB()) {
             endpoints = appProp.getSrcDBSecretManagerEndPoint();
            secretName = appProp.getSrcDBSecretName();
        }else{
             endpoints = appProp.getTgtDBSecretManagerEndPoint();
            secretName = appProp.getTgtDBSecretName();
        }
        AwsClientBuilder.EndpointConfiguration  config  =  new  AwsClientBuilder.EndpointConfiguration(endpoints, AWS_Region);
        AWSSecretsManagerClientBuilder  clientBuilder  =  AWSSecretsManagerClientBuilder.standard();
        clientBuilder.setEndpointConfiguration(config);
        AWSSecretsManager client  =  clientBuilder.build();
        ObjectMapper objectMapper  =  new  ObjectMapper();
        JsonNode secretsJson  =  null;
        ByteBuffer binarySecretData;
        GetSecretValueRequest getSecretValueRequest  =  new  GetSecretValueRequest().withSecretId(secretName);
        GetSecretValueResult getSecretValueResponse  =  null;
        try  {
            getSecretValueResponse  =  client.getSecretValue(getSecretValueRequest);
        }

        catch  (ResourceNotFoundException e)  {
            logger.error("The requested secret "  +  secretName  +  " was not found");
        }

        catch  (InvalidRequestException e)  {
            logger.error("The request was invalid due to: "  +  e.getMessage());
        }

        catch  (InvalidParameterException e)  {
            logger.error("The request had invalid params: "  +  e.getMessage());
        }
        if  (getSecretValueResponse  ==  null)  {
            logger.error("No credential found: " );
            return  null;
        }


        String secret = getSecretValueResponse.getSecretString();
        if (secret != null) {
            try {
                secretsJson  =  objectMapper.readTree(secret);
            }

            catch  (IOException e)  {
                logger.error("Exception while retrieving secret values: "  +  e.getMessage());
            }
        }

        else  {
            logger.error("The Secret String returned is null");

            return null;

        }
        String  host  =  secretsJson.get("host").textValue();
        String  port  =  secretsJson.get("port").textValue();
        String  dbname  =  secretsJson.get("dbname").textValue();
        String  username  =  secretsJson.get("username").textValue();
        String  password  =  secretsJson.get("password").textValue();
        if(appProp.isSourceDB()){
            appProp.setSourceDBName(dbname);
            appProp.setSourceUserName(username);
            appProp.setSourceUserPassword(password);
            if(port!=null)
            appProp.setSourcePort(Integer.parseInt(port));
            appProp.setSourceIP(host);

        } else{
            appProp.setTargetDBName(dbname);
            appProp.setTargetUserName(username);
            appProp.setTargetUserPassword(password);
            if(port!=null)
                appProp.setTargetPort(Integer.parseInt(port));
            appProp.setTargetIP(host);

        }
        return appProp;
    }
}

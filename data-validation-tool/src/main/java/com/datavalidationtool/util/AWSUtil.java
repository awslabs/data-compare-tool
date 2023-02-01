package com.datavalidationtool.util;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class AWSUtil {

    @Autowired
    private Environment env;

    public  String fetchValidationDetailsFromSSM(){
//        String secretName = env.getProperty("secretname");
//        Region region = Region.of(env.getProperty("region"));
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
            System.out.println("The requested secret " + secretName + " was not found");
        } catch (InvalidRequestException e) {
            System.out.println("The request was invalid due to: " + e.getMessage());
        } catch (InvalidParameterException e) {
            System.out.println("The request had invalid params: " + e.getMessage());
        }

        if(getSecretValueResponse != null) {
            if(getSecretValueResponse.getSecretString() != null) {
                secret = getSecretValueResponse.getSecretString();
            }
            else {
                binarySecretData = getSecretValueResponse.getSecretBinary();
            }
        }

        System.out.println("Secret Name : " + secretName + "\t Secret Value : " + secret + "\n");
        return secret;
        //Secret Value : {"username":"postgres","password":"postgres","engine":"postgres","host":"ukpg-instance-1.cl7uqmhlcmfi.eu-west-2.rds.amazonaws.com","port":"5432","dbname":"ttp,ttp_1"}
    }
}

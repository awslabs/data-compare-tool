package com.datavalidationtool.service;


import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import com.datavalidationtool.dao.DataSource;
import com.datavalidationtool.model.AwsSecret;
import com.datavalidationtool.model.DatabaseInfo;
import com.datavalidationtool.model.response.DatabaseDetails;
import com.datavalidationtool.model.response.HostDetails;
import com.datavalidationtool.model.response.SchemaDetails;
import com.datavalidationtool.model.response.TableDetails;
import com.datavalidationtool.util.AWSUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FetchValidationDetailsService {

    @Autowired
    private AWSUtil awsUtil;

    @Autowired
    public DataSource dataSource;

    @Autowired
    private RecommendationService recommendationService;

    public HostDetails getValidationDetails() throws Exception {

        String secret = awsUtil.fetchValidationDetailsFromSSM();
        ObjectMapper mapper = new ObjectMapper();
        AwsSecret awsSecret = mapper.readValue(secret, AwsSecret.class);

        DatabaseInfo databaseInfo = new DatabaseInfo(awsSecret.getHost(), awsSecret.getPort(), awsSecret.getDbname(),
                null, awsSecret.getUsername(), awsSecret.getPassword(), false, DatabaseInfo.dbType.POSTGRESQL,
                true, "/certs/", "changeit");

//        Connection conn = recommendationService.getConnection(databaseInfo);
        Connection conn = dataSource.getDBConnection();
//        ResultSet schemas = conn.getMetaData().getSchemas();
//        while(schemas.next()){
//           String schemaName = schemas.getString(1);
//           conn.getMetaData().getTables();
//        }
        String[] types = { "TABLE" };
        Map<String,List<String>> map = new HashMap<String, List<String>>();
        ResultSet tables = conn.getMetaData().getTables(null, null, "%", types);
        while(tables.next()){
            String tableName = tables.getString(3);

            String tableCatalog = tables.getString(1);
            String tableSchema = tables.getString(2);
            if(map.containsKey(tableSchema)){
                map.get(tableSchema).add(tableName);
            }else{
                map.put(tableSchema,new ArrayList<>(){{add(tableName);}});
            }
        }

        HostDetails hostDetails = new HostDetails();
        hostDetails.setHostName(awsSecret.getHost());
        DatabaseDetails databaseDetails = new DatabaseDetails();
        databaseDetails.setDatabaseName(awsSecret.getDbname());
        List<SchemaDetails> schemaList = new ArrayList<>();

        map.entrySet().stream().forEach((k) ->{
        SchemaDetails schemaDetails  = new SchemaDetails();
        schemaDetails.setSchemaName(k.getKey());
        List<TableDetails> tableList = new ArrayList<>();
        for(String val:k.getValue()){
            TableDetails tableDetails = new TableDetails();
            tableDetails.setTableName(val);
            tableList.add(tableDetails);
        }

        schemaDetails.setTableList(tableList);
        schemaList.add(schemaDetails);
        });
        databaseDetails.setSchemaList(schemaList);
        hostDetails.setDatabaseList(new ArrayList<>(){{add(databaseDetails);}});
        return hostDetails;
    }

//    public String fetchValidationDetailsFromSSM(){
////        String secretName = env.getProperty("secretname");
////        Region region = Region.of(env.getProperty("region"));
//        String secretName = env.getProperty("secretname");
//        String region = env.getProperty("region");
//
//        String endpoint =("secretsmanager." + region + ".amazonaws.com");
//        AwsClientBuilder.EndpointConfiguration config = new AwsClientBuilder.EndpointConfiguration(endpoint, region);
//        AWSSecretsManagerClientBuilder clientBuilder = AWSSecretsManagerClientBuilder.standard();
//        clientBuilder.setEndpointConfiguration(config);
//        AWSSecretsManager client = clientBuilder.build();
//        String secret = null;
//        ByteBuffer binarySecretData;
//        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
//        GetSecretValueResult getSecretValueResponse = null;
//        try {
//            getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
//
//        } catch(ResourceNotFoundException e) {
//            System.out.println("The requested secret " + secretName + " was not found");
//        } catch (InvalidRequestException e) {
//            System.out.println("The request was invalid due to: " + e.getMessage());
//        } catch (InvalidParameterException e) {
//            System.out.println("The request had invalid params: " + e.getMessage());
//        }
//
//        if(getSecretValueResponse != null) {
//            if(getSecretValueResponse.getSecretString() != null) {
//                secret = getSecretValueResponse.getSecretString();
//            }
//            else {
//                binarySecretData = getSecretValueResponse.getSecretBinary();
//            }
//        }
//
//        System.out.println("Secret Name : " + secretName + "\t Secret Value : " + secret + "\n");
//        return secret;
//        //Secret Value : {"username":"postgres","password":"postgres","engine":"postgres","host":"ukpg-instance-1.cl7uqmhlcmfi.eu-west-2.rds.amazonaws.com","port":"5432","dbname":"ttp,ttp_1"}
//    }

//    public static void getSecret() {
//
//        String secretName = "dvt-secret";
//        Region region = Region.of(env.getProperty("region"));
//
//        // Create a Secrets Manager client
//        SecretsManagerClient client = SecretsManagerClient.builder()
//                .region(region)
//                .build();
//
//        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
//                .secretId(secretName)
//                .build();
//
//        GetSecretValueResponse getSecretValueResponse;
//
//
//        try {
//            getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
//        } catch (Exception e) {
//            // For a list of exceptions thrown, see
//            // https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_GetSecretValue.html
//            throw e;
//        }
//
//        String secret = getSecretValueResponse.secretString();
//
//        // Your code goes here.
//    }
}

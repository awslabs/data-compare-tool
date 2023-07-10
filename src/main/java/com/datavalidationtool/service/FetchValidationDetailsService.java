package com.datavalidationtool.service;


import com.datavalidationtool.dao.DataSource;
import com.datavalidationtool.model.AwsSecret;
import com.datavalidationtool.model.response.DatabaseDetails;
import com.datavalidationtool.model.response.HostDetails;
import com.datavalidationtool.model.response.SchemaDetails;
import com.datavalidationtool.model.response.TableDetails;
import com.datavalidationtool.util.AWSUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

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
        Connection conn = dataSource.getDBConnection();
        String[] types = { "TABLE" };
        Map<String,List<String>> map = new HashMap<String, List<String>>();
        String sql ="SELECT table_schema,table_name\n" +
                "FROM information_schema.tables\n" +
                "ORDER BY table_schema,table_name;";
        HostDetails hostDetails =null;
        try {
            Statement st = conn.createStatement();
            ResultSet set1 = st.executeQuery(sql);
            while (set1.next()) {
                String tableSchema = set1.getString(1);
                String tableName = set1.getString(2);
                if (map.containsKey(tableSchema)) {
                    map.get(tableSchema).add(tableName);
                } else {
//                    map.put(tableSchema, new ArrayList<>() {{
//                        add(tableName);
//                    }});
                    map.put(tableSchema, Arrays.asList(tableName));
                }
            }
            hostDetails = new HostDetails();
            hostDetails.setHostName(awsSecret.getHost());
            hostDetails.setPort(awsSecret.getPort());
            hostDetails.setUsername(awsSecret.getUsername());
            hostDetails.setPassword(awsSecret.getPassword());
            DatabaseDetails databaseDetails = new DatabaseDetails();
            databaseDetails.setDatabaseName(awsSecret.getDbname());
            List<SchemaDetails> schemaList = new ArrayList<>();
            map.entrySet().stream().forEach((k) -> {
                SchemaDetails schemaDetails = new SchemaDetails();
                schemaDetails.setSchemaName(k.getKey());
                List<TableDetails> tableList = new ArrayList<>();
                for (String val : k.getValue()) {
                    TableDetails tableDetails = new TableDetails();
                    tableDetails.setTableName(val);
                    tableList.add(tableDetails);
                }
                schemaDetails.setTableList(tableList);
                schemaList.add(schemaDetails);
            });
            databaseDetails.setSchemaList(schemaList);
//            hostDetails.setDatabaseList(new ArrayList<>() {{
//                add(databaseDetails);
//            }});
            hostDetails.setDatabaseList(Arrays.asList(databaseDetails));
        }catch (Exception ex){
        }finally{
            conn.close();
        }
        return hostDetails;
    }
}

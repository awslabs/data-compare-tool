
# Data Validation Tool

Table of Contents

- [Data compare Tool Introduction](#data-compare-tool-introduction)
- [Features of Tool](#features-of-tool)
- [High Level Design](#high-level-design)
- [Setup & Infra](#setup-&-infra)
- [Web mode](#web-mode)
- [Command Line mode](#command-line-mode)
- [Output](#output)
- [Limitations](#limitations)
- [Security](#security)
- [License](#license)


## Data compare Tool Introduction


The data compare tool helps in validating the migrated data. Source will be either Oracle or SQL Server and target is PostgreSQL. The tool fetch the data from the table(s) (depending on the optional filter given) in chunks and each column in the row of the chunk will get concatenated and generates hash. The generated hash will be compared and produces the result row wise.

## Features of Tool

-  GUI / command line usage.
-  Validation for all tables in a schema or specific tables in a schema.
-  Validation for multiple schemas.
-  Ignore columns in a table from comparison.
-  Ability to provide chunk size for comparison in a table. Will fetch that many rows from a table.
-  Ability to execute chunks in parallel.
-  Option to set precision size (decimals) to compare.
-  Option to check only date in comparison and ignore time, time zone ..etc.
-  Option to get detail report for mismatch data.
-  Compare partial data using SQL Filter.
-  Option to set SSL Mode for target database.
-  Compares Clob and Blob data types also.
-  Supports Secrets in AWS Secret Manager to manage the database credentials and server details
-  Option to provide unique columns for a table, if there is no primary key

## High Level Design

<img width="400" alt="Flowchart" src="/doc/data-validation.png">

## Setup & Infra

Software:

-  Java/Jre: jre-1.8.x or more should be installed on the machine from where this tool will be executed.
-  Maven: It's required to build the project, preferable apache-maven-3.8.1 or higher.
-  OJDBC jar: It's required to run the project, preferable ojdbc7-12.1.0.2.jar or higher.
-  IDE: Your preferred IDE for code changes (like Eclipse/IntelliJ).
-  Web Browser/Postman: To launch the tool from web browser with URL.
-  Database management tool to connect and view DB (like DBever/pgAdmin)

Tool can be executed in two ways:

a) Command Line mode: Tool accepts arguments and stops the process once it completes the data validation.
b) Web mode: It can be used n number of times by supplying the required inputs from GUI one after another validation.

## Web Mode


Get the binary distribution package of data compare tool and execute the binary version using below command.

`java -cp "<Data compare jar folder path >/datacompare-tool-1.0.0.jar:<Oracle Driver Folder path>/ojdbc7-12.1.0.2.jar:< oracle driver folder path>/*" -Dloader.main="com.datacompare.Application" org.springframework.boot.loader.PropertiesLauncher`

Once 'Started Application' is seen launch the tool from web browser with URL: http://<<ipaddress>>:8080/

e.g:-http://localhost:8080/

By default it runs on 8080 port, so make sure this port is open in case if it blocked from access.

To run on a different port e.g:- 9000 use this argument with required port -Dserver.port=xxxx

`java -cp "<Data compare jar folder path >/datacompare-tool-1.0.0.jar:<Oracle Driver Folder path>/ojdbc7-12.1.0.2.jar:< oracle driver folder path>/*" -Dloader.main="com.datacompare.Application" -Dserver.port=9000 org.springframework.boot.loader.PropertiesLauncher`

To write the log data to a file instead of displaying in command.

`java -cp "<Data compare jar folder path >/datacompare-tool-1.0.0.jar:<Oracle Driver Folder path>/ojdbc7-12.1.0.2.jar:< oracle driver folder path>/*" -Dloader.main="com.datacompare.Application" org.springframework.boot.loader.PropertiesLauncher > CompareData.log`

Fields to understand after launching the application.

-  Select the Source database either 'ORACLE' or 'SQLSERVER' target is 'PostgreSQL' only.
-  Provide source database details like host name/ip, port, user name, password & select Service/SID and provide database name in case if source db is ORACLE, Database name in case if source db is SQLSERVER.
-  Provide target database details like host name/ip, port, user name, password & Database name. Select SSL Mode if target database is secured using SSL.
-  Schema name(s) (single or comma separated)
-  Table name(s) (single or comma separated) if there is single schema name then only table name(s) will be picked for comparison.
-  Ignore column(s)(single or comma separated) if these column(s) to be ignored from comparison. Present for data types like clob, blob & lob has no support for comparison.
-  Provide DB chunk size, this value will be used to fetch that many no of rows from a single table. By default the value is 10000, max value is 1000000.
-  Provide Parallel chunks, this value will be used to execute this many no of chunks in parallel from a single table. By default the value is 1, max value is 10.
-  Provide Decimal size, this value will be used to compare that many no of precision values after decimal point. By default the value is 5, max value is 10.
-  Select Compare Only Date check box if only date comparison is required by ignoring time from date and time stamp fields.
-  Select Report Detail Mismatch Data check box if detail report is required for mismatch data. Default it will provide unique key values.
-  Provide Output folder path to write the report files in this folder. Default it will write to the folder from where the tool is executed.
-  Provide Job Name, the report will be named with this Job name. The date and time will be append to this Job Name. Default it will give 'data_comparison_result' as Job name.
-  Provide SQL Filter, this value will be used to filter the data from fetch for comparison.
-  Provide AWS Secret Manager details for source and traget database


## Creating an executable jar

Get the latestcode from git barnch https://github.com/awslabs/data-compare-tool.git

Import the code into IDE

Run the "package" target in maven

spring-boot-maven-plugin dependency is added to pom.xml

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>

Run "mvn package" or " Run install" from the command line:

You can see the build status on the IDE console

[INFO] --- maven-jar-plugin:3.0.2:jar (default-jar) @ datacompare ---
[INFO] Building jar: /Users/amsudan/Desktop/Projects/DataValidation/EbsCode/target/datacompare-1.0.0.jar
[INFO]
[INFO] --- spring-boot-maven-plugin:2.0.3.RELEASE:repackage (default) @ datacompare ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  4.915 s
[INFO] Finished at: 2021-12-03T12:41:07+05:30
[INFO] ------------------------------------------------------------------------


## Command Line mode


Get the binary distribution package of data compare tool and execute the binary version using below command.

Example:

```
java -cp "<Data compare jar folder path >/datacompare-tool-1.0.0.jar:<Oracle Driver Folder path>/ojdbc7-12.1.0.2.jar:< oracle driver folder path>/*" -Dloader.main="com.datacompare.Application" org.springframework.boot.loader.PropertiesLauncher --sourceDBType=XXX --sourceHost=XXX --sourcePort=XXX --sourceUsername=XXX --sourcePassword=XXX --sourceDBService=XXX --sourceDBName=XXX --targetHost=XXX --targetPort=XXX --targetUsername=XXX --targetPassword=XXX --targetDBName=XXX --targetSSLRequire=X --schemaName=XXX --tableName=XXX --chunkSize=XXX --noofParallelChunks=X --compareOnlyDate=X --maxDecimals=X --displayCompleteData=X --jobName=XXX --outputFolderPath=XXX ,--sqlFilter=XXX
```
```
 java -cp  "/Users/amsudan/Desktop/Projects/DataValidation/awslab/DBJarFix/data-compare-tool/target/datacompare-tool-1.0.0.jar:/Users/amsudan/Desktop/lib/ojdbc7-12.1.0.2.jar:/Users/amsudan/Desktop/lib/*"   -Dloader.main="com.datacompare.Application" org.springframework.boot.loader.PropertiesLauncher --sourceDBType=ORACLE --sourceHost="XXXX" --sourcePort=1521 --sourceUsername="XXXX" --sourcePassword="XXXXX"  --sourceDBName="bcmsemdbpaf" --targetHost="XXXX" --targetPort=5432 --targetUsername="XXXX" --targetDBName=demo --targetPassword="XXXX"  --schemaName="XXXX" --tableName="XXXXX" --chunkSize=10000 --noofParallelChunks=5 --maxtextsizeforComparison=4000 --displayCompleteData=1
```


Required arguments:

```
--sourceDBType : Supported databases are ORACLE, SQLSERVER
--sourceHost | --targetHost : Database host name / ip.
--sourcePort | --targetPort : Database port
--sourceUsername | --targetUsername : Database user with with right access to the schema
--sourcePassword | --targetPassword : Database password to the above user
--sourceDBName | --targetDBName : Database name to be used to connect. SID/Service name in case if source db is ORACLE, Database name in case if source db is SQLSERVER. Database name for target db.
--schemaName : Database schema(s) single or comma separated. e.g:- "xxx,yyy,zzz"
--chunkSize : No of rows to fetch from a table for comparison. Default value is 10000, max value is 1000000
--noofParallelChunks : No of parallel chunks to fetch for comparison. Default value is 1, max value is 10.
```


Optional arguments:

```
--sourceDBService: If source is ORACLE then specific whether it should connect with Service/SID. Default is Service.
--targetSSLRequire: If target is secured using SSL then set the value. Possible values 0 - False, 1 - True. Default value is 0.
--tableName : Table name(s) single or comma separated. e.g:- "aaa,bbb,ccc"
--ignoreColumns : Column name(s)(single or comma separated) if these column(s) to be ignored from comparison.
--maxDecimals : Compare that many no of precision values after decimal point. Default value is 5, max value is 10.
--compareOnlyDate : Compares only date and ignores time if value set. Possible values 0 - False, 1 - True. Default value is 0.
--displayCompleteData : Ff detail report is required for mismatch data by default it will provide unique key values. Possible values 0 - False, 1 - True. Default value is 0.
--jobName : The report will be named with this Job name. The date and time will be append to this Job Name. Default it will give 'data_comparison_result' as Job name.
--outputFolderPath : Path to write the report files in this folder. Default it will write to the folder from where the tool is executed.
--sqlFilter : Filter the data from fetch for comparison
--maxTextSize : this parameter is used to set the column's varchar length max is 4000. In 11g upto varcahr(4000) is supported and beyond that datatype will be LOB(CLOB,BLOB).
--secMgrRegion : AWS region name where the secrets are defined eg:us-east-1
--srcDBSecretMgrEndPoint : Secret Manager endpoint for source database eg: secretsmanager.us-east-1.amazonaws.com
--srcDBSecretName : Source Database Secret Name defined in Secret Manager eg: OracleDB
--tgtDBSecretMgrEndPoint :Secret Manager endpoint for target database eg: secretsmanager.us-east-1.amazonaws.com
--tgtDBSecretName : Secret Manager endpoint for target database eg: PostgresDB

--primaryKey:<table name>:<columns with comma separation> provide uniuqe columns with comma(,) separted, if there is no primary key for the table
--primaryKey=EMPLOYEE:id,name,salary ; DEPARTMENT:id,name,org

--connectionPoolMaxSize: Integer value for pool max size like 25.
--connectionPoolMinSize: Integer value for pool min size like 15.

Note: Secrets defined in AWS Secret Manager is optional and it will override the database properties defined using sourceDBType, sourceHost, sourcePort etc..
```

Java heap size parameters:

 ```
 -Xms : It is used for setting the initial and minimum heap size.
 -Xmx : It is used for setting the maximum heap size.
 ```

Example:

 ```
java -Xms10m -Xmx1024m -cp  "/Users/amsudan/Desktop/Projects/DataValidation/awslab/DBJarFix/data-compare-tool/target/datacompare-tool-1.0.0.jar:/Users/amsudan/Desktop/lib/ojdbc7-12.1.0.2.jar:/Users/amsudan/Desktop/lib/*"   -Dloader.main="com.datacompare.Application" org.springframework.boot.loader.PropertiesLauncher --sourceDBType=ORACLE --sourceHost="XXXX" --sourcePort=1521 --sourceUsername="XXXX" --sourcePassword="XXXXX"  --sourceDBName="dbname" --targetHost="XXXX" --targetPort=5432 --targetUsername="XXXX" --targetDBName=dbmae --targetPassword="XXXX"  --schemaName="XXXX" --tableName="XXXXX" --chunkSize=10000 --noofParallelChunks=5 --maxTextSize=4000 --displayCompleteData=1 --primaryKey=EMPLOYEE:"id,name,salary" --connectionPoolMaxSize=25 --connectionPoolMinSize=15
```

 CLI arguments with AWS Secret Manager:

```
 java -cp "<Data compare jar folder path >/datacompare-tool-1.0.0.jar:<Oracle Driver Folder path>/datacompare-tool-1.0.0.jar:< oracle driver folder path>/ojdbc7-12.1.0.2.jar:< oracle driver folder path>/*"   -Dloader.main="com.datacompare.Application" org.springframework.boot.loader.PropertiesLauncher --sourceDBType=ORACLE  --secMgrRegion="region" --srcDBSecretMgrEndPoint="secretsmanager.region.amazonaws.com" --srcDBSecretName="source-dms-secrets" --tgtDBSecretMgrEndPoint="secretsmanager.region.amazonaws.com" --tgtDBSecretName="target-dms-secrets" --schemaName="schename" --tableName="tablename" --chunkSize=5 --noofParallelChunks=4 --maxTextSize=4000 --displayCompleteData=1 --primaryKey=<Table Name>:< column names with comma separation>

```

 ```
 java -cp “home/ec2-user/lib/datacompare-tool-1.0.0.jar:/home/ec2-user/lib/ojdbc7-12.1.0.2.jar:/home/ec2-user/lib/*”   -Dloader.main=“com.datacompare.Application” org.springframework.boot.loader.PropertiesLauncher --sourceDBType=ORACLE  --secMgrRegion=“us-east-1” --srcDBSecretMgrEndPoint=“secretsmanager.us-east-1.amazonaws.com” --srcDBSecretName=“OracleDB” --tgtDBSecretMgrEndPoint=“secretsmanager.us-east-1.amazonaws.com” --tgtDBSecretName=“PGDB” --schemaName=“xxx-schema” --tableName=“xxx-table” --chunkSize=10000 --noofParallelChunks=5 --maxTextSize=4000 --displayCompleteData=1

 ```
## Output

A. HTML:
Once the process is completed. The result will be generated as .html file. Filename format is <<Job Name>>_<<yyyy-MM-DD_HH-mm>>.html.
This file has details like table name, ignored columns, max decimals compared, compared only date, source table count, target table count, matched rows count, missing rows count, unmatched rows count, additional rows in target, execution started at, time taken for execution, status, message and report link in case if any data mismatch. Also it contains link Summary Report In CSV for these data in csv format.

In case if any mismatch of data found, then it generates separate .html file for each table. Filename format is <<schema_name>>_<<table_name>>_table_comparison_result_<<yyyy-MM-DD_HH-mm>>.html. Navigation link is available to this page from data_comparison_result_<<yyyy-MM-DD_HH-mm>>.html page in that respective row. Also it contains link Detailed Report In CSV for these data in csv format.

Possible Status values: Completed / Failed. Completed means the tool has compared the data. Failed means it was unable to compare the data.
Possible Message: Additional rows found in Target / Rows did not migrated from source / Tuple value mismatched / Data Matched.

B. CSV:
Once the process is completed. The result will be generated as .csv file for 1) Summary Report and 2) Detailed Report.

- Summary Report filename format is <<Job Name>>_<<yyyy-MM-DD_HH-mm>>.csv
This file has details similar to html file, i.e. table name, columns, max decimals compared, Max Text Size Compared, compared only date, source table count, target table count, matched rows count, missing rows count, unmatched rows count, additional rows in target, execution started at, time taken for execution, status, message, Sql Filter Used.

- Detailed Report filename format is <<schema_name>>_<<table_name>>_table_comparison_result_<<yyyy-MM-DD_HH-mm>>.csv
In case if any mismatch of data found, then it generates separate .csv file for each table. Similar to HTML report based on different conditions it contains values for REASON OF FAILURE, UNIQUE KEYS, SOURCE TUPLE, TARGET TUPLE etc.


## Limitations


-  Random or Sample data validation.
-  Only one Table can be compared at a time (Tables are compared in sequential order).
-  Unable to check the progress percentage of comparison.

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This project is licensed under the Apache-2.0 License.

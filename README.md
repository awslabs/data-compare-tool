# Data Compare Tool

Table of Contents

-   [Data compare Tool Introduction](#data-compare-tool-introduction)
-   [Features of Tool](#features-of-tool)
-   [High Level Design](#high-level-design)
-   [Setup & Infra](#setup-&-infra)
-   [Web mode](#web-mode)
-   [Command Line mode](#command-line-mode)
-   [Output](#output)
-   [Limitations](#limitations)
-   [Security](#security)
-   [License](#license)

## Data compare Tool Introduction

The data compare tool helps in validating the migrated data. Source will be either Oracle or SQL Server and target is PostgreSQL. The tool fetch the data from the table(s) (depending on the optional filter given) in chunks and each column in the row of the chunk will get concatenated and generates hash. The generated hash will be compared and produces the result row wise.

## Features of Tool

-   GUI usage.
-   Validation for all tables in a schema or specific tables in a schema.
-   Validation for multiple schemas.
-   Ignore columns in a table from comparison.
-   Ability to provide chunk size for comparison in a table. Will fetch that many rows from a table.
-   Ability to execute chunks in parallel.
-   Option to get detail report for mismatch data.
-   Compare partial data using SQL Filter.
-   Compares Clob and Blob data types also.
-   Supports Secrets in AWS Secret Manager to manage the database credentials and server details.
-   Option to provide unique columns for a table, if there is no primary key.
-   Ability to schedule validations.
-   Ability to schedule incremental validations.
-   Ability to remediate mismatched data.
-   Option to view last run/validation details.

## High Level Design

<img width="500" alt="Flowchart" src="/doc/data-validation.png">

## Setup & Infra

Software:

-   Java/Jre: jre-1.8.x or more should be installed on the machine from where this tool will be executed.
-   Maven: It's required to build the project, preferable apache-maven-3.8.1 or higher.
-   OJDBC jar: It's required to run the project, preferable ojdbc7-12.1.0.2.jar or higher.
-   IDE: Your preferred IDE for code changes (like Eclipse/IntelliJ).
-   Node: Required node version to run tool is v14.xx.x.(v14.21.3).
-   Web Browser/Postman: To launch the tool from web browser with URL.
-   Database management tool to connect and view DB (like DBever/pgAdmin)

Tool can be executed in web mode(GUI): It can be used n number of times by supplying the required inputs from GUI one after another validation.

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

-   Select the Source database either 'ORACLE' or 'SQLSERVER' target is 'PostgreSQL' only.
-   Provide source database details like host name/ip, port, user name, password & select Service/SID and provide database name in case if source db is ORACLE, Database name in case if source db is SQLSERVER.
-   Provide target database details like host name/ip, port, user name, password & Database name. Select SSL Mode if target database is secured using SSL.
-   Schema name(s) (single or comma separated)
-   Table name(s) (single or comma separated) if there is single schema name then only table name(s) will be picked for comparison.
-   Ignore column(s)(single or comma separated) if these column(s) to be ignored from comparison. Present for data types like clob, blob & lob has no support for comparison.
-   Provide DB chunk size, this value will be used to fetch that many no of rows from a single table. By default the value is 10000, max value is 1000000.
-   Provide Parallel chunks, this value will be used to execute this many no of chunks in parallel from a single table. By default the value is 1, max value is 10.
-   Provide Decimal size, this value will be used to compare that many no of precision values after decimal point. By default the value is 5, max value is 10.
-   Select Compare Only Date check box if only date comparison is required by ignoring time from date and time stamp fields.
-   Select Report Detail Mismatch Data check box if detail report is required for mismatch data. Default it will provide unique key values.
-   Provide Output folder path to write the report files in this folder. Default it will write to the folder from where the tool is executed.
-   Provide Job Name, the report will be named with this Job name. The date and time will be append to this Job Name. Default it will give 'data_comparison_result' as Job name.
-   Provide SQL Filter, this value will be used to filter the data from fetch for comparison.
-   Provide AWS Secret Manager details for source and traget database

## Limitations

-   Random or Sample data validation.
-   Unable to check the progress percentage of comparison.

## Security

See [CONTRIBUTING](SECURITY.md#security-issue-notifications) for more information.

## License

This project is licensed under the Apache-2.0 License.

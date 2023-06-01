import React, { useEffect, useState, useReducer } from "react";
import { useNavigate } from "react-router-dom";
import FormGroup from "@mui/material/FormGroup";
import FormControlLabel from "@mui/material/FormControlLabel";
import Box from "@mui/material/Box";
import Grid from "@mui/material/Grid";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import TextField from "@mui/material/TextField";
import Checkbox from "@mui/material/Checkbox";
import Button from "@mui/material/Button";
import Divider from "@mui/material/Divider";
import Select from "react-select";
import TableContainer from "@mui/material/TableContainer";
import Paper from "@mui/material/Paper";
import Table from "@mui/material/Table";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import TableCell from "@mui/material/TableCell";
import TableBody from "@mui/material/TableBody";
import { containerClasses, Autocomplete } from "@mui/material";
import dummyInputData from "./dummy_data.json";
import { FormStatus } from "./static_data";
import { Link } from "react-router-dom";
import LoadingSpinner from "../LoadingSpinner";
import "../styles.css";
// import logo from "../dart-logo.jpg";
import Header from "../Header.jsx";
import DatePicker from "react-datepicker";

import DateTimePicker from "react-datetime-picker";
import "react-datepicker/dist/react-datepicker.css";

const initialValue = {
    hostname: "ukpg-instance-1.cl7uqmhlcmfi.eu-west-2.rds.amazonaws.com",
    port: "5432",
    dbname: "ttp",
    usessl: false,
    username: "postgres",
    password: "postgres",
    schemaNames: [],
    tableNames: "",
    columnNames: "",
    exTables: false,
    exColumns: false,
    showTable: false,
    showRunTable: false,
    incremental: false,
};

const reducer = (userCred, action) => {
    switch (action.type) {
        case "update":
            if (
                ["schemaNames", "tableNames", "columnNames"].indexOf(
                    action.payload.key
                ) > -1
            ) {
                var info = action.payload.value;
                if (info.endsWith(",")) {
                    info = info.replace(/ /g, "");
                }
                return {
                    ...userCred,
                    [action.payload.key]: action.payload.value,
                };
            } else {
                return {
                    ...userCred,
                    [action.payload.key]: action.payload.value,
                };
            }
        case "set_default":
            return {
                ...userCred,
                userCred: action.payload,
            };
        case "reset":
            return initialValue;
        default:
            throw new Error(`Unknown action type: ${action.type}`);
    }
};

export default function Validation() {
    const [isLoading, setIsLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");
    const [userCred, dispatch] = useReducer(reducer, initialValue);
    const [loadDefaultData, setLoadDefaultData] = useState(false);
    const [ifFormTouched, setIfFormTouched] = useState(FormStatus.UNTOUCHED);
    const [isEntireFormValid, setIsEntireFormValid] = useState(false);
    const navigate = useNavigate();
    const [tableData, setTableData] = useState([{}]);
    const [showTable, setShowTable] = useState(false);
    const [showRunTable, setShowRunTable] = useState(false);
    const [runTableData, setRunTableData] = useState([{}]);
    const [showScheduleTable, setShowScheduleTable] = useState(false);
    const [scheduleTableData, setScheduleTableData] = useState([{}]);
    const [exTables, setExTables] = useState(false);
    const [exColumns, setExColumns] = useState(false);
    const [tableNames, setTableNames] = useState("");

    const [tableName, setTableName] = useState("");
    const [schemaName, setSchemaName] = useState("");
    const [srcSchemaName, setSrcSchemaName] = useState("");
    const [incremental, setIncremental] = useState(false);
    const [data, setData] = useState([{}]);
    const [value, onClick] = useState("");

    const [scheduleDate, setScheduleDate] = useState(new Date());
    const [scheduleEndDate, setScheduleEndDate] = useState(new Date());
    const [reoccurrence, setReoccurrence] = useState(false);
    const [dayFrequency, setDayFrequency] = useState("");
    const [timeFrequency, setTimeFrequency] = useState("");
    const [timeOccurrence, setTimeOccurrence] = useState("");
    const [numOccurrence, setNumOccurrence] = useState("");

    const handleInput = (event) => {
        dispatch({
            type: "update",
            payload: {
                key: event.target.name,
                value:
                    event.target.name === "usessl"
                        ? !userCred.usessl
                        : event.target.value,
            },
        });
    };
    const handleTabExcludeInput = (event) => {
        if (tableNames == null || tableNames.length == 0) {
            alert("Please select a Table name");
            return;
        }
        setExTables(event.target.checked);
    };
    const handleColExcludeInput = (event) => {
        if (userCred.columnNames == null || userCred.columnNames == "") {
            alert("Please provide a column name");
            setExColumns("");
            return;
        }
        setExColumns(event.target.checked);
    };

    const handleIncrementalInput = (event) => {
        setIncremental(event.target.checked);
    };

    const handleReoccurrenceInput = (event) => {
        setReoccurrence(event.target.checked);
    };
    const handleSrcSchemaChange = (event, selectedOption) => {
        if (selectedOption != null) {
            setSrcSchemaName(selectedOption?.value);
        } else {
            setSrcSchemaName("");
        }
    };
    function redirectToHome(event) {
        navigate("/dvt/menu");
    }
    function handleTableInput(event, selectedOption) {
        var value = Array.from(selectedOption, (item) => item.value);
        var length = value.length;
        if (length == 0) {
            alert("Please select a Table Name");
            return;
        }
        if (srcSchemaName == "") {
            alert("Please select a Source schema");
            return;
        }
        if (schemaName == "") {
            alert("Please select a Target schema");
            return;
        }
        setTableNames(value);
        setIsLoading(true);
        setShowRunTable(false);
        setShowScheduleTable(false);
        setShowTable(false);
        let requestParams = { method: "POST", headers: "", body: "" };
        requestParams.headers = { "Content-Type": "application/json" };
        requestParams.body = JSON.stringify({
            sourceSchemaName: srcSchemaName,
            tableName: value[length - 1],
        });
        fetch("http://localhost:8090/dvt/validation/getRunInfo", requestParams)
            .then((response) => {
                if (response.ok) {
                    return response.json();
                }
            })
            .then((resultData) => {
                setIsLoading(false);
                let slnumber = 1;
                let msg =
                    resultData !== null || resultData !== ""
                        ? resultData
                        : "Something went wrong, please try again";
                let obj = {};
                obj.slNo = slnumber;
                slnumber++;
                if (resultData.incremental) {
                    obj.incremental = "Yes";
                } else {
                    obj.incremental = "No";
                }
                console.log("slno", slnumber);
                obj.tableName = value[length - 1];
                obj.mismatchRows = resultData.mismatchRows;
                obj.missingRows = resultData.missingRows;
                obj.totalRows = resultData.totalRecords;
                obj.dataFilters = resultData.dataFilters;
                obj.uniqueCols = resultData.uniqueColumns;
                obj.chunkColumns = resultData.chunkColumns;
                obj.chunkSize = resultData.chunkSize;
                setTableData(obj);
                setShowTable(true);
                setShowRunTable(false);
                setShowScheduleTable(false);
                setIsLoading(false);
            })
            .catch((error) => {
                setErrorMessage("Unable to validate the data");
                setIsLoading(false);
                alert(error);
                console.log("Error ::", error);
            });
    }
    let databaseList = [];
    let clearable = true;

    const handleDayFrequencyChange = (event) => {
        if (event != null) {
            setDayFrequency(event.value);
        }
    };
    const handleTimeFrequencyChange = (event) => {
        if (event != null) {
            setTimeFrequency(event.value);
        }
    };
    const handleSchemaChange = (event, selectedOption) => {
        let list = [];
        if (selectedOption != null) {
            initialValue.tableNames = [];
            list = pageDetails[0][0].schemaList.find(
                (schema) => schema.schemaName == selectedOption.value
            ).tableList;
            for (let j = 0; j < list.length; j++) {
                list[j].label = list[j].tableName;
                list[j].value = list[j].tableName;
            }
            setSchemaName(selectedOption.value);
            setTableNames([]);
            setTableName("");
            setTableNames();
        }
        initialValue.tableNames = list;
        setDummy([]);
        dispatch({
            type: "reset",
            payload: initialValue,
        });
    };
    const [pageDetails, setPageDetails] = useState([]);
    const [dummy, setDummy] = useState([]);
    const fetchPageDetails = () => {
        setIsLoading(true);
        fetch("http://localhost:8090/dvt/validation/dbDetails")
            .then((res) => res.json()) //.then((res => JSON.stringify(res)))
            .then((res) => {
                pageDetails.push(res.databaseList);
                let details = initialValue;
                let d = res;
                details.hostName = d;
                initialValue.hostname = res.hostName;
                initialValue.port = res.port;
                initialValue.username = res.username;
                initialValue.password = res.password;
                databaseList = res.databaseList;
                initialValue.dbname = res.databaseList[0].databaseName;
                let schemaNamesArray = new Array();
                for (
                    let i = 0;
                    i < res.databaseList[0].schemaList.length;
                    i++
                ) {
                    let jsondata = {};
                    jsondata.label =
                        res.databaseList[0].schemaList[i].schemaName;
                    jsondata.value =
                        res.databaseList[0].schemaList[i].schemaName;
                    schemaNamesArray.push(jsondata);
                }
                initialValue.schemaNames = schemaNamesArray;
                let tableList = "";
                for (
                    let i = 0;
                    i < res.databaseList[0].schemaList[0].tableList.length;
                    i++
                ) {
                    tableList = tableList.concat(
                        res.databaseList[0].schemaList[0].tableList[i]
                            .tableName + ", "
                    );
                }
                tableList.slice(0, -3);
                initialValue.tableNames = tableList;
                dispatch({
                    type: "set_default",
                    payload: details,
                });
            })
            .catch((error) => {
                setErrorMessage("Unable to validate the data");
            });
        setIsLoading(false);
    };
    useEffect(() => {
        fetchPageDetails();
    }, []);

    function handleSubmit() {
        if (schemaName == "") {
            alert("Please select a Target schema");
            return;
        }
        if (srcSchemaName == "") {
            alert("Please select a Source schema");
            return;
        }
        if (tableNames == null || tableNames.length == 0) {
            alert("Please select a Table Name");
            return;
        }
        if (exTables != null || exTables != "") {
            if (tableNames == null || tableNames.length == 0) {
                alert("Please Select a table to exclude");
                return;
            }
        }
        if (exColumns) {
            if (userCred.columnNames == null || userCred.columnNames == "") {
                alert("Please provide a column name");
                return;
            }
        }
        setShowRunTable(false);
        setShowScheduleTable(false);
        setShowTable(false);
        setIsLoading(true);
        let requestParams = { method: "POST", headers: "", body: "" };
        requestParams.headers = { "Content-Type": "application/json" };
        requestParams.body = JSON.stringify({
            targetSchemaName: schemaName,
            sourceSchemaName: srcSchemaName,
            tableNames: tableNames,
            columns: userCred.columnNames,
            ignoreColumns: exColumns,
            dataFilters: userCred.dataFilters,
            uniqueCols: userCred.uniqueCols,
            chunkColumns: userCred.chunkColumns,
            ignoreTables: exTables,
            chunkSize: userCred.fetchSize,
            incremental: incremental,
            scheduleTime: scheduleDate,
            reoccurrence: reoccurrence,
            dayFrequency: dayFrequency,
            scheduleEndDate: scheduleEndDate,
            timeOccurrence: userCred.timeOccurrence,
            timeFrequency: timeFrequency,
            numOccurrence: userCred.numOccurrence,
        });
        console.log("Data To Submit == ", JSON.stringify(requestParams));
        fetch("http://localhost:8090/dvt/schedule/addSchedule", requestParams)
            .then((response) => {
                if (response.ok) {
                    return response.text();
                }
            })
            .then((resultData) => {
                setIsLoading(false);
                let msg =
                    resultData !== null || resultData !== ""
                        ? resultData
                        : "Something went wrong, please try again";
                alert("Added schedules");
                //   navigate("/dvt/selection");
            })
            .catch((error) => {
                setErrorMessage("Unable to validate the data");
                setIsLoading(false);
                alert("Something went wrong, please try again");
                console.log("Error ::", error);
            });
    }
    function getLastRunDetails() {
        //event.preventDefault();
        setShowTable(false);
        setIsLoading(true);
        let requestParams = { method: "POST", headers: "", body: "" };
        requestParams.headers = { "Content-Type": "application/json" };
        requestParams.body = JSON.stringify({
            targetSchemaName: schemaName,
            sourceSchemaName: srcSchemaName,
            targetDBName: userCred.dbname,
            targetHost: userCred.hostname,
            targetPort: userCred.port,
            targetUserName: userCred.username,
            targetUserPassword: userCred.password,
            tableName: tableName,
            columns: userCred.columnNames,
            ignoreColumns: userCred.exColumns,
            ignoreTables: exTables,
            dataFilters: userCred.dataFilters,
            uniqueCols: userCred.uniqueCols,
            chunkColumns: userCred.chunkColumns,
            fetchSize: userCred.fetchSize,
        });
        console.log("Data To Submit == ", JSON.stringify(requestParams));
        fetch(
            "http://localhost:8090/dvt/validation/getLastRunDetails",
            requestParams
        )
            .then((response) => {
                if (response.ok) {
                    return response.json();
                }
            })
            .then((response) => {
                let obj = {};
                obj.schemaName = response.schemaName;
                obj.runs = response.runs;
                setRunTableData(response.runs);
                setShowRunTable(true);
                setShowScheduleTable(false);
                setIsLoading(false);
            })
            .catch((error) => {
                setErrorMessage("Unable to validate the data");
                setIsLoading(false);
                alert("Something went wrong, please try again");
                console.log("Error ::", error);
            });
    }
    function getScheduleDetails() {
        setShowTable(false);
        setIsLoading(true);
        let requestParams = { method: "POST", headers: "", body: "" };
        requestParams.headers = { "Content-Type": "application/json" };
        requestParams.body = JSON.stringify({
            targetSchemaName: schemaName,
            sourceSchemaName: srcSchemaName,
            targetDBName: userCred.dbname,
            targetHost: userCred.hostname,
            targetPort: userCred.port,
            targetUserName: userCred.username,
            targetUserPassword: userCred.password,
            tableName: tableName,
            columns: userCred.columnNames,
            ignoreColumns: userCred.exColumns,
            ignoreTables: exTables,
            dataFilters: userCred.dataFilters,
            uniqueCols: userCred.uniqueCols,
            chunkColumns: userCred.chunkColumns,
            fetchSize: userCred.fetchSize,
        });
        console.log("Data To Submit == ", JSON.stringify(requestParams));
        fetch("http://localhost:8090/dvt/schedule/getSchedule", requestParams)
            .then((response) => {
                if (response.ok) {
                    return response.json();
                }
            })
            .then((response) => {
                setScheduleTableData(response);
                setShowScheduleTable(true);
                setIsLoading(false);
            })
            .catch((error) => {
                setErrorMessage("Unable to validate the data");
                setIsLoading(false);
                alert("Something went wrong, please try again");
                console.log("Error ::", error);
            });
    }

    useEffect(() => {
        if (isEntireFormValid) {
            setIsLoading(true);
            var params = {
                targetSchemaName: userCred.schemaNames[0],
                sourceSchemaName: "",
                targetDBName: userCred.dbname,
                targetHost: userCred.hostname,
                targetPort: userCred.port,
                targetUserName: userCred.username,
                targetUserPassword: userCred.password,
                tableName: userCred.tableNames,
                dataFilters: userCred.dataFilters,
                uniqueCols: userCred.uniqueCols,
                chunkColumns: userCred.chunkColumns,
                ignoreTables: exTables,
                ignoreColumns: userCred.exColumns,
                fetchSize: userCred.fetchSize,
                incremental: incremental,
            };
            var fetchContent = {
                method: "POST",
                headers: {
                    Accept: "application/json",
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(params),
            };
            let url = "http://localhost:8090/validation/compareData";
            fetch(url, fetchContent)
                .then((response) => {
                    if (response.ok) {
                        return response.json();
                    }
                })
                .then((data) => {})
                .catch((error) => {
                    console.log("An Unexpected Error occured..");
                });
            navigate("/dvt/recommend", userCred);
            setIsLoading(false);
        } else {
            console.log("User details: Invalid");
        }
    }, [isEntireFormValid]);

    function handleReset() {
        setIfFormTouched(FormStatus.UNTOUCHED);
        dispatch({
            type: "reset",
        });
    }
    const handleRecomm = () => {
        navigate("/dvt/selection");
    };
    const [state] = useReducer(reducer);
    const frequency = [
        { value: "D", label: "daily" },
        { value: "W", label: "weekly" },
        { value: "M", label: "monthly" },
    ];
    const frequencyDay = [
        { value: "MI", label: "minute" },
        { value: "H", label: "hour" },
    ];
    return (
        <div>
            <Grid
                container
                mb={2}
                spacing={1}
                columnSpacing={{ xs: 2 }}
                justifyContent="center"
                alignItems="center"
            >
                <Grid item xs={12} sm={6} md={11}>
                    <Typography variant="h4" align="center" valign="bottom">
                        Validation Schedules
                    </Typography>
                </Grid>
            </Grid>

            <Box>
                <Grid
                    container
                    mb={2}
                    spacing={2}
                    columnSpacing={{ xs: 2 }}
                    justifyContent="center"
                    alignItems="center"
                >
                    <Grid item xs={12}>
                        <Typography></Typography>
                    </Grid>

                    <Grid item xs={12} md={4}></Grid>
                </Grid>
                <Grid
                    container
                    mb={2}
                    spacing={2}
                    columnSpacing={{ xs: 2 }}
                    justifyContent="left"
                    alignItems="center"
                >
                    <Grid item xs={12}>
                        <Typography> Table Details </Typography>
                    </Grid>
                    <Grid item xs={12} md={3}>
                        {/*<TextField*/}
                        {/*  fullWidth*/}
                        {/*  multiline*/}
                        {/*  maxRows={4}*/}
                        {/*  name="schemaNames"*/}
                        {/*  label="Schema Names"*/}
                        {/*  variant="outlined"*/}
                        {/*  value={userCred.schemaNames}*/}
                        {/*  error={userCred.schemaNames === '' && ifFormTouched === FormStatus.MODIFIED}*/}
                        {/*  onChange={handleInput}*/}
                        {/*/>*/}
                        {/* <Select
                            isDisabled={false}
                            isLoading={false}
                            isClearable
                            isSearchable={false}
                            placeholder="Target Schema"
                            defaultValue={userCred.schemaNames[0]}
                            name="schemas"
                            label="Target Schema"
                            variant="outlined"
                            options={userCred.schemaNames}
                            onChange={handleSchemaChange}
                        /> */}
                        <Autocomplete
                            size="small"
                            options={userCred.schemaNames}
                            name="schemas"
                            defaultValue={userCred.schemaNames[0]}
                            onChange={handleSchemaChange}
                            filterSelectedOptions
                            renderInput={(params) => (
                                <TextField
                                    {...params}
                                    label="Target Schema"
                                    placeholder="Target Schema"
                                />
                            )}
                        />
                    </Grid>
                    <Grid item xs={12} md={3}>
                        {/*<TextField*/}
                        {/*  fullWidth*/}
                        {/*  multiline*/}
                        {/*  maxRows={4}*/}
                        {/*  name="schemaNames"*/}
                        {/*  label="Schema Names"*/}
                        {/*  variant="outlined"*/}
                        {/*  value={userCred.schemaNames}*/}
                        {/*  error={userCred.schemaNames === '' && ifFormTouched === FormStatus.MODIFIED}*/}
                        {/*  onChange={handleInput}*/}
                        {/*/>*/}
                        {/* <Select
                            isDisabled={false}
                            isLoading={false}
                            isClearable
                            isSearchable={false}
                            placeholder="Source Schema "
                            defaultValue={userCred.schemaNames[0]}
                            name="srcSchemas"
                            label="Source Schema"
                            variant="outlined"
                            options={userCred.schemaNames}
                            onChange={handleSrcSchemaChange}
                        /> */}
                        <Autocomplete
                            size="small"
                            options={userCred.schemaNames}
                            defaultValue={userCred.schemaNames[0]}
                            onChange={handleSrcSchemaChange}
                            name="srcSchemas"
                            filterSelectedOptions
                            renderInput={(params) => (
                                <TextField
                                    {...params}
                                    label="Source Schema"
                                    placeholder="Source Schema"
                                />
                            )}
                        />
                    </Grid>
                    <Grid item xs={12} md={3}>
                        {/*<TextField*/}
                        {/*  fullWidth*/}
                        {/*  multiline*/}
                        {/*  maxRows={4}*/}
                        {/*  name="tableNames"*/}
                        {/*  label="Table Names"*/}
                        {/*  variant="outlined"*/}
                        {/*  value={userCred.tableNames}*/}
                        {/*  error={userCred.tableNames === '' && ifFormTouched === FormStatus.MODIFIED}*/}
                        {/*  onChange={handleInput}*/}
                        {/*/>*/}
                        {/* <Select
                            isDisabled={false}
                            isLoading={false}
                            isMulti={true}
                            isClearable={clearable}
                            isSearchable={false}
                            hideSelectedOptions={false}
                            placeholder="Table Names.."
                            defaultValue={userCred.tableNames[0]}
                            name="tables"
                            label="Table Name"
                            variant="outlined"
                            options={userCred.tableNames}
                            onChange={handleTableInput}
                        /> */}
                        <Autocomplete
                            size="small"
                            multiple
                            limitTags={1}
                            disabled={!schemaName || !srcSchemaName}
                            options={userCred.tableNames}
                            defaultValue={userCred.tableNames[0] || undefined}
                            onChange={handleTableInput}
                            name="tables"
                            filterSelectedOptions
                            renderInput={(params) => (
                                <TextField
                                    {...params}
                                    label="Table Name"
                                    placeholder="Table Name"
                                />
                            )}
                        />
                    </Grid>
                    <Grid item xs={12} md={3}>
                        <FormGroup>
                            <FormControlLabel
                                control={
                                    <Checkbox
                                        name="exTables"
                                        value={exTables}
                                        onChange={handleTabExcludeInput}
                                    />
                                }
                                label="Exclude"
                            />
                        </FormGroup>
                    </Grid>
                    <Grid item xs={12} md={3}>
                        <TextField
                            size="small"
                            fullWidth
                            multiline
                            maxRows={4}
                            name="columnNames"
                            label="Column Names"
                            variant="outlined"
                            value={userCred.columnNames}
                            error={
                                userCred.columnNames === "" &&
                                ifFormTouched === FormStatus.MODIFIED
                            }
                            onChange={handleInput}
                        />
                    </Grid>
                    <Grid item xs={12} md={1}>
                        <FormGroup>
                            <FormControlLabel
                                control={
                                    <Checkbox
                                        name="exColumns"
                                        value={exColumns}
                                        onChange={handleColExcludeInput}
                                    />
                                }
                                label="Exclude"
                            />
                        </FormGroup>
                    </Grid>
                    <Grid item xs={4} md={3}>
                        <TextField
                            size="small"
                            fullWidth
                            multiline
                            maxRows={4}
                            name="uniqueCols"
                            label="Unique Columns"
                            variant="outlined"
                            value={userCred.uniqueCols}
                            error={
                                userCred.uniqueCols === "" &&
                                ifFormTouched === FormStatus.MODIFIED
                            }
                            onChange={handleInput}
                        />
                    </Grid>
                    <Grid item xs={12} md={4}>
                        <TextField
                            size="small"
                            fullWidth
                            multiline
                            maxRows={4}
                            name="dataFilters"
                            label="Data Filters"
                            variant="outlined"
                            value={userCred.dataFilters}
                            error={
                                userCred.dataFilters === "" &&
                                ifFormTouched === FormStatus.MODIFIED
                            }
                            onChange={handleInput}
                        />
                    </Grid>{" "}
                    <Grid item xs={12} md={3}>
                        <TextField
                            size="small"
                            fullWidth
                            multiline
                            maxRows={4}
                            name="chunkColumns"
                            label="Chunk Columns"
                            variant="outlined"
                            value={userCred.chunkColumns}
                            error={
                                userCred.chunkColumns === "" &&
                                ifFormTouched === FormStatus.MODIFIED
                            }
                            onChange={handleInput}
                        />
                    </Grid>{" "}
                    <Grid item xs={12} md={3}>
                        <TextField
                            size="small"
                            fullWidth
                            multiline
                            maxRows={4}
                            name="fetchSize"
                            label="No. of Chunks"
                            variant="outlined"
                            value={userCred.fetchSize}
                            error={
                                userCred.fetchSize === "" &&
                                ifFormTouched === FormStatus.MODIFIED
                            }
                            onChange={handleInput}
                        />
                    </Grid>
                    <Grid item xs={3} md={3}>
                        <FormGroup>
                            <FormControlLabel
                                control={
                                    <Checkbox
                                        name="incremental"
                                        onChange={handleIncrementalInput}
                                        value={incremental}
                                    />
                                }
                                label="CDC Run"
                            />
                        </FormGroup>
                    </Grid>
                    <Grid item xs={3} md={3}></Grid>
                    <Grid item xs={12} md={2}>
                        <Typography>Select Start Date and Time</Typography>
                    </Grid>
                    <Grid item xs={1} md={2}>
                        <DatePicker
                            multiline
                            selected={scheduleDate}
                            onChange={(date) => setScheduleDate(date)}
                            timeInputLabel="Time:"
                            dateFormat="MM/dd/yyyy h:mm aa"
                            showTimeInput
                            name="scheduleDate"
                            label="Schedule Date"
                            variant="outlined"
                            value={userCred.scheduleDate}
                        />
                    </Grid>
                    <Grid item xs={1} md={2}>
                        <FormGroup>
                            <FormControlLabel
                                control={
                                    <Checkbox
                                        name="reoccurrence"
                                        onChange={handleReoccurrenceInput}
                                        value={reoccurrence}
                                    />
                                }
                                label="Reoccurrence"
                            />
                        </FormGroup>
                    </Grid>
                    <Grid item xs={12} md={2}>
                        <Select
                            isDisabled={false}
                            isLoading={false}
                            isClearable
                            isSearchable={false}
                            placeholder="Frequency"
                            defaultValue=""
                            name="dayFrequency"
                            label="Frequency"
                            variant="outlined"
                            options={frequency}
                            onChange={handleDayFrequencyChange}
                        />
                    </Grid>
                    <Grid item xs={12} md={2}>
                        <Typography> Select End Date and Time</Typography>
                    </Grid>
                    <Grid item xs={1} md={2}>
                        <DatePicker
                            multiline
                            selected={scheduleEndDate}
                            onChange={(date) => setScheduleEndDate(date)}
                            timeInputLabel="Time:"
                            dateFormat="MM/dd/yyyy h:mm aa"
                            showTimeInput
                            name="scheduleEndDate"
                            label="Schedule End Date"
                            variant="outlined"
                            value={userCred.scheduleEndDate}
                        />
                    </Grid>
                    <Grid item xs={1} md={2}>
                        <Typography>Reoccurrence of every</Typography>
                    </Grid>
                    <Grid item xs={3} md={1}>
                        <TextField
                            size="small"
                            fullWidth
                            multiline
                            maxRows={4}
                            name="timeOccurrence"
                            label="Time"
                            variant="outlined"
                            value={userCred.timeOccurrence}
                            error={
                                userCred.timeOccurrence === "" &&
                                ifFormTouched === FormStatus.MODIFIED
                            }
                            onChange={handleInput}
                        />
                    </Grid>
                    <Grid item xs={12} md={1}>
                        <Select
                            isDisabled={false}
                            isLoading={false}
                            isClearable
                            isSearchable={false}
                            placeholder="Frequency"
                            defaultValue=""
                            name="timeFrequency"
                            label="Frequency"
                            variant="outlined"
                            options={frequencyDay}
                            onChange={handleTimeFrequencyChange}
                        />
                    </Grid>
                    <Grid item xs={3} md={2}>
                        <TextField
                            size="small"
                            fullWidth
                            multiline
                            maxRows={4}
                            name="numOccurrence"
                            label="Number of occurrences"
                            variant="outlined"
                            value={userCred.numOccurrence}
                            error={
                                userCred.numOccurrence === "" &&
                                ifFormTouched === FormStatus.MODIFIED
                            }
                            onChange={handleInput}
                        />
                    </Grid>
                    <Grid item md={6}></Grid>
                    <Grid item md={1}></Grid>{" "}
                    <Grid item md={7}>
                        <Stack
                            direction="row"
                            spacing={2}
                            style={{ justifyContent: "space-evenly" }}
                        >
                            <Button
                                color="secondary"
                                variant="contained"
                                onClick={handleSubmit}
                                disabled={isLoading}
                            >
                                Add Schedules
                            </Button>
                            <Button
                                color="success"
                                variant="contained"
                                onClick={getScheduleDetails}
                            >
                                View Schedules
                            </Button>
                            <Button
                                color="success"
                                variant="contained"
                                onClick={getLastRunDetails}
                            >
                                Last Run Info
                            </Button>
                            <Button
                                color="primary"
                                variant="contained"
                                onClick={handleRecomm}
                            >
                                Recommendation
                            </Button>
                            <Button
                                color="success"
                                variant="contained"
                                onClick={redirectToHome}
                            >
                                Home
                            </Button>
                        </Stack>
                    </Grid>
                    <Grid item md={5}></Grid>
                    <Grid item md={2}>
                        {isLoading ? <LoadingSpinner /> : ""}
                    </Grid>
                    <Grid item md={4}></Grid>
                </Grid>
                {showTable && (
                    <Grid item xs={12}>
                        <Typography variant="h5">Last run details</Typography>
                    </Grid>
                )}
                <Grid item xs={12} md={6}>
                    {showTable && (
                        <div>
                            <TableContainer
                                component={Paper}
                                align="center"
                                className="dvttbl"
                            >
                                <Table
                                    sx={{
                                        minWidth: 1600,
                                        border: 1,
                                        borderColor: "primary.main",
                                        borderRadius: 2,
                                        width: 200,
                                    }}
                                    aria-label="simple table"
                                >
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Sl No.</TableCell>
                                            <TableCell align="center">
                                                Table Name
                                            </TableCell>
                                            <TableCell align="center">
                                                Table Total Rows
                                            </TableCell>
                                            <TableCell align="center">
                                                Missing Rows
                                            </TableCell>
                                            <TableCell align="center">
                                                Mismatch Rows
                                            </TableCell>
                                            <TableCell align="center">
                                                Unique Columns
                                            </TableCell>
                                            <TableCell align="center">
                                                Data Filter
                                            </TableCell>
                                            <TableCell align="center">
                                                Chunk Column
                                            </TableCell>
                                            <TableCell align="center">
                                                No. of Chunks
                                            </TableCell>
                                            <TableCell align="center">
                                                Incremental
                                            </TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        <TableRow
                                            key={tableData.slNo}
                                            sx={{
                                                "&:last-child td, &:last-child th":
                                                    { border: 0 },
                                            }}
                                        >
                                            <TableCell scope="row">
                                                {tableData.slNo}
                                            </TableCell>
                                            <TableCell
                                                className="tablename"
                                                align="center"
                                            >
                                                {tableData.tableName}
                                            </TableCell>
                                            <TableCell align="center">
                                                {tableData.totalRows}
                                            </TableCell>
                                            <TableCell align="center">
                                                {tableData.missingRows}
                                            </TableCell>
                                            <TableCell align="center">
                                                {tableData.mismatchRows}
                                            </TableCell>
                                            <TableCell align="center">
                                                {tableData.uniqueCols}
                                            </TableCell>
                                            <TableCell align="center">
                                                {tableData.dataFilters}
                                            </TableCell>
                                            <TableCell align="center">
                                                {tableData.chunkColumns}
                                            </TableCell>
                                            <TableCell align="center">
                                                {tableData.chunkSize}
                                            </TableCell>
                                            <TableCell align="center">
                                                {tableData.incremental}
                                            </TableCell>
                                        </TableRow>
                                    </TableBody>
                                </Table>{" "}
                                &nbsp;&nbsp; &nbsp;&nbsp;{" "}
                            </TableContainer>
                        </div>
                    )}
                </Grid>
                {showRunTable && (
                    <Grid item xs={12}>
                        <Typography variant="h5">Last run details</Typography>
                    </Grid>
                )}
                <Grid item xs={12} md={6}>
                    {showRunTable && (
                        <div style={{ marginTop: 10, marginBottom: 10 }}>
                            <TableContainer
                                component={Paper}
                                align="center"
                                className="dvttbl"
                            >
                                <Table
                                    sx={{
                                        minWidth: 1600,
                                        border: 1,
                                        borderColor: "primary.main",
                                        borderRadius: 2,
                                        width: 200,
                                    }}
                                    aria-label="simple table"
                                >
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Sl No.</TableCell>
                                            <TableCell align="center">
                                                Table Name
                                            </TableCell>
                                            <TableCell align="center">
                                                Table Total Rows
                                            </TableCell>
                                            <TableCell align="center">
                                                Missing Rows
                                            </TableCell>
                                            <TableCell align="center">
                                                Mismatch Rows
                                            </TableCell>
                                            <TableCell align="center">
                                                Run Date
                                            </TableCell>
                                            <TableCell align="center">
                                                Unique Columns
                                            </TableCell>
                                            <TableCell align="center">
                                                Data Filter
                                            </TableCell>
                                            <TableCell align="center">
                                                Chunk Column
                                            </TableCell>
                                            <TableCell align="center">
                                                No. of Chunks
                                            </TableCell>
                                            <TableCell align="center">
                                                Incremental
                                            </TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {runTableData.map((element, index) => {
                                            var incrtext = "";
                                            if (element.incremental) {
                                                incrtext = "Yes";
                                            } else {
                                                incrtext = "No";
                                            }
                                            return (
                                                <TableRow
                                                    key={index + 1}
                                                    sx={{
                                                        "&:last-child td, &:last-child th":
                                                            { border: 0 },
                                                    }}
                                                >
                                                    <TableCell scope="row">
                                                        {index + 1}
                                                    </TableCell>
                                                    <TableCell scope="row">
                                                        {element.table}
                                                    </TableCell>
                                                    <TableCell align="center">
                                                        {element.totalRecords}
                                                    </TableCell>
                                                    <TableCell align="center">
                                                        {element.missingRows}
                                                    </TableCell>
                                                    <TableCell align="center">
                                                        {element.mismatchRows}
                                                    </TableCell>
                                                    <TableCell align="center">
                                                        {element.lastRunDate}
                                                    </TableCell>
                                                    <TableCell align="center">
                                                        {element.uniqueCols}
                                                    </TableCell>
                                                    <TableCell align="center">
                                                        {element.dataFilters}
                                                    </TableCell>
                                                    <TableCell align="center">
                                                        {element.chunkColumns}
                                                    </TableCell>
                                                    <TableCell align="center">
                                                        {element.chunkSize}
                                                    </TableCell>
                                                    <TableCell align="center">
                                                        {incrtext}
                                                    </TableCell>
                                                </TableRow>
                                            );
                                        })}
                                    </TableBody>
                                </Table>{" "}
                                &nbsp;&nbsp; &nbsp;&nbsp;{" "}
                            </TableContainer>
                        </div>
                    )}
                </Grid>
                {showScheduleTable && (
                    <Grid item xs={12}>
                        <Typography variant="h5">Schedule Details</Typography>
                    </Grid>
                )}
                <Grid item xs={12} md={6}>
                    {showScheduleTable && (
                        <div style={{ marginTop: 10, marginBottom: 10 }}>
                            <TableContainer
                                component={Paper}
                                align="center"
                                className="dvttbl"
                            >
                                <Table
                                    sx={{
                                        minWidth: 1600,
                                        border: 1,
                                        borderColor: "primary.main",
                                        borderRadius: 2,
                                        width: 200,
                                    }}
                                    aria-label="simple table"
                                >
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Sl No.</TableCell>
                                            <TableCell align="center">
                                                Source Schema
                                            </TableCell>
                                            <TableCell align="center">
                                                Target Schema
                                            </TableCell>
                                            <TableCell align="center">
                                                Table
                                            </TableCell>
                                            <TableCell align="center">
                                                Schedule Time
                                            </TableCell>
                                            <TableCell align="center">
                                                Status
                                            </TableCell>
                                            <TableCell align="center">
                                                Run Id
                                            </TableCell>
                                            <TableCell align="center">
                                                Data Filter
                                            </TableCell>
                                            <TableCell align="center">
                                                Chunk Column
                                            </TableCell>
                                            <TableCell align="center">
                                                No. of Chunks
                                            </TableCell>
                                            <TableCell align="center">
                                                Duration
                                            </TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {scheduleTableData.map(
                                            (element, index) => {
                                                return (
                                                    <TableRow
                                                        key={index + 1}
                                                        sx={{
                                                            "&:last-child td, &:last-child th":
                                                                { border: 0 },
                                                        }}
                                                    >
                                                        <TableCell scope="row">
                                                            {index + 1}
                                                        </TableCell>
                                                        <TableCell scope="row">
                                                            {
                                                                element.sourceSchemaName
                                                            }
                                                        </TableCell>
                                                        <TableCell scope="row">
                                                            {
                                                                element.targetSchemaName
                                                            }
                                                        </TableCell>
                                                        <TableCell scope="row">
                                                            {element.table}
                                                        </TableCell>
                                                        <TableCell align="center">
                                                            {
                                                                element.scheduleTime
                                                            }
                                                        </TableCell>
                                                        <TableCell align="center">
                                                            {element.status}
                                                        </TableCell>
                                                        <TableCell align="center">
                                                            {element.runId}
                                                        </TableCell>
                                                        <TableCell align="center">
                                                            {
                                                                element.dataFilters
                                                            }
                                                        </TableCell>
                                                        <TableCell align="center">
                                                            {
                                                                element.chunkColumns
                                                            }
                                                        </TableCell>
                                                        <TableCell align="center">
                                                            {element.chunkSize}
                                                        </TableCell>
                                                        <TableCell align="center">
                                                            {element.duration}
                                                        </TableCell>
                                                    </TableRow>
                                                );
                                            }
                                        )}
                                    </TableBody>
                                </Table>{" "}
                                &nbsp;&nbsp; &nbsp;&nbsp;{" "}
                            </TableContainer>
                        </div>
                    )}
                </Grid>
            </Box>
        </div>
    );
}

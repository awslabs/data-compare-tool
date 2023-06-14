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
import {
    containerClasses,
    Autocomplete,
    Accordion as MuiAccordion,
    AccordionSummary,
    AccordionDetails,
} from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import dummyInputData from "./dummy_data.json";
import { FormStatus } from "./static_data";
import { Link } from "react-router-dom";
import LoadingSpinner from "../LoadingSpinner";
import { styled } from "@mui/material/styles";
import "../styles.css";
// import logo from "../dart-logo.jpg";

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
    const [exTables, setExTables] = useState(false);
    const [exColumns, setExColumns] = useState(false);
    const [tableNames, setTableNames] = useState("");
    const [tableName, setTableName] = useState("");
    const [schemaName, setSchemaName] = useState("");
    const [srcSchemaName, setSrcSchemaName] = useState("");
    const [checkAdditionalRows, setCheckAdditionalRows] = useState(false);
    const [incremental, setIncremental] = useState(false);
    const [data, setData] = useState([{}]);

    const Accordion = styled((props) => (
        <MuiAccordion disableGutters elevation={0} square {...props} />
    ))(({ theme }) => ({
        border: `1px solid ${theme.palette.divider}`,
        borderRadius: "8px",
        // background: "#fafafa",
    }));

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
    const handleAdditionalRowsInput = (event) => {
        setCheckAdditionalRows(event.target.checked);
    };
    const handleSrcSchemaChange = (event, selectedOption) => {
        if (selectedOption != null) {
            setSrcSchemaName(selectedOption?.value);
        } else {
            setSrcSchemaName("");
        }
    };
    // function redirectToHome(event) {
    //     navigate("/dvt/menu");
    // }
    function handleTableInput(event, selectedOption) {
        let value = Array.from(selectedOption, (item) => item.value);
        let length = value.length;
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
        setShowTable(false);
        let requestParams = { method: "POST", headers: "", body: "" };
        requestParams.headers = { "Content-Type": "application/json" };
        requestParams.body = JSON.stringify({
            sourceSchemaName: srcSchemaName,
            targetSchemaName: schemaName,
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

    const handleSchemaChange = (event, selectedOption) => {
        let list = [];
        if (selectedOption != null) {
            initialValue.tableNames = [];
            list = pageDetails[0][0].schemaList.find(
                (schema) => schema?.schemaName == selectedOption?.value
            )?.tableList;
            for (let j = 0; j < list.length; j++) {
                list[j].label = list[j].tableName;
                list[j].value = list[j].tableName;
            }
            setSchemaName(selectedOption?.value);
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
            tableNames: tableNames,
            columns: userCred.columnNames,
            ignoreColumns: exColumns,
            dataFilters: userCred.dataFilters,
            uniqueCols: userCred.uniqueCols,
            chunkColumns: userCred.chunkColumns,
            ignoreTables: exTables,
            chunkSize: userCred.fetchSize,
            incremental: incremental,
            checkAdditionalRows: checkAdditionalRows,
        });
        console.log("Data To Submit == ", JSON.stringify(requestParams));
        fetch("http://localhost:8090/dvt/validation/compareData", requestParams)
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
                alert("Validation Successful");
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
    // const handleRecomm = () => {
    //     navigate("/dvt/selection");
    // };
    const [state] = useReducer(reducer);

    return (
        <>
            <Typography variant="h5" className="heading">
                Validate
            </Typography>
            <Box>
                <Accordion>
                    <AccordionSummary
                        expandIcon={<ExpandMoreIcon />}
                        aria-controls="panel1a-content"
                        id="panel1a-header"
                    >
                        <Typography>Schema Details</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                        <Grid
                            container
                            mb={"20px"}
                            spacing={2}
                            columnSpacing={{ xs: 2 }}
                            justifyContent="left"
                            alignItems="center"
                        >
                            <Grid item xs={12} md={6}>
                                <TextField
                                    fullWidth
                                    autoFocus
                                    size="small"
                                    required
                                    name="hostname"
                                    label="Hostname"
                                    variant="outlined"
                                    value={userCred.hostname}
                                    error={
                                        userCred.hostname === "" &&
                                        ifFormTouched === FormStatus.MODIFIED
                                    }
                                    onChange={handleInput}
                                />
                            </Grid>
                            <Grid item xs={12} md={2}>
                                <TextField
                                    fullWidth
                                    size="small"
                                    required
                                    name="port"
                                    label="Port"
                                    type="number"
                                    variant="outlined"
                                    value={userCred.port}
                                    error={
                                        userCred.port === "" &&
                                        ifFormTouched === FormStatus.MODIFIED
                                    }
                                    onChange={handleInput}
                                    onKeyPress={(e) =>
                                        !/[0-9]/.test(e.key) &&
                                        e.preventDefault()
                                    }
                                />
                            </Grid>
                            <Grid item xs={12} md={2}>
                                <TextField
                                    p={0}
                                    fullWidth
                                    size="small"
                                    required
                                    name="dbname"
                                    label="Database"
                                    variant="outlined"
                                    value={userCred.dbname}
                                    error={
                                        userCred.dbname === "" &&
                                        ifFormTouched === FormStatus.MODIFIED
                                    }
                                    onChange={handleInput}
                                />
                            </Grid>
                            <Grid item xs={12} md={2}>
                                <FormGroup>
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                name="usessl"
                                                value={userCred.usessl}
                                                onChange={handleInput}
                                            />
                                        }
                                        label="SSL Mode"
                                    />
                                </FormGroup>
                            </Grid>
                            <Grid item xs={12} md={3}>
                                <TextField
                                    fullWidth
                                    size="small"
                                    required
                                    name="username"
                                    label="Username"
                                    variant="outlined"
                                    value={userCred.username}
                                    error={
                                        userCred.username === "" &&
                                        ifFormTouched === FormStatus.MODIFIED
                                    }
                                    onChange={handleInput}
                                />
                            </Grid>
                            <Grid item xs={12} md={3}>
                                <TextField
                                    fullWidth
                                    size="small"
                                    required
                                    name="password"
                                    label="Password"
                                    type="password"
                                    variant="outlined"
                                    value={userCred.password}
                                    error={
                                        userCred.password === "" &&
                                        ifFormTouched === FormStatus.MODIFIED
                                    }
                                    onChange={handleInput}
                                />
                            </Grid>
                            {/* </Grid> */}
                            <Grid item xs={12} md={4}></Grid>
                        </Grid>
                    </AccordionDetails>
                </Accordion>
                <Grid
                    container
                    mb={"20px"}
                    mt={"20px"}
                    spacing={2}
                    columnSpacing={{ xs: 2 }}
                    justifyContent="left"
                    alignItems="center"
                >
                    <Grid item xs={12}>
                        <Typography> Table Details </Typography>
                    </Grid>
                    <Grid item xs={12} md={3}>
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

                    <Grid item xs={12} md={3}>
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
                    </Grid>

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
                    </Grid>
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
                    <Grid item xs={12} md={3} lg={2}>
                        <FormGroup>
                            <FormControlLabel
                                control={
                                    <Checkbox
                                        name="incremental"
                                        onChange={handleIncrementalInput}
                                        value={incremental}
                                    />
                                }
                                label="Incremental Run"
                            />
                        </FormGroup>
                    </Grid>
                    <Grid item xs={12} md={3} lg={2}>
                        <FormGroup>
                            <FormControlLabel
                                control={
                                    <Checkbox
                                        name="checkAdditionalRows"
                                        onChange={handleAdditionalRowsInput}
                                        value={checkAdditionalRows}
                                    />
                                }
                                label="Two Way"
                            />
                        </FormGroup>
                    </Grid>
                    <Grid item sm={12} md={12}>
                        <Stack
                            direction={{ md: "column", lg: "row" }}
                            spacing={2}
                            style={{ justifyContent: "center" }}
                        >
                            <Button
                                variant="contained"
                                onClick={handleSubmit}
                                disabled={isLoading}
                            >
                                Compare
                            </Button>
                            <Button
                                color="success"
                                variant="contained"
                                onClick={getLastRunDetails}
                            >
                                Last Run Info
                            </Button>
                        </Stack>
                    </Grid>
                    <Grid item sm={12} md={12} alignContent={"center"}>
                        {isLoading ? <LoadingSpinner /> : ""}
                    </Grid>
                </Grid>
                {showTable && (
                    <Grid item xs={12}>
                        <Typography variant="h6" className="heading">
                            Last run details
                        </Typography>
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
                                    mb={2}
                                    sx={{
                                        minWidth: 1600,
                                        border: 1,
                                        borderColor: "primary.main",
                                        borderRadius: 2,
                                        width: 200,
                                    }}
                                    aria-label="Last run information table"
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
                                </Table>
                            </TableContainer>
                        </div>
                    )}
                </Grid>
                {showRunTable && (
                    <Grid item xs={12}>
                        <Typography variant="h6" className="heading">
                            Last run details
                        </Typography>
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
                                    aria-label="Last run information table"
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
                                                            {
                                                                border: 0,
                                                            },
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
                                </Table>
                            </TableContainer>
                        </div>
                    )}
                </Grid>
            </Box>
        </>
    );
}

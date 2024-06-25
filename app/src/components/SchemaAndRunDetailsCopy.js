import React, { useReducer, useEffect, useState, useMemo } from "react";
import axios from 'axios';
import { Box, InputLabel, FormControl, Select, MenuItem, TableContainer, Paper, Table, TableHead, TableRow, TableCell, TableBody, Button, Grid, Typography, Stack } from "@mui/material";
import { Download as DownloadIcon, Construction as ConstructionIcon } from '@mui/icons-material';
import { useNavigate } from "react-router-dom";
import LoadingSpinner from "./LoadingSpinner";
import Pagination from './pagination/pagination';
import "./recommendation/css/Recommendation.css";
import "./styles.css";


const CLEAR = "clear";
const POPULATE_DATABASE = "populateDatabase";
const POPULATE_SCHEMA = "populateSchema";
const POPULATE_SCHEMA_RUN = "populateSchemaRun";
const POPULATE_TABLE = "populateTable";
const POPULATE_TABLE_RUN = "populateTableRun";
let data = [];

let PageSize = 2;
const data1 = {};
const initialState = {
  disableHostName: false,
  disableDBName: false,
  loadingDBName: false,
  disableSchemaName: true,
  loadingSchemaName: false,
  disableSchemaRun: true,
  loadingSchemaRun: false,
  disableTableName: true,
  loadingTableName: false,
  disableTableRun: true,
  loadingTableRun: false,
  dbNamesToBeLoaded: [],
  schemaNamesToBeLoaded: [],
  schemaRunsToBeLoaded: [],
  tableNamesToBeLoaded: [],
  tableRunsToBeLoaded: [],
  showTable: false,
};

function reducer(state, action) {
  console.log("ddddddd", data1);
  switch (action.type) {
    case POPULATE_DATABASE:
      return {
        ...state,
        disableDBName: false,
        loadingDBName: false,
        disableSchemaName: true,
        loadingSchemaName: false,
        disableSchemaRun: true,
        loadingSchemaRun: false,
        disableTableName: true,
        loadingTableName: false,
        disableTableRun: true,
        loadingTableRun: false,
        showTable: false,

        disableHostName: true,

        dbNamesToBeLoaded: action.hostName,
        schemaNamesToBeLoaded: [],
        schemaRunsToBeLoaded: [],
        tableNamesToBeLoaded: [],
        tableRunsToBeLoaded: [],
      };
    case POPULATE_SCHEMA:
      return {
        ...state,

        disableDBName: false,
        loadingDBName: false,
        loadingSchemaName: false,
        disableSchemaRun: true,
        loadingSchemaRun: false,
        disableTableName: true,
        loadingTableName: false,
        disableTableRun: true,
        loadingTableRun: false,
        showTable: false,

        disableHostName: true,
        disableSchemaName: false,

        schemaNamesToBeLoaded: state.dbNamesToBeLoaded.find((dbname) => dbname.value === action.dbName).schemaList, //this has to be same in handler
        schemaRunsToBeLoaded: [],
        tableNamesToBeLoaded: [],
        tableRunsToBeLoaded: [],
      };
    case POPULATE_SCHEMA_RUN:
      return {
        ...state,
        disableDBName: false,
        loadingDBName: false,
        loadingSchemaName: false,
        disableSchemaRun: true,
        loadingSchemaRun: false,
        disableTableName: true,
        loadingTableName: false,
        disableTableRun: true,
        loadingTableRun: false,
        showTable: false,

        disableHostName: true,
        disableSchemaName: false,

        schemaRunsToBeLoaded: state.schemaNamesToBeLoaded.find((schema) => schema.value === action.schemaName).schemaRun,
        tableNamesToBeLoaded: [],
        tableRunsToBeLoaded: [],
        //this has to be same in handler
      };
    case POPULATE_TABLE:
      return {
        ...state,
        disableDBName: false,
        loadingDBName: false,
        loadingSchemaName: false,
        disableSchemaRun: true,
        loadingSchemaRun: false,
        loadingTableName: false,
        disableTableRun: true,
        loadingTableRun: false,
        showTable: false,

        disableHostName: true,
        disableSchemaName: false,
        disableTableName: false,

        tableNamesToBeLoaded: state.schemaNamesToBeLoaded.find((schema) => schema.value === action.schemaName).tableList,
        tableRunsToBeLoaded: [],
      };
    case POPULATE_TABLE_RUN:
      return {
        ...state,

        disableDBName: false,
        loadingDBName: false,
        loadingSchemaName: false,
        disableSchemaRun: true,
        loadingSchemaRun: false,
        loadingTableName: false,
        loadingTableRun: false,

        disableHostName: true,
        disableSchemaName: false,
        disableTableName: false,
        disableTableRun: false,
        showTable: true,

        tableRunsToBeLoaded: state.tableNamesToBeLoaded.find(obj => obj.tableName === action.tableName).tableRun,
      };
    case CLEAR:
    default:
      return initialState;
  }
}

function Nestedselect() {
  const [isLoading, setIsLoading] = useState(false);
  const [post, setPost] = useState([]);
  const [tableData, setTableData] = useState(data);
  const [currentPage, setCurrentPage] = useState(1);
  //const API = 'https://mocki.io/v1/e29d853b-1a21-456d-b8a3-35d5f27da66f';
  const API = 'http://localhost:8090/dvt/recommendation/recommendation-selection';
  const [file, setFile] = useState()

  const [hostNameSelected, setHostNameSelected] = useState("");
  const [dbNameSelected, setDbNameSelected] = useState("");
  const [schemaNameSelected, setSchemaNameSelected] = useState("");
  const [schemaRunSelected, setSchemaRunSelected] = useState("");
  const [tableNameSelected, setTableNameSelected] = useState("");
  const [tableRunSelected, setTableRunSelected] = useState("");

  const [state, dispatch] = useReducer(reducer, initialState);
  const navigate = useNavigate();


  function handleChange(event) {
    setFile(event.target.files[0])
  }

  function redirectToValidation(event) {
    // navigate('/dvt/compare');
    window.location.href = '/dvt/compare';
  }

  // function handleDataSync(event) {
  //   //event.preventDefault();
  //   setIsLoading(true);
  //   let requestParams = { method: "POST", headers: "", body: "" };
  //   requestParams.headers = { "Content-Type": "application/json" };
  //   requestParams.body = JSON.stringify({
  //     tableName: event.target.value,
  //   });

  //   console.log("Data To Submit == ", JSON.stringify(requestParams));
  //   fetch('http://localhost:8090/dvt/validation/compareData', requestParams)

  //     .then((response) => {
  //       if (response.ok) {
  //         return response.text();
  //       }
  //     })
  //     .then((resultData) => {
  //       setIsLoading(false);
  //       let msg = (resultData !== null || resultData !== '') ? resultData : "Something went wrong, please try again";
  //       alert(msg);
  //       // navigate("/dvt/selection");
  //     })
  //     .catch((error) => {
  //       //setErrorMessage("Unable to validate the data");
  //       setIsLoading(false);
  //       alert("Something went wrong, please try again");
  //       console.log("Error ::", error);
  //     });
  // }

  function handleSubmit(event) {
    setIsLoading(true);
    event.preventDefault()
    const url = 'http://localhost:8090/dvt/recommendation/upload';
    const formData = new FormData();
    formData.append('file', file);
    formData.append('fileName', file.name);
    const config = {
      headers: {
        'content-type': 'multipart/form-data',
      },
    };

    axios.post(url, formData, config).then((response) => {
      console.log(response.data);
      setIsLoading(false);
      alert("uploaded successfully")
      window.location.reload();
      navigate("/dvt/selection");
    });

  }

  // const downloadReport = (event) => {
  //   setIsLoading(true);
  //   let requestParams = { method: "GET", headers: "", body: "" };
  //   requestParams.headers = { "Content-Type": "application/json" };
  //   //  requestParams.body =   JSON.stringify({
  //   // schemaName:event.target.
  //   // targetSchemaName:
  //   // tableName:
  //   // runId : event.target.
  //   //  });
  //   console.log("Data To Submit == ", JSON.stringify(requestParams));
  //   fetch('http://localhost:8090/dvt/validation/exportData?runId=' + event.target.value)
  //     .then((response) => {
  //       setIsLoading(false);

  //       if (response.ok) {
  //         return response.text();
  //       }
  //     })
  //     .then((resultData) => {
  //       let msg = resultData == "Success" ? " Excel report downloaded Successfully " : "Excel report downloaded Successfully";
  //       alert(msg)
  //     })
  //     .catch((error) => {
  //       setIsLoading(false);
  //       console.log("Error ::", error);
  //     });
  // }
  //
  const fetchPost = () => {
    fetch(API)
      .then((res) => res.json())
      .then((res) => {
        console.log("data....", res);
        data1.hostDetailsList = res.hostDetailsList;
        for (let i = 0, len = data1.hostDetailsList.length; i < len; i++) {
          data1.hostDetailsList[i].label = data1.hostDetailsList[i].hostName;
          data1.hostDetailsList[i].value = data1.hostDetailsList[i].hostName;
          for (let j = 0, len1 = data1.hostDetailsList[i].databaseList.length; j < len1; j++) {
            data1.hostDetailsList[i].databaseList[j].label = data1.hostDetailsList[i].databaseList[j].databaseName;
            data1.hostDetailsList[i].databaseList[j].value = data1.hostDetailsList[i].databaseList[j].databaseName;
            for (let k = 0, len3 = data1.hostDetailsList[i].databaseList[j].schemaList.length; k < len3; k++) {
              data1.hostDetailsList[i].databaseList[j].schemaList[k].label = data1.hostDetailsList[i].databaseList[j].schemaList[k].schemaName;
              data1.hostDetailsList[i].databaseList[j].schemaList[k].value = data1.hostDetailsList[i].databaseList[j].schemaList[k].schemaName;
              //let schemaRunArr = [];
              for (let m = 0, len4 = data1.hostDetailsList[i].databaseList[j].schemaList[k].schemaRun.length; m < len4; m++) {
                // let schemaObj = {};
                // schemaObj.label = data1.hostDetailsList[i].databaseList[j].schemaList[k].schemaRun[m];
                // schemaObj.value = data1.hostDetailsList[i].databaseList[j].schemaList[k].schemaRun[m]
                data1.hostDetailsList[i].databaseList[j].schemaList[k].schemaRun[m].label = data1.hostDetailsList[i].databaseList[j].schemaList[k].schemaRun[m].run;
                data1.hostDetailsList[i].databaseList[j].schemaList[k].schemaRun[m].value = data1.hostDetailsList[i].databaseList[j].schemaList[k].schemaRun[m].run;
                //schemaRunArr.push(schemaObj);
              }
              //data1.hostDetailsList[i].databaseList[j].schemaList[k].schemaRunArray = schemaRunArr;
              //console.log('tablename',data1.hostDetailsList[i].databaseList[j].schemaList[k].tableList)
              for (let n = 0, len4 = data1.hostDetailsList[i].databaseList[j].schemaList[k].tableList.length; n < len4; n++) {
                //console.log('tablename',data1.hostDetailsList[i].databaseList[j].schemaList[k].tableList[n].tableName)
                data1.hostDetailsList[i].databaseList[j].schemaList[k].tableList[n].label = data1.hostDetailsList[i].databaseList[j].schemaList[k].tableList[n].tableName;
                data1.hostDetailsList[i].databaseList[j].schemaList[k].tableList[n].value = data1.hostDetailsList[i].databaseList[j].schemaList[k].tableList[n].tableName;
                //let tableRunArr = [];
                for (let p = 0, len5 = data1.hostDetailsList[i].databaseList[j].schemaList[k].tableList[n].tableRun.length; p < len5; p++) {
                  // let tableObj = {};
                  // tableObj.label = data1.hostDetailsList[i].databaseList[j].schemaList[k].tableList[n].tableRun[p];
                  // tableObj.value = data1.hostDetailsList[i].databaseList[j].schemaList[k].tableList[n].tableRun[p];
                  data1.hostDetailsList[i].databaseList[j].schemaList[k].tableList[n].tableRun[p].label = data1.hostDetailsList[i].databaseList[j].schemaList[k].tableList[n].tableRun[p].run;
                  data1.hostDetailsList[i].databaseList[j].schemaList[k].tableList[n].tableRun[p].value = data1.hostDetailsList[i].databaseList[j].schemaList[k].tableList[n].tableRun[p].run;
                  //tableRunArr.push(tableObj);
                }
                //data1.hostDetailsList[i].databaseList[j].schemaList[k].tableList[n].tableRunArray = tableRunArr;
              }
            }
          }
        }
        setPost(res.hostDetailsList);
        console.log("post", { post });
        console.log("data1", data1.hostDetailsList);
      })
      .catch((error) => {
        console.log("Error ::", error);

      });
  };
  useEffect(() => {
    fetchPost();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleOnHostNameClick = (event) => {
    if (event) {
      if (hostNameSelected !== event.target.value) {
        setDbNameSelected("");
        setSchemaNameSelected("");
        setSchemaRunSelected("");
        setTableNameSelected("");
        setTableRunSelected("");
      }
      setHostNameSelected(event.target.value);
      const dataBaseName = data1.hostDetailsList.find((hostname) => hostname.value === event.target.value).databaseList;
      dispatch({ type: POPULATE_DATABASE, hostName: dataBaseName });
    }
  };

  const handleOnDBNameClick = (event) => {
    if (event) {
      if (dbNameSelected !== event.target.value) {
        setSchemaNameSelected("");
        setSchemaRunSelected("");
        setTableNameSelected("");
        setTableRunSelected("");
      }
      setDbNameSelected(event.target.value);
      dispatch({ type: POPULATE_SCHEMA, dbName: event.target.value });
    }
  };

  const handleOnSchemaNameClick = (event) => {
    if (event) {
      if (schemaNameSelected !== event.target.value) {
        setSchemaRunSelected("");
        setTableNameSelected("");
        setTableRunSelected("");
      }
      setSchemaNameSelected(event.target.value);
      dispatch({ type: POPULATE_TABLE, schemaName: event.target.value });
    }
  };

  const handleOnSchemaRunClick = (event) => {
    // schemaRunSelected = event.value;
    setSchemaRunSelected(event.target.value)
    state.showTable = true;
    let tempArr = [];
    let slnumber = 1;
    const currentTableList = state.schemaNamesToBeLoaded.find((schema) => schema.value === schemaNameSelected).tableList;

    for (let i = 0; i < currentTableList.length; i++) {
      const currentTableRun = currentTableList[i].tableRun;
      for (let j = 0; j < currentTableRun.length; j++) {
        const tempTableRunData = currentTableRun[j];
        const obj = {
          slNo: slnumber,
          tableName: currentTableList[i].tableName,
          tableRun: tempTableRunData.run,
          runDate: tempTableRunData.executionDate,
          runId: tempTableRunData.runId,
          schemaName: tempTableRunData.schemaName,
        }
        slnumber++;
        tempArr.push(obj);
      }
    }
    setTableData(tempArr);
  };

  // const handleOnTableClickOld = (event) => {
  //   tableNameSelected = event.value;
  //   dispatch({ type: POPULATE_TABLE_RUN, tableName: event.value });
  // };
  const handleOnTableRunClick = (event) => {
    if (event) {
      const tempTableRunSelected = event.target.value;
      setTableRunSelected(tempTableRunSelected);
      let tempArr = [];
      let slnumber = 1;
      for (let i = 0; i < state.tableRunsToBeLoaded.length; i++) {
        setTableData(tempArr);
        const obj = {
          slNo: slnumber,
          tableName: state.tableNamesToBeLoaded.find(items => { return items.tableName === tableNameSelected; }).tableName,
          tableRun: state.tableRunsToBeLoaded[i].run,
          runDate: state.tableRunsToBeLoaded[i].executionDate,
          runId: state.tableRunsToBeLoaded[i].runId,
          schemaName: state.tableRunsToBeLoaded[i].schemaName,
        };
        console.log("random", obj.tableRun);
        if (obj.tableRun == tempTableRunSelected) {
          tempArr.push(obj);
          slnumber++;
        }
      }
      setTableData(tempArr);
    }
  };
  const handleOnTableClick = (event) => {
    if (event) {
      const tempTableNameSelected = event.target.value;
      if (tableNameSelected !== event.target.value) {
        setTableRunSelected("");
      }
      setTableNameSelected(tempTableNameSelected);
      dispatch({ type: POPULATE_TABLE_RUN, tableName: tempTableNameSelected });
      let tempArr = [];
      let slnumber = 1;
      const currentTableRun = state.tableNamesToBeLoaded.find(obj => obj.tableName === tempTableNameSelected).tableRun;

      for (let i = 0; i < currentTableRun.length; i++) {
        setTableData(tempArr);
        const obj = {
          slNo: slnumber,
          tableName: state.tableNamesToBeLoaded.find(items => items.tableName === tempTableNameSelected).tableName,
          tableRun: currentTableRun[i].run,
          runDate: currentTableRun[i].executionDate,
          runId: currentTableRun[i].runId,
          schemaName: currentTableRun[i].schemaName,
        }
        slnumber++;
        tempArr.push(obj);
      }
      setTableData(tempArr);
    }
  };

  const currentTableData = useMemo(() => {
    const firstPageIndex = (currentPage - 1) * PageSize;
    const lastPageIndex = firstPageIndex + PageSize;
    return tableData.slice(firstPageIndex, lastPageIndex);
    console.log('page table data', tableData)
  }, [currentPage, tableData]);
  // @ts-ignore

  const redirectToRecommendation = (event, runDetails) => {
    navigate('/dvt/recommend?runId=' + runDetails + '&page=1')
    //event.preventDefault();
    /* let requestParams = { method: "POST", headers: "", body: "" };
        requestParams.headers = { "Content-Type": "application/json" };
        requestParams.body =   JSON.stringify({
                                               schemaName : 'ops$ora',
                                               runId: '1874d6b164bfee40ecb60067a96063f0',
                                               tableName : event.target.value,
                                   });

        console.log("Data To Submit == ", JSON.stringify(requestParams));
         fetch('http://localhost:8090/dvt/recommendation/recommendation-data/v2', requestParams)

     .then((response) => {
             if (response.ok) {
               return response.text();
             }
           })
           .then((resultData) => {
             let msg = resultData !== null ? resultData : "Saved Sucessfully";
              navigate('/dvt/recommend?tableName='+event.target.value+'&runId=1874d6b164bfee40ecb60067a96063f0&schemaName=ops$ora&page=1')
           })
           .catch((error) => {
             console.log("Error ::", error);
           });


*/

  }

  const resetAllFields = () => {
    setHostNameSelected("");
    setDbNameSelected("");
    setSchemaNameSelected("");
    setSchemaRunSelected("");
    setTableNameSelected("");
    setTableRunSelected("");
    dispatch({ type: CLEAR });
  };

  return (
    <>
      <Typography variant="h5" className='heading' >
        Recommendation Details
      </Typography>

      <Box>
        <Grid container mb={2} spacing={2} columnSpacing={{ xs: 2 }} justifyContent="center" alignItems="center"></Grid>

        <Grid container spacing={2} columnSpacing={{ xs: 2 }}>
          <Grid item xs={6}>
            <FormControl size="small" fullWidth>
              <InputLabel id="select-hostName">Select HostName</InputLabel>
              <Select
                labelId="select-hostName"
                id="select-hostName-field"
                value={hostNameSelected}
                label="Select HostName"
                name="hostname"
                onChange={handleOnHostNameClick}
              >
                {data1?.hostDetailsList && data1?.hostDetailsList?.map((element, index) => {
                  return <MenuItem key={index} value={element.value}>{element.label}</MenuItem>
                })}

              </Select>
            </FormControl>


          </Grid>
          {!state.disableDBName && (
            <Grid item xs={6}>
              <FormControl size="small" fullWidth>
                <InputLabel id="select-database">Select Database</InputLabel>
                <Select
                  labelId="select-database"
                  id="select-database-field"
                  value={dbNameSelected}
                  label="Select Database"
                  name="database"
                  onChange={handleOnDBNameClick}
                >
                  {state?.dbNamesToBeLoaded && state?.dbNamesToBeLoaded?.map((element, index) => {
                    return <MenuItem key={index} value={element.value}>{element.label}</MenuItem>
                  })}

                </Select>
              </FormControl>
            </Grid>
          )}
          {!state.disableSchemaName && (
            <Grid item xs={5}>
              <FormControl size="small" fullWidth>
                <InputLabel id="select-schema">Select Schema</InputLabel>
                <Select
                  labelId="select-schema"
                  id="select-schema-field"
                  value={schemaNameSelected}
                  label="Select Schema"
                  name="schema"
                  onChange={handleOnSchemaNameClick}
                >
                  {state?.schemaNamesToBeLoaded && state?.schemaNamesToBeLoaded?.map((element, index) => {
                    return <MenuItem key={index} value={element.value}>{element.label}</MenuItem>
                  })}

                </Select>
              </FormControl>
            </Grid>
          )}
          {!state.disableSchemaRun && (
            <Grid item xs={3}>
              <FormControl size="small" fullWidth>
                <InputLabel id="select-schema-run">Select Schema Run</InputLabel>
                <Select
                  labelId="select-schema-run"
                  id="select-schema-run-field"
                  value={schemaRunSelected}
                  label="Select Schema Run"
                  name="schemaRun"
                  onChange={handleOnSchemaRunClick}
                >
                  {state?.schemaRunsToBeLoaded && state?.schemaRunsToBeLoaded?.map((element, index) => {
                    return <MenuItem key={index} value={element.value}>{element.label}</MenuItem>
                  })}

                </Select>
              </FormControl>
            </Grid>
          )}
          {!state.disableTableName && (
            <Grid item xs={5}>
              <FormControl size="small" fullWidth>
                <InputLabel id="select-table">Select Table</InputLabel>
                <Select
                  labelId="select-table"
                  id="select-table-field"
                  value={tableNameSelected}
                  label="Select Table"
                  name="table"
                  onChange={handleOnTableClick}
                >
                  {state?.tableNamesToBeLoaded && state?.tableNamesToBeLoaded?.map((element, index) => {
                    return <MenuItem key={index} value={element.value}>{element.label}</MenuItem>
                  })}

                </Select>
              </FormControl>
            </Grid>
          )}
          {!state.disableTableRun && (
            <Grid item xs={2}>
              <FormControl size="small" fullWidth>
                <InputLabel id="select-table-run">Select Table Run</InputLabel>
                <Select
                  labelId="select-table-run"
                  id="select-table-run-field"
                  value={tableRunSelected}
                  label="Select Table Run"
                  name="tableRun"
                  onChange={handleOnTableRunClick}
                >
                  {state?.tableRunsToBeLoaded && state?.tableRunsToBeLoaded?.map((element, index) => {
                    return <MenuItem key={index} value={element.value}>{element.label}</MenuItem>
                  })}

                </Select>
              </FormControl>
            </Grid>
          )}
        </Grid>
        <br />
        {state.showTable && (
          <div>
            <TableContainer component={Paper} align="center" className="dvttbl">
              {" "} &nbsp;&nbsp;{" "}{" "} &nbsp;&nbsp;{" "}
              <Table sx={{ minWidth: '1400px', border: 1, borderColor: "primary.main", borderRadius: 2, width: '100%', minWidth: '600px' }} aria-label="Last run information table">
                <TableHead>
                  <TableRow>
                    <TableCell>Sl No.</TableCell>
                    <TableCell align="left">Table Name</TableCell>
                    <TableCell align="left">Table Run</TableCell>
                    <TableCell align="left">Run Date</TableCell>
                    <TableCell align="left">Action</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {currentTableData.map((row) => (
                    <TableRow key={row.slNo} sx={{ "&:last-child td, &:last-child th": { border: 0 } }}>
                      <TableCell scope="row">{row.slNo}</TableCell>
                      <TableCell className="tablename" align="left">{row.tableName}</TableCell>
                      <TableCell align="left">{row.tableRun}</TableCell>
                      <TableCell align="left">{row.runDate}</TableCell>
                      <TableCell align="left">
                        <Box>
                          <Button
                            variant="outlined"
                            color="success" size='small'
                            value={row.runId + '&schemaName=' + row.schemaName + '&tableName=' + row.tableName}
                            onClick={(event) => redirectToRecommendation(event, row.runId + '&schemaName=' + row.schemaName + '&tableName=' + row.tableName)}
                            sx={{ mx: 0.5 }}
                          >
                            Remediate <ConstructionIcon size='small' sx={{ pl: 1 }} />
                          </Button>

                          {/* <Button
                            variant="outlined"
                            size='small'
                            value={row.runId + '&schemaName=' + row.schemaName + '&tableName=' + row.tableName}
                            onClick={downloadReport} disabled={isLoading}
                            sx={{ mx: 0.5 }}
                          >
                            Download
                          </Button> */}
                          <Button
                            variant="outlined"
                            size='small'
                            onClick={() => window.open('http://localhost:8090/dvt/validation/exportData?runId=' + row.runId + '&schemaName=' + row.schemaName + '&tableName=' + row.tableName)}
                            disabled={isLoading}
                            sx={{ mx: 0.5 }}
                          >
                            Download <DownloadIcon size='small' sx={{ pl: 1 }} />
                          </Button>
                        </Box>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
              {" "} &nbsp;&nbsp;{" "}{" "} &nbsp;&nbsp;{" "}

            </TableContainer>
            {" "} &nbsp;&nbsp;{" "}{" "} &nbsp;&nbsp;{" "}
            <Pagination
              className="pagination-bar"
              currentPage={currentPage}
              totalCount={tableData.length}
              pageSize={PageSize}
              onPageChange={page => setCurrentPage(page)}
            />
          </div>
        )}
        <Grid item xs={3}>
          <Stack direction="row" justifyContent="center" alignItems="center" spacing={2}>
            <Button variant="contained" onClick={redirectToValidation}>
              Compare
            </Button>
            <Button color="success" variant="contained" onClick={resetAllFields} >
              Reset
            </Button>
            <Box sx={{ border: 1, borderColor: "primary.main", borderRadius: 1 }}>
              <input type="file" name="file" variant="outlined" color="success" onChange={handleChange} />
              &nbsp;&nbsp;{" "}
              <Button vcolor="primary" variant="contained" onClick={handleSubmit} disabled={isLoading}>Upload</Button>
            </Box>
          </Stack>
          {" "} &nbsp;&nbsp;{" "}{" "} &nbsp;&nbsp;{" "}
        </Grid>
        {" "} &nbsp;&nbsp;{" "}{" "} &nbsp;&nbsp;{" "}
        <Grid item xs={3} align="center" valign="top">    {isLoading ? <LoadingSpinner /> : ""}</Grid>
        <Grid container mb={2} spacing={2} columnSpacing={{ xs: 2 }} justifyContent="center" alignItems="center"></Grid>
      </Box>
    </>
  );
}

export default Nestedselect;

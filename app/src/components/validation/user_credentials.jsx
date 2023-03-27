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
import { containerClasses } from "@mui/material";
import dummyInputData from "./dummy_data.json";
import { FormStatus } from "./static_data";
import { Link } from "react-router-dom";
import LoadingSpinner from "../LoadingSpinner";
import "../styles.css";
import logo from '../dart-logo.jpg'
const initialValue = {
  hostname: "ukpg-instance-1.cl7uqmhlcmfi.eu-west-2.rds.amazonaws.com",
  port: "5432",
  dbname: "ttp",
  usessl: false,
  username: "postgres",
  password: "postgres",
  schemaNames: ["ops$ora:crtdms"],
  tableNames: "ppt_100",
  columnNames: "id",
  exTables:false,
  exColumns:false,
  showTable:false,
  showRunTable:false,
};

const reducer = (userCred, action) => {
  switch (action.type) {
    case "update":
      if (["schemaNames", "tableNames", "columnNames"].indexOf(action.payload.key) > -1) {
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

          userCred : action.payload
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
  const [showTable, setShowTable]= useState(false);
  const [showRunTable, setShowRunTable]= useState(false);
  const [runTableData, setRunTableData] = useState([{}]);
  const [exTables, setExTables]= useState(false);
   const [exColumns, setExColumns]= useState(false);
   const [tableName, setTableName]= useState("");
   const [schemaName, setSchemaName]= useState("");
    const [srcSchemaName, setSrcSchemaName]= useState("");
  const [data, setData] = useState([{}]);


  const handleInput = (event) => {
    dispatch({
      type: "update",
      payload: { key: event.target.name, value: event.target.name === "usessl" ? !userCred.usessl : event.target.value },
    });
  };
  const handleTabExcludeInput = (event) => {
   setExTables(event.target.checked);
  };
    const handleColExcludeInput = (event) => {
       setExColumns(event.target.checked);
       }
 const handleTableSelection = (event) => {
   setTableName(event.value);
  };
 const handleSrcSchemaChange = (event) => {
       setSrcSchemaName(event.value);
       }

function handleTableInput (event){
setTableName(event.value);
 setIsLoading(true);
  setShowRunTable(false);
  setShowTable(false);
     let requestParams = { method: "POST", headers: "", body: "" };
             requestParams.headers = { "Content-Type": "application/json" };
             requestParams.body =   JSON.stringify({
                                                    sourceSchemaName : srcSchemaName,
                                                    tableName : event.value,
                                        });
             console.log("Data To Submit == ", JSON.stringify(requestParams));
              fetch('http://localhost:8090/dvt/validation/getRunInfo', requestParams)
          .then((response) => {
                       if (response.ok) {
                         return response.json();
                       }
                     })
                      .then((resultData) => {
                                setIsLoading(false);
                                    let slnumber = 1;
                                  let msg = (resultData !== null || resultData!=='')? resultData : "Something went wrong, please try again";
                                          let obj = {};
                                          obj.slNo = slnumber;
                                          slnumber++;
                                          console.log("slno", slnumber);
                                          obj.tableName = event.value;;
                                          obj.mismatchRows = resultData.mismatchRows;
                                          obj.missingRows = resultData.missingRows;
                                          obj.totalRows = resultData.totalRecords;
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


    let databaseList=[];
    let clearable = true;
    const handleSchemaChange = (event) =>{
        let list= [];
        if(event!=null){
            console.log('selectedschema '+event.value)
            //console.log('pageDetails  '+pageDetails[0][0].schemaList)
            initialValue.tableNames=[]
            list=pageDetails[0][0].schemaList.find((schema) =>schema.schemaName==event.value).tableList;
            //console.log(list);
            //   console.log('databaseList '+databaseList)
            //   let list=databaseList[0].schemaList.find((schema) =>schema.schemaName==event.value).tableList;
            for(let j=0;j<list.length;j++){
                list[j].label=list[j].tableName;
                list[j].value=list[j].tableName;
            }
            setSchemaName( event.value);
        }


      initialValue.tableNames=list;
      setDummy([]);
      console.log('initialValue.tableNames  =>   '+JSON.stringify(initialValue.tableNames))
        dispatch({
            type: "reset",
            payload: initialValue,
        });
    }

    const [pageDetails,setPageDetails] = useState([]);
    const [dummy,setDummy] = useState([]);
    const fetchPageDetails = () =>{
        fetch('http://localhost:8090/dvt/validation/dbDetails').then((res =>res.json()))//.then((res => JSON.stringify(res)))
            .then((res) =>{
                //setPageDetails(res.databaseList);
                pageDetails.push(res.databaseList);
                console.log('dbdetails '+JSON.stringify(pageDetails));
                //console.log('dbdetails123 '+JSON.stringify(pageDetails[0][0]));
                console.log('dbdetails123 '+pageDetails[0][0].databaseName);
                //initialValue.hostname = res.hostname;
                let details = initialValue;
                console.log('details    '+details.hostname);
                console.log('dbdetails1 '+res.hostName);
                let d= res
                console.log('d '+d.hostName)
                details.hostName=d;
                initialValue.hostname=res.hostName;
                initialValue.port = res.port;
                initialValue.username = res.username;
                initialValue.password = res.password;
                databaseList = res.databaseList;
                console.log('databaseList '+databaseList)
                console.log(res.databaseList[0].databaseName);
                initialValue.dbname=res.databaseList[0].databaseName;

                let schemaNamesArray = new Array();
                for(let i=0;i<res.databaseList[0].schemaList.length;i++){
                    let jsondata={};
                    jsondata.label=res.databaseList[0].schemaList[i].schemaName;
                    jsondata.value=res.databaseList[0].schemaList[i].schemaName;
                    schemaNamesArray.push(jsondata);
                }

                console.log('array '+schemaNamesArray)
                initialValue.schemaNames = schemaNamesArray;
                console.log('schemaNames'+initialValue.schemaNames)
                let tableList='';
                for(let i=0;i<res.databaseList[0].schemaList[0].tableList.length;i++){
                    tableList = tableList.concat(res.databaseList[0].schemaList[0].tableList[i].tableName+', ');
                }
                tableList.slice(0,-3);
                console.log(tableList);
                initialValue.tableNames=tableList;
                //initialva
                //initialValue.dbname = res.databaseList.
                console.log('details '+details.hostName)
                dispatch({
                    type: "set_default",
                    payload: details,
                });
            })
            .catch(err => {throw new Error(err)})
    }
    useEffect(() => {
        fetchPageDetails();
    }, []);


 function handleSubmit () {
    //event.preventDefault();
    setIsLoading(true);
     let requestParams = { method: "POST", headers: "", body: "" };
        requestParams.headers = { "Content-Type": "application/json" };
        requestParams.body =   JSON.stringify({
                                               targetSchemaName : schemaName,
                                               sourceSchemaName : srcSchemaName,
                                               targetDBName : userCred.dbname,
                                               targetHost : userCred.hostname,
                                               targetPort : userCred.port,
                                               targetUserName :userCred.username,
                                               targetUserPassword :userCred.password,
                                               tableName : tableName,
                                               columns: userCred.columnNames,
                                               ignoreColumns:exColumns,
                                               dataFilters:userCred.dataFilters,
                                               uniqueCols:userCred.uniqueCols,
                                               ignoreTables:exTables,

                                   });
        console.log("Data To Submit == ", JSON.stringify(requestParams));
         fetch('http://localhost:8090/dvt/validation/compareData', requestParams)

     .then((response) => {
             if (response.ok) {
               return response.text();
             }
           })
           .then((resultData) => {
           setIsLoading(false);
             let msg = (resultData !== null || resultData!=='')? resultData : "Something went wrong, please try again";
             alert(msg);
             navigate("/dvt/selection");
           })
           .catch((error) => {
           setErrorMessage("Unable to validate the data");
           setIsLoading(false);
            alert("Something went wrong, please try again");
             console.log("Error ::", error);
           });
       }
function getLastRunDetails () {
    //event.preventDefault();
     setShowTable(false);
    setIsLoading(true);
     let requestParams = { method: "POST", headers: "", body: "" };
        requestParams.headers = { "Content-Type": "application/json" };
        requestParams.body =   JSON.stringify({
                                               targetSchemaName : schemaName,
                                               sourceSchemaName : srcSchemaName,
                                               targetDBName : userCred.dbname,
                                               targetHost : userCred.hostname,
                                               targetPort : userCred.port,
                                               targetUserName :userCred.username,
                                               targetUserPassword :userCred.password,
                                               tableName : tableName,
                                               columns: userCred.columnNames,
                                               ignoreColumns:userCred.exColumns,
                                               ignoreTables:exTables,
                                               dataFilters:userCred.dataFilters,
                                               uniqueCols:userCred.uniqueCols
                                   });
        console.log("Data To Submit == ", JSON.stringify(requestParams));
         fetch('http://localhost:8090/dvt/validation/getLastRunDetails', requestParams)
     .then((response) => {
             if (response.ok) {
               return response.json();
             }
           })
           .then((response) => {
             let obj = {};
             obj.schemaName=response.schemaName;
obj.runs=response.runs;
           setRunTableData(response.runs)
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

const object = {"runInfo":[{"totalRecords":0,"table":"ppt_100","missingRows":2,"mismatchRows":3,"lastRunDate":"2023-03-01 12:41:30.163"},{"totalRecords":0,"table":"ppt_100","missingRows":2,"mismatchRows":3,"lastRunDate":"2023-02-28 15:19:50.387"},{"totalRecords":0,"table":"ppt_20","missingRows":0,"mismatchRows":0,"lastRunDate":"2023-02-24 13:23:07.334"},{"totalRecords":0,"table":"ppt_2","missingRows":0,"mismatchRows":0,"lastRunDate":"2023-02-24 13:22:40.587"},{"totalRecords":0,"table":"ppt_19","missingRows":0,"mismatchRows":0,"lastRunDate":"2023-02-24 13:22:13.871"},{"totalRecords":0,"table":"ppt_18","missingRows":0,"mismatchRows":0,"lastRunDate":"2023-02-24 13:21:47.144"},{"totalRecords":0,"table":"ppt_17","missingRows":0,"mismatchRows":0,"lastRunDate":"2023-02-24 13:21:20.391"},{"totalRecords":0,"table":"ppt_16","missingRows":0,"mismatchRows":0,"lastRunDate":"2023-02-24 13:20:53.7"},{"totalRecords":0,"table":"ppt_15","missingRows":0,"mismatchRows":0,"lastRunDate":"2023-02-24 13:20:26.97"},{"totalRecords":0,"table":"ppt_14","missingRows":0,"mismatchRows":0,"lastRunDate":"2023-02-24 13:20:00.299"}]}
console.log("runTableData",runTableData)
//console.log("runTableData 1 ",runTableData.runs.map((item) => item.table))
console.log(Array.isArray(runTableData))
//
// Instead, map over the items property rather than the parent object
//const result =runTableData.runInfo.map((item) => item.table);

//console.log(result); // ["item 1 data", "item 2 data"]
/*async handleTableInput(event) {
    event.preventDefault();
    const {item} = this.state;

    await fetch('http://localhost:8090/validation/compareData' + (item.id ? '/' + item.id : ''), {
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(item),
    });
    this.props.history.push('/dvt/selection');
}*/



  useEffect(() => {
    if (isEntireFormValid) {
      //console.log("User details: Valid" + JSON.stringify(userCred));

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
        ignoreTables:exTables,
        ignoreColumns:userCred.exColumns,
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
        .then((data) => {
          //console.log("Data is ", data);
          ////////////////////////////////////////////////////////////////// ---> navigate here to next page
        })
        .catch((error) => {
          console.log("An Unexpected Error occured..");
       })

      //navigate("http://localhost:8090/host-run-details/selection", userCred);
      navigate('/dvt/recommend',userCred);

    } else {
      console.log("User details: Invalid" + JSON.stringify(userCred));
    }
   }, [isEntireFormValid]);

  function handleSubmitold() {
    setIsEntireFormValid(null);
    if (ifFormTouched === FormStatus.UNTOUCHED) {
      setIfFormTouched(FormStatus.MODIFIED);
    }

    if (
      userCred.hostname === "" ||
      userCred.port === "" ||
      userCred.dbname === "" ||
      userCred.username === "" ||
      userCred.password === "" ||
      userCred.schemaNames.length === 0 ||
      userCred.tableNames.length === 0 ||
      userCred.columnNames.length === 0 ||
      userCred.dataFilters.length=== 0
    ) {
      setIsEntireFormValid(false);
    } else {
      setIsEntireFormValid(true);
    }
  }

  function handleReset() {
    setIfFormTouched(FormStatus.UNTOUCHED);
    dispatch({
      type: "reset",
    });
  }
 const handleRecomm = () => {
        navigate("/dvt/selection");
    }
      const [state] = useReducer(reducer);

  return (
    <div>
 <Grid container mb={2} spacing={1} columnSpacing={{ xs: 2 }} justifyContent="center" alignItems="center">
      <Grid item xs={12} sm={6} md={2}><img src={logo}  alt="Logo"  align="right" valign="bottom"/></Grid><Grid item xs={12} sm={6} md={10}>
        <Typography variant="h4" align="left" valign="bottom" >Data Validation And Remediation Tool (DVART) </Typography>
      </Grid></Grid>

      <Box mx={{ xs: 1, md: 10 }} px={{ xs: 2 }} sx={{ border: 1, borderColor: "primary.main", borderRadius: 2 }}>
        <Grid container mb={2} spacing={2} columnSpacing={{ xs: 2 }} justifyContent="center" alignItems="center">
          <Grid item xs={12}>
            <Typography></Typography>
          </Grid>
          <Grid item xs={12}>
            <Typography> Schema Details </Typography>
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              autoFocus
              size="small"
              required
              name="hostname"
              label="Hostname"
              variant="outlined"
              value={userCred.hostname}
              error={userCred.hostname === "" && ifFormTouched === FormStatus.MODIFIED}
              onChange={handleInput}
            />
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <TextField
              fullWidth
              size="small"
              required
              name="port"
              label="Port"
              type="number"
              variant="outlined"
              value={userCred.port}
              error={userCred.port === "" && ifFormTouched === FormStatus.MODIFIED}
              onChange={handleInput}
              onKeyPress={(e) => !/[0-9]/.test(e.key) && e.preventDefault()}
            />
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <TextField
              p={0}
              fullWidth
              size="small"
              required
              name="dbname"
              label="Database"
              variant="outlined"
              value={userCred.dbname}
              error={userCred.dbname === "" && ifFormTouched === FormStatus.MODIFIED}
              onChange={handleInput}
            />
          </Grid>
          <Grid item xs={12} md={4}>
            <FormGroup>
              <FormControlLabel control={<Checkbox name="usessl" value={userCred.usessl} onChange={handleInput} />} label="SSL Mode" />
            </FormGroup>
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <TextField
              fullWidth
              size="small"
              required
              name="username"
              label="Username"
              variant="outlined"
              value={userCred.username}
              error={userCred.username === "" && ifFormTouched === FormStatus.MODIFIED}
              onChange={handleInput}
            />
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <TextField
              fullWidth
              size="small"
              required
              name="password"
              label="Password"
              type="password"
              variant="outlined"
              value={userCred.password}
              error={userCred.password === "" && ifFormTouched === FormStatus.MODIFIED}
              onChange={handleInput}
            />
          </Grid>
          <Grid item xs={12} md={4}></Grid>
        </Grid>
        <Divider pb={2} />
        <Divider pt={2} />
        <Grid container mb={2} spacing={2} columnSpacing={{ xs: 2 }} justifyContent="center" alignItems="center">
          <Grid item xs={12}>
            <Typography> Table Details </Typography>
          </Grid>
          <Grid item xs={12} md={2}>
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
              <Select
                  isDisabled= {false}
                  isLoading= {false}
                  isClearable
                  isSearchable={false}
                  placeholder="Schema Names.."
                  defaultValue={userCred.schemaNames[0]}
                  name="schemas"
                  label="Target Schema"
                  variant="outlined"
                  options={userCred.schemaNames}
                  onChange={handleSchemaChange}
              />
          </Grid>

                    <Grid item xs={12} md={2}>
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
                                         <Select
                                             isDisabled= {false}
                                             isLoading= {false}
                                             isClearable
                                             isSearchable={false}
                                             placeholder="Schema Names.."
                                             defaultValue={userCred.schemaNames[0]}
                                             name="srcSchemas"
                                             label="Source Schema"
                                             variant="outlined"
                                             options={userCred.schemaNames}
                                             onChange={handleSrcSchemaChange}
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
              <Select
                  isDisabled= {false}
                  isLoading= {false}
                  isClearable = {clearable}
                  isSearchable={false}
                  placeholder="Table Names.."
                  defaultValue={userCred.tableNames[0]}
                  //value={''}
                  name="tables"
                  label="Table Name"
                   variant="outlined"
                  options={userCred.tableNames}
                  onChange={handleTableInput}
              />
          </Grid>
          <Grid item xs={12} md={1}>
                      <FormGroup>
                        <FormControlLabel control={<Checkbox name="exTables" value={exTables} onChange={handleTabExcludeInput} />} label="Exclude" />
                      </FormGroup>
                    </Grid>
          <Grid item xs={12} md={3}>
            <TextField
              fullWidth
              multiline
              maxRows={4}
              name="columnNames"
              label="Column Names"
              variant="outlined"
              value={userCred.columnNames}
              error={userCred.columnNames === '' && ifFormTouched === FormStatus.MODIFIED}
              onChange={handleInput}
            />
          </Grid>

          <Grid item xs={12} md={1}>
                      <FormGroup>
                        <FormControlLabel control={<Checkbox name="exColumns" value={exColumns} onChange={handleColExcludeInput} />} label="Exclude" />
                      </FormGroup>
                    </Grid>
    <Grid item xs={4} md={4}>
                    <TextField
                    fullWidth
                    multiline
                    maxRows={4}
                    name="uniqueCols"
                    label="Unique Columns"
                    variant="outlined"
                    value={userCred.uniqueCols}
                    error={userCred.uniqueCols === '' && ifFormTouched === FormStatus.MODIFIED}
                    onChange={handleInput}
                    />
                     </Grid>
                        <Grid item xs={12} md={8}>
                                          <TextField
                                            fullWidth
                                            multiline
                                            maxRows={4}
                                            name="dataFilters"
                                            label="Data Filters"
                                            variant="outlined"
                                            value={userCred.dataFilters}
                                            error={userCred.dataFilters === '' && ifFormTouched === FormStatus.MODIFIED}
                                            onChange={handleInput}
                                          />
                                        </Grid>


          <Grid item md={3}></Grid>
          <Grid item md={6}>
            <Stack direction="row" spacing={2} style={{ justifyContent: "space-evenly" }}>
              <Button color="secondary" variant="contained" onClick={handleSubmit} disabled={isLoading}>
                Compare
              </Button>
              <Button color="success" variant="contained" onClick={getLastRunDetails}>
                Last Run Info
              </Button>
              <Button color="primary" variant="contained" onClick={handleRecomm}>
                Recommendation
              </Button>
            </Stack>

          </Grid>
          <Grid item md={3}></Grid>

         <Grid item md={5}></Grid>
        <Grid item md={2}>{isLoading ? <LoadingSpinner /> : ""}</Grid>
        <Grid item md={4}></Grid>
        </Grid>
          { showTable && (
        <Grid item xs={12}>
                <Typography variant="h5">Last run details</Typography>
              </Grid> )}
<Grid item xs={12} md={6}>
 { showTable && (
        <div>
          <TableContainer component={Paper} align="center" className="dvttbl">
            <Table sx={{ minWidth: 900, border: 1, borderColor: "primary.main", borderRadius: 2, width: 200 }} aria-label="simple table">
              <TableHead>
                <TableRow>
                  <TableCell>Sl No.</TableCell>
                  <TableCell align="center">Table Name</TableCell>
                   <TableCell align="center">Table Total Rows</TableCell>
                  <TableCell align="center">Missing Rows</TableCell>
                  <TableCell align="center">Mismatch Rows</TableCell>

                </TableRow>
              </TableHead>
              <TableBody>
                  <TableRow key={tableData.slNo} sx={{ "&:last-child td, &:last-child th": { border: 0 } }}>
                    <TableCell scope="row">{tableData.slNo}</TableCell>
                    <TableCell className="tablename" align="center">{tableData.tableName}</TableCell>
                    <TableCell align="center">{tableData.totalRows}</TableCell>
                    <TableCell align="center">{tableData.missingRows}</TableCell>
                    <TableCell align="center">{tableData.mismatchRows}</TableCell>

                  </TableRow>

              </TableBody>
            </Table>
            {" "} &nbsp;&nbsp;{" "}
            {" "} &nbsp;&nbsp;{" "}
          </TableContainer>
        </div>
  )}
      </Grid>
  { showRunTable && (
        <Grid item xs={12}>
                <Typography variant="h5">Last run details</Typography>
              </Grid> )}
<Grid item xs={12} md={6}>
 { showRunTable && (
 <div style={{ marginTop: 10, marginBottom: 10 }}>
   <TableContainer component={Paper} align="center" className="dvttbl">
              <Table sx={{ minWidth: 900, border: 1, borderColor: "primary.main", borderRadius: 2, width: 200 }} aria-label="simple table">
                <TableHead>
                  <TableRow>
                    <TableCell>Sl No.</TableCell>
                    <TableCell align="center">Table Name</TableCell>
                     <TableCell align="center">Table Total Rows</TableCell>
                    <TableCell align="center">Missing Rows</TableCell>
                    <TableCell align="center">Mismatch Rows</TableCell>
                     <TableCell align="center">Run Date</TableCell>

                  </TableRow>
                </TableHead>
                <TableBody>
                 {runTableData.map((element,index) => {
                   return (
                 <TableRow key={index+1} sx={{ "&:last-child td, &:last-child th": { border: 0 } }}>
                                    <TableCell scope="row">{index+1}</TableCell>
                                    <TableCell scope="row">{element.table}</TableCell>
                                    <TableCell align="center">{element.totalRecords}</TableCell>
                                    <TableCell align="center">{element.missingRows}</TableCell>
                                     <TableCell align="center">{element.mismatchRows}</TableCell>
                                    <TableCell align="center">{element.lastRunDate}</TableCell>
                                  </TableRow>

   );
 })}
          </TableBody>
           </Table>
           {" "} &nbsp;&nbsp;{" "}{" "} &nbsp;&nbsp;{" "}
           </TableContainer>
      </div>
       )}
      </Grid>
 {" "} &nbsp;&nbsp;{" "}
      </Box>
    </div>
  );
}

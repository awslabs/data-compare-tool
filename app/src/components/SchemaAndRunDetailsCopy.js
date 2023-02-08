import React, { useReducer, useEffect, ChangeEvent,useState } from "react";
import Select from "react-select";
import TableContainer from "@mui/material/TableContainer";
import Paper from "@mui/material/Paper";
import Table from "@mui/material/Table";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import TableCell from "@mui/material/TableCell";
import TableBody from "@mui/material/TableBody";
import { Box, containerClasses } from "@mui/material";
import Button from "@mui/material/Button";
import Grid from "@mui/material/Grid";
import "./recommendation/css/Recommendation.css";
import {useNavigate} from "react-router-dom";
import axios from 'axios';
const CLEAR = "clear";

const POPULATE_DATABASE = "populateDatabase";
const POPULATE_SCHEMA = "populateSchema";
const POPULATE_SCHEMA_RUN = "populateSchemaRun";
const POPULATE_TABLE = "populateTable";
const POPULATE_TABLE_RUN = "populateTableRun";

let data = [
  {
    slNo: 1,
    tableName: "table1",
    tableRun: "Run-1",
    runDate: "2022-12-14",
  },
  {
    slNo: 2,
    tableName: "table2",
    tableRun: "Run-2",
    runDate: "2022-12-14",
  },
  {
    slNo: 3,
    tableName: "table3",
    tableRun: "Run-3",
    runDate: "2022-12-14",
  },
];

// export const data1 = {
//     'hostDetailsList': [{
//         hostName: 'ukpg-instance-1.cl7uqmhlcmfi.eu-west-2.rds.amazonaws.com', databaseList: [{
//             databaseName: 'ttp', schemaList: [{
//                 schemaName: 'ops$ora_2', schemaRun: [1], tableList: [{
//                     tableName: 'ppt_12', tableRun: [0]
//                 }, {
//                     tableName: 'ppt_11', tableRun: [0]
//                 }, {
//                     tableName: 'ppt_10', tableRun: [0]
//                 }]
//             }, {
//                 schemaName: 'ops$ora', schemaRun: [0, 1], tableList: [{
//                     tableName: 'ppt_12', tableRun: [1]
//                 }, {
//                     tableName: 'ppt_11', tableRun: [1]
//                 }, {
//                     tableName: 'ppt_10', tableRun: [1, 2]
//                 }]
//             }]
//         }, {
//             databaseName: 'ttp_2', schemaList: [{
//                 schemaName: 'ops$ora', schemaRun: [0, 1], tableList: [{
//                     tableName: 'ppt_11', tableRun: [0]
//                 }, {
//                     tableName: 'ppt_10', tableRun: [0, 2]
//                 }]
//             }]
//         }]
//     }, {
//         hostName: 'localhost', databaseList: [{
//             databaseName: 'ttp', schemaList: [{
//                 schemaName: 'ops$ora_2', schemaRun: [1], tableList: [{
//                     tableName: 'ppt_12', tableRun: [0]
//                 }, {
//                     tableName: 'ppt_11', tableRun: [0]
//                 }, {
//                     tableName: 'ppt_10', tableRun: [0]
//                 }]
//             }]
//         }]
//     }]
// }

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
const downloadReport =  (event) => {
    console.log('event '+event.target.value);
let requestParams = { method: "POST", headers: "", body: "" };
        requestParams.headers = { "Content-Type": "application/json" };
        requestParams.body =   JSON.stringify({
                                             // schemaName:event.target.
                                             // targetSchemaName:
                                             // tableName:
                                             // runId : event.target.
                                   });
        console.log("Data To Submit == ", JSON.stringify(requestParams));
         fetch('http://localhost:8090/validation/exportData', requestParams)

     .then((response) => {
     alert(response)
             if (response.ok) {
               return response.text();
             }
           })
           .then((resultData) => {
             let msg = resultData !== null ? resultData : "Saved Sucessfully";
           })
           .catch((error) => {
             console.log("Error ::", error);
           });
       }

       function uploadReport () {

           //event.preventDefault();
            let requestParams = { method: "POST", headers: "", body: "" };
               requestParams.headers = { "Content-Type": "application/json" };
               requestParams.body =   JSON.stringify({

                                                    //  targetSchemaName : userCred.schemaNames,

                                          });

               console.log("Data To Submit == ", JSON.stringify(requestParams));
                fetch('http://localhost:8090//recommendation/api/upload', requestParams)

            .then((response) => {
            alert(response)
                    if (response.ok) {
                      return response.text();
                    }
                  })
                  .then((resultData) => {
                    let msg = resultData !== null ? resultData : "Saved Sucessfully";
                  })
                  .catch((error) => {
                    console.log("Error ::", error);
                  });
              }
function reducer(state, action) {
  switch (action.type) {
    case POPULATE_DATABASE:
      return {
        ...state,
        disableDBName: false,
        loadingDBName: false,
        disableHostName: true,

        dbNamesToBeLoaded: data1.hostDetailsList.find((hostname) => hostname.value === action.hostName).databaseList,
        schemaNamesToBeLoaded:[],
        schemaRunsToBeLoaded: [],
        tableNamesToBeLoaded: [],
        tableRunsToBeLoaded: [],
      };
    case POPULATE_SCHEMA:
      return {
        ...state,
        disableSchemaName: false,
        loadingSchemaName: false,

        schemaNamesToBeLoaded: data1.hostDetailsList.find(() => hostNameSelected).databaseList.find((dbname) => dbname.value === action.dbName).schemaList, //this has to be same in handler
        schemaRunsToBeLoaded: [],
        tableNamesToBeLoaded: [],
        tableRunsToBeLoaded: [],

      };
    case POPULATE_SCHEMA_RUN:
      return {
        ...state,
        disableSchemaRun: false,
        loadingSchemaRun: false,

        schemaRunsToBeLoaded: data1.hostDetailsList
          .find(() => hostNameSelected)
          .databaseList.find(() => dbNameSelected)
          .schemaList.find((schema) => schema.value === action.schemaName).schemaRun,
        tableNamesToBeLoaded: [],
        tableRunsToBeLoaded: [],
        //this has to be same in handler
      };
    case POPULATE_TABLE:
      return {
        ...state,
        disableTableName: false,
        loadingTableName: false,

        tableNamesToBeLoaded: data1.hostDetailsList
          .find(() => hostNameSelected)
          .databaseList.find(() => dbNameSelected)
          .schemaList.find((schema) => schema.value === action.schemaName).tableList,
        tableRunsToBeLoaded: [],
      };
    case POPULATE_TABLE_RUN:
      return {
        ...state,
        disableTableRun: false,
        loadingTableRun: false,
        showTable: true,

        tableRunsToBeLoaded: data1.hostDetailsList
          .find(() => hostNameSelected)
          .databaseList.find(() => dbNameSelected)
          .schemaList.find(() => schemaNameSelected)
          .tableList.find((tableName) => tableName.value === action.tableName).tableRun,
      };
    case CLEAR:
    default:
      return initialState;
  }
}

export let hostNameSelected = "";
export let dbNameSelected = "";
export let schemaNameSelected = "";
export let schemaRunSelected = "";
export let tableNameSelected = "";
export let tableRunSelected = "";

function Nestedselect() {
  const [post, setPost] = useState([]);
  const [tableData, setTableData] = useState(data);
   //const API = 'https://mocki.io/v1/e29d853b-1a21-456d-b8a3-35d5f27da66f';
  const API = 'http://localhost:8090/dvt/recommendation/recommendation-selection';
   const [file, setFile] = useState()

     function handleChange(event) {
       setFile(event.target.files[0])
     }

     function handleSubmit(event) {
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
       });

     }

  const fetchPost = () => {
    fetch(API)
      .then((res) => res.json())
      .then((res) => {
        setPost(res.hostDetailsList);

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

        console.log("post", { post });
        console.log("data1", data1.hostDetailsList);
      })
      .catch((error) => {
                   console.log("Error ::", error);
                   alert(error);
                 });
  };
  useEffect(() => {
    fetchPost();
  }, []);

  const handleOnHostNameClick = (event) => {
    hostNameSelected = event.value;
    dispatch({ type: POPULATE_DATABASE, hostName: event.value });
  };

  const handleOnDBNameClick = (event) => {
    dbNameSelected = event.value;
    console.log("showtabledb", state.showTable);
    dispatch({ type: POPULATE_SCHEMA, dbName: event.value });
  };

  const handleOnSchemaNameClick = (event) => {
    schemaNameSelected = event.value;
    console.log("showtablesc", state.showTable);
    dispatch({ type: POPULATE_SCHEMA_RUN, schemaName: event.value });
    dispatch({ type: POPULATE_TABLE, schemaName: event.value });
  };

  const handleOnSchemaRunClick = (event) => {
    schemaRunSelected = event.value;
    console.log("showtable", state.showTable);
    state.showTable = true;
    //console.log()

    let tempArr = [];
    let slnumber = 1;
    for (
      let i = 0,
        len = data1.hostDetailsList
          .find(() => hostNameSelected)
          .databaseList.find(() => dbNameSelected)
          .schemaList.find((schema) => schema.value === schemaNameSelected).tableList.length;
      i < len;
      i++
    ) {
      for (
        let j = 0,
          len1 = data1.hostDetailsList
            .find(() => hostNameSelected)
            .databaseList.find(() => dbNameSelected)
            .schemaList.find((schema) => schema.value === schemaNameSelected).tableList[i].tableRun.length;
        j < len1;
        j++
      ) {
        let obj = {};
        obj.slNo = slnumber;
        slnumber++;
        console.log("slno", slnumber);
        obj.tableName = data1.hostDetailsList
          .find(() => hostNameSelected)
          .databaseList.find(() => dbNameSelected)
          .schemaList.find((schema) => schema.value === schemaNameSelected).tableList[i].tableName;
        obj.tableRun = data1.hostDetailsList
          .find(() => hostNameSelected)
          .databaseList.find(() => dbNameSelected)
          .schemaList.find((schema) => schema.value === schemaNameSelected).tableList[i].tableRun[j].run;

        obj.runDate = data1.hostDetailsList
          .find(() => hostNameSelected)
          .databaseList.find(() => dbNameSelected)
          .schemaList.find((schema) => schema.value === schemaNameSelected).tableList[i].tableRun[j].executionDate;
        console.log("random", obj);
        tempArr.push(obj);
      }
    }
    console.log("temparr", tempArr);
    setTableData(tempArr);
    console.log("data", data);
  };

  const handleOnTableClick = (event) => {
    tableNameSelected = event.value;
    dispatch({ type: POPULATE_TABLE_RUN, tableName: event.value });
  };

  const handleOnTableRunClick = (event) => {
    tableRunSelected = event.value;
    let tempArr = [];
    let slnumber = 1;

    for (
      let i = 0,
        len = data1.hostDetailsList
          .find(() => hostNameSelected)
          .databaseList.find(() => dbNameSelected)
          .schemaList.find(() => schemaNameSelected)
          .tableList.find(() => tableNameSelected).tableRun.length;
      i < len;
      i++
    ) {
      let obj = {};
      obj.slNo = slnumber;
      obj.tableName = data1.hostDetailsList
        .find(() => hostNameSelected)
        .databaseList.find(() => dbNameSelected)
        .schemaList.find(() => schemaNameSelected)
        .tableList.find(() => tableNameSelected).tableName;
      obj.tableRun = data1.hostDetailsList
        .find(() => hostNameSelected)
        .databaseList.find(() => dbNameSelected)
        .schemaList.find(() => schemaNameSelected)
        .tableList.find(() => tableNameSelected).tableRun[i].run;
      obj.runDate = data1.hostDetailsList
        .find(() => hostNameSelected)
        .databaseList.find(() => dbNameSelected)
        .schemaList.find(() => schemaNameSelected)
        .tableList.find(() => tableNameSelected).tableRun[i].executionDate;
      tempArr.push(obj);
    }
    setTableData(tempArr);
  };

  // @ts-ignore
  let navigate = useNavigate();
  const redirectToRecommendation =  (event) => {
    console.log('event '+event.target.value);

    navigate('http://localhost:8090/host-run-details/selection?table='+event.target.value+'&page=1')

  }
  const [state, dispatch] = useReducer(reducer, initialState);

  return (
    <Box mt={2} mx={10}>
      <Grid container spacing={2} columnSpacing={{ xs: 2 }}>
        <Grid item xs={12}>
          <Select
            isDisabled={state.disableHostName}
            isLoading={state.loadingDBName}
            isClearable
            isSearchable
            placeholder="Select HostName..."
            name="hostname"
            options={data1.hostDetailsList}
            onChange={handleOnHostNameClick}
          />
        </Grid>
        {!state.disableDBName && (
          <Grid item xs={12}>
            <Select
              isDisabled={state.disableDBName}
              isLoading={state.loadingSchemaName}
              isClearable
              isSearchable
              placeholder="Select Database..."
              name="database"
              options={state.dbNamesToBeLoaded}
              onChange={handleOnDBNameClick}
            />
          </Grid>
        )}
        {!state.disableSchemaName && (
          <Grid item xs={6}>
            <Select
              isDisabled={state.disableSchemaName}
              isLoading={state.loadingSchemaRun}
              isClearable
              isSearchable
              placeholder="Select Schema..."
              name="schema"
              options={state.schemaNamesToBeLoaded}
              onChange={handleOnSchemaNameClick}
            />
          </Grid>
        )}
        {!state.disableSchemaRun && (
          <Grid item xs={6}>
            <Select
              isDisabled={state.disableSchemaRun}
              isLoading={state.loadingTableName}
              isClearable
              isSearchable
              placeholder="Select Schema Run..."
              name="schemaRun"
              options={state.schemaRunsToBeLoaded}
              onChange={handleOnSchemaRunClick}
            />
          </Grid>
        )}
        {!state.disableTableName && (
          <Grid item xs={6}>
            <Select
              isDisabled={state.disableTableName}
              isLoading={state.loadingTableRun}
              isClearable
              isSearchable
              placeholder="Select Table..."
              name="table"
              options={state.tableNamesToBeLoaded}
              onChange={handleOnTableClick}
            />
          </Grid>
        )}
        {!state.disableTableRun && (
          <Grid item xs={6}>
            <Select
              isDisabled={state.disableTableRun}
              isLoading={false}
              isClearable
              isSearchable
              placeholder="Select Table Run..."
              name="tableRun"
              options={state.tableRunsToBeLoaded}
              onChange={handleOnTableRunClick}
            />
          </Grid>
        )}
      </Grid>
      <br />
      <br />
      {state.showTable && (
        <div>
          <TableContainer component={Paper} align="center" className="dvttbl">
            <Table sx={{ minWidth: 900, border: 1, borderColor: "primary.main", borderRadius: 2, width: 100 }} aria-label="simple table">
              <TableHead>
                <TableRow>
                  <TableCell>Sl No.</TableCell>
                  <TableCell align="right">Table Name</TableCell>
                  <TableCell align="right">Table Run</TableCell>
                  <TableCell align="right">Run Date</TableCell>
                  <TableCell align="center">Action</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {tableData.map((row) => (
                  <TableRow key={row.slNo} sx={{ "&:last-child td, &:last-child th": { border: 0 } }}>
                    <TableCell scope="row">{row.slNo}</TableCell>
                    <TableCell className="tablename" align="left">{row.tableName}</TableCell>
                    <TableCell align="left">{row.tableRun}</TableCell>
                    <TableCell align="left">{row.runDate}</TableCell>
                    <TableCell align="center">
                      <Box>
                        <Button variant="outlined" color="secondary" >
                          Sync
                        </Button>{" "}
                        &nbsp;&nbsp;{" "}
                        <Button variant="outlined" color="success" value={row.tableName}
                            onClick={redirectToRecommendation}
                        >
                          Edit
                        </Button>
                        {" "} &nbsp;&nbsp;{" "}
                         <Button variant="outlined" color="success" value={row.tableRun}
                         onClick={downloadReport}
                          >
                          Download
                          </Button>
                          {" "}
                              <input type="file" name="file" variant="outlined" color="success" onChange={handleChange}  />
                               &nbsp;&nbsp;{" "}
                                                         <Button variant="outlined" color="success" value={row.tableRun}
                                                          onClick={handleSubmit}
                                                           >
                                                           Upload
                                                           </Button>
                      </Box>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </div>
      )}
    </Box>
  );
}

export default Nestedselect;

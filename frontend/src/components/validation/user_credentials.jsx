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

import dummyInputData from "./dummy_data.json";
import { FormStatus } from "./static_data";

const initialValue = {
  hostname: "",
  port: "",
  dbname: "",
  usessl: false,
  username: "",
  password: "",
  schemaNames: [],
  tableNames: [],
  columnNames: [],
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
          [action.payload.key]: info.split(","),
        };
      } else {
        return {
          ...userCred,
          [action.payload.key]: action.payload.value,
        };
      }
    case "set_default":
      return action.payload;
    case "reset":
      return initialValue;
    default:
      throw new Error(`Unknown action type: ${action.type}`);
  }
};

export default function Validation() {
  const [userCred, dispatch] = useReducer(reducer, initialValue);

  const [loadDefaultData, setLoadDefaultData] = useState(false);
  const [ifFormTouched, setIfFormTouched] = useState(FormStatus.UNTOUCHED);
  const [isEntireFormValid, setIsEntireFormValid] = useState(false);

  const navigate = useNavigate();

  const handleInput = (event) => {
    dispatch({
      type: "update",
      payload: { key: event.target.name, value: event.target.name === "usessl" ? !userCred.usessl : event.target.value },
    });
  };

  useEffect(() => {
    const makeApiCall = async () => {
      const defaultData = initialValue;
      defaultData.hostname = dummyInputData.host_name;
      defaultData.port = dummyInputData.port;
      defaultData.dbname = dummyInputData.database_name;
      defaultData.usessl = dummyInputData.ssl_mode;
      defaultData.username = dummyInputData.username;
      defaultData.password = dummyInputData.password;
      dispatch({
        type: "set_default",
        payload: defaultData,
      });
    };
    if (!loadDefaultData) {
      setLoadDefaultData(true);
      //makeApiCall(); ///////////////////////////////////////// --->> Use this to load any default data
    }
  }, [loadDefaultData]);

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
        tableName: userCred.tableNames[0],
      };

      var fetchContent = {
        method: "POST",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
        body: JSON.stringify(params),
      };
      /*
      let url = "URL_TO_LOAD_DATA";
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
        });
        */
      navigate("/dvt/selection", userCred);
    } else {
      console.log("User details: Invalid" + JSON.stringify(userCred));
    }
  }, [isEntireFormValid]);

  function handleSubmit() {
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
      userCred.columnNames.length === 0
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

  return (
    <div>
      <Grid item xs={12}>
        <Typography variant="h5">Data Comparision Tool Connection Details</Typography>
      </Grid>
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
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              multiline
              maxRows={4}
              name="schemaNames"
              label="Schema Names"
              variant="outlined"
              value={userCred.schemaNames.join()}
              error={userCred.schemaNames.length === 0 && ifFormTouched === FormStatus.MODIFIED}
              onChange={handleInput}
            />
          </Grid>
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              multiline
              maxRows={4}
              name="tableNames"
              label="Table Names"
              variant="outlined"
              value={userCred.tableNames.join()}
              error={userCred.tableNames.length === 0 && ifFormTouched === FormStatus.MODIFIED}
              onChange={handleInput}
            />
          </Grid>
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              multiline
              maxRows={4}
              name="columnNames"
              label="Column Names"
              variant="outlined"
              value={userCred.columnNames.join()}
              error={userCred.columnNames.length === 0 && ifFormTouched === FormStatus.MODIFIED}
              onChange={handleInput}
            />
          </Grid>
          <Grid item md={3}></Grid>
          <Grid item md={6}>
            <Stack direction="row" spacing={2} style={{ justifyContent: "space-evenly" }}>
              <Button color="secondary" variant="contained" onClick={handleSubmit}>
                Submit
              </Button>
              <Button color="success" variant="contained" onClick={handleReset}>
                Reset
              </Button>
              <Button color="primary" variant="contained">
                Compare
              </Button>
            </Stack>
          </Grid>
          <Grid item md={3}></Grid>
        </Grid>
      </Box>
    </div>
  );
}

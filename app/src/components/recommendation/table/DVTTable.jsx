import * as React from "react";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Paper from "@mui/material/Paper";
import Typography from "@mui/material/Typography";

import Checkbox from "@mui/material/Checkbox";
import { useEffect, useState } from "react";


import DVTTableHeader from "./DVTTableHeader";
import DVTTableBody from "./DVTTableBody";
import DVTTableModal from "./DVTTableModal";
import Button from "@mui/material/Button";
import DVTTablePaginator from "./DVTTablePaginator";

import { useNavigate } from "react-router-dom";

import "../css/Recommendation.css";

function DVTTable(props) {
  //const [data, setData] = useState(dataFromFile1);
  const data = props.data;
  const [selectedRows, setSelected] = useState([]);
  const [modalOpenFlag, setModalOpenFlag] = useState(false);
  const [modalDetailsData, setmodalDetailsData] = useState([]);
  const [modalDetailsDataRowIndex, setModalDetailsDataRowIndex] = useState(-1);

  const navigate = useNavigate();

  console.log("Entered DVTTable ", selectedRows.length);
  console.log("Entered DVTTable Data", setmodalDetailsData);

  //alert("DVTTable");
  function redirectToValidation(event) {
    navigate('dvt');
              }
    function redirectToRecommendation(event) {
   navigate('/dvt/selection');
            }
  function isUnsavedDataExist() {
    return data["unsavedRows"] !== undefined && data["unsavedRows"].length > 0
      ? true
      : false;
  }

  const fetchDataHandler = (pageNumber) => {
    navigate("/dvt/recommend?table=" + data.table + "&page=" + pageNumber, {
      replace: true,
    });
    /*
      if (pageNumber === 1) {
        setData(dataFromFile1);
      } else if (pageNumber === 2) {
        setData(dataFromFile2);
      } else {
        setData(dataFromFile3);
      }
      */
  };

  const isSelected = (value) => {
    /*console.log(
      "Is selected ",
      value,
      selectedRows,
      selectedRows.indexOf(parseInt("" + value))
    );*/
    let isPresent = false;
    selectedRows.map((val) => {
      if (val === "" + value) {
        isPresent = true;
      }
    });
    //console.log("isPresent", isPresent);
    return isPresent;
  };

  const isSelectAllChecked = () => {
    if (selectedRows.length === data.rows.length) {
      return true;
    } else {
      return false;
    }
  };

  function clearSelectedRowsHandler() {
    setSelected([]);
  }

  const selectAllCheckboxHandler = (event, name) => {
    //console.log("handleSelectAllClick " + event.target.checked);
    if (event.target.checked) {
      const newSelected = data.rows.map((n, index) => "" + index);
      //console.log("newSelected", newSelected);
      setSelected(newSelected);
    } else {
      console.log("newSelected", []);
      setSelected([]);
    }
  };

  const rowCheckboxHandler = (event, name) => {
    //console.log("Target..", event.target, "Isclicked=", event.target.checked);
    var tmp = [];
    if (event.target.checked === true) {
      //console.log("true called", selectedRows.indexOf(event.target.value));

      if (selectedRows.indexOf(event.target.value) === -1) {
        if (selectedRows.length > 0) {
          selectedRows.map((value) => {
            tmp.push(value);
          });
        }
        tmp.push(event.target.value);
        setSelected(tmp);
      }
    } else {
      console.log("false called");
      tmp = selectedRows.filter((eachVal) => eachVal != event.target.value);
      setSelected(tmp);
    }
    //console.log("Selected Array .", tmp);
  };

  function openDetailsModalHandler(event) {
    //console.log("Modal clicked for ", event.target.value);
    let rowIndex = event.target.value;
    setModalOpenFlag(true);
    setmodalDetailsData(data.rows[rowIndex]);
    setModalDetailsDataRowIndex(rowIndex);
  }

  function closeDetailsModalHandler(rowIndex) {
    //console.log("Modal clicked for ", rowIndex);
    setModalOpenFlag(false);
  }

  function resetDetailsModalHandler() {
    //console.log("resetDetailsModalHandler ", modalDetailsDataRowIndex);
    setmodalDetailsData(data.rows[modalDetailsDataRowIndex]);
  }

  function getColumns() {
    let cols = [];
    if (data.rows !== null && data.rows.length > 0) {
      console.log("complete data",data);
      const row = data.rows[0];
      row.columns.map((eachColum) => {
        cols.push(eachColum.colName);
      });
    }
    //console.log("Columns", cols);
    return cols;
  }

  function applyDetailsModalHandler(modalData) {
    //console.log("modalData === ", modalData);

    var clonedDeta = JSON.parse(JSON.stringify(data));
    clonedDeta.rows[modalDetailsDataRowIndex] = modalData;
    if (clonedDeta["unsavedRows"] === undefined) {
      clonedDeta["unsavedRows"] = [modalDetailsDataRowIndex];
    } else {
      if (clonedDeta["unsavedRows"].indexOf(modalDetailsDataRowIndex) === -1) {
        clonedDeta["unsavedRows"].push(modalDetailsDataRowIndex);
      }
    }
    console.log(clonedDeta);
    props.setDataHandler(clonedDeta);
    setModalOpenFlag(false);
  }

  function saveHandler() {
    if (selectedRows.length === 0) {
      //alert("Please select rows to save");
      props.errorMessageHandler("Please select rows to save");
      return;
    }
    let result = [];
    data.rows.map((eachRow, index) => {
      if (selectedRows.indexOf("" + index) !== -1) {
        result.push(eachRow);
      }
    });
    //alert("Result Length =" + result.length);
    props.handleSummary(result);
  }

  let cols = getColumns();

  return (
    <div>

      <TableContainer component={Paper}>
        <Table
          sx={{ minWidth: 650 }}
          aria-label="simple table"
          className="dvttblh2"
          style={{ width: "95%", margin: 10 }}
          align="center"
        >
         <DVTTableHeader
            cols={cols}
            columnsDsiplayLimit={props.columnsDsiplayLimit}
            selectAllCheckboxHandler={selectAllCheckboxHandler}
            isSelectAllChecked={isSelectAllChecked}
          />
          <DVTTableBody
            rows={data.rows}
            cols={cols}
            columnsDsiplayLimit={props.columnsDsiplayLimit}
            rowCheckboxHandler={rowCheckboxHandler}
            isChecked={isSelected}
            openDetailsModalHandler={openDetailsModalHandler}
          ></DVTTableBody>
        </Table>
      </TableContainer>
      <div style={{ marginTop: 20, marginBottom: 10 }} align="center" valign="center">
        <Button variant="contained" align="center" valign="center" onClick={saveHandler}>
          Save
        </Button> &nbsp;&nbsp;{" "}
         <Button color="primary" align="center" valign="center" variant="contained" onClick={redirectToValidation}>
            Compare
          </Button> &nbsp;&nbsp;{" "}
         <Button color="secondary" align="center" valign="center"variant="contained" onClick={redirectToRecommendation}>
             Recommendation
           </Button> &nbsp;&nbsp;{" "}
      </div><div style={{ marginTop: 20, marginBottom: 10 }} align="center" valign="center">
      <DVTTablePaginator
        dataFetchHandler={fetchDataHandler}
        isUnsavedDataExist={isUnsavedDataExist}
        recordSize={data.totalRecords}
        displaySize={data.pageSize}
        currentPageNumber={props.currentPageNumber}
        clearSelectedRowsHandler={clearSelectedRowsHandler}
      ></DVTTablePaginator>
      {modalDetailsData.length === 0 ? null : (
        <DVTTableModal
          modalOpenFlag={modalOpenFlag}
          onClose={closeDetailsModalHandler}
          modalDetailsData={modalDetailsData}
          uniqueColumns={data.uniqueColumns}
          columnMetaData={data.columnMetaData}
          onApply={applyDetailsModalHandler}
          setData={setmodalDetailsData}
        />
      )}</div>
    </div>
  );
}

export default DVTTable;

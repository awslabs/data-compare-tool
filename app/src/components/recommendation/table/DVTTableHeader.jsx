import * as React from "react";

import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";

import Checkbox from "@mui/material/Checkbox";

function DVTTableHeader(props) {
  const columns = props.cols;
  let columnsDsiplayLimit = null;

  if (props.columnsDsiplayLimit !== undefined) {
    columnsDsiplayLimit =
      props.cols.length < props.columnsDsiplayLimit
        ? props.cols.length
        : props.columnsDsiplayLimit;
  } else {
    columnsDsiplayLimit = props.cols.length < 5 ? props.cols.length : 5;
  }

  function getColumnHeaderArray() {
    var columnArr = [];
    columnArr.push(
      <TableCell padding="checkbox" key="-1">
        <Checkbox
          color="primary"
          onClick={props.selectAllCheckboxHandler}
          checked={props.isSelectAllChecked()}
          inputProps={{
            "aria-label": "select all",
          }}
          style={{ height: 8 }}
        />
      </TableCell>
    );

    //Source
    columns.map((eachColumn, index) => {
      if (index < columnsDsiplayLimit) {
        columnArr.push(<TableCell key={index}>{eachColumn}</TableCell>);
      }
    });

    //Target
    columns.map((eachColumn, index) => {
      if (index < columnsDsiplayLimit) {
        columnArr.push(
          <TableCell key={columnsDsiplayLimit + index}>{eachColumn}</TableCell>
        );
      }
    });

    columnArr.push(
        <TableCell>MismatchType</TableCell>
    );
    if (columns.length >= columnsDsiplayLimit) {
      columnArr.push(
        <TableCell key={2 * columnsDsiplayLimit + 1} style={{ width: "1%" }}>
          Details
        </TableCell>
      );
    }

    return columnArr;
  }

  const test1 = (val) => {
    //alert("test1 val is" + val);
  };

  let columHeaders = getColumnHeaderArray();

  return (
    <TableHead>
      <TableRow>
        <TableCell align="center" colSpan={columnsDsiplayLimit + 1}>
          <label>Source</label>
        </TableCell>
        <TableCell align="center" colSpan={columnsDsiplayLimit}>
          <label>Target</label>
        </TableCell>
      </TableRow>
      <TableRow style={{ color: "white" }}>{columHeaders}</TableRow>
    </TableHead>
  );
}

export default DVTTableHeader;

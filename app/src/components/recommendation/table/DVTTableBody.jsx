import * as React from "react";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";

import TableRow from "@mui/material/TableRow";

import Checkbox from "@mui/material/Checkbox";
import moreimage from "../images/more.png";
import Button from "@mui/material/Button";

function DVTTableBody(props) {
  const rows = props.rows;
  let columnsDsiplayLimit = null;
console.log(" in tableBody",props);
  if (props.columnsDsiplayLimit !== undefined) {
    columnsDsiplayLimit =
      props.cols.length < props.columnsDsiplayLimit
        ? props.cols.length
        : props.columnsDsiplayLimit;
  } else {
    columnsDsiplayLimit = props.cols.length < 5 ? props.cols.length : 5;
  }

  const showMismatchType = (val) =>{
    console.log(val);
    switch(val){
      case 1:
        return "missing_trgt";
      case 2:
        return "mismatch";
      case 3:
        return "missing_src";
    }
  }

  function getRows() {
    let tableRows = [];
    rows.map((eachRow, rowIndex) => {
      eachRow.columns.map((eachColumn, colIndex) => {});
    });
  }
  return (
    <TableBody>
      {rows.map((eachRow, rowIndex) => (
        <TableRow key={rowIndex}>
          <TableCell padding="checkbox">
            <Checkbox
              value={rowIndex}
              onClick={props.rowCheckboxHandler}
              checked={props.isChecked(rowIndex)}
              style={{ height: 8 }}
            />
          </TableCell>

          {eachRow.columns.map((eachCol, columnIndex) =>
            columnIndex < columnsDsiplayLimit ? (
              <TableCell key={columnIndex}>{eachCol.srcValue}</TableCell>
            ) : null
          )}

          {eachRow.columns.map((eachCol, columnIndex) =>
            columnIndex < columnsDsiplayLimit ? (
              <TableCell key={columnIndex + columnsDsiplayLimit}>
                {eachCol.targetValue}
              </TableCell>
            ) : null
          )}
          <TableCell >{showMismatchType(eachRow.recommendationCode)}</TableCell>
          <TableCell style={{ width: "1%" }} key={2 * columnsDsiplayLimit + 1}>
            <Button
              value={rowIndex}
              onClick={(event) => props.openDetailsModalHandler(event)}
            >
              +
            </Button>
          </TableCell>
        </TableRow>
      ))}
    </TableBody>
  );
}

export default DVTTableBody;

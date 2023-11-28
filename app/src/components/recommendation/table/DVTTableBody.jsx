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
    console.log(" in tableBody", props);
    if (props.columnsDsiplayLimit !== undefined) {
        columnsDsiplayLimit =
            props.cols.length < props.columnsDsiplayLimit
                ? props.cols.length
                : props.columnsDsiplayLimit;
    } else {
        columnsDsiplayLimit = props.cols.length < 5 ? props.cols.length : 5;
    }

    const showMismatchType = (val) => {
        console.log(val);
        switch (val) {
            case 1:
                return "missing";
            case 2:
                return "mismatch_src";
            case 3:
                return "mismatch_tgt";
            case 4:
                return "Additional_tgt";
        }
    };

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
                            disabled={eachRow?.isRemediated ? true : false}
                        />
                    </TableCell>

                    {eachRow.columns.map((eachCol, columnIndex) =>
                        columnIndex < columnsDsiplayLimit ? (
                            <TableCell
                                key={columnIndex}
                                title={eachCol.srcValue}
                                sx={{
                                    overflow: "hidden",
                                    whiteSpace: "nowrap",
                                    textOverflow: "ellipsis",
                                    maxWidth: "150px",
                                }}
                            >
                                {eachCol.srcValue}
                            </TableCell>
                        ) : null
                    )}

                    {eachRow.columns.map((eachCol, columnIndex) =>
                        columnIndex < columnsDsiplayLimit ? (
                            <TableCell
                                key={columnIndex + columnsDsiplayLimit}
                                title={eachCol.targetValue}
                                sx={{
                                    color: "#FD6552 !important",
                                    fontStyle: "italic",
                                    overflow: "hidden",
                                    whiteSpace: "nowrap",
                                    textOverflow: "ellipsis",
                                    maxWidth: "150px",
                                }}
                            >
                                {eachCol.targetValue}
                            </TableCell>
                        ) : null
                    )}
                    <TableCell>
                        {showMismatchType(eachRow.recommendationCode)}
                    </TableCell>
                    <TableCell>
                        {eachRow?.isRemediated
                            ? "Remediated"
                            : "Not Remediated"}
                    </TableCell>
                    <TableCell
                        style={{ width: "1%" }}
                        key={20 * columnsDsiplayLimit + 1}
                    >
                        <Button
                            value={rowIndex}
                            onClick={(event) =>
                                eachRow?.isRemediated
                                    ? null
                                    : props.openDetailsModalHandler(event)
                            }
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

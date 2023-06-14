import React from "react";
import { useEffect, useState } from "react";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";

import Modal from "@mui/material/Modal";

import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Paper from "@mui/material/Paper";

function DVTTableModal(props) {
    console.log("DVTTableModal called", props.modalDetailsData);
    const modalOpenFlag = props.modalOpenFlag;

    const [modalDetailsData, setModalDetailsData] = useState(
        props.modalDetailsData
    );

    const [validationMessage, setValidationMessage] = useState("");

    useEffect(() => {
        console.log("Inside useEffect ... ", props.modalDetailsData);
        setModalDetailsData(props.modalDetailsData);
    }, [props.modalDetailsData]);

    //const handleOpen = () => setOpen(true);
    const handleClose = () => {
        props.onClose();
    };

    const style = {
        position: "absolute",
        top: "50%",
        left: "50%",
        transform: "translate(-50%, -50%)",
        width: 1200,
        bgcolor: "background.paper",
        border: "2px solid #000",
        boxshadow: 24,
        p: 4,
    };

    const handleChange = (event) => {
        console.log("pppp", event.target.name, event.target.value);
        console.log(props.modalDetailsData);
    var clonedmodalDetailsData = JSON.parse(JSON.stringify(modalDetailsData));
        clonedmodalDetailsData.columns.map((eachCol) => {
            if (eachCol.colName + "_target" === event.target.name) {
                console.log("Matched colName ", eachCol.colName);
                eachCol.targetValue = event.target.value;
            }
        });
        //props.setData(clonedmodalDetailsData);
        setModalDetailsData(clonedmodalDetailsData);

        //this.setState(event.target.value);
        //props.value.target = event.target.value;
        //props.onCloseHandler(200);
    };

    const handleApply = () => {
        let validationError = false;
        if (
            props.columnMetaData !== undefined &&
            props.columnMetaData !== null &&
            props.columnMetaData.length > 0
        ) {
            modalDetailsData.columns.map((eachCol) => {
                props.columnMetaData.map((eachColMetaData) => {
                    if (eachColMetaData.colName === eachCol.colName) {
                        if (eachColMetaData.type === "Integer") {
                            if (isNaN(eachCol.targetValue)) {
                                validationError = true;
                setValidationMessage(eachCol.colName + " should be Integer");
                            }
                            if (!validationError && eachColMetaData !== null) {
                if (eachCol.targetValue < eachColMetaData.minValue) {
                                    validationError = true;
                                    setValidationMessage(
                                        eachCol.colName +
                                            " should be atleast " +
                                            eachColMetaData.minValue
                                    );
                                }
                            }
                        }
                        if (eachColMetaData.type === "String") {
                            if (!validationError && eachColMetaData !== null) {
                if (eachCol.targetValue.length < eachColMetaData.minLength) {
                                    validationError = true;
                                    setValidationMessage(
                                        eachCol.colName +
                                            " length should be atleast " +
                                            eachColMetaData.minLength
                                    );
                                }
                if (eachCol.targetValue.length > eachColMetaData.maxLength) {
                                    validationError = true;
                                    setValidationMessage(
                                        eachCol.colName +
                                            " length should be maximum " +
                                            eachColMetaData.maxLength
                                    );
                                }
                            }
                        }
                    }
                });
            });
        }

        if (!validationError) {
            setValidationMessage("");
            props.onApply(modalDetailsData);
        }
    };

    const reset = () => {
        console.log("Reset1", modalDetailsData);
        //props.onReset();
        setModalDetailsData(props.modalDetailsData);
        console.log("Reset Done ");
    };

    const isNonEditableColumn = (columnName) => {
        /*if (props.modalDetailsData.recommendationCode === 1) {
      return false;
    }*/
        return props.uniqueColumns === null || props.uniqueColumns === undefined
            ? false
            : props.uniqueColumns.includes(columnName);
    };

    const copyAllFrmSrcToTarget = () => {
        var clonedmodalDetailsData = JSON.parse(
            JSON.stringify(props.modalDetailsData)
        );
        clonedmodalDetailsData.columns.map((eachCol) => {
            eachCol.targetValue = eachCol.srcValue;
        });
        setModalDetailsData(clonedmodalDetailsData);
    };

    //console.log("Data====", modalDetailsData);
    return (
        <div>
            <Modal
                open={modalOpenFlag}
                aria-labelledby="modal-modal-title"
                aria-describedby="modal-modal-description"
            >
                <Box sx={style}>
                    <span>
                        {
              <Button onClick={copyAllFrmSrcToTarget}>Src To Target</Button>
                        }
                    </span>

                    <div>
                        <span>{validationMessage}</span>
                        {modalDetailsData === [] ? null : (
                            <TableContainer component={Paper}>
                <Table aria-label="simple table" className="dvttbl">
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Column</TableCell>
                                            <TableCell>Source</TableCell>
                                            <TableCell>Target</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                    {modalDetailsData.columns.map((eachCol, index) => (
                                                <TableRow key={index}>
                        <TableCell ><div className="text-wrap">{eachCol.colName}</div></TableCell>
                        <TableCell ><div className="text-wrap">{eachCol.srcValue}</div></TableCell>
                        {isNonEditableColumn(eachCol.colName) ? (
                          <TableCell>{eachCol.targetValue}</TableCell>
                                                    ) : (
                                                        <TableCell>
                                                            <input
                                                                type="text"
                              name={eachCol.colName + "_target"}
                              value={eachCol.targetValue}
                              onChange={handleChange}
                                                            ></input>
                                                        </TableCell>
                                                    )}
                                                </TableRow>
                    ))}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        )}
                    </div>
          <div style={{ "padding-top": 8 }} align="center" valign="center">
                        <Button variant="outlined" onClick={handleClose}>
                            close
                        </Button>{" "}
                        &nbsp;
                        <Button variant="outlined" onClick={handleApply}>
                            Apply
                        </Button>{" "}
                        &nbsp;
                        <Button variant="outlined" onClick={reset}>
                            Reset
                        </Button>
                    </div>
                </Box>
            </Modal>
        </div>
    );
}

export default DVTTableModal;

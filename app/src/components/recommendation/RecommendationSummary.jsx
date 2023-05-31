import * as React from "react";
import { useState } from "react";
import Button from "@mui/material/Button";
import { Checkbox } from "@mui/material";
import "./css/Recommendation.css";
import Typography from "@mui/material/Typography";
// import logo from '../dart-logo.jpg'
import Header from "../Header.jsx";
import Grid from "@mui/material/Grid";
function RecommendationSummary(props) {
    console.log("Entered RecommendationSummary.jsx..");

    const [acknowledgeFlag, setAcknowledgeFlag] = useState(false);

    const summaryData = props.data;

    function acknowledgeHandler(event) {
        if (event.target.checked) {
            setAcknowledgeFlag(true);
        } else {
            setAcknowledgeFlag(false);
        }
    }

    function continueHandler() {
        if (!acknowledgeFlag) {
            //alert("Please chek the acknowledgement flag");
            props.errorMessageHandler("Please chek the acknowledgement flag");
        } else {
            props.onContinue();
        }
    }

    return (
        <div align="center" valign="center">
            <br />
            <Grid
                container
                mb={2}
                spacing={1}
                columnSpacing={{ xs: 2 }}
                justifyContent="center"
                alignItems="center"
            >
                <Grid item xs={12} sm={6} md={11}>
                    <Typography variant="h5" align="center" valign="bottom">
                        {" "}
                        Summary of changes{" "}
                    </Typography>
                    <div
                        style={{
                            marginTop: 10,
                            marginBottom: 10,
                            // width: "100%",
                            maxWidth: "1400px",
                            minWidth: "700px",
                            width: "auto",
                            overflow: "scroll",
                        }}
                    >
                        <table
                            border="1"
                            align="center"
                            id="customers"
                            className="dvttbl"
                            width="auto"
                        >
                            <thead>
                                <tr>
                                    {summaryData[0].columns.map(
                                        (eachColumn, eachColumnIndex) => {
                                            return (
                                                <th key={eachColumnIndex}>
                                                    {eachColumn.colName}
                                                </th>
                                            );
                                        }
                                    )}
                                </tr>
                            </thead>
                            <tbody>
                                {summaryData.map((eachRow, rowIndex) => {
                                    return (
                                        <tr key={rowIndex}>
                                            {eachRow.columns.map(
                                                (eachCol, colIndex) => {
                                                    return (
                                                        <td
                                                            key={colIndex}
                                                            style={{
                                                                textAlign:
                                                                    "left",
                                                            }}
                                                        >
                                                            {
                                                                eachCol.targetValue
                                                            }
                                                        </td>
                                                    );
                                                }
                                            )}
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    </div>
                    <div>
                        <Checkbox onClick={acknowledgeHandler}></Checkbox>
                        <span>
                            I acknowledge that these values will be saved in
                            DataBase
                        </span>
                    </div>
                    <div align="center" valign="center">
                        <Button variant="outlined" onClick={continueHandler}>
                            Continue
                        </Button>{" "}
                        &nbsp;
                        <Button variant="outlined" onClick={props.onCancel}>
                            Cancel
                        </Button>
                    </div>
                </Grid>
            </Grid>
        </div>
    );
}
export default RecommendationSummary;

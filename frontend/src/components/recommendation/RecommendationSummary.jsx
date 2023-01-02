import * as React from "react";
import { useState } from "react";
import Button from "@mui/material/Button";
import { Checkbox } from "@mui/material";
import "./css/Recommendation.css";

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
    <div>
      <br />
      <span>Summary of Changes For Table {props.tableName}</span>

      <div style={{ marginTop: 10, marginBottom: 10 }}>
        <table border="1" align="center" id="customers" className="dvttbl">
          <thead>
            <tr>
              {summaryData[0].columns.map((eachColumn, eachColumnIndex) => {
                return <th key={eachColumnIndex}>{eachColumn.colName}</th>;
              })}
            </tr>
          </thead>
          <tbody>
            {summaryData.map((eachRow, rowIndex) => {
              return (
                <tr key={rowIndex}>
                  {eachRow.columns.map((eachCol, colIndex) => {
                    return (
                      <td key={colIndex} style={{ textAlign: "left" }}>
                        {eachCol.targetValue}
                      </td>
                    );
                  })}
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
      <div>
        <Checkbox onClick={acknowledgeHandler}></Checkbox>
        <span>I acknowledge that these values will be saved in DataBase</span>
      </div>
      <div>
        <Button variant="outlined" onClick={continueHandler}>
          Continue
        </Button>{" "}
        &nbsp;
        <Button variant="outlined" onClick={props.onCancel}>
          Cancel
        </Button>
      </div>
    </div>
  );
}
export default RecommendationSummary;

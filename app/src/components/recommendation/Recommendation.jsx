import React, { useEffect, useState } from "react";
import DVTTable from "./table/DVTTable";
import { useSearchParams } from "react-router-dom";
import RecommendationSummary from "./RecommendationSummary";
import Typography from "@mui/material/Typography";
import LoadingSpinner from "../LoadingSpinner";
import "./css/Recommendation.css";
import Grid from "@mui/material/Grid";
function Recommendation() {
    console.log("Entered Recommendation.jsx..");
    const [searchParams, setSearchParams] = useSearchParams();
    const [data, setData] = useState(null);
    const [successMessage, setSuccessMessage] = useState(null);
    const [errorMessage, setErrorMessage] = useState(null);
    // const [showSummaryFlag, setShowSummaryFlag] = useState(false);
    const [summaryData, setSummaryData] = useState([]);

    //alert("Length =" + summaryData.length);

    const BACKEND_BASEURL_FETCH = "http://localhost:8080/dvt/recommend";
    //const BACKEND_BASEURL_FETCH = "http://localhost:8090/dvt/recommendation/recommendation-data/v1";
    const BACKEND_BASEURL_SUBMIT =
        "http://localhost:8090/dvt/remediation/remediate-data";

    useEffect(() => {
        console.log("Entered useEffect");
        let table = searchParams.get("tableName");
        let schemaName = searchParams.get("schemaName");
        let runId = searchParams.get("runId");
        let pageNumber = searchParams.get("page");
        //fetchData(table, pageNumber);
        fetchRecData(table, schemaName, runId, pageNumber);
    }, [searchParams]);

    function fetchRecData(tableName, schemaName, runId, pageNumber) {
        let requestParams = { method: "POST", headers: "", body: "" };
        requestParams.headers = { "Content-Type": "application/json" };
        requestParams.body = JSON.stringify({
            schemaName: schemaName,
            runId: runId,
            tableName: tableName,
        });
        console.log("Data To Submit == ", JSON.stringify(requestParams));
        fetch(
            "http://localhost:8090/dvt/recommendation/recommendation-data/v2",
            requestParams
        )
            .then((response) => {
                if (response.ok) {
                    return response.json();
                }
            })
            .then((data) => {
                console.log("Data is.......> ", data);
                data.runId = runId;
                setData(data);
                setSuccessMessage(null);
                setErrorMessage(null);
                setSummaryData([]);
            })
            .catch((error) => {
                setErrorMessage("An Unexpected Error occured..");
            });
    }

    function fetchData(tableName, pageNumber) {
        if (pageNumber === undefined || pageNumber === null) {
            pageNumber = 1;
        }
        let url =
            BACKEND_BASEURL_FETCH +
            "?table=" +
            tableName +
            "&page=" +
            pageNumber;
        //console.log("fetchData URL is ", url);
        fetch(url)
            .then((response) => {
                if (response.ok) {
                    return response.json();
                }
            })
            .then((data) => {
                //console.log("Data is ", data);
                setData(data);
                setSuccessMessage(null);
                setErrorMessage(null);
                setSummaryData([]);
            })
            .catch((error) => {
                setErrorMessage("An Unexpected Error occured..");
            });
    }

    function setDataHandler(newData) {
        console.log("setDataHandler", newData);
        setData(newData);
    }

    function handleSummary(dataToSave) {
        console.log("Data to save ", dataToSave);
        setErrorMessage(null);
        setSummaryData(dataToSave);
    }

    function summaryCancelHandler() {
        setErrorMessage(null);
        setSuccessMessage(null);
        setSummaryData([]);
    }

    const showMismatchType = (val) => {
        console.log(val);
        switch (val) {
            case 1:
                return "missing";
            case 2:
                return "mismatch_src";
            case 3:
                return "mismatch_trg";
            case 4:
                return "Additional_tgt";
        }
    };

    function summaryContinueHandler() {
        setSummaryData([]);

        let dataToSubmit = [];

        summaryData.map((eachRow, rowIndex) => {
            let eachRowToSubmit = [];
            eachRow.columns.map((eachCol, colIndex) => {
                let eachEntry = {
                    column: "",
                    value: "",
                    valId: "",
                    exceptionType: "",
                };
                eachEntry.column = eachCol.colName;
                eachEntry.value = eachCol.targetValue;
                eachEntry.valId = eachRow.valId;
                eachEntry.exceptionType = showMismatchType(
                    eachRow.recommendationCode
                );
                dataToSubmit.push(eachEntry);
            });
            console.log(" valid......in", eachRow);
            console.log(
                " valid......in",
                showMismatchType(eachRow.recommendationCode)
            );
            //dataToSubmit.push(eachRowToSubmit);
        });

        let requestParams = { method: "POST", headers: "", body: "" };
        requestParams.headers = { "Content-Type": "application/json" };
        //requestParams.body = JSON.stringify(dataToSubmit);
        let requestBody = {
            runId: "",
            tableName: "",
            schemaName: "",
            exceptionType: "",
            columnDetails: "",
        };

        requestBody.runId = data.runId;
        requestBody.tableName = data.table;
        requestBody.schemaName = searchParams.get("schemaName");
        requestBody.exceptionType = "";
        requestBody.columnDetails = dataToSubmit;
        requestParams.body = JSON.stringify(requestBody);
        console.log(" request ", requestParams.body);
        fetch(BACKEND_BASEURL_SUBMIT, requestParams)
            .then((response) => {
                if (response.ok) {
                    return response.text();
                }
            })
            .then((resultData) => {
                let msg =
                    resultData !== null ? resultData : "Saved Sucessfully";
                setSuccessMessage(msg);
                setErrorMessage(null);
            })
            .catch((error) => {
                console.log("Error ::", error);
                setErrorMessage("Error in saving request. Please try again..");
                setSuccessMessage(null);
            });
    }

    /* if (message !== null) {
    return <span>{message}</span>;
  }*/
    /*if (data === null) {
    return <span>Loading...</span>;
  }*/
    //console.log("Data======", data);

    /*  if (summaryData.length > 0) {
    return (
      <RecommendationSummary
        data={summaryData}
        onContinue={summaryContinueHandler}
        onCancel={summaryCancelHandler}
        tableName={searchParams.get("table")}
      ></RecommendationSummary>
    );
  }*/
    return (
        <div>
            <div>
                {errorMessage !== null ? (
                    <div className="error-msg">{errorMessage}</div>
                ) : null}
                {summaryData.length === 0 && (
                    <Typography variant="h5" className="heading">
                        Summary of changes
                    </Typography>
                )}
                {data === null ? <LoadingSpinner /> : null}
            </div>

            {summaryData.length > 0 ? (
                <RecommendationSummary
                    data={summaryData}
                    onContinue={summaryContinueHandler}
                    onCancel={summaryCancelHandler}
                    tableName={searchParams.get("table")}
                    errorMessageHandler={setErrorMessage}
                ></RecommendationSummary>
            ) : null}

            {data !== null && summaryData.length === 0 ? (
                data.rows.length === 0 ? (
                    <Typography variant="p" sx={{ my: 2, color: "green" }}>
                        No Mismatch Found!!!
                    </Typography>
                ) : (
                    <DVTTable
                        data={data}
                        setDataHandler={setDataHandler}
                        columnsDsiplayLimit={5}
                        currentPageNumber={searchParams.get("page")}
                        handleSummary={handleSummary}
                        errorMessageHandler={setErrorMessage}
                    ></DVTTable>
                )
            ) : null}
        </div>
    );
}
export default Recommendation;

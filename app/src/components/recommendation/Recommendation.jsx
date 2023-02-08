import * as React from "react";
import { useEffect, useState } from "react";
import DVTTable from "./table/DVTTable";
import { useSearchParams } from "react-router-dom";
import RecommendationSummary from "./RecommendationSummary";
import loadingimage from "./images/loadingimage.gif";

import "./css/Recommendation.css";

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
  const BACKEND_BASEURL_SUBMIT = "http://localhost:8080/dvt/submit";

  useEffect(() => {
    console.log("Entered useEffect");
    let table = searchParams.get("table");
    let pageNumber = searchParams.get("page");
    fetchData(table, pageNumber);
  }, [searchParams]);

  function fetchData(tableName, pageNumber) {
    if (pageNumber === undefined || pageNumber === null) {
      pageNumber = 1;
    }
    let url = BACKEND_BASEURL_FETCH + "?table=" + tableName + "&page=" + pageNumber;
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

  function summaryContinueHandler() {
    setSummaryData([]);

    let dataToSubmit = [];

    summaryData.map((eachRow, rowIndex) => {
      let eachRowToSubmit = [];
      eachRow.columns.map((eachCol, colIndex) => {
        let eachEntry = { column: "", value: "" };
        eachEntry.column = eachCol.colName;
        eachEntry.value = eachCol.targetValue;
        eachRowToSubmit.push(eachEntry);
      });
      dataToSubmit.push(eachRowToSubmit);
    });

    let requestParams = { method: "POST", headers: "", body: "" };
    requestParams.headers = { "Content-Type": "text/plain" };
    requestParams.body = JSON.stringify(dataToSubmit);

    console.log("Data To Submit == ", JSON.stringify(requestParams));

    fetch(BACKEND_BASEURL_SUBMIT, requestParams)
      .then((response) => {
        if (response.ok) {
          return response.text();
        }
      })
      .then((resultData) => {
        let msg = resultData !== null ? resultData : "Saved Sucessfully";
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
        {successMessage !== null ? <div className="success-msg">{successMessage}</div> : null}
        {errorMessage !== null ? <div className="error-msg">{errorMessage}</div> : null}

        {data === null ? <img style={{ height: 40, width: 40 }} src={loadingimage}></img> : null}
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
        <div>
          <header>Table : {searchParams.get("table")}</header>
          <DVTTable
            data={data}
            setDataHandler={setDataHandler}
            columnsDsiplayLimit={5}
            currentPageNumber={searchParams.get("page")}
            handleSummary={handleSummary}
            errorMessageHandler={setErrorMessage}
          ></DVTTable>
        </div>
      ) : null}
    </div>
  );
}
export default Recommendation;

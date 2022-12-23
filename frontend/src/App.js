import logo from "./logo.svg";
import React from "react";

import "./App.css";
import NestedSelect from "./components/Nestedselect"

import CascadingSelections from "./components/CascadingSelections";
import SchemaAndTableSelect from "./components/SchemaAndTableSelect";
import SchemaAndTableSelectCopy from "./components/SchemaAndRunDetailsCopy";
import RunDetailsTable from "./components/RunDetailsTable";

//import Table from "./components/RunDetailsTableFinal"
import NewSelect from "./components/Newselect"



function App() {
  return (
    <div className="App">
      {/*<CascadingSelections></CascadingSelections>*/}
{/*<NestedSelect></NestedSelect>*/}
        <SchemaAndTableSelectCopy></SchemaAndTableSelectCopy>
        <br/><br/>
        {/*<Table></Table>*/}
        {/*<NewSelect></NewSelect>*/}
{/*        <Example></Example>*/}
    </div>
  );
}

export default App;

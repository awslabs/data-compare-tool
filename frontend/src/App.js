import logo from "./logo.svg";
import React from "react";
import { Route, Routes } from "react-router-dom";

import "./App.css";

import SchemaAndTableSelectCopy from "./components/SchemaAndRunDetailsCopy";
import Recommendation from "./components/recommendation/Recommendation";

function App() {
  return (
    <div className="App">
     <Routes>
     <Route path="/dvt/selection" element={<SchemaAndTableSelectCopy />}></Route>
     <Route path="/dvt/recommend" element={<Recommendation />}></Route>
     </Routes>
       
    </div>
  );
}

export default App;

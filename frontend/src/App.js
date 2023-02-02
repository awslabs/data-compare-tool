import logo from "./logo.svg";
import React from "react";
import { Route, Routes } from "react-router-dom";

import "./App.css";

import Validation from "./components/validation/user_credentials";
import SchemaAndTableSelectCopy from "./components/SchemaAndRunDetailsCopy";
import Recommendation from "./components/recommendation/Recommendation";

function App() {
  return (
    <div className="App">
      <Routes>
        <Route path="/dvt/selection" element={<SchemaAndTableSelectCopy />}></Route>
        <Route path="/dvt/recommend" element={<Recommendation />}></Route>
        <Route path="/dvt/validation" element={<Validation />}></Route>

        <Route path="*" element={<Validation />}></Route>
      </Routes>
    </div>
  );
}

export default App;

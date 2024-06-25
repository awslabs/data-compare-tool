import React from "react";
import "./spinner.css";

export default function LoadingSpinner() {
  return (
    <>
      {/* <div className="spinner-container">
        <div className="loading-spinner"></div>
      </div> */}
      <div className="loading-container">
        <div className="loading">
          <div className="loading-text">
            <span className="loading-text-words">D</span>
            <span className="loading-text-words">V</span>
            <span className="loading-text-words">T</span>
            <div className="is-loading-text">is loading...</div>
          </div>
        </div>
      </div>
    </>
  );
}
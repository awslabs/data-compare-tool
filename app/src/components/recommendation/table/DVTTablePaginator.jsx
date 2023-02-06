import * as React from "react";
import { useEffect, useState } from "react";
import Button from "@mui/material/Button";
import DVTTablePaginatorConfirmModal from "./DVTTablePaginatorConfirmModal";

function DVTTablePaginator(props) {
  const [currentPage, setCurrentPage] = useState(
    parseInt(props.currentPageNumber, 10)
  );

  const [unsavedRowsModalOpenFlag, setUnsavedRowsModalOpenFlag] =
    useState(false);

  const [actionIdentifier, setActionIdentifier] = useState(null);

  //let buttonIdentifier = null;
  // alert("Paginator " + props.currentPageNumber);

  function next() {
    setActionIdentifier("next");
    let nextPageNumber = currentPage;
    if (props.isUnsavedDataExist()) {
      setUnsavedRowsModalOpenFlag(true);
    } else {
      fetchData(currentPage + 1);
    }
  }

  function previous() {
    setActionIdentifier("previous");
    if (props.isUnsavedDataExist()) {
      setUnsavedRowsModalOpenFlag(true);
    } else {
      fetchData(currentPage - 1);
    }
  }

  function fetchData(pageNumber) {
    setCurrentPage(pageNumber);
    props.clearSelectedRowsHandler();
    props.dataFetchHandler(pageNumber);
  }

  function isNextDisabled() {
    return currentPage >= props.recordSize / props.displaySize ? true : false;
  }
  function isPreviousDisabled() {
    return currentPage <= 1 ? true : false;
  }

  function unsavedRowsModalCancelHandler() {
    setUnsavedRowsModalOpenFlag(false);
  }

  function unsavedRowsModalContinueHandler() {
    setUnsavedRowsModalOpenFlag(false);
    if (actionIdentifier === "next") {
      fetchData(currentPage + 1);
    } else if (actionIdentifier === "previous") {
      fetchData(currentPage - 1);
    }
  }

  return (
    <div>
      <div>
        <Button disabled={isPreviousDisabled()} onClick={previous}>
          Previous
        </Button>
        <span>|&nbsp;</span>
        <span>Page Number {currentPage}</span>
        <span>|&nbsp;</span>
        <Button disabled={isNextDisabled()} onClick={next}>
          Next
        </Button>
      </div>
      <div>
        <DVTTablePaginatorConfirmModal
          modalOpenFlag={unsavedRowsModalOpenFlag}
          onCancel={unsavedRowsModalCancelHandler}
          onContinue={unsavedRowsModalContinueHandler}
        ></DVTTablePaginatorConfirmModal>
      </div>
    </div>
  );
}

export default DVTTablePaginator;

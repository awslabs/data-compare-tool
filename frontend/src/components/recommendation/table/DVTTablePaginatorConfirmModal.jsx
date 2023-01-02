import * as React from "react";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";

import Modal from "@mui/material/Modal";
import { useEffect, useState } from "react";

function DVTTablePaginatorConfirmModal(props) {
  const style = {
    position: "absolute",
    top: "50%",
    left: "50%",
    transform: "translate(-50%, -50%)",
    width: 500,
    bgcolor: "background.paper",
    border: "2px solid #000",
    boxshadow: 24,
    p: 4,
  };

  return (
    <Modal
      open={props.modalOpenFlag}
      aria-labelledby="modal-modal-title"
      aria-describedby="modal-modal-description"
    >
      <Box sx={style}>
        <span>
          There are unsaved changes. If you navigate to the next page before
          saving, the changes will be lost
        </span>
        <br />
        <Button onClick={props.onCancel}>Cancel</Button>
        <Button onClick={props.onContinue}>Continue</Button>
      </Box>
    </Modal>
  );
}

export default DVTTablePaginatorConfirmModal;

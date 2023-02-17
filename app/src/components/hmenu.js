
import React from 'react';
import Sidebar from './Sidebar';
import Box from "@mui/material/Box";
import Grid from "@mui/material/Grid";
import Typography from "@mui/material/Typography";
export default function Home() {
  return (
   <div className="App" id="outer-container">
         <Sidebar pageWrapId={'page-wrap'} outerContainerId={'outer-container'} />
         <div>
               <Grid item xs={6}>
                {" "} &nbsp;&nbsp;{" "}
                 {" "} &nbsp;&nbsp;{" "}
                  {" "} &nbsp;&nbsp;{" "}
                   {" "} &nbsp;&nbsp;{" "}
                    {" "} &nbsp;&nbsp;{" "}
                 <Typography variant="h4">Data Validation and Remediation Tool(DVART)</Typography>
                 <Typography variant="h5">Check out our new offerings in the sidebar !</Typography>
               </Grid>

         </div>
       </div>
     );
   }

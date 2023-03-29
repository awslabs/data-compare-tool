
import React from 'react';
import Sidebar from './Sidebar';
import Box from "@mui/material/Box";
import Grid from "@mui/material/Grid";
import Typography from "@mui/material/Typography";
import logo from './dart-logo.jpg';
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
                    <Grid container mb={2} spacing={1} columnSpacing={{ xs: 1 }} justifyContent="center" alignItems="center">
                                  <Grid item xs={12} sm={6} md={4}><img src={logo}  alt="Logo"  align="right" valign="bottom"/></Grid><Grid item xs={12} sm={6} md={8}>
                                    <Typography variant="h4" align="left" valign="bottom" >Data Validation and Remediation Tool(DVART) </Typography>
                                         <Typography variant="h5" align="left" valign="bottom" >Check out our new offerings in the sidebar !</Typography>
                                  </Grid></Grid>

               </Grid>

         </div>
       </div>
     );
   }

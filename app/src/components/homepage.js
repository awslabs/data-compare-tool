
import React from 'react';
import Box from "@mui/material/Box";
import Grid from "@mui/material/Grid";
import Typography from "@mui/material/Typography";
import dvtFeatures from './dvt-features.png';
export default function Home() {
  return (
    <Box>
      <Grid container mb={2} spacing={1} columnSpacing={{ xs: 2 }} justifyContent="center" alignItems="center">
        <Grid item xs={12} sm={6} md={11}>
          <Typography variant="h5" align="center" valign="bottom" className='heading' >Home Page</Typography>
          <div>
            <Typography variant="p" align="left">
              DVT (Data Validation Tool) can be used to compare and remediate migrated data from the Oracle database (source) to  PostgreSQL database (target). This tool helps the users to validate the data after data migration, generate the data mismatch details and allows to remediate the mismatched data.
            </Typography>
            <Typography variant="h6" align="left" mt={1} className='sub-heading'>
              Features of DVT
            </Typography>

            <Grid container mt={1} spacing={1} columnSpacing={{ xs: 10 }} >

              <Grid item xs={12} sm={12} md={6} style={{ "float": "right" }}>
                <ul style={{ "padding-left": "20px" }}>
                  <li style={{ "padding-bottom": "10px" }}>
                    <span style={{ "font-weight": "bold" }}>Sign-Up/Sign-In:</span> First-time users have to sign up to access the tool. After signup, access the tool using the generated Username and Password.
                  </li>
                  <li style={{ "padding-bottom": "10px" }}>
                    <span style={{ "font-weight": "bold" }}>Validation:</span> Performs data comparison between the source and the target database. The user has to provide the database details like Host, Schema, Table, Chunk and other details. Complete list of validation criteria is available on 'Validation' screen. Users can also view the last compared data .
                  </li>
                  <li style={{ "padding-bottom": "10px" }}>
                    <span style={{ "font-weight": "bold" }}>Recommendation:</span> This functionality allows users to view validation/comparison results. It provides recommendations to fix the deviations of the source and target database. Users have an option to download the mismatch/recommendation report in an Excel file.
                  </li>
                  <li style={{ "padding-bottom": "10px" }}>
                    <span style={{ "font-weight": "bold" }}>Remediation:</span> This functionality allows users to update the target data to fix the differences. User will have source/target data values to take the appropriate decision and save the changes to database.
                  </li>
                  <li style={{ "padding-bottom": "10px" }}>
                    <span style={{ "font-weight": "bold" }}>Validation Schedules:</span> This functionality helps users to setup auto runs. Auto runs automatically trigger run at the selected schedules. User need to provide database and table details and set the schedule.
                  </li>
                </ul>
              </Grid>
              <Grid item xs={12} sm={12} md={6} style={{ "float": "right" }} >
                <Box textAlign="center" width="100%">
                  <img
                    src={dvtFeatures}
                    width="100%"
                    alt="Features of DVT"
                    loading="lazy"
                  />
                </Box>
              </Grid>
            </Grid>
          </div>
        </Grid>
      </Grid>
    </Box >
  );
}

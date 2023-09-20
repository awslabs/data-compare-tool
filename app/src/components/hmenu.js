
import React from 'react';
import Sidebar from './Sidebar';
import Box from "@mui/material/Box";
import Grid from "@mui/material/Grid";
import Typography from "@mui/material/Typography";
// import logo from './dart-logo.jpg';
export default function Home() {
  return (
    <Box>
      <Grid container mb={2} spacing={1} columnSpacing={{ xs: 2 }} justifyContent="center" alignItems="center">
        <Grid item xs={12} sm={6} md={11}>
          <Typography variant="h5" align="center" valign="bottom" className='heading' >Home Page</Typography>
        </Grid>
      </Grid>
    </Box>
  );
}

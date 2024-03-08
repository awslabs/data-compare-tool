import React, { useContext } from 'react'

import { useNavigate } from 'react-router-dom'

import Typography from '@mui/material/Typography'
import Grid from '@mui/material/Grid'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import GitHubIcon from '@mui/icons-material/GitHub'
import Link from '@mui/material/Link'

import logoImage from './logo.png'

import { AuthContext } from '../contexts/authContext'


export default function Home() {
  const history = useNavigate()

  const auth = useContext(AuthContext)

  function signOutClicked() {
    auth.signOut()
    history('/')
  }

  function changePasswordClicked() {
    history('changepassword')
  }

  return (
    <Grid container>
      <Grid container direction="column" alignItems="center" justifyContent="center">
        <Box width="100%" style={{ 'background': 'rgb(220,220,220)' }} p={4}>
          <Grid container direction="column" alignItems="center" justifyContent="center">
            <Box m={2}>
              <img src={logoImage} width={224} height={224} alt="logo" />
            </Box>
            <Box m={2}>
              <Link underline="none" color="inherit" href="https://github.com/dbroadhurst/aws-cognito-react">
                <Grid container direction="row" alignItems="center" justifyContent="center">
                  <Box mr={3}>
                    <GitHubIcon fontSize="large" />
                  </Box>
                  <Typography variant="h4" textAlign="center">
                    AWS Cognito Starter Home
                  </Typography>
                </Grid>
              </Link>
            </Box>
            <Box m={2}>
              <Button onClick={signOutClicked} variant="contained" color="primary">
                Sign Out
              </Button>
            </Box>
            <Box m={2}>
              <Button onClick={changePasswordClicked} variant="contained" color="primary">
                Change Password
              </Button>
            </Box>
          </Grid>
        </Box>
        <Box m={2}>
          <Typography variant="h5">Session Info</Typography>
          <pre style={{ 'width': '80vw', 'overflow': 'auto', 'overflowWrap': 'break-word', 'fontSize': '16px' }} >{JSON.stringify(auth.sessionInfo, null, 2)}</pre>
        </Box>
        <Box m={2}>
          <Typography variant="h5">User Attributes</Typography>
          <pre style={{ 'width': '80vw', 'overflow': 'auto', 'overflowWrap': 'break-word', 'fontSize': '16px' }} >{JSON.stringify(auth.attrInfo, null, 2)}</pre>
        </Box>
      </Grid>
    </Grid>
  )
}

import React from 'react'

import { useNavigate } from 'react-router-dom'

import Typography from '@mui/material/Typography'
import Grid from '@mui/material/Grid'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Link from '@mui/material/Link'
import GitHubIcon from '@mui/icons-material/GitHub'

import logoImage from './dart-logo.jpg'

const Landing: React.FunctionComponent = () => {
  const navigate = useNavigate()

  const signIn = () => {
    navigate('/signin')
  }

  return (
    <Grid container>
      <Grid height="100vh" container direction="column" alignItems="center" justifyContent="center">
        <Box m={2}>
          <img src={logoImage} width={224} height={224} alt="logo" />
        </Box>
        <Box m={2}>
          <Link underline="none" color="inherit" href="https://github.com/dbroadhurst/aws-cognito-react">
            <Grid container direction="row" alignItems="center" justifyContent="center">
              <Box mr={3}>
                <GitHubIcon fontSize="large" />
              </Box>
              <Typography variant="h3" textAlign="center">
                AWS Cognito Starter
              </Typography>
            </Grid>
          </Link>
        </Box>
        <Box m={2}>
          <Button onClick={signIn} variant="contained" color="primary">
            SIGN IN
          </Button>
        </Box>
      </Grid>
    </Grid>
  )
}

export default Landing

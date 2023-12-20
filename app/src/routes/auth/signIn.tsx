import React, { useState, useContext } from 'react'

import { useNavigate } from 'react-router-dom'

import Box from '@mui/material/Box'
import Grid from '@mui/material/Grid'
import Button from '@mui/material/Button'
import Typography from '@mui/material/Typography'
import Paper from '@mui/material/Paper'

import { useValidPassword, useValidUsername } from '../../hooks/useAuthHooks'
import { Password, Username } from '../../components/authComponents'
import * as cognito from '../../libs/cognito'
import { AuthContext } from '../../contexts/authContext'
import logo from '../../components/dart-logo.jpg'
import "../../components/styles.css";

export enum AuthStatus {
  Loading,
  SignedIn,
  SignedOut,
}
const SignIn: React.FunctionComponent<{}> = () => {
  const [authStatus, setAuthStatus] = useState(AuthStatus.Loading)
  const { username, setUsername, usernameIsValid } = useValidUsername('')
  const { password, setPassword, passwordIsValid } = useValidPassword('')
  const [error, setError] = useState('')

  const isValid = !usernameIsValid || username.length === 0 || !passwordIsValid || password.length === 0
  const history = useNavigate()
  let authContext = React.useContext(AuthContext)
  const signInClicked = async () => {
    try {

      console.log("authContext 2", authContext)

      await signInWithEmail(username, password)

    } catch (err: any) {
      if (err.code === 'UserNotConfirmedException') {
        history('/dvt/verify')
      } else {
        setError(err.message)
      }
    }
  }
  async function signInWithEmail(username: string, password: string) {
    try {
      const response = await cognito.signInWithEmail(username, password)
      console.log("response", response)
      if (response != null) {
        setAuthStatus(AuthStatus.SignedIn)
        history('/dvt/compare')
        window.location.reload()
      }
    } catch (err) {
      setAuthStatus(AuthStatus.SignedOut)
      throw err
    }
  }
  const passwordResetClicked = async () => {
    history('/dvt/requestcode')
  }

  return (
    <Grid container direction="row" alignItems="center" justifyContent="center">
      <Grid xs={11} sm={6} lg={4} container direction="row" alignItems="center" item justifyContent="center">
        <Paper style={{ width: '100%', padding: 32 }}>
          <Grid container direction="column" alignItems="center" justifyContent="center">
            {/* Title */}
            <Box m={2}>
              <Typography variant="h5" className='heading'>Sign in</Typography>
            </Box>

            {/* Sign In Form */}
            <Box width="80%" m={1}>
              {/* <Email emailIsValid={emailIsValid} setEmail={setEmail} /> */}
              <Username usernameIsValid={usernameIsValid} setUsername={setUsername} />{' '}
            </Box>
            <Box width="80%" m={1}>
              <Password label="Password" passwordIsValid={passwordIsValid} setPassword={setPassword} />
              <Grid container direction="row" alignItems="center" justifyContent="flex-start">
                <Box onClick={passwordResetClicked} mt={2}>
                  <Typography sx={{ ':hover': { cursor: 'pointer' } }} variant="body2">
                    Forgot Password?
                  </Typography>
                </Box>
              </Grid>
            </Box>

            {/* Error */}
            <Box mt={2}>
              <Typography color="error" variant="body2">
                {error}
              </Typography>
            </Box>

            {/* Buttons */}
            <Box mt={2}>
              <Grid container direction="row" justifyContent="center">
                <Box m={1}>
                  <Button color="secondary" variant="contained" onClick={() => history(-1)}>
                    Cancel
                  </Button>
                </Box>
                <Box m={1}>
                  <Button disabled={isValid} color="primary" variant="contained" onClick={signInClicked}>
                    Sign In
                  </Button>
                </Box>
              </Grid>
            </Box>
            <Box mt={2}>
              <Box onClick={() => history('/dvt/signup')}>
                <Typography sx={{ ':hover': { cursor: 'pointer' } }} variant="body1">
                  Register a new account
                </Typography>
              </Box>
            </Box>
          </Grid>
        </Paper>
      </Grid>
    </Grid>
  )
}

export default SignIn

import React, { useState, useContext } from 'react'

import { useNavigate } from 'react-router-dom'

import { makeStyles } from '@material-ui/core/styles'
import Box from '@material-ui/core/Box'
import Grid from '@material-ui/core/Grid'
import Button from '@material-ui/core/Button'
import Typography from '@material-ui/core/Typography'
import Paper from '@material-ui/core/Paper'

import { useValidPassword, useValidUsername } from '../../hooks/useAuthHooks'
import { Password, Username } from '../../components/authComponents'
import * as cognito from '../../libs/cognito'
import { AuthContext } from '../../contexts/authContext'
const useStyles = makeStyles({
  root: {
    height: '100vh',
  },
  hover: {
    '&:hover': { cursor: 'pointer' },
  },
})
export enum AuthStatus {
  Loading,
  SignedIn,
  SignedOut,
}
const SignIn: React.FunctionComponent<{}> = () => {
  const classes = useStyles()
 const [authStatus, setAuthStatus] = useState(AuthStatus.Loading)
  const { username, setUsername, usernameIsValid } = useValidUsername('')
  const { password, setPassword, passwordIsValid } = useValidPassword('')
  const [error, setError] = useState('')

  const isValid = !usernameIsValid || username.length === 0 || !passwordIsValid || password.length === 0

  const history = useNavigate()
 console.log("authContext 0",AuthContext)
  const authContext1 = React.useContext(AuthContext)
 console.log("authContext 1",authContext1)
 let authContext = React.useContext(AuthContext)
 console.log("authContext 2",authContext1)
  const signInClicked = async () => {
    try {

  console.log("authContext 2",authContext)
      await signInWithEmail(username, password)
      history('compare')
    } catch (err: any) {
      if (err.code === 'UserNotConfirmedException') {
        history('verify')
      } else {
        setError(err.message)
      }
    }
  }
 async function signInWithEmail(username: string, password: string) {
    try {
      await cognito.signInWithEmail(username, password)
      setAuthStatus(AuthStatus.SignedIn)
    } catch (err) {
      setAuthStatus(AuthStatus.SignedOut)
      throw err
    }
  }
  const passwordResetClicked = async () => {
    history('/dvt/requestcode')
  }

  return (
    <Grid className={classes.root} container direction="row" justify="center" alignItems="center">
      <Grid xs={11} sm={6} lg={4} container direction="row" justify="center" alignItems="center" item>
        <Paper style={{ width: '100%', padding: 32 }}>
          <Grid container direction="column" justify="center" alignItems="center">
            {/* Title */}
            <Box m={2}>
              <Typography variant="h3">Sign in</Typography>
            </Box>

            {/* Sign In Form */}
            <Box width="80%" m={1}>
              {/* <Email emailIsValid={emailIsValid} setEmail={setEmail} /> */}
              <Username usernameIsValid={usernameIsValid} setUsername={setUsername} />{' '}
            </Box>
            <Box width="80%" m={1}>
              <Password label="Password" passwordIsValid={passwordIsValid} setPassword={setPassword} />
              <Grid container direction="row" justify="flex-start" alignItems="center">
                <Box onClick={passwordResetClicked} mt={2}>
                  <Typography className={classes.hover} variant="body2">
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
              <Grid container direction="row" justify="center">
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
                <Typography className={classes.hover} variant="body1">
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

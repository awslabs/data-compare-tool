import React, { useState, useContext } from 'react'

import { useNavigate } from 'react-router-dom'
import * as cognito from '../../libs/cognito'
import Box from '@mui/material/Box'
import Grid from '@mui/material/Grid'
import Button from '@mui/material/Button'
import Typography from '@mui/material/Typography'
import Paper from '@mui/material/Paper'

import { useValidEmail, useValidPassword, useValidUsername } from '../../hooks/useAuthHooks'
import { Email, Password, Username } from '../../components/authComponents'

import { AuthContext } from '../../contexts/authContext'
import "../../components/styles.css";

const SignUp: React.FunctionComponent<{}> = () => {
  const { email, setEmail, emailIsValid } = useValidEmail('')
  const { password, setPassword, passwordIsValid } = useValidPassword('')
  const { username, setUsername, usernameIsValid } = useValidUsername('')
  const [error, setError] = useState('')
  const [created, setCreated] = useState(false)

  const {
    password: passwordConfirm,
    setPassword: setPasswordConfirm,
    passwordIsValid: passwordConfirmIsValid,
  } = useValidPassword('')

  const isValid =
    !emailIsValid ||
    email.length === 0 ||
    !usernameIsValid ||
    username.length === 0 ||
    !passwordIsValid ||
    password.length === 0 ||
    !passwordConfirmIsValid ||
    passwordConfirm.length === 0

  const history = useNavigate()

  const authContext = useContext(AuthContext)

  const signInClicked = async () => {
    try {
      await signUpWithEmail(username, email, password)
      setCreated(true)
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message)
      }
    }
  }
  async function signUpWithEmail(username: string, email: string, password: string) {
    try {
      await cognito.signUpUserWithEmail(username, email, password)
    } catch (err) {
      throw err
    }
  }
  const signUp = (
    <>
      <Box width="80%" m={1}>
        <Email emailIsValid={emailIsValid} setEmail={setEmail} />
      </Box>
      <Box width="80%" m={1}>
        <Username usernameIsValid={usernameIsValid} setUsername={setUsername} />
      </Box>
      <Box width="80%" m={1}>
        <Password label="Password" passwordIsValid={passwordIsValid} setPassword={setPassword} />
      </Box>
      <Box width="80%" m={1}>
        <Password label="Confirm Password" passwordIsValid={passwordConfirmIsValid} setPassword={setPasswordConfirm} />
      </Box>
      <Box mt={2}>
        <Typography color="error" variant="body2">
          {error}
        </Typography>
      </Box>

      {/* Buttons */}
      <Box mt={2}>
        <Grid container direction="row" justifyContent="center">
          <Box m={1}>
            <Button onClick={() => history(-1)} color="secondary" variant="contained">
              Cancel
            </Button>
          </Box>
          <Box m={1}>
            <Button disabled={isValid} color="primary" variant="contained" onClick={signInClicked}>
              Sign Up
            </Button>
          </Box>
        </Grid>
      </Box>
    </>
  )

  const accountCreated = (
    <>
      <Typography variant="h5">{`Created ${username} account`}</Typography>
      <Typography variant="h6">{`Verfiy Code sent to ${email}`}</Typography>

      <Box m={4}>
        <Button onClick={() => history('/dvt/verify')} color="primary" variant="contained">
          Send Code
        </Button>
      </Box>
    </>
  )

  return (
    <Grid container direction="row" alignItems="center" justifyContent="center">
      <Grid xs={11} sm={6} lg={4} container direction="row" alignItems="center" item justifyContent="center">
        <Paper style={{ width: '100%', padding: 16 }}>
          <Grid container direction="column" alignItems="center" justifyContent="center">
            {/* Title */}
            <Box m={3}>
              <Grid container direction="row" alignItems="flex-end" justifyContent="flex-end">
                <Grid xs={12} sm={12} md={12} container direction="column" alignItems="flex-end" item justifyContent="center">
                  <Typography variant="h5" className='heading'>Sign Up</Typography>
                </Grid>
              </Grid>
            </Box>
            {!created ? signUp : accountCreated}
          </Grid>
        </Paper>
      </Grid>
    </Grid>
  )
}

export default SignUp

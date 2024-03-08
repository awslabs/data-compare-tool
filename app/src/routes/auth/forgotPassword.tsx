import React, { useState, useContext } from 'react'

import { useNavigate } from 'react-router-dom'

import Box from '@mui/material/Box'
import Grid from '@mui/material/Grid'
import Button from '@mui/material/Button'
import Typography from '@mui/material/Typography'
import Paper from '@mui/material/Paper'

import { useValidCode, useValidPassword, useValidUsername } from '../../hooks/useAuthHooks'
import { Code, Password, Username } from '../../components/authComponents'

import { AuthContext } from '../../contexts/authContext'

export default function ForgotPassword() {
  const { code, setCode, codeIsValid } = useValidCode('')
  const { password, setPassword, passwordIsValid } = useValidPassword('')
  const { username, setUsername, usernameIsValid } = useValidUsername('')
  const [error, setError] = useState('')
  const [reset, setReset] = useState(false)

  const {
    password: passwordConfirm,
    setPassword: setPasswordConfirm,
    passwordIsValid: passwordConfirmIsValid,
  } = useValidPassword('')

  const isValid =
    !codeIsValid ||
    code.length === 0 ||
    !usernameIsValid ||
    username.length === 0 ||
    !passwordIsValid ||
    password.length === 0 ||
    !passwordConfirmIsValid ||
    passwordConfirm.length === 0

  const history = useNavigate()

  const authContext = useContext(AuthContext)

  const resetPassword = async () => {
    try {
      await authContext.forgotPassword(username, code, password)
      setReset(true)
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message)
      }
    }
  }

  const updatePassword = (
    <>
      <Box width="80%" m={1}>
        <Code codeIsValid={codeIsValid} setCode={setCode} />
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
            <Button onClick={() => history(-1)} color="secondary" variant="contained">
              Cancel
            </Button>
          </Box>
          <Box m={1}>
            <Button disabled={isValid} color="primary" variant="contained" onClick={resetPassword}>
              Change Password
            </Button>
          </Box>
        </Grid>
      </Box>
    </>
  )

  const passwordReset = (
    <>
      <Typography variant="h5">{`Password Reset`}</Typography>

      <Box m={4}>
        <Button onClick={() => history('/dvt/signin')} color="primary" variant="contained">
          Sign In
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
              <Grid container direction="row" alignItems="center" justifyContent="center">
                <Typography variant="h5" className='heading'>Forgot Password</Typography>
              </Grid>
            </Box>

            {!reset ? updatePassword : passwordReset}
          </Grid>
        </Paper>
      </Grid>
    </Grid>
  )
}

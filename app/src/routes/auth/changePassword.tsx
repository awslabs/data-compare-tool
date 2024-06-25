import React, { useState, useContext } from 'react'

import { useNavigate } from 'react-router-dom'

import Box from '@mui/material/Box'
import Grid from '@mui/material/Grid'
import Button from '@mui/material/Button'
import Typography from '@mui/material/Typography'
import Paper from '@mui/material/Paper'

import { useValidPassword } from '../../hooks/useAuthHooks'
import { Password } from '../../components/authComponents'

import { AuthContext } from '../../contexts/authContext'

export default function ChangePassword() {
  const [error, setError] = useState('')
  const [reset, setReset] = useState(false)

  const {
    password: oldPassword,
    setPassword: setOldPassword,
    passwordIsValid: oldPasswordIsValid,
  } = useValidPassword('')

  const {
    password: newPassword,
    setPassword: setNewPassword,
    passwordIsValid: newPasswordIsValid,
  } = useValidPassword('')

  const isValid = !oldPasswordIsValid || oldPassword.length === 0 || !newPasswordIsValid || newPassword.length === 0

  const history = useNavigate()

  const authContext = useContext(AuthContext)

  const changePassword = async () => {
    try {
      await authContext.changePassword(oldPassword, newPassword)
      setReset(true)
    } catch (err: any) {
      setError(err.message)
    }
  }

  const signOut = async () => {
    try {
      await authContext.signOut()
      history('/')
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message)
      }
    }
  }

  const updatePassword = (
    <>
      <Box width="80%" m={1}>
        <Password label="Old Password" passwordIsValid={oldPasswordIsValid} setPassword={setOldPassword} />
      </Box>
      <Box width="80%" m={1}>
        <Password label="Password" passwordIsValid={newPasswordIsValid} setPassword={setNewPassword} />
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
            <Button disabled={isValid} color="primary" variant="contained" onClick={changePassword}>
              Change Password
            </Button>
          </Box>
        </Grid>
      </Box>
    </>
  )

  const passwordReset = (
    <>
      <Typography variant="h5">{`Password Changed`}</Typography>

      <Box m={4}>
        <Button onClick={signOut} color="primary" variant="contained">
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
                <Typography variant="h5" className='heading'>Change Password</Typography>
              </Grid>
            </Box>
            {!reset ? updatePassword : passwordReset}
          </Grid>
        </Paper>
      </Grid>
    </Grid>
  )
}

import React, { useState, useContext } from 'react'

import { useNavigate } from 'react-router-dom'

import Box from '@mui/material/Box'
import Grid from '@mui/material/Grid'
import Button from '@mui/material/Button'
import Typography from '@mui/material/Typography'
import Paper from '@mui/material/Paper'

import { useValidCode, useValidUsername } from '../../hooks/useAuthHooks'
import { Code, Username } from '../../components/authComponents'

import { AuthContext } from '../../contexts/authContext'

const VerifyCode: React.FunctionComponent<{}> = () => {

  const { username, setUsername, usernameIsValid } = useValidUsername('')
  const { code, setCode, codeIsValid } = useValidCode('')
  const [error, setError] = useState('')

  const isValid = !usernameIsValid || username.length === 0 || !codeIsValid || code.length === 0

  const history = useNavigate()

  const authContext = useContext(AuthContext)

  const sendClicked = async () => {
    try {
      await authContext.verifyCode(username, code)
      history('/dvt/signin')
    } catch (err) {
      setError('Invalid Code')
    }
  }

  const passwordResetClicked = async () => {
    history('/dvt/resetpassword')
  }

  return (
    <Grid container direction="row" alignItems="center" justifyContent="center">
      <Grid xs={11} sm={6} lg={4} container direction="row" alignItems="center" item justifyContent="center">
        <Paper style={{ width: '100%', padding: 32 }}>
          <Grid container direction="column" alignItems="center" justifyContent="center">
            {/* Title */}
            <Box m={2}>
              <Typography variant="h5" className='heading'>Send Code</Typography>
            </Box>

            {/* Sign In Form */}
            <Box width="80%" m={1}>
              {/* <Email emailIsValid={emailIsValid} setEmail={setEmail} /> */}
              <Username usernameIsValid={usernameIsValid} setUsername={setUsername} />{' '}
            </Box>
            <Box width="80%" m={1}>
              <Code codeIsValid={codeIsValid} setCode={setCode} />
              <Grid container direction="row" alignItems="center" justifyContent="center">
                <Box onClick={passwordResetClicked} mt={2}>
                  <Typography sx={{ ':hover': { cursor: 'pointer' } }} variant="body2">
                    Resend Code
                  </Typography>
                  <Box mt={2}>
                    <Typography color="error" variant="body2">
                      {error}
                    </Typography>
                  </Box>
                </Box>
              </Grid>
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
                  <Button disabled={isValid} color="primary" variant="contained" onClick={sendClicked}>
                    Send
                  </Button>
                </Box>
              </Grid>
            </Box>
          </Grid>
        </Paper>
      </Grid>
    </Grid>
  )
}

export default VerifyCode

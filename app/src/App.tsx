import React from 'react'
import './App.css'

import { HashRouter as Router, Route, Routes } from 'react-router-dom'

import { createMuiTheme, ThemeProvider, responsiveFontSizes } from '@material-ui/core/styles'
import CssBaseline from '@material-ui/core/CssBaseline'
import AuthProvider, { AuthIsSignedIn, AuthIsNotSignedIn } from './contexts/authContext'
import SignIn from './routes/auth/signIn'
import SignUp from './routes/auth/signUp'
import VerifyCode from './routes/auth/verify'
import RequestCode from './routes/auth/requestCode'
import ForgotPassword from './routes/auth/forgotPassword'
import ChangePassword from './routes/auth/changePassword'
import Landing from './routes/landing'
import Home from './routes/home'
import logo from "./logo.svg";
import Validation from "./components/validation/user_credentials";
import SchemaAndTableSelectCopy from "./components/SchemaAndRunDetailsCopy";
import Recommendation from "./components/recommendation/Recommendation";
import Menu from "./components/hmenu";


let lightTheme = createMuiTheme({
  palette: {
    type: 'light',
  },
})
lightTheme = responsiveFontSizes(lightTheme)


// let darkTheme = createMuiTheme({
//   palette: {
//     type: 'dark',
//   },
// })
// darkTheme = responsiveFontSizes(darkTheme)

const SignInRoute: React.FunctionComponent = () => (

    <Routes>
     <Route path="/dvt/signin" element={<SignIn />}></Route>
                   <Route path="/dvt/signup" element={<SignUp/>}></Route>
                   <Route path="/dvt/verify" element={<VerifyCode/>}></Route>
                   <Route path="/dvt/requestcode" element={<RequestCode/>}></Route>
                   <Route path="/dvt/forgotpassword" element={<ForgotPassword/>}></Route>
                   <Route path="/" element={<SignIn/>}></Route>
                   <Route path="*" element={<SignIn/>}></Route>
    </Routes>

)

const MainRoute: React.FunctionComponent = () => (

    <Routes>

     <Route path="/dvt/changepassword" element={<ChangePassword/>} />
             <Route path="/dvt/selection" element={<SchemaAndTableSelectCopy />}></Route>
                   <Route path="/dvt/menu" element={<Menu />}></Route>
                  <Route path="/dvt/recommend" element={<Recommendation />}></Route>
                  <Route path="http://localhost:8090/compareData" element={<Validation />}></Route>
                   <Route path="/dvt/compare" element={<Validation />}></Route>
                    <Route path="/dvt/menu" element={<Menu />}></Route>
                    <Route path="/" element={<Menu />}></Route>
                    <Route path="*" element={<Menu />}></Route>
        </Routes>

)

const App: React.FunctionComponent = () => (
  <ThemeProvider theme={lightTheme}>
    <CssBaseline />
    <AuthProvider>
      <AuthIsSignedIn>
        <MainRoute />
      </AuthIsSignedIn>
      <AuthIsNotSignedIn>
        <SignInRoute />
      </AuthIsNotSignedIn>
    </AuthProvider>
  </ThemeProvider>
)

export default App
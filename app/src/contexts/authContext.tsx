import React, { useState, createContext,useEffect, useContext } from 'react'

import * as cognito from '../libs/cognito'

export enum AuthStatus {
  Loading,
  SignedIn,
  SignedOut,
}

export interface IAuth {
  sessionInfo?: { username?: string; email?: string; sub?: string; accessToken?: string; refreshToken?: string }
  attrInfo?: any
  authStatus?: AuthStatus
  signInWithEmail?: any
  signUpWithEmail?: any
  signOut?: any
  verifyCode?: any
  getSession?: any
  sendCode?: any
  forgotPassword?: any
  changePassword?: any
  getAttributes?: any
  setAttribute?: any
}

const defaultState: IAuth = {
  sessionInfo: {},
  authStatus: AuthStatus.Loading,
}

type Props = {
  children?: React.ReactNode
}
console.log("in authContext 35")
export const AuthContext = createContext(defaultState)
console.log("in authContext 35",AuthContext)

export const AuthIsSignedIn = ({ children }: Props) => {
  console.log( "AuthIsSignedIn ",children )
  const { authStatus }: IAuth = useContext(AuthContext)

  return <>{authStatus === AuthStatus.SignedIn ? children : null}</>
}

export const AuthIsNotSignedIn = ({ children }: Props) => {
  console.log( "AuthIsNotSignedIn",children )
  const { authStatus }: IAuth = useContext(AuthContext)
  return <>{authStatus === AuthStatus.SignedOut ? children : null}</>
}

const AuthProvider = ({ children }: Props) => {
  console.log( "AuthProvider",children )

  const [authStatus, setAuthStatus] = useState(AuthStatus.Loading)
  const [sessionInfo, setSessionInfo] = useState({})
  const [attrInfo, setAttrInfo] = useState([])

  useEffect(() => {
    async function getSessionInfo() {
      try {
        const session: any = await getSession()
        setSessionInfo({
          accessToken: session.accessToken.jwtToken,
          refreshToken: session.refreshToken.token,
        })
        window.localStorage.setItem('accessToken', `${session.accessToken.jwtToken}`)
        window.localStorage.setItem('refreshToken', `${session.refreshToken.token}`)
        await setAttribute({ Name: 'website', Value: 'https:localhost:3000' })
        const attr: any = await getAttributes()
        setAttrInfo(attr)
        setAuthStatus(AuthStatus.SignedIn)
      } catch (err) {
        setAuthStatus(AuthStatus.SignedOut)
      }
    }
    getSessionInfo()
  }, [setAuthStatus, authStatus])

  if (authStatus === AuthStatus.Loading) {
    return null
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

  async function signUpWithEmail(username: string, email: string, password: string) {
    try {
      await cognito.signUpUserWithEmail(username, email, password)
    } catch (err) {
      throw err
    }
  }

  function signOut() {
    cognito.signOut()
    setAuthStatus(AuthStatus.SignedOut)
  }

  async function verifyCode(username: string, code: string) {
    try {
      await cognito.verifyCode(username, code)
    } catch (err) {
      throw err
    }
  }

  async function getSession() {
    try {
      const session = await cognito.getSession()
      return session
    } catch (err) {
      throw err
    }
  }

  async function getAttributes() {
    try {
      const attr = await cognito.getAttributes()
      return attr
    } catch (err) {
      throw err
    }
  }

  async function setAttribute(attr: any) {
    try {
      const res = await cognito.setAttribute(attr)
      return res
    } catch (err) {
      throw err
    }
  }

  async function sendCode(username: string) {
    try {
      await cognito.sendCode(username)
    } catch (err) {
      throw err
    }
  }

  async function forgotPassword(username: string, code: string, password: string) {
    try {
      await cognito.forgotPassword(username, code, password)
    } catch (err) {
      throw err
    }
  }

  async function changePassword(oldPassword: string, newPassword: string) {
    try {
      await cognito.changePassword(oldPassword, newPassword)
    } catch (err) {
      throw err
    }
  }

  const state: IAuth = {
    authStatus,
    sessionInfo,
    attrInfo,
    signUpWithEmail,
    signInWithEmail,
    signOut,
    verifyCode,
    getSession,
    sendCode,
    forgotPassword,
    changePassword,
    getAttributes,
    setAttribute,
  }
console.log("chld",{children})
console.log("state",{state})


return (<AuthContext.Provider value={state}>{children}</AuthContext.Provider>)
}

export default AuthProvider

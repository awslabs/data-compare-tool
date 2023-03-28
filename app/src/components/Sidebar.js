import React from 'react';
import { slide as Menu } from 'react-burger-menu';
import './Sidebar.css';
import * as cognito from '../libs/cognito';
// ...

export default props => {

  function handleSignout() {
    try {
       cognito.signOut()
     // setAuthStatus(AuthStatus.SignedOut)
    } catch (err) {
      //setAuthStatus(AuthStatus.SignedOut)
    }
  }
  return (
    <Menu>
      <a className="menu-item" href="/">
        Home
      </a>
      <a className="menu-item" href="/dvt/signin">
              Login
            </a>
              <a className="menu-item" href="/dvt/signup">
                          Register User
                        </a>
         <a className="menu-item" href="/dvt/changepassword">
                    Change Password
                  </a>
      <a className="menu-item" href="/dvt/compare">
        Validation
      </a>
      <a className="menu-item" href="/dvt/selection">
        Recommendation
      </a>
      <a className="menu-item" href="/dvt/selection">
        Remediation
      </a>
        <a className="menu-item" href="/dvt/signin" onClick={handleSignout} >
              Sign out
            </a>
    </Menu>
  );
};
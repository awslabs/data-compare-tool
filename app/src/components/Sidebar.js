import React from 'react';
import { slide as Menu } from 'react-burger-menu';
import Box from "@mui/material/Box";
import './Sidebar.css';
import * as cognito from '../libs/cognito';
import logo from './logo-white.png';

const Sidebar = props => {

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
      <Box sx={{ display: 'inline-block', position: 'relative', top: '-95px', left: '-210px' }}>
        <img
          src={logo}
          alt="Logo"
          align="right"
          valign="bottom"
          width="70px"
        />
      </Box>

      <a className="menu-item" href="/">
        Home
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
      <a className="menu-item" href="/dvt/addschedules">
        Schedules
      </a>
      <a className="menu-item" href="/dvt/signin" onClick={handleSignout} >
        Sign out
      </a>
    </Menu>
  );
};
export default Sidebar;
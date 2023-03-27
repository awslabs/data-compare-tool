import React from 'react';
import { slide as Menu } from 'react-burger-menu';
import './Sidebar.css';

// ...
export default props => {
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
      <a className="menu-item" href="/dvt/compare">
        Validation
      </a>
      <a className="menu-item" href="/dvt/selection">
        Recommendation
      </a>
      <a className="menu-item" href="/dvt/selection">
        Remediation
      </a>
    </Menu>
  );
};
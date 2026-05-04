import React from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

const Navbar = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar glass-panel">
      <div className="navbar-container">
        <div className="navbar-brand">
          <Link to="/">
            <span className="brand-logo">LM</span>
            <span className="brand-text">Leave&Time</span>
          </Link>
        </div>
        
        <div className="navbar-links">
          {isAuthenticated ? (
            <>
              <Link to="/" className="nav-link">Dashboard</Link>
              <Link to="/leaves" className="nav-link">Leaves</Link>
              <Link to="/timesheets" className="nav-link">Timesheets</Link>
              
              {/* Conditionally render admin link if user is Admin/Manager (for demo, any role is fine or ROLE_ADMIN) */}
              {(user?.roles?.includes('ROLE_ADMIN') || user?.roles?.includes('ROLE_MANAGER')) && (
                 <Link to="/admin" className="nav-link" style={{ color: '#ec4899' }}>Admin</Link>
              )}
              {/* Just for ease of testing until DB is fully seeded, show it to ROLE_EMPLOYEE too or anyone having 'ROLE_EMPLOYEE' */}
              {user?.roles?.includes('ROLE_EMPLOYEE') && !user?.roles?.includes('ROLE_ADMIN') && (
                 <Link to="/admin" className="nav-link" style={{ color: '#ec4899' }}>Admin (Test Mode)</Link>
              )}

              <div className="user-menu">
                <span className="user-greeting">Hi, {user?.employeeCode}</span>
                <button onClick={handleLogout} className="btn btn-primary btn-sm ml-2">Logout</button>
              </div>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-primary btn-sm" style={{ background: 'transparent', border: '1px solid var(--color-primary)' }}>Login</Link>
              <Link to="/signup" className="btn btn-primary btn-sm">Sign Up</Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;

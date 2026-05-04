import React from 'react';
import { NavLink, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/* ── Icons (inline SVGs – no extra dep) ── */
const IconDashboard = () => (
  <svg className="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/>
    <rect x="14" y="14" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/>
  </svg>
);
const IconLeaves = () => (
  <svg className="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <path d="M12 22V12"/><path d="M8 6c0 0 0 6 4 6s4-6 4-6c-3-3-8-2-8 0Z"/>
    <path d="M12 12c0 0-4 1-6 5h12c-1-3-6-5-6-5Z"/>
  </svg>
);
const IconTimesheet = () => (
  <svg className="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/>
    <line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
    <path d="M8 14h.01M12 14h.01M16 14h.01M8 18h.01M12 18h.01"/>
  </svg>
);
const IconAdmin = () => (
  <svg className="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <path d="M12 2l2.4 7.4H22l-6.2 4.5 2.4 7.4L12 17l-6.2 4.3 2.4-7.4L2 9.4h7.6L12 2Z"/>
  </svg>
);
const IconLogout = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>
  </svg>
);

const Sidebar = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const isAdmin = user?.roles?.includes('ROLE_ADMIN') || user?.roles?.includes('ROLE_MANAGER') || user?.roles?.includes('ROLE_EMPLOYEE');

  const handleLogout = () => { logout(); navigate('/login'); };
  const initials = user?.employeeCode ? user.employeeCode.slice(0, 2).toUpperCase() : '??';

  if (!isAuthenticated) return null;

  return (
    <aside className="sidebar">
      {/* Brand */}
      <div className="sidebar-brand">
        <div className="sidebar-brand-icon">LT</div>
        <div>
          <div className="sidebar-brand-name">LeaveTime</div>
          <div className="sidebar-brand-sub">Workforce Hub</div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="sidebar-nav">
        <span className="sidebar-section-label">Workspace</span>

        <NavLink to="/" end className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}>
          <IconDashboard /> Dashboard
        </NavLink>
        <NavLink to="/leaves" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}>
          <IconLeaves /> Leaves
        </NavLink>
        <NavLink to="/timesheets" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}>
          <IconTimesheet /> Timesheets
        </NavLink>

        {isAdmin && (
          <>
            <span className="sidebar-section-label">Management</span>
            <NavLink to="/admin" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}>
              <IconAdmin /> Admin Portal
            </NavLink>
          </>
        )}
      </nav>

      {/* User Card */}
      <div className="sidebar-footer">
        <div className="user-card">
          <div className="user-avatar">{initials}</div>
          <div className="user-info">
            <div className="user-name">{user?.employeeCode}</div>
            <div className="user-role">
              {user?.roles?.includes('ROLE_ADMIN') ? 'Admin' :
               user?.roles?.includes('ROLE_MANAGER') ? 'Manager' : 'Employee'}
            </div>
          </div>
          <button className="logout-btn" onClick={handleLogout} title="Logout">
            <IconLogout />
          </button>
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;

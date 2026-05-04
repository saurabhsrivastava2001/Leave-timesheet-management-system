import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import { useAuth } from '../context/AuthContext';
import { leaveApi } from '../api/leaveApi';
import { adminApi } from '../api/adminApi';
import './Dashboard.css';

/* ── SVG Icons ─────────────────────────────────────── */
const LeafIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <path d="M11 20A7 7 0 0 1 9.8 6.1C15.5 5 17 4.48 19 2c1 2 2 4.18 2 8 0 5.5-4.78 10-10 10Z"/>
    <path d="M2 21c0-3 1.85-5.36 5.08-6C9.5 14.52 12 13 13 12"/>
  </svg>
);

const ChartIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/>
    <line x1="6" y1="20" x2="6" y2="14"/><line x1="2" y1="20" x2="22" y2="20"/>
  </svg>
);

const ClockIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
  </svg>
);

const AlertIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0Z"/>
    <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
  </svg>
);

const ArrowIcon = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/>
  </svg>
);

/* ── Stat Card ─────────────────────────────────────── */
const StatCard = ({ label, value, sub, accent, Icon, linkTo, linkLabel, loading }) => (
  <div className="stat-card" style={{ '--c': accent }}>
    <div className="stat-card-top">
      <div className="stat-icon-wrap" style={{ color: accent, background: `${accent}1a` }}>
        <Icon />
      </div>
      <span className="stat-label">{label}</span>
    </div>
    <div className="stat-value" style={{ color: accent }}>
      {loading ? <span className="skel-val" /> : value}
    </div>
    <p className="stat-sub">{sub}</p>
    {linkTo && (
      <Link to={linkTo} className="stat-link">
        {linkLabel} <ArrowIcon />
      </Link>
    )}
  </div>
);

/* ── Dashboard ─────────────────────────────────────── */
const Dashboard = () => {
  const { user } = useAuth();
  const [balance,      setBalance]      = useState(null);
  const [recentLeaves, setRecentLeaves] = useState([]);
  const [pendingCount, setPendingCount] = useState(null);
  const [loading,      setLoading]      = useState(true);

  const isManager = user?.roles?.some(r => ['ROLE_ADMIN', 'ROLE_MANAGER'].includes(r));

  const today = new Date().toLocaleDateString('en-GB', {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
  });

  useEffect(() => {
    if (!user?.employeeCode) return;
    (async () => {
      try {
        const [bals, leaves] = await Promise.allSettled([
          leaveApi.getBalance(user.employeeCode),
          leaveApi.getMyLeaves(),
        ]);
        if (bals.status === 'fulfilled' && Array.isArray(bals.value)) {
          setBalance(bals.value.find(b => b.leaveType === 'EARNED') || bals.value[0] || null);
        }
        if (leaves.status === 'fulfilled' && Array.isArray(leaves.value)) {
          setRecentLeaves([...leaves.value].sort((a, b) => b.id - a.id).slice(0, 5));
        }
        if (isManager) {
          try { const pl = await adminApi.getPendingLeaves(); setPendingCount(Array.isArray(pl) ? pl.length : 0); }
          catch { setPendingCount(0); }
        }
      } catch (e) { console.error(e); }
      finally { setLoading(false); }
    })();
  }, [user, isManager]);

  const avail = balance ? balance.allocated - balance.consumed : 0;
  const total = balance ? balance.allocated : 0;
  const pct   = total ? Math.round((balance.consumed / total) * 100) : 0;

  return (
    <div className="app-shell">
      <Sidebar />
      <div className="page-content">
        <div className="dash-page page-enter">

          {/* ── Header ── */}
          <div className="dash-header">
            <div className="dash-header-left">
              <p className="dash-date">{today}</p>
              <h1 className="dash-heading">
                Hello, <span className="dash-name">{user?.employeeCode}</span>
              </h1>
            </div>
            <Link to="/leaves" className="btn btn-primary">Request Leave</Link>
          </div>

          {/* ── KPI Grid ── */}
          <div className="kpi-grid">
            <StatCard
              label="Leave Balance"
              value={avail}
              sub={`${total} days total allocation`}
              accent="#34d399"
              Icon={LeafIcon}
              linkTo="/leaves"
              linkLabel="View details"
              loading={loading}
            />
            <StatCard
              label="Days Used"
              value={`${pct}%`}
              sub="of annual allocation consumed"
              accent="#38bdf8"
              Icon={ChartIcon}
              linkTo="/leaves"
              linkLabel="See history"
              loading={loading}
            />
            {isManager ? (
              <StatCard
                label="Pending Approvals"
                value={pendingCount ?? '—'}
                sub="requests awaiting manager action"
                accent="#fbbf24"
                Icon={AlertIcon}
                linkTo="/admin"
                linkLabel="Review queue"
                loading={loading}
              />
            ) : (
              <StatCard
                label="Log Timesheet"
                value="Today"
                sub="Keep your weekly hours current"
                accent="#a78bfa"
                Icon={ClockIcon}
                linkTo="/timesheets"
                linkLabel="Log hours"
                loading={loading}
              />
            )}
          </div>

          {/* ── Lower grid ── */}
          <div className="dash-lower">

            {/* Usage panel */}
            <div className="glass-panel dash-panel">
              <div className="dash-panel-header">
                <h3>Leave Overview</h3>
              </div>
              {loading ? (
                <div className="skel-block" />
              ) : (
                <>
                  <div className="usage-row">
                    <span className="usage-label">EARNED</span>
                    <div className="usage-track">
                      <div
                        className="usage-fill"
                        style={{
                          width: `${pct}%`,
                          background: pct > 80
                            ? 'var(--color-error)'
                            : pct > 50
                            ? 'var(--accent-amber)'
                            : 'var(--accent-emerald)'
                        }}
                      />
                    </div>
                    <span className="usage-pct">{pct}%</span>
                  </div>
                  <div className="usage-meta">
                    <span className="usage-meta-item">
                      <span className="usage-dot" style={{ background: 'var(--accent-emerald)' }} />
                      {avail} days remaining
                    </span>
                    <span className="usage-meta-item">
                      <span className="usage-dot" style={{ background: 'var(--color-text-muted)' }} />
                      {balance?.consumed ?? 0} days used
                    </span>
                  </div>
                </>
              )}
            </div>

            {/* Activity panel */}
            <div className="glass-panel dash-panel">
              <div className="dash-panel-header">
                <h3>Recent Activity</h3>
                <Link to="/leaves" className="panel-link">View all <ArrowIcon /></Link>
              </div>

              {loading ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                  {[1, 2, 3].map(i => <div key={i} className="skel-row" />)}
                </div>
              ) : recentLeaves.length === 0 ? (
                <div className="dash-empty">
                  <p>No leave applications on record yet.</p>
                  <Link to="/leaves" className="btn btn-primary btn-sm" style={{ marginTop: 16 }}>Apply for Leave</Link>
                </div>
              ) : (
                <ul className="act-list">
                  {recentLeaves.map(lv => (
                    <li key={lv.id} className="act-row">
                      <div className="act-marker" />
                      <div className="act-content">
                        <div className="act-title">{lv.leaveType} Leave</div>
                        <div className="act-dates">{lv.startDate} &mdash; {lv.endDate}</div>
                      </div>
                      <span className={`status-pill ${lv.status?.toLowerCase() || 'pending'}`}>
                        {lv.status || 'PENDING'}
                      </span>
                    </li>
                  ))}
                </ul>
              )}
            </div>

          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;

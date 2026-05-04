import React, { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import { adminApi } from '../api/adminApi';
import { getApiErrorMessage } from '../api/errors';
import './Leaves.css';

const Admin = () => {
  const [activeTab, setActiveTab] = useState('leaves');
  const [msg,       setMsg]       = useState({ text: '', type: '' });
  const [isLoading, setIsLoading] = useState(false);

  const [pendingTimesheets, setPendingTimesheets] = useState([]);
  const [pendingLeaves,     setPendingLeaves]     = useState([]);

  // Master data
  const [holidayDate, setHolidayDate] = useState('');
  const [holidayDesc, setHolidayDesc] = useState('');

  const getEmployeeDisplay = (item) => (
    item.employeeName || item.name || item.employeeCode || item.employee || 'Unknown employee'
  );

  const fetchData = async () => {
    setIsLoading(true);
    try {
      if (activeTab === 'timesheets') {
        const d = await adminApi.getPendingTimesheets();
        setPendingTimesheets(Array.isArray(d) ? d.filter((item) => !item.error) : []);
      } else if (activeTab === 'leaves') {
        const d = await adminApi.getPendingLeaves();
        setPendingLeaves(Array.isArray(d) ? d.filter((item) => !item.error) : []);
      }
    } catch (error) {
      setMsg({ text: getApiErrorMessage(error, 'Could not load admin data.'), type: 'error' });
    }
    finally { setIsLoading(false); }
  };

  useEffect(() => { fetchData(); }, [activeTab]);

  const handleApproveTimesheet = async (id, status) => {
    const comments = window.prompt(`Remarks for ${status}:`, status === 'APPROVED' ? 'Good work!' : 'Please revise.');
    if (comments === null) return;
    try {
      await adminApi.approveTimesheet(id, status, comments);
      setMsg({ text: `Timesheet #${id} ${status}`, type: status === 'APPROVED' ? 'success' : 'error' });
      setPendingTimesheets(p => p.filter(t => t.id !== id));
    } catch (error) { setMsg({ text: getApiErrorMessage(error, 'Action failed.'), type: 'error' }); }
  };

  const handleApproveLeave = async (id, status) => {
    const comments = window.prompt(`Remarks for ${status}:`, status === 'APPROVED' ? 'Approved!' : 'Declined.');
    if (comments === null) return;
    try {
      await adminApi.approveLeave(id, status, comments);
      setMsg({ text: `Leave #${id} ${status}`, type: status === 'APPROVED' ? 'success' : 'error' });
      setPendingLeaves(p => p.filter(l => l.id !== id));
    } catch (error) { setMsg({ text: getApiErrorMessage(error, 'Action failed.'), type: 'error' }); }
  };

  const handleCreateHoliday = async (e) => {
    e.preventDefault();
    try {
      await adminApi.createHoliday({ date: holidayDate, description: holidayDesc });
      setMsg({ text: 'Holiday created!', type: 'success' });
      setHolidayDate(''); setHolidayDesc('');
    } catch (error) { setMsg({ text: getApiErrorMessage(error, 'Failed to create holiday.'), type: 'error' }); }
  };

  return (
    <div className="app-shell">
      <Sidebar />
      <div className="page-content">
        <div className="page-inner page-enter">

          <div className="page-header">
            <div>
              <h1 className="page-title">Admin Portal</h1>
              <p className="page-sub">Review pending requests and manage master data.</p>
            </div>
          </div>

          {/* Tabs */}
          <div className="admin-page-tabs">
            {['leaves','timesheets','master'].map(t => (
              <button
                key={t}
                className={`admin-tab-btn${activeTab === t ? ' active' : ''}`}
                onClick={() => setActiveTab(t)}
              >
                {t === 'leaves' ? 'Leaves' : t === 'timesheets' ? 'Timesheets' : 'Master Data'}
              </button>
            ))}
          </div>

          {msg.text && <div className={`message-banner ${msg.type}`}>{msg.text}</div>}

          {/* ── Leaves Panel ── */}
          {activeTab === 'leaves' && (
            <div className="glass-panel admin-card page-enter-delay">
              <div className="admin-table-header">
                <h3 className="card-section-title" style={{ marginBottom: 0 }}>Pending Leave Requests</h3>
                <button className="btn btn-ghost btn-sm" onClick={fetchData}>↻ Refresh</button>
              </div>
              {isLoading ? (
                <div style={{ display:'flex', flexDirection:'column', gap:8 }}>
                  {[1,2,3].map(i => <div key={i} className="skel-row" />)}
                </div>
              ) : pendingLeaves.length === 0 ? (
                <div className="empty-state">
                  <p>All caught up — no pending leaves.</p>
                </div>
              ) : (
                <div className="table-container">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>#</th><th>Employee</th><th>Type</th><th>Duration</th><th>Reason</th><th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {pendingLeaves.map(lv => (
                        <tr key={lv.id}>
                          <td style={{ color: 'var(--color-text-muted)' }}>{lv.id}</td>
                          <td style={{ fontWeight: 600, color: 'var(--color-text-primary)' }}>{getEmployeeDisplay(lv)}</td>
                          <td>{lv.leaveType}</td>
                          <td>{lv.startDate} → {lv.endDate}</td>
                          <td style={{ maxWidth: 180, overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}>{lv.reason}</td>
                          <td>
                            <div className="action-cell">
                              <button className="btn btn-success btn-sm" onClick={() => handleApproveLeave(lv.id, 'APPROVED')}>✓ Approve</button>
                              <button className="btn btn-danger btn-sm"  onClick={() => handleApproveLeave(lv.id, 'REJECTED')}>✕ Reject</button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {/* ── Timesheets Panel ── */}
          {activeTab === 'timesheets' && (
            <div className="glass-panel admin-card page-enter-delay">
              <div className="admin-table-header">
                <h3 className="card-section-title" style={{ marginBottom: 0 }}>Pending Timesheets</h3>
                <button className="btn btn-ghost btn-sm" onClick={fetchData}>↻ Refresh</button>
              </div>
              {isLoading ? (
                <div style={{ display:'flex', flexDirection:'column', gap:8 }}>
                  {[1,2,3].map(i => <div key={i} className="skel-row" />)}
                </div>
              ) : pendingTimesheets.length === 0 ? (
                <div className="empty-state">
                  <p>No timesheets awaiting approval.</p>
                </div>
              ) : (
                <div className="table-container">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>#</th><th>Employee</th><th>Week Of</th><th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {pendingTimesheets.map(ts => (
                        <tr key={ts.id}>
                          <td style={{ color: 'var(--color-text-muted)' }}>{ts.id}</td>
                          <td style={{ fontWeight: 600, color: 'var(--color-text-primary)' }}>{getEmployeeDisplay(ts)}</td>
                          <td>{ts.weekStartDate}</td>
                          <td>
                            <div className="action-cell">
                              <button className="btn btn-success btn-sm" onClick={() => handleApproveTimesheet(ts.id, 'APPROVED')}>✓ Approve</button>
                              <button className="btn btn-danger btn-sm"  onClick={() => handleApproveTimesheet(ts.id, 'REJECTED')}>✕ Reject</button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {/* ── Master Data Panel ── */}
          {activeTab === 'master' && (
            <div className="glass-panel admin-card page-enter-delay" style={{ maxWidth: 500 }}>
              <h3 className="card-section-title">Add Public Holiday</h3>
              <form onSubmit={handleCreateHoliday}>
                <div className="form-group">
                  <label className="form-label">Date</label>
                  <input type="date" className="form-input" value={holidayDate} onChange={e => setHolidayDate(e.target.value)} required />
                </div>
                <div className="form-group">
                  <label className="form-label">Description</label>
                  <input type="text" className="form-input" value={holidayDesc} onChange={e => setHolidayDesc(e.target.value)} required placeholder="e.g. Christmas Day" />
                </div>
                <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>+ Create Holiday</button>
              </form>
            </div>
          )}

        </div>
      </div>
    </div>
  );
};

export default Admin;

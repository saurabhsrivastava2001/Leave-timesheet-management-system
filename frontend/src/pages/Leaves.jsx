import React, { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import { useAuth } from '../context/AuthContext';
import { leaveApi } from '../api/leaveApi';

const Leaves = () => {
  const { user } = useAuth();
  const [balance,  setBalance]  = useState([]);
  const [history,  setHistory]  = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  const [leaveType,  setLeaveType]  = useState('EARNED');
  const [startDate,  setStartDate]  = useState('');
  const [endDate,    setEndDate]    = useState('');
  const [reason,     setReason]     = useState('');
  const [msg,        setMsg]        = useState({ text: '', type: '' });
  const [submitting, setSubmitting] = useState(false);

  const fetchData = async () => {
    setIsLoading(true);
    try {
      const [bal, hist] = await Promise.allSettled([
        leaveApi.getBalance(user.employeeCode),
        leaveApi.getMyLeaves(),
      ]);
      if (bal.status === 'fulfilled') setBalance(bal.value || []);
      if (hist.status === 'fulfilled') setHistory(hist.value || []);
    } catch (err) { console.error(err); }
    finally { setIsLoading(false); }
  };

  useEffect(() => { if (user?.employeeCode) fetchData(); }, [user]);

  const handleApply = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setMsg({ text: '', type: '' });
    try {
      await leaveApi.applyLeave({ leaveType, startDate, endDate, reason });
      setMsg({ text: 'Leave request submitted!', type: 'success' });
      setStartDate(''); setEndDate(''); setReason('');
      fetchData();
    } catch {
      setMsg({ text: 'Failed to submit. Check your balance or dates.', type: 'error' });
    } finally { setSubmitting(false); }
  };

  const earnedBal = Array.isArray(balance)
    ? balance.find(b => b.leaveType === 'EARNED')
    : null;

  return (
    <div className="app-shell">
      <Sidebar />
      <div className="page-content">
        <div className="page-inner page-enter">

          {/* Header */}
          <div className="page-header">
            <div>
              <h1 className="page-title">Leaves</h1>
              <p className="page-sub">Manage your time off requests and check balances.</p>
            </div>
          </div>

          {/* Balance Strip */}
          {!isLoading && (
            <div className="balance-strip page-enter-delay">
              {Array.isArray(balance) && balance.length > 0 ? balance.map((b, i) => (
                <div key={i} className="balance-badge">
                  <div className="balance-badge-type">{b.leaveType}</div>
                  <div className="balance-badge-num">{b.allocated - b.consumed}</div>
                  <div className="balance-badge-label">of {b.allocated} days left</div>
                </div>
              )) : (
                <div className="balance-badge empty">
                  <div className="balance-badge-type">EARNED</div>
                  <div className="balance-badge-num">—</div>
                  <div className="balance-badge-label">No balance seeded yet</div>
                </div>
              )}
            </div>
          )}

          {/* Two-col layout */}
          <div className="leaves-layout page-enter-delay">

            {/* Apply Form */}
            <div className="glass-panel leaves-form-card">
              <h3 className="card-section-title">Apply for Leave</h3>

              {msg.text && <div className={`message-banner ${msg.type}`}>{msg.text}</div>}

              <form onSubmit={handleApply}>
                <div className="form-group">
                  <label className="form-label">Leave Type</label>
                  <select className="form-input" value={leaveType} onChange={e => setLeaveType(e.target.value)}>
                    <option value="EARNED">Earned Leave</option>
                    <option value="SICK">Sick Leave</option>
                    <option value="CASUAL">Casual Leave</option>
                  </select>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label className="form-label">Start Date</label>
                    <input type="date" className="form-input" value={startDate} onChange={e => setStartDate(e.target.value)} required />
                  </div>
                  <div className="form-group">
                    <label className="form-label">End Date</label>
                    <input type="date" className="form-input" value={endDate} onChange={e => setEndDate(e.target.value)} required />
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label">Reason</label>
                  <textarea className="form-input" rows="3" value={reason} onChange={e => setReason(e.target.value)} required placeholder="Briefly describe your reason..." />
                </div>

                <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={submitting}>
                  {submitting ? 'Submitting…' : '→ Submit Request'}
                </button>
              </form>
            </div>

            {/* History Table */}
            <div className="glass-panel leaves-history-card">
              <h3 className="card-section-title">My Leave History</h3>
              {isLoading ? (
                <div style={{ display:'flex', flexDirection:'column', gap:8 }}>
                  {[1,2,3].map(i => <div key={i} className="skel-row" />)}
                </div>
              ) : history.length === 0 ? (
                <div className="empty-state">
                  <p>No leave applications yet.</p>
                </div>
              ) : (
                <div className="table-container">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Type</th>
                        <th>Duration</th>
                        <th>Reason</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {history.map((h, i) => (
                        <tr key={h.id || i}>
                          <td style={{ fontWeight: 500, color: 'var(--color-text-primary)' }}>{h.leaveType}</td>
                          <td>{h.startDate} → {h.endDate}</td>
                          <td style={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{h.reason}</td>
                          <td>
                            <span className={`status-pill ${h.status?.toLowerCase() || 'pending'}`}>
                              {h.status || 'PENDING'}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Leaves;

import React, { useState } from 'react';
import Sidebar from '../components/Sidebar';
import { timesheetApi } from '../api/timesheetApi';
import './Leaves.css';

const Timesheets = () => {
  const [workDate,     setWorkDate]     = useState('');
  const [hours,        setHours]        = useState('');
  const [taskSummary,  setTaskSummary]  = useState('');
  const [projectCode,  setProjectCode]  = useState('PRJ-ALPHA');
  const [msg,          setMsg]          = useState({ text: '', type: '' });
  const [isLoading,    setIsLoading]    = useState(false);

  const getMonday = (d) => {
    const date = new Date(d);
    const day = date.getDay() || 7;
    date.setDate(date.getDate() - (day - 1));
    return date.toISOString().split('T')[0];
  };

  const handleApply = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setMsg({ text: '', type: '' });
    try {
      const weekStart = getMonday(workDate);
      await timesheetApi.logEntries({
        weekStartDate: weekStart,
        entries: [{ projectCode, workDate, hours: parseFloat(hours), taskSummary }]
      });
      await timesheetApi.submitTimesheet(weekStart);
      setMsg({ text: '✓ Timesheet logged and submitted to manager!', type: 'success' });
      setHours(''); setTaskSummary('');
    } catch {
      setMsg({ text: 'Submission failed. Please try again.', type: 'error' });
    } finally { setIsLoading(false); }
  };

  return (
    <div className="app-shell">
      <Sidebar />
      <div className="page-content">
        <div className="page-inner page-enter">

          <div className="page-header">
            <div>
              <h1 className="page-title">Timesheets</h1>
              <p className="page-sub">Log your daily hours directly — no drafts, no hassle.</p>
            </div>
          </div>

          {/* Central card */}
          <div className="glass-panel timesheet-form-card page-enter-delay">
            <h3>Log Daily Hours</h3>
            <p className="sub">Fill in what you worked on and hit submit. We'll handle the rest.</p>

            {msg.text && <div className={`message-banner ${msg.type}`} style={{ marginBottom: 'var(--sp-5)' }}>{msg.text}</div>}

            <form onSubmit={handleApply}>
              <div className="form-group">
                <label className="form-label">Project</label>
                <select className="form-input" value={projectCode} onChange={e => setProjectCode(e.target.value)}>
                  <option value="PRJ-ALPHA">Alpha Web App (PRJ-ALPHA)</option>
                  <option value="PRJ-BETA">Beta Mobile App (PRJ-BETA)</option>
                  <option value="PRJ-GAMMA">Gamma Platform (PRJ-GAMMA)</option>
                </select>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">Work Date</label>
                  <input type="date" className="form-input" value={workDate} onChange={e => setWorkDate(e.target.value)} required />
                </div>
                <div className="form-group">
                  <label className="form-label">Hours Worked</label>
                  <input type="number" min="0.5" max="24" step="0.5" className="form-input"
                    value={hours} onChange={e => setHours(e.target.value)} required placeholder="e.g. 8.0" />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Task Summary</label>
                <textarea className="form-input" rows="3" value={taskSummary}
                  onChange={e => setTaskSummary(e.target.value)} required
                  placeholder="Describe what you accomplished today..." />
              </div>

              <button type="submit" className="submit-btn-large" disabled={isLoading}>
                {isLoading ? 'Submitting…' : '→ Direct Apply & Submit'}
              </button>
            </form>
          </div>

          {/* Info blurb */}
          <div style={{ maxWidth: 560, margin: 'var(--sp-5) auto 0', padding: '0 var(--sp-4)' }}>
            <p className="text-muted text-sm" style={{ textAlign: 'center' }}>
              Entries are auto-submitted to your manager immediately. Contact your manager if you need to make corrections.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Timesheets;

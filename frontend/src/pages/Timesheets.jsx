import React, { useEffect, useMemo, useState } from 'react';
import Sidebar from '../components/Sidebar';
import { useAuth } from '../context/AuthContext';
import { timesheetApi } from '../api/timesheetApi';
import { getApiErrorMessage } from '../api/errors';
import './Timesheets.css';

const emptyMessage = { text: '', type: '' };

const formatDate = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const parseLocalDate = (value) => new Date(`${value}T00:00:00`);

const getMonday = (value = new Date()) => {
  const date = value instanceof Date ? new Date(value) : parseLocalDate(value);
  const day = date.getDay() || 7;
  date.setDate(date.getDate() - (day - 1));
  date.setHours(0, 0, 0, 0);
  return formatDate(date);
};

const getWeekDays = (weekStart) => {
  const start = new Date(`${weekStart}T00:00:00`);
  return Array.from({ length: 7 }, (_, index) => {
    const current = new Date(start);
    current.setDate(start.getDate() + index);
    return {
      label: current.toLocaleDateString('en-GB', { weekday: 'short', day: 'numeric', month: 'short' }),
      value: formatDate(current),
    };
  });
};

const createBlankEntry = (workDate) => ({
  workDate,
  projectCode: '',
  hours: '',
  taskSummary: '',
});

const createDefaultWeekEntries = (weekStart) =>
  getWeekDays(weekStart).map((day) => createBlankEntry(day.value));

const formatWeekRange = (weekStart) => {
  const start = new Date(`${weekStart}T00:00:00`);
  const end = new Date(start);
  end.setDate(start.getDate() + 6);
  return `${start.toLocaleDateString('en-GB', { day: 'numeric', month: 'short' })} - ${end.toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })}`;
};

const normalizeEntriesForSave = (entries) => {
  const normalized = [];

  for (const entry of entries) {
    const hasAnyValue = entry.projectCode || entry.hours || entry.taskSummary?.trim();
    if (!hasAnyValue) {
      continue;
    }

    if (!entry.projectCode || !entry.hours) {
      throw new Error('Every filled row needs a project and hours.');
    }

    normalized.push({
      projectCode: entry.projectCode,
      workDate: entry.workDate,
      hours: Number(entry.hours),
      taskSummary: entry.taskSummary?.trim() || '',
    });
  }

  return normalized;
};

const Timesheets = () => {
  const { user } = useAuth();
  const [weekStart, setWeekStart] = useState(getMonday());
  const [entries, setEntries] = useState(() => createDefaultWeekEntries(getMonday()));
  const [history, setHistory] = useState([]);
  const [projects, setProjects] = useState([]);
  const [sheetStatus, setSheetStatus] = useState('DRAFT');
  const [managerComments, setManagerComments] = useState('');
  const [message, setMessage] = useState(emptyMessage);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const weekDays = useMemo(() => getWeekDays(weekStart), [weekStart]);
  const totalHours = useMemo(
    () => entries.reduce((sum, entry) => sum + (Number(entry.hours) || 0), 0),
    [entries]
  );

  const syncEntriesToWeek = (nextEntries) => {
    const byDate = new Map();
    nextEntries.forEach((entry) => {
      const list = byDate.get(entry.workDate) || [];
      list.push({
        workDate: entry.workDate,
        projectCode: entry.projectCode || '',
        hours: entry.hours ?? '',
        taskSummary: entry.taskSummary || '',
      });
      byDate.set(entry.workDate, list);
    });

    const hydrated = [];
    weekDays.forEach((day) => {
      const matches = byDate.get(day.value);
      if (matches?.length) {
        hydrated.push(...matches);
      } else {
        hydrated.push(createBlankEntry(day.value));
      }
    });
    return hydrated;
  };

  const loadWeek = async (selectedWeek) => {
    setIsLoading(true);
    setMessage(emptyMessage);
    try {
      const [projectsData, historyData, timesheetData] = await Promise.all([
        timesheetApi.getProjects(),
        timesheetApi.getHistory(),
        timesheetApi.getWeek(selectedWeek),
      ]);

      setProjects(Array.isArray(projectsData) ? projectsData : []);
      setHistory(Array.isArray(historyData) ? historyData : []);

      if (timesheetData) {
        setEntries(syncEntriesToWeek(timesheetData.entries || []));
        setSheetStatus(timesheetData.status || 'DRAFT');
        setManagerComments(timesheetData.managerComments || '');
      } else {
        setEntries(createDefaultWeekEntries(selectedWeek));
        setSheetStatus('DRAFT');
        setManagerComments('');
      }
    } catch (error) {
      console.error(error);
      setMessage({ text: 'We could not load the timesheet details for this week.', type: 'error' });
      setEntries(createDefaultWeekEntries(selectedWeek));
      setSheetStatus('DRAFT');
      setManagerComments('');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadWeek(weekStart);
  }, [weekStart]);

  const updateEntry = (index, field, value) => {
    setEntries((current) =>
      current.map((entry, currentIndex) =>
        currentIndex === index ? { ...entry, [field]: value } : entry
      )
    );
  };

  const addRowForDate = (workDate) => {
    setEntries((current) => {
      const next = [...current];
      const insertAfter = next.reduce((lastMatch, entry, index) => (
        entry.workDate === workDate ? index : lastMatch
      ), -1);
      next.splice(insertAfter + 1, 0, createBlankEntry(workDate));
      return next;
    });
  };

  const removeRow = (index) => {
    setEntries((current) => {
      const target = current[index];
      const rowsForDate = current.filter((entry) => entry.workDate === target.workDate);
      if (rowsForDate.length === 1) {
        return current.map((entry, currentIndex) => (
          currentIndex === index ? createBlankEntry(entry.workDate) : entry
        ));
      }
      return current.filter((_, currentIndex) => currentIndex !== index);
    });
  };

  const saveTimesheet = async () => {
    setIsSaving(true);
    setMessage(emptyMessage);
    try {
      const normalizedEntries = normalizeEntriesForSave(entries);
      const response = await timesheetApi.saveWeek(weekStart, {
        weekStartDate: weekStart,
        entries: normalizedEntries,
      });
      setEntries(syncEntriesToWeek(response.entries || []));
      setSheetStatus(response.status || 'DRAFT');
      setManagerComments(response.managerComments || '');
      setMessage({ text: 'Draft saved for this week.', type: 'success' });
      const historyData = await timesheetApi.getHistory();
      setHistory(Array.isArray(historyData) ? historyData : []);
    } catch (error) {
      setMessage({
        text: getApiErrorMessage(error, 'Saving failed. Please review your rows.'),
        type: 'error',
      });
    } finally {
      setIsSaving(false);
    }
  };

  const submitTimesheet = async () => {
    setIsSubmitting(true);
    setMessage(emptyMessage);
    try {
      const normalizedEntries = normalizeEntriesForSave(entries);
      const saved = await timesheetApi.saveWeek(weekStart, {
        weekStartDate: weekStart,
        entries: normalizedEntries,
      });
      const submitted = await timesheetApi.submitTimesheet(weekStart);
      setEntries(syncEntriesToWeek(saved.entries || []));
      setSheetStatus(submitted.status || 'SUBMITTED');
      setManagerComments(submitted.managerComments || '');
      setMessage({ text: 'Timesheet submitted for approval.', type: 'success' });
      const historyData = await timesheetApi.getHistory();
      setHistory(Array.isArray(historyData) ? historyData : []);
    } catch (error) {
      setMessage({
        text: getApiErrorMessage(error, 'Submission failed. Please review your week and try again.'),
        type: 'error',
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const isLocked = sheetStatus === 'SUBMITTED' || sheetStatus === 'APPROVED';

  return (
    <div className="app-shell">
      <Sidebar />
      <div className="page-content">
        <div className="page-inner page-enter">
          <div className="page-header">
            <div>
              <h1 className="page-title">Timesheets</h1>
              <p className="page-sub">Work with one weekly sheet at a time. Save a draft first, then submit when the week looks right.</p>
            </div>
          </div>

          <div className="timesheet-layout page-enter-delay">
            <section className="glass-panel timesheet-week-card">
              <div className="timesheet-week-header">
                <div>
                  <div className="timesheet-eyebrow">Weekly Timesheet</div>
                  <h3>{formatWeekRange(weekStart)}</h3>
                  <p>{user?.employeeCode || 'Employee'} · Week starts on Monday</p>
                </div>

                <div className="timesheet-week-controls">
                  <label className="form-label" htmlFor="weekStart">Week Start</label>
                  <input
                    id="weekStart"
                    type="date"
                    className="form-input"
                    value={weekStart}
                    onChange={(e) => setWeekStart(getMonday(e.target.value))}
                  />
                </div>
              </div>

              <div className="timesheet-summary-row">
                <div className="timesheet-summary-pill">
                  <span>Status</span>
                  <strong>{sheetStatus}</strong>
                </div>
                <div className="timesheet-summary-pill">
                  <span>Total Hours</span>
                  <strong>{totalHours.toFixed(1)}</strong>
                </div>
                <div className="timesheet-summary-pill">
                  <span>Projects</span>
                  <strong>{projects.length}</strong>
                </div>
              </div>

              {managerComments && (
                <div className="timesheet-note">
                  <strong>Manager comments:</strong> {managerComments}
                </div>
              )}

              {message.text && <div className={`message-banner ${message.type}`}>{message.text}</div>}

              {isLoading ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                  {[1, 2, 3, 4].map((index) => <div key={index} className="skel-row" />)}
                </div>
              ) : (
                <>
                  <div className="table-container">
                    <table className="data-table timesheet-table">
                      <thead>
                        <tr>
                          <th>Date</th>
                          <th>Project</th>
                          <th>Hours</th>
                          <th>Task Summary</th>
                          <th>Action</th>
                        </tr>
                      </thead>
                      <tbody>
                        {entries.map((entry, index) => (
                          <tr key={`${entry.workDate}-${index}`}>
                            <td>
                              <div className="timesheet-day-cell">
                                <strong>{weekDays.find((day) => day.value === entry.workDate)?.label || entry.workDate}</strong>
                                <span>{entry.workDate}</span>
                              </div>
                            </td>
                            <td>
                              <select
                                className="form-input"
                                value={entry.projectCode}
                                onChange={(e) => updateEntry(index, 'projectCode', e.target.value)}
                                disabled={isLocked}
                              >
                                <option value="">Select project</option>
                                {projects.map((project) => (
                                  <option key={project.projectCode} value={project.projectCode}>
                                    {project.name} ({project.projectCode})
                                  </option>
                                ))}
                              </select>
                            </td>
                            <td>
                              <input
                                type="number"
                                min="0.5"
                                max="24"
                                step="0.5"
                                className="form-input"
                                value={entry.hours}
                                onChange={(e) => updateEntry(index, 'hours', e.target.value)}
                                disabled={isLocked}
                              />
                            </td>
                            <td>
                              <input
                                type="text"
                                className="form-input"
                                value={entry.taskSummary}
                                onChange={(e) => updateEntry(index, 'taskSummary', e.target.value)}
                                placeholder="What did you work on?"
                                disabled={isLocked}
                              />
                            </td>
                            <td>
                              <div className="timesheet-row-actions">
                                <button type="button" className="btn btn-ghost btn-sm" onClick={() => addRowForDate(entry.workDate)} disabled={isLocked}>
                                  + Row
                                </button>
                                <button type="button" className="btn btn-danger btn-sm" onClick={() => removeRow(index)} disabled={isLocked}>
                                  Remove
                                </button>
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  <div className="timesheet-actions">
                    <button type="button" className="btn btn-ghost" onClick={() => setEntries(createDefaultWeekEntries(weekStart))} disabled={isLocked}>
                      Reset Week
                    </button>
                    <button type="button" className="btn btn-primary" onClick={saveTimesheet} disabled={isLocked || isSaving || isSubmitting}>
                      {isSaving ? 'Saving...' : 'Save Draft'}
                    </button>
                    <button type="button" className="btn btn-success" onClick={submitTimesheet} disabled={isLocked || isSaving || isSubmitting}>
                      {isSubmitting ? 'Submitting...' : 'Submit Week'}
                    </button>
                  </div>
                </>
              )}
            </section>

            <aside className="glass-panel timesheet-side-card">
              <div className="timesheet-side-section">
                <h3 className="card-section-title">How this works</h3>
                <ul className="timesheet-help-list">
                  <li>Pick the week once. Every row belongs to that week.</li>
                  <li>Your employee code comes from login, so you do not need to enter it here.</li>
                  <li>Use one row per project-task block for a given day.</li>
                  <li>Save a draft any time. Submit only when the full week is ready.</li>
                </ul>
              </div>

              <div className="timesheet-side-section">
                <h3 className="card-section-title">Recent Weeks</h3>
                {history.length === 0 ? (
                  <div className="empty-state">
                    <p>No timesheet history yet.</p>
                  </div>
                ) : (
                  <div className="timesheet-history-list">
                    {history.slice(0, 6).map((sheet) => (
                      <button
                        key={`${sheet.weekStartDate}-${sheet.id || 'sheet'}`}
                        type="button"
                        className={`timesheet-history-item${sheet.weekStartDate === weekStart ? ' active' : ''}`}
                        onClick={() => setWeekStart(sheet.weekStartDate)}
                      >
                        <div>
                          <strong>{formatWeekRange(sheet.weekStartDate)}</strong>
                          <span>{sheet.totalHours || 0} hours logged</span>
                        </div>
                        <span className={`status-pill ${sheet.status?.toLowerCase() || 'draft'}`}>
                          {sheet.status || 'DRAFT'}
                        </span>
                      </button>
                    ))}
                  </div>
                )}
              </div>
            </aside>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Timesheets;

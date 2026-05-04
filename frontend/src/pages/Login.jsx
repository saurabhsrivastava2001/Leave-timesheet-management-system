import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error,    setError]    = useState('');
  const [loading,  setLoading]  = useState(false);
  const { login }    = useAuth();
  const navigate     = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); setLoading(true);
    try {
      const ok = await login(username, password);
      if (ok) navigate('/'); else setError('Invalid credentials. Please try again.');
    } catch { setError('Login failed. Check your connection or credentials.'); }
    finally { setLoading(false); }
  };

  return (
    <div style={{
      minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center',
      padding: '24px',
      background: 'radial-gradient(ellipse 80% 60% at 50% -10%, rgba(99,102,241,0.15), transparent)',
    }}>

      {/* Card */}
      <div style={{
        width: '100%', maxWidth: 420,
        background: 'rgba(255,255,255,0.04)',
        backdropFilter: 'blur(24px)',
        border: '1px solid rgba(255,255,255,0.09)',
        borderRadius: 24, padding: '48px 40px',
        boxShadow: '0 24px 80px rgba(0,0,0,0.5)',
      }}>
        {/* Brand */}
        <div style={{ textAlign: 'center', marginBottom: 36 }}>
          <div style={{
            width: 52, height: 52, borderRadius: 14, margin: '0 auto 16px',
            background: 'linear-gradient(135deg, #4f46e5, #a78bfa)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontWeight: 800, fontSize: 18, color: 'white',
            boxShadow: '0 8px 24px rgba(99,102,241,0.4)',
          }}>LT</div>
          <h2 style={{ fontSize: '1.6rem', fontWeight: 700, letterSpacing: '-0.04em', marginBottom: 6, color: '#f1f5f9' }}>Welcome back</h2>
          <p style={{ fontSize: '0.85rem', color: '#64748b' }}>Sign in to your LeaveTime account</p>
        </div>

        {error && (
          <div style={{
            background: 'rgba(251,113,133,0.1)', border: '1px solid rgba(251,113,133,0.25)',
            color: '#fb7185', borderRadius: 10, padding: '10px 16px',
            fontSize: '0.82rem', marginBottom: 20, display: 'flex', alignItems: 'center', gap: 8,
          }}>
            <span>⚠</span> {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: 16 }}>
            <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 500, color: '#94a3b8', marginBottom: 8 }}>
              Employee Code
            </label>
            <input
              type="text" value={username} onChange={e => setUsername(e.target.value)}
              placeholder="e.g. EMP001" required
              style={{
                width: '100%', background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)',
                color: '#f1f5f9', padding: '12px 16px', borderRadius: 10, fontSize: '0.9rem',
                outline: 'none', transition: 'border 0.2s',
              }}
              onFocus={e => e.target.style.borderColor = '#6366f1'}
              onBlur={e => e.target.style.borderColor = 'rgba(255,255,255,0.1)'}
            />
          </div>

          <div style={{ marginBottom: 28 }}>
            <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 500, color: '#94a3b8', marginBottom: 8 }}>
              Password
            </label>
            <input
              type="password" value={password} onChange={e => setPassword(e.target.value)}
              placeholder="••••••••" required
              style={{
                width: '100%', background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)',
                color: '#f1f5f9', padding: '12px 16px', borderRadius: 10, fontSize: '0.9rem',
                outline: 'none',
              }}
              onFocus={e => e.target.style.borderColor = '#6366f1'}
              onBlur={e => e.target.style.borderColor = 'rgba(255,255,255,0.1)'}
            />
          </div>

          <button type="submit" disabled={loading} style={{
            width: '100%', padding: '13px',
            background: 'linear-gradient(135deg, #4f46e5, #6366f1)',
            border: 'none', borderRadius: 12, color: 'white',
            fontWeight: 600, fontSize: '0.95rem', cursor: loading ? 'not-allowed' : 'pointer',
            opacity: loading ? 0.7 : 1,
            transition: 'all 0.2s',
            boxShadow: '0 4px 20px rgba(99,102,241,0.35)',
          }}>
            {loading ? 'Signing in…' : 'Sign In →'}
          </button>
        </form>

        <p style={{ textAlign: 'center', fontSize: '0.83rem', color: '#475569', marginTop: 24 }}>
          No account yet?&nbsp;
          <Link to="/signup" style={{ color: '#818cf8', fontWeight: 600, textDecoration: 'none' }}>Create one</Link>
        </p>
      </div>
    </div>
  );
};

export default Login;

import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Login.css'; // Reusing Login CSS for shared layout styles

const Signup = () => {
  const [formData, setFormData] = useState({
    employeeCode: '',
    name: '',
    email: '',
    password: ''
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  
  const { signup } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setIsLoading(true);

    try {
      await signup(
        formData.employeeCode,
        formData.name,
        formData.email,
        formData.password
      );
      setSuccess('Account created successfully! Redirecting to login...');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      if (err.response?.data) {
        setError(`Signup failed: ${JSON.stringify(err.response.data)}`);
      } else {
        setError('Signup failed. Please check your connection or details.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card glass-panel" style={{ maxWidth: '500px' }}>
        <div className="login-header">
          <div className="brand-logo-large">LM</div>
          <h2>Create Account</h2>
          <p>Register as a new employee</p>
        </div>

        {error && <div className="error-message">{error}</div>}
        {success && <div className="error-message" style={{ background: 'rgba(16, 185, 129, 0.1)', color: '#10b981', borderColor: 'rgba(16, 185, 129, 0.2)' }}>{success}</div>}

        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group">
            <label className="form-label" htmlFor="employeeCode">Employee Code</label>
            <input
              type="text"
              id="employeeCode"
              name="employeeCode"
              className="form-input"
              placeholder="e.g. EMP003"
              value={formData.employeeCode}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="name">Full Name</label>
            <input
              type="text"
              id="name"
              name="name"
              className="form-input"
              placeholder="Jane Doe"
              value={formData.name}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="email">Email</label>
            <input
              type="email"
              id="email"
              name="email"
              className="form-input"
              placeholder="jane@company.com"
              value={formData.email}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="password">Password</label>
            <input
              type="password"
              id="password"
              name="password"
              className="form-input"
              placeholder="••••••••"
              value={formData.password}
              onChange={handleChange}
              required
            />
          </div>

          <button 
            type="submit" 
            className="btn btn-primary login-btn"
            disabled={isLoading}
          >
            {isLoading ? 'Creating Account...' : 'Sign Up'}
          </button>
          
          <div style={{ marginTop: '20px', textAlign: 'center', fontSize: '14px', color: '#94a3b8'}}>
            Already have an account? <Link to="/login" style={{ color: '#4F46E5', fontWeight: '500' }}>Sign In here</Link>
          </div>
        </form>
      </div>

      <div className="background-shapes">
        <div className="shape shape-1" style={{ background: '#ec4899', top: '-150px' }}></div>
        <div className="shape shape-2" style={{ background: '#4F46E5', bottom: '-250px' }}></div>
      </div>
    </div>
  );
};

export default Signup;

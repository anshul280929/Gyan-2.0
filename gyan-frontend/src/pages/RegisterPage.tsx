import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AppHeader } from '../components/AppHeader';
import { useNotifications } from '../components/NotificationProvider';
import { register } from '../lib/api';

const STRONG_PASSWORD_RULE =
  /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,}$/;

export function RegisterPage() {
  const navigate = useNavigate();
  const { notify } = useNotifications();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    setSuccess('');

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    if (!STRONG_PASSWORD_RULE.test(password)) {
      setError('Use at least 8 characters with uppercase, lowercase, number, and special character.');
      return;
    }

    setSubmitting(true);

    try {
      const response = await register(email, password);
      setSuccess(`Account created for ${response.email}. You can log in now.`);
      notify('Account created successfully.', 'success');
      setTimeout(() => navigate('/login'), 800);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to create your account.';
      setError(message);
      notify(message, 'error');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="auth-shell">
      <AppHeader
        links={[
          { label: 'Home', to: '/' },
          { label: 'Login', to: '/login' }
        ]}
        actions={
          <Link className="primary-button" to="/login">
            Login
          </Link>
        }
      />
      <section className="auth-panel">
        <div className="auth-copy">
          <p className="eyebrow">Knowledge Workspace</p>
          <h1>Gyan</h1>
          <p className="lede">
            Create an account to start uploading documents and querying your knowledge base.
          </p>
        </div>

        <form className="auth-card" onSubmit={handleSubmit}>
          <div>
            <p className="card-kicker">Get started</p>
            <h2>Create your account</h2>
          </div>

          <label className="field">
            <span>Email</span>
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="you@example.com"
              required
            />
          </label>

          <label className="field">
            <span>Password</span>
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="Create a password"
              minLength={8}
              required
            />
          </label>

          <p className="empty-state">
            Use at least 8 characters with uppercase, lowercase, number, and special character.
          </p>

          <label className="field">
            <span>Confirm Password</span>
            <input
              type="password"
              value={confirmPassword}
              onChange={(event) => setConfirmPassword(event.target.value)}
              placeholder="Repeat your password"
              required
            />
          </label>

          {error ? <p className="status error">{error}</p> : null}
          {success ? <p className="status success">{success}</p> : null}

          <button className="primary-button" type="submit" disabled={submitting}>
            {submitting ? 'Creating account...' : 'Register'}
          </button>

          <p className="auth-link-row">
            Already have an account? <Link to="/login">Back to login</Link>
          </p>
          <p className="auth-link-row">
            <Link to="/">Back to home</Link>
          </p>
        </form>
      </section>
    </main>
  );
}

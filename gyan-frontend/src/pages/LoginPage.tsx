import { FormEvent, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { AppHeader } from '../components/AppHeader';
import { useNotifications } from '../components/NotificationProvider';
import { login } from '../lib/api';
import { saveTokens } from '../lib/auth';

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { notify } = useNotifications();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const redirectTo = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname ?? '/dashboard';

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError('');

    try {
      const response = await login(email, password);
      saveTokens(response.accessToken);
      notify('Logged in successfully.', 'success');
      navigate(redirectTo, { replace: true });
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to sign in right now.';
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
          { label: 'Register', to: '/register' }
        ]}
        actions={
          <Link className="primary-button" to="/register">
            Create account
          </Link>
        }
      />
      <section className="auth-panel">
        <div className="auth-copy">
          <p className="eyebrow">Knowledge Workspace</p>
          <h1>Gyan</h1>
          <p className="lede">
            Sign in to upload files, search across your knowledge base, and ask grounded questions
            against your documents.
          </p>
        </div>

        <form className="auth-card" onSubmit={handleSubmit}>
          <div>
            <p className="card-kicker">Welcome back</p>
            <h2>Log into your workspace</h2>
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
              placeholder="Enter your password"
              required
            />
          </label>

          {error ? <p className="status error">{error}</p> : null}

          <button className="primary-button" type="submit" disabled={submitting}>
            {submitting ? 'Signing in...' : 'Login'}
          </button>

          <p className="auth-link-row">
            New here? <Link to="/register">Create an account</Link>
          </p>
          <p className="auth-link-row">
            <Link to="/">Back to home</Link>
          </p>
        </form>
      </section>
    </main>
  );
}

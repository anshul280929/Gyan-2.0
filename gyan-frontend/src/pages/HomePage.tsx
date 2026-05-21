import { Link } from 'react-router-dom';
import { AppHeader } from '../components/AppHeader';
import { getAccessToken } from '../lib/auth';

const highlights = [
  {
    title: 'Organize work by chat',
    description: 'Create separate workspaces for invoices, policies, research, or any focused thread of work.'
  },
  {
    title: 'Upload and ground answers',
    description: 'Attach documents directly to a chat so every answer stays tied to the right source material.'
  },
  {
    title: 'Keep conversations persistent',
    description: 'Return to earlier discussions, review message history, and continue exactly where you left off.'
  }
];

const steps = [
  'Create a workspace for a topic or project.',
  'Upload PDFs, DOCX, spreadsheets, or text files into that chat.',
  'Ask questions and get answers grounded in the uploaded documents.'
];

export function HomePage() {
  const isAuthenticated = Boolean(getAccessToken());

  return (
    <main className="home-shell">
      <AppHeader
        links={
          isAuthenticated
            ? [
                { label: 'Home', to: '/' },
                { label: 'Workspace', to: '/dashboard' }
              ]
            : [
                { label: 'Features', to: '/#features' },
                { label: 'Workflow', to: '/#workflow' },
                { label: 'Login', to: '/login' }
              ]
        }
        actions={
          isAuthenticated ? (
            <Link className="primary-button" to="/dashboard">
              Open workspace
            </Link>
          ) : (
            <>
              <Link className="ghost-button" to="/login">
                Login
              </Link>
              <Link className="primary-button" to="/register">
                Get started
              </Link>
            </>
          )
        }
      />

      <section className="home-hero">
        <div className="home-hero-copy">
          <p className="eyebrow">Document Intelligence, Reframed</p>
          <h1 className="home-title">Turn every document set into a focused AI workspace.</h1>
          <p className="lede home-lede">
            Gyan helps you upload, organize, and question your files inside dedicated chat workspaces so the
            answers stay grounded, searchable, and easy to revisit.
          </p>

          <div className="home-actions">
            {isAuthenticated ? (
              <>
                <Link className="primary-button" to="/dashboard">
                  Open workspace
                </Link>
                <a className="ghost-button" href="#features">
                  Explore features
                </a>
              </>
            ) : (
              <>
                <Link className="primary-button" to="/login">
                  Login
                </Link>
                <Link className="ghost-button" to="/register">
                  Create account
                </Link>
              </>
            )}
          </div>

          <div className="home-stats">
            <article className="home-stat">
              <strong>5</strong>
              <span>separate chats per user</span>
            </article>
            <article className="home-stat">
              <strong>1</strong>
              <span>document context per workspace</span>
            </article>
            <article className="home-stat">
              <strong>∞</strong>
              <span>faster follow-up questions</span>
            </article>
          </div>
        </div>

        <div className="home-showcase">
          <div className="showcase-panel showcase-primary">
            <p className="card-kicker">Workspace flow</p>
            <h2>From upload to answer, without losing context.</h2>
            <div className="showcase-stack">
              <div className="showcase-chip">Invoice review</div>
              <div className="showcase-chip">Policy analysis</div>
              <div className="showcase-chip">Research notes</div>
            </div>
          </div>

          <div className="showcase-grid">
            <article className="showcase-card">
              <p className="card-kicker">Upload</p>
              <h3>Attach files to the right chat</h3>
              <p>Keep documents scoped so answers don’t drift across unrelated work.</p>
            </article>
            <article className="showcase-card">
              <p className="card-kicker">Ask</p>
              <h3>Get grounded responses</h3>
              <p>Use the chat page to ask focused questions against only that workspace’s documents.</p>
            </article>
            <article className="showcase-card">
              <p className="card-kicker">Track</p>
              <h3>See document readiness</h3>
              <p>Follow upload and processing states so you know when files are ready for questioning.</p>
            </article>
            <article className="showcase-card">
              <p className="card-kicker">Revisit</p>
              <h3>Keep message history</h3>
              <p>Return to past answers and continue the conversation without starting from scratch.</p>
            </article>
          </div>
        </div>
      </section>

      <section className="home-section" id="features">
        <div className="section-heading">
          <p className="card-kicker">Why Gyan</p>
          <h2>Built for focused document conversations</h2>
        </div>

        <div className="home-feature-grid">
          {highlights.map((item) => (
            <article className="feature-card" key={item.title}>
              <h3>{item.title}</h3>
              <p>{item.description}</p>
            </article>
          ))}
        </div>
      </section>

      <section className="home-section home-section-split" id="workflow">
        <article className="journey-card">
          <p className="card-kicker">How it works</p>
          <h2>Simple workflow, cleaner answers.</h2>
          <ol className="journey-list">
            {steps.map((step) => (
              <li key={step}>{step}</li>
            ))}
          </ol>
        </article>

        <article className="cta-card">
          <p className="card-kicker">Start now</p>
          <h2>Bring structure to your document chats.</h2>
          <p>
            Sign in if you already have an account, or create one and start building workspaces for the files you
            use every day.
          </p>
          <div className="home-actions">
            {isAuthenticated ? (
              <Link className="primary-button" to="/dashboard">
                Return to workspace
              </Link>
            ) : (
              <>
                <Link className="primary-button" to="/register">
                  Get started
                </Link>
                <Link className="ghost-button" to="/login">
                  I already have an account
                </Link>
              </>
            )}
          </div>
        </article>
      </section>
    </main>
  );
}

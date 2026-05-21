import { ReactNode, useMemo, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';

type HeaderLink = {
  label: string;
  to: string;
};

type AppHeaderProps = {
  links?: HeaderLink[];
  actions?: ReactNode;
  currentLabel?: string;
};

function getBaseLabel(pathname: string) {
  if (pathname === '/') {
    return 'Home';
  }

  if (pathname.startsWith('/login')) {
    return 'Login';
  }

  if (pathname.startsWith('/register')) {
    return 'Register';
  }

  if (pathname.startsWith('/dashboard')) {
    return 'Workspace';
  }

  if (pathname.startsWith('/chat/')) {
    return 'Chat';
  }

  return 'Page';
}

export function AppHeader({ links = [], actions, currentLabel }: AppHeaderProps) {
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);

  const crumbs = useMemo(() => {
    const baseLabel = getBaseLabel(location.pathname);

    if (location.pathname.startsWith('/chat/')) {
      return ['Workspace', currentLabel || 'Chat'];
    }

    if (baseLabel === 'Home') {
      return ['Home'];
    }

    return ['Home', baseLabel];
  }, [currentLabel, location.pathname]);

  return (
    <header className="site-header">
      <Link className="brand-mark" to="/">
        <span className="brand-badge">G</span>
        <span className="brand-copy">
          <strong>Gyan</strong>
          <span>AI workspaces for documents</span>
        </span>
      </Link>

      <nav className="site-nav" aria-label="Primary">
        {links.map((link) => (
          <Link key={link.to} to={link.to}>
            {link.label}
          </Link>
        ))}
      </nav>

      <div className="site-header-actions">{actions}</div>

      <div className="mobile-header-bar">
        <div className="mobile-breadcrumbs" aria-label="Breadcrumb">
          {crumbs.map((crumb, index) => (
            <span key={`${crumb}-${index}`} className={index === crumbs.length - 1 ? 'mobile-crumb-current' : ''}>
              {index > 0 ? <span className="mobile-crumb-separator">/</span> : null}
              {crumb}
            </span>
          ))}
        </div>

        <button
          className="ghost-button mobile-menu-toggle"
          type="button"
          onClick={() => setMenuOpen((open) => !open)}
          aria-expanded={menuOpen}
          aria-controls="mobile-nav-sheet"
        >
          {menuOpen ? 'Close' : 'Menu'}
        </button>
      </div>

      {menuOpen ? (
        <div className="mobile-nav-sheet" id="mobile-nav-sheet">
          <nav className="mobile-nav-links" aria-label="Mobile primary">
            {links.map((link) => (
              <Link key={`mobile-${link.to}`} to={link.to} onClick={() => setMenuOpen(false)}>
                {link.label}
              </Link>
            ))}
          </nav>
          {actions ? (
            <div className="mobile-nav-actions" onClick={() => setMenuOpen(false)}>
              {actions}
            </div>
          ) : null}
        </div>
      ) : null}
    </header>
  );
}

import { Link, NavLink, Outlet, useLocation } from 'react-router'

import logoUrl from '../assets/dk-logo.svg'

export function AppLayout() {
  const { pathname } = useLocation()
  // "Services" covers the list and every service's detail page, but not the register form,
  // which has its own nav item.
  const inServices = pathname.startsWith('/services') && pathname !== '/services/new'

  return (
    <div className="shell">
      <header className="sidebar">
        <Link to="/" className="brand">
          DK8s
        </Link>
        <nav aria-label="Main" className="nav">
          <NavLink to="/" end className="nav__link">
            Dashboard
          </NavLink>
          <Link
            to="/services"
            className={inServices ? 'nav__link active' : 'nav__link'}
            aria-current={inServices ? 'page' : undefined}
          >
            Services
          </Link>
          <NavLink to="/services/new" className="nav__link">
            Register service
          </NavLink>
        </nav>
        <div className="sidebar__footer">
          <p className="version">v{__APP_VERSION__}</p>
          <img src={logoUrl} alt="DK, Daniel Khojaste" title="Built by Daniel Khojaste" className="sidebar__logo" />
        </div>
      </header>
      <main className="content">
        <Outlet />
      </main>
    </div>
  )
}

import { Link } from 'react-router'

export function NotFoundPage() {
  return (
    <>
      <h1>Page not found</h1>
      <div className="notice">
        <p>This page doesn't exist in DK8s.</p>
        <Link to="/">Go to the dashboard</Link>
      </div>
    </>
  )
}

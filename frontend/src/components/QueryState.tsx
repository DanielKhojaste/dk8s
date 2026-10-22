import type { ReactNode } from 'react'

import { ApiError } from '../api/client'

export function Loading({ label }: { label: string }) {
  return (
    <p className="notice" role="status">
      {label}
    </p>
  )
}

/** Explains a failed request and how to fix it. */
export function LoadError({ error, what }: { error: unknown; what: string }) {
  let hint = 'Try again in a moment.'
  if (error instanceof ApiError && (error.status === 0 || error.status >= 500)) {
    hint = 'Check that control-api is running on port 8080 and PostgreSQL is up (docker compose up -d).'
  }

  return (
    <div className="notice notice--error" role="alert">
      <p className="notice__title">Couldn't load {what}.</p>
      <p>{hint}</p>
    </div>
  )
}

export function Empty({ children }: { children: ReactNode }) {
  return <div className="notice">{children}</div>
}

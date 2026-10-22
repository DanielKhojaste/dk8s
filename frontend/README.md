# DK8s frontend

The DK8s operations console: React, TypeScript, Vite, React Router, and TanStack Query.

## Prerequisites

- Node.js 22.22 or newer
- `control-api` running on `http://localhost:8080` (see the root README)

## Commands

Run from this `frontend/` folder (Git Bash):

```bash
npm install          # first time only
npm run dev          # start the dev server on http://localhost:5173
npm test             # run the tests once
npm run typecheck    # TypeScript check
npm run build        # type-check and build to dist/
```

## How it talks to the API

The browser only ever calls relative URLs such as `/api/services`. In development, the Vite dev server forwards every `/api` request to `http://localhost:8080` (see `server.proxy` in `vite.config.ts`). Because the browser sees a single origin, the backend needs no CORS configuration.

## Pages

| Route | Page |
|---|---|
| `/` | Dashboard: status breakdown and recently registered services |
| `/services` | All registered services |
| `/services/new` | Register a service |
| `/services/:id` | Service details |

## Structure

```text
src/
  main.tsx        Entry point: QueryClient + router
  router/         Route definitions
  pages/          One component per route (+ tests)
  components/     Shared UI: layout, status badge, table, loading/error states
  api/            fetch wrapper, API functions, TanStack Query hooks
  types/          TypeScript types matching the control-api JSON
  test/           Test setup and helpers
```

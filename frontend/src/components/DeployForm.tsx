import { useState, type FormEvent } from 'react'

import { ApiError } from '../api/client'
import { useCreateDeployment } from '../api/deployments'

/** Small inline form: type a version, click Deploy. */
export function DeployForm({ serviceId, currentVersion }: { serviceId: number; currentVersion: string }) {
  const createDeployment = useCreateDeployment(serviceId)
  const [version, setVersion] = useState('')
  const [error, setError] = useState<string | null>(null)

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)

    createDeployment.mutate(
      { version: version.trim() },
      {
        onSuccess: () => setVersion(''),
        onError: (err) => {
          if (err instanceof ApiError && err.status === 400) {
            setError(err.problem?.errors?.version ?? 'Enter a version to deploy.')
          } else if (err instanceof ApiError && err.status === 409) {
            setError('A deployment of this service is already in progress. Wait for it to finish, then try again.')
          } else if (err instanceof ApiError && err.status === 503 && err.problem?.detail) {
            setError(err.problem.detail)
          } else {
            setError("The deployment wasn't requested because the DK8s API couldn't be reached.")
          }
        },
      },
    )
  }

  return (
    <form className="deploy-form" onSubmit={handleSubmit} noValidate>
      <label htmlFor="deploy-version">Version to deploy</label>
      <p className="field__hint" id="deploy-version-hint">
        Currently running <span className="code">{currentVersion}</span>. Deployments are simulated by the DK8s
        worker; a version containing “fail” (e.g. <span className="code">2.0.0-fail</span>) fails on purpose.
      </p>
      <div className="deploy-form__row">
        <input
          id="deploy-version"
          className="code"
          value={version}
          onChange={(event) => setVersion(event.target.value)}
          placeholder="e.g. 1.5.0"
          maxLength={50}
          autoComplete="off"
          spellCheck={false}
          aria-describedby={error ? 'deploy-version-hint deploy-version-error' : 'deploy-version-hint'}
          aria-invalid={Boolean(error)}
        />
        <button type="submit" className="button" disabled={createDeployment.isPending}>
          {createDeployment.isPending ? 'Deploying…' : 'Deploy'}
        </button>
      </div>
      {error && (
        <p className="field__error" id="deploy-version-error" role="alert">
          {error}
        </p>
      )}
    </form>
  )
}

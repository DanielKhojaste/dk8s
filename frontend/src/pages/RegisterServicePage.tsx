import { useState, type FormEvent, type ReactNode } from 'react'
import { Link, useNavigate } from 'react-router'

import { ApiError } from '../api/client'
import { useCreateService } from '../api/managedServices'

type FieldName = 'name' | 'description' | 'version'
type FieldErrors = Partial<Record<FieldName, string>>

export function RegisterServicePage() {
  const navigate = useNavigate()
  const createService = useCreateService()

  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [version, setVersion] = useState('')
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
  const [formError, setFormError] = useState<string | null>(null)

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setFieldErrors({})
    setFormError(null)

    createService.mutate(
      {
        name: name.trim(),
        version: version.trim(),
        description: description.trim() || undefined,
      },
      {
        onSuccess: (created) => navigate(`/services/${created.id}`),
        onError: (error) => {
          // The backend is the source of truth for validation; show its messages next to each field.
          if (error instanceof ApiError && error.status === 400 && error.problem?.errors) {
            setFieldErrors(error.problem.errors)
          } else if (error instanceof ApiError && error.status === 409) {
            setFieldErrors({ name: `A service named “${name.trim()}” is already registered.` })
          } else if (error instanceof ApiError && error.status === 503 && error.problem?.detail) {
            setFormError(`The service wasn't registered. ${error.problem.detail}`)
          } else if (error instanceof ApiError && error.status > 0 && error.status < 500) {
            setFormError(`The service wasn't registered: ${error.message}`)
          } else {
            setFormError(
              "The service wasn't registered because the DK8s API couldn't be reached. Check that control-api is running, then try again.",
            )
          }
        },
      },
    )
  }

  return (
    <>
      <p className="breadcrumb">
        <Link to="/services">Services</Link>
      </p>
      <h1>Register service</h1>
      <p className="lead">
        Add a service to DK8s so its version and health can be tracked. New services start with status Unknown.
      </p>

      <form className="form" onSubmit={handleSubmit} noValidate>
        {formError && (
          <div className="notice notice--error" role="alert">
            {formError}
          </div>
        )}

        <Field
          id="name"
          label="Name"
          hint="Lowercase letters, digits, and hyphens, up to 63 characters. For example: payments-api"
          error={fieldErrors.name}
        >
          <input
            id="name"
            className="code"
            value={name}
            onChange={(event) => setName(event.target.value)}
            maxLength={63}
            aria-describedby={describedBy('name', fieldErrors)}
            aria-invalid={Boolean(fieldErrors.name)}
            autoComplete="off"
            spellCheck={false}
            required
          />
        </Field>

        <Field id="version" label="Version" hint="The version currently deployed. For example: 1.4.2" error={fieldErrors.version}>
          <input
            id="version"
            className="code"
            value={version}
            onChange={(event) => setVersion(event.target.value)}
            maxLength={50}
            aria-describedby={describedBy('version', fieldErrors)}
            aria-invalid={Boolean(fieldErrors.version)}
            autoComplete="off"
            spellCheck={false}
            required
          />
        </Field>

        <Field id="description" label="Description" hint="Optional. What the service does." error={fieldErrors.description}>
          <textarea
            id="description"
            value={description}
            onChange={(event) => setDescription(event.target.value)}
            maxLength={500}
            aria-describedby={describedBy('description', fieldErrors)}
            aria-invalid={Boolean(fieldErrors.description)}
            rows={3}
          />
        </Field>

        <div className="form__actions">
          <button type="submit" className="button" disabled={createService.isPending}>
            {createService.isPending ? 'Registering…' : 'Register service'}
          </button>
          <Link to="/services">Cancel</Link>
        </div>
      </form>
    </>
  )
}

interface FieldProps {
  id: FieldName
  label: string
  hint: string
  error?: string
  children: ReactNode
}

/** Links a control to its hint and (when present) its error message for screen readers. */
function describedBy(id: FieldName, errors: FieldErrors): string {
  return errors[id] ? `${id}-hint ${id}-error` : `${id}-hint`
}

/** Label, hint, and error message for one form control. */
function Field({ id, label, hint, error, children }: FieldProps) {
  return (
    <div className={error ? 'field field--invalid' : 'field'}>
      <label htmlFor={id}>{label}</label>
      <p className="field__hint" id={`${id}-hint`}>
        {hint}
      </p>
      {children}
      {error && (
        <p className="field__error" id={`${id}-error`} role="alert">
          {error}
        </p>
      )}
    </div>
  )
}

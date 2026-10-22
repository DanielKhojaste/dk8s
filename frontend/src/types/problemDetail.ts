/** Error body returned by control-api (RFC 9457 problem details). */
export interface ProblemDetail {
  status: number
  title?: string
  detail?: string
  instance?: string
  /** Present on 400 validation errors: field name -> message. */
  errors?: Record<string, string>
}

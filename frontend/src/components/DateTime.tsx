const formatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: 'medium',
  timeStyle: 'short',
})

/** Renders an ISO timestamp in the viewer's locale and time zone. */
export function DateTime({ value }: { value: string }) {
  return (
    <time dateTime={value} title={value}>
      {formatter.format(new Date(value))}
    </time>
  )
}

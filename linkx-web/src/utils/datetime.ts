const BACKEND_LOCAL_DATE_TIME_PATTERN = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}(:\d{2})?$/

function normalizeDateTimeInput(value: string) {
  const trimmed = value.trim()
  if (BACKEND_LOCAL_DATE_TIME_PATTERN.test(trimmed) && !trimmed.includes('T')) {
    return trimmed.replace(' ', 'T')
  }
  return trimmed
}

export function parseDateTime(value?: string | null) {
  if (!value) {
    return null
  }
  const normalized = normalizeDateTimeInput(value)
  const date = new Date(normalized)
  if (Number.isNaN(date.getTime())) {
    return null
  }
  return date
}

export function getDateTimeTimestamp(value?: string | null) {
  return parseDateTime(value)?.getTime() ?? 0
}

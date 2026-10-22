import { useEffect, useState } from 'react'

/** The current time, updated every second while `ticking` is true (for "waiting 12s" style labels). */
export function useNow(ticking: boolean): number {
  const [now, setNow] = useState(() => Date.now())

  useEffect(() => {
    if (!ticking) return
    const timer = setInterval(() => setNow(Date.now()), 1000)
    return () => clearInterval(timer) // stop the timer when ticking ends or the component unmounts
  }, [ticking])

  return now
}

/** 9 → "9s", 100 → "1m 40s" */
export function formatElapsed(seconds: number): string {
  const s = Math.max(0, Math.floor(seconds))
  return s < 60 ? `${s}s` : `${Math.floor(s / 60)}m ${s % 60}s`
}

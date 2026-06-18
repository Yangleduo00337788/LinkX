import { ref, watch } from 'vue'

export type ThemeMode = 'light' | 'dark' | 'system'

const THEME_KEY = 'linkx-theme'
const themeMode = ref<ThemeMode>((localStorage.getItem(THEME_KEY) as ThemeMode) || 'light')
const mediaQuery = typeof window !== 'undefined' && window.matchMedia
  ? window.matchMedia('(prefers-color-scheme: dark)')
  : null
let initialized = false

function getSystemTheme(): 'light' | 'dark' {
  if (mediaQuery) {
    return mediaQuery.matches ? 'dark' : 'light'
  }
  return 'light'
}

function applyTheme(mode: ThemeMode) {
  const actual = mode === 'system' ? getSystemTheme() : mode
  document.documentElement.setAttribute('data-theme', actual)
}

export function useTheme() {
  if (!initialized) {
    initialized = true
    applyTheme(themeMode.value)

    watch(themeMode, (val) => {
      localStorage.setItem(THEME_KEY, val)
      applyTheme(val)
    })

    mediaQuery?.addEventListener('change', () => {
      if (themeMode.value === 'system') {
        applyTheme(themeMode.value)
      }
    })
  }

  function setMode(val: ThemeMode) {
    themeMode.value = val
  }

  return { mode: themeMode, setMode }
}

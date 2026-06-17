import { ref, watch } from 'vue'

export type ThemeMode = 'light' | 'dark' | 'system'

const THEME_KEY = 'linkx-theme'

function getSystemTheme(): 'light' | 'dark' {
  if (typeof window !== 'undefined' && window.matchMedia) {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
  }
  return 'light'
}

function applyTheme(mode: ThemeMode) {
  const actual = mode === 'system' ? getSystemTheme() : mode
  document.documentElement.setAttribute('data-theme', actual)
}

export function useTheme() {
  const mode = ref<ThemeMode>((localStorage.getItem(THEME_KEY) as ThemeMode) || 'light')

  applyTheme(mode.value)

  watch(mode, (val) => {
    localStorage.setItem(THEME_KEY, val)
    applyTheme(val)
  })

  if (mode.value === 'system' && typeof window !== 'undefined' && window.matchMedia) {
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
      applyTheme(mode.value)
    })
  }

  function setMode(val: ThemeMode) {
    mode.value = val
  }

  return { mode, setMode }
}

import { ref } from 'vue'

type Theme = 'light' | 'dark'

const stored = localStorage.getItem('dp-theme') as Theme | null
const theme = ref<Theme>(stored ?? 'light')

function applyTheme(t: Theme) {
  document.documentElement.setAttribute('data-theme', t)
  localStorage.setItem('dp-theme', t)
}

applyTheme(theme.value)

export function useTheme() {
  return {
    theme,
    toggle() {
      theme.value = theme.value === 'light' ? 'dark' : 'light'
      applyTheme(theme.value)
    },
  }
}

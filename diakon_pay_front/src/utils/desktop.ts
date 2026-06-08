import { ref, onMounted, onUnmounted } from 'vue'

export function useDesktop(breakpoint = 768) {
  const isDesktop = ref(typeof window !== 'undefined' && window.innerWidth >= breakpoint)

  function onResize() {
    isDesktop.value = window.innerWidth >= breakpoint
  }

  onMounted(() => window.addEventListener('resize', onResize))
  onUnmounted(() => window.removeEventListener('resize', onResize))

  return { isDesktop }
}

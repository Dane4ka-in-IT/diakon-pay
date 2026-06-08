<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function onSubmit() {
  error.value = ''
  if (!email.value || !password.value) {
    error.value = 'Введите email и пароль'
    return
  }
  loading.value = true
  try {
    await auth.login({ email: email.value, password: password.value })
    router.push('/transactions')
  } catch (e: unknown) {
    const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message
    error.value = msg || 'Неверный email или пароль'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-wrap">
    <main class="auth-main">
      <div class="intro">
        <div class="intro-icon">💙</div>
        <p class="intro-tagline">DiakonPay — это твой личный финансовый помощник.</p>
        <ul class="intro-list">
          <li>— Записывай траты в два клика.</li>
          <li>— Получай аналитику за любой период.</li>
          <li>— Работает офлайн, синхронизируется автоматически.</li>
        </ul>
        <p class="intro-cta">Давай добавим твою первую транзакцию!</p>
      </div>

      <div class="form-card">
        <h2 class="form-title">Вход</h2>

        <form @submit.prevent="onSubmit">
          <div class="form-group">
            <input
              v-model="email"
              type="email"
              class="form-input"
              placeholder="E-mail"
              autocomplete="email"
            />
          </div>
          <div class="form-group">
            <input
              v-model="password"
              type="password"
              class="form-input"
              placeholder="Пароль"
              autocomplete="current-password"
            />
          </div>

          <div v-if="error" class="error-msg" style="margin-bottom: 14px">{{ error }}</div>

          <button type="submit" class="btn btn-primary" :disabled="loading">
            {{ loading ? 'Вход...' : '→ Войти' }}
          </button>
        </form>

        <div class="form-links">
          <RouterLink to="/register" class="form-link">Нет аккаунта? Регистрация</RouterLink>
        </div>
      </div>
    </main>

    <footer class="auth-footer">© IT_diakon 2026</footer>
  </div>
</template>

<style scoped>
.auth-wrap {
  display: flex;
  flex-direction: column;
  min-height: calc(100dvh - 54px);
}

.auth-main {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  align-items: center;
  gap: 40px;
  padding: 48px 80px;
  max-width: 1100px;
  margin: 0 auto;
  width: 100%;
}

/* Intro panel */
.intro {
  display: flex;
  flex-direction: column;
  color: var(--heading);
}
.intro-icon { font-size: 72px; margin-bottom: 24px; }
.intro-tagline {
  font-size: 16px;
  font-weight: 600;
  line-height: 1.55;
  margin-bottom: 16px;
  color: var(--heading);
}
.intro-list {
  list-style: none;
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.intro-list li {
  font-size: 15px;
  font-weight: 600;
  line-height: 1.6;
  color: var(--heading);
}
.intro-cta {
  font-size: 15px;
  font-weight: 600;
  color: var(--heading);
}

/* Form card */
.form-card {
  background: var(--surface);
  border-radius: var(--radius);
  border: 1px solid var(--border);
  box-shadow: var(--card-shadow);
  padding: 36px 40px;
  width: 100%;
  max-width: 420px;
  justify-self: center;
}
.form-title {
  color: var(--heading);
  text-align: center;
  margin-bottom: 24px;
  font-size: 24px;
  font-weight: 700;
}
.form-links {
  text-align: center;
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.form-link {
  color: var(--primary);
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
}
.form-link:hover { text-decoration: underline; }

/* Footer */
.auth-footer {
  background:
    linear-gradient(180deg, transparent 0%, transparent 30%, rgba(34,34,34,0.65) 30%, rgba(34,34,34,0.65) 100%),
    linear-gradient(90deg, #0E40C7 0%, #0E45D5 22%, #0C76F7 38%, #0C8DFD 51%, #0C76F7 66%, #0C76F7 79%, #0E4AE3 100%);
  color: #fff;
  text-align: center;
  padding: 14px;
  font-size: 13px;
}

/* Mobile: single column */
@media (max-width: 700px) {
  .auth-main {
    grid-template-columns: 1fr;
    padding: 24px 16px;
    gap: 0;
  }
  .intro { display: none; }
  .form-card {
    padding: 28px 24px;
    max-width: 100%;
    justify-self: stretch;
    box-shadow: none;
    border: none;
    background: transparent;
  }
  .form-title { font-size: 28px; margin-bottom: 28px; }
}
</style>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const name = ref('')
const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function onSubmit() {
  error.value = ''
  if (!name.value.trim() || !email.value || !password.value) {
    error.value = 'Заполните все поля'
    return
  }
  if (password.value.length < 6) {
    error.value = 'Пароль должен быть не менее 6 символов'
    return
  }
  loading.value = true
  try {
    await auth.register({
      name: name.value.trim(),
      userEmail: email.value,
      userPassword: password.value,
    })
    await auth.login({ email: email.value, password: password.value })
    router.push('/transactions')
  } catch (e: unknown) {
    const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message
    error.value = msg || 'Ошибка регистрации. Попробуйте другой email.'
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
        <p class="intro-tagline">Привет! Мы создали кошелёк, который работает по твоим правилам.</p>
        <p class="intro-body">Контролируй доходы, отслеживай расходы и управляй счетами легко и быстро.</p>
        <p class="intro-body">А главное: DiakonPay сохранит твои транзакции даже без интернета и автоматически синхронизирует их при появлении сети.</p>
      </div>

      <div class="form-card">
        <h2 class="form-title">Регистрация</h2>

        <form @submit.prevent="onSubmit">
          <div class="form-group">
            <input
              v-model="name"
              type="text"
              class="form-input"
              placeholder="Nickname"
              autocomplete="name"
            />
          </div>
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
              placeholder="Пароль (не менее 6 символов)"
              autocomplete="new-password"
            />
          </div>

          <div v-if="error" class="error-msg" style="margin-bottom: 14px">{{ error }}</div>

          <button type="submit" class="btn btn-primary" :disabled="loading">
            {{ loading ? 'Создание...' : '→ Создать аккаунт' }}
          </button>
        </form>

        <div class="form-links">
          <RouterLink to="/login" class="form-link">Уже есть аккаунт? Войти</RouterLink>
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
  margin-bottom: 14px;
  color: var(--heading);
}
.intro-body {
  font-size: 15px;
  font-weight: 600;
  line-height: 1.6;
  margin-bottom: 12px;
  color: var(--heading);
}

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
}
.form-link {
  color: var(--primary);
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
}
.form-link:hover { text-decoration: underline; }

.auth-footer {
  background:
    linear-gradient(180deg, transparent 0%, transparent 30%, rgba(34,34,34,0.65) 30%, rgba(34,34,34,0.65) 100%),
    linear-gradient(90deg, #0E40C7 0%, #0E45D5 22%, #0C76F7 38%, #0C8DFD 51%, #0C76F7 66%, #0C76F7 79%, #0E4AE3 100%);
  color: #fff;
  text-align: center;
  padding: 14px;
  font-size: 13px;
}

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

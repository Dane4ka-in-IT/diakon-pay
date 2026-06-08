<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAccountsStore } from '@/stores/accounts'
import { useCategoriesStore } from '@/stores/categories'
import { usersApi } from '@/api/users'
import { authApi } from '@/api/auth'
import AccountModal from '@/components/AccountModal.vue'
import CategoryModal from '@/components/CategoryModal.vue'
import type { AccountResponse, AccountRequest } from '@/types/account'
import type { CategoryResponse, CategoryRequest } from '@/types/category'
import { formatCurrency } from '@/utils/format'

const router = useRouter()
const auth = useAuthStore()
const accStore = useAccountsStore()
const catStore = useCategoriesStore()

onMounted(async () => {
  await Promise.all([accStore.fetchAccounts(), catStore.fetchCategories()])
})

// ─── Profile Edit ─────────────────────────────────────────────
const editOpen = ref(false)
const editName = ref('')
const editAvatar = ref('')
const editError = ref('')
const editLoading = ref(false)

function openEdit() {
  editName.value = auth.userName ?? ''
  editAvatar.value = auth.avatarUrl ?? ''
  editError.value = ''
  editOpen.value = true
}

async function submitEdit() {
  editError.value = ''
  if (!editName.value.trim()) { editError.value = 'Введите имя'; return }
  editLoading.value = true
  try {
    const { data } = await usersApi.updateProfile({
      name: editName.value.trim(),
      avatarUrl: editAvatar.value.trim() || undefined,
    })
    auth.patchProfile(data.user.name, data.user.avatarUrl)
    editOpen.value = false
  } catch {
    editError.value = 'Не удалось обновить профиль'
  } finally {
    editLoading.value = false
  }
}

// ─── Accounts ─────────────────────────────────────────────────
const accModalOpen = ref(false)
const editingAcc = ref<AccountResponse | null>(null)
const accError = ref('')

async function onAccSubmit(data: AccountRequest) {
  accError.value = ''
  try {
    if (editingAcc.value) {
      await accStore.updateAccount(editingAcc.value.id, data)
    } else {
      await accStore.createAccount(data)
    }
    accModalOpen.value = false
    editingAcc.value = null
  } catch (e: unknown) {
    const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message
    accError.value = msg || 'Ошибка при сохранении счёта'
  }
}

const deletingAccId = ref<string | null>(null)
async function doDeleteAcc() {
  if (!deletingAccId.value) return
  try {
    await accStore.deleteAccount(deletingAccId.value)
  } finally {
    deletingAccId.value = null
  }
}

// ─── Categories ───────────────────────────────────────────────
const catModalOpen = ref(false)
const editingCat = ref<CategoryResponse | null>(null)
const catError = ref('')

const userCategories = computed(() => catStore.categories.filter((c) => c.userId !== 1))
const systemCategories = computed(() => catStore.categories.filter((c) => c.userId === 1))

async function onCatSubmit(data: CategoryRequest) {
  catError.value = ''
  try {
    if (editingCat.value) {
      await catStore.updateCategory(editingCat.value.id, data)
    } else {
      await catStore.createCategory(data)
    }
    catModalOpen.value = false
    editingCat.value = null
  } catch (e: unknown) {
    const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message
    catError.value = msg || 'Ошибка при сохранении категории'
  }
}

const deletingCatId = ref<string | null>(null)
async function doDeleteCat() {
  if (!deletingCatId.value) return
  try {
    await catStore.deleteCategory(deletingCatId.value)
  } finally {
    deletingCatId.value = null
  }
}

// ─── Email Change ──────────────────────────────────────────────
const emailStep = ref<0 | 1 | 2>(0)
const newEmail = ref('')
const emailCode = ref('')
const emailError = ref('')
const emailLoading = ref(false)

async function requestEmailChange() {
  emailError.value = ''
  if (!newEmail.value) { emailError.value = 'Введите новый email'; return }
  emailLoading.value = true
  try {
    await usersApi.requestEmailChange(newEmail.value)
    emailStep.value = 2
  } catch (e: unknown) {
    const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message
    emailError.value = msg || 'Ошибка отправки кода'
  } finally {
    emailLoading.value = false
  }
}

async function confirmEmailChange() {
  emailError.value = ''
  if (!emailCode.value) { emailError.value = 'Введите код'; return }
  emailLoading.value = true
  try {
    await usersApi.confirmEmailChange(newEmail.value, Number(emailCode.value))
    auth.patchEmail(newEmail.value)
    emailStep.value = 0
    newEmail.value = ''
    emailCode.value = ''
  } catch (e: unknown) {
    const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message
    emailError.value = msg || 'Неверный или просроченный код'
  } finally {
    emailLoading.value = false
  }
}

// ─── Password Recovery ────────────────────────────────────────
const pwdStep = ref<0 | 1 | 2>(0)
const pwdCode = ref('')
const pwdNew = ref('')
const pwdError = ref('')
const pwdLoading = ref(false)

async function requestPwdRecovery() {
  if (!auth.userEmail) return
  pwdError.value = ''
  pwdLoading.value = true
  try {
    await authApi.requestPasswordRecovery(auth.userEmail)
    pwdStep.value = 2
  } catch (e: unknown) {
    const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message
    pwdError.value = msg || 'Ошибка отправки кода'
  } finally {
    pwdLoading.value = false
  }
}

async function confirmPwdRecovery() {
  pwdError.value = ''
  if (!pwdCode.value || !pwdNew.value) { pwdError.value = 'Заполните все поля'; return }
  if (pwdNew.value.length < 6) { pwdError.value = 'Пароль не менее 6 символов'; return }
  pwdLoading.value = true
  try {
    await authApi.confirmPasswordRecovery({
      email: auth.userEmail!,
      newPassword: pwdNew.value,
      code: Number(pwdCode.value),
    })
    pwdStep.value = 0
    pwdCode.value = ''
    pwdNew.value = ''
    alert('Пароль успешно изменён')
  } catch (e: unknown) {
    const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message
    pwdError.value = msg || 'Неверный или просроченный код'
  } finally {
    pwdLoading.value = false
  }
}

// ─── Logout ───────────────────────────────────────────────────
async function onLogout() {
  await auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="page">
    <div class="page-header">
      <span class="page-title">Профиль</span>
    </div>

    <div class="profile-cols">
      <!-- ── LEFT column: identity + security ── -->
      <div class="col-left">
        <!-- User Card -->
        <div class="card user-card">
          <div class="avatar-wrap">
            <img v-if="auth.avatarUrl" :src="auth.avatarUrl" alt="avatar" class="avatar" />
            <div v-else class="avatar-fallback">{{ (auth.userName ?? 'U')[0].toUpperCase() }}</div>
          </div>
          <div class="user-info">
            <span class="user-name">{{ auth.userName }}</span>
            <span class="user-email">{{ auth.userEmail }}</span>
          </div>
          <button class="btn btn-ghost btn-sm" @click="openEdit">Изменить</button>
        </div>

        <!-- Edit Profile -->
        <div v-if="editOpen" class="card section-card">
          <div class="section-title">Редактировать профиль</div>
          <div class="form-group">
            <label class="form-label">Имя</label>
            <input v-model="editName" type="text" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">URL аватара</label>
            <input v-model="editAvatar" type="url" class="form-input" placeholder="https://..." />
          </div>
          <div v-if="editError" class="error-msg" style="margin-bottom:10px">{{ editError }}</div>
          <div class="modal-actions">
            <button class="btn btn-ghost" @click="editOpen = false">Отмена</button>
            <button class="btn btn-primary" :disabled="editLoading" @click="submitEdit">Сохранить</button>
          </div>
        </div>

        <!-- Security -->
        <div class="card section-card">
          <div class="section-title">Безопасность</div>

          <!-- Email change -->
          <div class="security-item">
            <div class="security-row">
              <span class="security-label">Email</span>
              <button v-if="emailStep === 0" class="btn btn-ghost btn-sm" @click="emailStep = 1">Изменить</button>
              <button v-else class="btn btn-ghost btn-sm" @click="emailStep = 0; emailError = ''">Отмена</button>
            </div>
            <div v-if="emailStep === 1" class="security-form">
              <div class="form-group">
                <label class="form-label">Новый email</label>
                <input v-model="newEmail" type="email" class="form-input" placeholder="new@email.com" />
              </div>
              <p class="security-hint">Код подтверждения придёт на текущий email</p>
              <div v-if="emailError" class="error-msg" style="margin-bottom:8px">{{ emailError }}</div>
              <button class="btn btn-primary" :disabled="emailLoading" @click="requestEmailChange">
                {{ emailLoading ? 'Отправка...' : 'Отправить код' }}
              </button>
            </div>
            <div v-if="emailStep === 2" class="security-form">
              <div class="form-group">
                <label class="form-label">Код из письма</label>
                <input v-model="emailCode" type="number" class="form-input" placeholder="123456" />
              </div>
              <div v-if="emailError" class="error-msg" style="margin-bottom:8px">{{ emailError }}</div>
              <button class="btn btn-primary" :disabled="emailLoading" @click="confirmEmailChange">
                {{ emailLoading ? 'Подтверждение...' : 'Подтвердить' }}
              </button>
            </div>
          </div>

          <div class="divider" />

          <!-- Password change -->
          <div class="security-item">
            <div class="security-row">
              <span class="security-label">Пароль</span>
              <button v-if="pwdStep === 0" class="btn btn-ghost btn-sm" @click="pwdStep = 1">Сменить</button>
              <button v-else class="btn btn-ghost btn-sm" @click="pwdStep = 0; pwdError = ''">Отмена</button>
            </div>
            <div v-if="pwdStep === 1" class="security-form">
              <p class="security-hint">Код для смены пароля будет отправлен на {{ auth.userEmail }}</p>
              <div v-if="pwdError" class="error-msg" style="margin-bottom:8px">{{ pwdError }}</div>
              <button class="btn btn-primary" :disabled="pwdLoading" @click="requestPwdRecovery">
                {{ pwdLoading ? 'Отправка...' : 'Отправить код' }}
              </button>
            </div>
            <div v-if="pwdStep === 2" class="security-form">
              <div class="form-group">
                <label class="form-label">Код из письма</label>
                <input v-model="pwdCode" type="number" class="form-input" placeholder="123456" />
              </div>
              <div class="form-group">
                <label class="form-label">Новый пароль</label>
                <input v-model="pwdNew" type="password" class="form-input" placeholder="Не менее 6 символов" />
              </div>
              <div v-if="pwdError" class="error-msg" style="margin-bottom:8px">{{ pwdError }}</div>
              <button class="btn btn-primary" :disabled="pwdLoading" @click="confirmPwdRecovery">
                {{ pwdLoading ? 'Сохранение...' : 'Сменить пароль' }}
              </button>
            </div>
          </div>
        </div>

        <button class="btn btn-danger logout-btn" @click="onLogout">Выйти из аккаунта</button>
      </div>

      <!-- ── RIGHT column: accounts + categories ── -->
      <div class="col-right">
        <!-- Accounts -->
        <div class="card section-card">
          <div class="section-header">
            <span class="section-title">Счета</span>
            <button class="btn btn-ghost btn-sm" @click="editingAcc = null; accModalOpen = true">+ Добавить</button>
          </div>
          <div v-if="accError" class="error-msg" style="margin-bottom:10px">{{ accError }}</div>
          <div v-if="accStore.loading" style="color: var(--text-muted); font-size:14px">Загрузка...</div>
          <div v-else-if="accStore.accounts.length === 0" class="empty-state" style="padding: 20px 0">
            Счетов нет
          </div>
          <div v-else class="acc-list">
            <div
              v-for="(acc, i) in accStore.accounts"
              :key="acc.id"
              class="acc-item"
              :class="{ 'not-last': i < accStore.accounts.length - 1 }"
            >
              <div class="acc-info">
                <span class="acc-bank">{{ acc.bankName }} {{ acc.accountNumber }}</span>
                <span class="acc-balance">{{ formatCurrency(Number(acc.balance), acc.currencyCode) }}</span>
              </div>
              <div class="acc-actions">
                <button class="icon-btn" @click="editingAcc = acc; accModalOpen = true" title="Редактировать">✏️</button>
                <button class="icon-btn danger" @click="deletingAccId = acc.id" title="Удалить">🗑</button>
              </div>
            </div>
          </div>
        </div>

        <!-- User Categories -->
        <div class="card section-card">
          <div class="section-header">
            <span class="section-title">Мои категории</span>
            <button class="btn btn-ghost btn-sm" @click="editingCat = null; catModalOpen = true">+ Добавить</button>
          </div>
          <div v-if="catError" class="error-msg" style="margin-bottom:10px">{{ catError }}</div>
          <div v-if="userCategories.length === 0" class="empty-state" style="padding: 16px 0; font-size:13px">
            Нет личных категорий
          </div>
          <div v-else class="cat-list">
            <div
              v-for="(cat, i) in userCategories"
              :key="cat.id"
              class="cat-item"
              :class="{ 'not-last': i < userCategories.length - 1 }"
            >
              <span class="cat-type-dot" :class="cat.type === 'INCOME' ? 'income' : 'expense'" />
              <span class="cat-name">{{ cat.nameCategory }}</span>
              <span class="cat-type-label">{{ cat.type === 'INCOME' ? 'Доход' : 'Расход' }}</span>
              <button class="icon-btn" @click="editingCat = cat; catModalOpen = true">✏️</button>
              <button class="icon-btn danger" @click="deletingCatId = cat.id">🗑</button>
            </div>
          </div>

          <div class="divider" style="margin: 12px 0" />
          <div class="section-sub">Системные категории</div>
          <div class="cat-list">
            <div
              v-for="(cat, i) in systemCategories"
              :key="cat.id"
              class="cat-item"
              :class="{ 'not-last': i < systemCategories.length - 1 }"
            >
              <span class="cat-type-dot" :class="cat.type === 'INCOME' ? 'income' : 'expense'" />
              <span class="cat-name">{{ cat.nameCategory }}</span>
              <span class="badge badge-warning" style="font-size:10px; margin-left: auto">сист.</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modals -->
    <AccountModal
      v-if="accModalOpen"
      :account="editingAcc"
      @submit="onAccSubmit"
      @close="accModalOpen = false; editingAcc = null"
    />

    <CategoryModal
      v-if="catModalOpen"
      :category="editingCat"
      @submit="onCatSubmit"
      @close="catModalOpen = false; editingCat = null"
    />

    <!-- Confirm Delete Account -->
    <div v-if="deletingAccId" class="modal-overlay" @click.self="deletingAccId = null">
      <div class="modal-sheet" style="padding-bottom: 24px">
        <div class="modal-title">Удалить счёт?</div>
        <p style="color: var(--text-muted); margin-bottom: 20px; font-size: 14px">
          Счёт будет архивирован. История транзакций сохранится.
        </p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="deletingAccId = null">Отмена</button>
          <button class="btn btn-danger" @click="doDeleteAcc">Удалить</button>
        </div>
      </div>
    </div>

    <!-- Confirm Delete Category -->
    <div v-if="deletingCatId" class="modal-overlay" @click.self="deletingCatId = null">
      <div class="modal-sheet" style="padding-bottom: 24px">
        <div class="modal-title">Удалить категорию?</div>
        <p style="color: var(--text-muted); margin-bottom: 20px; font-size: 14px">
          Транзакции с этой категорией потеряют привязку к ней.
        </p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="deletingCatId = null">Отмена</button>
          <button class="btn btn-danger" @click="doDeleteCat">Удалить</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ── 2-column grid ── */
.profile-cols {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.col-left, .col-right {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ── User card ── */
.user-card {
  display: flex;
  align-items: center;
  gap: 14px;
}
.avatar-wrap { flex-shrink: 0; }
.avatar { width: 52px; height: 52px; border-radius: 50%; object-fit: cover; }
.avatar-fallback {
  width: 52px; height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2a6fd8, #0E40C7);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  color: #fff;
}
.user-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.user-name { font-size: 16px; font-weight: 700; color: var(--text); }
.user-email { font-size: 13px; color: var(--text-muted); }

/* ── Sections ── */
.section-card { }
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.section-title { font-size: 15px; font-weight: 700; color: var(--heading); }
.section-sub { font-size: 12px; font-weight: 700; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 8px; }

/* ── Accounts ── */
.acc-list, .cat-list { display: flex; flex-direction: column; }
.acc-item, .cat-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
}
.acc-item.not-last, .cat-item.not-last { border-bottom: 1px solid var(--border); }
.acc-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.acc-bank { font-size: 14px; font-weight: 600; color: var(--text); }
.acc-balance { font-size: 13px; color: var(--text-muted); }
.acc-actions { display: flex; gap: 4px; }

/* ── Categories ── */
.cat-type-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.cat-type-dot.income { background: var(--success); }
.cat-type-dot.expense { background: var(--danger); }
.cat-name { flex: 1; font-size: 14px; color: var(--text); }
.cat-type-label { font-size: 12px; color: var(--text-muted); }

/* ── Icon buttons ── */
.icon-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  padding: 4px 6px;
  border-radius: 6px;
  opacity: 0.6;
  transition: opacity 0.15s, background 0.15s;
}
.icon-btn:hover { opacity: 1; background: var(--surface-2); }
.icon-btn.danger:hover { background: rgba(220, 53, 69, 0.12); }

/* ── Security ── */
.security-item { padding: 4px 0; }
.security-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.security-label { font-size: 15px; font-weight: 600; color: var(--text); }
.security-form { padding-bottom: 8px; }
.security-hint { font-size: 12px; color: var(--text-muted); margin-bottom: 12px; }

/* ── Logout ── */
.logout-btn { margin-top: 4px; }

/* ── Desktop 2-column ── */
@media (min-width: 768px) {
  .profile-cols {
    flex-direction: row;
    align-items: flex-start;
    gap: 24px;
  }
  .col-left  { flex: 1; }
  .col-right { flex: 1; }
}
</style>

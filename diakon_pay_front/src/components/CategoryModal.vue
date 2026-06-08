<script setup lang="ts">
import { ref, watch } from 'vue'
import type { CategoryResponse, CategoryRequest, CategoryType } from '@/types/category'

const props = defineProps<{
  category: CategoryResponse | null
}>()

const emit = defineEmits<{
  submit: [data: CategoryRequest]
  close: []
}>()

const form = ref({
  nameCategory: '',
  categoryDescription: '',
  type: 'EXPENSE' as CategoryType,
})
const error = ref('')

watch(
  () => props.category,
  (cat) => {
    if (cat) {
      form.value = {
        nameCategory: cat.nameCategory,
        categoryDescription: cat.categoryDescription ?? '',
        type: cat.type,
      }
    } else {
      form.value = { nameCategory: '', categoryDescription: '', type: 'EXPENSE' }
    }
    error.value = ''
  },
  { immediate: true }
)

function onSubmit() {
  error.value = ''
  if (!form.value.nameCategory.trim()) { error.value = 'Введите название категории'; return }

  const request: CategoryRequest = {
    id: props.category?.id,
    nameCategory: form.value.nameCategory.trim(),
    categoryDescription: form.value.categoryDescription.trim() || undefined,
    type: form.value.type,
  }
  emit('submit', request)
}
</script>

<template>
  <div class="modal-overlay" @click.self="emit('close')">
    <div class="modal-sheet">
      <div class="modal-title">{{ category ? 'Редактировать категорию' : 'Новая категория' }}</div>

      <form @submit.prevent="onSubmit">
        <div class="form-group">
          <label class="form-label">Название</label>
          <input
            v-model="form.nameCategory"
            type="text"
            class="form-input"
            placeholder="Например: Кафе"
            maxlength="50"
          />
        </div>

        <div class="form-group">
          <label class="form-label">Описание</label>
          <input
            v-model="form.categoryDescription"
            type="text"
            class="form-input"
            placeholder="Необязательно"
          />
        </div>

        <div class="form-group">
          <label class="form-label">Тип</label>
          <div class="type-toggle">
            <button
              type="button"
              class="type-btn"
              :class="{ active: form.type === 'EXPENSE' }"
              @click="form.type = 'EXPENSE'"
            >
              📉 Расход
            </button>
            <button
              type="button"
              class="type-btn"
              :class="{ active: form.type === 'INCOME' }"
              @click="form.type = 'INCOME'"
            >
              📈 Доход
            </button>
          </div>
        </div>

        <div v-if="error" class="error-msg" style="margin-bottom:12px">{{ error }}</div>

        <div class="modal-actions">
          <button type="button" class="btn btn-ghost" @click="emit('close')">Отмена</button>
          <button type="submit" class="btn btn-primary">
            {{ category ? 'Сохранить' : 'Создать' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.type-toggle {
  display: flex;
  gap: 8px;
}
.type-btn {
  flex: 1;
  padding: 11px;
  border-radius: var(--radius-sm);
  border: 2px solid var(--border);
  background: var(--input-bg);
  color: var(--text-muted);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}
.type-btn.active {
  border-color: var(--primary);
  color: var(--primary);
  background: rgba(12, 118, 247, 0.08);
}
</style>

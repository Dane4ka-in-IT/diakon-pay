import { defineStore } from 'pinia'
import { ref } from 'vue'
import { categoriesApi } from '@/api/categories'
import type { CategoryResponse, CategoryRequest } from '@/types/category'
import { cacheCategories, getCachedCategories } from '@/utils/db'

export const useCategoriesStore = defineStore('categories', () => {
  const categories = ref<CategoryResponse[]>([])
  const loading = ref(false)

  async function fetchCategories() {
    loading.value = true
    try {
      const { data } = await categoriesApi.getAll()
      categories.value = data
      await cacheCategories(data)
    } catch {
      categories.value = (await getCachedCategories()) as CategoryResponse[]
    } finally {
      loading.value = false
    }
  }

  async function createCategory(request: CategoryRequest) {
    await categoriesApi.create(request)
    await fetchCategories()
  }

  async function updateCategory(id: string, request: CategoryRequest) {
    await categoriesApi.update(id, request)
    await fetchCategories()
  }

  async function deleteCategory(id: string) {
    await categoriesApi.delete(id)
    await fetchCategories()
  }

  return { categories, loading, fetchCategories, createCategory, updateCategory, deleteCategory }
})

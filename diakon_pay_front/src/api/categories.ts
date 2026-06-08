import apiClient from './client'
import type { CategoryResponse, CategoryRequest } from '@/types/category'

export const categoriesApi = {
  getAll() {
    return apiClient.get<CategoryResponse[]>('/api/categories')
  },

  create(data: CategoryRequest) {
    return apiClient.post<{ message: string; categoryId: string }>('/api/categories', data)
  },

  update(id: string, data: CategoryRequest) {
    return apiClient.put<{ message: string }>(`/api/categories/${id}`, data)
  },

  delete(id: string) {
    return apiClient.delete<{ id: string; message: string }>(`/api/categories/${id}`)
  },
}

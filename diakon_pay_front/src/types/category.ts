export type CategoryType = 'INCOME' | 'EXPENSE'

export interface CategoryResponse {
  id: string
  userId: number
  nameCategory: string
  categoryDescription: string | null
  type: CategoryType
}

export interface CategoryRequest {
  id?: string
  nameCategory: string
  categoryDescription?: string
  type: CategoryType
}

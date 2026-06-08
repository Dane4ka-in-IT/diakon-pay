export interface AnalyticsTotals {
  period: string
  totalIncome: number
  totalExpense: number
  netSavings: number
  currency: string
}

export interface AnalyticsByCategory {
  categoryName: string
  amount: number
  percentage: number
}

export interface AnalyticsTotalBalance {
  totalBalance: number
  currencyCode: string
}

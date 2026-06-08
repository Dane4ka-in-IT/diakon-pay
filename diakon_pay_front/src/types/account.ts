export interface AccountResponse {
  id: string
  bankName: string
  accountNumber: string
  balance: number
  currencyCode: string
}

export interface AccountRequest {
  id?: string
  bankName: string
  accountNumber: string
  balance: number
  currencyCode: string
}

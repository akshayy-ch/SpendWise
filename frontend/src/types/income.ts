export interface Income {
  source: string;
  walletName: string;
  amount: number;
  incomeAt: string;
}
export interface IncomePage {
  content: Income[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
export interface IncomeFilters {
  source?: string;
  fromDate?: string;
  toDate?: string;
}
export interface CreateIncomeRequest {
  source: string;
  walletName: string;
  description?: string;
  amount: number;
  incomeAt: string;
}

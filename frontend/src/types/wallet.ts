export interface Wallet {
  walletName: string;
  username: string;
  type: string;
  currentBalance: number;
  status: string;
  createdAt: string;
}
export interface CreateWalletRequest {
  walletName: string;
  type: string;
  initialBalance?: number;
}

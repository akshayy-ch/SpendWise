import { api } from "./client";
import type { CreateWalletRequest, Wallet } from "../types/wallet";

export const walletApi = {
  getWallets: async () => (await api.get<Wallet[]>("/wallets/getWallets")).data,
  createWallet: async (payload: CreateWalletRequest) => (await api.post<Wallet>("/wallets/createWallet", payload)).data,
  archiveWallet: async (walletName: string) => (await api.patch<Wallet>("/wallets/archiveWallet", { walletName })).data,
  activateWallet: async (walletName: string) => (await api.patch<Wallet>("/wallets/activateWallet", { walletName })).data,
};

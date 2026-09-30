export interface UserProfile {
  username: string;
  name: string;
  email: string;
  phoneNumber: string;
  role: "USER" | "ADMIN";
}
export interface UserSearchResult {
  id: string;
  username: string;
  name: string;
}
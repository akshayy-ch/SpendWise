export interface Group {
  id: string;
  name: string;
  description?: string;
  status: string;
  creatorName: string;
}
export interface GroupMember {
  userId: string;
  userName: string;
  status: string;
  joinedAt: string;
}
export interface UserSearchResult {
  id: string;
  username: string;
  name: string;
}
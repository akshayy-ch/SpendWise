import { api } from "./client";
import type { Group, GroupMember, UserSearchResult } from "../types/group";

export const groupApi = {
  getMyGroups: async () => (await api.get<Group[]>("/groups/my")).data,
  createGroup: async (name: string, description?: string) =>
    (await api.post<Group>("/groups/createGroup", { name, description })).data,
  archiveGroup: async (groupId: string) => (await api.patch<Group>("/groups/" + groupId + "/archive")).data,
  getMembers: async (groupId: string) => (await api.get<GroupMember[]>("/groups/" + groupId + "/members")).data,
  addMembers: async (groupId: string, userIds: string[]) =>
    (await api.post<GroupMember[]>("/groups/" + groupId + "/members", { userIds })).data,
  removeMember: async (groupId: string, userId: string) =>
    (await api.delete<GroupMember>("/groups/" + groupId + "/members/" + userId)).data,
  leaveGroup: async (groupId: string) =>
    (await api.post<GroupMember>("/groups/" + groupId + "/members/leave")).data,
  searchUsers: async (query: string) =>
    (await api.get<UserSearchResult[]>("/users/search", { params: { query } })).data,
};
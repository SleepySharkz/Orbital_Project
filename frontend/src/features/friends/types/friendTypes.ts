type FriendRequestStatus = "PENDING" | "ACCEPTED" | "DECLINED" | "CANCELLED";

type UserSearchResult = {
  id: number;
  username: string;
  email: string;
  isSelf: boolean;
  isFriend: boolean;
  incomingRequestPending: boolean;
  outgoingRequestPending: boolean;
};

type FriendRequest = {
  id: number;
  senderUserId: number;
  senderUsername: string;
  senderEmail: string;
  recipientUserId: number;
  recipientUsername: string;
  recipientEmail: string;
  status: FriendRequestStatus;
  createdAt: string;
  respondedAt: string | null;
};

type FriendSummary = {
  userId: number;
  username: string;
  email: string;
  friendsSince: string;
};

type FriendSharingIndicator = {
  incomingRequestCount: number;
  incomingTcCount: number;
  outgoingRequestCount: number;
  outgoingTcCount: number;
  outgoingRequestId: number | null;
  acceptedSharedTcCount: number;
};

export type {
  FriendRequest,
  FriendRequestStatus,
  FriendSharingIndicator,
  FriendSummary,
  UserSearchResult,
};

import type {
  FriendRequest,
  FriendSummary,
  UserSearchResult,
} from "../types/friendTypes";

type ErrorResponse = {
  message?: string;
  detail?: string;
  error?: string;
};

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

function authHeaders(token: string) {
  return {
    Authorization: `Bearer ${token}`,
  };
}

function jsonAuthHeaders(token: string) {
  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  };
}

async function parseJson<T>(response: Response): Promise<T> {
  return (await response.json()) as T;
}

async function getErrorMessage(response: Response, fallback: string) {
  try {
    const error = await parseJson<ErrorResponse>(response);
    return error.message || error.detail || error.error || fallback;
  } catch {
    return fallback;
  }
}

export async function searchUsersByEmail(email: string, token: string) {
  const params = new URLSearchParams({ email });
  const response = await fetch(
    `${API_BASE_URL}/api/v1/users/search?${params.toString()}`,
    {
      method: "GET",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "Could not search users."));
  }

  return parseJson<UserSearchResult[]>(response);
}

export async function fetchFriends(token: string) {
  const response = await fetch(`${API_BASE_URL}/api/v1/friends`, {
    method: "GET",
    headers: authHeaders(token),
  });

  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "Could not load friends."));
  }

  return parseJson<FriendSummary[]>(response);
}

export async function fetchIncomingFriendRequests(token: string) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/friend-requests/incoming`,
    {
      method: "GET",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not load incoming requests."),
    );
  }

  return parseJson<FriendRequest[]>(response);
}

export async function fetchOutgoingFriendRequests(token: string) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/friend-requests/outgoing`,
    {
      method: "GET",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not load outgoing requests."),
    );
  }

  return parseJson<FriendRequest[]>(response);
}

export async function sendFriendRequest(
  recipientUserId: number,
  token: string,
) {
  const response = await fetch(`${API_BASE_URL}/api/v1/friend-requests`, {
    method: "POST",
    headers: jsonAuthHeaders(token),
    body: JSON.stringify({ recipientUserId }),
  });

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not send friend request."),
    );
  }

  return parseJson<FriendRequest>(response);
}

export async function acceptFriendRequest(requestId: number, token: string) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/friend-requests/${requestId}/accept`,
    {
      method: "POST",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not accept friend request."),
    );
  }

  return parseJson<FriendRequest>(response);
}

export async function declineFriendRequest(requestId: number, token: string) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/friend-requests/${requestId}/decline`,
    {
      method: "POST",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not decline friend request."),
    );
  }

  return parseJson<FriendRequest>(response);
}

export async function removeFriend(friendUserId: number, token: string) {
  const response = await fetch(`${API_BASE_URL}/api/v1/friends/${friendUserId}`, {
    method: "DELETE",
    headers: authHeaders(token),
  });

  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "Could not remove friend."));
  }
}

import type {
  SharedTCDetail,
  SharedTCSummary,
  TCSharingRequestDetail,
  TCSharingRequestSummary,
} from "../types/tcSharingTypes";

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

export async function sendTCSharingRequest(
  friendUserId: number,
  tcIds: number[],
  token: string,
) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/friends/${friendUserId}/tc-sharing-requests`,
    {
      method: "POST",
      headers: jsonAuthHeaders(token),
      body: JSON.stringify({ tcIds }),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not send TC sharing request."),
    );
  }

  return parseJson<TCSharingRequestDetail>(response);
}

export async function fetchIncomingTCSharingRequests(token: string) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/tc-sharing-requests/incoming`,
    {
      method: "GET",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(
        response,
        "Could not load incoming TC sharing requests.",
      ),
    );
  }

  return parseJson<TCSharingRequestSummary[]>(response);
}

export async function fetchOutgoingTCSharingRequests(token: string) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/tc-sharing-requests/outgoing`,
    {
      method: "GET",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(
        response,
        "Could not load outgoing TC sharing requests.",
      ),
    );
  }

  return parseJson<TCSharingRequestSummary[]>(response);
}

export async function fetchTCSharingRequestDetail(
  requestId: number,
  token: string,
) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/tc-sharing-requests/${requestId}`,
    {
      method: "GET",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(
        response,
        "Could not load TC sharing request.",
      ),
    );
  }

  return parseJson<TCSharingRequestDetail>(response);
}

export async function acceptTCSharingRequest(
  requestId: number,
  token: string,
) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/tc-sharing-requests/${requestId}/accept`,
    {
      method: "POST",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not accept TC sharing request."),
    );
  }

  return parseJson<TCSharingRequestDetail>(response);
}

export async function declineTCSharingRequest(
  requestId: number,
  token: string,
) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/tc-sharing-requests/${requestId}/decline`,
    {
      method: "POST",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not decline TC sharing request."),
    );
  }

  return parseJson<TCSharingRequestDetail>(response);
}

export async function cancelTCSharingRequest(
  requestId: number,
  token: string,
) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/tc-sharing-requests/${requestId}/cancel`,
    {
      method: "POST",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not cancel TC sharing request."),
    );
  }

  return parseJson<TCSharingRequestDetail>(response);
}

export async function fetchSharedTCs(token: string) {
  const response = await fetch(`${API_BASE_URL}/api/v1/shared-tcs`, {
    method: "GET",
    headers: authHeaders(token),
  });

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not load shared TCs."),
    );
  }

  return parseJson<SharedTCSummary[]>(response);
}

export async function fetchSharedTCById(sharedTcId: number, token: string) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/shared-tcs/${sharedTcId}`,
    {
      method: "GET",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not load shared TC."),
    );
  }

  return parseJson<SharedTCDetail>(response);
}

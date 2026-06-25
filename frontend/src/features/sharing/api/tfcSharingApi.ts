import type {
  TFCSharingRequestDetail,
  TFCSharingRequestSummary,
} from "../types/tfcSharingTypes";

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

export async function sendTFCSharingRequest(
  friendUserId: number,
  tfcIds: number[],
  token: string,
) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/friends/${friendUserId}/tfc-sharing-requests`,
    {
      method: "POST",
      headers: jsonAuthHeaders(token),
      body: JSON.stringify({ tfcIds }),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not send TFC sharing request."),
    );
  }

  return parseJson<TFCSharingRequestDetail>(response);
}

export async function fetchIncomingTFCSharingRequests(token: string) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/tfc-sharing-requests/incoming`,
    {
      method: "GET",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(
        response,
        "Could not load incoming TFC sharing requests.",
      ),
    );
  }

  return parseJson<TFCSharingRequestSummary[]>(response);
}

export async function fetchOutgoingTFCSharingRequests(token: string) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/tfc-sharing-requests/outgoing`,
    {
      method: "GET",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(
        response,
        "Could not load outgoing TFC sharing requests.",
      ),
    );
  }

  return parseJson<TFCSharingRequestSummary[]>(response);
}

export async function fetchTFCSharingRequestDetail(
  requestId: number,
  token: string,
) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/tfc-sharing-requests/${requestId}`,
    {
      method: "GET",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(
        response,
        "Could not load TFC sharing request.",
      ),
    );
  }

  return parseJson<TFCSharingRequestDetail>(response);
}

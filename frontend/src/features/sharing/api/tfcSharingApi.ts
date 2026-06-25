import type {
  SharedTFCDetail,
  SharedTFCSummary,
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

export async function acceptTFCSharingRequest(
  requestId: number,
  token: string,
) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/tfc-sharing-requests/${requestId}/accept`,
    {
      method: "POST",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not accept TFC sharing request."),
    );
  }

  return parseJson<TFCSharingRequestDetail>(response);
}

export async function declineTFCSharingRequest(
  requestId: number,
  token: string,
) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/tfc-sharing-requests/${requestId}/decline`,
    {
      method: "POST",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not decline TFC sharing request."),
    );
  }

  return parseJson<TFCSharingRequestDetail>(response);
}

export async function fetchSharedTFCs(token: string) {
  const response = await fetch(`${API_BASE_URL}/api/v1/shared-tfcs`, {
    method: "GET",
    headers: authHeaders(token),
  });

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not load shared TFCs."),
    );
  }

  return parseJson<SharedTFCSummary[]>(response);
}

export async function fetchSharedTFCById(sharedTfcId: number, token: string) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/shared-tfcs/${sharedTfcId}`,
    {
      method: "GET",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not load shared TFC."),
    );
  }

  return parseJson<SharedTFCDetail>(response);
}

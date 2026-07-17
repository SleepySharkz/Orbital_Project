import type {
  MindmapInsightDetail,
  MindmapResponse,
} from "../types/mindmapTypes";

type MessageResponse = {
  message?: string;
  error?: string;
  detail?: string;
};

type PollOptions = {
  intervalMs?: number;
  timeoutMs?: number;
  signal?: AbortSignal;
};

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

async function parseJsonResponse<T>(response: Response): Promise<T> {
  return (await response.json()) as T;
}

export async function fetchModuleMindmap(moduleId: number, token: string) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/modules/${moduleId}/mindmap`,
    {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    },
  );

  const data = await parseJsonResponse<MindmapResponse | MessageResponse>(
    response,
  );

  if (!response.ok) {
    const errorData = data as MessageResponse;
    throw new Error(
      errorData.message || errorData.error || "Could not load mindmap.",
    );
  }

  return data as MindmapResponse;
}

export async function discoverInsight(
  moduleId: number,
  tcIds: number[],
  token: string,
) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/modules/${moduleId}/mindmap/insights/discover`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ tcIds }),
    },
  );
  const data = await parseJsonResponse<MindmapInsightDetail | MessageResponse>(
    response,
  );

  if (!response.ok) {
    const errorData = data as MessageResponse;
    throw new Error(
      errorData.message ||
        errorData.detail ||
        errorData.error ||
        "Could not discover insight.",
    );
  }

  return data as MindmapInsightDetail;
}

export async function fetchInsightDetail(
  insightId: number,
  token: string,
  signal?: AbortSignal,
) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/mindmap/insights/${insightId}`,
    {
      method: "GET",
      headers: { Authorization: `Bearer ${token}` },
      signal,
    },
  );
  const data = await parseJsonResponse<MindmapInsightDetail | MessageResponse>(
    response,
  );

  if (!response.ok) {
    const errorData = data as MessageResponse;
    throw new Error(
      errorData.message ||
        errorData.detail ||
        errorData.error ||
        "Could not load insight detail.",
    );
  }

  return data as MindmapInsightDetail;
}

export async function pollInsightUntilSettled(
  insightId: number,
  token: string,
  options: PollOptions = {},
) {
  const intervalMs = options.intervalMs ?? 1000;
  const timeoutMs = options.timeoutMs ?? 45000;
  const startedAt = Date.now();

  while (Date.now() - startedAt < timeoutMs) {
    await wait(intervalMs, options.signal);
    const detail = await fetchInsightDetail(insightId, token, options.signal);

    if (detail.status !== "GENERATING") {
      return detail;
    }
  }

  throw new Error(
    "Insight generation is still running. You can reopen this pair shortly.",
  );
}

function wait(delayMs: number, signal?: AbortSignal) {
  return new Promise<void>((resolve, reject) => {
    if (signal?.aborted) {
      reject(new DOMException("The request was aborted.", "AbortError"));
      return;
    }

    const timeoutId = window.setTimeout(() => {
      signal?.removeEventListener("abort", handleAbort);
      resolve();
    }, delayMs);

    function handleAbort() {
      window.clearTimeout(timeoutId);
      reject(new DOMException("The request was aborted.", "AbortError"));
    }

    signal?.addEventListener("abort", handleAbort, { once: true });
  });
}

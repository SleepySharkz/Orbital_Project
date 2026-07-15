import type { MindmapResponse } from "../types/mindmapTypes";

type MessageResponse = {
  message?: string;
  error?: string;
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

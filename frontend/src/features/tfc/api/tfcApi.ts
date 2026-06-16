type TfcSummary = {
  id: number;
  moduleId: number;
  ownerUsername: string;
  courseCode: string;
  schoolSem: string;
  topic: string;
  entryCount: number;
  updatedAt: string;
  isStale?: boolean | null;
};

type TfcEntryView = {
  entryId: number;
  topic: string;
  flashcardQuestion: string;
  flashcardNoteContent: string;
  questionText: string | null;
  roughNote: string;
  createdAt: string;
};

type TfcContentResponse = {
  id: number;
  moduleId: number;
  courseCode: string;
  schoolSem: string;
  topic: string;
  updatedAt: string;
  entries: TfcEntryView[];
};

type MessageResponse = {
  message: string;
  error?: string;
};

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

async function parseJsonResponse<T>(response: Response): Promise<T> {
  return (await response.json()) as T;
}

function buildAuthHeaders(token: string) {
  return {
    Authorization: `Bearer ${token}`,
  };
}

export async function fetchTFCsForModule(moduleId: number, token: string) {
  const response = await fetch(`${API_BASE_URL}/api/v1/modules/${moduleId}/tfcs`, {
    method: "GET",
    headers: buildAuthHeaders(token),
  });

  const data = await parseJsonResponse<TfcSummary[] | MessageResponse>(response);

  if (!response.ok) {
    const errorData = data as MessageResponse;
    throw new Error(errorData.message || "Could not load topic sheets.");
  }

  return data as TfcSummary[];
}

export async function fetchTFCById(tfcId: number, token: string) {
  const response = await fetch(`${API_BASE_URL}/api/v1/tfcs/${tfcId}`, {
    method: "GET",
    headers: buildAuthHeaders(token),
  });

  const data = await parseJsonResponse<TfcContentResponse | MessageResponse>(response);

  if (!response.ok) {
    const errorData = data as MessageResponse;
    throw new Error(errorData.message || "Could not load topic sheet.");
  }

  return data as TfcContentResponse;
}

export type {
  TfcContentResponse,
  TfcEntryView,
  TfcSummary,
};

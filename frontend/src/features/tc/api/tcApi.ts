type TcSummary = {
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

type TcEntryView = {
  entryId: number;
  topic: string;
  flashcardQuestion: string;
  flashcardNoteContent: string;
  questionText: string | null;
  roughNote: string;
  createdAt: string;
};

// Summary level response
type TcContentResponse = {
  id: number;
  moduleId: number;
  courseCode: string;
  schoolSem: string;
  topic: string;
  isStale: boolean;
  updatedAt: string;
  entries: TcEntryView[];
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

export async function fetchTCsForModule(moduleId: number, token: string) {
  const response = await fetch(`${API_BASE_URL}/api/v1/modules/${moduleId}/tcs`, {
    method: "GET",
    headers: buildAuthHeaders(token),
  });

  const data = await parseJsonResponse<TcSummary[] | MessageResponse>(response);

  if (!response.ok) {
    const errorData = data as MessageResponse;
    throw new Error(errorData.message || "Could not load topic sheets.");
  }

  return data as TcSummary[];
}

export async function fetchTCById(tcId: number, token: string) {
  const response = await fetch(`${API_BASE_URL}/api/v1/tcs/${tcId}`, {
    method: "GET",
    headers: buildAuthHeaders(token),
  });

  const data = await parseJsonResponse<TcContentResponse | MessageResponse>(response);

  if (!response.ok) {
    const errorData = data as MessageResponse;
    throw new Error(errorData.message || "Could not load topic sheet.");
  }

  return data as TcContentResponse;
}

export type {
  TcContentResponse,
  TcEntryView,
  TcSummary,
};

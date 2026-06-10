type SourceType = "ASSIGNMENT" | "TUTORIAL" | "PRACTICE_PAPER";

type CFCContent = {
  flashcardQuestion: string;
  flashcardNoteContent: string;
};

type SourceMaterial = {
  questionText: string | null;
  roughNote: string;
};

type CreateCFCEntryPayload = {
  itemId: number;
  topic: string;
  questionText: string | null;
  imageKeys: string[];
  roughNote: string;
};

type CreateCFCPayload = {
  moduleId: number;
  flashcardHeader: {
    sourceType: SourceType;
    sourceTitle: string;
  };
  items: CreateCFCEntryPayload[];
};

type CreatedCFCEntry = {
  id: number;
  requestItemId: number;
  topic: string;
  flashcardQuestion: string;
  flashcardNoteContent: string;
  sourceMaterial: SourceMaterial;
};

type CFCSummary = {
  id: number;
  moduleId: number;
  courseCode: string;
  schoolSem: string;
  sourceType: SourceType;
  sourceTitle: string;
  title: string;
  summary: string;
  createdAt: string;
};

type CFCResponse = {
  id: number;
  moduleId: number;
  courseCode: string;
  schoolSem: string;
  sourceType: SourceType;
  sourceTitle: string;
  title: string;
  summary: string;
  entries: CreatedCFCEntry[];
  createdAt: string;
};

type CreatedCFCResponse = CFCResponse;

type UpdateCFCSummaryPayload = {
  summary: string;
};

type UpdateCFCEntryContentPayload = CFCContent;

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

function buildJsonAuthHeaders(token: string) {
  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  };
}

export async function createCFCRequest(
  payload: CreateCFCPayload,
  entryFiles: File[][],
  token: string,
) {
  const formData = new FormData();
  formData.append(
    "request",
    new Blob([JSON.stringify(payload)], { type: "application/json" }),
  );

  entryFiles.forEach((files, entryIndex) => {
    files.forEach((file, fileIndex) => {
      const imageKey = `item_${entryIndex + 1}_img_${fileIndex + 1}`;
      formData.append(imageKey, file);
    });
  });

  const response = await fetch(`${API_BASE_URL}/api/v1/cfcs`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: formData,
  });

  const data = await parseJsonResponse<CreatedCFCResponse | MessageResponse>(response);

  if (!response.ok) {
    const errorData = data as MessageResponse;

    if (response.status === 429) {
      const retryAfterSeconds = response.headers.get("Retry-After") ?? "60";
      throw new Error(`Rate limit reached. Try again in ${retryAfterSeconds} seconds.`);
    }

    throw new Error(errorData.message || "Could not create flashcard.");
  }

  return data as CreatedCFCResponse;
}

export async function fetchCFCsForModule(moduleId: number, token: string) {
  const response = await fetch(`${API_BASE_URL}/api/v1/modules/${moduleId}/cfcs`, {
    method: "GET",
    headers: buildAuthHeaders(token),
  });

  const data = await parseJsonResponse<CFCSummary[] | MessageResponse>(response);

  if (!response.ok) {
    const errorData = data as MessageResponse;
    throw new Error(errorData.message || "Could not load saved flashcards.");
  }

  return data as CFCSummary[];
}

export async function fetchCFCById(cfcId: number, token: string) {
  const response = await fetch(`${API_BASE_URL}/api/v1/cfcs/${cfcId}`, {
    method: "GET",
    headers: buildAuthHeaders(token),
  });

  const data = await parseJsonResponse<CFCResponse | MessageResponse>(response);

  if (!response.ok) {
    const errorData = data as MessageResponse;
    throw new Error(errorData.message || "Could not load saved flashcard.");
  }

  return data as CFCResponse;
}

export async function updateCFCSummary(
  cfcId: number,
  payload: UpdateCFCSummaryPayload,
  token: string,
) {
  const response = await fetch(`${API_BASE_URL}/api/v1/cfcs/${cfcId}/summary`, {
    method: "PATCH",
    headers: buildJsonAuthHeaders(token),
    body: JSON.stringify(payload),
  });

  const data = await parseJsonResponse<CFCResponse | MessageResponse>(response);

  if (!response.ok) {
    const errorData = data as MessageResponse;
    throw new Error(errorData.message || "Could not update CFC summary.");
  }

  return data as CFCResponse;
}

export async function updateCFCEntryContent(
  cfcId: number,
  entryId: number,
  payload: UpdateCFCEntryContentPayload,
  token: string,
) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/cfcs/${cfcId}/entries/${entryId}/content`,
    {
      method: "PATCH",
      headers: buildJsonAuthHeaders(token),
      body: JSON.stringify(payload),
    },
  );

  const data = await parseJsonResponse<CreatedCFCEntry | MessageResponse>(response);

  if (!response.ok) {
    const errorData = data as MessageResponse;
    throw new Error(errorData.message || "Could not update CFC entry.");
  }

  return data as CreatedCFCEntry;
}

export type {
  CreatedCFCEntry,
  CreatedCFCResponse,
  CFCContent,
  CFCResponse,
  CFCSummary,
  SourceMaterial,
  SourceType,
  UpdateCFCEntryContentPayload,
  UpdateCFCSummaryPayload,
};

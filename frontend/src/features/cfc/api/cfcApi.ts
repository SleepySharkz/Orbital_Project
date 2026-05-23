type SourceType = "ASSIGNMENT" | "TUTORIAL" | "PRACTICE_PAPER";

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
  content: {
    learningPoint: string;
    explanation: string;
    mistakePattern: string;
    reviewPrompt: string;
  };
  sourceMaterial: {
    questionText: string | null;
    roughNote: string;
  };
};

type CreatedCFCResponse = {
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

type MessageResponse = {
  message: string;
};

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

async function parseJsonResponse<T>(response: Response): Promise<T> {
  return (await response.json()) as T;
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
    throw new Error(errorData.message || "Could not create flashcard.");
  }

  return data as CreatedCFCResponse;
}

export type { CreatedCFCResponse, SourceType };

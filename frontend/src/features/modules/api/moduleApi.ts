type CreateModulePayload = {
  courseCode: string;
  schoolSem: string;
  topics: string[];
};

type UpdateModulePayload = {
  courseCode: string;
  schoolSem: string;
  topics: string[];
};

type ModuleSummary = {
  id: number;
  courseCode: string;
  schoolSem: string;
};

type ModuleResponse = {
  id: number;
  courseCode: string;
  schoolSem: string;
  topics: string[];
  createdAt: string;
  updatedAt: string;
};

type ModuleTopicsResponse = {
  moduleId: number;
  courseCode: string;
  topics: string[];
};

type MessageResponse = {
  message: string;
};

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

async function parseJsonResponse<T>(response: Response): Promise<T> {
  return (await response.json()) as T;
}

function buildAuthHeaders(token: string) {
  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  };
}

// just like authApi, this file only worries about fetching and talking to the backend
export async function createModuleRequest(
  payload: CreateModulePayload,
  token: string,
) {
  const response = await fetch(`${API_BASE_URL}/api/v1/modules`, {
    method: "POST",
    headers: buildAuthHeaders(token),
    body: JSON.stringify(payload),
  });

  const data = await parseJsonResponse<ModuleResponse | MessageResponse>(response);

  if (!response.ok) {
    const errorData = data as MessageResponse;
    throw new Error(errorData.message || "Could not create module.");
  }

  return data as ModuleResponse;
}

export async function fetchModules(token: string) {
  const response = await fetch(`${API_BASE_URL}/api/v1/modules`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  const data = await parseJsonResponse<ModuleSummary[] | MessageResponse>(response);

  if (!response.ok) {
    const errorData = data as MessageResponse;
    throw new Error(errorData.message || "Could not load modules.");
  }

  return data as ModuleSummary[];
}

export async function fetchModuleById(moduleId: number, token: string) {
  const response = await fetch(`${API_BASE_URL}/api/v1/modules/${moduleId}`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  const data = await parseJsonResponse<ModuleResponse | MessageResponse>(response);

  if (!response.ok) {
    const errorData = data as MessageResponse;
    throw new Error(errorData.message || "Could not load module.");
  }

  return data as ModuleResponse;
}

export async function updateModuleRequest(
  moduleId: number,
  payload: UpdateModulePayload,
  token: string,
) {
  const response = await fetch(`${API_BASE_URL}/api/v1/modules/${moduleId}`, {
    method: "PUT",
    headers: buildAuthHeaders(token),
    body: JSON.stringify(payload),
  });

  const data = await parseJsonResponse<ModuleResponse | MessageResponse>(response);

  if (!response.ok) {
    const errorData = data as MessageResponse;
    throw new Error(errorData.message || "Could not update module.");
  }

  return data as ModuleResponse;
}

export async function fetchModuleTopics(moduleId: number, token: string) {
  const response = await fetch(`${API_BASE_URL}/api/v1/modules/${moduleId}/topics`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  const data = await parseJsonResponse<ModuleTopicsResponse | MessageResponse>(response);

  if (!response.ok) {
    const errorData = data as MessageResponse;
    throw new Error(errorData.message || "Could not load module topics.");
  }

  return data as ModuleTopicsResponse;
}

export type {
  CreateModulePayload,
  ModuleResponse,
  ModuleSummary,
  ModuleTopicsResponse,
  UpdateModulePayload,
};

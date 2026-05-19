type User = {
  username: string;
  email: string;
};

type LoginCredentials = {
  email: string;
  password: string;
};

type SignupCredentials = {
  username: string;
  email: string;
  password: string;
};

type LoginResponse = {
  token: string;
  user: User;
};

type MessageResponse = {
  message: string;
};

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

async function parseJsonResponse<T>(response: Response): Promise<T> {
  return (await response.json()) as T;
}


// EVEN MORE separation of concerns here, we let this worry about the fetching, and talking to the backend, while the AuthContext becomes more of an orchestrator
export async function signupRequest(credentials: SignupCredentials) {
  const response = await fetch(`${API_BASE_URL}/auth/signup`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(credentials),
  });

  const data = await parseJsonResponse<MessageResponse>(response);

  if (!response.ok) {
    throw new Error(data.message || "Signup failed.");
  }

  return data.message || "Account created successfully.";
}

export async function loginRequest(credentials: LoginCredentials) {
  const response = await fetch(`${API_BASE_URL}/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(credentials),
  });

  const data = await parseJsonResponse<LoginResponse | MessageResponse>(response);

  if (!response.ok) {
    const errorData = data as MessageResponse;
    throw new Error(errorData.message || "Login failed.");
  }

  return data as LoginResponse;
}

export async function fetchCurrentUser(token: string) {
  const response = await fetch(`${API_BASE_URL}/auth/me`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error("Unauthorized");
  }

  return parseJsonResponse<User>(response);
}

export async function logoutRequest(token: string | null) {
  await fetch(`${API_BASE_URL}/auth/logout`, {
    method: "POST",
    headers: token
      ? {
        Authorization: `Bearer ${token}`,
      }
      : {},
  });
}

export type { LoginCredentials, SignupCredentials, User };

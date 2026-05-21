import { clearToken, getAccessToken, saveTokens } from './auth';

const API_BASE_URL = import.meta.env['VITE_API_BASE_URL'] ?? '';
const MAX_UPLOAD_BYTES = 10 * 1024 * 1024;

type RequestOptions = Omit<RequestInit, 'body'> & {
  body?: BodyInit | object | null;
};

let refreshRequest: Promise<string | null> | null = null;

function buildRequestBody(body: RequestOptions['body']) {
  return body == null || typeof body === 'string' || body instanceof FormData
    ? (body ?? null)
    : JSON.stringify(body);
}

async function executeRequest(path: string, options: RequestOptions = {}, token?: string | null) {
  const headers = new Headers(options.headers);
  const isFormData = typeof FormData !== 'undefined' && options.body instanceof FormData;

  if (!isFormData) {
    headers.set('Content-Type', 'application/json');
  }

  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  return fetch(`${API_BASE_URL}${path}`, {
    ...options,
    credentials: 'include',
    headers,
    body: buildRequestBody(options.body)
  });
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed with status ${response.status}`);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const contentLength = response.headers.get('content-length');
  if (contentLength === '0') {
    return undefined as T;
  }

  const contentType = response.headers.get('content-type') ?? '';
  if (!contentType.includes('application/json')) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

async function refreshAccessToken() {
  if (refreshRequest) {
    return refreshRequest;
  }

  refreshRequest = (async () => {
    const response = await executeRequest('/auth/refresh', {
      method: 'POST'
    });

    if (!response.ok) {
      clearToken();
      return null;
    }

    const data = (await response.json()) as LoginResponse;
    saveTokens(data.accessToken);
    return data.accessToken;
  })();

  try {
    return await refreshRequest;
  } finally {
    refreshRequest = null;
  }
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const accessToken = getAccessToken();
  let response = await executeRequest(path, options, accessToken);

  if (response.status === 401 && !path.startsWith('/auth/')) {
    const refreshedAccessToken = await refreshAccessToken();

    if (refreshedAccessToken) {
      response = await executeRequest(path, options, refreshedAccessToken);
    }
  }

  return handleResponse<T>(response);
}

export type LoginResponse = {
  accessToken: string;
};

export type RegisterResponse = {
  id: number;
  email: string;
};

export type ChatRecord = {
  id: number;
  name: string;
  createdAt: string;
  updatedAt: string;
  documentCount: number;
};

export type DocumentRecord = {
  id: number;
  fileName: string;
  fileType: string;
  fileSize: number;
  chatId: number | null;
  processingStatus: string | null;
  processingError: string | null;
  processingMessage: string | null;
  processingStartedAt: string | null;
  processingCompletedAt: string | null;
};

export type ChatMessageRecord = {
  id: number;
  role: string;
  content: string;
  createdAt: string;
};

export type DocumentPreviewRecord = {
  url: string;
  contentType: string;
  fileName: string;
};

type PaginatedDocuments = {
  content: DocumentRecord[];
};

export async function login(email: string, password: string) {
  return request<LoginResponse>('/auth/login', {
    method: 'POST',
    body: { email, password }
  });
}

export async function register(email: string, password: string) {
  return request<RegisterResponse>('/auth/register', {
    method: 'POST',
    body: { email, password }
  });
}

export async function logout() {
  try {
    await request<void>('/auth/logout', {
      method: 'POST'
    });
  } finally {
    clearToken();
  }
}

export async function getDocuments() {
  const response = await request<PaginatedDocuments | DocumentRecord[]>('/documents');
  return Array.isArray(response) ? response : response.content;
}

export async function getChats() {
  return request<ChatRecord[]>('/chats');
}

export async function getChat(chatId: number) {
  return request<ChatRecord>(`/chats/${chatId}`);
}

export async function getChatMessages(chatId: number) {
  return request<ChatMessageRecord[]>(`/chats/${chatId}/messages`);
}

export async function createChat(name: string) {
  return request<ChatRecord>('/chats', {
    method: 'POST',
    body: { name }
  });
}

export async function deleteChat(chatId: number) {
  return request<void>(`/chats/${chatId}`, {
    method: 'DELETE'
  });
}

export async function renameChat(chatId: number, name: string) {
  return request<ChatRecord>(`/chats/${chatId}`, {
    method: 'PATCH',
    body: { name }
  });
}

export async function getChatDocuments(chatId: number) {
  const response = await request<PaginatedDocuments | DocumentRecord[]>(`/documents/chats/${chatId}`);
  return Array.isArray(response) ? response : response.content;
}

export async function uploadDocument(chatId: number, file: File) {
  if (file.size > MAX_UPLOAD_BYTES) {
    throw new Error('File size exceeds the 10 MB upload limit.');
  }

  const formData = new FormData();
  formData.append('file', file);

  return request<DocumentRecord>(`/documents/chats/${chatId}/upload`, {
    method: 'POST',
    body: formData
  });
}

export async function askQuestion(chatId: number, question: string) {
  return request<{ answer: string }>(`/ai/chats/${chatId}/ask`, {
    method: 'POST',
    body: { question }
  });
}

export async function downloadDocument(chatId: number, documentId: number) {
  let accessToken = getAccessToken();
  let response = await fetch(`${API_BASE_URL}/documents/chats/${chatId}/${documentId}/download`, {
    headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : undefined
  });

  if (response.status === 401) {
    accessToken = await refreshAccessToken();
    response = await fetch(`${API_BASE_URL}/documents/chats/${chatId}/${documentId}/download`, {
      headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : undefined
    });
  }

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed with status ${response.status}`);
  }

  const blob = await response.blob();
  const contentDisposition = response.headers.get('content-disposition') ?? '';
  const fileNameMatch = contentDisposition.match(/filename="?([^"]+)"?/i);
  const fileName = fileNameMatch?.[1] ?? `document-${documentId}`;
  const url = window.URL.createObjectURL(blob);
  const anchor = window.document.createElement('a');

  anchor.href = url;
  anchor.download = fileName;
  window.document.body.appendChild(anchor);
  anchor.click();
  anchor.remove();

  window.setTimeout(() => window.URL.revokeObjectURL(url), 60_000);
}

export async function deleteDocument(chatId: number, documentId: number) {
  return request<void>(`/documents/chats/${chatId}/${documentId}`, {
    method: 'DELETE'
  });
}

export async function renameDocument(chatId: number, documentId: number, name: string) {
  return request<DocumentRecord>(`/documents/chats/${chatId}/${documentId}`, {
    method: 'PATCH',
    body: { name }
  });
}

export async function previewDocument(chatId: number, documentId: number) {
  let accessToken = getAccessToken();
  let response = await fetch(`${API_BASE_URL}/documents/chats/${chatId}/${documentId}/preview`, {
    credentials: 'include',
    headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : undefined
  });

  if (response.status === 401) {
    accessToken = await refreshAccessToken();
    response = await fetch(`${API_BASE_URL}/documents/chats/${chatId}/${documentId}/preview`, {
      credentials: 'include',
      headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : undefined
    });
  }

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed with status ${response.status}`);
  }

  const blob = await response.blob();
  const contentDisposition = response.headers.get('content-disposition') ?? '';
  const fileNameMatch = contentDisposition.match(/filename="?([^"]+)"?/i);

  return {
    url: window.URL.createObjectURL(blob),
    contentType: response.headers.get('content-type') ?? 'application/octet-stream',
    fileName: fileNameMatch?.[1] ?? `document-${documentId}`
  } satisfies DocumentPreviewRecord;
}

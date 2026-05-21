const ACCESS_TOKEN_KEY = 'access_token';

export function getAccessToken() {
  return window.localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function saveTokens(accessToken: string) {
  window.localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
}

export function setAccessToken(accessToken: string) {
  window.localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
}

export function clearToken() {
  window.localStorage.removeItem(ACCESS_TOKEN_KEY);
}

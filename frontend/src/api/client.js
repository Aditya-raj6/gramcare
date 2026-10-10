import axios from 'axios';

const TOKEN_KEY = 'hh_token';
const USER_KEY = 'hh_user';

/** Single HTTP client for the API gateway. All microservices are reached through /api. */
const api = axios.create({ baseURL: import.meta.env.VITE_API_BASE || '/api', timeout: 20000 });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !error.config.url.includes('/auth/login')) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      window.location.assign('/login');
    }
    return Promise.reject(error);
  },
);

export const errorMessage = (error) =>
  error?.response?.data?.message ||
  (error?.response?.status === 503 ? 'Service temporarily unavailable' : null) ||
  error?.message ||
  'Something went wrong';

export const session = {
  save(token, user) {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  },
  user() {
    try {
      return JSON.parse(localStorage.getItem(USER_KEY));
    } catch {
      return null;
    }
  },
  token: () => localStorage.getItem(TOKEN_KEY),
  clear() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },
};

export default api;

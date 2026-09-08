// Centralized backend base URL. Default to local development so the Vite app
// works out of the box, while allowing a production override via VITE_API_URL.
const defaultApiRoot = 'http://localhost:8080/api'
export const API_ROOT = (import.meta.env.VITE_API_URL || defaultApiRoot).replace(/\/+$/, '')
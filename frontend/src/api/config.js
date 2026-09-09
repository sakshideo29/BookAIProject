// Centralized backend base URL. Default to local development so the Vite app
// works out of the box, while allowing a production override via VITE_API_URL.
const defaultApiRoot = 'https://bookaiproject.onrender.com'
//export const API_ROOT = (import.meta.env.VITE_API_BASE_URL || defaultApiRoot).replace(/\/+$/, '')
export const API_ROOT = 'https://bookaiproject.onrender.com'
import axios from 'axios'
import { API_ROOT } from './config.js'

const API_BASE_URL = `${API_ROOT}/rag`

export async function askQuestion({ query, k = 5 }) {
    const response = await axios.post(`${API_BASE_URL}/ask`, { query, k })
    return response.data
}

export async function queryChunks({ query, k = 5 }) {
    const response = await axios.post(`${API_BASE_URL}/query`, { query, k })
    return response.data
}
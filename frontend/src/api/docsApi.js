import axios from 'axios'
import { API_ROOT } from './config.js'

const API_BASE_URL = `${API_ROOT}/docs`

/**
 * Uploads a PDF file for ingestion. Supports an onProgress callback
 * (0-100) since large books can take a while due to embedding rate limits.
 */
export async function ingestPdf({ file, source, chunkSize, onProgress }) {
    const formData = new FormData()
    formData.append('file', file)
    if (source) formData.append('source', source)
    if (chunkSize) formData.append('chunkSize', chunkSize)

    const response = await axios.post(`${API_BASE_URL}/ingest-pdf`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        timeout: 40 * 60 * 1000, // 40 min — large books can take a while due to embedding rate limits
        onUploadProgress: (event) => {
            if (onProgress && event.total) {
                // Note: this tracks upload of the file itself, not backend
                // embedding progress (which happens after upload completes and
                // can take much longer for large books).
                onProgress(Math.round((event.loaded * 100) / event.total))
            }
        },
    })
    return response.data
}

export async function ingestText({ text, source, chunkSize }) {
    const response = await axios.post(`${API_BASE_URL}/ingest`, {
        text,
        source,
        chunkSize,
    })
    return response.data
}
import { useState, useRef } from 'react'
import { ingestPdf } from '../api/docsApi.js'

export default function UploadBook() {
  const [file, setFile] = useState(null)
  const [source, setSource] = useState('')
  const [chunkSize, setChunkSize] = useState(500)
  const [uploadProgress, setUploadProgress] = useState(0)
  const [status, setStatus] = useState('idle') // idle | uploading | processing | done | error
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')
  const fileInputRef = useRef(null)

  function handleFileChange(e) {
    const selected = e.target.files?.[0]
    if (selected) {
      setFile(selected)
      setResult(null)
      setError('')
      setStatus('idle')
    }
  }

  function handleDrop(e) {
    e.preventDefault()
    const dropped = e.dataTransfer.files?.[0]
    if (dropped && dropped.type === 'application/pdf') {
      setFile(dropped)
      setResult(null)
      setError('')
      setStatus('idle')
    }
  }

  async function handleUpload(e) {
    e.preventDefault()
    if (!file) return

    setStatus('uploading')
    setUploadProgress(0)
    setError('')
    setResult(null)

    try {
      const data = await ingestPdf({
        file,
        source: source || undefined,
        chunkSize: Number(chunkSize) || undefined,
        onProgress: (pct) => {
          setUploadProgress(pct)
          if (pct === 100) setStatus('processing')
        },
      })
      setResult(data)
      setStatus('done')
    } catch (err) {
      setError(
        err.response?.data?.error || 'Upload failed. Is the backend running?'
      )
      setStatus('error')
    }
  }

  function reset() {
    setFile(null)
    setResult(null)
    setError('')
    setStatus('idle')
    setUploadProgress(0)
    if (fileInputRef.current) fileInputRef.current.value = ''
  }

  const isBusy = status === 'uploading' || status === 'processing'

  return (
    <div className="upload-panel">
      <form onSubmit={handleUpload}>
        <div
          className={`dropzone ${file ? 'dropzone-has-file' : ''}`}
          onDragOver={(e) => e.preventDefault()}
          onDrop={handleDrop}
          onClick={() => fileInputRef.current?.click()}
        >
          <input
            ref={fileInputRef}
            type="file"
            accept="application/pdf"
            onChange={handleFileChange}
            hidden
          />
          {file ? (
            <p>📄 {file.name} <span className="muted">({(file.size / 1024 / 1024).toFixed(1)} MB)</span></p>
          ) : (
            <p>Click or drag a PDF here to upload</p>
          )}
        </div>

        <div className="form-row form-row-inline">
          <div>
            <label>Source label (optional)</label>
            <input
              type="text"
              placeholder="e.g. the-alchemist"
              value={source}
              onChange={(e) => setSource(e.target.value)}
            />
          </div>
          <div>
            <label>Chunk size</label>
            <input
              type="number"
              min="100"
              max="2000"
              value={chunkSize}
              onChange={(e) => setChunkSize(e.target.value)}
            />
          </div>
        </div>

        <button type="submit" disabled={!file || isBusy}>
          {status === 'uploading' && `Uploading... ${uploadProgress}%`}
          {status === 'processing' && 'Processing (embedding chunks, this can take a while)...'}
          {(status === 'idle' || status === 'done' || status === 'error') && 'Upload & Ingest PDF'}
        </button>

        {status === 'processing' && (
          <p className="muted small-note">
            Large books can take several minutes to embed due to API rate limits — please don't close this tab.
          </p>
        )}
      </form>

      {error && <p className="error">{error}</p>}

      {result && (
        <div className="upload-result">
          <p>✅ Ingested <strong>{result.chunks}</strong> chunks from <strong>{result.source}</strong></p>
          <p className="muted">{result.extractedChars} characters extracted</p>
          <button className="secondary-btn" onClick={reset}>Upload another</button>
        </div>
      )}
    </div>
  )
}
import { useState } from 'react'
import { askQuestion } from '../api/ragApi.js'

export default function AskQuestion() {
    const [query, setQuery] = useState('')
    const [k, setK] = useState(5)
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')
    const [history, setHistory] = useState([]) // [{ query, answer }]

    async function handleSubmit(e) {
        e.preventDefault()
        if (!query.trim()) return

        setLoading(true)
        setError('')

        try {
            const data = await askQuestion({ query, k: Number(k) })
            setHistory((prev) => [{ query: data.query, answer: data.answer }, ...prev])
            setQuery('')
        } catch (err) {
            setError(
                err.response?.data?.error || 'Something went wrong. Is the backend running?'
            )
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="ask-panel">
            <form className="ask-form" onSubmit={handleSubmit}>
                <div className="form-row">
                    <label>Ask a question about your ingested books</label>
                    <textarea
                        rows={3}
                        placeholder="e.g. What is the main theme of the story?"
                        value={query}
                        onChange={(e) => setQuery(e.target.value)}
                    />
                </div>

                <div className="form-row form-row-inline">
                    <div>
                        <label>Chunks to retrieve (k)</label>
                        <input
                            type="number"
                            min="1"
                            max="10"
                            value={k}
                            onChange={(e) => setK(e.target.value)}
                        />
                    </div>
                </div>

                <button type="submit" disabled={loading || !query.trim()}>
                    {loading ? 'Thinking...' : 'Ask'}
                </button>
            </form>

            {error && <p className="error">{error}</p>}

            {history.length > 0 && (
                <div className="ask-history">
                    {history.map((item, idx) => (
                        <div className="ask-history-item" key={idx}>
                            <p className="ask-q">🙋 {item.query}</p>
                            <p className="ask-a">🤖 {item.answer}</p>
                        </div>
                    ))}
                </div>
            )}
        </div>
    )
}
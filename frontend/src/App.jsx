import { useState } from 'react'
import SearchForm from './components/SearchForm.jsx'
import ResultsGrid from './components/ResultsGrid.jsx'
import { getRecommendations } from './api/bookApi.js'

export default function App() {
  const [books, setBooks] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function handleSearch(criteria) {
    setLoading(true)
    setError('')
    setBooks([])
    try {
      const results = await getRecommendations(criteria)
      setBooks(results)
    } catch (err) {
      setError(
        err.response?.data?.error || 'Something went wrong. Is the backend running?'
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="app">
      <header>
        <h1>📚 BookAI</h1>
        <p>Get book recommendations by genre, author, or mood</p>
      </header>

      <SearchForm onSearch={handleSearch} loading={loading} />

      {error && <p className="error">{error}</p>}

      <ResultsGrid books={books} />
    </div>
  )
}

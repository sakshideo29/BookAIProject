import { useState } from 'react'
import SearchForm from './components/SearchForm.jsx'
import ResultsGrid from './components/ResultsGrid.jsx'
import UploadBook from './components/UploadBook.jsx'
import AskQuestion from './components/AskQuestion.jsx'
import { getRecommendations } from './api/bookApi.js'

const TABS = [
  { id: 'recommend', label: '📚 Recommendations' },
  { id: 'upload', label: '⬆️ Upload Book' },
  { id: 'ask', label: '💬 Ask a Question' },
]

export default function App() {
  const [activeTab, setActiveTab] = useState('recommend')
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
          <p>Get recommendations, upload books, or ask questions about them</p>
        </header>

        <nav className="tabs">
          {TABS.map((tab) => (
              <button
                  key={tab.id}
                  className={`tab-btn ${activeTab === tab.id ? 'tab-btn-active' : ''}`}
                  onClick={() => setActiveTab(tab.id)}
                  type="button"
              >
                {tab.label}
              </button>
          ))}
        </nav>

        {activeTab === 'recommend' && (
            <>
              <SearchForm onSearch={handleSearch} loading={loading} />
              {error && <p className="error">{error}</p>}
              <ResultsGrid books={books} />
            </>
        )}

        {activeTab === 'upload' && <UploadBook />}

        {activeTab === 'ask' && <AskQuestion />}
      </div>
  )
}
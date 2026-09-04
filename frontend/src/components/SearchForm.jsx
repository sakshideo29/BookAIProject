import { useState } from 'react'

export default function SearchForm({ onSearch, loading }) {
  const [genre, setGenre] = useState('')
  const [author, setAuthor] = useState('')
  const [mood, setMood] = useState('')
  const [count, setCount] = useState(5)
  const [minutesPerDay, setMinutesPerDay] = useState(30)

  function handleSubmit(e) {
    e.preventDefault()
    onSearch({ genre, author, mood, count: Number(count), minutesPerDay: Number(minutesPerDay) })
  }

  return (
    <form className="search-form" onSubmit={handleSubmit}>
      <div className="form-row">
        <label>Genre</label>
        <input
          type="text"
          placeholder="e.g. Fantasy, Thriller, Sci-Fi"
          value={genre}
          onChange={(e) => setGenre(e.target.value)}
        />
      </div>

      <div className="form-row">
        <label>Favorite Author</label>
        <input
          type="text"
          placeholder="e.g. Haruki Murakami"
          value={author}
          onChange={(e) => setAuthor(e.target.value)}
        />
      </div>

      <div className="form-row">
        <label>Current Mood</label>
        <input
          type="text"
          placeholder="e.g. cozy rainy day, need motivation"
          value={mood}
          onChange={(e) => setMood(e.target.value)}
        />
      </div>

      <div className="form-row form-row-inline">
        <div>
          <label>Number of books</label>
          <input
            type="number"
            min="1"
            max="10"
            value={count}
            onChange={(e) => setCount(e.target.value)}
          />
        </div>
        <div>
          <label>Minutes you read/day</label>
          <input
            type="number"
            min="5"
            max="600"
            value={minutesPerDay}
            onChange={(e) => setMinutesPerDay(e.target.value)}
          />
        </div>
      </div>

      <button type="submit" disabled={loading}>
        {loading ? 'Finding books...' : 'Get Recommendations'}
      </button>
    </form>
  )
}

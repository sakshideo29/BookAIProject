import BookCard from './BookCard.jsx'

export default function ResultsGrid({ books }) {
  if (!books || books.length === 0) return null

  return (
    <div className="results-grid">
      {books.map((book, idx) => (
        <BookCard key={`${book.title}-${idx}`} book={book} />
      ))}
    </div>
  )
}

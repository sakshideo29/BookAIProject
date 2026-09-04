export default function BookCard({ book }) {
  return (
    <div className="book-card">
      <h3>{book.title}</h3>
      <p className="author">by {book.author}</p>
      {book.genre && <span className="genre-tag">{book.genre}</span>}
      <p className="summary">{book.shortSummary}</p>
      <div className="book-meta">
        <span>{book.pageCount} pages</span>
        <span>
          ~{book.estimatedReadingDays} day{book.estimatedReadingDays === 1 ? '' : 's'} to finish
        </span>
        <span>{book.estimatedReadingHours}h total reading</span>
      </div>
    </div>
  )
}

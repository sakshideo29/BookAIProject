import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api/books'

export async function getRecommendations({ genre, author, mood, count, minutesPerDay }) {
  const response = await axios.post(`${API_BASE_URL}/recommend`, {
    genre,
    author,
    mood,
    count,
    minutesPerDay,
  })
  return response.data
}

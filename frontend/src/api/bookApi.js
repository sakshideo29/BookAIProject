import axios from 'axios'
import { API_ROOT } from './config.js'

const API_BASE_URL = `${API_ROOT}/api/books`

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
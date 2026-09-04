# BookAI

AI-powered book recommendation app. Suggests books by genre, favorite author, or current
mood — returns title, page count, a short summary, and a predicted number of days to
finish reading, calculated from your own reading speed.

- **Frontend:** React (Vite) + Axios
- **Backend:** Java 21, Spring Boot 3, WebClient, Gemini API
- **AI:** Google Gemini (`gemini-2.0-flash`)

## Project structure

```
bookai/
├── backend/     Spring Boot REST API
└── frontend/    React app (Vite)
```

## 1. Backend setup

Requires Java 21 and Maven.

```bash
cd backend

# Get a free Gemini API key: https://aistudio.google.com/app/apikey
export GEMINI_API_KEY=your_key_here

./mvnw spring-boot:run
```

Runs on `http://localhost:8080`. Health check: `GET /api/books/health`.

### API

`POST /api/books/recommend`

```json
{
  "genre": "Fantasy",
  "author": "Brandon Sanderson",
  "mood": "need something epic",
  "count": 5,
  "minutesPerDay": 30
}
```

All fields are optional except `count` and `minutesPerDay` (which default to 5 and 30 if omitted).

Response:

```json
[
  {
    "title": "Mistborn: The Final Empire",
    "author": "Brandon Sanderson",
    "pageCount": 541,
    "genre": "Fantasy",
    "shortSummary": "...",
    "estimatedReadingHours": 25,
    "estimatedReadingDays": 50.0
  }
]
```

## 2. Frontend setup

Requires Node.js 18+.

```bash
cd frontend
npm install
npm run dev
```

Runs on `http://localhost:5173` and calls the backend at `http://localhost:8080`.

## Notes / things you said you'll handle yourself

- **Caching** of repeated genre/mood queries — not implemented yet, add at the
  `BookRecommendationService` layer (e.g. Spring `@Cacheable` or Redis).
- **Deployment** — not configured. Suggested: backend on AWS Elastic Beanstalk/ECS,
  frontend on S3 + CloudFront.

## Why reading time isn't from the AI

`estimatedReadingDays` and `estimatedReadingHours` are computed in
`ReadingTimeCalculator.java` from page count and your reading speed — never asked from
the LLM. LLMs are unreliable at arithmetic-style estimation; this keeps the number
trustworthy and lets you later make reading speed a user-adjustable setting.

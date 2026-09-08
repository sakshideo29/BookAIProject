-- Flyway migration: create documents table for PGVector
-- Gemini gemini-embedding-001 produces 768-dimensional vectors.

CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS documents (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  source text,
  chunk_index integer,
  content text,
  content_hash text UNIQUE,
  metadata jsonb,
  embedding vector(768)
);

-- HNSW index for cosine similarity (pgvector >= 0.6.0)
CREATE INDEX IF NOT EXISTS documents_embedding_hnsw_idx
  ON documents USING hnsw (embedding vector_cosine_ops) WITH (m=16, ef_construction=200);

-- Index on content_hash for fast dedupe checks
CREATE UNIQUE INDEX IF NOT EXISTS documents_content_hash_idx ON documents(content_hash);

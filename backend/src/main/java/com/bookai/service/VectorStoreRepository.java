package com.bookai.service;

import com.bookai.dto.DocumentChunk;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class VectorStoreRepository {

    private final JdbcTemplate jdbcTemplate;

    public VectorStoreRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Upsert a single chunk (idempotent by content_hash). Uses raw SQL and casts the
     * embedding parameter to the Postgres vector type. This implementation expects
     * the pgvector extension to be installed and the documents table to exist.
     */
    public void upsertChunk(DocumentChunk chunk) {
        String sql = "INSERT INTO documents (id, source, chunk_index, content, content_hash, metadata, embedding) " +
                "VALUES (?, ?, ?, ?, ?, ?::jsonb, ?::vector) " +
                "ON CONFLICT (content_hash) DO UPDATE SET content = EXCLUDED.content, metadata = EXCLUDED.metadata, embedding = EXCLUDED.embedding";

        // convert embedding to vector literal like '[1,2,3]'
        String vecLiteral = toVectorLiteral(chunk.getEmbedding());

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setObject(1, chunk.getId());
            ps.setString(2, chunk.getSource());
            ps.setInt(3, chunk.getChunkIndex());
            ps.setString(4, chunk.getContent());
            ps.setString(5, chunk.getContentHash());
            ps.setString(6, JsonUtils.toJson(chunk.getMetadata()));
            ps.setString(7, vecLiteral);
            return ps;
        });
    }

    /**
     * Query top-k nearest neighbors. Note: the similarity operator used below is <-> (L2).
     * For cosine use the appropriate operator (vector_cosine_ops) or adjust SQL depending
     * on your pgvector version. This is intentionally a simple approach that can be
     * tuned for your deployment.
     */
    public List<DocumentChunk> queryNearest(double[] queryEmbedding, int k) {
        String sql = "SELECT id, source, chunk_index, content, content_hash, metadata FROM documents " +
                "ORDER BY embedding <-> ?::vector LIMIT ?"; // '<->' is euclidean distance operator

        String vecLiteral = toVectorLiteral(queryEmbedding);

        return jdbcTemplate.query(sql, new Object[]{vecLiteral, k}, new DocumentChunkRowMapper());
    }

    private String toVectorLiteral(double[] embedding) {
        if (embedding == null) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(embedding[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    private static class DocumentChunkRowMapper implements RowMapper<DocumentChunk> {
        @Override
        public DocumentChunk mapRow(ResultSet rs, int rowNum) throws SQLException {
            DocumentChunk c = new DocumentChunk();
            c.setId(UUID.fromString(rs.getString("id")));
            c.setSource(rs.getString("source"));
            c.setChunkIndex(rs.getInt("chunk_index"));
            c.setContent(rs.getString("content"));
            c.setContentHash(rs.getString("content_hash"));
            // metadata and embedding omitted in query; embedding can be fetched separately if needed
            return c;
        }
    }
}

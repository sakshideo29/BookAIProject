package com.bookai.service;

import com.bookai.dto.DocumentChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final EmbeddingService embeddingService;
    private final VectorStoreRepository vectorStoreRepository;

    /**
     * Chunks the given text, embeds each chunk, and stores it (idempotent via
     * content_hash upsert). Shared by both plain-text ingestion and PDF ingestion,
     * so both paths behave identically once text is in hand.
     *
     * @return number of chunks stored
     */
    public int chunkEmbedAndStore(String text, String source, int chunkSize) {
        List<String> chunks = chunkText(text, chunkSize);
        int stored = 0;

        for (int i = 0; i < chunks.size(); i++) {
            String chunkText = chunks.get(i);
            DocumentChunk chunk = new DocumentChunk();
            chunk.setId(UUID.randomUUID());
            chunk.setSource(source);
            chunk.setChunkIndex(i);
            chunk.setContent(chunkText);
            chunk.setContentHash(DigestUtils.sha256Hex(chunkText));
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", source);
            metadata.put("chunkIndex", i);
            chunk.setMetadata(metadata);
            chunk.setEmbedding(embeddingService.embed(chunkText));
            vectorStoreRepository.upsertChunk(chunk);
            stored++;
        }

        log.info("Ingested {} chunks from source '{}'", stored, source);
        return stored;
    }

    private List<String> chunkText(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        String normalized = text.replace("\r\n", "\n").trim();
        if (normalized.isEmpty()) {
            return chunks;
        }

        String[] paragraphs = normalized.split("\\n\\s*\\n");
        StringBuilder buffer = new StringBuilder();

        for (String paragraph : paragraphs) {
            String cleaned = paragraph.trim();
            if (cleaned.isEmpty()) {
                continue;
            }
            if (buffer.length() > 0 && buffer.length() + cleaned.length() + 1 > chunkSize) {
                chunks.add(buffer.toString().trim());
                buffer = new StringBuilder();
            }
            if (buffer.length() > 0) {
                buffer.append("\n");
            }
            buffer.append(cleaned);
        }

        if (buffer.length() > 0) {
            chunks.add(buffer.toString().trim());
        }

        if (chunks.isEmpty()) {
            chunks.add(normalized);
        }
        return chunks;
    }
}

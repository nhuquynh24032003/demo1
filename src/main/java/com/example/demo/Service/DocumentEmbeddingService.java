    package com.example.demo.Service;

    import com.example.demo.Model.DocumentDetailEmbedding;
    import com.example.demo.Model.DocumentDetailEmbeddingES;
    import com.example.demo.Model.LegalDocumentDetail;
    import com.example.demo.Repository.DocumentDetailEmbeddingESRepository;
    import com.example.demo.Repository.DocumentDetailEmbeddingRepository;
    import com.example.demo.Repository.LegalDocumentDetailRepository;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import com.fasterxml.jackson.core.JsonProcessingException;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.stream.IntStream;

    @Service
    public class DocumentEmbeddingService {
        @Autowired
        private LegalDocumentDetailRepository legalDocumentDetailRepository;

        @Autowired
        private DocumentDetailEmbeddingRepository documentDetailEmbeddingRepository;
        @Autowired
        DocumentDetailEmbeddingESRepository elasticsearchRepository;
        @Autowired
        private OllamaEmbeddingService embeddingService;
        @Autowired
        private ObjectMapper objectMapper;
        @Transactional
        public void processAndSaveEmbeddings(Long documentId) {
            LegalDocumentDetail document = legalDocumentDetailRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            List<String> chunks = TextChunker.splitText(document.getContent(), 300);

            List<DocumentDetailEmbedding> embeddings = new ArrayList<>();
            List<DocumentDetailEmbeddingES> esEmbeddings = new ArrayList<>();

            IntStream.range(0, chunks.size()).forEach(index -> {
                try {
                    List<Double> embeddingVector = embeddingService.getEmbedding(chunks.get(index));
                    String embeddingJson = objectMapper.writeValueAsString(embeddingVector);

                    // Lưu vào MySQL
                    DocumentDetailEmbedding embeddingEntity = new DocumentDetailEmbedding();
                    embeddingEntity.setDocumentId(documentId);
                    embeddingEntity.setChunkIndex(index);
                    embeddingEntity.setChunkText(chunks.get(index));
                    embeddingEntity.setEmbedding(embeddingJson);
                    embeddings.add(embeddingEntity);

                    // Lưu vào Elasticsearch
                    DocumentDetailEmbeddingES esEntity = new DocumentDetailEmbeddingES();
                    esEntity.setId(documentId + "-" + index); // ID duy nhất
                    esEntity.setDocumentId(documentId);
                    esEntity.setChunkIndex(index);
                    esEntity.setChunkText(chunks.get(index));
                    esEntity.setEmbedding(embeddingVector);
                    esEmbeddings.add(esEntity);

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });

            // Lưu vào MySQL
            documentDetailEmbeddingRepository.saveAll(embeddings);

            // Lưu vào Elasticsearch
            elasticsearchRepository.saveAll(esEmbeddings);
        }


        @Transactional
        public void processAndSaveEmbeddings2(Long documentId) {
            LegalDocumentDetail document = legalDocumentDetailRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            // Lấy nội dung gốc
            String content = document.getContent().trim();

            // Kiểm tra nội dung có hợp lệ không
            if (content.isEmpty() || "Không tìm thấy nội dung".equalsIgnoreCase(content)) {
                System.out.println("⚠ Bỏ qua documentId: " + documentId + " vì nội dung trống hoặc không hợp lệ.");
                return;
            }

            try {
                // Lấy embedding cho toàn bộ nội dung gốc
                List<Double> embeddingVector = embeddingService.getEmbedding(content);
                String embeddingJson = objectMapper.writeValueAsString(embeddingVector);

                // Lưu vào Elasticsearch
                DocumentDetailEmbeddingES esEntity = new DocumentDetailEmbeddingES();
                esEntity.setId(documentId.toString()); // ID duy nhất
                esEntity.setDocumentId(documentId);
                esEntity.setEmbedding(embeddingVector);
                esEntity.setContent(content); // Lưu nội dung gốc vào Elasticsearch
                elasticsearchRepository.save(esEntity);

                System.out.println("✅ Đã lưu thành công documentId: " + documentId);

            } catch (Exception e) {
                System.err.println("❌ Lỗi xử lý documentId: " + documentId + " - " + e.getMessage());
                e.printStackTrace();
            }
        }


        public static class TextChunker {
            public static List<String> splitText(String text, int chunkSize) {
                List<String> chunks = new ArrayList<>();
                String[] words = text.split("\\s+");
                StringBuilder chunk = new StringBuilder();

                for (String word : words) {
                    if (chunk.length() + word.length() > chunkSize) {
                        chunks.add(chunk.toString().trim());
                        chunk = new StringBuilder();
                    }
                    chunk.append(word).append(" ");
                }
                if (!chunk.isEmpty()) {
                    chunks.add(chunk.toString().trim());
                }
                return chunks;
            }
        }
    }

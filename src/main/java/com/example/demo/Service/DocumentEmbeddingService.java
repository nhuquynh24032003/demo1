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
    import java.util.regex.Matcher;
    import java.util.regex.Pattern;
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
            // Lấy nội dung gốc
            String content = document.getContent().trim();

            // Kiểm tra nội dung có hợp lệ không
            if (content.isEmpty() || "Không tìm thấy nội dung".equalsIgnoreCase(content)) {
                System.out.println("⚠ Bỏ qua documentId: " + documentId + " vì nội dung trống hoặc không hợp lệ.");
                return;
            }
            List<String> chunks = TextChunker.splitText(document.getContent());

            //List<DocumentDetailEmbedding> embeddings = new ArrayList<>();
            List<DocumentDetailEmbeddingES> esEmbeddings = new ArrayList<>();

            IntStream.range(0, chunks.size()).forEach(index -> {
                List<Double> embeddingVector = embeddingService.getEmbedding(chunks.get(index));
                // Lưu vào Elasticsearch
                DocumentDetailEmbeddingES esEntity = new DocumentDetailEmbeddingES();
                esEntity.setId(documentId + "-" + index); // ID duy nhất
                esEntity.setDocumentId(documentId);
                esEntity.setChunkIndex(index);
                esEntity.setChunkText(chunks.get(index));
                esEntity.setEmbedding(embeddingVector);
                esEntity.setTitle(document.getTitle());
                esEmbeddings.add(esEntity);

            });

            // Lưu vào MySQL
          //  documentDetailEmbeddingRepository.saveAll(embeddings);

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
                esEntity.setTitle(document.getTitle());
                esEntity.setContent(content); // Lưu nội dung gốc vào Elasticsearch
                elasticsearchRepository.save(esEntity);

                System.out.println("✅ Đã lưu thành công documentId: " + documentId);

            } catch (Exception e) {
                System.err.println("❌ Lỗi xử lý documentId: " + documentId + " - " + e.getMessage());
                e.printStackTrace();
            }
        }


        public static class TextChunker {
            public static List<String> splitText(String text) {
                List<String> chunks = new ArrayList<>();

                Pattern pattern = Pattern.compile(
                        "(Chương \\w+|Mục \\w+|Điều \\d+:.*?|Khoản \\d+.*?|Điểm [a-z]+.*?)" +
                                "(?=(Chương \\w+|Mục \\w+|Điều \\d+:|Khoản \\d+|Điểm [a-z]+|$))",
                        Pattern.DOTALL
                );

                Matcher matcher = pattern.matcher(text);

                while (matcher.find()) {
                    chunks.add(matcher.group(1).trim());
                }

                return chunks;
            }

        }
    }

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
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestBody;

    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.Collections;
    import java.util.List;
    import java.util.regex.Matcher;
    import java.util.regex.Pattern;
    import java.util.stream.IntStream;

    @Service
    public class DocumentEmbeddingService {
        @Autowired
        private LegalDocumentDetailRepository legalDocumentDetailRepository;
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
                List<Double> embeddingTitleVector = embeddingService.getEmbedding(document.getTitle());
                // Lưu vào Elasticsearch
                DocumentDetailEmbeddingES esEntity = new DocumentDetailEmbeddingES();
                esEntity.setId(documentId + "-" + index); // ID duy nhất
                esEntity.setDocumentId(documentId);
                esEntity.setChunkIndex(index);
                esEntity.setChunkText(chunks.get(index));
                esEntity.setEmbedding(embeddingVector);
                esEntity.setEmbedding_title(embeddingTitleVector);
                esEntity.setTitle(document.getTitle());
                esEmbeddings.add(esEntity);

            });


            elasticsearchRepository.saveAll(esEmbeddings);
        }

        public static class TextChunker {
            public static List<String> splitText(String text) {
                List<String> chunks = new ArrayList<>();

                String[] words = text.split("\\s+"); // Tách theo dấu cách
                StringBuilder currentChunk = new StringBuilder();
                int count = 0;

                for (String word : words) {
                    currentChunk.append(word).append(" ");
                    count++;
                    if (count >= 300) {
                        chunks.add(currentChunk.toString().trim());
                        currentChunk = new StringBuilder();
                        count = 0;
                    }
                }
                if (!currentChunk.isEmpty()) {
                    chunks.add(currentChunk.toString().trim());
                }
                return chunks;
            }



            public static List<String> splitLawText(String text) {
                List<String> resultChunks = new ArrayList<>();

                // Regex nhận diện các mục quan trọng
                String chapterRegex = "(Chương \\w+\\.)";
                String articleRegex = "(Điều \\d+\\.)";
                String clauseRegex = "(\\(\\d+\\))";  // Khoản
                String pointRegex = "(\\([a-z]\\))";  // Điểm
                String sectionRegex = "(\\d+\\.)";   // Mục lớn "1.", "2.", "3."
                String subsectionRegex = "(\\d+\\.\\d+)"; // Tiểu mục "5.1.", "5.2."

                // Tìm vị trí các mục trong văn bản
                List<Integer> chapterPositions = findPositions(text, chapterRegex);
                List<Integer> sectionPositions = findPositions(text, sectionRegex);
                List<Integer> articlePositions = findPositions(text, articleRegex);
                List<Integer> subsectionPositions = findPositions(text, subsectionRegex);

                // Gom tất cả vị trí lại để chia đoạn
                List<Integer> allPositions = new ArrayList<>();
                allPositions.addAll(chapterPositions);
                allPositions.addAll(sectionPositions);
                allPositions.addAll(articlePositions);
                allPositions.addAll(subsectionPositions);
                Collections.sort(allPositions);

                // Nếu không có mục lớn, lấy toàn bộ văn bản
                if (allPositions.isEmpty()) {
                    return splitBySentence(text, 300);
                }

                // Tách văn bản theo các mục tìm được
                for (int i = 0; i < allPositions.size(); i++) {
                    int start = allPositions.get(i);
                    int end = (i + 1 < allPositions.size()) ? allPositions.get(i + 1) : text.length();
                    String chunk = text.substring(start, end).trim();

                    if (chunk.length() > 500) {
                        resultChunks.addAll(splitBySentence(chunk, 300));
                    } else {
                        resultChunks.add(chunk);
                    }
                }

                return resultChunks;
            }

            public static List<Integer> findPositions(String text, String regex) {
                List<Integer> positions = new ArrayList<>();
                Matcher matcher = Pattern.compile(regex).matcher(text);
                while (matcher.find()) {
                    positions.add(matcher.start());
                }
                return positions;
            }

            public static List<String> splitBySentence(String text, int maxLen) {
                List<String> result = new ArrayList<>();
                String[] sentences = text.split("(?<=[.!?])\\s+");
                StringBuilder chunk = new StringBuilder();

                for (String sentence : sentences) {
                    if (chunk.length() + sentence.length() > maxLen) {
                        result.add(chunk.toString().trim());
                        chunk.setLength(0);
                    }
                    chunk.append(sentence).append(" ");
                }

                if (!chunk.isEmpty()) {
                    result.add(chunk.toString().trim());
                }
                return result;
            }
            }
    }

package com.njbank.ticket.service;

import com.njbank.ticket.dto.SimilarTicketDTO;
import com.njbank.ticket.entity.Ticket;
import com.njbank.ticket.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SimilarityService {

    private final TicketRepository ticketRepository;
    private Map<String, Double> idfCache = new HashMap<>();
    private Set<String> vocabulary = new HashSet<>();

    public SimilarityService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * 查找相似工单
     */
    public List<SimilarTicketDTO> findSimilarTickets(String title, int topN) {
        List<Ticket> allTickets = ticketRepository.findAll();
        
        if (allTickets.isEmpty()) {
            return Collections.emptyList();
        }

        // 构建词汇表和IDF
        buildVocabularyAndIDF(allTickets);

        // 计算输入标题的TF-IDF向量
        Map<String, Double> inputTfIdf = computeTfIdf(title);

        // 计算每个工单的相似度
        List<SimilarTicketDTO> similarTickets = new ArrayList<>();
        for (Ticket ticket : allTickets) {
            String ticketText = ticket.getTitle() + " " + (ticket.getDescription() != null ? ticket.getDescription() : "");
            Map<String, Double> ticketTfIdf = computeTfIdf(ticketText);
            
            double similarity = cosineSimilarity(inputTfIdf, ticketTfIdf);
            
            if (similarity > 0.1) { // 相似度阈值
                similarTickets.add(SimilarTicketDTO.builder()
                        .id(ticket.getId())
                        .ticketNo(ticket.getTicketNo())
                        .title(ticket.getTitle())
                        .type(ticket.getType())
                        .status(ticket.getStatus())
                        .similarity(similarity)
                        .solution("根据历史工单分析，建议检查账户状态并核实交易记录")
                        .build());
            }
        }

        // 按相似度排序并返回TopN
        return similarTickets.stream()
                .sorted(Comparator.comparingDouble(SimilarTicketDTO::getSimilarity).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * 构建词汇表和IDF值
     */
    private void buildVocabularyAndIDF(List<Ticket> tickets) {
        vocabulary.clear();
        idfCache.clear();

        // 统计每个词出现的文档数
        Map<String, Integer> docFrequency = new HashMap<>();
        int totalDocs = tickets.size();

        for (Ticket ticket : tickets) {
            String text = ticket.getTitle() + " " + (ticket.getDescription() != null ? ticket.getDescription() : "");
            Set<String> words = new HashSet<>(tokenize(text));
            vocabulary.addAll(words);
            
            for (String word : words) {
                docFrequency.put(word, docFrequency.getOrDefault(word, 0) + 1);
            }
        }

        // 计算IDF
        for (String word : vocabulary) {
            int freq = docFrequency.getOrDefault(word, 0);
            double idf = Math.log((double) totalDocs / (freq + 1));
            idfCache.put(word, idf);
        }
    }

    /**
     * 计算TF-IDF向量
     */
    private Map<String, Double> computeTfIdf(String text) {
        List<String> words = tokenize(text);
        Map<String, Double> tf = computeTF(words);
        Map<String, Double> tfIdf = new HashMap<>();

        for (String word : tf.keySet()) {
            double idf = idfCache.getOrDefault(word, 0.0);
            tfIdf.put(word, tf.get(word) * idf);
        }

        return tfIdf;
    }

    /**
     * 计算词频TF
     */
    private Map<String, Double> computeTF(List<String> words) {
        Map<String, Integer> termCount = new HashMap<>();
        for (String word : words) {
            termCount.put(word, termCount.getOrDefault(word, 0) + 1);
        }

        Map<String, Double> tf = new HashMap<>();
        int totalWords = words.size();
        for (Map.Entry<String, Integer> entry : termCount.entrySet()) {
            tf.put(entry.getKey(), (double) entry.getValue() / totalWords);
        }

        return tf;
    }

    /**
     * 计算余弦相似度
     */
    private double cosineSimilarity(Map<String, Double> vec1, Map<String, Double> vec2) {
        // 获取所有词的并集
        Set<String> allWords = new HashSet<>();
        allWords.addAll(vec1.keySet());
        allWords.addAll(vec2.keySet());

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String word : allWords) {
            double v1 = vec1.getOrDefault(word, 0.0);
            double v2 = vec2.getOrDefault(word, 0.0);
            
            dotProduct += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 分词（简单实现，按空格和标点符号分割）
     */
    private List<String> tokenize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 转小写，移除标点符号和特殊符号，按空格分割
        // 使用兼容Java 8的正则表达式
        String cleaned = text.toLowerCase()
                .replaceAll("[^\\p{L}\\p{N}\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        // 对于中文，按字符分割（简单实现）
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                // 对于中文，每个字符作为一个词
                if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                    tokens.add(String.valueOf(c));
                }
            }
        }

        // 对于英文，按空格分割
        String[] words = cleaned.split("\\s+");
        for (String word : words) {
            if (word.length() > 1 && !isStopWord(word)) {
                tokens.add(word);
            }
        }

        return tokens;
    }

    /**
     * 停用词列表
     */
    private boolean isStopWord(String word) {
        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "的", "了", "是", "在", "我", "有", "和", "就", "不", "人", "都", "一", "一个", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好", "自己", "这",
                "the", "a", "an", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "do", "does", "did", "will", "would", "should", "could", "may", "might", "must", "can"
        ));
        return stopWords.contains(word);
    }
}

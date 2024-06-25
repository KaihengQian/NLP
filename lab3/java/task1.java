import com.huaban.analysis.jieba.JiebaSegmenter;

import java.io.*;
import java.util.*;

public class task1 {
    public static void main(String[] args) throws IOException {
        List<Set<String>> allWords = new ArrayList<>();

        for (int docID = 1; docID < 21; docID++) {
            String textFilePath = "E:\\Code\\NLP\\Lab3\\lab3\\src\\main\\resources\\article\\" + docID + ".txt";
            List<String> sentences = readTextFile(textFilePath);
            Set<String> words = tokenize(sentences);
            allWords.add(words);
        }

        Map<String, List<Integer>> invertedIndex = buildInvertedIndex(allWords);
        String indexFilePath = "E:\\Code\\NLP\\Lab3\\lab3\\src\\main\\resources\\result\\inverted_index.txt";
        saveInvertedIndex(invertedIndex, indexFilePath);
        String frequencyFilePath = "E:\\Code\\NLP\\Lab3\\lab3\\src\\main\\resources\\result\\document_frequency.txt";
        saveDocumentFrequency(invertedIndex, frequencyFilePath);
    }

    public static List<String> readTextFile(String filePath) throws IOException {
        List<String> text = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim(); // 去除句子两端的空格
            if (!line.isEmpty()) {
                text.add(line);
            }
        }
        reader.close();

        return text;
    }

    public static Set<String> tokenize(List<String> sentences) {
        Set<String> words = new HashSet<>();
        JiebaSegmenter segmenter = new JiebaSegmenter();

        for (String sentence : sentences) {
            List<String> tokens = segmenter.sentenceProcess(sentence);
            for (String token : tokens) {
                token = token.replaceAll("[\\p{Punct}\\p{IsPunctuation}]", ""); // 去除标点符号
                token = token.replaceAll("\\d", ""); // 去除数字
                if (token.length() > 1) { // 去除单字
                    words.add(token);
                }
            }
        }

        return words;
    }

    public static Map<String, List<Integer>> buildInvertedIndex(List<Set<String>> articles) {
        Map<String, List<Integer>> invertedIndex = new HashMap<>();

        // 顺序访问每篇文章的分词结果，得到的倒排索引即已按照docID排好序
        int docID = 1;
        for (Set<String> article : articles) {
            for (String word : article) {
                // 如果倒排记录表中不存在该词，则为其先创建一个新的列表
                invertedIndex.computeIfAbsent(word, k -> new ArrayList<>()).add(docID);
            }
            docID++;
        }

        return invertedIndex;
    }

    public static void saveInvertedIndex(Map<String, List<Integer>> invertedIndex, String filepath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            for (Map.Entry<String, List<Integer>> entry : invertedIndex.entrySet()) {
                String word = entry.getKey();
                List<Integer> docIDs = entry.getValue();
                String line = word + ": " + docIDs.toString();
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveDocumentFrequency(Map<String, List<Integer>> invertedIndex, String filepath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            for (Map.Entry<String, List<Integer>> entry : invertedIndex.entrySet()) {
                String word = entry.getKey();
                List<Integer> docIDs = entry.getValue();
                String line = word + ": " + docIDs.size();
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

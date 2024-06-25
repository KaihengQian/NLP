package main.java;

import java.io.*;
import java.util.*;

public class task2 {
    public static void main(String[] args) {
        String segmentedWordsFilePath = "E:\\Code\\NLP\\Lab\\lab1\\src\\main\\resources\\result\\word_segmentation.txt";
        String stopWordsFilePath = "E:\\Code\\NLP\\Lab\\lab1\\src\\main\\resources\\data\\cn_stopwords.txt";
        String resultFilePath = "E:\\Code\\NLP\\Lab\\lab1\\src\\main\\resources\\result\\high_frequency.txt";
        int frequenciesNumber = 20; // 输出高频项的数量

        try {
            Set<String> stopWords = readStopWordsFile(stopWordsFilePath);
            List<List<String>> segmentedWords = readSegmentedWordsFile(segmentedWordsFilePath, stopWords);
            List<Map.Entry<String, Double>> wordFrequencies = countWords(segmentedWords);
            BufferedWriter writer = new BufferedWriter(new FileWriter(resultFilePath));

            writer.write("{");
            writer.newLine();
            for (int i = 0; i < frequenciesNumber; i++) {
                Map.Entry<String, Double> entry = wordFrequencies.get(i);
                String word = entry.getKey();
                double frequency = entry.getValue();
                String str = " ".repeat(4) + "\"" + word + "\": " + Double.toString(frequency);
                writer.write(str);
                if (i < frequenciesNumber - 1) {
                    writer.write(",");
                }
                writer.newLine();
            }
            writer.write("}");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<List<String>> readSegmentedWordsFile(String filePath, Set<String> stopWords) throws IOException {
        List<List<String>> segmentedWords = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line;
        while ((line = reader.readLine()) != null) {
            // 根据空格得到分词结果
            String[] lineWords = line.trim().split("\\s+");
            // 去除标点及停用词
            List<String> processedWords = processWords(lineWords, stopWords);
            segmentedWords.add(processedWords);
        }
        reader.close();

        return segmentedWords;
    }

    public static Set<String> readStopWordsFile(String filePath) throws IOException {
        Set<String> stopWords = new HashSet<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String word;
        while ((word = reader.readLine()) != null) {
            stopWords.add(word.trim());
        }
        reader.close();

        return stopWords;
    }

    public static List<String> processWords(String[] words, Set<String> stopWords) {
        List<String> processedWords = new ArrayList<>();

        for (String word : words) {
            if (!stopWords.contains(word)) {
                processedWords.add(word);
            }
        }

        return processedWords;
    }

    public static List<Map.Entry<String, Double>> countWords(List<List<String>> words) {
        // 词频统计
        Map<String, Integer> wordCounts = new HashMap<>();
        int wordsNumber = 0;

        for (List<String> lineWords : words) {
            for (String word : lineWords) {
                wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
            }
            wordsNumber += lineWords.size();
        }

        // 词频计算
        Map<String, Double> wordFrequencies = new HashMap<>();

        for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
            double frequency = (double) entry.getValue() / wordsNumber;
            wordFrequencies.put(entry.getKey(), frequency);
        }

        // 词频排序
        List<Map.Entry<String, Double>> sortedWordFrequencies = new ArrayList<>(wordFrequencies.entrySet());
        sortedWordFrequencies.sort((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()));

        return sortedWordFrequencies;
    }
}

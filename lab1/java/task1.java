package main.java;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class task1 {
    public static void main(String[] args) {
        String textFilePath = "E:\\Code\\NLP\\Lab\\lab1\\src\\main\\resources\\data\\corpus.sentence.txt";
        String dictFilePath = "E:\\Code\\NLP\\Lab\\lab1\\src\\main\\resources\\data\\corpus.dict.txt";
        String resultFilePath = "E:\\Code\\NLP\\Lab\\lab1\\src\\main\\resources\\result\\word_segmentation.txt";
        int maxLength = 7; // 最大长度为7

        try {
            List<String> text = readTextFile(textFilePath);
            List<Set<String>> dicts = readDictFile(dictFilePath, maxLength);
            BufferedWriter writer = new BufferedWriter(new FileWriter(resultFilePath));

            for (String sentence : text) {
                List<String> segmented = segmentWord(sentence, dicts, maxLength);

                for (String word : segmented) {
                    System.out.print(word + " ");
                    writer.write(word + " ");
                }
                System.out.println();
                writer.newLine();
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> segmentWord(String text, List<Set<String>> dicts, int maxLength) {
        List<String> result = new ArrayList<>();
        int leftIndex = 0; // 初始化指向句子的首部

        while (leftIndex < text.length()) {
            int rightIndex = Math.min(leftIndex + maxLength, text.length());
            String sequence = text.substring(leftIndex, rightIndex);
            boolean match = false; // 记录是否匹配

            for(int i = sequence.length(); i > 0; i--) {
                String word = sequence.substring(0, i);
                // 算法优化：在对应词长的词典中尝试匹配
                if (dicts.get(i - 1).contains(word)) {
                    result.add(word);
                    match = true;
                    leftIndex += i;
                    break;
                }
            }

            // 若未匹配，则为单字
            if (!match) {
                result.add(sequence.substring(0, 1));
                leftIndex++;
            }
        }

        return result;
    }

    public static List<String> readTextFile(String filePath) throws IOException {
        List<String> text = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line;
        while ((line = reader.readLine()) != null) {
            text.add(line.trim()); // 去除句子两端的空格
        }
        reader.close();

        return text;
    }

    public static List<Set<String>> readDictFile(String filePath, int maxLength) throws IOException {
        List<Set<String>> dicts = new ArrayList<>(); // 子词典列表
        // 初始化子词典列表，分别存储不同词长的词
        for (int length = 0; length < maxLength; length++) {
            dicts.add(new HashSet<>());
        }

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String word;
        while ((word = reader.readLine()) != null) {
            // 读取词典
            word = word.trim();
            int wordLength = word.length();
            if (wordLength > 0) {
                // 将词保存至对应词长的子词典
                dicts.get(wordLength - 1).add(word);
            }
        }
        reader.close();

        return dicts;
    }
}

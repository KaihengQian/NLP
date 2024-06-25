import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class task2 {
    public static void main(String[] args) {
        String frequencyFilePath = "E:\\Code\\NLP\\Lab3\\lab3\\src\\main\\resources\\result\\document_frequency.txt";
        String indexFilePath = "E:\\Code\\NLP\\Lab3\\lab3\\src\\main\\resources\\result\\inverted_index.txt";
        Map<String, Integer> documentFrequency = loadDocumentFrequency(frequencyFilePath);
        Map<String, List<Integer>> invertedIndex = loadInvertedIndex(indexFilePath);

        Scanner scanner = new Scanner(System.in);
        String input;

        while (true) {
            System.out.println("robot：您好，请选择检索风格（严格/适中/宽松）");
            System.out.print("user：");
            input = scanner.nextLine();
            String style = input;
            System.out.println("robot：好的，请输入关键词");
            System.out.print("user：");
            input = scanner.nextLine();
            String[] keywords = input.split("\\s+"); // 以空格切分输入

            // 记录检索开始时间
            long startTime = System.nanoTime();

            String[] styles = {"严格", "适中", "宽松"};
            List<Integer> docIDs;
            if (style.equals(styles[0])) {
                docIDs = exactMatch(keywords, documentFrequency, invertedIndex);
            } else if (style.equals(styles[2])) {
                docIDs = looseMatch(keywords, invertedIndex);
            }
            else {
                docIDs = moderateMatch(keywords, documentFrequency, invertedIndex);
            }

            respond(docIDs);

            // 记录检索结束时间
            long endTime = System.nanoTime();

            // 计算检索时间
            long duration = endTime - startTime;
            double result = (double) duration / 1_000_000;

            System.out.println("robot：本次检索用时 " + result + " 毫秒");
            System.out.println();

            System.out.println("robot：请问您是否想要继续检索（是/否）");
            System.out.print("user：");
            input = scanner.nextLine();

            if (input.equals("否")) {
                break;
            }
            else {
                System.out.println();
                System.out.println("#".repeat(37));
                System.out.println();
            }
        }
    }

    public static Map<String,Integer> loadDocumentFrequency(String filepath) {
        Map<String, Integer> documentFrequency = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(": ");
                String word = parts[0];
                String df = parts[1];
                documentFrequency.put(word, Integer.parseInt(df));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return documentFrequency;
    }

    public static Map<String, List<Integer>> loadInvertedIndex(String filepath) {
        Map<String, List<Integer>> invertedIndex = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(": ");
                String word = parts[0];
                String[] docIDsStr = parts[1].replaceAll("\\[|\\]", "").split(", ");
                List<Integer> docIDs = new ArrayList<>();
                for (String docIDStr : docIDsStr) {
                    docIDs.add(Integer.parseInt(docIDStr));
                }
                invertedIndex.put(word, docIDs);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return invertedIndex;
    }

    public static List<Integer> combineInvertedIndex(Map<String, List<Integer>> indexes, Map<String, Integer> frequencies) {
        // 按照文档频率升序排列
        List<Map.Entry<String, Integer>> sortedFrequencies = new ArrayList<>(frequencies.entrySet());
        sortedFrequencies.sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));

        List<Integer> docIDs = new ArrayList<>();
        int flag = 0;
        for (Map.Entry<String, Integer> entry : sortedFrequencies) {
            String keyword = entry.getKey();
            List<Integer> index = indexes.get(keyword);
            if (flag == 0) {
                docIDs = index;
                flag++;
            }
            else {
                List<Integer> newDocIDs = new ArrayList<>();
                int pointer1 = 0;
                int pointer2 = 0;
                while (pointer1 < docIDs.size() && pointer2 < index.size()) {
                    int docID1 = docIDs.get(pointer1);
                    int docID2 = index.get(pointer2);
                    if (docID1 == docID2) {
                        newDocIDs.add(docIDs.get(pointer1));
                        pointer1++;
                        pointer2++;
                    } else if (docID1 < docID2) {
                        pointer1++;
                    }
                    else {
                        pointer2++;
                    }
                }
                docIDs = newDocIDs;
            }
        }

        return docIDs;
    }

    public static List<Integer> exactMatch(String[] keywords, Map<String, Integer> documentFrequency,
                                                 Map<String, List<Integer>> invertedIndex) {
        List<Integer> docIDs = new ArrayList<>();

        Map<String, List<Integer>> indexes = new HashMap<>();
        for (String keyword : keywords) {
            if (documentFrequency.containsKey(keyword)) {
                List<Integer> index = invertedIndex.get(keyword);
                indexes.put(keyword, index);
            }
            else {
                return docIDs;
            }
        }

        // 匹配的关键词个数为1
        if (indexes.size() == 1) {
            docIDs = indexes.values().iterator().next();
        }
        // 匹配的关键词个数大于等于2
        else {
            Map<String, Integer> frequencies = new HashMap<>();
            for (String keyword : indexes.keySet()) {
                frequencies.put(keyword, documentFrequency.get(keyword));
            }
            docIDs = combineInvertedIndex(indexes, frequencies);
        }

        return docIDs;
    }

    public static List<Integer> moderateMatch(String[] keywords, Map<String, Integer> documentFrequency,
                                              Map<String, List<Integer>> invertedIndex) {
        List<Integer> docIDs = new ArrayList<>();
        int numKeyWords = keywords.length;
        int halfNum = (numKeyWords + 1) / 2;

        Map<String, List<Integer>> indexes = new HashMap<>();
        for (String keyword : keywords) {
            if (documentFrequency.containsKey(keyword)) {
                List<Integer> index = invertedIndex.get(keyword);
                indexes.put(keyword, index);
            }
        }
        if (indexes.size() < (halfNum)) {
            return docIDs;
        }

        // 统计各文章含有的关键词数
        Map<Integer, Integer> count = new HashMap<>();
        for (List<Integer> index : indexes.values()) {
            for (int docID : index) {
                if (count.containsKey(docID)) {
                    count.put(docID, count.get(docID) + 1);
                }
                else {
                    count.put(docID, 1);
                }
            }
        }

        // 按含有的关键词数对文章进行降序排序
        List<Map.Entry<Integer, Integer>> sortedCount = count.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed()).toList();

        // 保留含有至少一半关键词的文章
        for (Map.Entry<Integer, Integer> entry : sortedCount) {
            int docID = entry.getKey();
            int num = entry.getValue();
            if (num >= (halfNum)) {
                docIDs.add(docID);
            }
        }

        return docIDs;
    }

    public static List<Integer> looseMatch(String[] keywords, Map<String, List<Integer>> invertedIndex) {
        Set<Integer> docIDs = new HashSet<>();

        for (String keyword : keywords) {
            if (invertedIndex.containsKey(keyword)) {
                List<Integer> index = invertedIndex.get(keyword);
                docIDs.addAll(index);
            }
        }

        return new ArrayList<>(docIDs);
    }

    public static void respond(List<Integer> docIDs) {
        if (docIDs.isEmpty()) {
            System.out.println("robot：抱歉，没有与此相关的检索结果");
        }
        else {
            Collections.sort(docIDs);
            System.out.print("robot：共 " + docIDs.size() + " 个检索结果： ");
            for (int docID : docIDs) {
                System.out.print(docID + ".txt  ");
            }
            System.out.print("\n");
        }
    }
}

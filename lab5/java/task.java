import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class task {
    static int termMaxLength = 107;
    static int pointerMaxLength = 8;
    static int termAndPointerMaxLength = termMaxLength + pointerMaxLength;

    public static Map<String, String> readTermData(String filePath, Integer taskID) throws IOException {
        Map<String, String> termData;
        if (taskID == 1) {
            termData = new HashMap<>();
        } else {  // task2中要求将词项按字典序排列
            termData = new TreeMap<>();
        }
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line = reader.readLine();
        reader.close();

        int curr = 0;
        while (curr < line.length()) {
            // 截取得到词项并去除左端用于补齐缺省的空格
            String term = line.substring(curr, curr + termMaxLength).replaceAll("^\s+", "");
            // 截取得到倒排索引表指针并去除左端用于补齐缺省的0
            String pointer = line.substring(curr + termMaxLength, curr + termAndPointerMaxLength).replaceAll("^0+", "");
            if (pointer.isEmpty()) {  // 处理指针指向地址为0的情况
                pointer = "0";
            }
            termData.put(term, pointer);
            curr += termAndPointerMaxLength;
        }

        return termData;
    }

    public static void saveDict(String dict, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(dict);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void evaluate(String rawDictPath, String newDictPath, Integer taskID) throws IOException {
        File rawDict = new File(rawDictPath);
        File newDict = new File(newDictPath);

        double rawDictSize = (double) Files.size(rawDict.toPath());
        double newDictSize = (double) Files.size(newDict.toPath());
        double ratio = 1.0 - newDictSize / rawDictSize;

        double rawDictSizeInMB = rawDictSize / 1024 / 1024;
        double newDictSizeInMB = newDictSize / 1024 / 1024;

        String method;
        if (taskID == 1) {
            method = "长字符串+词项长度";
        } else {
            method = "按块存储+前端编码";
        }
        System.out.printf("原词典空间大小为 %.2f MB，压缩后词典空间大小为 %.2f MB。\n", rawDictSizeInMB, newDictSizeInMB);
        System.out.printf("利用 %s 的方法压缩词典，节省了 %.2f%% 的空间消耗。\n", method, ratio * 100);
    }
}

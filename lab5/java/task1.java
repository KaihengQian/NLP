import java.io.*;
import java.util.Map;

public class task1 {
    public static void main(String[] args) throws IOException {
        String dictPath = "E:\\Code\\NLP\\Lab5\\src\\main\\resources\\dict.txt";
        String newDictPath = "E:\\Code\\NLP\\Lab5\\src\\main\\resources\\result\\new_dict_1.txt";
        String rawDictPath = "E:\\Code\\NLP\\Lab5\\src\\main\\resources\\result\\raw_dict_1.txt";

        // 读取原词典
        Map<String, String> termData = task.readTermData(dictPath, 1);

        // 压缩
        String newDict = compress(termData);
        task.saveDict(newDict, newDictPath);

        // 解压
        String rawDict = decompress(newDict);
        task.saveDict(rawDict, rawDictPath);

        // 评估压缩效果
        task.evaluate(dictPath, newDictPath, 1);
    }

    public static String compress(Map<String, String> termData) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : termData.entrySet()) {
            String term = entry.getKey();
            int termLength = term.length();
            String pointer = entry.getValue();
            // 存入词项长度的位数（因为可能超过一位）
            sb.append((int) Math.floor(Math.log10(termLength)) + 1);
            // 存入词项长度
            sb.append(termLength);
            // 存入词项
            sb.append(term);
            // 存入倒排索引表指针长度（不会超过一位）
            sb.append(pointer.length());
            // 存入倒排索引表指针
            sb.append(pointer);
        }

        return sb.toString();
    }

    public static String decompress(String newDict) {
        StringBuilder sb = new StringBuilder();

        int curr = 0;
        while (curr < newDict.length()) {
            // 读取词项长度的位数
            int termLengthDigitCount = Character.getNumericValue(newDict.charAt(curr));
            curr += 1;
            // 读取词项长度
            int termLength = Integer.parseInt(newDict.substring(curr, curr + termLengthDigitCount));
            curr += termLengthDigitCount;
            // 读取词项
            String term  = newDict.substring(curr, curr + termLength);
            curr += termLength;
            // 读取倒排索引表指针长度
            int pointerLength = Character.getNumericValue(newDict.charAt(curr));
            curr += 1;
            // 读取倒排索引表指针
            String pointer = newDict.substring(curr, curr + pointerLength);
            curr += pointerLength;

            // 存入词项，缺省用空格补齐
            sb.append(" ".repeat(task.termMaxLength - termLength));
            sb.append(term);
            // 存入倒排索引表指针，缺省用0补齐
            sb.append("0".repeat(task.pointerMaxLength - pointerLength));
            sb.append(pointer);
        }

        return sb.toString();
    }
}

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class task2 {
    public static void main(String[] args) throws IOException {
        String dictPath = "E:\\Code\\NLP\\Lab5\\src\\main\\resources\\dict.txt";
        String newDictPath = "E:\\Code\\NLP\\Lab5\\src\\main\\resources\\result\\new_dict_2.txt";
        String rawDictPath = "E:\\Code\\NLP\\Lab5\\src\\main\\resources\\result\\raw_dict_2.txt";

        // 读取原词典
        Map<String, String> termData = task.readTermData(dictPath, 2);

        // 压缩
        String newDict = compress(termData);
        task.saveDict(newDict, newDictPath);

        // 解压
        String rawDict = decompress(newDict);
        task.saveDict(rawDict, rawDictPath);

        // 评估压缩效果
        task.evaluate(dictPath, newDictPath, 2);
    }

    private static int findCommonPrefix(String s1, String s2) {
        int length = 0;
        while (length < s1.length() && length < s2.length() && s1.charAt(length) == s2.charAt(length)) {
            length++;
        }
        return length;
    }

    public static String compress(Map<String, String> termData) {
        StringBuilder stringBuilder = new StringBuilder();

        String prefix = null;
        Map<String, String> blockTermData = new TreeMap<>();
        int prefixLengthThreshold = 3;

        for (Map.Entry<String, String> entry : termData.entrySet()) {
            String term = entry.getKey();
            String pointer = entry.getValue();

            if (prefix == null) {
                prefix = term;
                blockTermData.put(term, pointer);
            } else {
                int prefixLength = findCommonPrefix(prefix, term);
                if (prefixLength >= prefixLengthThreshold) {
                    blockTermData.put(term, pointer);
                    prefix = prefix.substring(0, prefixLength);
                } else {
                    StringBuilder sb = new StringBuilder();
                    if (blockTermData.size() == 1) {
                        Map.Entry<String, String> e = blockTermData.entrySet().iterator().next();
                        String t = e.getKey();
                        String p = e.getValue();

                        // 存入前缀（即词项）长度的位数（因为可能超过一位）
                        int tLength = t.length();
                        int tLengthDigitCount = (int) Math.floor(Math.log10(tLength)) + 1;
                        sb.append(tLengthDigitCount);
                        // 存入前缀（即词项）长度
                        sb.append(tLength);
                        // 存入前缀（即词项）
                        sb.append(t);
                        // 存入后缀长度的位数（此时不存在后缀）
                        sb.append(0);
                        // 存入倒排索引表指针长度（不会超过一位）
                        int pLength = p.length();
                        sb.append(pLength);
                        // 存入倒排索引表指针
                        sb.append(p);

                        // 存入块长度的位数
                        int blockLength = 1 + tLengthDigitCount + tLength + 1 + 1 + pLength;
                        int blockLengthDigitCount = (int) Math.floor(Math.log10(blockLength)) + 1;
                        stringBuilder.append(blockLengthDigitCount);
                        // 存入块长度
                        stringBuilder.append(blockLength);
                        // 存入块信息
                        stringBuilder.append(sb);
                    } else {
                        // 存入前缀长度的位数（因为可能超过一位）
                        int pfLength = prefix.length();
                        int pfLengthDigitCount = (int) Math.floor(Math.log10(pfLength)) + 1;
                        sb.append(pfLengthDigitCount);
                        // 存入前缀长度
                        sb.append(pfLength);
                        // 存入前缀
                        sb.append(prefix);

                        int blockLength = 1 + pfLengthDigitCount + pfLength;
                        for (Map.Entry<String, String> e : blockTermData.entrySet()) {
                            String t = e.getKey();
                            String p = e.getValue();

                            // 存入后缀长度的位数（因为可能超过一位）
                            int tLength = t.length();
                            int sfLength = tLength - pfLength;
                            int sfLengthDigitCount;
                            if (sfLength == 0) {
                                sfLengthDigitCount = 1;
                            } else {
                                sfLengthDigitCount = (int) Math.floor(Math.log10(sfLength)) + 1;
                            }
                            sb.append(sfLengthDigitCount);
                            // 存入后缀长度
                            sb.append(sfLength);
                            // 存入后缀
                            String suffix = t.substring(pfLength);
                            sb.append(suffix);
                            // 存入倒排索引表指针长度（不会超过一位）
                            int pLength = p.length();
                            sb.append(pLength);
                            // 存入倒排索引表指针
                            sb.append(p);

                            blockLength += 1 + sfLengthDigitCount + sfLength + 1 + pLength;
                        }
                        // 存入块长度的位数
                        int blockLengthDigitCount = (int) Math.floor(Math.log10(blockLength)) + 1;
                        stringBuilder.append(blockLengthDigitCount);
                        // 存入块长度
                        stringBuilder.append(blockLength);
                        // 存入块信息
                        stringBuilder.append(sb);
                    }
                    // 重置块信息
                    prefix = term;
                    blockTermData.clear();
                    blockTermData.put(term, pointer);
                }
            }
        }

        return stringBuilder.toString();
    }

    public static String decompress(String newDict) {
        StringBuilder sb = new StringBuilder();

        int curr = 0;
        while (curr < newDict.length()) {
            // 读取块长度的位数
            int blockLengthDigitCount = Character.getNumericValue(newDict.charAt(curr));
            curr += 1;
            // 读取块长度
            int blockLength = Integer.parseInt(newDict.substring(curr, curr + blockLengthDigitCount));
            curr += blockLengthDigitCount;
            int blockEnd = curr + blockLength;  // 块末尾
            // 读取前缀长度的位数
            int prefixLengthDigitCount = Character.getNumericValue(newDict.charAt(curr));
            curr += 1;
            // 读取前缀长度
            int prefixLength = Integer.parseInt(newDict.substring(curr, curr + prefixLengthDigitCount));
            curr += prefixLengthDigitCount;
            // 读取前缀
            String prefix = newDict.substring(curr, curr + prefixLength);
            curr += prefixLength;
            while (curr < blockEnd) {
                // 读取后缀长度的位数
                int suffixLengthDigitCount = Character.getNumericValue(newDict.charAt(curr));
                curr += 1;
                if (suffixLengthDigitCount == 0) {
                    // 读取倒排索引表指针长度
                    int pointerLength = Character.getNumericValue(newDict.charAt(curr));
                    curr += 1;
                    // 读取倒排索引表指针
                    String pointer = newDict.substring(curr, curr + pointerLength);
                    curr += pointerLength;

                    // 存入词项（即前缀），缺省用空格补齐
                    sb.append(" ".repeat(task.termMaxLength - prefixLength));
                    sb.append(prefix);
                    // 存入倒排索引表指针，缺省用0补齐
                    sb.append("0".repeat(task.pointerMaxLength - pointerLength));
                    sb.append(pointer);
                } else {
                    // 读取后缀长度
                    int suffixLength = Integer.parseInt(newDict.substring(curr, curr + suffixLengthDigitCount));
                    curr += suffixLengthDigitCount;
                    // 读取后缀
                    String suffix = newDict.substring(curr, curr + suffixLength);
                    curr += suffixLength;
                    // 读取倒排索引表指针长度
                    int pointerLength = Character.getNumericValue(newDict.charAt(curr));
                    curr += 1;
                    // 读取倒排索引表指针
                    String pointer = newDict.substring(curr, curr + pointerLength);
                    curr += pointerLength;

                    // 存入词项（即前缀+后缀），缺省用空格补齐
                    sb.append(" ".repeat(task.termMaxLength - prefixLength - suffixLength));
                    sb.append(prefix).append(suffix);
                    // 存入倒排索引表指针，缺省用0补齐
                    sb.append("0".repeat(task.pointerMaxLength - pointerLength));
                    sb.append(pointer);
                }
            }
        }

        return sb.toString();
    }
}

import json
import math
import queue
import random
import re
import jieba


# 读取文档集
def read_file(path):
    articles = []

    for i in range(1, 10001):
        article = []
        file_path = path + str(i) + '.txt'
        with open(file_path, 'r', encoding='utf-8') as file:
            for line in file:
                # 去除换行符和空行
                line = line.strip()
                if line:
                    article.append(line)
        articles.append(article)

    return articles


# 读取停用词词典
def read_stopwords(path):
    with open(path, 'r+', encoding='utf-8') as file:
        stopwords = file.read().split('\n')
    return stopwords


# 分词
def segment_word(articles, stopwords):
    segmentation = []

    for article in articles:
        segmented_article = []
        for sentence in article:
            segmented_sentence = jieba.cut(sentence)
            for word in segmented_sentence:
                # 去除停用词、标点、数字和单字
                if len(word) > 1 and word not in stopwords and not re.match(r"[\W\d_]+", word):
                    segmented_article.append(word)
        segmentation.append(segmented_article)

    return segmentation


# 建立倒排索引
def build_inverted_index(segmentation):
    inverted_index = dict()

    docID = 1
    for article in segmentation:
        for word in article:
            if word not in inverted_index:
                inverted_index[word] = dict()
            # 统计词频
            if docID not in inverted_index[word]:
                inverted_index[word][docID] = 1
            else:
                inverted_index[word][docID] += 1
        docID += 1

    return inverted_index


# 优先队列
class KMaxQueue:
    def __init__(self, k):
        self.pq = queue.PriorityQueue()
        self.k = k

    def insert(self, index, value):
        if self.pq.qsize() < self.k:
            self.pq.put((value, index))
        else:
            ele = self.pq.get()
            if value > ele[0]:
                self.pq.put((value, index))
            else:
                self.pq.put(ele)

    def get_pair(self):
        result = dict()
        while not self.pq.empty():
            ele = self.pq.get()
            result[ele[1]] = ele[0]
        return {k: result[k] for k in reversed(result)}

    def get(self):
        result = []
        while not self.pq.empty():
            ele = self.pq.get()
            result.append(ele[1])
        return result[::-1]


# 建立胜者表和低端表
def build_champion_list(inverted_index, r):
    champion_list = dict()
    low_list = dict()

    for word, index in inverted_index.items():
        # 文档频率
        df = len(index)
        # 按词项频率选取最高的r篇文档
        kmq = KMaxQueue(r)
        for docID, tf in index.items():
            kmq.insert(docID, tf)
        champion = kmq.get_pair()
        for key in champion.keys():
            index.pop(key)
        champion['df'] = df
        champion_list[word] = champion
        index['df'] = df
        low_list[word] = index

    return champion_list, low_list


# 保存文件
def save_json_file(data, path):
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f)


# 加载文件
def load_json_file(path):
    with open(path, 'r') as f:
        data = json.load(f)
    return data


# 胜者表检索
def champion_list_retrieval(keywords, k, champion_list):
    # 取胜者表的交集
    result = set(champion_list[keywords[0]].keys())
    for i in range(1, len(keywords)):
        index = set(champion_list[keywords[i]].keys())
        result.intersection_update(index)
    result.remove('df')

    if len(result) == k:
        return list(result)
    elif len(result) > k:
        kmq = KMaxQueue(k)
        # 根据TF-IDF值进一步筛选
        for docID in result:
            tf_idf = 0
            for keyword in keywords:
                tf = champion_list[keyword][docID]
                df = champion_list[keyword]['df']
                tf_idf += (1 + math.log10(tf)) * math.log10(10000 / df)
            kmq.insert(docID, tf_idf)
        return kmq.get()
    else:
        # 在低端表中继续检索
        return list(result) + low_list_retrieval(keywords, k - len(result))


# 低端表检索
def low_list_retrieval(keywords, k):
    low_list = load_json_file('result/low_list.json')

    # 取低端表的交集
    result = set(low_list[keywords[0]].keys())
    for i in range(1, len(keywords)):
        index = set(low_list[keywords[i]].keys())
        result.intersection_update(index)
    result.remove('df')

    if len(result) > k:
        return random.sample(list(result), k)
    else:
        return list(result)


# 根据检索结果进行回答
def respond(docIDs):
    if not docIDs:
        print("robot：抱歉，没有与此相关的检索结果")
    else:
        print("robot：共 " + str(len(docIDs)) + " 个检索结果：", end=" ")
        for docID in docIDs:
            print(str(docID) + ".txt", end=" ")
        print()


if __name__ == '__main__':
    file_dir_path = 'article/'
    stopwords_dict_path = 'dict/hit_stopwords.txt'
    champion_list_file_path = 'result/champion_list.json'
    low_list_file_path = 'result/low_list.json'

    stopwords = read_stopwords(stopwords_dict_path)
    articles = read_file(file_dir_path)
    segmentation = segment_word(articles, stopwords)

    inverted_index = build_inverted_index(segmentation)
    champion_list, low_list = build_champion_list(inverted_index, 250)
    save_json_file(champion_list, champion_list_file_path)
    save_json_file(low_list, low_list_file_path)

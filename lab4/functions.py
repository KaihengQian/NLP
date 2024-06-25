import heapq
import pickle
import re
import jieba
import numpy as np
import torch
from scipy.spatial.distance import cosine
from transformers import BertTokenizer, BertModel, RobertaTokenizer, RobertaModel


# 读取文档集
def read_file(path):
    articles = []

    for i in range(1, 20739):
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


# 分词
def segment_word(articles):
    segmentation = []

    for article in articles:
        segmented_article = set()
        for sentence in article:
            segmented_sentence = jieba.cut(sentence)
            for word in segmented_sentence:
                # 去除标点、数字和单字
                if len(word) > 1 and not re.match(r"[\W\d_]+", word):
                    segmented_article.add(word)
        segmentation.append(segmented_article)

    return segmentation


# 对词语进行 Embedding
def embedding(segmentation, file_path):
    # 词语去重
    vocab = set()
    for article in segmentation:
        vocab.update(article)

    vocab_embedding = dict()

    # 加载 RoBERTa 分词器和模型
    vocab_file = 'model/roberta-base/vocab.json'
    merges_file = 'model/roberta-base/merges.txt'
    tokenizer = RobertaTokenizer(vocab_file, merges_file)
    model_path = "model/roberta-base/roberta-base-model"
    model = RobertaModel.from_pretrained(model_path)

    # 使用 RoBERTa 模型生成 embedding
    for word in vocab:
        encoded_input = tokenizer(word, return_tensors='pt')
        with torch.no_grad():
            outputs = model(**encoded_input)
        # 获取最后一层的隐藏状态
        last_hidden_states = outputs.last_hidden_state
        word_embedding = last_hidden_states[0][0].numpy()
        vocab_embedding[word] = word_embedding

    '''
    # 加载预训练的 BERT 模型和分词器
    model_path = "model/bert-base-uncased"
    tokenizer = BertTokenizer.from_pretrained(model_path)
    model = BertModel.from_pretrained(model_path)

    # 使用 BERT 模型生成 embedding
    for word in vocab:
        encoded_input = tokenizer(word, return_tensors='pt')
        input_ids = encoded_input["input_ids"]
        attention_mask = encoded_input["attention_mask"]
        outputs = model(input_ids=input_ids, attention_mask=attention_mask)
        # 取最后一层的隐藏状态，即 embedding
        last_hidden_states = outputs.last_hidden_state
        word_embedding = last_hidden_states[:, 0, :]
        # 转为 numpy 数组
        if word_embedding.device.type != 'cpu':
            word_embedding = word_embedding.to('cpu')
        word_embedding = word_embedding.detach().numpy().flatten()
        vocab_embedding[word] = word_embedding
    '''

    # 将 embedding 结果保存至文件
    with open(file_path, 'wb') as f:
        pickle.dump(vocab_embedding, f)


# 构建倒排索引
def build_inverted_index(segmentation, file_path):
    inverted_index = dict()

    docID = 1
    for article in segmentation:
        for word in article:
            if word not in inverted_index:
                inverted_index[word] = []
            inverted_index[word].append(docID)
        docID += 1

    # 将倒排索引保存至文件
    with open(file_path, 'wb') as f:
       pickle.dump(inverted_index, f)


def load_vocab(file_path):
    with open(file_path, 'rb') as f:
        vocab = pickle.load(f)
    return vocab


class SimilarK:
    def __init__(self, k):
        self.k = k
        self.min_heap = []

    def add(self, key, value):
        heapq.heappush(self.min_heap, (value, key))
        if len(self.min_heap) > self.k:
            heapq.heappop(self.min_heap)

    def get_all_keys(self):
        return [key for value, key in self.min_heap]


# 寻找相似词
def find_similar_words(keyword, k, vocab):
    keyword_embedding = vocab[keyword]
    similar = SimilarK(k)

    for word, word_embedding in vocab.items():
        if word != keyword:
            # 计算两个 embedding 向量的欧氏距离
            # sim = np.linalg.norm(keyword_embedding - word_embedding)
            # 计算两个 embedding 向量的余弦相似度
            sim = 1 - cosine(keyword_embedding, word_embedding)
            similar.add(word, sim)

    similar_words = similar.get_all_keys()

    return similar_words


# and 检索
def exact_match(keyword, embedding, inverted_index):
    # 寻找语义相似度最高的 Top 1 个词
    similar_words = find_similar_words(keyword, 1, embedding)

    indexes = []
    indexes.append(inverted_index[keyword])
    indexes.append(inverted_index[similar_words[0]])
    # indexes.append(inverted_index[similar_words[1]])
    # 按照文档频率升序排列
    sorted_indexes = sorted(indexes, key=lambda x: len(x))

    # 合并倒排记录表
    docIDs = sorted_indexes[0]
    for i in range(1, 2):
        index = sorted_indexes[i]
        new_docIDs = []
        length_1 = len(docIDs)
        length_2 = len(index)
        pointer_1 = 0
        pointer_2 = 0
        forward_step_1 = int(np.sqrt(length_1))
        forward_step_2 = int(np.sqrt(length_2))
        while pointer_1 < length_1 and pointer_2 < length_2:
            docID_1 = docIDs[pointer_1]
            docID_2 = index[pointer_2]
            if docID_1 == docID_2:
                new_docIDs.append(docID_1)
                pointer_1 += 1
                pointer_2 += 1
            elif docID_1 < docID_2:
                if pointer_1 % forward_step_1 == 0:
                    # 实现跳表
                    while pointer_1 + forward_step_1 < length_1 and docIDs[pointer_1 + forward_step_1] < docID_2:
                        pointer_1 += forward_step_1
                pointer_1 += 1
            else:
                if pointer_2 % forward_step_2 == 0:
                    # 实现跳表
                    while pointer_2 + forward_step_2 < length_1 and index[pointer_2 + forward_step_2] < docID_1:
                        pointer_2 += forward_step_2
                pointer_2 += 1
        docIDs = new_docIDs

    return docIDs


# 根据检索结果进行回答
def respond(docIDs):
    if not docIDs:
        print("robot：抱歉，没有与此相关的检索结果")
    else:
        docIDs.sort()
        print("robot：共 " + str(len(docIDs)) + " 个检索结果：", end=" ")
        for docID in docIDs:
            print(str(docID) + ".txt", end=" ")
        print()


if __name__ == '__main__':
    file_dir_path = 'article/'
    embedding_file_path = 'result/word_embedding_roberta.pkl'
    index_file_path = 'result/inverted_index.pkl'

    articles = read_file(file_dir_path)
    segmentation = segment_word(articles)
    embedding(segmentation, embedding_file_path)
    # build_inverted_index(segmentation, index_file_path)

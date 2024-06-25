import math
from collections import Counter
import jieba
import numpy as np
from matplotlib import pyplot as plt
from PIL import Image
from wordcloud import WordCloud, ImageColorGenerator


# 读取文档集
def read_file(path):
    articles = []

    for i in range(1, 21):
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
                # 去除停用词和单字
                if word not in stopwords and len(word) > 1:
                    segmented_article.append(word)
        segmentation.append(segmented_article)

    return segmentation


# 计算 TF-IDF 指标
def calculate_tf_idf(word, count, total, articles):
    tf = count / total

    num = 0
    for article in articles:
        if word in article:
            num += 1
    idf = math.log(len(articles) / (1 + num))

    tf_idf = tf * idf
    return tf_idf


# 词频统计
def calculate_word_frequency(segmentation):
    corpus = [word for article in segmentation for word in article]  # 语料库
    word_counts = Counter(corpus)  # 词计数
    total_counts = len(corpus)  # 总词数

    word_frequency = dict()
    for word, count in word_counts.items():
        word_frequency[word] = calculate_tf_idf(word, count, total_counts, segmentation)

    return word_frequency


# 绘制词云图
def plot_wordcloud(word_frequency, path, bg=True, bg_color=False):
    # 将字典中的键值对转换成符合 wordcloud 库要求的格式
    wordcloud_data = {word: freq for word, freq in word_frequency.items()}

    plt.figure(figsize=(10, 8), dpi=1000)
    font_path = r'C:\Windows\Fonts\msyh.ttc'  # 解决中文显示问题

    if bg:  # 自定义背景图片
        mask = plt.imread("picture/china_map.jpg")
        # 创建 WordCloud 对象并生成词云图
        wordcloud = WordCloud(font_path=font_path, mask=mask, width=800, height=400, scale=2, mode="RGBA",
                              background_color='black').generate_from_frequencies(wordcloud_data)

        if bg_color:  # 根据图片色设置字体颜色
            background_image = np.array(Image.open("picture/china_map.jpg"))  # 读取背景图片
            img_colors = ImageColorGenerator(background_image)  # 提取背景图片颜色
            wordcloud.recolor(color_func=img_colors)

    else:
        # 创建 WordCloud 对象并生成词云图
        wordcloud = WordCloud(font_path=font_path, width=800, height=400,
                              background_color='black').generate_from_frequencies(wordcloud_data)

    # 绘制并保存词云图
    wordcloud.to_file(path)


if __name__ == '__main__':
    file_dir_path = 'article/'
    stopwords_dict_path = 'dict/hit_stopwords.txt'
    wordcloud_path = 'result/wordcloud5.png'

    articles = read_file(file_dir_path)
    stopwords = read_stopwords(stopwords_dict_path)
    segmentation = segment_word(articles, stopwords)
    word_frequency = calculate_word_frequency(segmentation)
    plot_wordcloud(word_frequency, wordcloud_path)

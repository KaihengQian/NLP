import time
import functions as func


if __name__ == '__main__':
    embedding_file_path = 'result/word_embedding_roberta.pkl'
    index_file_path = 'result/inverted_index.pkl'

    vocab_embedding = func.load_vocab(embedding_file_path)
    vocab_inverted_index = func.load_vocab(index_file_path)

    while True:
        print("robot：您好，请输入关键词")
        keyword = input("user：")

        print("robot：请问您是否想要查看与输入词的语义相似度最高的 Top K 个相似词（是/否）")
        flag = input("user：")

        if flag == "是":
            print("robot：好的，请设置 K 的值")
            k = int(input("user："))

            # 记录检索开始时间
            start_time = time.time()

            similar_words = func.find_similar_words(keyword, k, vocab_embedding)
            print("robot：与输入词的语义相似度最高的 Top " + str(k) + " 个相似词： " + " ".join(similar_words))

            # 记录检索结束时间
            end_time = time.time()

            # 计算检索时间
            duration = end_time - start_time

            print("robot：本次检索用时 " + str(duration) + " 秒")

        print("robot：好的，接下来将为您检索同时包含输入词及其语义相似度最高的 Top 1 个词的所有文档")

        # 记录检索开始时间
        start_time = time.time()

        docIDs = func.exact_match(keyword, vocab_embedding, vocab_inverted_index)
        func.respond(docIDs)

        # 记录检索结束时间
        end_time = time.time()

        # 计算检索时间
        duration = end_time - start_time

        print("robot：本次检索用时 " + str(duration) + " 秒")
        print()

        print("robot：请问您是否想要继续检索（是/否）")
        flag = input("user：")

        if flag == "否":
            break
        else:
            print()
            print("#" * 60)
            print()

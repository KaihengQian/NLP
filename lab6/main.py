import time
import functions as func


if __name__ == '__main__':
    champion_list_file_path = 'result/champion_list.json'

    champion_list = func.load_json_file(champion_list_file_path)

    while True:
        print("robot：您好，请输入关键词")
        keywords = input("user：").split()

        print("robot：好的，请设置 K 的值")
        k = int(input("user："))

        print(f"robot：好的，接下来将为您检索非精确的 Top {k} 文档")

        # 记录检索开始时间
        start_time = time.time()

        docIDs = func.champion_list_retrieval(keywords, k, champion_list)
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

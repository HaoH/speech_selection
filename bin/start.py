#!/bin/python

import os
import sys
import pymysql
import json
import time

import config
from baiduaip import run_baiduaip
from googlesp import run_google_speech

def fetch_audio_files(base_dir):


    # 打开数据库连接
    db = pymysql.connect(host=config.mysql['HOST'], user=config.mysql['USER'], password=config.mysql['PASSWD'], db=config.mysql['DB'])

    # 使用 cursor() 方法创建一个游标对象 cursor
    cursor = db.cursor()

    # 使用 execute()  方法执行 SQL 查询
    cursor.execute("select member_id, title, content, path, api_result, size, addtime from sr_follow_voice where addtime <= 1520492967 order by addtime desc limit 1")

    result = cursor.fetchall()
    file_content_map = {}
    for data in result:
        filename = os.path.join(base_dir, data[3])
        content = data[2].replace('<p>', '').replace('</p>', '').replace('<br/>', '').replace('&nbsp;', '')
        ifly_result = json.loads(data[4])
        ifly_text = ' '.join(ifly_result["transcribedWords"])
        file_content_map.update({
            filename : {
                'content' : content,
                'ifly_text' : ifly_text
            }
        })

    # 关闭数据库连接
    db.close()

    #print(file_content_map)
    return file_content_map

def run_speeches():

    base_dir = '/nas/api'
    #base_dir = sys.argvs[1]

    file_content_map = fetch_audio_files(base_dir)

    for filename in file_content_map.keys():
        data = file_content_map[filename]
        content = data['content']
        ifly_result = data['ifly_text']

        start = time.time()
        baidu_result = run_baiduaip(filename)
        baidu_cost = time.time() - start

        start = time.time()
        google_result = run_google_speech(filename)
        google_cost = time.time() - start

        print("content: %s\nbaidu: %d sec.\n%s\ngoogle: %d sec.\n%s\nifly: x sec.\n%s\n\n" % (content, baidu_cost, baidu_result, google_cost, google_result, ifly_result))


if __name__ == '__main__':

    run_speeches()

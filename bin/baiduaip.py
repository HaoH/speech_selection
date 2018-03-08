#!/bin/python

import os
import sys
import config
from aip import AipSpeech

def run_baiduaip(filename):

    client = AipSpeech(config.baidu['APP_ID'], config.baidu['API_KEY'], config.baidu['SECRET_KEY'])

    with open(filename, 'rb') as fp:
        content = fp.read()

    result = client.asr(content, 'wav', 16000, {'lan': 'en',})

    if result['err_msg'] == 'success.':
        print(result['result'][0])
    else:
        print("ERROR: %s" % result['err_msg'])


def print_usage():
    print("Usage: python baiduaip [filename]")


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print_usage()

    filename = os.path.join(os.path.dirname(os.path.dirname(sys.argv[0])), 'resources', sys.argv[1])

    run_baiduaip(filename)


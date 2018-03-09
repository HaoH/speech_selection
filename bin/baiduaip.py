#!/bin/python

import os
import sys
import audioread
from aip import AipSpeech
import config

def print_usage():
    print("Usage: python baiduaip.py [filename]")
    print("e.g.: python baiduaip.py . audio.wav")

def get_sample_rate(filename):
    with audioread.audio_open(filename) as f:
        return f.samplerate

def run_baiduaip(filename):

    client = AipSpeech(config.baidu['APP_ID'], config.baidu['API_KEY'], config.baidu['SECRET_KEY'])

    with open(filename, 'rb') as fp:
        content = fp.read()

    sample_rate = get_sample_rate(filename)
    #print("sample_rate: %d\n" % sample_rate)

    result = client.asr(content, 'wav', sample_rate, {'lan': 'en',})

    if result['err_msg'] == 'success.':
        msg = result['result'][0]
        print(msg)
    else:
        msg = ''
        print("ERROR: %s" % result['err_msg'])

    return msg

if __name__ == '__main__':

    if len(sys.argv) < 2:
        print_usage()
        sys.exit(-1)

    if sys.argv[1].startswith("/"):
        filename = sys.argv[1]
    else:
        filename = os.path.join(os.path.dirname(os.path.dirname(sys.argv[0])), sys.argv[1])

    run_baiduaip(filename)

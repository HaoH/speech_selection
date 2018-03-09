#!/usr/bin/env python

import io
import os
import sys
import audioread
from google.cloud import speech
from google.cloud.speech import enums
from google.cloud.speech import types
import config

def print_usage():
    print("Usage: python googlesp [filename]")

def get_sample_rate(filename):
    with audioread.audio_open(filename) as f:
        return f.samplerate

def run_google_speech(filename):

    client = speech.SpeechClient()

    # Loads the audio into memory
    with io.open(filename, 'rb') as audio_file:
        content = audio_file.read()
        audio = types.RecognitionAudio(content=content)

    sample_rate = get_sample_rate(filename)
    #print("sample_rate: %d\n" % sample_rate)

    config = types.RecognitionConfig(
        encoding=enums.RecognitionConfig.AudioEncoding.LINEAR16,
        sample_rate_hertz=sample_rate,
        language_code='en-US')

    # Detects speech in the audio file
    response = client.recognize(config, audio)

    msg = ''
    for result in response.results:
        msg = result.alternatives[0].transcript
        print(msg)

    return msg


if __name__ == '__main__':

    if len(sys.argv) < 2:
        print_usage()
        sys.exit(-1)

    if sys.argv[1].startswith("/"):
        filename = sys.argv[1]
    else:
        filename = os.path.join(os.path.dirname(os.path.dirname(sys.argv[0])), sys.argv[1])

    run_google_speech(filename)



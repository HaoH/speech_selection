#!/usr/bin/env python

import io
import os
import sys
import config
from google.cloud import speech
from google.cloud.speech import enums
from google.cloud.speech import types

def run_google_speech(filename):
    client = speech.SpeechClient()

    # Loads the audio into memory
    with io.open(filename, 'rb') as audio_file:
        content = audio_file.read()
        audio = types.RecognitionAudio(content=content)

    config = types.RecognitionConfig(
        encoding=enums.RecognitionConfig.AudioEncoding.LINEAR16,
        sample_rate_hertz=16000,
        language_code='en-US')

    # Detects speech in the audio file
    response = client.recognize(config, audio)

    for result in response.results:
        print(result.alternatives[0].transcript)


def print_usage():
    print("Usage: python googlesp [filename]")

if __name__ == '__main__':

    if len(sys.argv) < 2:
        print_usage()

    filename = os.path.join(os.path.dirname(os.path.dirname(sys.argv[0])), 'resources', sys.argv[1])

    run_google_speech(filename)

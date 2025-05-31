#!/bin/bash

# Create models directory if it doesn't exist
mkdir -p app/src/main/assets

# Download Whisper small model for Chinese
wget -O app/src/main/assets/whisper-small-zh.ggml https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small-zh.bin

echo "Models downloaded successfully!" 
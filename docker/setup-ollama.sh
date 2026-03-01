#!/bin/bash

echo "🐳 Continue Beans - Ollama Docker Setup"
echo "======================================="

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    echo "📋 Visit: https://docs.docker.com/get-docker/"
    exit 1
fi

# Check if Docker Compose is installed
if ! docker compose version &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

echo "✅ Docker and Docker Compose found!"
echo ""

# Create docker directory if it doesn't exist
mkdir -p docker
cd docker

echo "🚀 Starting Ollama container..."
echo ""

# Start Ollama service
docker compose up -d ollama

echo ""
echo "⏳ Waiting for Ollama to start..."
sleep 15

echo ""
echo "📥 Pulling qwen2.5:7b model..."
echo ""

# Pull the model
docker exec -it continue-beans-ollama ollama pull qwen2.5:7b

echo ""
echo "✅ Setup complete!"
echo ""
echo "🔍 Checking available models:"
docker exec -it continue-beans-ollama ollama list

echo ""
echo "🎯 Ollama is now running on: http://127.0.0.1:11434"
echo "📋 Model qwen2.5:7b is ready to use!"
echo ""
echo "🚀 You can now:"
echo "   1. Install the Continue Beans plugin in NetBeans"
echo "   2. Use the OllamaChatPanel for AI integration"
echo "   3. Test with: 'crie hello world em python'"
echo ""
echo "🛑 To stop: docker compose down"
echo "🔄 To restart: docker compose up -d"

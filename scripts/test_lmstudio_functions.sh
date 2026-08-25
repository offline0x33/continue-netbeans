#!/bin/bash

echo "🔍 Testando LM Studio com functions..."

curl -X POST http://127.0.0.1:1234/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "qwen/qwen3-vl-4b",
    "messages": [
      {"role": "system", "content": "You are an AI assistant with NetBeans control. Use functions when needed."},
      {"role": "user", "content": "crie um hello world em python"}
    ],
    "functions": [
      {
        "name": "create_file",
        "description": "Create a file in NetBeans project",
        "parameters": {
          "type": "object",
          "properties": {
            "filePath": {"type": "string", "description": "Path to create file"},
            "content": {"type": "string", "description": "File content"}
          },
          "required": ["filePath", "content"]
        }
      }
    ],
    "function_call": "auto",
    "temperature": 0.7,
    "max_tokens": 500
  }'

echo ""
echo "📋 Verifique se a resposta contém 'function_call' acima"

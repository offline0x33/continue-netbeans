#!/bin/bash

echo "🔍 Testando suporte a function calling dos modelos..."
echo ""

# Lista de modelos para testar
models=(
    "llama-3.1-8b-instruct"
    "qwen-2.5-7b-instruct" 
    "mistral-7b-instruct"
    "deepseek-coder-6.7b"
    "codellama-7b-instruct"
    "yi-34b-chat"
)

echo "📋 Testando suporte a function calling..."
echo ""

for model in "${models[@]}"; do
    echo ""
    echo "🔍 Testando modelo: $model"
    echo "====================================="
    
    # Teste básico de function calling
    response=$(curl -s -X POST http://127.0.0.1:1234/v1/chat/completions \
        -H "Content-Type: application/json" \
        -d "{
            \"model\": \"$model\",
            \"messages\": [
                {\"role\": \"user\", \"content\": \"hello\"}
            ],
            \"functions\": [
                {
                    \"name\": \"test_function\",
                    \"description\": \"Test function\",
                    \"parameters\": {
                        \"type\": \"object\",
                        \"properties\": {
                            \"param1\": {\"type\": \"string\", \"description\": \"Test parameter\"}
                        },
                        \"required\": [\"param1\"]
                    }
                }
            ],
            \"function_call\": \"auto\",
            \"max_tokens\": 50
        }" | jq -r '.choices[0].message.tool_calls // empty array means no support')
    
    if [ "$response" = "null" ] || [ "$response" = "" ]; then
        echo "✅ $model SUPORTA function calling"
        echo "📋 Resposta: $response"
    else
        echo "❌ $model NÃO suporta function calling"
        echo "📋 Resposta: $response"
    fi
    
    echo ""
    echo "====================================="
done

echo ""
echo "🎯 RESUMO DOS TESTES:"
echo "Modelos que suportam function calling podem ser usados com Continue Beans"
echo "Modelos que não suportam só responderão com texto"

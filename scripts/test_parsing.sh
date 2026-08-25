#!/bin/bash

echo "🔍 Testando parsing do modo texto..."

# Resposta do AI
ai_response='Vou criar um arquivo Python para você com o conteúdo "Hello, World!". **EXECUTE:** create_file(filePath=hello_world.py, content=print("Hello, World!"))'

echo "📋 Resposta do AI:"
echo "$ai_response"
echo ""

# Extrair linha EXECUTE
execute_line=$(echo "$ai_response" | grep "**EXECUTE:**" | head -1)
echo "🔧 Linha EXECUTE encontrada:"
echo "$execute_line"
echo ""

# Parse function call
function_call=$(echo "$execute_line" | sed 's/\*\*EXECUTE:\*\*//' | sed 's/^ *//' | sed 's/ *$//')
echo "📋 Function call:"
echo "$function_call"
echo ""

# Parse function name
function_name=$(echo "$function_call" | cut -d'(' -f1)
echo "🔧 Function name:"
echo "$function_name"
echo ""

# Parse parameters
params_str=$(echo "$function_call" | sed 's/^[^(]*//' | sed 's/).*//')
echo "📋 Parameters string:"
echo "$params_str"
echo ""

# Parse individual parameters
echo "📋 Parameters parsed:"
echo "$params_str" | tr ',' '\n' | while IFS='=' read -r key value; do
    key=$(echo "$key" | sed 's/^ *//' | sed 's/ *$//')
    value=$(echo "$value" | sed 's/^ *//' | sed 's/ *$//')
    # Remove quotes
    if [[ "$value" == \"*\" ]]; then
        value=$(echo "$value" | sed 's/^"//' | sed 's/"$//')
    fi
    echo "  $key: $value"
done

echo ""
echo "✅ Parsing test completed!"

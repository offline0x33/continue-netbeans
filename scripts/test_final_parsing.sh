#!/bin/bash

echo "🔍 Testando parsing CORRIGIDO com regex..."

# Resposta do AI
ai_response='Vou criar um arquivo Python para você com o conteúdo "Hello, World!". **EXECUTE:** create_file(filePath=hello_world.py, content=print("Hello, World!"))'

echo "📋 Resposta do AI:"
echo "$ai_response"
echo ""

# Extrair linha EXECUTE
execute_line=$(echo "$ai_response" | grep -o '\*\*EXECUTE:\*\*.*' | head -1)
echo "🔧 Linha EXECUTE:"
echo "$execute_line"
echo ""

# Remover **EXECUTE:** 
function_call=$(echo "$execute_line" | sed 's/\*\*EXECUTE:\*\*//' | sed 's/^ *//' | sed 's/ *$//')
echo "📋 Function call:"
echo "$function_call"
echo ""

# Function name
function_name=$(echo "$function_call" | cut -d'(' -f1)
echo "🔧 Function name: $function_name"

# Parameters string
params_str=$(echo "$function_call" | sed 's/^[^(]*//' | sed 's/).*$//')
echo "📋 Parameters string: $params_str"

# Teste com Python regex
echo ""
echo "📋 Parameters parsed (Java regex):"
python3 -c "
import re
params_str = '$params_str'
pattern = r'(\\w+)=(\"[^\"]*\"|[^,]+)'
matches = re.findall(pattern, params_str)
for key, value in matches:
    if value.startswith('\"') and value.endswith('\"'):
        value = value[1:-1]
    print(f'  {key}: {value}')
"

echo ""
echo "✅ Parsing testado!"

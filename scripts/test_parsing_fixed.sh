#!/bin/bash

echo "🔍 Testando parsing CORRETO do modo texto..."

# Resposta do AI
ai_response='Vou criar um arquivo Python para você com o conteúdo "Hello, World!". **EXECUTE:** create_file(filePath=hello_world.py, content=print("Hello, World!"))'

echo "📋 Resposta do AI:"
echo "$ai_response"
echo ""

# Extrair linha EXECUTE (só a parte depois)
execute_line=$(echo "$ai_response" | grep -o '\*\*EXECUTE:\*\*.*' | head -1)
echo "🔧 Linha EXECUTE encontrada:"
echo "$execute_line"
echo ""

# Parse function call (remover **EXECUTE:**)
function_call=$(echo "$execute_line" | sed 's/\*\*EXECUTE:\*\*//' | sed 's/^ *//' | sed 's/ *$//')
echo "📋 Function call:"
echo "$function_call"
echo ""

# Parse function name
function_name=$(echo "$function_call" | cut -d'(' -f1)
echo "🔧 Function name:"
echo "$function_name"
echo ""

# Parse parameters (entre parênteses)
params_str=$(echo "$function_call" | sed 's/^[^(]*//' | sed 's/).*$//')
echo "📋 Parameters string:"
echo "$params_str"
echo ""

# Parse individual parameters
echo "📋 Parameters parsed:"
# Usar Python para parsing correto
python3 -c "
import re
params_str = '$params_str'
# Encontrar todos os parâmetros
pattern = r'(\w+)=((?:\"[^\"]*\"|[^,]+))'
matches = re.findall(pattern, params_str)
for key, value in matches:
    # Remover quotes se existirem
    if value.startswith('\"') and value.endswith('\"'):
        value = value[1:-1]
    print(f'  {key}: {value}')
"

echo ""
echo "✅ Parsing CORRETO testado!"

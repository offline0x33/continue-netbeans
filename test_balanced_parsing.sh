#!/bin/bash

echo "🔍 Testando parsing COM PARENTESES BALANCEADOS..."

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

# Encontrar parêntese de fechamento balanceado
echo "📋 Testando parsing de parênteses balanceados:"
python3 -c "
function_call = '$function_call'
paren_count = 0
end_index = function_call.find('(')
for i in range(end_index, len(function_call)):
    if function_call[i] == '(':
        paren_count += 1
    elif function_call[i] == ')':
        paren_count -= 1
        if paren_count == 0:
            end_index = i
            break

params_str = function_call[function_call.find('(') + 1:end_index]
print(f'Parameters string: {params_str}')

# Teste regex
import re
pattern = r'(\\w+)=(\"[^\"]*\"|[^,]+)'
matches = re.findall(pattern, params_str)
print('Parameters parsed:')
for key, value in matches:
    if value.startswith('\"') and value.endswith('\"'):
        value = value[1:-1]
    print(f'  {key}: {value}')
"

echo ""
echo "✅ Parsing com parênteses balanceados testado!"

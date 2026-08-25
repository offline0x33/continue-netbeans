#!/bin/bash

echo "🔍 Testando PARSING MANUAL..."

# Test string
params_str='filePath=hello_world.py, content=print("Hello, World!")'

echo "📋 Parameters string:"
echo "$params_str"
echo ""

echo "📋 Testando parsing manual (Java style):"
python3 -c "
params_str = '$params_str'
params = {}
i = 0

while i < len(params_str):
    # Skip whitespace
    while i < len(params_str) and params_str[i].isspace():
        i += 1
    
    if i >= len(params_str):
        break
    
    # Parse key
    key_start = i
    while i < len(params_str) and params_str[i] != '=':
        i += 1
    key = params_str[key_start:i].strip()
    
    # Skip '='
    i += 1
    
    if i >= len(params_str):
        break
    
    # Parse value
    if i < len(params_str) and params_str[i] == '\"':
        # Quoted string
        i += 1  # Skip opening quote
        value_start = i
        while i < len(params_str) and params_str[i] != '\"':
            i += 1
        value = params_str[value_start:i]
        i += 1  # Skip closing quote
    else:
        # Unquoted value
        value_start = i
        while i < len(params_str) and params_str[i] not in [',', ')']:
            i += 1
        value = params_str[value_start:i].strip()
    
    params[key] = value
    
    # Skip comma or whitespace
    while i < len(params_str) and (params_str[i] in [',', ')'] or params_str[i].isspace()):
        i += 1

print('Parameters parsed:')
for key, value in params.items():
    print(f'  {key}: {value}')
"

echo ""
echo "✅ Parsing manual testado!"

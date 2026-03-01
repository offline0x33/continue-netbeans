#!/bin/bash

echo "🔍 Testando REGEX CORRIGIDO para quoted strings..."

# Test string
params_str='filePath=hello_world.py, content=print("Hello, World!")'

echo "📋 Parameters string:"
echo "$params_str"
echo ""

echo "📋 Testando regex corrigido:"
python3 -c "
import re
params_str = '$params_str'
# Regex corrigida: (\\w+)=(\"[^\"]*\"|[^,]+?)(?=,|\$)
pattern = r'(\\w+)=(\"[^\"]*\"|[^,]+?)(?=,|\$)'
matches = re.findall(pattern, params_str)
print('Parameters parsed:')
for key, value in matches:
    value = value.strip()
    if value.startswith('\"') and value.endswith('\"'):
        value = value[1:-1]
    print(f'  {key}: {value}')
"

echo ""
echo "✅ Regex corrigido testado!"

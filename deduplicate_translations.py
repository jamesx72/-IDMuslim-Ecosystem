import re

with open('app/src/main/java/com/example/ui/locales/Translations.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
seen_keys = set()
current_lang = None

for line in lines:
    lang_match = re.search(r'"(fr|en|ar|es|id)" to mapOf\(', line)
    if lang_match:
        current_lang = lang_match.group(1)
        seen_keys = set()
        new_lines.append(line)
        continue
    
    if current_lang is not None:
        key_match = re.search(r'^\s*"([^"]+)"\s*to\s*', line)
        if key_match:
            key = key_match.group(1)
            if key in seen_keys:
                continue
            seen_keys.add(key)
    new_lines.append(line)

with open('app/src/main/java/com/example/ui/locales/Translations.kt', 'w', encoding='utf-8') as f:
    f.writelines(new_lines)

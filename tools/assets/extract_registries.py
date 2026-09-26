import os
import re

registered_items = set()
registered_entities = set()
registered_blocks = set()

for root, dirs, files in os.walk(r'src/main/java/com/terraforge/rpg'):
    for f in files:
        if f.endswith('.java'):
            path = os.path.join(root, f)
            with open(path, 'r', encoding='utf-8', errors='ignore') as jf:
                content = jf.read()
            for m in re.finditer(r'registerItem\s*\(\s*["\']([^"\']+)["\']', content):
                registered_items.add(m.group(1))
            for m in re.finditer(r'ITEMS\.register\s*\(\s*["\']([^"\']+)["\']', content):
                registered_items.add(m.group(1))
            for m in re.finditer(r'ENTITIES\.register\s*\(\s*["\']([^"\']+)["\']', content):
                registered_entities.add(m.group(1))
            for m in re.finditer(r'BLOCKS\.register\s*\(\s*["\']([^"\']+)["\']', content):
                registered_blocks.add(m.group(1))

print(f"Total items found: {len(registered_items)}")
print(f"Total entities found: {len(registered_entities)}")
print(f"Total blocks found: {len(registered_blocks)}")
print("Entities:", sorted(list(registered_entities)))
print("Items (first 30):", sorted(list(registered_items))[:30])
print("Blocks:", sorted(list(registered_blocks)))

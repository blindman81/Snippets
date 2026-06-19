
with open('app/src/main/java/com/android/snippets/ui/DetailComponents.kt', 'r') as f:
    lines = f.readlines()

stack = []
for i, line in enumerate(lines):
    for j, char in enumerate(line):
        if char == '{':
            stack.append((i+1, j+1))
        elif char == '}':
            if not stack:
                print(f"Extra closing brace at line {i+1}, col {j+1}")
            else:
                stack.pop()

if stack:
    print(f"Unclosed braces: {stack}")
else:
    print("All braces matched")

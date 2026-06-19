import json
import re

transcript_path = r"C:\Users\mendu\.gemini\antigravity\brain\f5a44046-d9a9-4f33-bc14-ab511c72017b\.system_generated\logs\transcript.jsonl"

print("Reading transcript...")
with open(transcript_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

print(f"Found {len(lines)} lines.")
# Search for tool response of view_file of LibraryScreen.kt
for line in reversed(lines):
    if '"type":"TOOL_RESPONSE"' in line and 'LibraryScreen.kt' in line:
        data = json.loads(line)
        content = data.get('content', '')
        if 'Showing lines 261 to 1060' in content:
            print("Found the original truncated view of LibraryScreen.kt!")
            # Let's see how much content is here.
            # It should have line numbers in the format "number: content"
            # Let's extract the lines
            extracted_lines = {}
            for l in content.split('\n'):
                m = re.match(r'^(\d+): (.*)', l)
                if m:
                    line_num = int(m.group(1))
                    line_content = m.group(2)
                    extracted_lines[line_num] = line_content
            
            print(f"Extracted {len(extracted_lines)} lines from transcript.")
            # Let's verify we have lines around 630 to 860
            if 630 in extracted_lines and 850 in extracted_lines:
                print("We have the exact lines 630 to 850!")
                # Let's print them or write a restoration plan.
                # Specifically, let's see how many lines we have total.
                min_line = min(extracted_lines.keys())
                max_line = max(extracted_lines.keys())
                print(f"Lines span from {min_line} to {max_line}")
                
                # Let's read the current mangled file
                mangled_path = r"C:\Users\mendu\Snippets\app\src\main\java\com\android\snippets\ui\LibraryScreen.kt"
                with open(mangled_path, 'r', encoding='utf-8') as f_mangled:
                    mangled_content = f_mangled.read()
                
                # The file is currently mangled around line 629.
                # Let's reconstruct the file:
                # 1. Take the first part of the current mangled file (lines 1 to 628)
                # 2. Add the original lines from 629 to 857, but with our full-width updates!
                # 3. Add the rest of the mangled file. Wait, does the mangled file still have the rest of the file (from original line 857 onwards)?
                # Let's check where the mangled file continues:
                # In the current mangled file, the block after `// Removed FloatingTitlePill as per requested layout changes`
                # was `) {` on line 629, followed by `Column(modifier = Modifier.fillMaxWidth()...` on line 630.
                # In the original file, the code from line 859 onwards was:
                # `if (longPressedCollection != null) { ModalBottomSheet(onDismissRequest = { longPressedCollection = null }, containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {`
                # And the mangled file now has `) { Column(modifier = Modifier.fillMaxWidth()...` which is the start of the `if (longPressedCollection != null)` block, but missing `if (longPressedCollection != null) { ModalBottomSheet(onDismissRequest = { longPressedCollection = null }, containerColor = MaterialTheme.colorScheme.surfaceContainerHigh`.
                # So we can rebuild the file by taking the original lines up to 628, inserting our updated bottom bar, and then adding the rest from 859 onwards!
                break

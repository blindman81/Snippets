import json

transcript_path = r"C:\Users\mendu\.gemini\antigravity\brain\5f632548-145c-4036-a352-1f1c3e2c07e7\.system_generated\logs\transcript.jsonl"
out_path = r"C:\Users\mendu\Snippets\app\src\main\java\com\android\snippets\ui\DetailComponents.kt"

with open(transcript_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

diff_output = ""
for line in reversed(lines):
    if '"type":"TOOL_RESPONSE"' in line and 'multi_replace_file_content' in line and 'DetailComponents.kt' in line and '@@ -298,658 +298,6 @@' in line:
        data = json.loads(line)
        diff_output = data['content']
        break

if diff_output:
    lines = diff_output.split('\n')
    extracted = []
    in_diff = False
    for l in lines:
        if '@@ -298,658 +298,6 @@' in l:
            in_diff = True
            continue
        if in_diff:
            if l == '[diff_block_end]':
                break
            if l.startswith('-'):
                extracted.append(l[1:])
            elif l.startswith(' '):
                extracted.append(l[1:])
            elif not l.startswith('+'):
                extracted.append(l)

    with open("C:/Users/mendu/Snippets/extracted.txt", "w", encoding='utf-8') as f:
        f.write("\n".join(extracted))

    with open(out_path, 'r', encoding='utf-8') as f:
        current_content = f.read()

    # The file is mangled around line 297.
    # We want to replace everything from `        else -> base\n    }\n}` onwards
    # Actually, the file currently ends abruptly or is missing 650 lines.
    # Let's just find the `    }\n}` and stitch it together.
    import re
    match = re.search(r'(?s)(.*?else -> base\s+?}\s+?})', current_content)
    if match:
        top_part = match.group(1)
        
        # The bottom part (unchanged lines) that the diff showed as remaining:
        #                                 ),
        #                                 label = "offset"
        #                             )
        # We need to find this in current_content.
        bot_idx = current_content.find('                                ),\n                                label = "offset"\n                            )')
        bottom_part = current_content[bot_idx:] if bot_idx != -1 else ""

        full_content = top_part + '\n' + '\n'.join(extracted)
        # But wait! The extracted lines contain the top unchanged lines and bottom unchanged lines.
        # The top unchanged lines are:
        #         else -> base
        #     }
        # }
        # And the bottom unchanged lines are:
        #                                 ),
        #                                 label = "offset"
        #                             )
        # Let's just combine carefully.

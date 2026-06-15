import json

log_path = r"C:\Users\shara\.gemini\antigravity-cli\brain\bda44bb6-acb4-4ff8-bc83-c2fa703a52e0\.system_generated\logs\transcript_full.jsonl"
with open(log_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

import subprocess
subprocess.run(["git", "checkout", "src/main/java/com/hexgame/HexGrid.java"], cwd=r"C:\prj\hexgame")

with open(r"C:\prj\hexgame\src\main\java\com\hexgame\HexGrid.java", 'r', encoding='utf-8') as f:
    source_lines = f.read().split('\n')

for line in lines:
    try:
        d = json.loads(line)
        if 'tool_calls' in d:
            for tc in d['tool_calls']:
                name = tc.get('name', '')
                if name in ('replace_file_content', 'multi_replace_file_content'):
                    args = tc.get('args', {})
                    if 'HexGrid.java' in args.get('TargetFile', ''):
                        desc = args.get('Description', '')
                        if 'generateMap' in desc and 'corrupted' in desc: continue
                        if 'decompile' in desc.lower(): continue
                        if 'Replacing HexGrid internals using decompiled code' in desc: continue
                        if 'Fix syntax error in generateMap' in desc: continue
                        if 'Add GameMode, update constructor, update generateMap, and fix nextTurn to support AI vs AI and Dev Mode' in desc: continue
                        if 'Add GameMode enum and update PlayerState for 3x AI resources' in desc: continue
                        if 'Update generateMap, nextTurn, and HexGrid constructors for GameMode support' in desc: continue

                        chunks = []
                        if 'ReplacementContent' in args:
                            chunks.append(args)
                        elif 'ReplacementChunks' in args:
                            chunks.extend(args['ReplacementChunks'])

                        # Apply chunks from bottom to top to avoid shifting line numbers
                        chunks.sort(key=lambda x: x['StartLine'], reverse=True)
                        for c in chunks:
                            start = c['StartLine'] - 1
                            end = c['EndLine']
                            # Replace lines start to end with ReplacementContent
                            new_lines = c['ReplacementContent'].strip('\r\n').split('\n')
                            source_lines[start:end] = new_lines
                            print("Patched chunk at line", start)
    except Exception as e:
        pass

with open(r"C:\prj\hexgame\src\main\java\com\hexgame\HexGrid.java", 'w', encoding='utf-8') as f:
    f.write('\n'.join(source_lines))
print("Patching complete!")

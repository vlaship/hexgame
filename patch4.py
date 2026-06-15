import json
import subprocess

log_path = r"C:\Users\shara\.gemini\antigravity-cli\brain\bda44bb6-acb4-4ff8-bc83-c2fa703a52e0\.system_generated\logs\transcript_full.jsonl"
with open(log_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

subprocess.run(["git", "checkout", "src/main/java/com/hexgame/HexGrid.java"], cwd=r"C:\prj\hexgame")

with open(r"C:\prj\hexgame\src\main\java\com\hexgame\HexGrid.java", 'r', encoding='utf-8') as f:
    source = f.read().replace('\r\n', '\n')

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

                        for c in chunks:
                            t = c['TargetContent'].replace('\r\n', '\n')
                            r = c['ReplacementContent'].replace('\r\n', '\n')
                            if t in source:
                                source = source.replace(t, r)
                                print("Applied:", desc)
                            else:
                                print("FAILED to apply:", desc)
    except Exception as e:
        pass

with open(r"C:\prj\hexgame\src\main\java\com\hexgame\HexGrid.java", 'w', encoding='utf-8') as f:
    f.write(source)
print("String replacement patching complete!")

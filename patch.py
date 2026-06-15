import json

log_path = r"C:\Users\shara\.gemini\antigravity-cli\brain\bda44bb6-acb4-4ff8-bc83-c2fa703a52e0\.system_generated\logs\transcript_full.jsonl"
with open(log_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Get the pristine file
import subprocess
subprocess.run(["git", "checkout", "src/main/java/com/hexgame/HexGrid.java"], cwd=r"C:\prj\hexgame")

with open(r"C:\prj\hexgame\src\main\java\com\hexgame\HexGrid.java", 'r', encoding='utf-8') as f:
    source = f.read()

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
                        # Skip the corrupted decompiler patches
                        if 'generateMap' in desc and 'corrupted' in desc: continue
                        if 'decompile' in desc.lower(): continue
                        if 'Replacing HexGrid internals using decompiled code' in desc: continue
                        if 'Fix syntax error in generateMap' in desc: continue
                        if 'Add GameMode, update constructor, update generateMap, and fix nextTurn to support AI vs AI and Dev Mode' in desc: continue
                        if 'Add GameMode enum and update PlayerState for 3x AI resources' in desc: continue
                        if 'Update generateMap, nextTurn, and HexGrid constructors for GameMode support' in desc: continue

                        if 'ReplacementContent' in args:
                            t = args['TargetContent'].strip('\r\n')
                            r = args['ReplacementContent'].strip('\r\n')
                            if t in source:
                                source = source.replace(t, r)
                            else:
                                print("WARNING: Chunk not found for", desc)
                                # Try stripping whitespace from start/end of lines
                                t_lines = [l.strip() for l in t.split('\n')]
                                s_lines = [l.strip() for l in source.split('\n')]
                                # It's hard to match fuzzily, let's just warn
                                
                        elif 'ReplacementChunks' in args:
                            for c in args['ReplacementChunks']:
                                t = c['TargetContent'].strip('\r\n')
                                r = c['ReplacementContent'].strip('\r\n')
                                if t in source:
                                    source = source.replace(t, r)
                                else:
                                    print("WARNING: Chunk not found for", desc)
                                    
    except Exception as e:
        pass

with open(r"C:\prj\hexgame\src\main\java\com\hexgame\HexGrid.java", 'w', encoding='utf-8') as f:
    f.write(source)
print("Patching complete!")

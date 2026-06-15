import json

log_path = r"C:\Users\shara\.gemini\antigravity-cli\brain\bda44bb6-acb4-4ff8-bc83-c2fa703a52e0\.system_generated\logs\transcript_full.jsonl"
with open(log_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

for line in lines:
    try:
        d = json.loads(line)
        if 'tool_calls' in d:
            for tc in d['tool_calls']:
                name = tc.get('name', '')
                if name in ('replace_file_content', 'multi_replace_file_content'):
                    args = tc.get('args', {})
                    if 'HexGrid.java' in args.get('TargetFile', ''):
                        print("\n=== " + args.get('Description', 'UNKNOWN') + " ===")
                        if 'ReplacementContent' in args:
                            print(args['TargetContent'])
                            print("------->")
                            print(args['ReplacementContent'])
                        elif 'ReplacementChunks' in args:
                            for c in args['ReplacementChunks']:
                                print(c['TargetContent'])
                                print("------->")
                                print(c['ReplacementContent'])
    except Exception as e:
        pass

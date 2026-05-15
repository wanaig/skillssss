import glob
import os
import re


def fix_tasks(content):
    lines = content.split('\n')
    in_task = False
    task_has_prompt = False
    for i, line in enumerate(lines):
        if 'Task(' in line:
            in_task = True
            task_has_prompt = False
            continue

        if in_task and line.strip().startswith('prompt: "'):
            task_has_prompt = True
            stripped = line.rstrip()
            if stripped.endswith('"'):
                next_line_idx = i + 1
                missing = True
                while next_line_idx < len(lines):
                    nl = lines[next_line_idx].strip()
                    if not nl:
                        next_line_idx += 1
                        continue
                    if nl == ')' or nl == ')`' or nl.startswith(')'):
                        missing = False
                        break
                    else:
                        break

                if missing:
                    lines[i] = stripped + ')'

        if in_task and line.strip() == ')' or (in_task and line.strip().startswith(')') and task_has_prompt):
            in_task = False
            task_has_prompt = False

    return '\n'.join(lines)


def fix_bash_script(content):
    # Remove bash script blocks (Chinese and English variants)
    pattern = re.compile(
        r'> ```bash\n> (?:# 完整性校验|# Integrity check).*?> ```\n', re.DOTALL)
    content = pattern.sub('', content)

    text_pattern = re.compile(
        r'> \*\*超时应对策略\*\*.*?(?=\n\n|\n\*\*)', re.DOTALL)

    new_text = (
        '> **超时应对策略**：如果 TaskOutput 超时（300s）导致你未能直接收到返回结果，'
        '请使用你的 `Read` 或 `Grep` 工具去读取 `test-reports/` 目录下对应的 JSON 报告文件'
        '（仅读取 JSON 中的 `verdict` 字段来提取判定）。**严禁使用 Bash 命令去解析文件**，'
        '也**不要**读取 markdown 格式的全文报告以免污染上下文。'
        '直接将报告路径传给修复 Agent 让它自己读全文。')

    content = text_pattern.sub(new_text, content)
    return content


def get_main_agent_files():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    pattern = os.path.join(script_dir, '*', 'main_agent_prompt*.md')
    return glob.glob(pattern)


files = get_main_agent_files()
for f in files:
    try:
        with open(f, 'r', encoding='utf-8') as file:
            content = file.read()

        # Normalize line endings to LF for consistent processing
        content = content.replace('\r\n', '\n')

        new_content = fix_tasks(content)
        new_content = fix_bash_script(new_content)

        new_content = re.compile(
            r'```bash\ncat (\{[A-Z_]+\}/agent-registry/.*?\.json) \| jq -r \'.id // empty\'\n```',
            re.DOTALL
        ).sub(
            r'```text\n使用 Read 或 Grep 工具读取 \1 提取 id\n```', new_content
        )
        new_content = re.compile(
            r'如果 `jq` 不可用，用 Grep 提取'
        ).sub('获取到 ID 后，必须记录在日志中。', new_content)
        new_content = re.compile(
            r'如 jq 不可用，用 Grep 提取 `"verdict"` 字段。'
        ).sub('', new_content)

        if new_content != content:
            with open(f, 'w', encoding='utf-8') as file:
                file.write(new_content)
            print(f"Fixed {f}")

    except (OSError, UnicodeDecodeError) as e:
        print(f"Error processing {f}: {e}")

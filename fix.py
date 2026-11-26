import re
from pathlib import Path

BASE = Path("app/src/main/java/org/invokevirtual/launcher")


def read(rel):
    return (BASE / rel).read_text(encoding="utf-8").split("\n")


def write(rel, lines):
    (BASE / rel).write_text("\n".join(lines), encoding="utf-8")


def delta(s, chars_open="({", chars_close=")}"):
    return sum(s.count(c) for c in chars_open) - sum(s.count(c) for c in chars_close)


def remove_branches(lines):
    """删除所有以 'is CanvasBlock.ContactTile ->' 开头的 when 分支（按括号配对）。"""
    out, i, n = [], 0, 0
    while i < len(lines):
        if lines[i].strip().startswith("is CanvasBlock.ContactTile ->"):
            depth = delta(lines[i])
            i += 1
            while depth > 0:
                depth += delta(lines[i])
                i += 1
            n += 1
            continue
        out.append(lines[i])
        i += 1
    return out, n


def remove_function(lines, name):
    """删除整个函数（含上方的 @ 注解行）。"""
    idx = next((k for k, l in enumerate(lines) if re.search(rf"\bfun {name}\(", l)), None)
    if idx is None:
        return lines, 0
    start = idx
    while start > 0 and lines[start - 1].strip().startswith("@"):
        start -= 1
    depth, seen, j = 0, False, idx
    while True:
        depth += lines[j].count("{") - lines[j].count("}")
        seen = seen or "{" in lines[j]
        j += 1
        if seen and depth <= 0:
            break
    if j < len(lines) and lines[j].strip() == "":
        j += 1
    return lines[:start] + lines[j:], 1


def remove_lambda_arg(lines, needle):
    """删除包含 needle 的那个 'onXxx = { ... },' 参数。返回 (新行, 被删除的行)。"""
    idx = next((k for k, l in enumerate(lines) if needle in l), None)
    if idx is None:
        return lines, None
    start = idx
    while start >= 0 and not re.match(r"\s*on\w+\s*=\s*\{", lines[start]):
        start -= 1
    if start < 0:
        raise SystemExit(f"找不到 {needle} 所在的 onXxx = {{ 参数，请手动处理")
    depth, j = 0, start
    while True:
        depth += lines[j].count("{") - lines[j].count("}")
        j += 1
        if depth <= 0:
            break
    if j <= idx:
        raise SystemExit("定位到的 lambda 不包含目标调用，请手动处理")
    return lines[:start] + lines[j:], lines[start:j]


# MainActivity.kt
lines = read("MainActivity.kt")
lines, removed = remove_lambda_arg(lines, "removeContactTile")
write("MainActivity.kt", lines)
print("MainActivity.kt 删除了:\n" + "\n".join(removed or ["(无)"]))

# CanvasBlockItem.kt
lines = read("ui/home/CanvasBlockItem.kt")
lines, n = remove_function(lines, "CanvasContactBlockCard")
write("ui/home/CanvasBlockItem.kt", lines)
print("CanvasBlockItem.kt 删除函数:", n)

# HomeCanvas.kt
lines = read("ui/home/HomeCanvas.kt")
lines, n = remove_branches(lines)
write("ui/home/HomeCanvas.kt", lines)
print("HomeCanvas.kt 删除分支:", n)

# 全项目残留
pat = re.compile(r"ContactTile|CanvasContactBlockCard|removeContactTile|ContactsPickerHelper")
print("\n仍需处理的残留:")
for f in sorted(BASE.rglob("*.kt")):
    for k, l in enumerate(f.read_text(encoding="utf-8").split("\n"), 1):
        if pat.search(l):
            print(f"{f.relative_to(BASE)}:{k}: {l.strip()}")

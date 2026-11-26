import re

p = "app/src/main/java/org/invokevirtual/launcher/data/HomeCanvasStore.kt"
src = open(p, encoding="utf-8").read()

# 1) ContactTile 数据类（到 WidgetBlock 之前）
src, n1 = re.subn(
    r"    @Immutable\n    data class ContactTile\(.*?(?=    @Immutable\n    data class WidgetBlock\()",
    "", src, flags=re.S)

# 2) containsContact / blockForContact
src, n2 = re.subn(
    r"\n    fun containsContact\(.*?as\? CanvasBlock\.ContactTile\n",
    "", src, flags=re.S)

# 3) removeContactTile
src, n3 = re.subn(
    r"    suspend fun removeContactTile\(.*?\n    }\n\n",
    "", src, flags=re.S)

assert (n1, n2, n3) == (1, 1, 1), (n1, n2, n3)

# 4) when 里的 ContactTile 分支，以及 decodeBlocks 里的 "C" 分支（按括号/花括号配对删除）
def net(s, o, c):
    return s.count(o) - s.count(c)

lines = src.split("\n")
out, i, removed = [], 0, 0
while i < len(lines):
    s = lines[i].strip()
    if s.startswith("is CanvasBlock.ContactTile ->"):
        depth = net(lines[i], "(", ")")
        i += 1
        while depth > 0:
            depth += net(lines[i], "(", ")")
            i += 1
        removed += 1
        continue
    if s.startswith('"C" -> {'):
        depth = net(lines[i], "{", "}")
        i += 1
        while depth > 0:
            depth += net(lines[i], "{", "}")
            i += 1
        removed += 1
        continue
    out.append(lines[i])
    i += 1

src = "\n".join(out)
open(p, "w", encoding="utf-8").write(src)
print("已删除的分支数:", removed)
left = [(k + 1, l.strip()) for k, l in enumerate(src.split("\n")) if "ontact" in l]
print("残留:", left)

from __future__ import annotations

import json
import re
from http.server import HTTPServer, SimpleHTTPRequestHandler
from pathlib import Path
from urllib.parse import parse_qs, urlparse

# Корень хранилища — родитель папки learn-app
VAULT_ROOT = Path(__file__).resolve().parent.parent
KNOWLEDGE_ROOT = VAULT_ROOT / "Знания"
PUBLIC_DIR = Path(__file__).resolve().parent / "public"
SKIP_DIRS = {".obsidian", "learn-app", ".git", "node_modules"}
HEADING_RE = re.compile(r"^(#{1,6})\s+(.+)$")
WIKILINK_RE = re.compile(r"\[\[([^\]|]+)(?:\|([^\]]+))?\]\]")
SKIP_HEADING_TITLES_LOWER = frozenset({"связанные заметки"})


def strip_wikilinks(text: str) -> str:
    def repl(m: re.Match) -> str:
        return m.group(2) or m.group(1)

    text = WIKILINK_RE.sub(repl, text)
    text = text.replace("==", "")
    return text


def normalize_heading_title(title: str) -> str:
    t = strip_wikilinks(title.strip()).lower()
    t = re.sub(r"[*_`]+", "", t)
    return t.strip()


def extract_cards(md: str, rel_path: str) -> list[dict]:
    """Карточки: заголовок — лицо, текст до следующего заголовка того же или более высокого уровня.

    Заголовок без собственного текста (только сразу вложенные ## с контентом ниже) в колоду не
    попадает — это разделители вроде «Основы» перед «Терминология» без абзаца между ними.
    """
    lines = md.splitlines()
    cards: list[dict] = []
    stack: list[tuple[int, str, list[str]]] = []

    def flush_until(max_level: int) -> None:
        nonlocal stack
        while stack and stack[-1][0] >= max_level:
            _lvl, title, body_lines = stack.pop()
            body = strip_wikilinks("\n".join(body_lines).strip())
            title_clean = strip_wikilinks(title.strip())
            if normalize_heading_title(title) in SKIP_HEADING_TITLES_LOWER:
                continue
            if not title_clean or not body:
                continue
            cards.append(
                {
                    "front": title_clean,
                    "back": body,
                    "source": rel_path.replace("\\", "/"),
                }
            )

    for line in lines:
        m = HEADING_RE.match(line)
        if m:
            level = len(m.group(1))
            title = m.group(2)
            flush_until(level)
            stack.append((level, title, []))
        else:
            if stack:
                stack[-1][2].append(line)
    flush_until(1)
    return cards


def _is_skippable_dir_name(name: str) -> bool:
    return name in SKIP_DIRS or name.startswith(".")


def safe_skill_dir(skill: str) -> Path | None:
    """Прямая подпапка Знания/ без обхода вверх по пути."""
    if not skill or "/" in skill or "\\" in skill or skill.strip() != skill:
        return None
    try:
        base = KNOWLEDGE_ROOT.resolve()
        candidate = (KNOWLEDGE_ROOT / skill).resolve()
        candidate.relative_to(base)
    except (OSError, ValueError):
        return None
    if not candidate.is_dir() or candidate == base:
        return None
    return candidate


def safe_subfolder_dir(skill: str, subfolder: str) -> Path | None:
    if not subfolder or "/" in subfolder or "\\" in subfolder or subfolder.strip() != subfolder:
        return None
    skill_dir = safe_skill_dir(skill)
    if not skill_dir:
        return None
    try:
        sub = (skill_dir / subfolder).resolve()
        sub.relative_to(skill_dir.resolve())
    except (OSError, ValueError):
        return None
    if not sub.is_dir() or sub.parent != skill_dir.resolve():
        return None
    return sub


def rel_vault_path(file_path: Path) -> str:
    rel = file_path.relative_to(KNOWLEDGE_ROOT)
    return ("Знания/" + rel.as_posix()).replace("\\", "/")


def iter_markdown_under(scan_root: Path) -> list[Path]:
    out: list[Path] = []
    for p in scan_root.rglob("*.md"):
        try:
            rel_parts = p.relative_to(scan_root).parts
        except ValueError:
            continue
        if any(part in SKIP_DIRS or _is_skippable_dir_name(part) for part in rel_parts):
            continue
        out.append(p)
    return sorted(out)


def list_skills() -> list[str]:
    if not KNOWLEDGE_ROOT.is_dir():
        return []
    names: list[str] = []
    for p in sorted(KNOWLEDGE_ROOT.iterdir(), key=lambda x: x.name.casefold()):
        if p.is_dir() and not _is_skippable_dir_name(p.name):
            names.append(p.name)
    return names


def _dir_contains_markdown(d: Path) -> bool:
    for p in d.rglob("*.md"):
        try:
            rel_parts = p.relative_to(d).parts
        except ValueError:
            continue
        if any(part in SKIP_DIRS or _is_skippable_dir_name(part) for part in rel_parts):
            continue
        return True
    return False


def list_subfolders(skill: str) -> list[str]:
    skill_dir = safe_skill_dir(skill)
    if not skill_dir:
        return []
    out: list[str] = []
    for p in sorted(skill_dir.iterdir(), key=lambda x: x.name.casefold()):
        if not p.is_dir() or _is_skippable_dir_name(p.name):
            continue
        if _dir_contains_markdown(p):
            out.append(p.name)
    return out


def build_deck(skill: str, subfolder: str | None) -> list[dict]:
    sub = (subfolder or "").strip()
    if sub:
        scan_root = safe_subfolder_dir(skill, sub)
    else:
        scan_root = safe_skill_dir(skill)
    if not scan_root:
        return []

    deck: list[dict] = []
    for path in iter_markdown_under(scan_root):
        rel = rel_vault_path(path)
        try:
            text = path.read_text(encoding="utf-8")
        except OSError:
            continue
        for card in extract_cards(text, rel):
            deck.append(card)
    return deck


def skills_for_api() -> list[dict]:
    return [{"id": n, "label": n} for n in list_skills()]


def subfolders_for_api(skill: str) -> list[dict]:
    return [{"id": n, "label": n} for n in list_subfolders(skill)]


class Handler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(PUBLIC_DIR), **kwargs)

    def _send_json(self, obj: dict, status: int = 200) -> None:
        body = json.dumps(obj, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Cache-Control", "no-store")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self) -> None:
        parsed = urlparse(self.path)
        if parsed.path == "/api/skills":
            self._send_json({"skills": skills_for_api()})
            return
        if parsed.path == "/api/subfolders":
            qs = parse_qs(parsed.query)
            skill = (qs.get("skill", [""])[0] or "").strip()
            self._send_json({"subfolders": subfolders_for_api(skill)})
            return
        if parsed.path == "/api/cards":
            qs = parse_qs(parsed.query)
            prefix = (qs.get("prefix", [""])[0] or "").strip().lower()
            skill = (qs.get("skill", [""])[0] or "").strip()
            subfolder = (qs.get("subfolder", [""])[0] or "").strip()

            if not skill:
                self._send_json(
                    {"cards": [], "error": "Выберите навык (папку в «Знания»)."},
                    status=400,
                )
                return
            if skill not in list_skills():
                self._send_json({"cards": [], "error": "Неизвестный навык."}, status=400)
                return
            if subfolder and subfolder not in list_subfolders(skill):
                self._send_json({"cards": [], "error": "Неизвестный подкаталог."}, status=400)
                return

            deck = build_deck(skill, subfolder or None)
            if prefix:
                deck = [
                    c
                    for c in deck
                    if prefix in c["source"].lower() or prefix in c["front"].lower()
                ]
            self._send_json({"cards": deck})
            return
        if parsed.path in ("/", "/index.html"):
            self.path = "/index.html"
        return super().do_GET()

    def log_message(self, format: str, *args) -> None:
        print("[%s] %s" % (self.log_date_time_string(), format % args))


def main() -> None:
    if not PUBLIC_DIR.is_dir():
        raise SystemExit(f"Нет папки public: {PUBLIC_DIR}")
    print("Хранилище:", VAULT_ROOT)
    print("Знания:", KNOWLEDGE_ROOT)
    print("Открой в браузере: http://127.0.0.1:8765")
    print("(если «localhost» не открывается — используйте именно 127.0.0.1)")
    HTTPServer(("127.0.0.1", 8765), Handler).serve_forever()


if __name__ == "__main__":
    main()

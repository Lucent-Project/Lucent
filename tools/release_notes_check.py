import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(HERE, ".."))

LANG_FILES = {"zh-CN": "简体中文", "zh-Hant": "繁體中文", "es": "Español", "ar": "العربية", "pt": "Português", "fr": "Français", "ru": "Русский", "de": "Deutsch", "ja": "日本語", "tr": "Türkçe", "ko": "한국어", "it": "Italiano", "nl": "Nederlands"}
MIN_WORDS = 300
MAX_WORDS = 700


def check_notes(text):
    problems = []
    lines = text.split("\n")
    if not lines or not lines[0].startswith("# "):
        problems.append("the notes must open with a first-level title, '# Title'")
    if lines and lines[0].startswith("# Lucent "):
        problems.append("the title is the bare subtitle, without the 'Lucent x.y.z —' prefix")
    if "<details>" in text:
        problems.append(
            "no <details> language blocks in the notes; "
            "translations live in docs/releases/<tag>.<lang>.md"
        )
    words = len(re.findall(r"[A-Za-z][A-Za-z'-]*", text))
    if words < MIN_WORDS or words > MAX_WORDS:
        problems.append(
            f"the English notes run to {words} words; keep them between {MIN_WORDS} and {MAX_WORDS}"
        )
    return problems, words


def check_translations(tag):
    problems = []
    for lang, name in LANG_FILES.items():
        path = os.path.join(REPO, "docs", "releases", f"{tag}.{lang}.md")
        if not os.path.isfile(path):
            problems.append(f"missing translation file docs/releases/{tag}.{lang}.md ({name})")
            continue
        with open(path, encoding="utf-8") as handle:
            first = handle.readline()
        if not first.startswith("# "):
            problems.append(f"docs/releases/{tag}.{lang}.md must open with a first-level title")
    return problems


def main():
    if len(sys.argv) < 3:
        print("usage: python3 tools/release_notes_check.py <notes-file> <tag>")
        return 2
    notes_file, tag = sys.argv[1], sys.argv[2]
    if not os.path.isfile(notes_file):
        print(f"::error::no release notes at {notes_file}")
        return 1
    with open(notes_file, encoding="utf-8") as handle:
        text = handle.read()
    problems, words = check_notes(text)
    problems += check_translations(tag)
    print(f"Release notes: {words} English words, tag {tag}")
    if problems:
        for problem in problems:
            print(f"::error::{problem}")
        return 1
    print("The notes match the house format: English body, three translation files, no stacked blocks.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

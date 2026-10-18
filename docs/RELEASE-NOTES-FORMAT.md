# Release notes: the house format

Every release body is English-first, the way the README is: one canonical language up front,
with links to the translations. No stacked language blocks in the body.

The `release_notes` workflow input carries the English notes only:

```markdown
# <English subtitle, without the "Lucent x.y.z —" prefix>

<English notes, roughly 500 words, British English, dry, and never negative>
```

The translations live as separate files, one per language, committed to `docs/releases/`
before the release workflow runs:

- `docs/releases/v3.1.2.zh-CN.md`
- `docs/releases/v3.1.2.ja.md`
- `docs/releases/v3.1.2.ko.md`

Each file opens with its own `# ` title followed by the translated notes. There is no
English file — the release body itself is the canonical English notes.

The release workflow prepends the language-links header and appends the build info, so the
published body looks like this:

```markdown
<div align="center">

[🇨🇳 简体中文](https://github.com/Lucent-Project/Lucent/blob/main/docs/releases/v3.1.2.zh-CN.md) · [🇯🇵 日本語](https://github.com/Lucent-Project/Lucent/blob/main/docs/releases/v3.1.2.ja.md) · [🇰🇷 한국어](https://github.com/Lucent-Project/Lucent/blob/main/docs/releases/v3.1.2.ko.md)

</div>

# <English subtitle>

<English notes>

---

**Build info**

- Build-ID: R202610180019
- Android APK: 37.26 MiB
- Windows installer: 85.37 MiB
- Built from commit `8ddc5cf9`
- Build log: https://github.com/Lucent-Project/Lucent/actions/runs/39082353741
```

The rules the checker enforces:

- The notes open with a first-level title, and it is the subtitle alone, not the whole release name.
- No `<details>` blocks anywhere in the notes or the body. Languages live in their own files.
- The English notes stay between 300 and 700 words.
- All three translation files exist under `docs/releases/<tag>.<lang>.md` and each opens with
  its own `# ` title.
- If a build info block is present it carries the build ID, the APK size, the installer size
  and the commit.

`python3 tools/release_notes_check.py <notes-file> <tag>` checks a set of notes; the release
workflow runs it on the notes it is given, and refuses to build a release whose notes or
translation files do not match.

Preview releases carry no release notes and need no translations.

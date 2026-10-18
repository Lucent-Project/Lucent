import os, re, sys, importlib.util

_HERE = os.path.dirname(os.path.abspath(__file__))
_REPO = os.path.abspath(os.path.join(_HERE, "..", ".."))

spec = importlib.util.spec_from_file_location("catalog", os.path.join(_HERE, "catalog.py"))
cat = importlib.util.module_from_spec(spec)
spec.loader.exec_module(cat)

spec_d = importlib.util.spec_from_file_location("catalog_desktop", os.path.join(_HERE, "catalog_desktop.py"))
cat_d = importlib.util.module_from_spec(spec_d)
spec_d.loader.exec_module(cat_d)

def expand_entry(e):
    if isinstance(e, str):
        return e
    if len(e) >= 15:
        return e
    if len(e) == 5:
        return (e[0], e[1], e[2], None, None, None, None, None, None, None, e[3], None, e[4], None, None)
    return tuple(e) + (None,) * (15 - len(e))

ENTRIES = [expand_entry(e) for e in cat.ENTRIES]
DESKTOP_ONLY = [expand_entry(e) for e in cat_d.DESKTOP_ONLY]
ANDROID_ONLY = set(cat_d.ANDROID_ONLY)

def esc(s: str) -> str:
    s = s.replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$")
    s = s.replace("\n", "\\n")
    return re.sub(r"\\?\{(\w+)\}", r"${\1}", s)

def is_fn(key: str) -> bool:
    return "(" in key

def fn_name(key: str) -> str:
    return key.split("(")[0]

seen = set()
for e in list(ENTRIES) + list(DESKTOP_ONLY):
    if isinstance(e, str):
        continue
    k = fn_name(e[0]) if is_fn(e[0]) else e[0]
    if k in seen:
        sys.exit(f"DUPLICATE KEY: {k}")
    seen.add(k)
for k in ANDROID_ONLY:
    if k not in seen:
        sys.exit(f"ANDROID_ONLY names a key that is not in catalog.py: {k}")

def base_decl(key, en):
    if is_fn(key):
        return f'    open fun {key}: String = "{esc(en)}"'
    return f'    open val {key}: String = "{esc(en)}"'

def override_decl(key, val):
    if val is None:
        return None
    if is_fn(key):
        return f'    override fun {key}: String = "{esc(val)}"'
    return f'    override val {key}: String = "{esc(val)}"'

header = '''package com.lucent.app.i18n

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


enum class PluralCategory { ZERO, ONE, TWO, FEW, MANY, OTHER }

enum class AppLanguage(val key: String, val label: String) {
    SYSTEM("system", "System"),
    EN("en", "English"),
    ZH_HANS("zh", "\\u7b80\\u4f53\\u4e2d\\u6587"),
    ZH_HANT("zh-Hant", "\\u7e41\\u9ad4\\u4e2d\\u6587"),
    ES("es", "Espa\\u00f1ol"),
    AR("ar", "\\u0627\\u0644\\u0639\\u0631\\u0628\\u064a\\u0629"),
    PT("pt", "Portugu\\u00eas"),
    FR("fr", "Fran\\u00e7ais"),
    RU("ru", "\\u0420\\u0443\\u0441\\u0441\\u043a\\u0438\\u0439"),
    DE("de", "Deutsch"),
    JA("ja", "\\u65e5\\u672c\\u8a9e"),
    TR("tr", "T\\u00fcrk\\u00e7e"),
    KO("ko", "\\ud55c\\uad6d\\uc5b4"),
    IT("it", "Italiano"),
    NL("nl", "Nederlands");

    companion object {
        val uiOrder: List<AppLanguage> =
            listOf(EN, ZH_HANS, ZH_HANT, ES, AR, PT, FR, RU, DE, JA, TR, KO, IT, NL)

        fun fromKey(key: String?): AppLanguage = entries.firstOrNull { it.key == key } ?: SYSTEM

        fun systemDefault(): AppLanguage {
            val locale = java.util.Locale.getDefault()
            val tag = locale.language.lowercase()
            val country = locale.country.uppercase()
            return when {
                tag.startsWith("zh") && (country == "HK" || country == "TW" || country == "MO") -> ZH_HANT
                tag.startsWith("zh") -> ZH_HANS
                tag.startsWith("es") -> ES
                tag.startsWith("ar") -> AR
                tag.startsWith("pt") -> PT
                tag.startsWith("fr") -> FR
                tag.startsWith("ru") -> RU
                tag.startsWith("de") -> DE
                tag.startsWith("ja") -> JA
                tag.startsWith("tr") -> TR
                tag.startsWith("ko") -> KO
                tag.startsWith("it") -> IT
                tag.startsWith("nl") -> NL
                else -> EN
            }
        }
    }
}

object L {
    var current: Tr by mutableStateOf(resolve(AppLanguage.SYSTEM))
        private set

    var language: AppLanguage = AppLanguage.SYSTEM
        private set

    fun apply(key: String?) {
        language = AppLanguage.fromKey(key)
        current = resolve(language)
    }

    fun isRtl(): Boolean =
        (if (language == AppLanguage.SYSTEM) AppLanguage.systemDefault() else language) == AppLanguage.AR

    private fun resolve(lang: AppLanguage): Tr = when (
        if (lang == AppLanguage.SYSTEM) AppLanguage.systemDefault() else lang
    ) {
        AppLanguage.ZH_HANS -> ZhHans
        AppLanguage.ZH_HANT -> ZhHant
        AppLanguage.ES -> Es
        AppLanguage.AR -> Ar
        AppLanguage.PT -> Pt
        AppLanguage.FR -> Fr
        AppLanguage.RU -> Ru
        AppLanguage.DE -> De
        AppLanguage.JA -> Ja
        AppLanguage.TR -> Trk
        AppLanguage.KO -> Ko
        AppLanguage.IT -> It
        AppLanguage.NL -> Nl
        else -> En
    }
}

val S: Tr get() = L.current

fun lucentLocale(): java.util.Locale {
    val lang = if (L.language == AppLanguage.SYSTEM) AppLanguage.systemDefault() else L.language
    return when (lang) {
        AppLanguage.ZH_HANS -> java.util.Locale.SIMPLIFIED_CHINESE
        AppLanguage.ZH_HANT -> java.util.Locale("zh", "HK")
        AppLanguage.ES -> java.util.Locale("es")
        AppLanguage.AR -> java.util.Locale("ar")
        AppLanguage.PT -> java.util.Locale("pt")
        AppLanguage.FR -> java.util.Locale.FRENCH
        AppLanguage.RU -> java.util.Locale("ru")
        AppLanguage.DE -> java.util.Locale.GERMAN
        AppLanguage.JA -> java.util.Locale.JAPANESE
        AppLanguage.TR -> java.util.Locale("tr")
        AppLanguage.KO -> java.util.Locale.KOREAN
        AppLanguage.IT -> java.util.Locale.ITALIAN
        AppLanguage.NL -> java.util.Locale("nl")
        else -> java.util.Locale.ENGLISH
    }
}

object LDates {
    private var cachedFor: Tr? = null
    private val cache = HashMap<String, java.time.format.DateTimeFormatter>()

    fun of(pattern: String): java.time.format.DateTimeFormatter {
        val now = L.current
        if (cachedFor !== now) {
            cache.clear()
            cachedFor = now
        }
        return cache.getOrPut(pattern) { java.time.format.DateTimeFormatter.ofPattern(pattern, lucentLocale()) }
    }
}

open class Tr {
    open fun pluralCategory(count: Int): PluralCategory =
        if (count == 1) PluralCategory.ONE else PluralCategory.OTHER

    protected fun counted(count: Int, templates: Map<PluralCategory, String>): String {
        val template = templates[pluralCategory(count)] ?: templates[PluralCategory.OTHER] ?: ""
        return template.replace("{count}", count.toString())
    }'''

PLURAL_OVERRIDES = {
    "ZhHans": '    override fun pluralCategory(count: Int): PluralCategory = PluralCategory.OTHER',
    "ZhHant": '    override fun pluralCategory(count: Int): PluralCategory = PluralCategory.OTHER',
    "Ja": '    override fun pluralCategory(count: Int): PluralCategory = PluralCategory.OTHER',
    "Trk": '    override fun pluralCategory(count: Int): PluralCategory = PluralCategory.OTHER',
    "Ko": '    override fun pluralCategory(count: Int): PluralCategory = PluralCategory.OTHER',
    "Fr": '    override fun pluralCategory(count: Int): PluralCategory =\n        if (count == 0 || count == 1) PluralCategory.ONE else PluralCategory.OTHER',
    "Ru": '    override fun pluralCategory(count: Int): PluralCategory {\n        val n = kotlin.math.abs(count)\n        val mod10 = n % 10\n        val mod100 = n % 100\n        return when {\n            mod10 == 1 && mod100 != 11 -> PluralCategory.ONE\n            mod10 in 2..4 && mod100 !in 12..14 -> PluralCategory.FEW\n            else -> PluralCategory.MANY\n        }\n    }',
    "Ar": '    override fun pluralCategory(count: Int): PluralCategory = when {\n        count == 0 -> PluralCategory.ZERO\n        count == 1 -> PluralCategory.ONE\n        count == 2 -> PluralCategory.TWO\n        count % 100 in 3..10 -> PluralCategory.FEW\n        count % 100 in 11..99 -> PluralCategory.MANY\n        else -> PluralCategory.OTHER\n    }',
}

LANGUAGES = [
    ("ZhHans", 2),
    ("ZhHant", 3),
    ("Es", 4),
    ("Ar", 5),
    ("Pt", 6),
    ("Fr", 7),
    ("Ru", 8),
    ("De", 9),
    ("Ja", 10),
    ("Trk", 11),
    ("Ko", 12),
    ("It", 13),
    ("Nl", 14),
]

def expand_conditional(v):
    if len(v) >= 15:
        return list(v)
    if len(v) == 4:
        v = [v[0], v[1], None, None, None, None, None, None, None, v[2], None, v[3], None, None]
    return list(v) + [None] * (15 - len(v))

CONDITIONAL = {
    k: expand_conditional(v)
    for k, v in getattr(cat, "CONDITIONAL_ENTRIES", {}).items()
}

def base_decl_for(e):
    key = e[0]
    if key in CONDITIONAL and CONDITIONAL[key][0] is not None:
        return f"    open fun {key}: String = {CONDITIONAL[key][0]}"
    return base_decl(key, e[1])

def override_decl_for(e, idx):
    key = e[0]
    if key in CONDITIONAL:
        expr = CONDITIONAL[key][idx - 1] if idx - 1 < len(CONDITIONAL[key]) else None
        if expr is None:
            return None
        return f"    override fun {key}: String = {expr}"
    return override_decl(key, e[idx])

def lang_object_for(entries, name, idx):
    lines = [f"object {name} : Tr() {{"]
    if name in PLURAL_OVERRIDES:
        lines.append(PLURAL_OVERRIDES[name])
    for e in entries:
        if isinstance(e, str):
            continue
        d = override_decl_for(e, idx)
        if d:
            lines.append(d)
    lines.append("}")
    return "\n".join(lines)

def emit(entries, path):
    out = [header]
    for e in entries:
        if isinstance(e, str):
            out.append("    " + e if e else "")
        else:
            out.append(base_decl_for(e))
    out.append("}")
    out.append("")
    out.append("object En : Tr()")
    for name, idx in LANGUAGES:
        out.append("")
        out.append(lang_object_for(entries, name, idx))
    out.append("")
    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(out))
    n = sum(1 for e in entries if not isinstance(e, str))
    print(f"Wrote {path}: {n} entries")

all_entries = list(ENTRIES)
all_entries.append("")
all_entries.extend(DESKTOP_ONLY)
emit(all_entries, os.path.join(_REPO, "shared", "src", "commonMain", "kotlin", "com", "lucent", "app", "i18n", "I18n.kt"))

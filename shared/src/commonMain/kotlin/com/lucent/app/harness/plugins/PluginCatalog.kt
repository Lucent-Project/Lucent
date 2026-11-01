package com.lucent.app.harness.plugins

object PluginCatalog {

    fun aptMirror(region: String): String =
        if (region == "cn") "http://mirrors.tuna.tsinghua.edu.cn" else "http://ports.ubuntu.com"

    fun aptFallback(region: String): String =
        if (region == "cn") "http://ports.ubuntu.com" else "http://mirrors.tuna.tsinghua.edu.cn"

    fun pipIndex(region: String): String =
        if (region == "cn") "https://pypi.tuna.tsinghua.edu.cn/simple" else "https://pypi.org/simple"

    fun pipFallbackIndex(region: String): String =
        if (region == "cn") "https://pypi.org/simple" else "https://pypi.tuna.tsinghua.edu.cn/simple"

    fun effectiveRegion(region: String): String {
        val normalized = region.trim().lowercase()
        return if (normalized == "cn") "cn" else "global"
    }

    fun buildAptInstallScript(
        packages: List<String>,
        postAptCommand: String = ""
    ): String {
        val pkgList = packages.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(" ")
        val lines = mutableListOf(
            "set +e",
            "LUCENT_CUR_STEP=proot-preflight",
            "echo \"[LUCENT_STEP:proot-preflight]\"",
            "mkdir -p /tmp /var/tmp 2>/dev/null || true",
            "chmod 1777 /tmp /var/tmp 2>/dev/null || true",
            "if [ ! -x /usr/bin/dpkg ] || [ ! -x /usr/bin/apt-get ]; then",
            "  echo \"proot preflight failed: /usr/bin/dpkg or /usr/bin/apt-get is missing\"",
            "  ls -la /usr/bin/dpkg /usr/bin/apt-get 2>&1 || true",
            "  echo \"[LUCENT_FAIL:PROOT_ENV:proot-preflight]\"",
            "  exit 71",
            "fi",
            "if ! touch /tmp/.lucent-proot-probe 2>/dev/null; then",
            "  echo \"/tmp is not writable inside the built-in environment\"",
            "  ls -ld /tmp 2>&1 || true",
            "  echo \"[LUCENT_FAIL:PROOT_ENV:proot-preflight]\"",
            "  exit 71",
            "fi",
            "rm -f /tmp/.lucent-proot-probe 2>/dev/null || true",
            "mkdir -p /usr/sbin /etc/dpkg/dpkg.cfg.d 2>/dev/null || true",
            "printf '#!/bin/sh\\nexit 101\\n' > /usr/sbin/policy-rc.d 2>/dev/null || true",
            "chmod 755 /usr/sbin/policy-rc.d 2>/dev/null || true",
            "echo 'force-unsafe-io' > /etc/dpkg/dpkg.cfg.d/unsafe-io 2>/dev/null || true",
            "LUCENT_CUR_STEP=dpkg-state-check",
            "echo \"[LUCENT_STEP:dpkg-state-check]\"",
            "chmod -R u+rwX /var/lib/dpkg 2>/dev/null || true",
            "mkdir -p /var/lib/dpkg/updates /var/lib/dpkg/info /var/lib/dpkg/triggers /var/cache/apt/archives/partial /var/lib/apt/lists/partial 2>/dev/null || true",
            "if [ ! -f /var/lib/dpkg/status ] && [ -f /var/lib/dpkg/status-old ]; then",
            "  cp /var/lib/dpkg/status-old /var/lib/dpkg/status 2>/dev/null || true",
            "fi",
            "if [ ! -f /var/lib/dpkg/status ] || ! touch /var/lib/dpkg/.lucent-state-probe 2>/dev/null; then",
            "  echo \"dpkg status database is missing or unwritable at /var/lib/dpkg\"",
            "  ls -ld /var/lib/dpkg /var/lib/dpkg/status 2>&1 || true",
            "  df -k /var/lib/dpkg 2>&1 || true",
            "  echo \"[LUCENT_FAIL:DPKG_STATE:dpkg-state-check]\"",
            "  exit 72",
            "fi",
            "rm -f /var/lib/dpkg/.lucent-state-probe 2>/dev/null || true",
            "FREE_KB=\$(df -k /var/lib/dpkg 2>/dev/null | awk 'NR==2{print \$4}')",
            "if [ -n \"\$FREE_KB\" ] && [ \"\$FREE_KB\" -lt 32768 ] 2>/dev/null; then",
            "  echo \"Not enough free space in /var/lib/dpkg (\${FREE_KB} KB available)\"",
            "  echo \"[LUCENT_FAIL:DPKG_STATE:dpkg-state-check]\"",
            "  exit 72",
            "fi",
            "lucent_lock_holders() {",
            "  _holders=\"\"",
            "  for _d in /proc/[0-9]*/cmdline; do",
            "    _pid=\${_d#/proc/}",
            "    _pid=\${_pid%/cmdline}",
            "    if [ \"\$_pid\" = \"\$\$\" ] || [ \"\$_pid\" = \"\$BASHPID\" ]; then continue; fi",
            "    _c0=\$(tr '\\0' '\\n' <\"\$_d\" 2>/dev/null | head -n 1)",
            "    _c0=\${_c0##*/}",
            "    case \"\$_c0\" in",
            "      apt-get|apt|dpkg|aptitude|unattended-upgr)",
            "        _full=\$(tr '\\0' ' ' <\"\$_d\" 2>/dev/null | sed 's/ *\$//')",
            "        if [ -z \"\$_holders\" ]; then _holders=\"PID \$_pid (\$_full)\"; else _holders=\"\$_holders; PID \$_pid (\$_full)\"; fi",
            "        ;;",
            "    esac",
            "  done",
            "  printf '%s' \"\$_holders\"",
            "}",
            "lucent_pending_pkgs() {",
            "  awk '/^Package: /{p=\$2} /^Status: /{if (\$0 !~ /install ok installed/ && \$0 !~ /deinstall ok config-files/ && \$0 !~ /purge ok not-installed/) print p}' /var/lib/dpkg/status 2>/dev/null | paste -sd ' ' -",
            "}",
            "lucent_run_apt() {",
            "  _try=0",
            "  _log=/tmp/lucent-apt-\$\$.log",
            "  while [ \$_try -lt 8 ]; do",
            "    rm -f \"\$_log\" 2>/dev/null || true",
            "    \"\$@\" 2>&1 | tee \"\$_log\"",
            "    _rc=\${PIPESTATUS[0]}",
            "    if [ \$_rc -eq 0 ]; then rm -f \"\$_log\" 2>/dev/null || true; return 0; fi",
            "    if grep -qiE 'Unable to acquire the dpkg frontend lock|Could not get lock|dpkg frontend lock was locked|is another process using it' \"\$_log\" 2>/dev/null; then",
            "      _try=\$((_try + 1))",
            "      _h=\$(lucent_lock_holders)",
            "      if [ -n \"\$_h\" ]; then echo \"[LUCENT_LOCK:\$_h]\"; fi",
            "      echo \"[lucent] package lock busy (attempt \$_try/8) \${_h}\"",
            "      if [ \$_try -lt 8 ]; then sleep 3; fi",
            "    else",
            "      cp \"\$_log\" /tmp/lucent-last-err.log 2>/dev/null || true",
            "      rm -f \"\$_log\" 2>/dev/null || true",
            "      return \$_rc",
            "    fi",
            "  done",
            "  _h=\$(lucent_lock_holders)",
            "  if [ -n \"\$_h\" ]; then echo \"[LUCENT_LOCK:\$_h]\"; fi",
            "  echo \"[LUCENT_FAIL:DPKG_LOCK:\$LUCENT_CUR_STEP]\"",
            "  rm -f \"\$_log\" 2>/dev/null || true",
            "  exit 73",
            "}",
            "lucent_write_sources() {",
            "  _uri=\"\$1\"",
            "  mkdir -p /etc/apt/sources.list.d 2>/dev/null || true",
            "  rm -f /etc/apt/sources.list 2>/dev/null || true",
            "  printf '%s\\n' \\",
            "    'Types: deb' \\",
            "    \"URIs: \${_uri}/ubuntu-ports/\" \\",
            "    'Suites: noble noble-updates noble-backports' \\",
            "    'Components: main universe restricted multiverse' \\",
            "    'Signed-By: /usr/share/keyrings/ubuntu-archive-keyring.gpg' \\",
            "    '' \\",
            "    'Types: deb' \\",
            "    \"URIs: \${_uri}/ubuntu-ports/\" \\",
            "    'Suites: noble-security' \\",
            "    'Components: main universe restricted multiverse' \\",
            "    'Signed-By: /usr/share/keyrings/ubuntu-archive-keyring.gpg' \\",
            "    > /etc/apt/sources.list.d/ubuntu.sources",
            "}",
            "APT_UPDATED=0",
            "lucent_ensure_apt_lists() {",
            "  _force=\"\$1\"",
            "  if [ \"\$APT_UPDATED\" = \"1\" ] && [ \"\$_force\" != \"1\" ]; then return 0; fi",
            "  if [ \"\$_force\" != \"1\" ] && [ -s /etc/apt/sources.list.d/ubuntu.sources ] && grep -qF '{aptMirror}/ubuntu-ports/' /etc/apt/sources.list.d/ubuntu.sources 2>/dev/null; then",
            "    if find /var/lib/apt/lists -maxdepth 1 -name '*_Packages*' -mmin -10 2>/dev/null | grep -q .; then",
            "      APT_UPDATED=1",
            "      return 0",
            "    fi",
            "  fi",
            "  _prev_step=\"\$LUCENT_CUR_STEP\"",
            "  LUCENT_CUR_STEP=apt-update",
            "  echo \"[LUCENT_STEP:apt-update]\"",
            "  lucent_write_sources '{aptMirror}'",
            "  if lucent_run_apt apt-get update -o Acquire::Retries=2 -o Acquire::http::Timeout=20 -o Acquire::https::Timeout=20 && ls /var/lib/apt/lists/*_Packages* >/dev/null 2>&1; then",
            "    APT_UPDATED=1",
            "    LUCENT_CUR_STEP=\"\$_prev_step\"",
            "    return 0",
            "  fi",
            "  echo \"[lucent] primary mirror {aptMirror} failed; switching to fallback {aptFallback}\"",
            "  lucent_write_sources '{aptFallback}'",
            "  if lucent_run_apt apt-get update -o Acquire::Retries=2 -o Acquire::http::Timeout=20 -o Acquire::https::Timeout=20 && ls /var/lib/apt/lists/*_Packages* >/dev/null 2>&1; then",
            "    APT_UPDATED=1",
            "    LUCENT_CUR_STEP=\"\$_prev_step\"",
            "    return 0",
            "  fi",
            "  echo \"[LUCENT_FAIL:APT_UPDATE:apt-update]\"",
            "  exit 75",
            "}",
            "LUCENT_CUR_STEP=dpkg-lock-check",
            "echo \"[LUCENT_STEP:dpkg-lock-check]\"",
            "PENDING_NOW=\$(lucent_pending_pkgs)",
            "if [ -n \"\$PENDING_NOW\" ]; then echo \"[LUCENT_PENDING:\$PENDING_NOW]\"; fi",
            "UPDATES_NOW=\$(ls /var/lib/dpkg/updates/ 2>/dev/null | grep -E '^[0-9]+\$' | head -n 1)",
            "if [ -n \"\$PENDING_NOW\" ] || [ -n \"\$UPDATES_NOW\" ]; then",
            "  LUCENT_CUR_STEP=dpkg-configure",
            "  echo \"[LUCENT_STEP:dpkg-configure]\"",
            "  lucent_run_apt dpkg --configure -a",
            "  DCA_RC=\$?",
            "  echo \"[LUCENT_DCA_EXIT:\$DCA_RC]\"",
            "  if [ \$DCA_RC -ne 0 ]; then",
            "    if grep -qiE 'read-only file system|unable to access the dpkg database|no space left on device' /tmp/lucent-last-err.log 2>/dev/null; then",
            "      PENDING_NOW=\$(lucent_pending_pkgs)",
            "      echo \"[LUCENT_PENDING:\$PENDING_NOW]\"",
            "      echo \"[LUCENT_FAIL:DPKG_STATE:dpkg-configure]\"",
            "      exit 72",
            "    fi",
            "    lucent_ensure_apt_lists 0",
            "    LUCENT_CUR_STEP=dpkg-configure",
            "    echo \"[LUCENT_STEP:dpkg-configure]\"",
            "    lucent_run_apt apt-get -f install -y --no-install-recommends -o Dpkg::Options::=--force-confdef -o Dpkg::Options::=--force-confold",
            "    FIX_RC=\$?",
            "    lucent_run_apt dpkg --configure -a",
            "    DCA_RC=\$?",
            "    echo \"[LUCENT_DCA_EXIT:\$DCA_RC]\"",
            "    if [ \$FIX_RC -ne 0 ] || [ \$DCA_RC -ne 0 ]; then",
            "      PENDING_NOW=\$(lucent_pending_pkgs)",
            "      echo \"[LUCENT_PENDING:\$PENDING_NOW]\"",
            "      echo \"[LUCENT_FAIL:DPKG_CONFIGURE:dpkg-configure]\"",
            "      exit 74",
            "    fi",
            "  fi",
            "  echo \"[LUCENT_PENDING:]\"",
            "fi"
        )

        if (pkgList.isNotEmpty()) {
            lines.addAll(
                listOf(
                    "MISSING_PKGS=\"\"",
                    "for _pkg in $pkgList; do",
                    "  _st=\$(dpkg-query -W -f='\${Status}\\n' \"\$_pkg\" 2>/dev/null || true)",
                    "  if [ \"\$_st\" != \"install ok installed\" ]; then",
                    "    if [ -z \"\$MISSING_PKGS\" ]; then MISSING_PKGS=\"\$_pkg\"; else MISSING_PKGS=\"\$MISSING_PKGS \$_pkg\"; fi",
                    "  fi",
                    "done",
                    "if [ -n \"\$MISSING_PKGS\" ]; then",
                    "  lucent_ensure_apt_lists 0",
                    "  LUCENT_CUR_STEP=apt-install",
                    "  echo \"[LUCENT_STEP:apt-install]\"",
                    "  echo \"[lucent] installing missing packages: \$MISSING_PKGS\"",
                    "  lucent_run_apt apt-get install -y --no-install-recommends -o Acquire::Retries=2 -o Acquire::http::Timeout=25 -o Dpkg::Options::=--force-confdef -o Dpkg::Options::=--force-confold \$MISSING_PKGS",
                    "  INST_RC=\$?",
                    "  if [ \$INST_RC -ne 0 ] && grep -qiE 'Failed to fetch|Unable to fetch some archives|Temporary failure resolving|Could not resolve|Connection timed out|Network is unreachable' /tmp/lucent-last-err.log 2>/dev/null; then",
                    "    echo \"[lucent] package download failed; switching to fallback mirror {aptFallback} and resuming\"",
                    "    lucent_write_sources '{aptFallback}'",
                    "    LUCENT_CUR_STEP=apt-update",
                    "    echo \"[LUCENT_STEP:apt-update]\"",
                    "    if ! (lucent_run_apt apt-get update -o Acquire::Retries=2 -o Acquire::http::Timeout=20 -o Acquire::https::Timeout=20 && ls /var/lib/apt/lists/*_Packages* >/dev/null 2>&1); then",
                    "      echo \"[LUCENT_FAIL:APT_UPDATE:apt-update]\"",
                    "      exit 75",
                    "    fi",
                    "    lucent_run_apt dpkg --configure -a || true",
                    "    LUCENT_CUR_STEP=apt-install",
                    "    echo \"[LUCENT_STEP:apt-install]\"",
                    "    lucent_run_apt apt-get install -y --no-install-recommends -o Acquire::Retries=2 -o Acquire::http::Timeout=25 -o Dpkg::Options::=--force-confdef -o Dpkg::Options::=--force-confold \$MISSING_PKGS",
                    "    INST_RC=\$?",
                    "  fi",
                    "  if [ \$INST_RC -ne 0 ]; then",
                    "    PENDING_NOW=\$(lucent_pending_pkgs)",
                    "    echo \"[LUCENT_PENDING:\$PENDING_NOW]\"",
                    "    if grep -qiE 'read-only file system|no space left on device' /tmp/lucent-last-err.log 2>/dev/null; then",
                    "      echo \"[LUCENT_FAIL:DPKG_STATE:apt-install]\"",
                    "      exit 72",
                    "    fi",
                    "    echo \"[LUCENT_FAIL:APT_INSTALL:apt-install]\"",
                    "    exit 76",
                    "  fi",
                    "else",
                    "  echo \"[lucent] system packages already installed: $pkgList\"",
                    "fi"
                )
            )
        }

        if (postAptCommand.isNotBlank()) {
            lines.add(postAptCommand)
        }
        lines.add("exit 0")
        return lines.joinToString("\n")
    }

    private fun buildPipOfficePostCommand(): String = listOf(
        "LUCENT_CUR_STEP=pip-install",
        "echo \"[LUCENT_STEP:pip-install]\"",
        "MISSING_PIP=\$(python3 -c \"import importlib.util; req=[('docx','python-docx'),('openpyxl','openpyxl'),('xlsxwriter','XlsxWriter'),('pptx','python-pptx'),('fitz','pymupdf'),('pandas','pandas')]; print(' '.join(p for m,p in req if importlib.util.find_spec(m) is None))\" 2>/dev/null)",
        "if [ -z \"\$MISSING_PIP\" ] && python3 -c \"import docx, openpyxl, xlsxwriter, pptx, fitz, pandas\" 2>/dev/null; then",
        "  echo \"[lucent] Python document libraries already installed\"",
        "else",
        "  if [ -z \"\$MISSING_PIP\" ]; then MISSING_PIP=\"python-docx openpyxl XlsxWriter python-pptx pymupdf pandas\"; fi",
        "  echo \"[lucent] installing missing Python wheels: \$MISSING_PIP\"",
        "  if ! pip3 install --break-system-packages --index-url {pipIndex} \$MISSING_PIP; then",
        "    echo \"[lucent] primary PyPI index {pipIndex} failed; retrying with fallback {pipFallback}\"",
        "    if ! pip3 install --break-system-packages --index-url {pipFallback} \$MISSING_PIP; then",
        "      echo \"[LUCENT_FAIL:APT_INSTALL:pip-install]\"",
        "      exit 77",
        "    fi",
        "  fi",
        "fi"
    ).joinToString("\n")

    private fun desktopTool(
        id: String,
        name: String,
        binary: String,
        winget: String,
        summary: String,
        licence: String,
        homepage: String
    ) = PluginSpec(
        id = id,
        name = name,
        summary = "$summary (installed on this PC)",
        android = false,
        desktop = true,
        bytes = 0L,
        sources = emptyList(),
        detectCommand = "command -v $binary",
        installScript = "winget install --id $winget -e --accept-package-agreements --accept-source-agreements",
        removeScript = "winget uninstall --id $winget -e",
        licence = licence,
        homepage = homepage,
        windowsDetect = "where $binary",
        windowsInstall = "winget install --id $winget -e --accept-package-agreements --accept-source-agreements",
        windowsRemove = "winget uninstall --id $winget -e"
    )

    private val ALL: List<PluginSpec> = listOf(
        PluginSpec(
            id = "python-office",
            name = "Python document libraries",
            summary = "Python with python-docx, openpyxl, XlsxWriter, python-pptx, pandas and PyMuPDF",
            android = true,
            desktop = true,
            bytes = 0L,
            sources = emptyList(),
            detectCommand = "python3 -c \"import docx, openpyxl, pptx\"",
            installScript = buildAptInstallScript(
                packages = listOf("python3", "python3-pip", "python3-venv", "ca-certificates"),
                postAptCommand = buildPipOfficePostCommand()
            ),
            removeScript = "pip3 uninstall -y python-docx openpyxl XlsxWriter python-pptx pymupdf pandas",
            licence = "Python PSF-2.0; libraries MIT/BSD; PyMuPDF AGPL-3.0",
            homepage = "https://www.python.org",
            windowsDetect = "py -c \"import docx, openpyxl, pptx\"",
            windowsInstall = "winget install --id Python.Python.3.12 -e --accept-package-agreements " +
                "--accept-source-agreements && py -m pip install --no-warn-script-location " +
                "python-docx openpyxl XlsxWriter python-pptx pymupdf pandas",
            windowsRemove = "py -m pip uninstall -y python-docx openpyxl XlsxWriter python-pptx pymupdf pandas",
            required = true
        ),
        PluginSpec(
            id = "libreoffice",
            name = "LibreOffice",
            summary = "Converts and renders Word, Excel, PowerPoint and PDF so the assistant can check its own work",
            android = true,
            desktop = true,
            bytes = 0L,
            sources = emptyList(),
            detectCommand = "command -v soffice",
            installScript = buildAptInstallScript(
                packages = listOf(
                    "libreoffice-writer-nogui",
                    "libreoffice-calc-nogui",
                    "libreoffice-impress-nogui"
                )
            ),
            removeScript = "apt-get remove -y 'libreoffice*'",
            licence = "MPL-2.0",
            homepage = "https://www.libreoffice.org",
            windowsDetect = "if exist \"%ProgramFiles%\\LibreOffice\\program\\soffice.exe\" (exit 0) else (exit 1)",
            windowsInstall = "winget install --id TheDocumentFoundation.LibreOffice -e " +
                "--accept-package-agreements --accept-source-agreements",
            windowsRemove = "winget uninstall --id TheDocumentFoundation.LibreOffice -e",
            required = true
        ),
        PluginSpec(
            id = "nodejs",
            name = "Node.js",
            summary = "Runs JavaScript tooling and the Playwright browser engine",
            android = true,
            desktop = true,
            bytes = 0L,
            sources = emptyList(),
            detectCommand = "command -v node",
            installScript = buildAptInstallScript(
                packages = listOf("nodejs", "npm")
            ),
            removeScript = "apt-get remove -y nodejs npm",
            licence = "MIT",
            homepage = "https://nodejs.org",
            windowsDetect = "where node",
            windowsInstall = "winget install --id OpenJS.NodeJS.LTS -e " +
                "--accept-package-agreements --accept-source-agreements",
            windowsRemove = "winget uninstall --id OpenJS.NodeJS.LTS -e",
            required = true
        ),
        PluginSpec(
            id = "adb",
            name = "ADB (Android Debug Bridge)",
            summary = "Control Android devices and emulators from the shell",
            android = true,
            desktop = false,
            bytes = 0L,
            sources = emptyList(),
            detectCommand = "command -v adb",
            installScript = buildAptInstallScript(
                packages = listOf("android-tools-adb")
            ),
            removeScript = "apt-get remove -y android-tools-adb",
            licence = "Apache-2.0",
            homepage = "https://developer.android.com/tools/adb"
        ),
        desktopTool("git", "Git", "git", "Git.Git", "Version control for the workspace", "GPL-2.0", "https://git-scm.com"),
        desktopTool("pandoc", "Pandoc", "pandoc", "JohnMacFarlane.Pandoc", "Converts between document formats", "GPL-2.0-or-later", "https://pandoc.org"),
        desktopTool("ffmpeg", "FFmpeg", "ffmpeg", "Gyan.FFmpeg", "Audio and video conversion", "LGPL-2.1/GPL-2.0", "https://ffmpeg.org"),
        desktopTool("rg", "ripgrep", "rg", "BurntSushi.ripgrep.MSVC", "Very fast text search", "MIT/Unlicense", "https://github.com/BurntSushi/ripgrep"),
        desktopTool("7z", "7-Zip", "7z", "7zip.7zip", "Archive handling", "LGPL-2.1 with unRAR restriction", "https://7-zip.org"),
        desktopTool("tesseract", "Tesseract OCR", "tesseract", "UB-Mannheim.TesseractOCR", "Reads text out of images", "Apache-2.0", "https://github.com/tesseract-ocr/tesseract"),
        desktopTool("magick", "ImageMagick", "magick", "ImageMagick.ImageMagick", "Image conversion and editing", "ImageMagick licence", "https://imagemagick.org"),
        desktopTool("qpdf", "qpdf", "qpdf", "QPDF.QPDF", "PDF splitting and merging", "Apache-2.0", "https://qpdf.readthedocs.io"),
        PluginSpec(
            id = "playwright",
            name = "Playwright browser",
            summary = "A real Chromium the assistant can drive when a page needs JavaScript",
            android = false,
            desktop = true,
            bytes = 0L,
            sources = emptyList(),
            detectCommand = "npx --no-install playwright --version",
            installScript = "(npm install -g playwright || npm install -g playwright --registry=https://registry.npmmirror.com) && " +
                "(npx playwright install chromium || PLAYWRIGHT_DOWNLOAD_HOST=https://cdn.npmmirror.com/binaries/playwright npx playwright install chromium)",
            removeScript = "npm uninstall -g playwright",
            licence = "Apache-2.0",
            homepage = "https://playwright.dev",
            windowsDetect = "npx --no-install playwright --version",
            windowsInstall = "(npm install -g playwright || npm install -g playwright --registry=https://registry.npmmirror.com) && " +
                "(npx playwright install chromium || (set \"PLAYWRIGHT_DOWNLOAD_HOST=https://cdn.npmmirror.com/binaries/playwright\" && npx playwright install chromium))",
            windowsRemove = "npm uninstall -g playwright"
        )
    )

    fun all(): List<PluginSpec> = ALL

    fun find(id: String): PluginSpec? = ALL.firstOrNull { it.id == id }

    fun forPlatform(android: Boolean): List<PluginSpec> = ALL.filter { if (android) it.android else it.desktop }

    suspend fun effective(): List<PluginSpec> = PluginCatalogRemote.fetchEffective(com.lucent.app.harness.HarnessRuntime.config())

    suspend fun findEffective(id: String): PluginSpec? = effective().firstOrNull { it.id == id }

    suspend fun forPlatformEffective(android: Boolean): List<PluginSpec> = effective().filter { if (android) it.android else it.desktop }
}

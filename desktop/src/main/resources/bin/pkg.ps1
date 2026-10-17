param(
    [string]${'$'}Command = "",
    [string]${'$'}Package = "",
    [Parameter(ValueFromRemainingArguments = ${'$'}true)]
    [string[]]${'$'}ExtraArgs
)

function Map-Package([string]${'$'}name) {
    switch (${'$'}name.ToLower()) {
        "git" { return "Git.Git" }
        "vim" { return "vim.vim" }
        "nano" { return "GNU.Nano" }
        "jq" { return "jqlang.jq" }
        "python" { return "Python.Python.3.12" }
        "python3" { return "Python.Python.3.12" }
        "curl" { return "cURL.cURL" }
        "wget" { return "JernejSimoncic.Wget" }
        "7z" { return "7zip.7zip" }
        "7zip" { return "7zip.7zip" }
        default { return ${'$'}name }
    }
}

switch (${'$'}Command.ToLower()) {
    "install" {
        if (-not ${'$'}Package) {
            Write-Host "Usage: pkg install <package>"
            exit 1
        }
        ${'$'}target = Map-Package ${'$'}Package
        Write-Host "Installing ${'$'}target via winget..."
        winget install --exact --id ${'$'}target @ExtraArgs
    }
    "search" {
        if (-not ${'$'}Package) {
            Write-Host "Usage: pkg search <query>"
            exit 1
        }
        winget search ${'$'}Package @ExtraArgs
    }
    "uninstall" {
        if (-not ${'$'}Package) {
            Write-Host "Usage: pkg uninstall <package>"
            exit 1
        }
        ${'$'}target = Map-Package ${'$'}Package
        winget uninstall ${'$'}target @ExtraArgs
    }
    "list-installed" {
        winget list @ExtraArgs
    }
    "list" {
        winget list @ExtraArgs
    }
    "upgrade" {
        if (${'$'}Package) {
            ${'$'}target = Map-Package ${'$'}Package
            winget upgrade ${'$'}target @ExtraArgs
        } else {
            winget upgrade --all @ExtraArgs
        }
    }
    "update" {
        winget upgrade @ExtraArgs
    }
    default {
        Write-Host "pkg on Windows wraps the Windows Package Manager (winget)."
        Write-Host "Usage: pkg <command> [package]"
        Write-Host ""
        Write-Host "Commands:"
        Write-Host "  install <package>    Install a package via winget (e.g. git, vim, jq, python3)"
        Write-Host "  search <query>       Search packages with winget search"
        Write-Host "  list-installed       List installed packages with winget list"
        Write-Host "  upgrade              Upgrade packages with winget upgrade"
        Write-Host "  uninstall <package>  Uninstall a package with winget uninstall"
    }
}

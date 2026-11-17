# build.ps1 -- cog-gen + gradle build for one or more cells; jars go to dist\.
# Usage:
#   pwsh scripts\build.ps1                        # every cell in matrix.json
#   pwsh scripts\build.ps1 Fabric                 # every Fabric cell
#   pwsh scripts\build.ps1 Fabric 26.2 NeoForge 26.2   # explicit pairs
# (scripts\build-fabric.ps1 / build-neoforge.ps1 / build-forge.ps1 are thin wrappers over this.)
param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Cells)
$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
$dist = Join-Path $repo 'dist'
New-Item -ItemType Directory -Force -Path $dist | Out-Null
$matrix = Get-Content (Join-Path $repo '_codegen\matrix.json') -Raw | ConvertFrom-Json
$all = @($matrix.cells.PSObject.Properties.Name)

$want = @()
if (-not $Cells -or $Cells.Count -eq 0) { $want = $all }
elseif ($Cells.Count -eq 1 -and $Cells[0] -match '^(Fabric|NeoForge|Forge)$') { $want = $all | Where-Object { $_ -like "$($Cells[0])/*" } }
else { for ($i = 0; $i -lt $Cells.Count; $i += 2) { $want += "$($Cells[$i])/$($Cells[$i+1])" } }

function Find-Jdk([string]$major) {
    (Get-ChildItem 'C:\Program Files\Eclipse Adoptium' -Directory -Filter "jdk-$major.*" |
        Sort-Object { [version]($_.Name -replace '^jdk-|-hotspot$','') } -Descending | Select-Object -First 1).FullName
}

$failed = @()
foreach ($key in $want) {
    $loader, $line = $key -split '/'
    $c = $matrix.cells.$key
    if (-not $c) { Write-Host "unknown cell $key" -ForegroundColor Red; $failed += $key; continue }
    Write-Host "=== $key (mc $($c.mc)) ==="
    & pwsh -NoProfile -File (Join-Path $PSScriptRoot 'cog-gen.ps1') -Loader $loader -Line $line
    if ($LASTEXITCODE -ne 0) { $failed += $key; continue }
    $cell = Join-Path (Join-Path $repo $loader) $line
    $env:JAVA_HOME = Find-Jdk $c.jdk
    Push-Location $cell
    & "$cell\gradlew.bat" clean build --console=plain --no-daemon *> "$cell\_build.log"
    $code = $LASTEXITCODE
    Pop-Location
    $jar = Get-ChildItem "$cell\build\libs\*.jar" -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notmatch 'sources|dev|slim' } | Select-Object -First 1
    $warn = (Select-String -CaseSensitive -Path "$cell\_build.log" -Pattern 'warning:' -ErrorAction SilentlyContinue | Measure-Object).Count
    if ($code -eq 0 -and $jar) {
        Copy-Item $jar.FullName (Join-Path $dist $jar.Name) -Force
        Write-Host "  OK -> $($jar.Name)  (javac/gradle warnings: $warn)"
    } else {
        Write-Host "  FAIL exit=$code (see $loader\$line\_build.log)" -ForegroundColor Red
        $failed += $key
    }
}
if ($failed.Count -gt 0) { Write-Host "FAILED: $($failed -join ', ')" -ForegroundColor Red; exit 1 }
Write-Host 'build complete'

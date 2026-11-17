# build-forge.ps1 -- wrapper: build every Forge cell (or pass a Line). See build.ps1.
param([string]$Line)
if ($Line) { & (Join-Path $PSScriptRoot "build.ps1") Forge $Line } else { & (Join-Path $PSScriptRoot "build.ps1") Forge }
exit $LASTEXITCODE

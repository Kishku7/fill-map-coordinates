# build-neoforge.ps1 -- wrapper: build every NeoForge cell (or pass a Line). See build.ps1.
param([string]$Line)
if ($Line) { & (Join-Path $PSScriptRoot "build.ps1") NeoForge $Line } else { & (Join-Path $PSScriptRoot "build.ps1") NeoForge }
exit $LASTEXITCODE

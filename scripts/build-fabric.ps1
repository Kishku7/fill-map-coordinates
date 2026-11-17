# build-fabric.ps1 -- wrapper: build every Fabric cell (or pass a Line). See build.ps1.
param([string]$Line)
if ($Line) { & (Join-Path $PSScriptRoot "build.ps1") Fabric $Line } else { & (Join-Path $PSScriptRoot "build.ps1") Fabric }
exit $LASTEXITCODE

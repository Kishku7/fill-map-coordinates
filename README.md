# Fill Map Coordinates - Build Guide (minecraft-1.21.11-26.4 branch)

Landing page: the `main` branch README. Report issues / support: https://github.com/Kishku7/mod_support

Fill Map Coordinates shows the corner coordinates of the area a filled map covers in the item tooltip.
This branch holds the one source that builds every supported Minecraft line and loader.

## What you need installed
- JDK 21 and JDK 25 (Eclipse Temurin; Gradle finds them by major version, no paths are pinned)
- Python 3 and Cog: `pip install cogapp`
- PowerShell 7

## How to build
```
pwsh scripts/build.ps1                   # every cell
pwsh scripts/build.ps1 Fabric            # every Fabric cell
pwsh scripts/build.ps1 NeoForge 26.2     # one cell
```
Jars land in `dist/` as `fillmapcoordinates-<version>+<mc line>-<loader>.jar`. Each cell build logs to
`<Loader>/<line>/_build.log`; a cell is only green when that log holds no `warning:` line.

## Toolchain matrix
Every cell is generated from `_codegen/matrix.json`.

| Loader | Lines | Toolchain |
|---|---|---|
| Fabric | 1.21.11 | fabric-loom 1.13.6, Mojang mappings, Gradle 8.14, JDK 21 |
| Fabric | 26.1, 26.2, 26.3, 26.4 | fabric-loom 1.16, unobfuscated, Gradle 9.4.1, JDK 25 |
| NeoForge | 1.21.11 | ModDevGradle 2.0.141, Gradle 8.14, JDK 21 |
| NeoForge | 26.1, 26.2, 26.3 | ModDevGradle 2.0.141, Gradle 9.2.1, JDK 25 |
| Forge | 1.21.11 | ForgeGradle 7, Gradle 9.6.0, JDK 21 |

Left out on purpose: Forge for 26.x (Forge never shipped a build that can compile the unobfuscated 26 line);
NeoForge 26.4 (NeoForge has not released it yet). The 26.4 Fabric cell is pinned to the single build
26.4-snapshot-10 (dependency `26.4-alpha.10`, pack format 104) because the line is still in snapshots.

## Repository layout
- `_codegen/cog_sources/` - the ONE copy of every Java and resource file (`common` = Minecraft-free, `shared` = Minecraft code, `resources`).
- `_codegen/compat.py` - per-loader entrypoints and the version axis.
- `_codegen/matrix.json` - pins, JDK, Gradle, pack format and dependency ranges per cell.
- `_codegen/cell_templates/` - per-loader build wiring and manifests with `@TOKENS@`.
- `scripts/` - `cog-gen.ps1` (materialise a cell), `build.ps1` (build cells), `_metadata.py` (manifest check).
- `Fabric/ NeoForge/ Forge/` - generated cells, gitignored.

## How the code generation works
`scripts/cog-gen.ps1` wipes a cell directory and rebuilds it from `_codegen`: build files, manifest, gradle
wrapper and the `gen/` Java tree. The Java is the same on every line because the Minecraft classes the mod
touches (`MapItem.create`, `MapItem.scaleMap`, `Item.appendHoverText`, `CustomData`) have the same names and
signatures from 1.21.11 to 26.4. Cog stamps the loader and line into `FmcInfo`; `compat.py` emits the
entrypoints. No Stonecutter, no Architectury.

## How it works
Two mixins. On the server, `MapItemMixin` records the centre and zoom of a map's saved data on the item when
the map is created and when it is zoomed out. On the client, `ItemMixin` adds the area line to the tooltip of a
filled map. The record lives in the item's custom data (`fmc_center_x`, `fmc_center_z`, `fmc_scale`); maps
made by the original mod (`coordinateX`, `coordinateZ`, `coordinateScale`) are still understood.

## Credits and licence
Based on Filled Map Coordinates by manuelgrabowski. Licence: Creative Commons Attribution-NonCommercial-ShareAlike
4.0 International. See `LICENSE` and `NOTICE.md`.

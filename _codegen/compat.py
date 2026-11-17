"""compat.py -- the ONE source of truth for per-loader ENTRYPOINTS and the version axis.

Fill Map Coordinates is one source for several Minecraft lines and three loaders:

    Fabric    1.21.11 (obfuscated, loom + mojang mappings, JDK 21)   26.1 26.2 26.3 26.4 (unobfuscated, JDK 25)
    NeoForge  1.21.11 (MDG, JDK 21)                                  26.1 26.2 26.3
    Forge     1.21.11 (FG7, JDK 21)                                  -- Forge never built 26.x

The Java under cog_sources/ is IDENTICAL on every line: MapItem.create / MapItem.scaleMap /
Item.appendHoverText / CustomData / MapItemSavedData have the same mojmap names and signatures from
1.21.11 through 26.4-snapshot-10 (checked against MC-Java for 1.21.11, 26.1.2, 26.2, 26.3, 26.4-s10).
So the drift lives only here and in matrix.json:
  * ENTRYPOINTS: each loader's @Mod / ModInitializer, emitted below (D15).
  * info_constants(): the loader name + Minecraft line stamped into FmcInfo by a Cog block.
  * version gates (vt/is_26/...): used by cog-gen.ps1 through `python compat.py gate <name> <ver>`.
  * matrix.json: pins, JDK, Gradle, pack_format, dependency ranges per cell.

Usage (called by scripts/cog-gen.ps1):
    python _codegen/compat.py <fabric|neoforge|forge> <gen_src_main_java_dir> <ver>
    python _codegen/compat.py gate <name> <ver>
    cog -r -D ver=<ver> -D loader=<loader> -D codegen=<_codegen> <file with [[[cog markers]]]>
"""

import os
import sys

PKG = "com.kishku7.fillmapcoordinates"
PKG_PATH = PKG.replace(".", "/")


# ============================================================ version axis ====
def vt(ver):
    """('1.21.11' | '26.2' | '26.4-snapshot-10') -> comparable int tuple."""
    return tuple(int(x) for x in ver.split("-")[0].split("."))


def is_26(ver):
    """26.x ships deobfuscated (no mappings step, JDK 25)."""
    return vt(ver) >= (26, 0)


def gate(name, ver):
    if name == "is_26":
        return "1" if is_26(ver) else "0"
    raise SystemExit("unknown gate: " + name)


# ============================================================ entrypoints =====
_FABRIC_MAIN = '''package com.kishku7.fillmapcoordinates;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

/** Fill Map Coordinates - Fabric entrypoint: loads the config and registers /mapcoordinates. */
public class FillMapCoordinatesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        FmcConfig.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> FmcCommand.register(dispatcher));
        FmcInfo.LOGGER.info("Fill Map Coordinates ready ({} / {})", FmcInfo.LOADER, FmcInfo.MC_LINE);
    }
}
'''

_NEOFORGE_MAIN = '''package com.kishku7.fillmapcoordinates;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Fill Map Coordinates - NeoForge entrypoint: loads the config and registers /mapcoordinates. */
@Mod(FmcInfo.MOD_ID)
public class FillMapCoordinatesNeoForge {

    public FillMapCoordinatesNeoForge() {
        FmcConfig.init();
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> FmcCommand.register(event.getDispatcher()));
        FmcInfo.LOGGER.info("Fill Map Coordinates ready ({} / {})", FmcInfo.LOADER, FmcInfo.MC_LINE);
    }
}
'''

# Forge 61 (1.21.11, EventBus 7): game-bus events expose a static BUS; listeners take the event.
_FORGE_MAIN = '''package com.kishku7.fillmapcoordinates;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;

/** Fill Map Coordinates - Forge entrypoint: loads the config and registers /mapcoordinates. */
@Mod(FmcInfo.MOD_ID)
public class FillMapCoordinatesForge {

    public FillMapCoordinatesForge() {
        FmcConfig.init();
        RegisterCommandsEvent.BUS.addListener(event -> FmcCommand.register(event.getDispatcher()));
        FmcInfo.LOGGER.info("Fill Map Coordinates ready ({} / {})", FmcInfo.LOADER, FmcInfo.MC_LINE);
    }
}
'''

_FILES = {
    "fabric": {PKG_PATH + "/FillMapCoordinatesFabric.java": _FABRIC_MAIN},
    "neoforge": {PKG_PATH + "/FillMapCoordinatesNeoForge.java": _NEOFORGE_MAIN},
    "forge": {PKG_PATH + "/FillMapCoordinatesForge.java": _FORGE_MAIN},
}


def entrypoint_files(loader):
    loader = loader.lower()
    if loader not in _FILES:
        raise SystemExit("unknown loader: {0} (fabric|neoforge|forge)".format(loader))
    return _FILES[loader]


def emit(loader, gen_java_dir, ver=None):
    for rel, text in entrypoint_files(loader).items():
        dest = os.path.join(gen_java_dir, rel.replace("/", os.sep))
        os.makedirs(os.path.dirname(dest), exist_ok=True)
        with open(dest, "w", encoding="utf-8", newline="\n") as f:
            f.write(text)
        print("emit entrypoint: " + rel)


# ==================================================== shared-code Cog emit ====
def info_constants(cog, ver, loader):
    """Stamp the loader name and the Minecraft line the cell was generated for."""
    line = ".".join(ver.split("-")[0].split(".")[:2])
    cog.outl('public static final String LOADER = "{0}";'.format(loader))
    cog.outl('public static final String MC_LINE = "{0}";'.format(line))


if __name__ == "__main__":
    if len(sys.argv) == 4 and sys.argv[1] == "gate":
        print(gate(sys.argv[2], sys.argv[3]))
    elif len(sys.argv) in (3, 4):
        emit(sys.argv[1], sys.argv[2], sys.argv[3] if len(sys.argv) == 4 else None)
    else:
        raise SystemExit("usage: python compat.py <fabric|neoforge|forge> <gen_src_main_java_dir> [ver]  |  gate <name> <ver>")

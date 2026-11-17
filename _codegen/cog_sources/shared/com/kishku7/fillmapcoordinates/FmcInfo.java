package com.kishku7.fillmapcoordinates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Identity constants. The loader and Minecraft line are stamped in by cog at generation time. */
public final class FmcInfo {
    public static final String MOD_ID = "fillmapcoordinates";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    //[[[cog
    // import sys; sys.path.insert(0, codegen); import compat
    // compat.info_constants(cog, ver, loader)
    //]]]
    public static final String LOADER = "";
    public static final String MC_LINE = "";
    //[[[end]]]

    private FmcInfo() {}
}

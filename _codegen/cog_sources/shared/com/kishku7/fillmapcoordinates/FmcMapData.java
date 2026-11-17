package com.kishku7.fillmapcoordinates;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/**
 * Reads and writes the map-area record kept in the item's custom data. The server writes the real
 * centre and zoom of the map's saved data when a map is created or zoomed; the client turns that
 * into the corner coordinates it shows.
 */
public final class FmcMapData {
    private static final String KEY_X = "fmc_center_x";
    private static final String KEY_Z = "fmc_center_z";
    private static final String KEY_SCALE = "fmc_scale";
    // Keys written by the original Filled Map Coordinates (the filling player's position + zoom).
    private static final String OLD_X = "coordinateX";
    private static final String OLD_Z = "coordinateZ";
    private static final String OLD_SCALE = "coordinateScale";

    private FmcMapData() {}

    /** Records centre and zoom of the map this stack points at. Server side only. */
    public static void record(ItemStack stack, ServerLevel level) {
        MapItemSavedData data = MapItem.getSavedData(stack, level);
        if (data == null) {
            return;
        }
        final int cx = data.centerX;
        final int cz = data.centerZ;
        final int scale = data.scale;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt(KEY_X, cx);
            tag.putInt(KEY_Z, cz);
            tag.putInt(KEY_SCALE, scale);
        });
    }

    /** Corner coordinates {x1, z1, x2, z2} (top-left, bottom-right), or null if the stack carries no record. */
    public static int[] corners(ItemStack stack) {
        CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
        if (custom == null) {
            return null;
        }
        CompoundTag tag = custom.copyTag();
        if (tag.contains(KEY_X) && tag.contains(KEY_Z) && tag.contains(KEY_SCALE)) {
            int width = 128 << clampScale(tag.getIntOr(KEY_SCALE, 0));
            int left = tag.getIntOr(KEY_X, 0) - width / 2;
            int top = tag.getIntOr(KEY_Z, 0) - width / 2;
            return new int[] {left, top, left + width - 1, top + width - 1};
        }
        if (tag.contains(OLD_X) && tag.contains(OLD_Z) && tag.contains(OLD_SCALE)) {
            int width = 128 << clampScale(tag.getIntOr(OLD_SCALE, 0));
            int left = snap(tag.getIntOr(OLD_X, 0), width) - width / 2;
            int top = snap(tag.getIntOr(OLD_Z, 0), width) - width / 2;
            return new int[] {left, top, left + width - 1, top + width - 1};
        }
        return null;
    }

    // Vanilla map grid: the centre of the map cell that contains the point.
    private static int snap(int point, int width) {
        int cell = (int) Math.floor((point + 64.0) / width);
        return cell * width + width / 2 - 64;
    }

    private static int clampScale(int scale) {
        return Math.max(0, Math.min(4, scale));
    }
}

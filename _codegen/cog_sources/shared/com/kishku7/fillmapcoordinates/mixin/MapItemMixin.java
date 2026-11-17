package com.kishku7.fillmapcoordinates.mixin;

import com.kishku7.fillmapcoordinates.FmcConfig;
import com.kishku7.fillmapcoordinates.FmcMapData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Server side: record the area a map covers when it is filled, and again when it is zoomed out. */
@Mixin(MapItem.class)
public abstract class MapItemMixin {

    @Inject(method = "create", at = @At("RETURN"))
    private static void fmc$afterCreate(ServerLevel level, int originX, int originZ, byte scale,
                                        boolean trackPosition, boolean unlimitedTracking,
                                        CallbackInfoReturnable<ItemStack> cir) {
        if (FmcConfig.isEnabled()) {
            FmcMapData.record(cir.getReturnValue(), level);
        }
    }

    @Inject(method = "scaleMap", at = @At("TAIL"))
    private static void fmc$afterScale(ItemStack stack, ServerLevel level, CallbackInfo ci) {
        if (FmcConfig.isEnabled()) {
            FmcMapData.record(stack, level);
        }
    }
}

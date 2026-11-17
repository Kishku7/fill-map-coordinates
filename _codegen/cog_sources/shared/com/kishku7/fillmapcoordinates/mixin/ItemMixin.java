package com.kishku7.fillmapcoordinates.mixin;

import com.kishku7.fillmapcoordinates.FmcConfig;
import com.kishku7.fillmapcoordinates.FmcMapData;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Client side: append the covered area to a filled map's tooltip. */
@Mixin(Item.class)
public abstract class ItemMixin {

    @Inject(method = "appendHoverText", at = @At("TAIL"))
    private void fmc$areaLine(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                              Consumer<Component> lines, TooltipFlag flag, CallbackInfo ci) {
        if (!stack.is(Items.FILLED_MAP) || !FmcConfig.isEnabled()) {
            return;
        }
        int[] c = FmcMapData.corners(stack);
        if (c != null) {
            lines.accept(Component.translatable("fillmapcoordinates.coords",
                    c[0] + ", " + c[1], c[2] + ", " + c[3]).withStyle(ChatFormatting.GRAY));
        }
    }
}

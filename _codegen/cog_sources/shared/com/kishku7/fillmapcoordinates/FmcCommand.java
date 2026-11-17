package com.kishku7.fillmapcoordinates;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** {@code /mapcoordinates enabled [true|false]} - shows or sets the one config value. */
public final class FmcCommand {
    private FmcCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mapcoordinates")
                .then(Commands.literal("enabled")
                        .executes(ctx -> {
                            String state = FmcConfig.isEnabled() ? "enabled" : "disabled";
                            ctx.getSource().sendSuccess(() -> note("Mod is currently " + state), false);
                            return 1;
                        })
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    FmcConfig.setEnabled(BoolArgumentType.getBool(ctx, "value"));
                                    String state = FmcConfig.isEnabled() ? "enabled" : "disabled";
                                    ctx.getSource().sendSuccess(() -> note("Mod is now " + state), true);
                                    return 1;
                                }))));
    }

    private static Component note(String text) {
        return Component.literal(text).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    }
}

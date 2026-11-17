package com.kishku7.fillmapcoordinates;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
//[[[cog
// import sys; sys.path.insert(0, codegen); import compat
// compat.command_bool_import(cog, ver)
//]]]
import com.mojang.brigadier.arguments.BoolArgumentType;
//[[[end]]]
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
                        .then(Commands.argument("value", boolArg())
                                .executes(ctx -> {
                                    FmcConfig.setEnabled(boolValue(ctx, "value"));
                                    String state = FmcConfig.isEnabled() ? "enabled" : "disabled";
                                    ctx.getSource().sendSuccess(() -> note("Mod is now " + state), true);
                                    return 1;
                                }))));
    }

    //[[[cog
    // compat.command_bool_helpers(cog, ver)
    //]]]
    private static ArgumentType<Boolean> boolArg() {
        return BoolArgumentType.bool();
    }

    private static boolean boolValue(CommandContext<CommandSourceStack> ctx, String name) {
        return BoolArgumentType.getBool(ctx, name);
    }
    //[[[end]]]

    private static Component note(String text) {
        return Component.literal(text).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    }
}

package com.chinaex123.winter_falls.event;

import com.chinaex123.winter_falls.util.WinterFallsWorld;
import com.chinaex123.winter_falls.util.WorldSavedData;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber
public class CommandHandler {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // 拦截 /weather 命令（只在寒冬世界）
        dispatcher.register(Commands.literal("weather").executes(source -> {
            ServerLevel level = source.getSource().getLevel();
            if (WinterFallsWorld.isWinterFallsWorld(level)) {
                source.getSource().sendSuccess(
                    () -> Component.translatable("command.winter_falls.weather.blocked").withStyle(ChatFormatting.RED), true);
                return 0;
            }
            return 1;
        }));
    }
}
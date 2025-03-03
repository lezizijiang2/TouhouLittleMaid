package com.github.tartaricacid.touhoulittlemaid.command.subcommand;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.AvailableSites;
import com.github.tartaricacid.touhoulittlemaid.network.message.SyncAiSettingPackage;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class AIChatCommand {
    private static final String ROOT_NAME = "ai_chat";
    private static final String RELOAD_NAME = "reload";

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.literal(ROOT_NAME);
        LiteralArgumentBuilder<CommandSourceStack> reload = LiteralArgumentBuilder.literal(RELOAD_NAME);
        root.then(reload.executes(AIChatCommand::reload));
        return root;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        AvailableSites.readSites();
        PacketDistributor.sendToAllPlayers(SyncAiSettingPackage.getInstance());
        context.getSource().sendSuccess(() -> Component.translatable("message.touhou_little_maid.ai_chat.reload_success"), true);
        return Command.SINGLE_SUCCESS;
    }
}

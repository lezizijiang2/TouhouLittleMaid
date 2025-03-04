package com.github.tartaricacid.touhoulittlemaid.command.subcommand;

import com.github.tartaricacid.touhoulittlemaid.client.event.ReloadResourceEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.info.ServerCustomPackLoader;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.network.message.SyncAiSettingMessage;
import com.github.tartaricacid.touhoulittlemaid.util.version.TComponent;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.PacketDistributor;

public final class PackCommand {
    private static final String PACK_NAME = "pack";
    private static final String RELOAD_NAME = "reload";

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        LiteralArgumentBuilder<CommandSourceStack> pack = Commands.literal(PACK_NAME);
        LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal(RELOAD_NAME);
        pack.then(reload.executes(PackCommand::reloadAllPack));
        return pack;
    }

    private static int reloadAllPack(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(TComponent.translatable("commands.touhou_little_maid.pack.reload.start"), true);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ReloadResourceEvent::asyncReloadAllPack);
        ServerCustomPackLoader.reloadPacks();
        NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new SyncAiSettingMessage());
        return Command.SINGLE_SUCCESS;
    }
}

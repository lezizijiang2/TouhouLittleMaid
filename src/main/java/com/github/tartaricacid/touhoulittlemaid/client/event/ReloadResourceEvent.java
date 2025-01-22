package com.github.tartaricacid.touhoulittlemaid.client.event;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.animation.inner.InnerAnimation;
import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.github.tartaricacid.touhoulittlemaid.client.resource.GeckoModelLoader;
import com.github.tartaricacid.touhoulittlemaid.client.resource.models.PlayerMaidModels;
import com.github.tartaricacid.touhoulittlemaid.util.version.TComponent;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.time.StopWatch;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ReloadResourceEvent extends SimplePreparableReloadListener<Void> {
    public static final ResourceLocation BLOCK_ATLAS_TEXTURE = new ResourceLocation("textures/atlas/blocks.png");
    public static final ResourceLocation TANK_INPUT_SLOT = new ResourceLocation(TouhouLittleMaid.MOD_ID, "slot/tank_input_slot");
    public static final ResourceLocation TANK_OUTPUT_SLOT = new ResourceLocation(TouhouLittleMaid.MOD_ID, "slot/tank_output_slot");
    public static final ResourceLocation EMPTY_MAINHAND_SLOT = new ResourceLocation(TouhouLittleMaid.MOD_ID, "slot/empty_mainhand_slot");
    public static final ResourceLocation EMPTY_BACK_SHOW_SLOT = new ResourceLocation(TouhouLittleMaid.MOD_ID, "slot/empty_back_show_slot");

    @SubscribeEvent
    public static void onRegister(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ReloadResourceEvent());
    }

    public static void asyncReloadAllPack() {
        CompletableFuture.supplyAsync(() -> {
            reloadAllPack();
            return null;
        }, Util.backgroundExecutor());
    }

    private static void reloadAllPack() {
        StopWatch watch = StopWatch.createStarted();
        {
            GeckoModelLoader.reload();
            InnerAnimation.init();
            CustomPackLoader.reloadPacks();
            PlayerMaidModels.reload();
        }
        watch.stop();
        double time = watch.getTime(TimeUnit.MICROSECONDS) / 1000.0;
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.sendMessage(TComponent.translatable("message.touhou_little_maid.reload.tip", time), Util.NIL_UUID);
        }
        TouhouLittleMaid.LOGGER.info("Model loading time: {} ms", time);
    }

    @Override
    protected Void prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        reloadAllPack();
        return null;
    }

    @Override
    protected void apply(Void pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
    }
}

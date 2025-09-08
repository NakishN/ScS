package com.scs;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.scs.client.ChatTap;
import com.scs.client.HudOverlay;
import com.scs.client.ChatButtonHandler;
import com.scs.client.ShaurmaSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Scs.MODID)
public final class Scs {
    public static final String MODID = "scs";
    public static final Logger LOGGER = LogManager.getLogger();

    public Scs() {
        // Регистрируем конфиг
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);

        // Регистрируем на MOD bus для инициализации
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);

        // Только на клиенте - ТОЛЬКО НУЖНЫЕ КОМПОНЕНТЫ
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            // Регистрируем только основные обработчики
            MinecraftForge.EVENT_BUS.register(new ChatTap());
            MinecraftForge.EVENT_BUS.register(new HudOverlay());
            MinecraftForge.EVENT_BUS.register(new ChatButtonHandler());

            LOGGER.info("ScS mod initialized - ПРОСТЫЕ КНОПКИ ГОТОВЫ!");
        });
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        // Инициализируем систему шаурмы при загрузке клиента
        event.enqueueWork(() -> {
            ShaurmaSystem.loadShaurmaData();
            LOGGER.info("Shaurma system initialized!");
        });
    }
}
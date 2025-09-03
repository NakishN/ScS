package com.scs;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.scs.client.ChatTap;
import com.scs.client.HudOverlay;
import com.scs.client.KeyBindings;
import com.scs.client.ChatButtonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Scs.MODID)
public final class Scs {
    public static final String MODID = "scs";
    public static final Logger LOGGER = LogManager.getLogger();

    public Scs() {
        // Регистрируем конфиг
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);

        // Только на клиенте
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            // Регистрируем обработчики событий
            MinecraftForge.EVENT_BUS.register(new ChatTap());
            MinecraftForge.EVENT_BUS.register(new HudOverlay());
            MinecraftForge.EVENT_BUS.register(new ChatButtonHandler());
            MinecraftForge.EVENT_BUS.register(KeyBindings.class);

            // Регистрируем клавиши
            FMLJavaModLoadingContext.get().getModEventBus().register(KeyBindings.class);

            LOGGER.info("ScS mod initialized successfully!");
        });
    }
}
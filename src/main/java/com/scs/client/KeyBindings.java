package com.scs.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import com.scs.Scs;

@Mod.EventBusSubscriber(modid = Scs.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeyBindings {

    public static final KeyMapping TOGGLE_HUD = new KeyMapping(
            "key.scs.toggle_hud",
            GLFW.GLFW_KEY_F8,
            "key.categories.scs"
    );

    public static final KeyMapping SHOW_HISTORY = new KeyMapping(
            "key.scs.show_history",
            GLFW.GLFW_KEY_F9,
            "key.categories.scs"
    );

    public static final KeyMapping CLEAR_ENTRIES = new KeyMapping(
            "key.scs.clear_entries",
            GLFW.GLFW_KEY_F10,
            "key.categories.scs"
    );

    // Новые клавиши для системы шаурмы
    public static final KeyMapping SHAURMA_TAP = new KeyMapping(
            "key.scs.shaurma_tap",
            GLFW.GLFW_KEY_U,
            "key.categories.scs.shaurma"
    );

    public static final KeyMapping SHAURMA_MENU = new KeyMapping(
            "key.scs.shaurma_menu",
            GLFW.GLFW_KEY_Y,
            "key.categories.scs.shaurma"
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_HUD);
        event.register(SHOW_HISTORY);
        event.register(CLEAR_ENTRIES);
        event.register(SHAURMA_TAP);
        event.register(SHAURMA_MENU);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();

        // Существующие клавиши
        if (TOGGLE_HUD.consumeClick()) {
            HudOverlay.toggleHud();
        }

        if (SHOW_HISTORY.consumeClick()) {
            mc.setScreen(new ChatHistoryScreen());
        }

        if (CLEAR_ENTRIES.consumeClick()) {
            ChatTap.clearEntries();
            mc.gui.getChat().addMessage(net.minecraft.network.chat.Component.literal(
                    "§e[ScS] История очищена!"));
        }

        // Новые клавиши для шаурмы (только если система включена)
        if (com.scs.Config.enableShaurma) {
            if (SHAURMA_TAP.consumeClick()) {
                // Тап шаурмы - работает везде, даже в игре
                ShaurmaSystem.onShaurmaTap();
            }

            if (SHAURMA_MENU.consumeClick()) {
                // Открыть меню шаурмы
                mc.setScreen(new ShaurmaMenuScreen());
            }
        }
    }
}
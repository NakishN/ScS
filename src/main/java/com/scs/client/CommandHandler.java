package com.scs.client;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import com.scs.Scs;

public class CommandHandler {

    @SubscribeEvent
    public void onClientChat(ClientChatEvent event) {
        String message = event.getMessage();

        Scs.LOGGER.info("[ScS] ClientChatEvent: '{}'", message);

        // Перехватываем специальные команды ScS
        if (message.startsWith("/scs:")) {
            Scs.LOGGER.info("[ScS] Intercepting ScS command: {}", message);
            event.setCanceled(true); // Отменяем отправку на сервер

            // Обрабатываем команду
            handleScSCommand(message);
            return;
        }
    }

    private void handleScSCommand(String command) {
        try {
            Minecraft mc = Minecraft.getInstance();

            if (command.startsWith("/scs:history_all ")) {
                String playersStr = command.substring("/scs:history_all ".length());
                String[] players = playersStr.split(",");

                mc.gui.getChat().addMessage(Component.literal(
                        "§e[ScS] Запускаю проверку истории для " + players.length + " игроков..."));

                for (String player : players) {
                    String cleanPlayer = player.trim();
                    if (!cleanPlayer.isEmpty()) {
                        CommandScheduler.scheduleCommand("/history " + cleanPlayer, "История для " + cleanPlayer);
                    }
                }

            } else if (command.startsWith("/scs:freezing_history_all ")) {
                String playersStr = command.substring("/scs:freezing_history_all ".length());
                String[] players = playersStr.split(",");

                mc.gui.getChat().addMessage(Component.literal(
                        "§e[ScS] Запускаю проверку freezing истории для " + players.length + " игроков..."));

                for (String player : players) {
                    String cleanPlayer = player.trim();
                    if (!cleanPlayer.isEmpty()) {
                        CommandScheduler.scheduleCommand("/freezinghistory " + cleanPlayer, "Freezing история для " + cleanPlayer);
                    }
                }

            } else if (command.equals("/scs:clear_queue")) {
                CommandScheduler.clearQueue();

            } else if (command.startsWith("/scs:delay ")) {
                try {
                    int newDelay = Integer.parseInt(command.substring("/scs:delay ".length()));
                    CommandScheduler.setCommandDelay(newDelay);
                    mc.gui.getChat().addMessage(Component.literal(
                            "§a[ScS] Задержка между командами установлена: " + (newDelay / 1000.0) + "с"));
                } catch (NumberFormatException e) {
                    mc.gui.getChat().addMessage(Component.literal(
                            "§c[ScS] Неверный формат. Используйте: /scs:delay 1200"));
                }

            } else if (command.equals("/scs:help")) {
                showHelp();

            } else {
                mc.gui.getChat().addMessage(Component.literal(
                        "§c[ScS] Неизвестная команда. Используйте /scs:help"));
            }

        } catch (Exception e) {
            Scs.LOGGER.error("[ScS] Error handling ScS command: {}", e.getMessage());
        }
    }

    private void showHelp() {
        Minecraft mc = Minecraft.getInstance();
        mc.gui.getChat().addMessage(Component.literal("§6=== ScS Команды ==="));
        mc.gui.getChat().addMessage(Component.literal("§e/scs:history_all ник1,ник2,ник3 §7- история всех игроков"));
        mc.gui.getChat().addMessage(Component.literal("§e/scs:freezing_history_all ник1,ник2 §7- freezing история всех"));
        mc.gui.getChat().addMessage(Component.literal("§e/scs:clear_queue §7- очистить очередь команд"));
        mc.gui.getChat().addMessage(Component.literal("§e/scs:delay 1200 §7- задержка между командами (мс)"));
        mc.gui.getChat().addMessage(Component.literal("§7Текущая очередь: " + CommandScheduler.getQueueSize() + " команд"));
    }
}
package com.scs.client;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import com.scs.Scs;

@Mod.EventBusSubscriber(modid = Scs.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandHandler {

    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        String message = event.getMessage();

        // Перехватываем специальные команды ScS
        if (message.startsWith("/scs:")) {
            Scs.LOGGER.info("[ScS] Intercepting ScS command: {}", message);
            event.setCanceled(true); // Отменяем отправку на сервер

            // Обрабатываем команду
            handleScSCommand(message);
            return;
        }

        // Перехватываем команды от наших кнопок для логирования
        if (message.startsWith("/freezing ") || message.startsWith("/matrix spectate ")) {
            String[] parts = message.split(" ", 3);
            if (parts.length >= 2) {
                String command = parts[0].substring(1); // убираем /
                if (parts.length >= 3 && "matrix".equals(command)) {
                    command = "matrix " + parts[1]; // matrix spectate
                }
                String target = parts[parts.length - 1]; // берем последний аргумент как имя игрока

                logCommand(command, target, message);
            }
        }
    }

    private static void handleScSCommand(String command) {
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
                        CommandScheduler.scheduleCommand("/freezinghistory " + cleanPlayer, "F.История для " + cleanPlayer);
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

            } else if (command.equals("/scs:status")) {
                showStatus();

            } else {
                mc.gui.getChat().addMessage(Component.literal(
                        "§c[ScS] Неизвестная команда. Используйте /scs:help"));
            }

        } catch (Exception e) {
            Scs.LOGGER.error("[ScS] Error handling ScS command: {}", e.getMessage());
        }
    }

    private static void showHelp() {
        Minecraft mc = Minecraft.getInstance();
        mc.gui.getChat().addMessage(Component.literal("§6=== ScS Команды ==="));
        mc.gui.getChat().addMessage(Component.literal("§e/scs:history_all ник1,ник2,ник3 §7- история всех игроков"));
        mc.gui.getChat().addMessage(Component.literal("§e/scs:freezing_history_all ник1,ник2 §7- freezing история всех"));
        mc.gui.getChat().addMessage(Component.literal("§e/scs:clear_queue §7- очистить очередь команд"));
        mc.gui.getChat().addMessage(Component.literal("§e/scs:delay 1200 §7- задержка между командами (мс)"));
        mc.gui.getChat().addMessage(Component.literal("§e/scs:status §7- показать статус мода"));
        mc.gui.getChat().addMessage(Component.literal("§7Текущая очередь: " + CommandScheduler.getQueueSize() + " команд"));
    }

    private static void showStatus() {
        Minecraft mc = Minecraft.getInstance();
        mc.gui.getChat().addMessage(Component.literal("§6=== ScS Статус ==="));
        mc.gui.getChat().addMessage(Component.literal("§7Записей в памяти: " + ChatTap.ENTRIES.size()));
        mc.gui.getChat().addMessage(Component.literal("§7Нарушений: " + ChatTap.VIOLATIONS.size()));
        mc.gui.getChat().addMessage(Component.literal("§7DupeIP сканов: " + ChatTap.getDupeIPResults().size()));
        mc.gui.getChat().addMessage(Component.literal("§7Сообщений чата: " + ChatTap.PLAYER_CHAT.size()));
        mc.gui.getChat().addMessage(Component.literal("§7" + CommandScheduler.getQueueInfo()));
    }

    private static void logCommand(String command, String target, String fullMessage) {
        String logEntry = String.format("Command executed: %s -> %s (full: %s)",
                command.toUpperCase(), target, fullMessage);
        Scs.LOGGER.info(logEntry);
    }
}
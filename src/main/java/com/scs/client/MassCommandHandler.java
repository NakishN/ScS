package com.scs.client;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import com.scs.Scs;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MassCommandHandler {

    private static final Queue<String> commandQueue = new ConcurrentLinkedQueue<>();
    private static long lastCommandTime = 0;
    private static final int COMMAND_DELAY = 1200; // 1.2 секунды

    @SubscribeEvent
    public void onClientChat(ClientChatEvent event) {
        String message = event.getMessage();

        if (message.startsWith("/scs_history_mass ")) {
            event.setCanceled(true);
            String players = message.substring("/scs_history_mass ".length());
            executeMassHistory(players);

        } else if (message.startsWith("/scs_freezing_mass ")) {
            event.setCanceled(true);
            String players = message.substring("/scs_freezing_mass ".length());
            executeMassFreezingHistory(players);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Обрабатываем очередь команд
        processCommandQueue();
    }

    private void executeMassHistory(String players) {
        Minecraft mc = Minecraft.getInstance();
        String[] playerList = players.split("\\s+");

        mc.gui.getChat().addMessage(Component.literal(
                "§e[ScS] Запускаю /history для " + playerList.length + " игроков с задержкой 1.2с..."));

        for (String player : playerList) {
            player = player.trim();
            if (!player.isEmpty()) {
                commandQueue.offer("/history " + player);
                Scs.LOGGER.info("[ScS] Добавлена команда в очередь: /history {}", player);
            }
        }
    }

    private void executeMassFreezingHistory(String players) {
        Minecraft mc = Minecraft.getInstance();
        String[] playerList = players.split("\\s+");

        mc.gui.getChat().addMessage(Component.literal(
                "§d[ScS] Запускаю /freezinghistory для " + playerList.length + " игроков с задержкой 1.2с..."));

        for (String player : playerList) {
            player = player.trim();
            if (!player.isEmpty()) {
                commandQueue.offer("/freezinghistory " + player);
                Scs.LOGGER.info("[ScS] Добавлена команда в очередь: /freezinghistory {}", player);
            }
        }
    }

    private void processCommandQueue() {
        if (commandQueue.isEmpty()) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCommandTime >= COMMAND_DELAY) {
            String command = commandQueue.poll();
            if (command != null) {
                executeCommand(command);
                lastCommandTime = currentTime;
            }
        }
    }

    private void executeCommand(String command) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.connection != null) {
                // Убираем / из команды
                String cmdToExecute = command.startsWith("/") ? command.substring(1) : command;
                mc.player.connection.sendCommand(cmdToExecute);

                // Показываем прогресс
                int remaining = commandQueue.size();
                mc.gui.getChat().addMessage(Component.literal(
                        "§7[ScS] Выполнено: " + command + " §7(осталось: " + remaining + ")"));

                Scs.LOGGER.info("[ScS] Выполнена массовая команда: {} (осталось: {})", command, remaining);
            }
        } catch (Exception e) {
            Scs.LOGGER.error("[ScS] Ошибка выполнения массовой команды: {}", e.getMessage());
        }
    }

    public static int getQueueSize() {
        return commandQueue.size();
    }

    public static void clearQueue() {
        int cleared = commandQueue.size();
        commandQueue.clear();
        Scs.LOGGER.info("[ScS] Очищена очередь массовых команд: {} команд", cleared);
    }
}
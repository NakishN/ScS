package com.scs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import com.scs.Config;
import com.scs.Scs;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod.EventBusSubscriber(modid = Scs.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandScheduler {

    private static final Queue<ScheduledCommand> commandQueue = new ConcurrentLinkedQueue<>();
    private static long lastCommandTime = 0;
    private static int commandDelay = 1200; // 1.2 секунды между командами (настраиваемо)

    public static class ScheduledCommand {
        public final String command;
        public final String description;
        public final long scheduledTime;

        public ScheduledCommand(String command, String description) {
            this.command = command;
            this.description = description;
            this.scheduledTime = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Проверяем очередь команд каждый тик
        processCommandQueue();
    }

    private static void processCommandQueue() {
        if (commandQueue.isEmpty()) return;

        long currentTime = System.currentTimeMillis();

        // Проверяем, прошло ли достаточно времени с последней команды
        if (currentTime - lastCommandTime >= commandDelay) {
            ScheduledCommand nextCommand = commandQueue.poll();
            if (nextCommand != null) {
                executeCommand(nextCommand);
                lastCommandTime = currentTime;
            }
        }
    }

    private static void executeCommand(ScheduledCommand scheduledCommand) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.connection != null) {
                // Выполняем команду
                String cmdToExecute = scheduledCommand.command.startsWith("/") ?
                        scheduledCommand.command.substring(1) : scheduledCommand.command;

                mc.player.connection.sendCommand(cmdToExecute);

                // Показываем что команда выполнена
                mc.gui.getChat().addMessage(Component.literal(
                        "§7[ScS] Выполнено: " + scheduledCommand.description));

                Scs.LOGGER.info("[ScS] Executed scheduled command: {}", scheduledCommand.command);
            }
        } catch (Exception e) {
            Scs.LOGGER.error("[ScS] Error executing scheduled command: {}", e.getMessage());
        }
    }

    public static void scheduleCommand(String command, String description) {
        if (command == null || command.trim().isEmpty()) return;

        // Добавляем команду в очередь
        commandQueue.offer(new ScheduledCommand(command, description));

        // Показываем что команда добавлена в очередь
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui != null) {
                int queueSize = commandQueue.size();
                mc.gui.getChat().addMessage(Component.literal(
                        "§e[ScS] В очереди: " + description + " (всего: " + queueSize + ")"));
            }
        } catch (Exception e) {
            Scs.LOGGER.error("[ScS] Error showing queue message: {}", e.getMessage());
        }

        Scs.LOGGER.info("[ScS] Scheduled command: {} ({})", command, description);
    }

    public static void scheduleMultipleCommands(List<String> players, String commandTemplate, String actionDescription) {
        if (players == null || players.isEmpty()) return;

        for (String player : players) {
            if (player != null && !player.trim().isEmpty()) {
                String command = commandTemplate.replace("{player}", player.trim());
                String description = actionDescription + " для " + player.trim();
                scheduleCommand(command, description);
            }
        }

        // Показываем общую информацию
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui != null) {
                mc.gui.getChat().addMessage(Component.literal(
                        "§6[ScS] Запланировано " + players.size() + " команд: " + actionDescription +
                                " (задержка: " + (commandDelay / 1000.0) + "с)"));
            }
        } catch (Exception ignored) {}
    }

    public static void clearQueue() {
        int clearedCount = commandQueue.size();
        commandQueue.clear();

        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui != null) {
                mc.gui.getChat().addMessage(Component.literal(
                        "§c[ScS] Очередь команд очищена (" + clearedCount + " команд удалено)"));
            }
        } catch (Exception ignored) {}

        Scs.LOGGER.info("[ScS] Command queue cleared: {} commands", clearedCount);
    }

    public static int getQueueSize() {
        return commandQueue.size();
    }

    public static void setCommandDelay(int delayMs) {
        commandDelay = Math.max(500, delayMs); // Минимум 0.5 секунды
        Scs.LOGGER.info("[ScS] Command delay set to: {}ms", commandDelay);
    }

    public static String getQueueInfo() {
        if (commandQueue.isEmpty()) {
            return "Очередь команд пуста";
        }

        StringBuilder info = new StringBuilder();
        info.append("В очереди ").append(commandQueue.size()).append(" команд:");

        int count = 0;
        for (ScheduledCommand cmd : commandQueue) {
            if (count >= 3) {
                info.append("\n  ... и еще ").append(commandQueue.size() - 3);
                break;
            }
            info.append("\n  ").append(count + 1).append(". ").append(cmd.description);
            count++;
        }

        return info.toString();
    }
}
package com.scs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.ClientChatEvent;
import com.scs.Config;
import com.scs.Scs;

public class ChatButtonHandler {

    @SubscribeEvent
    public void onClientChat(ClientChatEvent event) {
        String message = event.getMessage();

        // DUPEIP команды
        if (message.startsWith("/dupeip_history ")) {
            event.setCanceled(true);
            String players = message.substring("/dupeip_history ".length());
            executeDupeIPHistory(players.split(" "));
            return;
        }

        if (message.startsWith("/dupeip_freezing ")) {
            event.setCanceled(true);
            String players = message.substring("/dupeip_freezing ".length());
            executeDupeIPFreezing(players.split(" "));
            return;
        }

        // Логирование обычных команд
        if (message.startsWith("/freezing ") || message.startsWith("/matrix spectate ") ||
                message.startsWith("/playeractivity ") || message.startsWith("/freezinghistory ")) {

            String[] parts = message.split(" ");
            if (parts.length >= 2) {
                String command = parts[0].substring(1);
                String target = parts[parts.length - 1];
                logCommand(command, target, message);
            }
        }
    }

    // /history для всех DupeIP игроков с задержкой
    private void executeDupeIPHistory(String[] players) {
        Minecraft mc = Minecraft.getInstance();
        mc.gui.getChat().addMessage(Component.literal(
                "§e[ScS] Запускаю /history для " + players.length + " игроков..."));

        new Thread(() -> {
            for (int i = 0; i < players.length; i++) {
                String player = players[i].trim();
                if (!player.isEmpty() && isValidPlayerName(player)) {

                    mc.execute(() -> {
                        if (mc.player != null && mc.player.connection != null) {
                            mc.player.connection.sendCommand("history " + player);
                            mc.gui.getChat().addMessage(Component.literal(
                                    "§7[ScS] История: " + player));
                        }
                    });

                    try {
                        Thread.sleep(1200); // 1.2 секунды
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }).start();
    }

    // /freezinghistory для всех DupeIP игроков с задержкой
    private void executeDupeIPFreezing(String[] players) {
        Minecraft mc = Minecraft.getInstance();
        mc.gui.getChat().addMessage(Component.literal(
                "§d[ScS] Запускаю /freezinghistory для " + players.length + " игроков..."));

        new Thread(() -> {
            for (int i = 0; i < players.length; i++) {
                String player = players[i].trim();
                if (!player.isEmpty() && isValidPlayerName(player)) {

                    mc.execute(() -> {
                        if (mc.player != null && mc.player.connection != null) {
                            mc.player.connection.sendCommand("freezinghistory " + player);
                            mc.gui.getChat().addMessage(Component.literal(
                                    "§7[ScS] F.История: " + player));
                        }
                    });

                    try {
                        Thread.sleep(1200); // 1.2 секунды
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }).start();
    }

    private boolean isValidPlayerName(String name) {
        return name != null && name.length() >= 3 && name.length() <= 16 &&
                name.matches("[a-zA-Z0-9_]+");
    }

    private void logCommand(String command, String target, String fullMessage) {
        Scs.LOGGER.info("[ScS] Command: {} -> {} ({})", command, target, fullMessage);
    }
}
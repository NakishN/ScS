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

                // Если автокоманды выключены, предупреждаем пользователя
                if (!Config.autoCommands) {
                    Minecraft.getInstance().gui.getChat().addMessage(
                            Component.literal("§c[ScS] Автокоманды отключены в конфиге!")
                    );
                }
            }
        }
    }

    private void logCommand(String command, String target, String fullMessage) {
        String logEntry = String.format("Command executed: %s -> %s (full: %s)",
                command.toUpperCase(), target, fullMessage);
        Scs.LOGGER.info(logEntry);

        // Добавляем запись в наш лог чата
        ChatTap.Entry commandEntry = new ChatTap.Entry("COMMAND",
                String.format("Команда: /%s %s", command, target), target);
        // Не добавляем в основной поток, но логируем отдельно
    }
}
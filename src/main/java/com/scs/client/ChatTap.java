package com.scs.client;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import com.scs.Config;
import com.scs.Scs;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;

public final class ChatTap {

    private static final int MAX = 100;
    public static final Deque<Entry> ENTRIES = new ConcurrentLinkedDeque<>();
    public static final Deque<ViolationEntry> VIOLATIONS = new ConcurrentLinkedDeque<>();

    private static final Path LOG_FILE = Paths.get("logs", "scs-chat.log");
    private static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // Основные паттерны для проверок
    private static final Pattern CHECK_START = Pattern.compile(".*Проверка успешно начата.*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern CHECK_WHO = Pattern.compile(".*Проверяемый игрок:\\s*(\\S+).*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern CHECK_MODE = Pattern.compile(".*Вы находитесь на режиме:\\s*(.+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    // Улучшенный паттерн для античита с извлечением всех данных
    private static final Pattern ANTICHEAT = Pattern.compile(
            ".*\\[Анти-Чит\\]\\s+(\\S+)\\s+(.*?)(?:\\s*\\(([^)]+)\\))?(?:\\s*#(\\d+))?.*",
            Pattern.UNICODE_CASE
    );

    public static final class Entry {
        public final Instant timestamp;
        public final String kind;
        public final String text;
        public final String playerName;

        public Entry(String kind, String text, String playerName) {
            this.timestamp = Instant.now();
            this.kind = kind;
            this.text = text;
            this.playerName = playerName;
        }

        public Entry(String kind, String text) {
            this(kind, text, null);
        }
    }

    public static final class ViolationEntry {
        public final Instant timestamp;
        public final String kind;
        public final String text;
        public final String playerName;
        public final String violation;
        public final String detectionType;
        public final int count;
        public final boolean isSerious;

        public ViolationEntry(String playerName, String violation, String detectionType, int count) {
            this.timestamp = Instant.now();
            this.kind = "VIOLATION";
            this.text = formatViolation(playerName, violation, detectionType, count);
            this.playerName = playerName;
            this.violation = violation;
            this.detectionType = detectionType;
            this.count = count;
            this.isSerious = isSeriousViolation(violation);
        }

        private static String formatViolation(String playerName, String violation, String detectionType, int count) {
            String countStr = count > 0 ? " #" + count : "";
            String typeStr = detectionType != null ? " (" + detectionType + ")" : "";
            return playerName + " → " + violation + typeStr + countStr;
        }

        private static boolean isSeriousViolation(String violation) {
            return violation.toLowerCase().contains("combat hacks") ||
                    violation.toLowerCase().contains("killaura") ||
                    violation.toLowerCase().contains("speed") ||
                    violation.toLowerCase().contains("fly") ||
                    violation.toLowerCase().contains("autobot");
        }
    }

    private void push(Entry entry) {
        if (entry.text == null || entry.text.isBlank()) return;

        ENTRIES.addFirst(entry);
        while (ENTRIES.size() > MAX) {
            ENTRIES.removeLast();
        }

        // Логируем в файл
        if (Config.enableLogging) {
            logToFile(entry);
        }
    }

    private void pushViolation(ViolationEntry violation) {
        VIOLATIONS.addFirst(violation);
        while (VIOLATIONS.size() > 50) {
            VIOLATIONS.removeLast();
        }

        // Звуковое уведомление
        if (Config.soundAlerts && violation.isSerious) {
            playAlertSound();
        }

        Scs.LOGGER.info("New violation detected: {} - {}", violation.playerName, violation.violation);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        Component comp = event.getMessage();
        String raw = comp == null ? "" : comp.getString();

        if (raw.isEmpty()) return;

        // Логируем весь чат если включено
        if (Config.logAllChat) {
            logRawMessage(raw);
        }

        Matcher m;

        // Проверяем стандартные сообщения проверки
        if (CHECK_START.matcher(raw).matches()) {
            push(new Entry("CHECK", "Проверка начата"));
            return;
        }

        if ((m = CHECK_WHO.matcher(raw)).matches()) {
            push(new Entry("CHECK", "Проверяемый: " + m.group(1), m.group(1)));
            return;
        }

        if ((m = CHECK_MODE.matcher(raw)).matches()) {
            push(new Entry("CHECK", "Режим: " + m.group(1)));
            return;
        }

        // Обрабатываем сообщения античита
        if ((m = ANTICHEAT.matcher(raw)).matches()) {
            String playerName = m.group(1);
            String violation = m.group(2) != null ? m.group(2).trim() : "Unknown violation";
            String detectionType = m.group(3);
            int count = 0;

            if (m.group(4) != null) {
                try {
                    count = Integer.parseInt(m.group(4));
                } catch (NumberFormatException e) {
                    // Игнорируем ошибки парсинга
                }
            }

            ViolationEntry violationEntry = new ViolationEntry(playerName, violation, detectionType, count);
            pushViolation(violationEntry);
            push(new Entry(violationEntry.kind, violationEntry.text, violationEntry.playerName));

            // Добавляем интерактивные кнопки в чат
            if (Config.enableChatButtons) {
                addInteractiveButtons(event, playerName, violation);
            }
        }
    }

    private void addInteractiveButtons(ClientChatReceivedEvent event, String playerName, String violation) {
        try {
            // Создаем новое сообщение с кнопками
            MutableComponent originalMessage = event.getMessage().copy();

            // Кнопка "Проверить"
            MutableComponent checkButton = Component.literal(" [Проверить]")
                    .setStyle(Style.EMPTY
                            .withColor(ChatFormatting.GREEN)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/freezing " + playerName))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("Проверить игрока " + playerName)))
                    );

            // Кнопка "Спек"
            MutableComponent specButton = Component.literal(" [Спек]")
                    .setStyle(Style.EMPTY
                            .withColor(ChatFormatting.YELLOW)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/matrix spectate " + playerName))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("Наблюдать за " + playerName + " (повторно для выхода)")))
                    );

            // Объединяем все компоненты
            MutableComponent enhancedMessage = originalMessage
                    .append(checkButton)
                    .append(specButton);

            // Заменяем оригинальное сообщение
            event.setMessage(enhancedMessage);

        } catch (Exception e) {
            Scs.LOGGER.error("Error adding interactive buttons", e);
        }
    }

    private boolean isSeriousViolation(String violation) {
        return violation.toLowerCase().contains("combat hacks") ||
                violation.toLowerCase().contains("killaura") ||
                violation.toLowerCase().contains("speed") ||
                violation.toLowerCase().contains("fly") ||
                violation.toLowerCase().contains("autobot");
    }

    private void playAlertSound() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
                mc.level.playLocalSound(
                        mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                        SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.MASTER,
                        0.5f, 1.0f, false
                );
            }
        } catch (Exception e) {
            Scs.LOGGER.warn("Failed to play alert sound: {}", e.getMessage());
        }
    }

    private void logToFile(Entry entry) {
        try {
            if (!Files.exists(LOG_FILE.getParent())) {
                Files.createDirectories(LOG_FILE.getParent());
            }

            String timestamp = LocalDateTime.now().format(LOG_FORMATTER);
            String logEntry = String.format("[%s] [%s] %s%n", timestamp, entry.kind, entry.text);

            Files.write(LOG_FILE, logEntry.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (IOException e) {
            Scs.LOGGER.error("Failed to write to log file", e);
        }
    }

    private void logRawMessage(String message) {
        try {
            if (!Files.exists(LOG_FILE.getParent())) {
                Files.createDirectories(LOG_FILE.getParent());
            }

            String timestamp = LocalDateTime.now().format(LOG_FORMATTER);
            String logEntry = String.format("[%s] [RAW] %s%n", timestamp, message);

            Files.write(LOG_FILE, logEntry.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (IOException e) {
            Scs.LOGGER.error("Failed to write raw message to log file", e);
        }
    }

    // Утилиты для внешнего доступа
    public static void clearEntries() {
        ENTRIES.clear();
        VIOLATIONS.clear();
        Scs.LOGGER.info("Chat entries cleared");
    }

    public static int getViolationCount(String playerName) {
        return (int) VIOLATIONS.stream()
                .filter(v -> v.playerName != null && v.playerName.equals(playerName))
                .count();
    }
}
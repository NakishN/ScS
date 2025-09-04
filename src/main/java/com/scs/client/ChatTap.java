package com.scs.client;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.Gui;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import com.scs.Config;
import com.scs.Scs;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.Deque;
import java.util.regex.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public final class ChatTap {

    private static final int MAX = 100;
    public static final Deque<Entry> ENTRIES = new ConcurrentLinkedDeque<>();
    public static final Deque<ViolationEntry> VIOLATIONS = new ConcurrentLinkedDeque<>();

    private static final Path LOG_FILE = Paths.get("logs", "scs-chat.log");
    private static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // Кэш для предотвращения дубликатов
    private static final Set<String> processedMessages = new HashSet<>();
    private static int lastChatSize = 0;
    private static boolean structureAnalyzed = false;

    // Reflection кэш
    private static final List<Field> stringListFields = new ArrayList<>();
    private static final List<Field> componentListFields = new ArrayList<>();
    private static final List<Field> otherFields = new ArrayList<>();

    // Паттерны
    private static final Pattern[] CHECK_PATTERNS = {
            Pattern.compile(".*[►▶]\\s*проверка.*успешно.*начата.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*проверка.*успешно.*начата.*", Pattern.CASE_INSENSITIVE),
    };

    private static final Pattern[] PLAYER_PATTERNS = {
            Pattern.compile(".*проверяемый.*игрок.*[:\\s]+(\\w+).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*игрок.*[:\\s]+(\\w+).*", Pattern.CASE_INSENSITIVE),
    };

    private static final Pattern[] MODE_PATTERNS = {
            Pattern.compile(".*режим.*[:\\s]+(.+?)(?:\\s*$)", Pattern.CASE_INSENSITIVE),
    };

    private static final Pattern[] ANTICHEAT_PATTERNS = {
            Pattern.compile(".*\\[.*анти.*чит.*\\]\\s*(\\w+)\\s+(.+?)(?:\\s*\\((.+?)\\))?(?:\\s*#(\\d+))?", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*(\\w+)\\s+(tried to .+?)(?:\\s*\\((.+?)\\))?(?:\\s*#(\\d+))?", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*(\\w+)\\s+(might be .+?)(?:\\s*\\((.+?)\\))?(?:\\s*#(\\d+))?", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*(\\w+)\\s+(is using .+?)(?:\\s*\\((.+?)\\))?(?:\\s*#(\\d+))?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^.*\\]\\s*(\\w+)\\s+(.+?)(?:\\s*\\((.+?)\\))?(?:\\s*#(\\d+))?", Pattern.CASE_INSENSITIVE),
    };

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
            String v = violation.toLowerCase();
            return v.contains("combat") || v.contains("killaura") || v.contains("speed") ||
                    v.contains("fly") || v.contains("bot") || v.contains("velocity") ||
                    v.contains("hack") || v.contains("aura");
        }
    }

    // Fallback - пытаемся поймать через события
    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        try {
            Component message = event.getMessage();
            if (message != null) {
                String text = message.getString();
                if (text != null && !text.trim().isEmpty()) {
                    processMessage(text, "EVENT");
                }
            }
        } catch (Exception e) {
            Scs.LOGGER.error("[ScS-EVENT] Error: {}", e.getMessage());
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui == null) return;

            // Анализируем структуру один раз
            if (!structureAnalyzed) {
                analyzeStructure(mc.gui);
                structureAnalyzed = true;
            }

            // Проверяем чат разными способами
            checkChatViaReflection(mc.gui);
            checkGuiChat(mc.gui);

        } catch (Exception e) {
            Scs.LOGGER.error("[ScS-TICK] Error: {}", e.getMessage());
        }
    }

    private static void analyzeStructure(Gui gui) {
        try {
            Scs.LOGGER.info("[ScS-STRUCTURE] Analyzing chat structure...");

            // Анализируем Gui
            analyzeClass(gui, "GUI");

            // Анализируем ChatComponent если доступен
            if (gui.getChat() != null) {
                analyzeClass(gui.getChat(), "CHAT");
            }

        } catch (Exception e) {
            Scs.LOGGER.error("[ScS-STRUCTURE] Error analyzing: {}", e.getMessage());
        }
    }

    private static void analyzeClass(Object obj, String prefix) {
        try {
            Class<?> clazz = obj.getClass();
            Scs.LOGGER.info("[ScS-STRUCTURE-{}] Class: {}", prefix, clazz.getSimpleName());

            // Анализируем все поля
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                try {
                    Object value = field.get(obj);
                    String fieldInfo = String.format("Field: %s, Type: %s, Value type: %s",
                            field.getName(),
                            field.getType().getSimpleName(),
                            value != null ? value.getClass().getSimpleName() : "null");

                    Scs.LOGGER.info("[ScS-STRUCTURE-{}] {}", prefix, fieldInfo);

                    // Классифицируем поля по типам
                    if (List.class.isAssignableFrom(field.getType()) && value instanceof List) {
                        List<?> list = (List<?>) value;
                        if (!list.isEmpty()) {
                            Object firstItem = list.get(0);
                            String listInfo = String.format("List field: %s, size: %d, item type: %s",
                                    field.getName(), list.size(), firstItem.getClass().getSimpleName());
                            Scs.LOGGER.info("[ScS-STRUCTURE-{}] {}", prefix, listInfo);

                            if (firstItem instanceof String) {
                                stringListFields.add(field);
                            } else if (firstItem instanceof Component) {
                                componentListFields.add(field);
                            } else {
                                otherFields.add(field);
                            }
                        }
                    }
                } catch (Exception e) {
                    Scs.LOGGER.warn("[ScS-STRUCTURE-{}] Error accessing field {}: {}", prefix, field.getName(), e.getMessage());
                }
            }

            // Анализируем методы
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().toLowerCase().contains("message") ||
                        method.getName().toLowerCase().contains("chat")) {

                    String methodInfo = String.format("Method: %s, params: %s, return: %s",
                            method.getName(),
                            java.util.Arrays.toString(method.getParameterTypes()),
                            method.getReturnType().getSimpleName());

                    Scs.LOGGER.info("[ScS-STRUCTURE-{}] {}", prefix, methodInfo);
                }
            }

        } catch (Exception e) {
            Scs.LOGGER.error("[ScS-STRUCTURE-{}] Error analyzing class: {}", prefix, e.getMessage());
        }
    }

    private static void checkChatViaReflection(Gui gui) {
        try {
            // Проверяем String поля (самый вероятный случай для 1.21.3)
            for (Field field : stringListFields) {
                checkStringListField(gui.getChat(), field);
            }

            // Проверяем Component поля
            for (Field field : componentListFields) {
                checkComponentListField(gui.getChat(), field);
            }

            // Проверяем другие поля
            for (Field field : otherFields) {
                checkOtherField(gui.getChat(), field);
            }

        } catch (Exception e) {
            Scs.LOGGER.error("[ScS-REFLECTION] Error checking chat: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void checkStringListField(Object chatComponent, Field field) {
        try {
            List<String> messages = (List<String>) field.get(chatComponent);
            if (messages == null || messages.isEmpty()) return;

            for (String message : messages) {
                if (message != null && !processedMessages.contains(message)) {
                    processedMessages.add(message);
                    processMessage(message, "STRING-REFLECTION");

                    if (processedMessages.size() > 200) {
                        processedMessages.clear(); // Очищаем кэш
                    }
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки cast
        }
    }

    @SuppressWarnings("unchecked")
    private static void checkComponentListField(Object chatComponent, Field field) {
        try {
            List<Component> messages = (List<Component>) field.get(chatComponent);
            if (messages == null || messages.isEmpty()) return;

            for (Component message : messages) {
                if (message != null) {
                    String text = message.getString();
                    if (text != null && !processedMessages.contains(text)) {
                        processedMessages.add(text);
                        processMessage(text, "COMPONENT-REFLECTION");
                    }
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки cast
        }
    }

    private static void checkOtherField(Object chatComponent, Field field) {
        try {
            Object value = field.get(chatComponent);
            if (value instanceof List) {
                List<?> list = (List<?>) value;
                for (Object item : list) {
                    if (item != null) {
                        String text = item.toString();
                        if (text.length() > 10 && !processedMessages.contains(text)) {
                            processedMessages.add(text);
                            processMessage(text, "OTHER-REFLECTION");
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
    }

    private static void checkGuiChat(Gui gui) {
        try {
            // Простая проверка - получаем текущий размер чата
            ChatComponent chat = gui.getChat();
            if (chat == null) return;

            // Используем toString() для получения содержимого
            String chatContent = chat.toString();
            if (chatContent != null && chatContent.length() > lastChatSize) {
                lastChatSize = chatContent.length();

                // Ищем новый контент
                String[] lines = chatContent.split("\\n");
                for (String line : lines) {
                    if (line.trim().length() > 10 && !processedMessages.contains(line)) {
                        processedMessages.add(line);
                        processMessage(line, "TOSTRING");
                    }
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
    }

    private static void processMessage(String text, String source) {
        if (text == null || text.trim().isEmpty()) return;

        // Очищаем от форматирования
        String cleanText = stripFormatting(text);

        // Детальное логирование
        Scs.LOGGER.info("[ScS-{}] Processing: '{}'", source, text);
        if (!text.equals(cleanText)) {
            Scs.LOGGER.info("[ScS-{}] Cleaned: '{}'", source, cleanText);
        }

        // Логируем в файл
        if (Config.logAllChat) {
            logMessage(source, text);
            if (!text.equals(cleanText)) {
                logMessage(source + "-CLEAN", cleanText);
            }
        }

        // Проверяем оба варианта
        checkPatterns(text, source);
        if (!text.equals(cleanText)) {
            checkPatterns(cleanText, source);
        }
    }

    private static void checkPatterns(String text, String source) {
        // Проверки
        for (Pattern pattern : CHECK_PATTERNS) {
            if (pattern.matcher(text).matches()) {
                addEntry(new Entry("CHECK", "Проверка начата"));
                Scs.LOGGER.info("[ScS-{}] Check started detected", source);
                return;
            }
        }

        // Игроки
        for (Pattern pattern : PLAYER_PATTERNS) {
            Matcher m = pattern.matcher(text);
            if (m.find() && m.groupCount() >= 1) {
                String player = m.group(1);
                if (isValidPlayerName(player)) {
                    addEntry(new Entry("CHECK", "Проверяемый: " + player, player));
                    Scs.LOGGER.info("[ScS-{}] Player detected: {}", source, player);
                    return;
                }
            }
        }

        // Режимы
        for (Pattern pattern : MODE_PATTERNS) {
            Matcher m = pattern.matcher(text);
            if (m.find() && m.groupCount() >= 1) {
                String mode = m.group(1).trim();
                if (!mode.isEmpty()) {
                    addEntry(new Entry("CHECK", "Режим: " + mode));
                    Scs.LOGGER.info("[ScS-{}] Mode detected: {}", source, mode);
                    return;
                }
            }
        }

        // Античит
        for (int i = 0; i < ANTICHEAT_PATTERNS.length; i++) {
            Pattern pattern = ANTICHEAT_PATTERNS[i];
            Matcher m = pattern.matcher(text);
            if (m.find()) {
                String player = null;
                String violation = null;
                String type = null;
                int count = 0;

                if (m.groupCount() >= 1) player = m.group(1);
                if (m.groupCount() >= 2) violation = m.group(2);
                if (m.groupCount() >= 3) type = m.group(3);
                if (m.groupCount() >= 4) {
                    try {
                        String countStr = m.group(4);
                        if (countStr != null) count = Integer.parseInt(countStr);
                    } catch (Exception ignored) {}
                }

                if (player != null && violation != null && isValidPlayerName(player.trim())) {
                    ViolationEntry entry = new ViolationEntry(player.trim(), violation.trim(), type, count);
                    VIOLATIONS.addFirst(entry);
                    while (VIOLATIONS.size() > 50) VIOLATIONS.removeLast();

                    addEntry(new Entry("VIOLATION", entry.text, entry.playerName));
                    Scs.LOGGER.info("[ScS-{}] Violation detected: {} - {}", source, player, violation);

                    // Звук и кнопки
                    if (Config.soundAlerts && entry.isSerious) playSound();
                    if (Config.enableChatButtons) addButtonsToChat(player.trim());
                    return;
                }
            }
        }

        // Fallback поиск
        performFallbackSearch(text, source);
    }

    private static void performFallbackSearch(String text, String source) {
        String lowerText = text.toLowerCase();
        if (!((lowerText.contains("анти") && lowerText.contains("чит")) ||
                lowerText.contains("tried to") || lowerText.contains("might be") ||
                lowerText.contains("is using"))) {
            return;
        }

        String[] words = text.split("\\s+");
        for (String word : words) {
            String cleanWord = word.replaceAll("[^a-zA-Z0-9_]", "");
            if (isValidPlayerName(cleanWord) && !isSystemWord(cleanWord)) {
                String rest = text.substring(text.indexOf(word) + word.length()).trim();
                if (!rest.isEmpty()) {
                    ViolationEntry entry = new ViolationEntry(cleanWord, rest, null, 0);
                    VIOLATIONS.addFirst(entry);
                    while (VIOLATIONS.size() > 50) VIOLATIONS.removeLast();

                    addEntry(new Entry("VIOLATION", entry.text, entry.playerName));
                    Scs.LOGGER.info("[ScS-{}] Fallback detection: {} - {}", source, cleanWord, rest);
                    return;
                }
            }
        }
    }

    private static boolean isValidPlayerName(String name) {
        return name != null && name.length() >= 3 && name.length() <= 16 &&
                name.matches("[a-zA-Z0-9_]+");
    }

    private static boolean isSystemWord(String word) {
        String lowerWord = word.toLowerCase();
        return lowerWord.matches(".*(анти|чит|anti|cheat|tried|might|using|system).*");
    }

    private static String stripFormatting(String text) {
        if (text == null) return "";
        String result = text.replaceAll("§[0-9a-fk-or]", "");
        result = result.replaceAll("[\u00A7\u001B]\\[[0-9;]*[a-zA-Z]", "");
        result = result.replaceAll("\\s+", " ").trim();
        return result;
    }

    private static void addEntry(Entry entry) {
        ENTRIES.addFirst(entry);
        while (ENTRIES.size() > MAX) {
            ENTRIES.removeLast();
        }

        if (Config.enableLogging) {
            logMessage(entry.kind, entry.text);
        }
    }

    private static void addButtonsToChat(String player) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui == null || mc.gui.getChat() == null) return;

            MutableComponent checkBtn = Component.literal(" [Проверить]")
                    .setStyle(Style.EMPTY
                            .withColor(ChatFormatting.GREEN)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/freezing " + player))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("Проверить " + player)))
                    );

            MutableComponent specBtn = Component.literal(" [Спек]")
                    .setStyle(Style.EMPTY
                            .withColor(ChatFormatting.YELLOW)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/matrix spectate " + player))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("Наблюдать за " + player)))
                    );

            Component message = Component.literal("").append(checkBtn).append(specBtn);
            mc.gui.getChat().addMessage(message);

        } catch (Exception e) {
            Scs.LOGGER.error("[ScS-BUTTONS] Error: {}", e.getMessage());
        }
    }

    private static void playSound() {
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
            Scs.LOGGER.warn("[ScS-SOUND] Error: {}", e.getMessage());
        }
    }

    private static void logMessage(String type, String message) {
        try {
            if (!Files.exists(LOG_FILE.getParent())) {
                Files.createDirectories(LOG_FILE.getParent());
            }

            String timestamp = LocalDateTime.now().format(LOG_FORMATTER);
            String logEntry = String.format("[%s] [%s] %s%n", timestamp, type, message);

            Files.write(LOG_FILE, logEntry.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            Scs.LOGGER.error("[ScS-LOG] Error: {}", e.getMessage());
        }
    }

    public static void clearEntries() {
        ENTRIES.clear();
        VIOLATIONS.clear();
        processedMessages.clear();
        Scs.LOGGER.info("[ScS] Entries cleared");
    }

    public static int getViolationCount(String playerName) {
        return (int) VIOLATIONS.stream()
                .filter(v -> v.playerName != null && v.playerName.equals(playerName))
                .count();
    }
}
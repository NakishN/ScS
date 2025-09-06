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
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;
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
    public static final Deque<PlayerChatEntry> PLAYER_CHAT = new ConcurrentLinkedDeque<>();
    public static final Deque<DupeIPEntry> DUPEIP_SCAN_RESULTS = new ConcurrentLinkedDeque<>();

    private static final Path LOG_FILE = Paths.get("logs", "scs-chat.log");
    private static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static final Set<String> processedMessages = new HashSet<>();
    private static ChatComponent hookedChatComponent = null;
    private static Method originalAddMessageMethod = null;
    private static boolean hookInstalled = false;
    private static int tickCounter = 0;

    // Временное хранение для отслеживания последнего сканируемого игрока DupeIP
    private static String lastScannedPlayer = null;
    private static long lastDupeIPScanTime = 0;

    // Улучшенные паттерны
    private static final Pattern[] ANTICHEAT_PATTERNS = {
            Pattern.compile(".*\\[.*анти.*чит.*\\]\\s*(\\w+)\\s+(.+?)(?:\\s*\\((.+?)\\))?(?:\\s*#(\\d+))?", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*(\\w+)\\s+(tried to .+?)(?:\\s*\\((.+?)\\))?(?:\\s*#(\\d+))?", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*(\\w+)\\s+(suspected .+?)(?:\\s*\\((.+?)\\))?(?:\\s*#(\\d+))?", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*(\\w+)\\s+tried to move abnormally.*(?:\\((.+?)\\))?(?:\\s*#(\\d+))?", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*(\\w+)\\s+suspected using.*(?:\\((.+?)\\))?(?:\\s*#(\\d+))?", Pattern.CASE_INSENSITIVE),
    };

    // ИСПРАВЛЕННЫЕ паттерны для проверок (учитывают пробелы в начале)
    private static final Pattern[] CHECK_PATTERNS = {
            Pattern.compile(".*[►▶]\\s*проверка.*успешно.*начата.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*проверка.*успешно.*начата.*", Pattern.CASE_INSENSITIVE),
    };

    private static final Pattern[] PLAYER_PATTERNS = {
            Pattern.compile(".*проверяемый\\s+игрок\\s*[:：]\\s*(\\w+).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*игрок\\s*[:：]\\s*(\\w+).*", Pattern.CASE_INSENSITIVE),
    };

    private static final Pattern[] MODE_PATTERNS = {
            Pattern.compile(".*вы\\s+находитесь\\s+на\\s+режиме\\s*[:：]\\s*(.+?)\\s*$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*режим\\s*[:：]\\s*(.+?)\\s*$", Pattern.CASE_INSENSITIVE),
    };

    // Паттерны для чата игроков
    private static final Pattern[] PLAYER_CHAT_PATTERNS = {
            Pattern.compile(".*[«»]\\s*([a-zA-Z0-9_]+)\\s*[»]\\s*(.+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*ᴄʜᴇᴀᴛᴇʀ.*[»]\\s*(\\w+)\\s*[»]\\s*(.+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*(\\w+)\\s*[»]\\s*(.+)", Pattern.CASE_INSENSITIVE),
    };

    // УЛУЧШЕННЫЕ ПАТТЕРНЫ ДЛЯ DUPEIP
    private static final Pattern DUPEIP_SCAN_PATTERN = Pattern.compile(
            ".*Сканирование\\s+(\\w+).*",
            Pattern.UNICODE_CASE | Pattern.DOTALL
    );

    // Паттерн для результатов дубликатов - строка с никнеймами через запятую
    private static final Pattern DUPEIP_RESULTS_PATTERN = Pattern.compile(
            "^([A-Za-z0-9_]+(?:,\\s*[A-Za-z0-9_]+)+)$"
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
            String v = violation.toLowerCase();
            return v.contains("combat") || v.contains("killaura") || v.contains("speed") ||
                    v.contains("fly") || v.contains("bot") || v.contains("velocity") ||
                    v.contains("hack") || v.contains("aura") || v.contains("vehicle");
        }
    }

    public static final class PlayerChatEntry {
        public final Instant timestamp;
        public final String kind;
        public final String text;
        public final String playerName;
        public final String message;

        public PlayerChatEntry(String playerName, String message) {
            this.timestamp = Instant.now();
            this.kind = "CHAT";
            this.text = playerName + ": " + message;
            this.playerName = playerName;
            this.message = message;
        }
    }

    // КЛАСС ДЛЯ DUPEIP
    public static final class DupeIPEntry {
        public final Instant timestamp;
        public final String scannedPlayer;
        public final List<String> duplicateAccounts;
        public final int totalDupes;

        public DupeIPEntry(String scannedPlayer, List<String> duplicateAccounts) {
            this.timestamp = Instant.now();
            this.scannedPlayer = scannedPlayer;
            this.duplicateAccounts = new ArrayList<>(duplicateAccounts);
            this.totalDupes = duplicateAccounts.size();
        }

        public String getFormattedText() {
            return String.format("DupeIP %s: %d дубл (%s)",
                    scannedPlayer, totalDupes, String.join(", ", duplicateAccounts));
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;

        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui == null || mc.gui.getChat() == null) return;

            ChatComponent chatComponent = mc.gui.getChat();

            if (!hookInstalled) {
                installChatHook(chatComponent);
                hookInstalled = true;
            }

            // Сканируем чат каждые 2 тика
            if (tickCounter % 2 == 0) {
                scanChatViaReflection(chatComponent);
            }

        } catch (Exception e) {
            Scs.LOGGER.error("[ScS] Tick error: {}", e.getMessage());
        }
    }

    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        try {
            if (event.getMessage() != null) {
                String messageText = event.getMessage().getString();
                processMessage(messageText, "EVENT");

                // ПРЯМАЯ ПРОВЕРКА НА DUPEIP В EVENT
                checkForDupeIPDirect(messageText);
            }
        } catch (Exception ignored) {}
    }

    // СУПЕР УСИЛЕННАЯ ПРОВЕРКА DUPEIP - ВСЕ ВАРИАНТЫ
    private static void checkForDupeIPDirect(String text) {
        if (text == null || text.trim().isEmpty()) return;

        Scs.LOGGER.info("[ScS] *** ПРЯМАЯ ПРОВЕРКА DUPEIP *** Текст: '{}'", text);

        // ВАРИАНТ 1: Проверка на сканирование - ЛЮБОЕ УПОМИНАНИЕ
        if (text.toLowerCase().contains("сканирование")) {
            Scs.LOGGER.info("[ScS] *** НАЙДЕНО СКАНИРОВАНИЕ *** Ищем игрока...");

            String[] words = text.split("\\s+");
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                if (word.toLowerCase().contains("сканирование") && i + 1 < words.length) {
                    String nextWord = words[i + 1];
                    String playerName = nextWord.replaceAll("[^a-zA-Z0-9_]", "");

                    if (isValidPlayerName(playerName)) {
                        lastScannedPlayer = playerName;
                        lastDupeIPScanTime = System.currentTimeMillis();
                        addEntry(new Entry("DUPEIP_SCAN", "DupeIP скан: " + playerName, playerName));
                        Scs.LOGGER.info("[ScS] ✓✓✓ DUPEIP СКАН НАЙДЕН: {}", playerName);
                        return;
                    }
                }
            }
        }

        // ВАРИАНТ 2: Проверка на результаты - ЕСЛИ ЕСТЬ ЗАПЯТЫЕ И НИКНЕЙМЫ
        if (lastScannedPlayer != null &&
                System.currentTimeMillis() - lastDupeIPScanTime < 15000) { // 15 секунд

            Scs.LOGGER.info("[ScS] *** ИЩЕМ РЕЗУЛЬТАТЫ *** Последний скан: {} ({}сек назад)",
                    lastScannedPlayer, (System.currentTimeMillis() - lastDupeIPScanTime) / 1000);

            // Если в тексте есть запятые и это похоже на список игроков
            if (text.contains(",")) {
                String[] parts = text.split(",");
                List<String> nicknames = new ArrayList<>();

                for (String part : parts) {
                    String cleanNick = part.trim().replaceAll("[^a-zA-Z0-9_]", "");
                    if (isValidPlayerName(cleanNick)) {
                        nicknames.add(cleanNick);
                        Scs.LOGGER.info("[ScS] *** НАЙДЕН НИКНЕЙМ: '{}'", cleanNick);
                    }
                }

                if (nicknames.size() >= 2) {
                    DupeIPEntry dupeEntry = new DupeIPEntry(lastScannedPlayer, nicknames);
                    DUPEIP_SCAN_RESULTS.addFirst(dupeEntry);
                    while (DUPEIP_SCAN_RESULTS.size() > 20) {
                        DUPEIP_SCAN_RESULTS.removeLast();
                    }

                    addEntry(new Entry("DUPEIP_RESULT", dupeEntry.getFormattedText(), lastScannedPlayer));

                    Scs.LOGGER.info("[ScS] ✓✓✓ DUPEIP РЕЗУЛЬТАТЫ: {} -> {} аккаунтов", lastScannedPlayer, nicknames.size());

                    // ДОБАВЛЯЕМ КНОПКИ СРАЗУ ЖЕ БЛЯТЬ!
                    if (Config.enableChatButtons) {
                        Scs.LOGGER.info("[ScS] *** ДОБАВЛЯЕМ КНОПКИ ПРЯМО СЕЙЧАС ***");
                        addDupeIPButtonsToChat(lastScannedPlayer, nicknames);
                    } else {
                        Scs.LOGGER.warn("[ScS] *** КНОПКИ ОТКЛЮЧЕНЫ В КОНФИГЕ! ***");
                    }

                    lastScannedPlayer = null; // Сбрасываем
                    return;
                }
            }
        }

        // ВАРИАНТ 3: ОТДЕЛЬНЫЕ НИКНЕЙМЫ В РАЗНЫХ СТРОКАХ (если плагин шлет по одному)
        if (lastScannedPlayer != null &&
                System.currentTimeMillis() - lastDupeIPScanTime < 10000 &&
                !text.contains(",") &&
                isValidPlayerName(text.trim())) {

            // Возможно это отдельный результат
            Scs.LOGGER.info("[ScS] *** ВОЗМОЖНО ОТДЕЛЬНЫЙ РЕЗУЛЬТАТ: '{}' для скана '{}'", text.trim(), lastScannedPlayer);
        }
    }

    private static void installChatHook(ChatComponent chatComponent) {
        try {
            Scs.LOGGER.info("[ScS] Installing chat hook...");
            hookedChatComponent = chatComponent;

            Method[] methods = ChatComponent.class.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().contains("addMessage") || method.getName().contains("add")) {
                    method.setAccessible(true);
                    originalAddMessageMethod = method;
                    Scs.LOGGER.info("[ScS] Found method: {}", method.getName());
                }
            }
        } catch (Exception e) {
            Scs.LOGGER.error("[ScS] Failed to install hook: {}", e.getMessage());
        }
    }

    private static void createChatProxy(ChatComponent chatComponent) {
        try {
            // Заглушка для совместимости с оригинальным кодом
        } catch (Exception e) {
            Scs.LOGGER.error("[ScS] Failed to create proxy: {}", e.getMessage());
        }
    }

    private static void scanChatViaReflection(ChatComponent chatComponent) {
        try {
            Class<?> chatClass = chatComponent.getClass();

            for (Field field : chatClass.getDeclaredFields()) {
                field.setAccessible(true);

                try {
                    Object value = field.get(chatComponent);
                    if (value instanceof List) {
                        List<?> list = (List<?>) value;
                        if (!list.isEmpty()) {
                            scanListField(list, field.getName());
                        }
                    }
                } catch (Exception e) {
                    // Продолжаем сканирование
                }
            }

        } catch (Exception e) {
            Scs.LOGGER.error("[ScS] Scan error: {}", e.getMessage());
        }
    }

    // УЛУЧШЕННОЕ СКАНИРОВАНИЕ СПИСКОВ
    private static void scanListField(List<?> list, String fieldName) {
        try {
            for (Object item : list) {
                if (item == null) continue;

                String text = extractTextFromObject(item);

                if (text != null && text.length() > 5 && !processedMessages.contains(text)) {
                    processedMessages.add(text);

                    // СПЕЦИАЛЬНАЯ ОТЛАДКА ДЛЯ DUPEIP
                    if (text.contains("Сканирование") || text.contains("nakish")) {
                        Scs.LOGGER.info("[ScS] *** DUPEIP FOUND *** Field: {}, Raw: {}", fieldName, item);
                        Scs.LOGGER.info("[ScS] *** DUPEIP EXTRACTED *** Text: '{}'", text);
                    }

                    // ЧИСТЫЕ ЛОГИ - только важное + DupeIP
                    if (containsImportantKeywords(text)) {
                        String shortText = text.length() > 100 ? text.substring(0, 100) + "..." : text;
                        Scs.LOGGER.info("[ScS-{}] Found: {}", fieldName, shortText);
                        processMessage(text, "SCAN-" + fieldName.toUpperCase());
                    }

                    if (processedMessages.size() > 300) {
                        processedMessages.clear();
                    }
                }
            }
        } catch (Exception e) {
            Scs.LOGGER.error("[ScS] Error scanning list field {}: {}", fieldName, e.getMessage());
        }
    }

    // НОВЫЙ метод для проверки важных ключевых слов
    private static boolean containsImportantKeywords(String text) {
        String lowerText = text.toLowerCase();
        return lowerText.contains("сканирование") ||
                lowerText.contains("анти") && lowerText.contains("чит") ||
                lowerText.contains("tried to") ||
                lowerText.contains("suspected") ||
                lowerText.contains("проверка") ||
                lowerText.contains("игрок") ||
                lowerText.contains("режим") ||
                lowerText.contains("ᴄʜᴇᴀᴛᴇʀ");
    }

    // ИСПРАВЛЕННЫЙ метод extractTextFromObject
    private static String extractTextFromObject(Object item) {
        try {
            if (item instanceof String) {
                return (String) item;
            } else if (item instanceof Component) {
                Component comp = (Component) item;
                return extractFullTextFromComponent(comp);
            } else {
                String itemStr = item.toString();

                // Проверяем наличие ключевых слов ПЕРЕД извлечением текста
                if (containsImportantKeywords(itemStr)) {
                    // Если это GuiMessage, извлекаем текст из структуры
                    if (itemStr.startsWith("GuiMessage[") && itemStr.contains("content=")) {
                        return parseGuiMessageContent(itemStr);
                    }
                    return itemStr;
                }
            }
        } catch (Exception e) {
            Scs.LOGGER.error("[ScS] Error extracting text from object: {}", e.getMessage());
        }
        return null;
    }

    // НОВЫЙ метод для парсинга GuiMessage
    private static String parseGuiMessageContent(String guiMessageStr) {
        try {
            // Ищем content= часть
            int contentStart = guiMessageStr.indexOf("content=");
            if (contentStart == -1) return "";

            String contentPart = guiMessageStr.substring(contentStart);

            // Извлекаем все literal{} блоки из content части
            StringBuilder result = new StringBuilder();
            Pattern literalPattern = Pattern.compile("literal\\{([^}]+)\\}");
            Matcher matcher = literalPattern.matcher(contentPart);

            while (matcher.find()) {
                String literalText = matcher.group(1);
                result.append(literalText);
            }

            String finalResult = result.toString().trim();
            Scs.LOGGER.info("[ScS] Parsed GuiMessage content: '{}'", finalResult);
            return finalResult;

        } catch (Exception e) {
            Scs.LOGGER.error("[ScS] Error parsing GuiMessage: {}", e.getMessage());
            return "";
        }
    }

    // ИСПРАВЛЕННЫЙ метод extractFullTextFromComponent
    private static String extractFullTextFromComponent(Component component) {
        try {
            StringBuilder fullText = new StringBuilder();

            // Сначала проверяем основной текст
            String mainText = component.getString();
            if (mainText != null && !mainText.trim().isEmpty() && !mainText.equals("empty")) {
                fullText.append(mainText);
            }

            // Получаем siblings - здесь основная проблема
            List<Component> siblings = component.getSiblings();
            if (siblings != null && !siblings.isEmpty()) {
                for (Component sibling : siblings) {
                    String siblingText = sibling.getString();
                    if (siblingText != null && !siblingText.trim().isEmpty()) {
                        fullText.append(siblingText);
                    }
                }
            }

            String result = fullText.toString().trim();

            // НОВОЕ: Если обычное извлечение не сработало, парсим toString()
            if (result.isEmpty() || result.equals("empty")) {
                result = parseFromToString(component.toString());
            }

            return result;

        } catch (Exception e) {
            Scs.LOGGER.error("[ScS] Error extracting text from component: {}", e.getMessage());
            return parseFromToString(component.toString());
        }
    }

    // НОВЫЙ метод для парсинга из toString()
    private static String parseFromToString(String componentString) {
        try {
            StringBuilder result = new StringBuilder();

            // Паттерн для извлечения literal{текст} блоков
            Pattern literalPattern = Pattern.compile("literal\\{([^}]+)\\}");
            Matcher matcher = literalPattern.matcher(componentString);

            while (matcher.find()) {
                String literalText = matcher.group(1);
                // Убираем экранирование если есть
                literalText = literalText.replace("\\{", "{").replace("\\}", "}");
                result.append(literalText);
            }

            return result.toString().trim();

        } catch (Exception e) {
            Scs.LOGGER.error("[ScS] Error parsing from toString: {}", e.getMessage());
            return "";
        }
    }

    private static void processMessage(String text, String source) {
        if (text == null || text.trim().isEmpty()) return;

        String cleanText = stripFormatting(text);

        // ТОЛЬКО ВАЖНЫЕ ЛОГИ + DupeIP
        if (containsImportantKeywords(text)) {
            String shortText = cleanText.length() > 100 ? cleanText.substring(0, 100) + "..." : cleanText;
            Scs.LOGGER.info("[ScS-{}] Processing: '{}'", source, shortText);
        }

        if (Config.logAllChat) {
            logMessage(source, cleanText);
        }

        checkMessage(text, source);
        if (!text.equals(cleanText)) {
            checkMessage(cleanText, source);
        }
    }

    // УЛУЧШЕННЫЙ метод checkMessage
    private static void checkMessage(String text, String source) {
        if (text == null || text.trim().isEmpty()) return;

        // ЛОГИРУЕМ ВСЕ ВАЖНЫЕ СООБЩЕНИЯ ДЛЯ ОТЛАДКИ
        if (text.contains("Сканирование") || text.contains("накиш") || text.contains("nakish")) {
            Scs.LOGGER.info("[ScS] *** DUPEIP DEBUG *** Source: {}, Text: '{}'", source, text);
        }

        Matcher m;

        // ПРОВЕРКА НА DUPEIP СКАНИРОВАНИЕ - УЛУЧШЕННАЯ
        if ((m = DUPEIP_SCAN_PATTERN.matcher(text)).matches()) {
            lastScannedPlayer = m.group(1);
            lastDupeIPScanTime = System.currentTimeMillis();
            addEntry(new Entry("DUPEIP_SCAN", "Сканирование DupeIP: " + lastScannedPlayer, lastScannedPlayer));
            Scs.LOGGER.info("[ScS] ✓ DUPEIP SCAN detected: {}", lastScannedPlayer);
            return;
        }

        // АЛЬТЕРНАТИВНЫЙ паттерн для DupeIP (более простой)
        if (text.contains("Сканирование") && text.contains("Онлайн") && text.contains("Оффлайн")) {
            String[] words = text.split("\\s+");
            for (int i = 0; i < words.length; i++) {
                if ("Сканирование".equals(words[i]) && i + 1 < words.length) {
                    String playerName = words[i + 1].replaceAll("[^a-zA-Z0-9_]", ""); // Убираем точки и прочее
                    if (isValidPlayerName(playerName)) {
                        lastScannedPlayer = playerName;
                        lastDupeIPScanTime = System.currentTimeMillis();
                        addEntry(new Entry("DUPEIP_SCAN", "Сканирование DupeIP: " + lastScannedPlayer, lastScannedPlayer));
                        Scs.LOGGER.info("[ScS] ✓ DUPEIP SCAN (alternative) detected: {}", lastScannedPlayer);
                        return;
                    }
                }
            }
        }

        // ПРОВЕРКА НА DUPEIP РЕЗУЛЬТАТЫ
        if (lastScannedPlayer != null &&
                System.currentTimeMillis() - lastDupeIPScanTime < 15000 && // 15 секунд
                (m = DUPEIP_RESULTS_PATTERN.matcher(text.trim())).matches()) {

            String nicknamesStr = m.group(1);
            List<String> nicknames = Arrays.stream(nicknamesStr.split(","))
                    .map(String::trim)
                    .filter(nick -> !nick.isEmpty())
                    .toList();

            if (!nicknames.isEmpty()) {
                DupeIPEntry dupeEntry = new DupeIPEntry(lastScannedPlayer, nicknames);
                DUPEIP_SCAN_RESULTS.addFirst(dupeEntry);
                while (DUPEIP_SCAN_RESULTS.size() > 20) {
                    DUPEIP_SCAN_RESULTS.removeLast();
                }

                addEntry(new Entry("DUPEIP_RESULT", dupeEntry.getFormattedText(), lastScannedPlayer));

                // ДОБАВЛЯЕМ КНОПКИ ДЛЯ DUPEIP
                if (Config.enableChatButtons) {
                    addDupeIPButtonsToChat(lastScannedPlayer, nicknames);
                }

                Scs.LOGGER.info("[ScS] ✓ DUPEIP RESULTS: {} -> {} accounts", lastScannedPlayer, nicknames.size());
            }
            lastScannedPlayer = null; // Сбрасываем после обработки
            return;
        }

        // Проверки на проверки
        for (Pattern pattern : CHECK_PATTERNS) {
            if (pattern.matcher(text).matches()) {
                addEntry(new Entry("CHECK", "Проверка начата"));
                Scs.LOGGER.info("[ScS] ✓ CHECK START detected");
                return;
            }
        }

        // Проверки на игроков
        for (Pattern pattern : PLAYER_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find() && matcher.groupCount() >= 1) {
                String player = matcher.group(1);
                if (isValidPlayerName(player)) {
                    addEntry(new Entry("CHECK", "Проверяемый: " + player, player));
                    Scs.LOGGER.info("[ScS] ✓ PLAYER detected: {}", player);
                    return;
                }
            }
        }

        // Проверки на режим
        for (Pattern pattern : MODE_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find() && matcher.groupCount() >= 1) {
                String mode = matcher.group(1).trim();
                if (!mode.isEmpty()) {
                    addEntry(new Entry("CHECK", "Режим: " + mode));
                    Scs.LOGGER.info("[ScS] ✓ MODE detected: {}", mode);
                    return;
                }
            }
        }

        // Проверки на чат игроков
        for (Pattern pattern : PLAYER_CHAT_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find() && matcher.groupCount() >= 2) {
                String player = matcher.group(1);
                String message = matcher.group(2);

                if (isValidPlayerName(player) && message != null && message.trim().length() > 0) {
                    PlayerChatEntry chatEntry = new PlayerChatEntry(player.trim(), message.trim());
                    PLAYER_CHAT.addFirst(chatEntry);
                    while (PLAYER_CHAT.size() > 50) PLAYER_CHAT.removeLast();

                    Scs.LOGGER.info("[ScS] ✓ CHAT: {} -> {}", player, message);

                    // Логируем в файл
                    if (Config.logAllChat) {
                        logMessage("CHAT", player + ": " + message);
                    }
                    return;
                }
            }
        }

        // АНТИЧИТ - ГЛАВНОЕ
        for (int i = 0; i < ANTICHEAT_PATTERNS.length; i++) {
            Pattern pattern = ANTICHEAT_PATTERNS[i];
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String player = null;
                String violation = null;
                String type = null;
                int count = 0;

                if (matcher.groupCount() >= 1) player = matcher.group(1);
                if (matcher.groupCount() >= 2) violation = matcher.group(2);
                if (matcher.groupCount() >= 3) type = matcher.group(3);
                if (matcher.groupCount() >= 4) {
                    try {
                        String countStr = matcher.group(4);
                        if (countStr != null) count = Integer.parseInt(countStr);
                    } catch (Exception ignored) {}
                }

                // УЛУЧШЕННАЯ ВАЛИДАЦИЯ
                if (isValidPlayerName(player) && violation != null && violation.trim().length() >= 3) {
                    processViolation(player.trim(), violation.trim(), type, count, source);
                    Scs.LOGGER.info("[ScS] *** VIOLATION *** Player: '{}', Violation: '{}'", player, violation);
                    return;
                }
            }
        }

        // Fallback
        if (containsAntiCheatKeywords(text)) {
            performFallbackAntiCheatSearch(text, source);
        }
    }

    private static void processViolation(String player, String violation, String type, int count, String source) {
        ViolationEntry entry = new ViolationEntry(player, violation, type, count);
        VIOLATIONS.addFirst(entry);
        while (VIOLATIONS.size() > 50) VIOLATIONS.removeLast();

        addEntry(new Entry("VIOLATION", entry.text, entry.playerName));

        // ЧИСТЫЙ ЛОГ О НАРУШЕНИИ
        Scs.LOGGER.info("[ScS] VIOLATION: {} - {} ({})", player, violation, entry.isSerious ? "СЕРЬЕЗНОЕ" : "обычное");

        if (Config.soundAlerts && entry.isSerious) {
            playSound();
        }

        if (Config.enableChatButtons) {
            addAntiCheatButtonsToChat(player); // Используем правильное имя игрока
        }
    }

    private static void performFallbackAntiCheatSearch(String text, String source) {
        // УЛУЧШЕННЫЙ FALLBACK - ищем имена игроков точнее
        String[] words = text.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            String cleanWord = word.replaceAll("[^a-zA-Z0-9_]", "");

            if (isValidPlayerName(cleanWord) && !isSystemWord(cleanWord)) {
                // Берем все слова после имени игрока
                StringBuilder violation = new StringBuilder();
                for (int j = i + 1; j < words.length; j++) {
                    violation.append(words[j]).append(" ");
                }

                String violationStr = violation.toString().trim();
                if (violationStr.length() > 5) {
                    processViolation(cleanWord, violationStr, null, 0, source + "-FALLBACK");
                    Scs.LOGGER.info("[ScS] FALLBACK: {} - {}", cleanWord, violationStr);
                    return;
                }
            }
        }
    }

    private static boolean containsAntiCheatKeywords(String text) {
        String lowerText = text.toLowerCase();
        return (lowerText.contains("анти") && lowerText.contains("чит")) ||
                lowerText.contains("tried to") || lowerText.contains("suspected") ||
                lowerText.contains("move abnormally") || lowerText.contains("vehicle cheat");
    }

    private static boolean isValidPlayerName(String name) {
        return name != null && name.length() >= 3 && name.length() <= 16 &&
                name.matches("[a-zA-Z0-9_]+");
    }

    private static boolean isSystemWord(String word) {
        String lowerWord = word.toLowerCase();
        return lowerWord.matches(".*(анти|чит|anti|cheat|tried|suspected|system|render|thread|literal|style|color).*");
    }

    private static String stripFormatting(String text) {
        if (text == null) return "";
        return text.replaceAll("§[0-9a-fk-or]", "")
                .replaceAll("[\u00A7\u001B]\\[[0-9;]*[a-zA-Z]", "")
                .replaceAll("\\s+", " ").trim();
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

    // КНОПКИ ДЛЯ DUPEIP
    private static void addDupeIPButtonsToChat(String scannedPlayer, List<String> duplicates) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui == null || mc.gui.getChat() == null) return;

            Scs.LOGGER.info("[ScS] Adding DupeIP buttons for player: '{}'", scannedPlayer);

            // Создаем компонент для копирования всех никнеймов
            String allNicknames = String.join(",", duplicates);
            MutableComponent message = Component.literal("DupeIP: " + scannedPlayer + " (" + duplicates.size() + " дубликатов) ")
                    .append(Component.literal("[Копировать все]")
                            .setStyle(Style.EMPTY
                                    .withColor(ChatFormatting.AQUA)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, allNicknames))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.literal("Копировать в буфер: " + allNicknames)))
                            ))
                    .append(Component.literal(" [История всех]")
                            .setStyle(Style.EMPTY
                                    .withColor(ChatFormatting.YELLOW)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/scs:history_all " + allNicknames))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.literal("Проверить историю всех игроков с антиспамом")))
                            ))
                    .append(Component.literal(" [F.История всех]")
                            .setStyle(Style.EMPTY
                                    .withColor(ChatFormatting.LIGHT_PURPLE)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/scs:freezing_history_all " + allNicknames))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.literal("Freezing история всех игроков")))
                            ));

            mc.gui.getChat().addMessage(message);

            // Добавляем индивидуальные кнопки для первых 3 игроков
            int count = 0;
            for (String player : duplicates) {
                if (count >= 3) break; // Ограничиваем количество

                MutableComponent playerMessage = Component.literal("→ " + player + " ")
                        .append(Component.literal("[Проверить]")
                                .setStyle(Style.EMPTY
                                        .withColor(ChatFormatting.GREEN)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/freezing " + player))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                Component.literal("Проверить " + player)))
                                ))
                        .append(Component.literal(" [История]")
                                .setStyle(Style.EMPTY
                                        .withColor(ChatFormatting.YELLOW)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/history " + player))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                Component.literal("История " + player)))
                                ))
                        .append(Component.literal(" [F.История]")
                                .setStyle(Style.EMPTY
                                        .withColor(ChatFormatting.LIGHT_PURPLE)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/freezinghistory " + player))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                Component.literal("Freezing история " + player)))
                                ))
                        .append(Component.literal(" [Активность]")
                                .setStyle(Style.EMPTY
                                        .withColor(ChatFormatting.AQUA)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/playeractivity " + player))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                Component.literal("Активность " + player)))
                                ));

                mc.gui.getChat().addMessage(playerMessage);
                count++;
            }

            Scs.LOGGER.info("[ScS] ✓ DupeIP buttons added for: {} ({} individual buttons)", scannedPlayer, count);

        } catch (Exception e) {
            Scs.LOGGER.error("[ScS] Error adding DupeIP buttons: {}", e.getMessage());
        }
    }

    // РАСШИРЕННЫЕ КНОПКИ ДЛЯ АНТИЧИТА
    private static void addAntiCheatButtonsToChat(String player) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui == null || mc.gui.getChat() == null) return;

            // ЧИСТЫЙ ЛОГ
            Scs.LOGGER.info("[ScS] Adding anticheat buttons for player: '{}'", player);

            MutableComponent message = Component.literal("Нарушение: " + player + " ")
                    .append(Component.literal("[Проверить]")
                            .setStyle(Style.EMPTY
                                    .withColor(ChatFormatting.GREEN)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/freezing " + player))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.literal("Проверить " + player)))
                            ))
                    .append(Component.literal(" [Спек]")
                            .setStyle(Style.EMPTY
                                    .withColor(ChatFormatting.YELLOW)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/matrix spectate " + player))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.literal("Наблюдать за " + player)))
                            ))
                    .append(Component.literal(" [Активность]")
                            .setStyle(Style.EMPTY
                                    .withColor(ChatFormatting.AQUA)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/playeractivity " + player))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.literal("Проверить активность игрока: " + player)))
                            ))
                    .append(Component.literal(" [История]")
                            .setStyle(Style.EMPTY
                                    .withColor(ChatFormatting.LIGHT_PURPLE)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/freezinghistory " + player))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.literal("История проверок: " + player)))
                            ));

            mc.gui.getChat().addMessage(message);
            Scs.LOGGER.info("[ScS] ✓ Anticheat buttons added for: {}", player);

        } catch (Exception e) {
            Scs.LOGGER.error("[ScS] Error adding anticheat buttons: {}", e.getMessage());
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
        } catch (Exception ignored) {}
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
        } catch (IOException ignored) {}
    }

    public static void clearEntries() {
        ENTRIES.clear();
        VIOLATIONS.clear();
        PLAYER_CHAT.clear();
        DUPEIP_SCAN_RESULTS.clear();
        processedMessages.clear();
        lastScannedPlayer = null;
        lastDupeIPScanTime = 0;
        Scs.LOGGER.info("[ScS] All entries cleared (including player chat and DupeIP)");
    }

    public static int getViolationCount(String playerName) {
        return (int) VIOLATIONS.stream()
                .filter(v -> v.playerName != null && v.playerName.equals(playerName))
                .count();
    }

    // API ДЛЯ DUPEIP
    public static List<DupeIPEntry> getDupeIPResults() {
        return new ArrayList<>(DUPEIP_SCAN_RESULTS);
    }

    public static DupeIPEntry getLatestDupeIPResult() {
        return DUPEIP_SCAN_RESULTS.isEmpty() ? null : DUPEIP_SCAN_RESULTS.peekFirst();
    }
}
package com.scs;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(modid = Scs.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // HUD настройки
    public static final ForgeConfigSpec.BooleanValue ENABLE_HUD = BUILDER
            .comment("Включить HUD ScS")
            .define("enableHud", true);

    public static final ForgeConfigSpec.IntValue MAX_MESSAGES = BUILDER
            .comment("Максимум сообщений для хранения")
            .defineInRange("maxMessages", 50, 1, 200);

    public static final ForgeConfigSpec.IntValue SHOW_LAST = BUILDER
            .comment("Сколько последних сообщений показывать на экране")
            .defineInRange("showLast", 15, 1, 50);

    // Позиционирование HUD
    public static final ForgeConfigSpec.IntValue HUD_X = BUILDER
            .comment("X позиция HUD (отрицательное значение = от правого края)")
            .defineInRange("hudX", -320, -1000, 1000);

    public static final ForgeConfigSpec.IntValue HUD_Y = BUILDER
            .comment("Y позиция HUD")
            .defineInRange("hudY", 6, 0, 500);

    // Цвета
    public static final ForgeConfigSpec.ConfigValue<String> CHECK_COLOR = BUILDER
            .comment("Цвет сообщений проверки (HEX без #)")
            .define("checkColor", "00FF7F");

    public static final ForgeConfigSpec.ConfigValue<String> AC_COLOR = BUILDER
            .comment("Цвет сообщений античита (HEX без #)")
            .define("acColor", "FF4444");

    public static final ForgeConfigSpec.ConfigValue<String> VIOLATION_COLOR = BUILDER
            .comment("Цвет нарушений (HEX без #)")
            .define("violationColor", "FFA500");

    // Интерактивные кнопки
    public static final ForgeConfigSpec.BooleanValue ENABLE_CHAT_BUTTONS = BUILDER
            .comment("Включить интерактивные кнопки в чате")
            .define("enableChatButtons", true);

    public static final ForgeConfigSpec.BooleanValue AUTO_COMMANDS = BUILDER
            .comment("Разрешить автоматическое выполнение команд")
            .define("autoCommands", false);

    // Звуки и уведомления
    public static final ForgeConfigSpec.BooleanValue SOUND_ALERTS = BUILDER
            .comment("Включить звуковые уведомления")
            .define("soundAlerts", true);

    public static final ForgeConfigSpec.ConfigValue<String> ALERT_SOUND = BUILDER
            .comment("Звук для уведомлений")
            .define("alertSound", "minecraft:block.note_block.bell");

    // Логирование
    public static final ForgeConfigSpec.BooleanValue ENABLE_LOGGING = BUILDER
            .comment("Включить логирование в файл")
            .define("enableLogging", true);

    public static final ForgeConfigSpec.BooleanValue LOG_ALL_CHAT = BUILDER
            .comment("Логировать все сообщения чата")
            .define("logAllChat", false);

    // Фильтры и паттерны
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> VIOLATION_KEYWORDS = BUILDER
            .comment("Ключевые слова для определения нарушений")
            .defineList("violationKeywords", Arrays.asList(
                    "tried to move abnormally",
                    "tried to reach entity outside",
                    "might be using combat hacks",
                    "suspected use of automatic robots",
                    "tried to interact",
                    "invalid movement",
                    "speed hacks",
                    "fly hacks"
            ), o -> o instanceof String);

    public static final ForgeConfigSpec.BooleanValue DEBUG_MODE = BUILDER
            .comment("Включить отладочные сообщения в логи")
            .define("debugMode", true);

    public static final ForgeConfigSpec.BooleanValue LOG_RAW_MESSAGES = BUILDER
            .comment("Логировать сырые сообщения чата для отладки")
            .define("logRawMessages", true);


    static final ForgeConfigSpec SPEC = BUILDER.build();

    // Статические поля для быстрого доступа
    public static boolean enableHud;
    public static int maxMessages;
    public static boolean debugMode;
    public static boolean logRawMessages;
    public static int showLast;
    public static int hudX;
    public static int hudY;
    public static String checkColor;
    public static String acColor;
    public static String violationColor;
    public static boolean enableChatButtons;
    public static boolean autoCommands;
    public static boolean soundAlerts;
    public static String alertSound;
    public static boolean enableLogging;
    public static boolean logAllChat;
    public static List<? extends String> violationKeywords;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        enableHud = ENABLE_HUD.get();
        maxMessages = MAX_MESSAGES.get();
        showLast = SHOW_LAST.get();
        hudX = HUD_X.get();
        hudY = HUD_Y.get();
        checkColor = CHECK_COLOR.get();
        acColor = AC_COLOR.get();
        violationColor = VIOLATION_COLOR.get();
        enableChatButtons = ENABLE_CHAT_BUTTONS.get();
        autoCommands = AUTO_COMMANDS.get();
        soundAlerts = SOUND_ALERTS.get();
        alertSound = ALERT_SOUND.get();
        enableLogging = ENABLE_LOGGING.get();
        logAllChat = LOG_ALL_CHAT.get();
        violationKeywords = VIOLATION_KEYWORDS.get();
        debugMode = DEBUG_MODE.get();
        logRawMessages = LOG_RAW_MESSAGES.get();

        Scs.LOGGER.info("ScS Config loaded successfully!");
    }

    public static int parseColor(String hex, int fallback) {
        try {
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            Scs.LOGGER.warn("Invalid color format: {}, using fallback", hex);
            return fallback;
        }
    }
}
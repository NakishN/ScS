package com.scs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import com.scs.Scs;
import com.scs.Config;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class ShaurmaSystem {
    private static long shaurmaCount = 0;
    private static long totalTaps = 0;
    private static long lastSaveTime = 0;
    private static final Random random = new Random();
    private static final Path SAVE_FILE = Paths.get("config", "scs-shaurma.dat");

    // Настройки таполки (теперь используют конфиг)
    private static final int[] BONUS_MULTIPLIERS = {2, 3, 5, 10};
    private static final String[] BONUS_MESSAGES = {
            "ДВОЙНАЯ ШАУРМА! 🌯🌯",
            "ТРОЙНАЯ ШАУРМА! 🌯🌯🌯",
            "МЕГА ШАУРМА! 🌯✨",
            "ЛЕГЕНДАРНАЯ ШАУРМА! 🌯⭐"
    };

    // Обычные сообщения для разнообразия
    private static final String[] TAP_MESSAGES = {
            "Вкусная шаурма! 🌯",
            "Сочная шаурма! 🌯💧",
            "Ароматная шаурма! 🌯🔥",
            "Питательная шаурма! 🌯💪",
            "Свежая шаурма! 🌯🌿",
            "Острая шаурма! 🌯🌶️",
            "Сытная шаурма! 🌯😋"
    };

    static {
        loadShaurmaData();
    }

    public static void onShaurmaTap() {
        if (!Config.enableShaurma) return;

        totalTaps++;

        // Определяем награду
        int reward = Config.shaurmaBaseReward;
        String message;

        if (random.nextInt(100) < Config.shaurmaBonusChance) {
            // Бонусная шаурма!
            int bonusIndex = random.nextInt(BONUS_MULTIPLIERS.length);
            reward = Config.shaurmaBaseReward * BONUS_MULTIPLIERS[bonusIndex];
            message = BONUS_MESSAGES[bonusIndex];

            // Специальный звук для бонуса
            if (Config.shaurmaSounds) {
                playBonusSound();
            }

            // ЛОГИРУЕМ ТОЛЬКО БОНУСЫ
            Scs.LOGGER.info("[ScS] Shaurma BONUS: +{}x{} = {} (total: {})", Config.shaurmaBaseReward, BONUS_MULTIPLIERS[bonusIndex], reward, shaurmaCount + reward);
        } else {
            // Обычная шаурма
            message = TAP_MESSAGES[random.nextInt(TAP_MESSAGES.length)];
            if (Config.shaurmaSounds) {
                playTapSound();
            }
        }

        shaurmaCount += reward;

        // Создаем красивое сообщение в чат
        if (Config.shaurmaChatMessages) {
            sendShaurmaMessage(message, reward);
        }

        // Сохраняем каждые 10 тапов
        if (totalTaps % 10 == 0) {
            saveShaurmaData();
            // Логируем каждые 50 тапов
            if (totalTaps % 50 == 0) {
                Scs.LOGGER.info("[ScS] Shaurma milestone: {} taps, {} shaurma total", totalTaps, shaurmaCount);
            }
        }
    }

    private static void sendShaurmaMessage(String message, int reward) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Основное сообщение с градиентом
        Component mainMessage = Component.literal("✨ " + message + " ✨")
                .setStyle(Style.EMPTY
                        .withColor(reward > Config.shaurmaBaseReward ? ChatFormatting.GOLD : ChatFormatting.YELLOW)
                        .withBold(reward > Config.shaurmaBaseReward));

        // Сообщение о награде
        Component rewardMessage = Component.literal(String.format("▶ +%d шаурмы! Всего: %d 🌯", reward, shaurmaCount))
                .setStyle(Style.EMPTY
                        .withColor(ChatFormatting.GREEN)
                        .withItalic(true));

        // Отправляем в чат
        mc.gui.getChat().addMessage(mainMessage);
        mc.gui.getChat().addMessage(rewardMessage);

        // Если большой бонус, добавляем дополнительное сообщение
        if (reward >= 10) {
            Component epicMessage = Component.literal("🎉 ЭПИЧЕСКАЯ НАГРАДА! 🎉")
                    .setStyle(Style.EMPTY
                            .withColor(ChatFormatting.LIGHT_PURPLE)
                            .withBold(true));
            mc.gui.getChat().addMessage(epicMessage);
        }
    }

    private static void playTapSound() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
                mc.level.playLocalSound(
                        mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.MASTER,
                        0.3f, 1.2f + random.nextFloat() * 0.3f, false
                );
            }
        } catch (Exception e) {
            // Убираем частые логи ошибок звука
            if (totalTaps % 100 == 0) {
                Scs.LOGGER.warn("[ScS] Sound issues detected (logged every 100 taps)");
            }
        }
    }

    private static void playBonusSound() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
                // Играем несколько звуков для эффекта
                mc.level.playLocalSound(
                        mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.MASTER,
                        0.5f, 1.5f, false
                );

                // Дополнительный звук через небольшую задержку
                Thread delayedSound = new Thread(() -> {
                    try {
                        Thread.sleep(200);
                        if (mc.level != null && mc.player != null) {
                            mc.level.playLocalSound(
                                    mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.MASTER,
                                    0.4f, 2.0f, false
                            );
                        }
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                });
                delayedSound.start();
            }
        } catch (Exception e) {
            // Убираем частые логи ошибок
        }
    }

    public static void saveShaurmaData() {
        try {
            if (!Files.exists(SAVE_FILE.getParent())) {
                Files.createDirectories(SAVE_FILE.getParent());
            }

            String data = String.format("%d:%d:%d", shaurmaCount, totalTaps, System.currentTimeMillis());
            Files.write(SAVE_FILE, data.getBytes(StandardCharsets.UTF_8));
            lastSaveTime = System.currentTimeMillis();

        } catch (IOException e) {
            Scs.LOGGER.error("[ScS] Failed to save shaurma data", e);
        }
    }

    public static void loadShaurmaData() {
        try {
            if (Files.exists(SAVE_FILE)) {
                String data = Files.readString(SAVE_FILE, StandardCharsets.UTF_8);
                String[] parts = data.split(":");

                if (parts.length >= 2) {
                    shaurmaCount = Long.parseLong(parts[0]);
                    totalTaps = Long.parseLong(parts[1]);
                    if (parts.length >= 3) {
                        lastSaveTime = Long.parseLong(parts[2]);
                    }

                    Scs.LOGGER.info("[ScS] Loaded shaurma data: {} shaurma, {} taps", shaurmaCount, totalTaps);
                }
            } else {
                Scs.LOGGER.info("[ScS] Starting fresh shaurma session!");
            }
        } catch (Exception e) {
            Scs.LOGGER.error("[ScS] Failed to load shaurma data, starting fresh", e);
            shaurmaCount = 0;
            totalTaps = 0;
        }
    }

    // Геттеры для GUI
    public static long getShaurmaCount() {
        return shaurmaCount;
    }

    public static long getTotalTaps() {
        return totalTaps;
    }

    public static double getAveragePerTap() {
        return totalTaps > 0 ? (double) shaurmaCount / totalTaps : 0.0;
    }

    // Методы для достижений
    public static boolean hasAchievement(String achievement) {
        return switch (achievement) {
            case "first_tap" -> totalTaps >= 1;
            case "hundred_taps" -> totalTaps >= 100;
            case "thousand_taps" -> totalTaps >= 1000;
            case "hundred_shaurma" -> shaurmaCount >= 100;
            case "thousand_shaurma" -> shaurmaCount >= 1000;
            case "ten_thousand_shaurma" -> shaurmaCount >= 10000;
            default -> false;
        };
    }

    // Сброс данных (для отладки)
    public static void resetData() {
        long oldShaurma = shaurmaCount;
        long oldTaps = totalTaps;

        shaurmaCount = 0;
        totalTaps = 0;
        saveShaurmaData();

        // Логируем только при сбросе
        Scs.LOGGER.info("[ScS] Shaurma data reset: was {} shaurma, {} taps", oldShaurma, oldTaps);

        Minecraft mc = Minecraft.getInstance();
        if (mc.gui != null) {
            mc.gui.getChat().addMessage(
                    Component.literal("🔄 Данные шаурмы сброшены!")
                            .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))
            );
        }
    }
}
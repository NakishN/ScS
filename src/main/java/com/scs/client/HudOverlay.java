package com.scs.client;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import com.scs.Config;
import com.scs.Scs;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class HudOverlay {

    private static boolean hudVisible = true;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static int animationTick = 0;

    public static void toggleHud() {
        hudVisible = !hudVisible;
        Minecraft.getInstance().gui.setOverlayMessage(
                Component.literal("ScS HUD: " + (hudVisible ? "Включен" : "Выключен")),
                false
        );
        Scs.LOGGER.info("[ScS] HUD toggled: {}", hudVisible ? "ON" : "OFF");
    }

    @SubscribeEvent
    public void onRenderHud(CustomizeGuiOverlayEvent event) {
        if (!Config.enableHud || !hudVisible) return;

        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        GuiGraphics g = event.getGuiGraphics();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        // Увеличиваем счетчик анимации
        animationTick++;

        // Вычисляем позицию
        int x = Config.hudX < 0 ? sw + Config.hudX : Config.hudX;
        int y = Config.hudY;
        int w = 320;

        // Считаем количество записей для отображения
        List<ChatTap.Entry> entriesToShow = new ArrayList<>();
        int count = 0;
        for (ChatTap.Entry entry : ChatTap.ENTRIES) {
            if (count >= Config.showLast) break;
            entriesToShow.add(entry);
            count++;
        }

        // Высота основной панели
        int mainPanelHeight = 16 + (entriesToShow.size() * 11) + 8;

        // Рисуем основную панель античита
        renderMainPanel(g, x, y, w, mainPanelHeight, entriesToShow, mc.font);

        // Рисуем панель DupeIP если есть последний результат
        ChatTap.DupeIPEntry latestDupeIP = ChatTap.getLatestDupeIPResult();
        if (latestDupeIP != null && System.currentTimeMillis() - latestDupeIP.timestamp.toEpochMilli() < 30000) {
            renderDupeIPPanel(g, x, y + mainPanelHeight + 6, w, latestDupeIP, mc.font);
        }

        // Рисуем панель чата игроков если есть сообщения
        if (!ChatTap.PLAYER_CHAT.isEmpty()) {
            int chatPanelY = latestDupeIP != null && System.currentTimeMillis() - latestDupeIP.timestamp.toEpochMilli() < 30000
                    ? y + mainPanelHeight + 6 + 46 + 6
                    : y + mainPanelHeight + 6;
            renderPlayerChatPanel(g, x, chatPanelY, w, mc.font);
        }
    }

    private void renderMainPanel(GuiGraphics g, int x, int y, int w, int panelHeight, List<ChatTap.Entry> entriesToShow, net.minecraft.client.gui.Font font) {
        // Анимированный фон с волновым эффектом
        float wave = Mth.sin(animationTick * 0.1f) * 0.2f + 0.8f;
        int bgColor = (int)(255 * wave * 0.7f) << 24 | 0x000000;

        // Рисуем полупрозрачный фон панели
        g.fill(x - 4, y - 2, x + w + 4, y + panelHeight, bgColor);

        // Рисуем анимированную рамку
        int borderColor = (int)(255 * (0.5f + 0.3f * Mth.sin(animationTick * 0.05f))) << 24 | 0x444444;
        g.fill(x - 5, y - 3, x + w + 5, y - 2, borderColor); // верх
        g.fill(x - 5, y + panelHeight, x + w + 5, y + panelHeight + 1, borderColor); // низ
        g.fill(x - 5, y - 3, x - 4, y + panelHeight + 1, borderColor); // лево
        g.fill(x + w + 4, y - 3, x + w + 5, y + panelHeight + 1, borderColor); // право

        // Заголовок с анимированной подсветкой
        int headerBg = (int)(255 * wave * 0.8f) << 24 | 0x222222;
        g.fill(x - 4, y - 2, x + w + 4, y + 14, headerBg);

        // Анимированный цвет заголовка
        int headerColor = 0xFFFFFF;
        if (animationTick % 60 < 30 && !ChatTap.VIOLATIONS.isEmpty()) {
            headerColor = 0xFFFF88; // Мигание желтым при наличии нарушений
        }

        g.drawString(font, "ScS • Античит + DupeIP Монитор", x, y, headerColor, true);

        // Счетчики с цветовой индикацией
        int violationCount = ChatTap.VIOLATIONS.size();
        int dupeIPCount = ChatTap.getDupeIPResults().size();
        int chatCount = ChatTap.PLAYER_CHAT.size();

        String stats = String.format("Нарушений: %d | DupeIP: %d | Чат: %d | Всего: %d",
                violationCount, dupeIPCount, chatCount, ChatTap.ENTRIES.size());

        // Цвет статистики зависит от наличия нарушений
        int statsColor = violationCount > 0 ? 0xFFCC88 : 0xCCCCCC;
        g.drawString(font, stats, x + 180, y, statsColor, false);

        y += 16;

        // Рисуем записи
        for (ChatTap.Entry entry : entriesToShow) {
            int color = getColorForEntry(entry);
            String timeStr = entry.timestamp.atZone(ZoneId.systemDefault()).format(TIME_FORMATTER);
            String displayText = String.format("[%s] %s", timeStr, entry.text);

            // Обрезаем текст если он слишком длинный
            displayText = clipText(displayText, w - 8, font);

            // Подсвечиваем фон для серьезных нарушений и DupeIP результатов
            boolean isSerious = isEntrySerious(entry);
            boolean isDupeIP = entry.kind.startsWith("DUPEIP");
            boolean isPlayerChat = entry.kind.equals("CHAT");

            if (isSerious) {
                // Анимированная подсветка для серьезных нарушений
                float pulseIntensity = (Mth.sin(animationTick * 0.3f) + 1.0f) * 0.5f;
                int seriousBg = (int)(255 * pulseIntensity * 0.3f) << 24 | 0xFF0000;
                g.fill(x - 2, y - 1, x + w + 2, y + 9, seriousBg);
            } else if (isDupeIP) {
                g.fill(x - 2, y - 1, x + w + 2, y + 9, 0x440099FF);
            } else if (isPlayerChat) {
                g.fill(x - 2, y - 1, x + w + 2, y + 9, 0x44888888);
            }

            // Добавляем индикатор типа
            String typeIndicator = switch (entry.kind) {
                case "CHECK" -> "✓ ";
                case "DUPEIP_SCAN" -> "🔍 ";
                case "DUPEIP_RESULT" -> "👥 ";
                case "VIOLATION", "AC" -> isSerious ? "⚠ " : "⚠ ";
                case "CHAT" -> "💬 ";
                default -> "";
            };
            displayText = typeIndicator + displayText;

            g.drawString(font, displayText, x, y, color, false);
            y += 11;
        }

        // Дополнительная информация внизу
        if (!entriesToShow.isEmpty()) {
            y += 4;
            int infoBg = (int)(255 * wave * 0.6f) << 24 | 0x111111;
            g.fill(x - 4, y - 2, x + w + 4, y + 12, infoBg);

            String info = "F8 - переключить HUD | F9 - история | F10 - очистить";
            g.drawString(font, info, x, y, 0x888888, false);
        }
    }

    private void renderDupeIPPanel(GuiGraphics g, int x, int y, int w, ChatTap.DupeIPEntry dupeEntry, net.minecraft.client.gui.Font font) {
        // Рисуем отдельную панель для DupeIP информации
        int panelHeight = 40;

        // Анимированный фон панели
        float wave = Mth.sin(animationTick * 0.15f) * 0.3f + 0.7f;
        int bgColor = (int)(255 * wave * 0.8f) << 24 | 0x001122;
        g.fill(x - 4, y - 2, x + w + 4, y + panelHeight, bgColor);

        // Анимированная рамка
        int borderColor = (int)(255 * (0.6f + 0.4f * Mth.sin(animationTick * 0.1f))) << 24 | 0x0099FF;
        g.fill(x - 5, y - 3, x + w + 5, y - 2, borderColor); // верх
        g.fill(x - 5, y + panelHeight, x + w + 5, y + panelHeight + 1, borderColor); // низ
        g.fill(x - 5, y - 3, x - 4, y + panelHeight + 1, borderColor); // лево
        g.fill(x + w + 4, y - 3, x + w + 5, y + panelHeight + 1, borderColor); // право

        // Заголовок с мигающим эффектом
        int titleColor = animationTick % 40 < 20 ? 0x00DDFF : 0x0099CC;
        g.drawString(font, "🔍 Последний DupeIP скан:", x, y, titleColor, true);
        y += 12;

        // Основная информация
        String mainInfo = String.format("Игрок: %s | Найдено: %d дубликатов",
                dupeEntry.scannedPlayer, dupeEntry.totalDupes);
        g.drawString(font, mainInfo, x, y, 0xFFFFFF, false);
        y += 10;

        // Список никнеймов (обрезанный)
        String nicknames = String.join(", ", dupeEntry.duplicateAccounts);
        nicknames = clipText("Дубликаты: " + nicknames, w - 8, font);
        g.drawString(font, nicknames, x, y, 0xFFDD00, false);
        y += 10;

        // Подсказка
        g.drawString(font, "💡 Используйте кнопки в чате для действий", x, y, 0x888888, false);
    }

    private void renderPlayerChatPanel(GuiGraphics g, int x, int y, int w, net.minecraft.client.gui.Font font) {
        // Показываем последние 3 сообщения чата игроков
        List<ChatTap.PlayerChatEntry> recentChat = new ArrayList<>();
        int count = 0;
        for (ChatTap.PlayerChatEntry chatEntry : ChatTap.PLAYER_CHAT) {
            if (count >= 3) break;
            recentChat.add(chatEntry);
            count++;
        }

        if (recentChat.isEmpty()) return;

        int panelHeight = 12 + (recentChat.size() * 11) + 6;

        // Фон панели чата
        float wave = Mth.sin(animationTick * 0.08f) * 0.2f + 0.6f;
        int bgColor = (int)(255 * wave * 0.7f) << 24 | 0x112200;
        g.fill(x - 4, y - 2, x + w + 4, y + panelHeight, bgColor);

        // Рамка
        int borderColor = (int)(255 * wave) << 24 | 0x448844;
        g.fill(x - 5, y - 3, x + w + 5, y - 2, borderColor); // верх
        g.fill(x - 5, y + panelHeight, x + w + 5, y + panelHeight + 1, borderColor); // низ
        g.fill(x - 5, y - 3, x - 4, y + panelHeight + 1, borderColor); // лево
        g.fill(x + w + 4, y - 3, x + w + 5, y + panelHeight + 1, borderColor); // право

        // Заголовок
        g.drawString(font, "💬 Чат игроков:", x, y, 0x88FF88, true);
        y += 12;

        // Сообщения чата
        for (ChatTap.PlayerChatEntry chatEntry : recentChat) {
            String timeStr = chatEntry.timestamp.atZone(ZoneId.systemDefault()).format(TIME_FORMATTER);
            String chatText = String.format("[%s] %s: %s", timeStr, chatEntry.playerName, chatEntry.message);
            chatText = clipText(chatText, w - 8, font);

            g.drawString(font, chatText, x, y, 0xDDDDDD, false);
            y += 11;
        }
    }

    private boolean isEntrySerious(ChatTap.Entry entry) {
        if (entry.playerName == null) return false;

        for (ChatTap.ViolationEntry violation : ChatTap.VIOLATIONS) {
            if (violation.playerName != null && violation.playerName.equals(entry.playerName) && violation.isSerious) {
                return true;
            }
        }
        return false;
    }

    private int getColorForEntry(ChatTap.Entry entry) {
        switch (entry.kind) {
            case "CHECK":
                return Config.parseColor(Config.checkColor, 0x00FF7F);
            case "DUPEIP_SCAN":
                return 0x00AAFF;
            case "DUPEIP_RESULT":
                return 0x0099DD;
            case "CHAT":
                return 0x88DD88;
            case "AC":
            case "VIOLATION":
                // Проверяем, является ли это серьезным нарушением
                for (ChatTap.ViolationEntry violation : ChatTap.VIOLATIONS) {
                    if (violation.playerName != null && violation.playerName.equals(entry.playerName)) {
                        return violation.isSerious ? 0xFF4444 : Config.parseColor(Config.violationColor, 0xFFA500);
                    }
                }
                return Config.parseColor(Config.violationColor, 0xFFA500);
            default:
                return 0xCCCCCC;
        }
    }

    private String clipText(String text, int maxWidth, net.minecraft.client.gui.Font font) {
        if (font.width(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        int ellipsisWidth = font.width(ellipsis);

        int lo = 0, hi = text.length();
        while (lo < hi) {
            int mid = (lo + hi + 1) >>> 1;
            String substring = text.substring(0, mid) + ellipsis;
            if (font.width(substring) <= maxWidth) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }

        return text.substring(0, Math.max(0, lo)) + ellipsis;
    }

    // Статические методы для внешнего управления
    public static boolean isHudVisible() {
        return hudVisible;
    }

    public static void setHudVisible(boolean visible) {
        hudVisible = visible;
        Scs.LOGGER.info("[ScS] HUD visibility set to: {}", visible);
    }
}
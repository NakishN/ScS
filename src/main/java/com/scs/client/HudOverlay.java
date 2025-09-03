package com.scs.client;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import com.scs.Config;
import com.scs.Scs;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class HudOverlay {

    private static boolean hudVisible = true;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void toggleHud() {
        hudVisible = !hudVisible;
        Minecraft.getInstance().gui.setOverlayMessage(
                Component.literal("ScS HUD: " + (hudVisible ? "Включен" : "Выключен")),
                false
        );
    }

    @SubscribeEvent
    public void onRenderHud(CustomizeGuiOverlayEvent event) {
        if (!Config.enableHud || !hudVisible) return;

        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        GuiGraphics g = event.getGuiGraphics();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

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

        int panelHeight = 16 + (entriesToShow.size() * 11) + 8;

        // Рисуем полупрозрачный фон панели
        g.fill(x - 4, y - 2, x + w + 4, y + panelHeight, 0x90000000);

        // Рисуем рамку
        g.fill(x - 5, y - 3, x + w + 5, y - 2, 0xFF444444); // верх
        g.fill(x - 5, y + panelHeight, x + w + 5, y + panelHeight + 1, 0xFF444444); // низ
        g.fill(x - 5, y - 3, x - 4, y + panelHeight + 1, 0xFF444444); // лево
        g.fill(x + w + 4, y - 3, x + w + 5, y + panelHeight + 1, 0xFF444444); // право

        // Заголовок с подсветкой
        g.fill(x - 4, y - 2, x + w + 4, y + 14, 0xAA222222);
        g.drawString(mc.font, "ScS • Античит Монитор", x, y, 0xFFFFFF, true);

        // Счетчики
        int violationCount = ChatTap.VIOLATIONS.size();
        String stats = String.format("Нарушений: %d | Всего: %d", violationCount, ChatTap.ENTRIES.size());
        g.drawString(mc.font, stats, x + 200, y, 0xCCCCCC, false);

        y += 16;

        // Рисуем записи
        for (ChatTap.Entry entry : entriesToShow) {
            int color = getColorForEntry(entry);
            String timeStr = entry.timestamp.atZone(ZoneId.systemDefault()).format(TIME_FORMATTER);
            String displayText = String.format("[%s] %s", timeStr, entry.text);

            // Обрезаем текст если он слишком длинный
            displayText = clipText(displayText, w - 8, mc.font);

            // Подсвечиваем фон для серьезных нарушений
            boolean isSerious = false;
            for (ChatTap.ViolationEntry violation : ChatTap.VIOLATIONS) {
                if (violation.playerName != null && violation.playerName.equals(entry.playerName) && violation.isSerious) {
                    isSerious = true;
                    break;
                }
            }

            if (isSerious) {
                g.fill(x - 2, y - 1, x + w + 2, y + 9, 0x44FF0000);
            }

            g.drawString(mc.font, displayText, x, y, color, false);
            y += 11;
        }

        // Дополнительная информация внизу
        if (!entriesToShow.isEmpty()) {
            y += 4;
            g.fill(x - 4, y - 2, x + w + 4, y + 12, 0xAA111111);

            String info = "F8 - переключить HUD | F9 - история | F10 - очистить";
            g.drawString(mc.font, info, x, y, 0x888888, false);
        }
    }

    private int getColorForEntry(ChatTap.Entry entry) {
        switch (entry.kind) {
            case "CHECK":
                return Config.parseColor(Config.checkColor, 0x00FF7F);
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
    }
}
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
import java.text.DecimalFormat;

public final class HudOverlay {

    private static boolean hudVisible = true;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DecimalFormat SHAURMA_FORMAT = new DecimalFormat("#,###");
    private static int animationTick = 0;

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

        animationTick++;
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

        // Добавляем место для шаурма-панели только если она включена
        int shaurmaPanelHeight = (Config.enableShaurma && Config.shaurmaHud) ? 45 : 0;
        int mainPanelHeight = 16 + (entriesToShow.size() * 11) + 8;

        // Рисуем шаурма-панель сверху только если включена
        if (Config.enableShaurma && Config.shaurmaHud) {
            renderShaurmaPanel(g, x, y, w);
            y += shaurmaPanelHeight + 4;
        }

        // Рисуем полупрозрачный фон основной панели
        g.fill(x - 4, y - 2, x + w + 4, y + mainPanelHeight, 0x90000000);

        // Рисуем рамку основной панели
        g.fill(x - 5, y - 3, x + w + 5, y - 2, 0xFF444444); // верх
        g.fill(x - 5, y + mainPanelHeight, x + w + 5, y + mainPanelHeight + 1, 0xFF444444); // низ
        g.fill(x - 5, y - 3, x - 4, y + mainPanelHeight + 1, 0xFF444444); // лево
        g.fill(x + w + 4, y - 3, x + w + 5, y + mainPanelHeight + 1, 0xFF444444); // право

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

            String info = Config.enableShaurma ?
                    "F8 - переключить HUD | F9 - история | F10 - очистить | U - тап | Y - меню" :
                    "F8 - переключить HUD | F9 - история | F10 - очистить";
            g.drawString(mc.font, info, x, y, 0x888888, false);
        }
    }

    private void renderShaurmaPanel(GuiGraphics g, int x, int y, int w) {
        // Анимированный фон панели шаурмы
        float wave = Mth.sin(animationTick * 0.1f) * 0.2f + 0.8f;
        int bgAlpha = (int)(255 * wave * 0.7f);
        int bgColor = (bgAlpha << 24) | 0x4A4A00; // Желтоватый фон

        g.fill(x - 4, y - 2, x + w + 4, y + 43, bgColor);

        // Золотая рамка
        int borderColor = 0xFFFFD700;
        g.fill(x - 5, y - 3, x + w + 5, y - 2, borderColor); // верх
        g.fill(x - 5, y + 43, x + w + 5, y + 44, borderColor); // низ
        g.fill(x - 5, y - 3, x - 4, y + 44, borderColor); // лево
        g.fill(x + w + 4, y - 3, x + w + 5, y + 44, borderColor); // право

        // Заголовок шаурма-панели
        String title = "🌯 ШАУРМА ИМПЕРИЯ 🌯";
        g.drawString(Minecraft.getInstance().font, title, x, y, 0xFFFFD700, true);

        // Количество шаурмы с анимацией
        long shaurmaCount = ShaurmaSystem.getShaurmaCount();
        String shaurmaText = SHAURMA_FORMAT.format(shaurmaCount) + " 🌯";

        // Анимированный размер текста для шаурмы
        g.pose().pushPose();
        float scale = 1.3f + Mth.sin(animationTick * 0.08f) * 0.1f;
        g.pose().scale(scale, scale, 1.0f);

        int scaledX = (int)((x + 5) / scale);
        int scaledY = (int)((y + 15) / scale);
        g.drawString(Minecraft.getInstance().font, shaurmaText, scaledX, scaledY, 0xFF00FF00, true);
        g.pose().popPose();

        // Статистика тапов
        long totalTaps = ShaurmaSystem.getTotalTaps();
        String tapsText = "Тапов: " + SHAURMA_FORMAT.format(totalTaps);
        g.drawString(Minecraft.getInstance().font, tapsText, x + 200, y + 15, 0xFFCCCCCC, false);

        // Среднее за тап
        double avgPerTap = ShaurmaSystem.getAveragePerTap();
        String avgText = String.format("Среднее: %.2f", avgPerTap);
        g.drawString(Minecraft.getInstance().font, avgText, x + 200, y + 27, 0xFFCCCCCC, false);

        // Подсказка
        String hintText = "Нажми U для тапа или Y для меню";
        float hintAlpha = Mth.sin(animationTick * 0.05f) * 0.3f + 0.7f;
        int hintColor = (int)(255 * hintAlpha) << 24 | 0xFFFF88;
        g.drawString(Minecraft.getInstance().font, hintText, x + 5, y + 30, hintColor, false);

        // Мигающие звездочки для эффекта
        if (animationTick % 40 < 20) {
            g.drawString(Minecraft.getInstance().font, "✨", x + w - 20, y + 5, 0xFFFFFF00, false);
        }
        if ((animationTick + 20) % 40 < 20) {
            g.drawString(Minecraft.getInstance().font, "✨", x + w - 40, y + 25, 0xFFFFFF00, false);
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
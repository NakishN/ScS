package com.scs.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import com.scs.Config;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ShaurmaMenuScreen extends Screen {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###");
    private static final DecimalFormat AVERAGE_FORMAT = new DecimalFormat("#.##");

    private int animationTick = 0;
    private Button tapButton;
    private Button resetButton;
    private final List<FloatingNumber> floatingNumbers = new ArrayList<>();

    // Данные достижений
    private static final Achievement[] ACHIEVEMENTS = {
            new Achievement("first_tap", "Первый тап", "Сделай свой первый тап!", "🌯", 1),
            new Achievement("hundred_taps", "Столетие", "100 тапов", "💯", 100),
            new Achievement("thousand_taps", "Тысячник", "1000 тапов", "🔥", 1000),
            new Achievement("hundred_shaurma", "Коллекционер", "100 шаурмы", "📦", 100),
            new Achievement("thousand_shaurma", "Миллионер", "1000 шаурмы", "💰", 1000),
            new Achievement("ten_thousand_shaurma", "Олигарх", "10000 шаурмы", "👑", 10000)
    };

    public ShaurmaMenuScreen() {
        super(Component.literal("🌯 Шаурма Меню 🌯"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Большая кнопка тапа в центре
        this.tapButton = this.addRenderableWidget(Button.builder(
                        Component.literal("🌯 ТАП! 🌯"),
                        button -> {
                            if (Config.enableShaurma) {
                                ShaurmaSystem.onShaurmaTap();
                                createFloatingNumber();
                                playTapAnimation();
                            }
                        })
                .bounds(centerX - 60, centerY - 15, 120, 30)
                .build());

        // Кнопка сброса (только для отладки)
        this.resetButton = this.addRenderableWidget(Button.builder(
                        Component.literal("🔄 Сброс"),
                        button -> {
                            ShaurmaSystem.resetData();
                            this.onClose();
                        })
                .bounds(this.width - 110, this.height - 30, 100, 20)
                .build());

        // Кнопка закрытия
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE,
                        button -> this.onClose())
                .bounds(10, this.height - 30, 100, 20)
                .build());
    }

    @Override
    public void tick() {
        super.tick();
        animationTick++;

        // Обновляем летающие числа
        floatingNumbers.removeIf(num -> {
            num.tick();
            return num.isDead();
        });
    }

    private void createFloatingNumber() {
        int x = this.width / 2 + (int)(Math.random() * 40 - 20);
        int y = this.height / 2 + (int)(Math.random() * 20 - 10);
        floatingNumbers.add(new FloatingNumber(x, y, "+1"));
    }

    private void playTapAnimation() {
        // Простая анимация нажатия кнопки
        if (tapButton != null) {
            // Кнопка будет выглядеть нажатой на несколько тиков
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Красивый градиентный фон
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        renderGradientBackground(guiGraphics);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Заголовок с анимацией
        renderAnimatedTitle(guiGraphics);

        // Главная статистика
        renderMainStats(guiGraphics);

        // Боковые панели
        renderLeftPanel(guiGraphics);  // Статистика
        renderRightPanel(guiGraphics); // Достижения

        // Летающие числа
        renderFloatingNumbers(guiGraphics);

        // Нижняя панель с подсказками
        renderBottomPanel(guiGraphics);
    }

    private void renderGradientBackground(GuiGraphics guiGraphics) {
        // Создаем красивый градиентный фон
        int color1 = 0xFF1a1a2e;  // Темно-синий
        int color2 = 0xFF16213e;  // Еще темнее
        int color3 = 0xFF0f3460;  // Синий

        // Вертикальный градиент
        guiGraphics.fillGradient(0, 0, this.width, this.height / 3, color1, color2);
        guiGraphics.fillGradient(0, this.height / 3, this.width, 2 * this.height / 3, color2, color3);
        guiGraphics.fillGradient(0, 2 * this.height / 3, this.width, this.height, color3, color1);

        // Добавляем блики
        float wave = Mth.sin(animationTick * 0.1f) * 0.1f + 0.9f;
        int blinkColor = (int)(255 * wave) << 24 | 0x00FFFF;
        guiGraphics.fill(0, 0, this.width, 2, blinkColor);
        guiGraphics.fill(0, this.height - 2, this.width, this.height, blinkColor);
    }

    private void renderAnimatedTitle(GuiGraphics guiGraphics) {
        String title = "🌯 ШАУРМА ИМПЕРИЯ 🌯";

        // Анимированный размер текста
        float scale = 1.5f + Mth.sin(animationTick * 0.1f) * 0.1f;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0f);

        int titleWidth = (int)(this.font.width(title) * scale);
        int x = (int)((this.width - titleWidth) / 2 / scale);
        int y = (int)(20 / scale);

        // Тень
        guiGraphics.drawString(this.font, title, x + 2, y + 2, 0xFF000000, false);
        // Основной текст
        guiGraphics.drawString(this.font, title, x, y, 0xFFFFD700, false);

        guiGraphics.pose().popPose();
    }

    private void renderMainStats(GuiGraphics guiGraphics) {
        int centerX = this.width / 2;
        int startY = 70;

        // Панель со статистикой
        int panelWidth = 300;
        int panelHeight = 80;
        int panelX = centerX - panelWidth / 2;

        // Фон панели
        guiGraphics.fill(panelX - 5, startY - 5, panelX + panelWidth + 5, startY + panelHeight + 5, 0xFF2a2a3e);
        guiGraphics.fill(panelX, startY, panelX + panelWidth, startY + panelHeight, 0xFF3a3a5e);

        // Рамка
        guiGraphics.fill(panelX - 1, startY - 1, panelX + panelWidth + 1, startY, 0xFFFFD700);
        guiGraphics.fill(panelX - 1, startY + panelHeight, panelX + panelWidth + 1, startY + panelHeight + 1, 0xFFFFD700);
        guiGraphics.fill(panelX - 1, startY - 1, panelX, startY + panelHeight + 1, 0xFFFFD700);
        guiGraphics.fill(panelX + panelWidth, startY - 1, panelX + panelWidth + 1, startY + panelHeight + 1, 0xFFFFD700);

        // Количество шаурмы - большими буквами
        String shaurmaText = DECIMAL_FORMAT.format(ShaurmaSystem.getShaurmaCount()) + " 🌯";
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(2.0f, 2.0f, 1.0f);
        int bigTextX = (int)((centerX - this.font.width(shaurmaText)) / 2);
        guiGraphics.drawString(this.font, shaurmaText, bigTextX, (startY + 10) / 2, 0xFF00FF00, true);
        guiGraphics.pose().popPose();

        // Дополнительная статистика
        String tapsText = "Всего тапов: " + DECIMAL_FORMAT.format(ShaurmaSystem.getTotalTaps());
        String avgText = "Среднее за тап: " + AVERAGE_FORMAT.format(ShaurmaSystem.getAveragePerTap());

        guiGraphics.drawString(this.font, tapsText, centerX - this.font.width(tapsText) / 2, startY + 50, 0xFFCCCCCC, false);
        guiGraphics.drawString(this.font, avgText, centerX - this.font.width(avgText) / 2, startY + 62, 0xFFCCCCCC, false);
    }

    private void renderLeftPanel(GuiGraphics guiGraphics) {
        int panelX = 20;
        int panelY = 100;
        int panelWidth = 150;
        int panelHeight = 200;

        // Фон панели
        guiGraphics.fill(panelX - 3, panelY - 3, panelX + panelWidth + 3, panelY + panelHeight + 3, 0xFF2a2a3e);
        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF1a1a2e);

        // Заголовок
        String title = "📊 Статистика";
        guiGraphics.drawString(this.font, title, panelX + 5, panelY + 5, 0xFFFFD700, true);

        int y = panelY + 20;

        // Различная статистика
        String[] stats = {
                "Всего шаурмы:",
                DECIMAL_FORMAT.format(ShaurmaSystem.getShaurmaCount()),
                "",
                "Всего тапов:",
                DECIMAL_FORMAT.format(ShaurmaSystem.getTotalTaps()),
                "",
                "Лучший тап:",
                "10 шаурмы",
                "",
                "Время игры:",
                "∞ часов"
        };

        for (String stat : stats) {
            if (stat.isEmpty()) {
                y += 5;
                continue;
            }

            boolean isValue = stat.matches(".*\\d.*");
            int color = isValue ? 0xFF00FF00 : 0xFFCCCCCC;

            guiGraphics.drawString(this.font, stat, panelX + 5, y, color, false);
            y += 12;
        }
    }

    private void renderRightPanel(GuiGraphics guiGraphics) {
        int panelX = this.width - 170;
        int panelY = 100;
        int panelWidth = 150;
        int panelHeight = 200;

        // Фон панели
        guiGraphics.fill(panelX - 3, panelY - 3, panelX + panelWidth + 3, panelY + panelHeight + 3, 0xFF2a2a3e);
        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF1a1a2e);

        // Заголовок
        String title = "🏆 Достижения";
        guiGraphics.drawString(this.font, title, panelX + 5, panelY + 5, 0xFFFFD700, true);

        int y = panelY + 20;

        for (Achievement achievement : ACHIEVEMENTS) {
            boolean unlocked = ShaurmaSystem.hasAchievement(achievement.id);

            String text = achievement.icon + " " + achievement.name;
            int color = unlocked ? 0xFF00FF00 : 0xFF666666;

            guiGraphics.drawString(this.font, text, panelX + 5, y, color, false);

            // Описание достижения мелким текстом
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(0.7f, 0.7f, 1.0f);
            String desc = achievement.description;
            guiGraphics.drawString(this.font, desc,
                    (int)((panelX + 8) / 0.7f), (int)((y + 10) / 0.7f),
                    unlocked ? 0xFFAAAAAA : 0xFF444444, false);
            guiGraphics.pose().popPose();

            y += 22;
        }
    }

    private void renderFloatingNumbers(GuiGraphics guiGraphics) {
        for (FloatingNumber number : floatingNumbers) {
            number.render(guiGraphics, this.font);
        }
    }

    private void renderBottomPanel(GuiGraphics guiGraphics) {
        int panelY = this.height - 60;
        int panelHeight = 30;

        // Фон
        guiGraphics.fill(0, panelY, this.width, panelY + panelHeight, 0x88000000);

        // Подсказки
        String hint = "💡 Совет: Тапайте быстрее для получения бонусов! Шанс бонуса: 15%";
        int hintX = this.width / 2 - this.font.width(hint) / 2;

        // Анимированный цвет подсказки
        float alpha = Mth.sin(animationTick * 0.05f) * 0.3f + 0.7f;
        int color = (int)(255 * alpha) << 24 | 0xFFFFFF;

        guiGraphics.drawString(this.font, hint, hintX, panelY + 10, color, false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Возможность тапать пробелом или U (только если система включена)
        if (Config.enableShaurma && (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_U)) {
            ShaurmaSystem.onShaurmaTap();
            createFloatingNumber();
            playTapAnimation();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // Класс для летающих чисел
    private static class FloatingNumber {
        private final int startX, startY;
        private final String text;
        private int life = 60; // 3 секунды при 20 FPS
        private final int maxLife = 60;

        public FloatingNumber(int x, int y, String text) {
            this.startX = x;
            this.startY = y;
            this.text = text;
        }

        public void tick() {
            life--;
        }

        public boolean isDead() {
            return life <= 0;
        }

        public void render(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font) {
            if (isDead()) return;

            float progress = 1.0f - (float)life / maxLife;
            float alpha = life > 20 ? 1.0f : (float)life / 20f;

            int x = startX;
            int y = (int)(startY - progress * 30); // Поднимается вверх

            int color = (int)(255 * alpha) << 24 | 0x00FF00;

            guiGraphics.pose().pushPose();
            float scale = 1.0f + progress * 0.5f;
            guiGraphics.pose().scale(scale, scale, 1.0f);

            int scaledX = (int)(x / scale);
            int scaledY = (int)(y / scale);
            guiGraphics.drawString(font, text, scaledX, scaledY, color, true);

            guiGraphics.pose().popPose();
        }
    }

    // Класс достижения
    private static class Achievement {
        final String id;
        final String name;
        final String description;
        final String icon;
        final long requirement;

        Achievement(String id, String name, String description, String icon, long requirement) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.icon = icon;
            this.requirement = requirement;
        }
    }
}
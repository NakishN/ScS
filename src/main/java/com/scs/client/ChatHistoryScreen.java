package com.scs.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import com.scs.Config;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatHistoryScreen extends Screen {
    private final List<ChatTap.Entry> allEntries;
    private final List<ChatTap.ViolationEntry> violations;
    private EntryList entryList;
    private Button filterButton;
    private FilterMode currentFilter = FilterMode.ALL;
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public enum FilterMode {
        ALL("Все"),
        VIOLATIONS_ONLY("Только нарушения"),
        CHECKS_ONLY("Только проверки"),
        SERIOUS_ONLY("Серьезные");

        private final String displayName;

        FilterMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public ChatHistoryScreen() {
        super(Component.literal("ScS - История чата и нарушений"));
        this.allEntries = new ArrayList<>(ChatTap.ENTRIES);
        this.violations = new ArrayList<>(ChatTap.VIOLATIONS);
    }

    @Override
    protected void init() {
        super.init();

        // Список записей
        this.entryList = new EntryList(this.minecraft, this.width, this.height, 32, this.height - 64);
        updateEntryList();
        this.addWidget(this.entryList);

        // Кнопки
        int buttonWidth = 100;
        int buttonSpacing = 110;
        int startX = (this.width - (buttonSpacing * 4 - 10)) / 2;

        // Кнопка фильтра
        this.filterButton = this.addRenderableWidget(Button.builder(
                        Component.literal("Фильтр: " + currentFilter.getDisplayName()),
                        button -> {
                            currentFilter = FilterMode.values()[(currentFilter.ordinal() + 1) % FilterMode.values().length];
                            button.setMessage(Component.literal("Фильтр: " + currentFilter.getDisplayName()));
                            updateEntryList();
                        })
                .bounds(startX, this.height - 56, buttonWidth, 20)
                .build());

        // Кнопка экспорта
        this.addRenderableWidget(Button.builder(
                        Component.literal("Экспорт"),
                        button -> exportToFile())
                .bounds(startX + buttonSpacing, this.height - 56, buttonWidth, 20)
                .build());

        // Кнопка очистки
        this.addRenderableWidget(Button.builder(
                        Component.literal("Очистить"),
                        button -> {
                            ChatTap.clearEntries();
                            this.allEntries.clear();
                            this.violations.clear();
                            updateEntryList();
                        })
                .bounds(startX + buttonSpacing * 2, this.height - 56, buttonWidth, 20)
                .build());

        // Кнопка закрытия
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE,
                        button -> this.onClose())
                .bounds(startX + buttonSpacing * 3, this.height - 56, buttonWidth, 20)
                .build());
    }

    private void updateEntryList() {
        this.entryList.children().clear();

        List<ChatTap.Entry> filteredEntries = getFilteredEntries();

        for (ChatTap.Entry entry : filteredEntries) {
            this.entryList.addEntryPublic(new EntryWidget(entry));
        }
    }

    private List<ChatTap.Entry> getFilteredEntries() {
        return switch (currentFilter) {
            case ALL -> allEntries;
            case VIOLATIONS_ONLY -> allEntries.stream()
                    .filter(e -> "VIOLATION".equals(e.kind) || "AC".equals(e.kind))
                    .toList();
            case CHECKS_ONLY -> allEntries.stream()
                    .filter(e -> "CHECK".equals(e.kind))
                    .toList();
            case SERIOUS_ONLY -> allEntries.stream()
                    .filter(e -> {
                        for (ChatTap.ViolationEntry v : violations) {
                            if (v.playerName != null && v.playerName.equals(e.playerName) && v.isSerious) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .toList();
        };
    }

    private void exportToFile() {
        // TODO: Реализовать экспорт в файл
        Minecraft.getInstance().gui.getChat().addMessage(
                Component.literal("§e[ScS] Экспорт в разработке...")
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        this.entryList.render(guiGraphics, mouseX, mouseY, partialTick);

        // Заголовок
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 0xFFFFFF);

        // Статистика
        String stats = String.format("Всего записей: %d | Нарушений: %d | Серьезных: %d",
                allEntries.size(), violations.size(),
                violations.stream().mapToInt(v -> v.isSerious ? 1 : 0).sum());
        guiGraphics.drawCenteredString(this.font, stats, this.width / 2, this.height - 80, 0xCCCCCC);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private class EntryList extends ObjectSelectionList<EntryWidget> {
        public EntryList(Minecraft minecraft, int width, int height, int y0, int y1) {
            super(minecraft, width, height, y0, y1);
        }

        public void addEntryPublic(EntryWidget entry) {
            this.addEntry(entry);
        }
    }

    private class EntryWidget extends ObjectSelectionList.Entry<EntryWidget> {
        private final ChatTap.Entry entry;

        public EntryWidget(ChatTap.Entry entry) {
            this.entry = entry;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean isMouseOver, float partialTick) {

            // Определяем цвет записи
            int color = switch (entry.kind) {
                case "CHECK" -> Config.parseColor(Config.checkColor, 0x00FF7F);
                case "AC", "VIOLATION" -> {
                    boolean isSerious = false;
                    for (ChatTap.ViolationEntry violation : ChatHistoryScreen.this.violations) {
                        if (violation.playerName != null && violation.playerName.equals(entry.playerName) && violation.isSerious) {
                            isSerious = true;
                            break;
                        }
                    }
                    yield isSerious ? 0xFF4444 : Config.parseColor(Config.violationColor, 0xFFA500);
                }
                default -> 0xCCCCCC;
            };

            // Подсвечиваем фон для серьезных нарушений
            boolean isSerious = false;
            for (ChatTap.ViolationEntry violation : ChatHistoryScreen.this.violations) {
                if (violation.playerName != null && violation.playerName.equals(entry.playerName) && violation.isSerious) {
                    isSerious = true;
                    break;
                }
            }

            if (isSerious) {
                guiGraphics.fill(left, top, left + width, top + height, 0x44FF0000);
            } else if (isMouseOver) {
                guiGraphics.fill(left, top, left + width, top + height, 0x44FFFFFF);
            }

            // Время
            String timestamp = entry.timestamp.atZone(ZoneId.systemDefault())
                    .format(DISPLAY_FORMATTER);

            // Основной текст
            String displayText = String.format("[%s] %s", timestamp, entry.text);
            if (entry.playerName != null) {
                displayText += String.format(" (Игрок: %s)", entry.playerName);
            }

            // Рисуем текст с переносом если нужно
            List<String> lines = wrapText(displayText, width - 8);
            for (int i = 0; i < lines.size() && i < 2; i++) { // максимум 2 строки
                guiGraphics.drawString(ChatHistoryScreen.this.font, lines.get(i),
                        left + 4, top + 2 + (i * 10), color);
            }
        }

        private List<String> wrapText(String text, int maxWidth) {
            List<String> result = new ArrayList<>();
            if (ChatHistoryScreen.this.font.width(text) <= maxWidth) {
                result.add(text);
                return result;
            }

            String[] words = text.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
                if (ChatHistoryScreen.this.font.width(testLine) <= maxWidth) {
                    if (currentLine.length() > 0) currentLine.append(" ");
                    currentLine.append(word);
                } else {
                    if (currentLine.length() > 0) {
                        result.add(currentLine.toString());
                        currentLine = new StringBuilder(word);
                    } else {
                        // Слово слишком длинное, обрезаем
                        result.add(word.substring(0, Math.min(word.length(), 50)) + "...");
                    }
                }
            }

            if (currentLine.length() > 0) {
                result.add(currentLine.toString());
            }

            return result;
        }

        @Override
        public Component getNarration() {
            return Component.literal(entry.text);
        }
    }
}
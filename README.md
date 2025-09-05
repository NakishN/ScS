# 🛡️ ScS Enhanced - Advanced Anti-Cheat Monitor + 🌯 Shaurma Clicker

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.3-green.svg)](https://minecraft.net)
[![Forge](https://img.shields.io/badge/Forge-53.1.2-orange.svg)](https://minecraftforge.net)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.java.net/projects/jdk/21/)
[![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red.svg)](LICENSE)

**ScS Enhanced** - это продвинутый клиентский мод для Minecraft 1.21.3, предназначенный для администраторов серверов. Мод автоматически отслеживает сообщения античита в чате, добавляет интерактивные кнопки для быстрых действий, предоставляет расширенный HUD для мониторинга нарушений в реальном времени, а также включает в себя систему шаурмы - мини-игру типа "кликер" для расслабления между проверками игроков.

## 📋 Содержание

- [Возможности](#-возможности)
- [Система шаурмы](#-система-шаурмы)
- [Установка](#-установка)
- [Использование](#-использование)
- [Конфигурация](#-конфигурация)
- [Структура проекта](#-структура-проекта)
- [Техническая реализация](#-техническая-реализация)
- [API и расширения](#-api-и-расширения)
- [Сборка](#-сборка)
- [Вклад в проект](#-вклад-в-проект)
- [Лицензия](#-лицензия)

## 🚀 Возможности

### 🎯 Интерактивные кнопки в чате
- **Автоматическое добавление кнопок** к сообщениям античита
- **[Проверить]** - выполняет `/freezing [игрок]` для заморозки игрока
- **[Спек]** - выполняет `/matrix spectate [игрок]` для наблюдения (повторно для выхода)
- **Hover-подсказки** с информацией о действии
- **Цветовая индикация** по типам нарушений

### 📊 Продвинутый HUD
- **Настраиваемое позиционирование** и размеры
- **Реальное время** отображения нарушений
- **Цветовая классификация**: 
  - 🟢 Зеленый - проверки администрации
  - 🟡 Оранжевый - обычные нарушения
  - 🔴 Красный - серьезные нарушения (KillAura, AutoBot)
- **Временные метки** и статистика
- **Полупрозрачный дизайн** с настраиваемыми цветами

### ⌨️ Горячие клавиши (Античит)
- **F8** - переключение видимости HUD
- **F9** - открытие экрана истории нарушений
- **F10** - очистка всех записей

### 📈 Система мониторинга
- **Автоматическое логирование** в `logs/scs-chat.log`
- **Классификация нарушений** по степени серьезности
- **История с фильтрацией** (все, только нарушения, только проверки, серьезные)
- **Экспорт данных** для анализа
- **Звуковые уведомления** для критичных событий

## 🌯 Система шаурмы

### 🎮 Игровая механика
- **Кликер-игра** в стиле Hamster Kombat
- **Базовая награда**: 1 шаурма за тап
- **Бонусная система**: 15% шанс на x2, x3, x5, x10 шаурмы
- **Красивые эффекты**: анимации, звуки, летающие числа
- **Система достижений** с прогрессом

### 🎨 Визуальные эффекты
```
✨ ЛЕГЕНДАРНАЯ ШАУРМА! 🌯⭐ ✨
▶ +10 шаурмы! Всего: 152 🌯
🎉 ЭПИЧЕСКАЯ НАГРАДА! 🎉
```

### 🏆 Достижения
| Значок | Название | Описание | Требование |
|--------|----------|----------|------------|
| 🌯 | Первый тап | Сделай свой первый тап! | 1 тап |
| 💯 | Столетие | 100 тапов | 100 тапов |
| 🔥 | Тысячник | 1000 тапов | 1000 тапов |
| 📦 | Коллекционер | 100 шаурмы | 100 шаурмы |
| 💰 | Миллионер | 1000 шаурмы | 1000 шаурмы |
| 👑 | Олигарх | 10000 шаурмы | 10000 шаурмы |

### ⌨️ Горячие клавиши (Шаурма)
- **U** - тап шаурмы (работает везде, даже в игре)
- **Y** - открыть меню шаурмы
- **Пробел** - тап в меню (альтернатива)

### 📱 GUI меню шаурмы
- **Анимированный интерфейс** с градиентами в стиле cyberpunk
- **Большая кнопка тапа** в центре экрана
- **Статистика в реальном времени**: количество, тапы, среднее за тап
- **Панель достижений** с прогрессом
- **Летающие числа** при тапах (+1, +2, +5, +10)
- **Звуковые эффекты** для обычных и бонусных тапов

### 💾 Сохранение прогресса
- **Автосохранение** каждые 10 тапов
- **Файл сохранения**: `config/scs-shaurma.dat`
- **Сохраняется**: количество шаурмы, тапы, время последнего сохранения

## 📦 Установка

### Системные требования
- **Minecraft**: 1.21.3
- **Forge**: 53.1.2+
- **Java**: 21+
- **Память**: Минимум 4GB RAM для Minecraft

### Шаги установки
1. **Скачайте** Minecraft Forge 53.1.2+ для версии 1.21.3
2. **Поместите** файл `scs-enhanced-2.0.jar` в папку `mods/`
3. **Запустите** Minecraft с профилем Forge
4. **Настройте** мод через файл `config/scs-client.toml`

### Первый запуск
При первом запуске мод создаст:
- Конфигурационный файл `config/scs-client.toml`
- Файл сохранения шаурмы `config/scs-shaurma.dat`
- Лог-файл `logs/scs-chat.log`

## 🎮 Использование

### Автоматическое отслеживание античита
Мод автоматически отслеживает следующие типы сообщений:

```
[Анти-Чит] zigydeer tried to move abnormally (Move) #2
[Анти-Чит] player123 might be using combat hacks (KillAura)
[Анти-Чит] cheater456 suspected use of automatic robots (AutoBot)
Проверка успешно начата
Проверяемый игрок: suspicious_player
```

### Интерактивные действия
При появлении сообщения античита автоматически добавляются кнопки:
- Нажмите **[Проверить]** → выполнит `/freezing игрок`
- Нажмите **[Спек]** → выполнит `/matrix spectate игрок`

### Система шаурмы
1. **Нажмите U** в любое время для получения шаурмы
2. **Нажмите Y** для открытия полного меню с достижениями
3. **Наслаждайтесь** красивыми эффектами и звуками
4. **Соревнуйтесь** с друзьями в количестве шаурмы

### Навигация по HUD
- **Панель шаурмы** (верхняя) - количество, статистика, подсказки
- **Панель античита** (нижняя) - информация о нарушениях
- **Цветовая индикация** по критичности
- **Анимированные эффекты** для привлечения внимания

## ⚙️ Конфигурация

Конфигурационный файл: `config/scs-client.toml`

### Основные настройки

```toml
# HUD настройки
enableHud = true
hudX = -320              # Позиция по X (отрицательные значения от правого края)
hudY = 6                 # Позиция по Y
showLast = 15            # Количество отображаемых записей

# Цвета (HEX без #)
checkColor = "00FF7F"    # Цвет сообщений проверки
acColor = "FF4444"       # Цвет сообщений античита
violationColor = "FFA500" # Цвет обычных нарушений

# Интерактивность
enableChatButtons = true  # Включить кнопки в чате
autoCommands = false     # Автоматическое выполнение команд

# Звуки и уведомления
soundAlerts = true       # Звуковые уведомления
alertSound = "minecraft:block.note_block.bell"

# Логирование
enableLogging = true     # Логирование в файл
logAllChat = false       # Логировать весь чат
```

### Настройки системы шаурмы

```toml
# === Настройки системы шаурмы ===
enableShaurma = true           # Включить систему шаурмы
shaurmaHud = true             # Показывать панель шаурмы в HUD
shaurmaSounds = true          # Включить звуки для системы шаурмы
shaurmaBonusChance = 15       # Шанс бонуса при тапе (в процентах)
shaurmaBaseReward = 1         # Базовая награда за тап
shaurmaChatMessages = true    # Показывать сообщения шаурмы в чате
```

### Расширенные настройки

```toml
# Фильтры нарушений
violationKeywords = [
    "tried to move abnormally",
    "might be using combat hacks", 
    "suspected use of automatic robots",
    "tried to reach entity outside",
    "invalid movement",
    "speed hacks",
    "fly hacks"
]

maxMessages = 50         # Максимум записей в памяти
```

## 🏗️ Структура проекта

```
ScS-Enhanced/
├── src/main/java/com/scs/
│   ├── Scs.java                    # Главный класс мода (@Mod)
│   ├── Config.java                 # Система конфигурации (ForgeConfigSpec)
│   └── client/
│       ├── ChatTap.java            # Обработчик чата + античит мониторинг
│       ├── HudOverlay.java         # Рендеринг HUD (античит + шаурма)
│       ├── KeyBindings.java        # Горячие клавиши (F8-F10, U, Y)
│       ├── ChatButtonHandler.java  # Логика обработки команд кнопок
│       ├── ChatHistoryScreen.java  # GUI экран истории нарушений
│       ├── ShaurmaSystem.java      # Основная логика системы шаурмы
│       └── ShaurmaMenuScreen.java  # GUI меню шаурмы с достижениями
├── src/main/resources/
│   ├── META-INF/
│   │   └── mods.toml               # Манифест мода
│   ├── assets/scs/lang/
│   │   ├── en_us.json              # Английская локализация
│   │   └── ru_ru.json              # Русская локализация
│   ├── pack.mcmeta                 # Метаданные ресурспака
│   └── scs.mixins.json             # Конфигурация Mixin (резерв)
├── build.gradle                    # Система сборки Gradle + ForgeGradle
├── gradle.properties               # Версии и настройки проекта
├── README.md                       # Документация проекта
└── SHAURMA_GUIDE.md               # Подробное руководство по шаурме
```

## 🔧 Техническая реализация

### Архитектурные решения

#### 1. **Dual-System Architecture**
- **Античит модуль** - мониторинг, анализ, уведомления
- **Шаурма модуль** - игровая механика, GUI, достижения
- **Общий HUD** - интегрированное отображение обеих систем
- **Единая конфигурация** - централизованное управление настройками

#### 2. **Event-Driven Design**
```java
// Разные типы событий на разных шинах
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public static class ModEvents {
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        // Регистрация клавиш на MOD_EVENT_BUS
    }
}

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public static class ForgeEvents {
    @SubscribeEvent  
    public static void onKeyInput(InputEvent.Key event) {
        // Обработка нажатий на FORGE_EVENT_BUS
    }
}
```

#### 3. **Advanced HUD Rendering**
```java
@SubscribeEvent
public void onRenderHud(CustomizeGuiOverlayEvent event) {
    // Условный рендеринг панелей
    if (Config.enableShaurma && Config.shaurmaHud) {
        renderShaurmaPanel(g, x, y, w);
        y += shaurmaPanelHeight + 4; // Динамический сдвиг
    }
    
    // Анимированные эффекты
    float wave = Mth.sin(animationTick * 0.1f) * 0.2f + 0.8f;
    int bgColor = (int)(255 * wave * 0.7f) << 24 | 0x4A4A00;
}
```

### Используемые технологии

#### **Core Technologies**
- **Java 21** - современный LTS с record классами и switch expressions
- **Minecraft Forge 53.1.2** - мод-платформа для Minecraft 1.21.3
- **Gradle 8+** - система сборки с ForgeGradle plugin

#### **Forge API Integration**
```java
// События клиента (FORGE_EVENT_BUS)
ClientChatReceivedEvent     # Перехват сообщений чата
CustomizeGuiOverlayEvent    # Рендеринг HUD поверх игры
InputEvent.Key              # Обработка горячих клавиш

// События мода (MOD_EVENT_BUS)  
RegisterKeyMappingsEvent    # Регистрация клавиш
FMLClientSetupEvent         # Инициализация клиентской части

// Компоненты GUI
GuiGraphics                 # Современная система рендеринга
ObjectSelectionList        # Списки с прокруткой для истории
Component + Style           # Интерактивные текстовые компоненты

// Конфигурация
ForgeConfigSpec            # Типобезопасная система настроек
ModLoadingContext          # Контекст загрузки мода
```

#### **Game Design Patterns**
- **Clicker Game Mechanics** - базовые награды + бонусная система
- **Achievement System** - прогрессивные цели с визуальной обратной связью
- **Persistent Data** - сохранение прогресса между сессиями
- **Audio-Visual Feedback** - звуки, анимации, частицы

### Алгоритмы шаурма-системы

#### **Генерация наград**
```java
public static void onShaurmaTap() {
    int reward = Config.shaurmaBaseReward;
    
    if (random.nextInt(100) < Config.shaurmaBonusChance) {
        // Бонусная шаурма с экспоненциальным ростом
        int bonusIndex = random.nextInt(BONUS_MULTIPLIERS.length);
        reward *= BONUS_MULTIPLIERS[bonusIndex]; // [2, 3, 5, 10]
        
        playBonusSound(); // Специальные эффекты
        showBonusMessage();
    }
    
    shaurmaCount += reward;
    updateAchievements();
}
```

#### **Система достижений**
```java
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
```

#### **Персистентность данных**
```java
// Формат: шаурма:тапы:время_последнего_сохранения
String data = String.format("%d:%d:%d", shaurmaCount, totalTaps, System.currentTimeMillis());
Files.write(SAVE_FILE, data.getBytes(StandardCharsets.UTF_8));
```

### Система управления состоянием

#### **Dual-System State Management**
```java
// Общие данные античита
public static final Deque<Entry> ENTRIES = new ConcurrentLinkedDeque<>();
public static final Deque<ViolationEntry> VIOLATIONS = new ConcurrentLinkedDeque<>();

// Состояние шаурмы
private static long shaurmaCount = 0;
private static long totalTaps = 0;
private static final Random random = new Random();
```

#### **Configuration-Driven Behavior**
```java
// Условная активация систем
if (Config.enableShaurma) {
    if (SHAURMA_TAP.consumeClick()) {
        ShaurmaSystem.onShaurmaTap();
    }
}

if (Config.enableShaurma && Config.shaurmaHud) {
    renderShaurmaPanel(g, x, y, w);
}
```

## 📊 API и расширения

### Публичный API для разработчиков

```java
// Античит API
public static Deque<Entry> getChatEntries();
public static Deque<ViolationEntry> getViolations();
public static void clearEntries();
public static int getViolationCount(String playerName);

// Шаурма API
public static long getShaurmaCount();
public static long getTotalTaps();
public static double getAveragePerTap();
public static boolean hasAchievement(String achievement);
public static void onShaurmaTap(); // Программный тап
public static void resetData(); // Сброс прогресса

// HUD API
public static void toggleHud();
public static boolean isHudVisible();
public static void setHudVisible(boolean visible);
```

### События для интеграции

```java
// Кастомные события (можно добавить)
public class ShaurmaTapEvent extends Event {
    public final long newCount;
    public final int reward;
    public final boolean wasBonus;
}

public class AchievementUnlockedEvent extends Event {
    public final String achievementId;
    public final String playerName;
}
```

### Расширение функциональности

#### **Добавление новых достижений**
```java
private static final Achievement[] ACHIEVEMENTS = {
    new Achievement("custom_achievement", "Супер тапер", "1000000 тапов", "⭐", 1000000),
    // Добавляйте свои достижения
};
```

#### **Кастомные звуки и эффекты**
```java
// В конфигурации можно изменить звуки
alertSound = "minecraft:entity.experience_orb.pickup"
// Или добавить свои через ресурспак
```

## 🔨 Сборка

### Требования для разработки
- **JDK 21+** (рекомендуется Eclipse Temurin)
- **Git** для клонирования репозитория
- **IDE** с поддержкой Gradle (IntelliJ IDEA, Eclipse, VSCode)

### Команды сборки

```bash
# Клонирование репозитория
git clone https://github.com/your-username/ScS-Enhanced.git
cd ScS-Enhanced

# Сборка проекта
./gradlew build

# Запуск клиента для тестирования (рекомендуется)
./gradlew runClient

# Запуск сервера для тестирования
./gradlew runServer

# Генерация данных (если используются)
./gradlew runData

# Очистка build-артефактов
./gradlew clean
```

### Структура сборки
```
build/
├── libs/
│   ├── scs-enhanced-2.0.jar        # Готовый мод
│   └── scs-enhanced-2.0-sources.jar # Исходники
├── classes/
├── resources/
└── tmp/
```

### Настройки разработки

#### **IntelliJ IDEA**
1. Import Project → Gradle
2. Set Project SDK → Java 21
3. Enable Annotation Processing
4. Configure Run Configurations для `runClient`
5. Установить Minecraft Development Kit (опционально)

#### **Eclipse**
1. Import → Existing Gradle Project
2. Configure Java Build Path
3. Install Buildship Gradle plugin

### Отладка

#### **Рекомендуемый workflow**
```bash
# 1. Разработка с автоперезагрузкой
./gradlew runClient

# 2. Тестирование изменений без сборки jar
# Изменения в коде применяются автоматически

# 3. Финальная сборка для релиза
./gradlew build
cp build/libs/scs-enhanced-*.jar /path/to/minecraft/mods/
```

#### **Логирование для отладки**
```java
Scs.LOGGER.info("Shaurma tap: +{} (total: {})", reward, shaurmaCount);
Scs.LOGGER.warn("Failed to play tap sound: {}", e.getMessage());
Scs.LOGGER.debug("Processing chat message: {}", rawMessage);
```

## 🤝 Вклад в проект

### Как внести вклад

1. **Fork** репозитория
2. **Создайте** feature-ветку (`git checkout -b feature/amazing-feature`)
3. **Commit** изменения (`git commit -m 'Add amazing feature'`)
4. **Push** в ветку (`git push origin feature/amazing-feature`)
5. **Откройте** Pull Request

### Стандарты кода

#### **Java Code Style**
```java
// Используйте camelCase для методов и переменных
public void processViolation(String playerName) {
    
// Константы в UPPER_SNAKE_CASE
private static final String MOD_ID = "scs";

// Классы в PascalCase
public class ViolationEntry {
    
// Комментарии для публичных методов
/**
 * Processes anti-cheat violation and adds interactive buttons
 * @param playerName The name of the player who violated
 * @param violation The type of violation detected
 */
```

#### **Commit Convention**
```
feat: add shaurma clicker system with achievements
fix: resolve HUD positioning issues on different screen sizes  
docs: update README with shaurma system documentation
style: improve code formatting and comments
refactor: optimize violation detection algorithms
test: add unit tests for shaurma mechanics
perf: improve HUD rendering performance
```

### Области для вклада

#### **Приоритетные направления**
- 🎮 **Новые достижения** для системы шаурмы
- 🎨 **Улучшение UI/UX** меню и HUD
- 🔊 **Звуковые эффекты** и анимации
- 🌐 **Локализация** на другие языки
- 📊 **Статистика и аналитика** прогресса игроков
- 🏆 **Система лидербордов** (если планируется мультиплеер)

#### **Технические улучшения**
- ⚡ **Оптимизация производительности** рендеринга
- 🔧 **Расширение API** для сторонних разработчиков
- 🧪 **Юнит-тесты** для критичной логики
- 📱 **Адаптивный UI** для разных разрешений экранов

### Отчеты об ошибках

При создании issue включите:
- **Версию Minecraft** и **Forge**
- **Версию мода ScS Enhanced**
- **Лог-файлы** (`latest.log`, `debug.log`, `scs-chat.log`)
- **Шаги для воспроизведения**
- **Ожидаемое поведение** vs **фактическое**
- **Скриншоты** (особенно для UI багов)

### Предложения функций

Для новых функций создайте issue с тегом `enhancement`:
- **Описание функции** и варианты использования
- **Mockup или концепт** (для UI функций)
- **Примеры реализации** (если есть идеи)
- **Совместимость** с существующими функциями

## 📄 Лицензия

```
Copyright (c) 2024 nakish_

All Rights Reserved

Данное программное обеспечение и связанные с ним файлы документации ("Программное обеспечение") 
предоставляются исключительно для личного использования. Запрещается копирование, изменение, 
распространение, продажа или использование Программного обеспечения в коммерческих целях без 
письменного разрешения автора.

Система шаурмы является оригинальной игровой механикой, разработанной специально для этого мода
и не предназначена для коммерческого использования.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

## 🎯 Краткое руководство

### Быстрый старт (5 минут)
1. **Скачайте** и установите мод
2. **Запустите** Minecraft
3. **Нажмите U** для первого тапа шаурмы
4. **Нажмите Y** для открытия меню достижений
5. **Нажмите F8** для просмотра HUD

### Основные команды
- **U** - тап шаурмы 🌯
- **Y** - меню шаурмы 📱  
- **F8** - переключить HUD 👁️
- **F9** - история нарушений 📊
- **F10** - очистить данные 🗑️

### Полезные ссылки

- **[Minecraft Forge Documentation](https://docs.minecraftforge.net/)**
- **[Forge Community Wiki](https://forge.gemwire.uk/wiki/Main_Page)**
- **[ModCoderPack](https://github.com/MinecraftForge/MCPConfig)**
- **[Подробное руководство по шаурме](SHAURMA_GUIDE.md)**

---

*ScS Enhanced - делаем администрирование серверов проще, эффективнее и веселее! 🌯✨*

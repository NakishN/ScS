# 🛡️ ScS Enhanced - Advanced Anti-Cheat Monitor + 🌯 Shaurma Clicker + 🔍 DupeIP Integration

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.3-green.svg)](https://minecraft.net)
[![Forge](https://img.shields.io/badge/Forge-53.1.2-orange.svg)](https://minecraftforge.net)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.java.net/projects/jdk/21/)
[![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red.svg)](LICENSE)

**ScS Enhanced** - продвинутый клиентский мод для Minecraft 1.21.3, предназначенный для администраторов серверов. Мод автоматически отслеживает сообщения античита в чате, добавляет интерактивные кнопки для быстрых действий, предоставляет расширенный HUD для мониторинга нарушений в реальном времени, интегрируется с LuckyPerms DupeIP, а также включает в себя систему шаурмы - мини-игру типа "кликер" для расслабления между проверками игроков.

## 📋 Содержание

- [Возможности](#-возможности)
- [Система шаурмы](#-система-шаурмы)
- [DupeIP интеграция](#-dupeip-интеграция)
- [Система антиспама команд](#-система-антиспама-команд)
- [Установка](#-установка)
- [Использование](#-использование)
- [Конфигурация](#-конфигурация)
- [Команды](#-команды)
- [Структура проекта](#-структура-проекта)
- [Техническая реализация](#-техническая-реализация)
- [Сборка](#-сборка)
- [Лицензия](#-лицензия)

## 🚀 Возможности

### 🎯 Интерактивные кнопки в чате
- **Автоматическое добавление кнопок** к сообщениям античита
- **[Проверить]** - выполняет `/freezing [игрок]` для заморозки игрока
- **[Спек]** - выполняет `/matrix spectate [игрок]` для наблюдения (повторно для выхода)
- **[Активность]** - выполняет `/playeractivity [игрок]` для проверки активности
- **[История]** - выполняет `/freezinghistory [игрок]` для истории проверок
- **Hover-подсказки** с информацией о действии
- **Цветовая индикация** по типам нарушений

### 📊 Продвинутый HUD
- **Трёхпанельная система**:
  - Панель шаурмы (верхняя) - золотистая с анимациями
  - Панель античита (средняя) - основная информация о нарушениях
  - Панель DupeIP (нижняя) - временная, показывается 30 секунд
  - Панель чата игроков - последние 3 сообщения
- **Настраиваемое позиционирование** и размеры
- **Реальное время** отображения нарушений
- **Цветовая классификация**: 
  - 🟢 Зеленый - проверки администрации
  - 🟡 Оранжевый - обычные нарушения
  - 🔴 Красный - серьезные нарушения (KillAura, AutoBot)
  - 🔵 Синий - DupeIP сканирования
  - 🟣 Фиолетовый - чат игроков
- **Анимированные эффекты** - волновые фоны, мигающие рамки, пульсирующая подсветка
- **Временные метки** и статистика
- **Индикаторы типов записей**: ✓ ⚠ 🔍 👥 💬

### ⌨️ Горячие клавиши
**Античит управление:**
- **F8** - переключение видимости HUD
- **F9** - открытие экрана истории нарушений
- **F10** - очистка всех записей (включая шаурму)

**Система шаурмы:**
- **U** - тап шаурмы (работает везде, даже в игре)
- **Y** - открыть меню шаурмы (в разработке)

### 📈 Система мониторинга
- **Автоматическое логирование** в `logs/scs-chat.log`
- **Классификация нарушений** по степени серьезности
- **История с фильтрацией** (все, только нарушения, только проверки, серьезные, DupeIP)
- **Экспорт данных** для анализа (в разработке)
- **Звуковые уведомления** для критичных событий
- **Чат игроков** - отслеживание сообщений в специальных каналах

## 🌯 Система шаурмы

### 🎮 Игровая механика
- **Кликер-игра** в стиле Hamster Kombat
- **Базовая награда**: 1 шаурма за тап (настраиваемо)
- **Бонусная система**: 15% шанс на x2, x3, x5, x10 шаурмы (настраиваемо)
- **Красивые эффекты**: анимации, звуки, летающие числа
- **Система достижений** с прогрессом (6 достижений)

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

### 💾 Сохранение прогресса
- **Автосохранение** каждые 10 тапов
- **Файл сохранения**: `config/scs-shaurma.dat`
- **Формат**: `шаурма:тапы:время_последнего_сохранения`

## 🔍 DupeIP интеграция

### 📋 Автоматическое отслеживание LuckyPerms
Мод автоматически отслеживает команду `/dupeip` и её результаты:

**Сканирование:**
```
Сканирование nakish_. [Онлайн] [Оффлайн] [Забанен]
```

**Результаты:**
```
nakish_, kanawaka, opera2
```

### 🔧 Интерактивные кнопки для DupeIP

**Основная панель:**
- **[Копировать все]** - копирует все никнеймы в буфер обмена
- **[История всех]** - запускает `/history` для всех игроков с антиспамом
- **[Freezing история всех]** - запускает `/freezinghistory` для всех игроков

**Индивидуальные кнопки** (для первых 3 игроков):
- **[Проверить]** - `/freezing ник`
- **[История]** - `/history ник`
- **[F.История]** - `/freezinghistory ник`
- **[Активность]** - `/playeractivity ник`

### 📊 HUD интеграция
- **Временная панель** показывается 30 секунд после обнаружения DupeIP
- **Статистика** в основном HUD (количество DupeIP сканов)
- **Цветовая подсветка** синим цветом для DupeIP записей

## ⏱️ Система антиспама команд

### 🎯 CommandScheduler
- **Очередь команд** с настраиваемой задержкой (по умолчанию 1.2 секунды)
- **Массовые операции** для DupeIP результатов
- **Защита от спама** команд на сервере
- **Визуальная обратная связь** о статусе очереди

### 🔧 Принцип работы
```java
// Пример: при нажатии [История всех] для "nakish_, kanawaka, opera2"
Очередь команд:
1. /history nakish_    (выполнится сразу)
2. /history kanawaka   (через 1.2 сек)
3. /history opera2     (через 2.4 сек)
```

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

### DupeIP workflow
1. **Администратор** выполняет `/dupeip игрок`
2. **Мод отслеживает** сканирование и результаты
3. **Автоматически добавляются** интерактивные кнопки
4. **Массовые команды** выполняются с антиспамом

### Система шаурмы
1. **Нажмите U** в любое время для получения шаурмы
2. **Наслаждайтесь** красивыми эффектами и звуками
3. **Разблокируйте** достижения прогрессивно
4. **Соревнуйтесь** с друзьями в количестве шаурмы

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
[shaurma]
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

## 💻 Команды

### Специальные команды ScS
```bash
/scs:help                                    # Показать справку по командам
/scs:history_all ник1,ник2,ник3             # История всех игроков с задержкой
/scs:freezing_history_all ник1,ник2         # Freezing история всех игроков
/scs:clear_queue                             # Очистить очередь команд
/scs:delay 1500                              # Установить задержку между командами (мс)
```

### Примеры использования
```bash
# Проверить историю всех из DupeIP результата
/scs:history_all nakish_,kanawaka,opera2

# Установить задержку 2 секунды между командами
/scs:delay 2000

# Очистить очередь если накопилось много команд
/scs:clear_queue
```

### Информационные команды
- Текущая очередь отображается в `/scs:help`
- Статус выполнения команд показывается в чате
- Ошибки команд логируются и отображаются

## 🏗️ Структура проекта

```
ScS-Enhanced/
├── src/main/java/com/scs/
│   ├── Scs.java                      # Главный класс мода (@Mod)
│   ├── Config.java                   # Система конфигурации (ForgeConfigSpec)
│   └── client/
│       ├── ChatTap.java              # Обработчик чата + античит + DupeIP мониторинг
│       ├── HudOverlay.java           # Рендеринг HUD (античит + шаурма + DupeIP)
│       ├── KeyBindings.java          # Горячие клавиши (F8-F10, U, Y)
│       ├── ChatButtonHandler.java    # Логика обработки команд кнопок
│       ├── ChatHistoryScreen.java    # GUI экран истории нарушений
│       ├── ShaurmaSystem.java        # Основная логика системы шаурмы
│       ├── CommandScheduler.java     # Система антиспама команд
│       └── CommandHandler.java       # Обработчик специальных команд /scs:
├── src/main/resources/
│   ├── META-INF/
│   │   └── mods.toml                 # Манифест мода
│   ├── assets/scs/lang/
│   │   ├── en_us.json                # Английская локализация
│   │   └── ru_ru.json                # Русская локализация
│   ├── pack.mcmeta                   # Метаданные ресурспака
│   └── scs.mixins.json               # Конфигурация Mixin (резерв)
├── build.gradle                      # Система сборки Gradle + ForgeGradle
├── gradle.properties                 # Версии и настройки проекта
└── README.md                         # Документация проекта
```

## 🔧 Техническая реализация

### Архитектурные решения

#### 1. **Multi-System Architecture**
- **Античит модуль** - мониторинг, анализ, уведомления
- **Шаурма модуль** - игровая механика, GUI, достижения
- **DupeIP модуль** - интеграция с LuckyPerms, массовые команды
- **CommandScheduler** - антиспам система для команд
- **Общий HUD** - интегрированное отображение всех систем

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
    // Многопанельная архитектура
    int currentY = y;
    renderShaurmaPanel(g, x, currentY, w, font);
    currentY += 50;
    
    renderMainPanel(g, x, currentY, w, mainPanelHeight, entriesToShow, font);
    currentY += mainPanelHeight + 6;
    
    // Условный рендеринг дополнительных панелей
    if (latestDupeIP != null && isRecent(latestDupeIP)) {
        renderDupeIPPanel(g, x, currentY, w, latestDupeIP, font);
    }
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
ClientChatEvent             # Перехват команд клиента

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
- **Command Queuing** - антиспам система с задержками
- **Event Interception** - перехват и модификация игровых событий

### Алгоритмы и системы

#### **DupeIP Parser Algorithm**
```java
// Двухэтапный парсинг
1. Detect scan: "Сканирование" + player name extraction
2. Detect results: comma-separated list validation

if (text.contains("Сканирование")) {
    String[] words = text.split("\\s+");
    // Find player name after "Сканирование"
    lastScannedPlayer = extractPlayerName(words);
}

if (lastScannedPlayer != null && text.contains(",")) {
    List<String> players = parsePlayerList(text);
    if (players.size() >= 2) {
        processDupeIPResult(lastScannedPlayer, players);
    }
}
```

#### **Command Scheduling System**
```java
public class CommandScheduler {
    private static final Queue<ScheduledCommand> commandQueue = new ConcurrentLinkedQueue<>();
    private static long lastCommandTime = 0;
    private static int commandDelay = 1200; // 1.2 seconds
    
    // Выполнение команд с антиспамом
    private void processCommandQueue() {
        if (System.currentTimeMillis() - lastCommandTime >= commandDelay) {
            ScheduledCommand next = commandQueue.poll();
            if (next != null) {
                executeCommand(next);
                lastCommandTime = System.currentTimeMillis();
            }
        }
    }
}
```

#### **Shaurma System Algorithm**
```java
public static void onShaurmaTap() {
    int reward = Config.shaurmaBaseReward;
    
    if (random.nextInt(100) < Config.shaurmaBonusChance) {
        int multiplier = BONUS_MULTIPLIERS[random.nextInt(BONUS_MULTIPLIERS.length)];
        reward *= multiplier;
        showBonusEffects(reward, multiplier);
    }
    
    shaurmaCount += reward;
    totalTaps++;
    checkAchievements();
    
    if (totalTaps % 10 == 0) saveData();
}
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

### Отладка и разработка

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
Scs.LOGGER.info("[ScS] Shaurma tap: +{} (total: {})", reward, shaurmaCount);
Scs.LOGGER.warn("[ScS] Failed to play tap sound: {}", e.getMessage());
Scs.LOGGER.debug("[ScS] Processing chat message: {}", rawMessage);
```

#### **Тестирование DupeIP**
```bash
# В игре для тестирования:
1. Запустить /dupeip тест_игрок
2. Отправить в чат: "тест_игрок, другой_игрок, третий_игрок"
3. Проверить появление кнопок
4. Тестировать команды /scs:help
```

## 🎯 Краткое руководство

### Быстрый старт (5 минут)
1. **Скачайте** и установите мод
2. **Запустите** Minecraft
3. **Нажмите U** для первого тапа шаурмы
4. **Нажмите F8** для просмотра HUD
5. **Попробуйте** `/scs:help` для справки по командам

### Основные команды и клавиши
```
U                    - тап шаурмы 🌯
Y                    - меню шаурмы 📱  
F8                   - переключить HUD 👁️
F9                   - история нарушений 📊
F10                  - очистить данные 🗑️

/scs:help            - справка по командам
/scs:clear_queue     - очистить очередь команд
/scs:delay 1500      - установить задержку команд
```

### Workflow для администраторов
1. **Античит срабатывает** → автоматически появляются кнопки [Проверить] [Спек] [Активность] [История]
2. **DupeIP сканирование** → появляются кнопки массовых проверок с антиспамом
3. **История и мониторинг** → F9 для просмотра всех записей с фильтрацией
4. **Расслабление** → U для тапа шаурмы между проверками

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

## 🔗 Полезные ссылки

- **[Minecraft Forge Documentation](https://docs.minecraftforge.net/)**
- **[Forge Community Wiki](https://forge.gemwire.uk/wiki/Main_Page)**
- **[LuckyPerms Documentation](https://luckperms.net/wiki)**
- **[Matrix Anti-Cheat](https://www.mc-market.org/resources/4757/)**

---

**Статистика проекта:**
- **Строк кода**: ~2500+
- **Файлов**: 10+ Java классов
- **Функций**: 50+ методов
- **Возможностей**: Античит + DupeIP + Шаурма + Антиспам

*ScS Enhanced - делаем администрирование серверов проще, эффективнее и веселее!* 🌯✨🛡️

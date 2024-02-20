package com.filiahin.home.settings;

import java.util.List;

public class Settings {
    public static List<Setting<?>> values() {
        return List.of(
                Settings.MORNING_OPEN,
                Settings.DINNER_CLOSE,
                Settings.EVENING_CLOSE,
                Settings.CLOSE_BRIGHT_BLINDS,
                Settings.NOTIFY_EVERY_HALF_HOUR,
                Settings.NOTIFY_BEFORE_BLACK_ZONE,
                Settings.BOOST_BATHROOM_FLOOR,
                Settings.AUTO_WATER_PUMP,
                Settings.LOG_ELECTRICITY_ON
        );
    }
    public static final Setting<Boolean> MORNING_OPEN =
            new Setting<>("morningOpen", "Открыть все шторы утром", false);
    public static final Setting<Boolean> DINNER_CLOSE =
            new Setting<>("dinnerClose", "Закрыть шторы в гостинной до обеда", false);
    public static final Setting<Boolean> EVENING_CLOSE =
            new Setting<>("eveningClose", "Закрыть все шторы вечером", false);

    public static final Setting<Boolean> CLOSE_BRIGHT_BLINDS =
            new Setting<>("closeBrightBlinds", "Закрыть шторы по освещенности", false);
    public static final Setting<Boolean> NOTIFY_EVERY_HALF_HOUR =
            new Setting<>("notifyEveryHalfHour", "Электричество каждые 30 мин", false);

    public static final Setting<Boolean> NOTIFY_BEFORE_BLACK_ZONE =
            new Setting<>("notifyBeforeBlackZone", "Электричество перед черной зоной", false);

    public static final Setting<Boolean> BOOST_BATHROOM_FLOOR =
            new Setting<>("boostBathroomFloor", "Теплый пол на макс. температуру", true);

    public static final Setting<Boolean> AUTO_WATER_PUMP =
            new Setting<>("autoWaterPump", "Автоматический насос рециркуляции", true);

    public static final Setting<Boolean> AUTO_CONDITIONER_OFF =
            new Setting<>("autoConditionerOff", "Автоматическое выключение кондиционеров", true);

    public static final Setting<Boolean> LOG_ELECTRICITY_ON =
            new Setting<>("logElectricityOn", "Логирование электричества", false);
}

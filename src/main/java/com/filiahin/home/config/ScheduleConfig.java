package com.filiahin.home.config;

import com.filiahin.home.HomeApplication;
import com.filiahin.home.airconditioner.services.GreeAirconditionerService;
import com.filiahin.home.climate.ClimateService;
import com.filiahin.home.elan.services.ElanBlindsService;
import com.filiahin.home.elan.services.ElanControlsService;
import com.filiahin.home.electro.ElectroService;
import com.filiahin.home.qbittorrent.QBittorrentService;
import com.filiahin.home.settings.SettingsService;
import com.filiahin.home.status.HomeStatusService;
import com.filiahin.home.telegram.TelegramService;
import com.filiahin.home.telegram.TelegramSmartHomeService;
import com.filiahin.home.telegram.YouTubeNotificationBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Set;

@Configuration
@EnableScheduling
public class ScheduleConfig {

    @Autowired
    private ElanBlindsService elanBlindsService;

    @Autowired
    private ElanControlsService elanControlsService;
    @Autowired
    private TelegramService telegramService;
    @Autowired
    private YouTubeNotificationBot youTubeNotificationBot;

    @Autowired
    private TelegramSmartHomeService telegramSmartHomeService;

    @Autowired
    private ElectroService electroService;

    @Autowired
    private GreeAirconditionerService airconditionerService;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private ClimateService climateService;

    @Autowired
    private QBittorrentService qBittorrentService;

    @Autowired
    private HomeStatusService homeStatusService;

    // At 10:00 every day Monday-Friday
    @Scheduled(cron = "0 00 10 * * 1-5")
    public void openAllBlinds() {
        elanBlindsService.openAllBlinds();
    }

    // At 15:00 every day
    @Scheduled(cron = "0 0 15 * * *")
    public void closeAllBlinds() {
        elanBlindsService.closeAllBlinds();
        homeStatusService.notifyDay(climateService.get());
    }

    // At 13:00 every day
    @Scheduled(cron = "0 0 13 * * *")
    public void dinnerTime() {
        elanBlindsService.closeLivingRoom();
    }

    // Every 30 minutes
    @Scheduled(cron = "0 0/30 * * * *")
//    @Scheduled(cron = "0/10 * * * * *")
    public void serverIsAvailable() {
        if (settingsService.notifyEveryHalfHour()) {
            telegramService.sendBroadcastMessage("I am alive!");
        }
    }

    // Every 1 minute
    @Scheduled(cron = "0/60 * * * * *")
    public void electroIsAvailable() {
        if (settingsService.logElectricityOn()) {
            electroService.log();
        }
    }

    // At 02:00 every day
    @Scheduled(cron = "0 0 2 * * *")
    public void turnPumpOff() {
        if (settingsService.autoWaterPump()) {
            elanControlsService.pumpOff();
        }

        if (settingsService.autoConditionerOff()) {
            airconditionerService.turnOffAllDevices();
        }
    }

    // At 07:00 every day
    @Scheduled(cron = "0 0 7 * * *")
    public void turnPumpOn() {
        if (settingsService.autoWaterPump()) {
            elanControlsService.pumpOn();
        }
        homeStatusService.notifyMorning(climateService.get());
    }

    // Every 5 minutes
    @Scheduled(cron = "0 0/5 * * * *")
    public void isTorrentFinished() {
        if (!HomeApplication.DEBUG) {
            Set<String> finished = qBittorrentService.checkFinished();
            if (!finished.isEmpty()) {
                telegramSmartHomeService.sendBroadcastMessage("Торренты загружены: " + String.join(", ", finished));
            }
        }
    }

    // At 23:00 every day
    @Scheduled(cron = "0 0 23 * * *")
    public void homeReport() {
        homeStatusService.notifyEvening(climateService.get());
        String textStatus = homeStatusService.textStatus();
        homeStatusService.clearData();
        telegramSmartHomeService.sendBroadcastMessage(textStatus);

        homeStatusService.startNightWatch((report) ->
                telegramSmartHomeService.sendBroadcastMessage(report));
    }

    // Every hour
//    @Scheduled(cron = "0 0/60 * * * *")
//    @Scheduled(cron = "0/60 * * * * *")
//    public void logClimate() {
//        climateService.log();
//    }

    // At 20:00 on day-of-month 27
    @Scheduled(cron = "0 0 20 27 * ?")
    public void notifyYouTube() {
        youTubeNotificationBot.sendBroadcastMessage("Сегодня 27-ое число, а значит настало время платить по счетам!");
    }
}

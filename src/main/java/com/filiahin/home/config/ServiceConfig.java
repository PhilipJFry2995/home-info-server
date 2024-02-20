package com.filiahin.home.config;

import com.filiahin.home.HomeApplication;
import com.filiahin.home.audio.AudioConverter;
import com.filiahin.home.audio.AudioGenerator;
import com.filiahin.home.climate.ApImageService;
import com.filiahin.home.climate.ClimateService;
import com.filiahin.home.elan.services.ElanBlindsService;
import com.filiahin.home.elan.services.ElanControlsService;
import com.filiahin.home.elan.services.ElanFloorService;
import com.filiahin.home.elan.services.ElanLedService;
import com.filiahin.home.electro.ElectroService;
import com.filiahin.home.nodes.NodesService;
import com.filiahin.home.notifications.WebSocketController;
import com.filiahin.home.qbittorrent.QBittorrentService;
import com.filiahin.home.settings.SettingsService;
import com.filiahin.home.status.HomeStatusService;
import com.filiahin.home.storage.StorageService;
import com.filiahin.home.telegram.DelayMessageStorage;
import com.filiahin.home.telegram.SettingsStorage;
import com.filiahin.home.telegram.NetworkService;
import com.filiahin.home.telegram.PhotoService;
import com.filiahin.home.telegram.TelegramService;
import com.filiahin.home.telegram.TelegramSmartHomeService;
import com.filiahin.home.telegram.TelegramStorage;
import com.filiahin.home.telegram.YouTubeNotificationBot;
import com.filiahin.home.windows.ShellyDoorWindowService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class ServiceConfig {
    @Bean
    public TelegramService telegramService() {
        TelegramService service = new TelegramService(telegramStorage(), messageStorage(), networkService(),
                photoService(), nodesService());
        photoService().registerListener(service);
        if (!HomeApplication.DEBUG) {
            try {
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(service);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        return service;
    }

    @Bean
    public TelegramSmartHomeService smartHomeService() {
        TelegramSmartHomeService service = new TelegramSmartHomeService();
        if (!HomeApplication.DEBUG) {
            try {
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(service);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        return service;
    }

    @Bean
    public YouTubeNotificationBot youTubeNotificationBot() {
        YouTubeNotificationBot bot = new YouTubeNotificationBot();
        if (!HomeApplication.DEBUG) {
            try {
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(bot);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        return bot;
    }

    @Bean
    public TelegramStorage telegramStorage() {
        return new TelegramStorage();
    }

    @Bean
    public DelayMessageStorage messageStorage() {
        return new DelayMessageStorage();
    }

    @Bean
    public NetworkService networkService() {
        return new NetworkService();
    }

    @Bean
    public ElectroService electroService() {
        return new ElectroService(networkService(), telegramService());
    }

    @Bean
    public PhotoService photoService() {
        return new PhotoService(webcamWebSocketController());
    }

    @Bean
    public ElanBlindsService elanBlindsService() {
        return new ElanBlindsService();
    }

    @Bean
    public ElanLedService elanLedService() {
        return new ElanLedService();
    }

    @Bean
    public ElanFloorService elanFloorService() {
        return new ElanFloorService(electroService());
    }

    @Bean
    public ElanControlsService elanControlsService() {
        return new ElanControlsService();
    }

    @Bean
    public SettingsService settingsService() {
        return new SettingsService();
    }

    @Bean
    public SettingsStorage settingsStorage() {
        return new SettingsStorage();
    }

    @Bean
    public NodesService nodesService() {
        return new NodesService();
    }

    @Bean
    public WebSocketController webcamWebSocketController() {
        return new WebSocketController();
    }

    @Bean
    public ClimateService climateService() {
        return new ClimateService(nodesService());
    }

    @Bean
    public ApImageService appImageService() {
        return new ApImageService();
    }

    @Bean
    public AudioConverter audioConverter() {
        return new AudioConverter();
    }

    @Bean
    public AudioGenerator audioGenerator() {
        return new AudioGenerator();
    }

    @Bean
    public HomeStatusService homeStatusService() {
        return new HomeStatusService();
    }

    @Bean
    public ShellyDoorWindowService shellyDoorWindowService() {
        return new ShellyDoorWindowService();
    }

    @Bean
    public QBittorrentService qBittorrentService() {
        return new QBittorrentService();
    }

    @Bean
    public StorageService storageService() {
        return new StorageService();
    }
}

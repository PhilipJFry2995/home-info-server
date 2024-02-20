package com.filiahin.home.telegram;

import com.filiahin.home.electro.ElectroEventsListener;
import com.filiahin.home.exceptions.NoWebcamException;
import com.filiahin.home.nodes.NodesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TelegramService extends TelegramLongPollingBot implements PhotoService.MotionDetectedListener, ElectroEventsListener {
    private static final Logger logger = LoggerFactory.getLogger(TelegramService.class);
    public static final String BOT_USERNAME = "";
    public static final String BOT_TOKEN = "";
    private static final Set<String> PHOTO_COMMAND = Set.of("фото", "photo", "світлина");
    private static final String USERS_COMMAND = "users";
    private static final String DETECT_MOTION = "alarm";
    private static final long DELAY = 300000;
    private final Timer timer = new Timer("TelegramPhotoTimer");
    private boolean isTimerRunning = false;
    private final TelegramStorage telegramStorage;
    private final DelayMessageStorage messageStorage;
    private final NetworkService networkService;
    private final PhotoService photoService;
    private final NodesService nodesService;

    public TelegramService(TelegramStorage telegramStorage, DelayMessageStorage messageStorage, NetworkService networkService,
                           PhotoService photoService, NodesService nodesService) {
        this.telegramStorage = telegramStorage;
        this.messageStorage = messageStorage;
        this.networkService = networkService;
        this.photoService = photoService;
        this.nodesService = nodesService;
    }

    public void sendBroadcastMessage(String text) {
        telegramStorage.loadChats().forEach(chat -> {
            if (networkService.isUserAtHome(chat)) {
                return;
            }
            if (!networkService.isIpReachable(NetworkService.ROUTER_IP)) {
                return;
            }
            sendMessage(chat.getChatId(), chat.getName() + ", " + text);
        });
    }

    public void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error(e.getMessage());
        }
    }

    public void sendAdminMessage(String text) {
        telegramStorage.loadChats().stream()
                .filter(chat -> chat.getStatus() == TelegramStatus.ADMIN)
                .forEach(chat ->
                        sendMessage(chat.getChatId(), text));
    }

    public void sendPhoto(String chatId) {
        try {
            sendPhoto(chatId, new File(photoService.photo()));
        } catch (NoWebcamException e) {
            sendMessage(chatId, "No webcam on main node");
        }
    }

    public void sendPhoto(String chatId, File photo) {
        SendPhoto sendPhoto = SendPhoto.builder()
                .chatId(chatId)
                .photo(new InputFile(photo))
                .build();

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendDarkTimeMessage(LocalDateTime now, LocalDateTime lastAvailable) {
        long minutes = ChronoUnit.MINUTES.between(lastAvailable, now);
        if (minutes < 5) {
            return;
        }
        String duration =  minutes/24/60 + "d " + minutes/60%24 + "h " + minutes%60 + "m";
        String message = "Electricity was off from " + lastAvailable.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                + " till " + now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
                "\n" + duration;
        logger.info(message);
        sendBroadcastMessage(message);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            logger.info("Message received: " + update.getMessage().getText());
            String chatId = update.getMessage().getChatId().toString();
            String text = update.getMessage().getText();

            Optional<TelegramStorage.TelegramStorageEntity> clientOpt =
                    telegramStorage.loadChats().stream()
                            .filter(chat -> Objects.equals(chat.getChatId(), chatId))
                            .findFirst();

            if (clientOpt.isEmpty()) {
                sendAdminMessage("Пользователь " + update.getMessage().getFrom() + " хочет добавиться в чат");
                return;
            }

            if (TelegramStatus.GUEST.equals(clientOpt.get().getStatus())) {
                sendMessage(chatId, "Снова здравствуй, " + clientOpt.get().getName());
                return;
            }

            sendMessage(chatId, "Привет, " + clientOpt.get().getName());
            if (PHOTO_COMMAND.stream()
                    .anyMatch(command -> command.equalsIgnoreCase(text.strip()))) {
                try {
                    sendPhoto(chatId);
                } catch (NullPointerException | NoWebcamException e) {
                    sendMessage(chatId, "webcam is not available");
                }
                return;
            }

            if (DETECT_MOTION.equals(text)) {
                boolean detection = telegramStorage.switchDetection();
                sendMessage(chatId, "Слежение включено: " + detection);
                return;
            }

            if (USERS_COMMAND.equals(text)) {
                sendMessage(chatId, telegramStorage.loadChats().stream()
                        .map(TelegramStorage.TelegramStorageEntity::toString)
                        .collect(Collectors.joining("\n")));
            }

            String helpText = PHOTO_COMMAND + " - сделать фото с вебкамеры\n"
                    + DETECT_MOTION + " - включить/выключить слежение\n"
                    + USERS_COMMAND + " - посмотреть список пользователей";
            sendMessage(chatId, helpText);
        }
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void motionDetected(BufferedImage image) {
        if (!telegramStorage.isDetectionEnabled()) {
            return;
        }

        if (isTimerRunning) {
            return;
        }

        try {
            File file = new File(PhotoService.FILENAME);
            ImageIO.write(image, "JPG", file);
            logger.info("Sending photo to all chats");
            telegramStorage.loadChats().stream()
                    .filter(config -> TelegramStatus.ADMIN.equals(config.getStatus()))
                    .forEach(chat -> sendPhoto(chat.getChatId(), file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        isTimerRunning = true;

        TelegramTimerTask timerTask = new TelegramTimerTask();
        timer.schedule(timerTask, DELAY);
    }

    private class TelegramTimerTask extends TimerTask {
        @Override
        public void run() {
            isTimerRunning = false;
        }
    }

    @Override
    public void onElectricityOn() {
        logger.info("Electricity is on!");
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(() -> {
            sendBroadcastMessage("Electricity is on!");
        }, 60, TimeUnit.SECONDS);
    }

    @Override
    public void onElectricityOff() {
        logger.info("Electricity is off!");
        String message = "Electricity was off from " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                + " till ";
        messageStorage.addMessage(message);
    }
}

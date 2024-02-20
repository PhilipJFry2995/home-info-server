package com.filiahin.home.telegram;

import com.fasterxml.jackson.core.type.TypeReference;
import com.filiahin.home.storage.JsonStorage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class YouTubeNotificationBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(YouTubeNotificationBot.class);
    public static final String BOT_USERNAME = "";
    public static final String BOT_TOKEN = "";
    private static final String FILEPATH = "../../youtube_chats.json";
    private final JsonStorage<Subscribers> chatStorage = new JsonStorage<>(FILEPATH);
    
    public YouTubeNotificationBot() {
        File chatsFile = new File(FILEPATH);
        if (!chatsFile.exists()) {
            try {
                chatsFile.createNewFile();
                chatStorage.save(new Subscribers());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendBroadcastMessage(String text) {
        Optional<Subscribers> chats = chatStorage.load(new TypeReference<>() {
        });
        if (chats.isEmpty()) {
            logger.warn("No users found for YouTube bot");
            return;
        }

        chats.get().subs.forEach(chat -> {
            sendMessage(chat.chatId, chat.name + ", " + text);
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

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            logger.info("Message received: " + update.getMessage().getText());
            String chatId = update.getMessage().getChatId().toString();
            String text = update.getMessage().getText();
            String name = update.getMessage().getFrom().getUserName();

            Optional<Subscriber> chat = chatStorage.load(new TypeReference<>() {
                    })
                    .stream()
                    .flatMap(obj -> obj.subs.stream())
                    .filter(sub -> sub.chatId.equals(chatId))
                    .findFirst();

            if (chat.isEmpty()) {
                sendMessage(chatId, name + ", добро пожаловать в YouTube Premium Notification Bot! Оповещения будут приходить раз в месяц.");
                Optional<Subscribers> subs = chatStorage.load(new TypeReference<>() {
                });
                if (subs.isPresent()) {
                    subs.get().subs.add(new Subscriber(name, chatId));
                    chatStorage.save(subs.get());
                }
                return;
            }

            sendMessage(chatId, "Снова привет, " + name + "! Уже оплатил(ла)? =)");
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

    @Data
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Subscribers {
        List<Subscriber> subs = new ArrayList<>();
    }

    @Data
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Subscriber {
        private String name;
        private String chatId;
    }
}

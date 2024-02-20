package com.filiahin.home.telegram;

import com.filiahin.home.audio.AudioConverter;
import com.filiahin.home.audio.AudioGenerator;
import com.filiahin.home.audio.AudioMessage;
import com.filiahin.home.status.HomeStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileNotFoundException;
import java.util.Set;

public class TelegramSmartHomeService extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(TelegramSmartHomeService.class);
    private static final String BOT_USERNAME = "";
    private static final String BOT_TOKEN = "";
    private static final Set<String> ADMINS_CHAT_ID = Set.of();

    @Autowired
    private AudioConverter audioConverter;

    @Autowired
    private AudioGenerator audioGenerator;

    @Autowired
    private HomeStatusService homeStatusService;

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            logger.info("empty message received");
            return;
        }
        String chatId = update.getMessage().getChatId().toString();
        if (!ADMINS_CHAT_ID.contains(chatId)) {
            sendMessage(chatId, "Bot has private access only");
            return;
        }

        logger.info(update.getMessage().getText());

        if (update.getMessage().getText().equals("voice")) {
            String statusTextMessage = homeStatusService.voiceStatus();
            try {
                String audioPath = audioGenerator.generate(statusTextMessage);

                AudioMessage audioMessage = audioConverter.convert(audioPath);
                SendVoice sendVoice = new SendVoice();
                sendVoice.setChatId(chatId);
                sendVoice.setDuration((int) audioMessage.getDuration());
                sendVoice.setVoice(new InputFile(audioMessage.getFile()));
                try {
                    execute(sendVoice);
                } catch (TelegramApiException e) {
                    logger.error(e.getMessage());
                }
            } catch (FileNotFoundException | NullPointerException ex) {
                logger.error(ex.getMessage());
                sendMessage(chatId, "Audio message couldn't be created");
            }
        }
    }

    public void sendBroadcastMessage(String text) {
        ADMINS_CHAT_ID.forEach(chat -> sendMessage(chat, text));
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
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}

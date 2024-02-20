package com.filiahin.home.telegram;

import com.filiahin.home.nodes.NodesService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

class TelegramServiceTest {

    @Test
    void sendDarkTimeMessage() {
        TelegramStorage telegramStorage = Mockito.mock(TelegramStorage.class);
        DelayMessageStorage delayMessageStorage = Mockito.mock(DelayMessageStorage.class);
        NetworkService networkService = Mockito.mock(NetworkService.class);
        PhotoService photoService = Mockito.mock(PhotoService.class);
        NodesService nodesService = Mockito.mock(NodesService.class);
        TelegramService telegramService = new TelegramService(telegramStorage, delayMessageStorage, networkService,
                photoService, nodesService);

        telegramService.sendDarkTimeMessage(LocalDateTime.now(), LocalDateTime.parse("2023-02-20T00:00:01.7887706"));
    }
}
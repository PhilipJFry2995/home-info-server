package com.filiahin.home.qbittorrent;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QBittorrentServiceTest {

    @Test
    public void checkFinished() {
        QBittorrentRepository repo = Mockito.mock(QBittorrentRepository.class);
        Mockito.when(repo.torrents()).thenReturn(new ArrayList<>());

        QBittorrentService service = new QBittorrentService(repo);
        assertTrue(service.checkFinished().isEmpty());

        TorrentDto dto = new TorrentDto();
        dto.setName("torrent1");
        dto.setAmountLeft("100");
        List<TorrentDto> torrents = List.of(dto);
        Mockito.when(repo.torrents()).thenReturn(torrents);
        assertTrue(service.checkFinished().isEmpty());

        dto.setAmountLeft("0");
        assertEquals(Set.of("torrent1"), service.checkFinished());
        assertTrue(service.checkFinished().isEmpty());
    }
}
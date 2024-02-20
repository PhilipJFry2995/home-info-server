package com.filiahin.home.qbittorrent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class QBittorrentRepository {
    private static final Logger log = LogManager.getLogger(QBittorrentRepository.class);
    public List<TorrentDto> torrents() {
        try {
            return qbittorrents();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new NotFoundException("Torrents not loaded");
        }
    }

    private List<TorrentDto> qbittorrents() throws URISyntaxException, IOException, InterruptedException {
        String endpoint = QBittorrentService.API + "torrents/info";

        HttpRequest request = HttpRequest.newBuilder(new URI(endpoint))
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // log.info("Response for {}: {}", endpoint, response.statusCode());
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String body = response.body();
        return mapper.readValue(body, new TypeReference<>() {
        });
    }
}

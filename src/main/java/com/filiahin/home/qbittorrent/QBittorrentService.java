package com.filiahin.home.qbittorrent;

import com.filiahin.home.HomeApplication;
import com.google.common.annotations.VisibleForTesting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QBittorrentService {
    private static final Logger log = LogManager.getLogger(QBittorrentService.class);
    private static final String NO_AMOUNT = "0";
    public static final String API = "http://localhost:8092/api/v2/";

    private final Set<String> finishedTorrents = new HashSet<>();
    private final Set<String> activeTorrents = new HashSet<>();
    private final QBittorrentRepository repository;

    public QBittorrentService() {
        this.repository = new QBittorrentRepository();
        if (!HomeApplication.DEBUG) {
            this.repository.torrents().forEach(torrent -> {
                if (NO_AMOUNT.equals(torrent.getAmountLeft())) {
                    this.finishedTorrents.add(torrent.getHash());
                } else {
                    this.activeTorrents.add(torrent.getHash());
                }
            });
        }
    }

    @VisibleForTesting
    public QBittorrentService(QBittorrentRepository repo) {
        this.repository = repo;
        this.repository.torrents().forEach(torrent -> {
            if ("0".equals(torrent.getAmountLeft())) {
                finishedTorrents.add(torrent.getHash());
            } else {
                activeTorrents.add(torrent.getHash());
            }
        });
    }

    public List<TorrentDto> torrents() {
        return this.repository.torrents();
    }

    public void pause(String... hashes) {
        String endpoint = API + "torrents/pause";
        Map<String, String> content = Map.of("hashes", String.join("|", hashes));
        post(endpoint, content);
    }

    public void resume(String... hashes) {
        String endpoint = API + "torrents/resume";
        Map<String, String> content = Map.of("hashes", String.join("|", hashes));
        post(endpoint, content);
    }

    public void delete(boolean deleteFiles, String... hashes) {
        String endpoint = API + "torrents/delete";
        Map<String, ?> content = Map.of("hashes", String.join("|", hashes), "deleteFiles", deleteFiles);
        post(endpoint, content);
    }

    public void add(String... magnetUrl) {
        String endpoint = API + "torrents/add";
        Map<String, ?> content = Map.of("urls", String.join("|", magnetUrl));
        post(endpoint, content);
    }

    private void post(String url, Map<String, ?> content) {
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(url))
                    .header("Content-Type", "multipart/form-data; boundary=boundary")
                    .POST(HttpRequest.BodyPublishers.ofString(buildFormDataString(content), StandardCharsets.UTF_8))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Response for {}: {}", url, response.statusCode());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new NotFoundException("Torrent not paused");
        }
    }

    private String buildFormDataString(Map<String, ?> formData) {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, ?> entry : formData.entrySet()) {
            builder.append("--boundary\r\n");
            builder.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"\r\n\r\n");
            builder.append(entry.getValue()).append("\r\n");
        }

        builder.append("--boundary--\r\n");

        return builder.toString();
    }

    public Set<String> checkFinished() {
        Set<String> names = new HashSet<>();
        List<TorrentDto> qbittorrents = this.repository.torrents();
        qbittorrents.forEach(torrent -> {
            if (NO_AMOUNT.equals(torrent.getAmountLeft())) {
                if (!this.finishedTorrents.contains(torrent.getHash())
                        && this.activeTorrents.contains(torrent.getHash())) {
                    names.add(torrent.getName());
                    this.activeTorrents.remove(torrent.getHash());
                    this.finishedTorrents.add(torrent.getHash());
                    log.info("Torrent is finished: " + torrent.getName());
                }
            } else {
                log.info("Torrent is active: " + torrent.getName());
                this.activeTorrents.add(torrent.getHash());
            }
        });
        return names;
    }
}

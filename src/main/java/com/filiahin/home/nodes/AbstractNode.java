package com.filiahin.home.nodes;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@NoArgsConstructor
public abstract class AbstractNode {
    private final static Logger log = LogManager.getLogger(AbstractNode.class);
    private String ip;

    public AbstractNode(String ip) {
        this.ip = ip;
    }

    public Optional<ClimateDto> climate() {
        try {
            String climateUrl = ip + "/climate";
            HttpRequest request = HttpRequest.newBuilder(new URI(climateUrl))
                    .GET()
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Response for {}: {}", climateUrl, response.statusCode());
            if (response.statusCode() != HttpStatus.OK.value()) {
                return Optional.empty();
            }
            ObjectMapper mapper = new ObjectMapper();
            String body = response.body();
            return Optional.ofNullable(mapper.readValue(body, ClimateDto.class));
        } catch (URISyntaxException | IOException | InterruptedException e) {
            log.error("climate error: " + e.getMessage());
        }
        return Optional.empty();
    }
}

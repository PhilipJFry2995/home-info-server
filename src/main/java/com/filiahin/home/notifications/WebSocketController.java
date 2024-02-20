package com.filiahin.home.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class WebSocketController extends AbstractWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(WebSocketController.class.getName());
    private final Set<UserSocketSession> connections = new HashSet<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage ms) throws Exception {
        String messageJson = ms.getPayload();
        Map<String, String> messageMap = mapper.readValue(messageJson, new TypeReference<>() {
        });

        if (messageMap.get("action").equals("connect")) {
            log.info("new user connected to websocket " + messageMap.get("userId"));
            connections.add(new UserSocketSession(session, messageMap.get("userId")));
        } else if (messageMap.get("action").equals("disconnect")) {
            log.info("user disconnected from websocket " + messageMap.get("userId"));
            connections.remove(new UserSocketSession(session, messageMap.get("userId")));
        }
    }

    public boolean hasClients() {
        return !connections.isEmpty();
    }

    public void broadcast(Map<String, String> message) {
        try {
            broadcast((session) -> true, mapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void broadcast(String sender, Map<String, String> message) {
        try {
            broadcast((session) -> !session.getUserId().equals(sender), mapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void broadcast(Predicate<WebSocketController.UserSocketSession> filter, String message) {
        connections.stream()
                .filter(filter)
                .forEach(connection -> {
                            try {
                                connection.getSession().sendMessage(new TextMessage(message));
                            } catch (Exception e) {
                                // log.error(e.getMessage());
                                try {
                                    connection.getSession().close();
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        }
                );
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode
    private static final class UserSocketSession {
        WebSocketSession session;
        String userId;
    }
}

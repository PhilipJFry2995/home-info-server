package com.filiahin.home.windows;

import com.filiahin.home.notifications.WebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/window")
public class ShellyController implements WindowEvents {

    private final WebSocketController socketController;
    private final ShellyDoorWindowService service;

    @Autowired
    public ShellyController(ShellyDoorWindowService service, WebSocketController socketController) {
        this.service = service;
        service.registerListener(this);
        this.socketController = socketController;
    }

    @Override
    public void onWindowReport(Map<String, String> report) {
        socketController.broadcast(report);
    }

    @GetMapping("/{id}/state")
    public WindowReport state(@PathVariable String id) {
        Optional<WindowReport> report = this.service.report(id);
        if (report.isPresent()) {
            return report.get();
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find id " + id);
    }
}

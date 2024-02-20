package com.filiahin.home.nodes;

import com.filiahin.home.telegram.TelegramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/nodes")
public class NodesController {
    @Autowired
    private NodesService raspberryService;

    @Autowired
    private TelegramService telegramService;

    @GetMapping("/{room}/climate")
    public ClimateDto climate(@PathVariable int room) {
        Optional<ClimateDto> opt = raspberryService.climate(Room.room(room));
        if (opt.isPresent()) {
            return opt.get();
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find room or node " + Room.room(room));
    }
}

package com.filiahin.home.settings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/settings")
public class SettingsController {

    @Autowired
    private SettingsService service;

    @Autowired
    private ApplicationContext context;

    @GetMapping
    public List<Setting<?>> findAll() {
        return service.findAll();
    }

    @PostMapping
    public ResponseEntity<Void> set(@RequestBody Setting<?> setting) {
        if (setting.getKey() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        service.save(setting);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping("/shutdown-server")
    public void shutdown() {
        int exitCode = SpringApplication.exit(context, () -> 0);
        System.exit(exitCode);
    }
}

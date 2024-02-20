package com.filiahin.home.windows;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/shelly")
public class ShellyDoorWindowListener {
    private static final Logger log = LogManager.getLogger(ShellyDoorWindowListener.class);

    @Autowired
    private ShellyDoorWindowService service;

    @GetMapping("/{id}/opened")
    public void opened(@PathVariable String id) {
        log.info("opened " + id);
    }

    @GetMapping("/{id}/closed")
    public void closed(@PathVariable String id) {
        log.info("closed " + id);
    }

    @GetMapping("/report")
    public void report(@RequestParam Map<String, String> allRequestParams) {
        log.info("report:" + allRequestParams.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining(",")));
        service.onWindowReport(allRequestParams);
    }
}

package com.filiahin.home;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAutoConfiguration
@EnableScheduling
public class HomeApplication extends SpringBootServletInitializer {
    private final static Logger log = LogManager.getLogger(HomeApplication.class);
    public static boolean DEBUG = false;

    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            String dev = args[0];
            if (dev.equals("dev=true")) {
                log.info("Starting spring application in debug mode");
                DEBUG = true;
            }
        }
        SpringApplication.run(HomeApplication.class, args);
    }

}

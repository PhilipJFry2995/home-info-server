package com.filiahin.home.elan.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ElanLoginUtil {
    private final static Logger log = LogManager.getLogger(ElanLoginUtil.class);

    public static final String BASE_URL = "";
    public static final String BASE_API = BASE_URL + "/api";

    public static String cookie;
    private static LocalDateTime cookieDateTime;

    public static synchronized void tryLogin() throws URISyntaxException, IOException, InterruptedException {
        throw new RuntimeException("Implement API call");
    }

    public static synchronized void login() throws URISyntaxException, IOException, InterruptedException {
        throw new RuntimeException("Implement API call");
    }
}

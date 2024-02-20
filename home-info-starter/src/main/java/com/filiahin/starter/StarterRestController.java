package com.filiahin.starter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Stream;

@RestController
@RequestMapping
public class StarterRestController {
    private final static Logger log = LogManager.getLogger(StarterRestController.class);
    private Process homeServerProcess = null;

    @GetMapping("/restart")
    public void restart() throws URISyntaxException, IOException, InterruptedException {
        shutDown();
        startUp();
    }

    @GetMapping("/upgrade")
    public void upgrade() throws URISyntaxException, IOException, InterruptedException {
        shutDown();
        build();
        startUp();
    }

    private void shutDown() throws URISyntaxException, InterruptedException {
        String url = "http://localhost:8090/api/settings/shutdown-server";
        HttpRequest request = HttpRequest.newBuilder(new URI(url))
                .GET()
                .timeout(Duration.ofSeconds(2))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Response for {}: {}", url, response.statusCode());
        } catch (IOException ex) {
            log.warn("Server is not running");
        }
        if (homeServerProcess != null) {
            homeServerProcess.destroy();
        }
    }

    private void build() throws IOException {
        File currentDir = new File(".");
        File[] files = currentDir.listFiles();
        if (files == null) {
            log.error("No files in current directory");
            return;
        }

        String serverPath = "";
        if (Stream.of(files).anyMatch(file -> file.getPath().contains("pom.xml"))) {
            serverPath += "..";
        } else {
            serverPath += "..\\..";
        }
        File srcFileDir = new File(serverPath);

        ProcessBuilder builder = new ProcessBuilder();
        builder.inheritIO();
        builder.command("cmd.exe", "/c", "git pull");
        builder.directory(srcFileDir);
        Process gitProcess = builder.start();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        StreamGobbler streamGobbler =
                new StreamGobbler(gitProcess.getInputStream(), System.out::println);
        Future<?> future = executorService.submit(streamGobbler);
        try {
            int exitCode = gitProcess.waitFor();
            log.info("gitProcess exitCode:" + exitCode);
        }
        catch (InterruptedException e) {
            gitProcess.destroy();
            log.info("gitProcess exitCode:destroy");
        }

        builder = new ProcessBuilder();
        builder.inheritIO();
        builder.command("cmd.exe", "/c", "mvn clean install");
        builder.directory(srcFileDir);
        Process mavenProcess = builder.start();
        executorService = Executors.newSingleThreadExecutor();
        streamGobbler = new StreamGobbler(mavenProcess.getInputStream(), System.out::println);
        future = executorService.submit(streamGobbler);
        try {
            int exitCode = mavenProcess.waitFor();
            log.info("mavenProcess exitCode:" + exitCode);
        }
        catch (InterruptedException e) {
            mavenProcess.destroy();
            log.info("mavenProcess exitCode:destroy");
        }
    }

    private void startUp() throws IOException {
        File currentDir = new File(".");
        File[] files = currentDir.listFiles();
        if (files == null) {
            log.error("No files in current directory");
            return;
        }

        String serverPath = "";
        if (Stream.of(files).anyMatch(file -> file.getPath().contains("pom.xml"))) {
            serverPath += "..";
        } else {
            serverPath += "..\\..";
        }
        serverPath += "\\target";
        File jarFileDir = new File(serverPath);

        ProcessBuilder builder = new ProcessBuilder();
        builder.inheritIO();
        builder.command("cmd.exe", "/c", "java -jar home.jar");
        builder.directory(jarFileDir);
        homeServerProcess = builder.start();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        StreamGobbler streamGobbler =
                new StreamGobbler(homeServerProcess.getInputStream(), System.out::println);
        Future<?> future = executorService.submit(streamGobbler);
        try {

            int exitCode = homeServerProcess.waitFor();
            log.info("homeServerProcess exitCode:" + exitCode);
        }
        catch (InterruptedException e) {
            homeServerProcess.destroy();
            log.info("homeServerProcess exitCode:destroy");
        }
    }

    private static class StreamGobbler implements Runnable {
        private final InputStream inputStream;
        private final Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }
}

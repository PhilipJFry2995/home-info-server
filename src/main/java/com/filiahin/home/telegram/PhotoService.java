package com.filiahin.home.telegram;

import com.filiahin.home.exceptions.NoWebcamException;
import com.filiahin.home.notifications.WebSocketController;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDiscoveryEvent;
import com.github.sarxos.webcam.WebcamDiscoveryListener;
import com.github.sarxos.webcam.WebcamMotionDetector;
import com.github.sarxos.webcam.WebcamMotionEvent;
import com.github.sarxos.webcam.WebcamMotionListener;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.WebcamUtils;
import com.github.sarxos.webcam.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PreDestroy;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PhotoService implements WebcamMotionListener, WebcamDiscoveryListener {
    public static final String FILENAME = "webcam.jpg";
    private static final Logger logger = LoggerFactory.getLogger(PhotoService.class);
    private static final Dimension[] nonStandardResolutions = new Dimension[]{
            WebcamResolution.PAL.getSize(),
            WebcamResolution.HD.getSize(),
            new Dimension(1024, 768),
            new Dimension(1280, 1024),
    };
    private final List<MotionDetectedListener> listeners;
    private Webcam webcam = null;
    private WebcamMotionDetector detector = null;


    private WebSocketController webSocketController;

    @Autowired
    public PhotoService(WebSocketController webSocketController) {
        this.webSocketController = webSocketController;
        listeners = new ArrayList<>();

        List<Webcam> webcams = Webcam.getWebcams();
        for (Webcam webcam : webcams) {
            System.out.println("Webcam detected: " + webcam.getName());
        }
        logger.info("Webcams found: " + webcams.size());

        Webcam.addDiscoveryListener(this);

        if (!webcams.isEmpty()) {
            webcam = webcams.get(webcams.size() - 1);
            webcam.setCustomViewSizes(nonStandardResolutions);
            webcam.setViewSize(WebcamResolution.HD.getSize());
            webcam.open();
            detector = new WebcamMotionDetector(webcam);
            registerDetector();
        }

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::broadcastStream, 0, 40, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void onDestroy() {
        logger.info("onDestroy release camera");
        if (webcam != null) {
            webcam.close();
        }
    }

    public void registerListener(MotionDetectedListener listener) {
        this.listeners.add(listener);
    }

    public String photo() {
        if (webcam == null) {
            throw new NoWebcamException();
        }

        WebcamUtils.capture(webcam, FILENAME);
        return FILENAME;
    }

    public byte[] photoBytes() {
        if (webcam == null) {
            throw new NoWebcamException();
        }

        return WebcamUtils.getImageBytes(webcam, ImageUtils.FORMAT_PNG);
    }

    private void broadcastStream() {
        if (webSocketController.hasClients()) {
            String encodedString = Base64.getEncoder().encodeToString(photoBytes());
//            webSocketController.broadcast(encodedString);
        }
    }

    private void registerDetector() {
        detector.setInterval(500);
        detector.addMotionListener(this);
        detector.start();
    }

    @Override
    public void motionDetected(WebcamMotionEvent wme) {
        listeners.forEach(listener -> listener.motionDetected(wme.getCurrentImage()));
    }

    @Override
    public void webcamFound(WebcamDiscoveryEvent event) {
        logger.info("Webcam connected: " + event.getWebcam().getName());
        webcam = event.getWebcam();
        webcam.setCustomViewSizes(nonStandardResolutions);
        webcam.setViewSize(WebcamResolution.HD.getSize());
        webcam.open();
        detector = new WebcamMotionDetector(webcam);
        registerDetector();
    }

    @Override
    public void webcamGone(WebcamDiscoveryEvent event) {
        if (webcam.getName().equals(event.getWebcam().getName())) {
            logger.info("Webcam disconnected: " + event.getWebcam().getName());
            detector.stop();
            webcam = null;
        }
    }

    public interface MotionDetectedListener {
        void motionDetected(BufferedImage image);
    }
}

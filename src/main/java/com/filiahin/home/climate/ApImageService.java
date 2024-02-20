package com.filiahin.home.climate;

import com.filiahin.home.nodes.ClimateDto;
import com.filiahin.home.nodes.Room;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class ApImageService {
    private static final Color[] TEMPERATURE_RANGE_COLORS = {
            new Color(0, 0, 255),
            new Color(0, 0, 255),
            new Color(0, 0, 255),
            new Color(0, 0, 255),
            new Color(0, 127, 255),
            new Color(0, 191, 255),
            new Color(0, 255, 255),
            new Color(127, 255, 255),
            new Color(191, 255, 255),
            new Color(255, 255, 255),
            new Color(255, 255, 127),
            new Color(255, 255, 0),
            new Color(255, 215, 0),
            new Color(255, 191, 0),
            new Color(255, 165, 0),
            new Color(255, 140, 0),
            new Color(255, 115, 0),
            new Color(255, 89, 0),
            new Color(255, 64, 0),
            new Color(255, 38, 0),
            new Color(255, 13, 0),
            new Color(255, 0, 0),
            new Color(255, 0, 0),
            new Color(255, 0, 0),
    };

    private static final Map<Room, Coordinates> IMAGE_COORDINATES = Map.of(
            Room.LIVINGROOM, new Coordinates(600, 400, 600),
            Room.LIVINGROOM_BALCONY, new Coordinates(1000, 140, 200),
            Room.BEDROOM, new Coordinates(500, 1300, 600),
            Room.BEDROOM_BALCONY, new Coordinates(150, 1300, 300),
            Room.STUDY, new Coordinates(500, 830, 300)
    );

    public Optional<byte[]> image(ClimateRecord climateRecord) {
        try {
            Resource resource = new ClassPathResource("images/222.png");
            BufferedImage bufImage = ImageIO.read(resource.getInputStream());
            Graphics2D g2d = bufImage.createGraphics();

            Map<Room, ClimateDto> rooms = climateRecord.getDto();

            rooms.forEach((room, record) -> {
                Coordinates coord = IMAGE_COORDINATES.get(room);
                int index = (int) (record.getTemperature() - 12);
                drawOval(g2d, coord.x, coord.y, coord.r, TEMPERATURE_RANGE_COLORS[index]);
                drawText(g2d, coord.x, coord.y, record.getTemperature() + " C° " + record.getHumidity() + " %");
            });

            double averageTemperature = rooms.values()
                    .stream()
                    .mapToDouble(ClimateDto::getTemperature)
                    .average()
                    .orElse(Double.NaN);

            double averageHumidity = rooms.values()
                    .stream()
                    .mapToDouble(ClimateDto::getHumidity)
                    .average()
                    .orElse(Double.NaN);

            double maxTemperature = rooms.values()
                    .stream()
                    .mapToDouble(ClimateDto::getTemperature)
                    .max()
                    .orElse(Double.NaN);

            double maxHumidity = rooms.values()
                    .stream()
                    .mapToDouble(ClimateDto::getHumidity)
                    .max()
                    .orElse(Double.NaN);

            double minTemperature = rooms.values()
                    .stream()
                    .mapToDouble(ClimateDto::getTemperature)
                    .min()
                    .orElse(Double.NaN);

            double minHumidity = rooms.values()
                    .stream()
                    .mapToDouble(ClimateDto::getHumidity)
                    .min()
                    .orElse(Double.NaN);

            String[] statistics = new String[]{"Average temperature: " + String.format("%2.2f C°", averageTemperature),
                    "Average humidity: " + String.format("%2.2f ", averageHumidity) + "%",
                    "",
                    "Maximum temperature: " + String.format("%2.2f C°", maxTemperature),
                    "Maximum humidity: " + String.format("%2.2f ", maxHumidity) + "%",
                    "",
                    "Minimum temperature: " + String.format("%2.2f C°", minTemperature),
                    "Minimum humidity: " + String.format("%2.2f ", minHumidity) + "%",
            };

            drawText(g2d, 10, 10, 330, 200, statistics);

            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufImage, "png", baos);
            return Optional.of(baos.toByteArray());
        } catch (IOException e) {
            System.err.println(" " + e.getMessage());
        }
        return Optional.empty();
    }

    private void drawOval(Graphics2D g2d, int centerX, int centerY, int radius, Color color) {
        Color centerColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 178);
        Color outerColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);

        RadialGradientPaint gradient = new RadialGradientPaint(
                new Point2D.Float(centerX, centerY), radius,
                new float[]{0.0f, 1.0f}, new Color[]{centerColor, outerColor}
        );

        g2d.setPaint(gradient);

        int ovalX = centerX - radius;
        int ovalY = centerY - radius;
        g2d.fillOval(ovalX, ovalY, 2 * radius, 2 * radius);
    }

    private void drawText(Graphics2D g2d, int centerX, int centerY, String... lines) {
        int rectangleWidth = 150;
        int rectangleHeight = 50;
        drawText(g2d, centerX, centerY, rectangleWidth, rectangleHeight, lines);
    }

    private void drawText(Graphics2D g2d, int centerX, int centerY, int width, int height, String... lines) {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(centerX, centerY, width, height);

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(centerX, centerY, width, height);

        Font font = new Font("Arial", Font.BOLD, 20);
        g2d.setFont(font);

        g2d.setColor(Color.BLACK);

        FontMetrics fontMetrics = g2d.getFontMetrics(font);
        int lineHeight = fontMetrics.getHeight();

        int textX = centerX + (width - fontMetrics.stringWidth(lines[0])) / 2;
        int textY = centerY + (height - lines.length * lineHeight) / 2 + fontMetrics.getAscent();
        for (String line : lines) {
            g2d.drawString(line, textX, textY);
            textY += lineHeight; // Move to the next line
        }
    }

    @AllArgsConstructor
    private static final class Coordinates {
        int x;
        int y;
        int r;
    }
}

package com.filiahin.home.electro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Controller
public class ElectroController {

    @Autowired
    private ElectroService service;

    @GetMapping("/timeline")
    public String getTimelineChart(Model model) {
        ElectroService.ElectroEntities data = service.getData();
        if (data == null) {
            return "google-charts";
        }

        Map<String, Map<String, String>> graphData = new TreeMap<>();
        data.getDates().forEach(day ->
                day.getPeriods().forEach(period ->
                        graphData.computeIfAbsent(day.getDate(), v -> new HashMap<>())
                                .put(prepareDate(period.getKey()),
                                        Optional.ofNullable(period.getValue())
                                                .map(this::prepareDate)
                                                .orElse(prepareDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))));

        model.addAttribute("chartData", graphData);
        return "google-charts";
    }

    private String prepareDate(@NonNull String date) {
        return date.replaceAll(".+T", "2022-01-01T");
    }
}

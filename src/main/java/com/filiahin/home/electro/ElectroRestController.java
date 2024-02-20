package com.filiahin.home.electro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/electro")
public class ElectroRestController {
    @Autowired
    private ElectroService service;

    @GetMapping("/dates")
    public ElectroService.ElectroEntities dates() {
        return service.getData();
    }

    @PostMapping("/merge")
    public void merge(@RequestBody ElectroService.ElectroEntities dates) {
        this.service.merge(dates);
    }

    @GetMapping("/schedule")
    public ElectroService.ScheduleEntities schedule() {
        return service.getSchedule();
    }
}

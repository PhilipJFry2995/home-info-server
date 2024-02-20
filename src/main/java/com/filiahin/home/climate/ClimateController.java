package com.filiahin.home.climate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/climate-log")
public class ClimateController {
    @Autowired
    private ClimateService climateService;

    @Autowired
    private ApImageService imageService;

    @GetMapping("/{date}")
    public ClimateJson record(@PathVariable String date) {
        Optional<ClimateJson> climateJson = climateService.get(date);
        if (climateJson.isPresent()) {
            return climateJson.get();
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find room or date");
    }

    @GetMapping("/dates")
    public List<String> dates() {
        return climateService.dates();
    }

    @ResponseBody
    @GetMapping(value = "/222", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] image(@RequestParam Optional<String> date, @RequestParam Optional<String> time) {
        Optional<ClimateRecord> record;
        if (date.isPresent() && time.isPresent()) {
            record = climateService.get(date.get(), time.get());
        } else {
            record = Optional.of(climateService.get());
        }

        if (record.isPresent()) {
            Optional<byte[]> bytes = imageService.image(record.get());
            if (bytes.isPresent()) {
                return bytes.get();
            }
        }

        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create image");
    }

}

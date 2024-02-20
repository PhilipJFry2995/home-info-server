package com.filiahin.home.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/storage")
public class StorageController {
    @Autowired
    private StorageService storageService;

    @GetMapping
    public StorageDto get() {
        return storageService.getSpaceInfo();
    }

    @GetMapping("files")
    public List<FileDto> files(@RequestParam(required = false) String relativePath) {
        return storageService.getFilesList(relativePath);
    }

    @DeleteMapping
    public boolean deleteFile(String path) {
        return storageService.deleteFile(path);
    }

}

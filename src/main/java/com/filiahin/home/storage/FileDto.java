package com.filiahin.home.storage;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.thymeleaf.util.StringUtils;

import java.io.File;

@Data
@AllArgsConstructor
public class FileDto {
    public String filename;
    public String relativePath;
    public boolean isDirectory;
    public double size;

    public FileDto(File file) {
        this.filename = file.getName();
        this.relativePath = StringUtils.substringAfter(file.getAbsolutePath(), StorageService.DOWNLOADS);
        this.isDirectory = file.isDirectory();
        this.size = file.length();
    }
}

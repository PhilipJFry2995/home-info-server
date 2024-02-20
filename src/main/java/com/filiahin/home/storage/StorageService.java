package com.filiahin.home.storage;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StorageService {
    public static final String DOWNLOADS = "";
    private static final Logger log = LogManager.getLogger(StorageService.class);
    private static final long BYTE_IN_GIGABYTE = 1073741824L;

    public StorageDto getSpaceInfo() {
        File cDrive = new File("C:");
        return new StorageDto(
                (double) cDrive.getFreeSpace() / BYTE_IN_GIGABYTE,
                (double) cDrive.getTotalSpace() / BYTE_IN_GIGABYTE
        );
    }

    public List<FileDto> getFilesList(String path) {
        File[] files = new File(Optional.ofNullable(path)
                .map(p -> DOWNLOADS + "\\" + path)
                .orElse(DOWNLOADS))
                .listFiles();
        if (files == null) {
            return List.of();
        }
        return Arrays.stream(files)
                .map(FileDto::new)
                .collect(Collectors.toList());
    }

    public boolean deleteFile(String path) {
        File file = new File(DOWNLOADS + "\\" + path);
        if (file.isDirectory()) {
            try {
                FileUtils.deleteDirectory(file);
                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return file.delete();
        }
    }
}

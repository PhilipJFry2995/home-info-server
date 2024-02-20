package com.filiahin.home.qbittorrent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/torrent")
public class TorrentController {
    @Autowired
    private QBittorrentService qBittorrentService;

    @GetMapping
    public List<TorrentDto> torrents() {
        return qBittorrentService.torrents();
    }

    @PostMapping("/{hash}/pause")
    public void pause(@PathVariable String hash) {
        qBittorrentService.pause(hash);
    }

    @PostMapping("/{hash}/resume")
    public void resume(@PathVariable String hash) {
        qBittorrentService.resume(hash);
    }

    @DeleteMapping("/{hash}")
    public void delete(@PathVariable String hash, @RequestParam Boolean deleteFiles) {
        qBittorrentService.delete(deleteFiles, hash);
    }

    @PostMapping
    public void add(@RequestBody String magnetUrl) {
        qBittorrentService.add(magnetUrl);
    }
}

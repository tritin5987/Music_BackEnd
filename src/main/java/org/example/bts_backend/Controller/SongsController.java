package org.example.bts_backend.Controller;

import org.example.bts_backend.Models.Songs;
import org.example.bts_backend.Services.SongsService;
import org.example.bts_backend.dto.SongDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
public class SongsController {

    @Autowired
    private SongsService songsService;

    @GetMapping("/search")
    public ResponseEntity<List<Songs>> searchSongs(@RequestParam String keyword) {
        List<Songs> results = songsService.searchSongsByLyrics(keyword);
        return ResponseEntity.ok(results);
    }
//    @GetMapping("/titles")
//    public List<String> getAllSongTitles() {
//        return songsService.getAllSongTitles();
//    }
        @GetMapping
        public List<SongDTO> getAllSongs() {
        return songsService.getAllSongs(); // Gọi tới service để lấy dữ liệu
}

}



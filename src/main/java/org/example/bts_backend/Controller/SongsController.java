package org.example.bts_backend.Controller;

import org.example.bts_backend.Models.Songs;
import org.example.bts_backend.Services.SongsService;
import org.example.bts_backend.dto.SongDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/songs")
public class SongsController {

    @Autowired
    private SongsService songsService;

    // Tìm kiếm bài hát
    @GetMapping("/search")
    public List<SongDTO> searchSongs(@RequestParam String keyword) throws Exception {
        return songsService.searchSongsByKeyword(keyword);
    }

    @PostMapping("/index")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> indexSongs() {
        try {
            songsService.indexAllSongs();
            return ResponseEntity.ok("Indexing completed");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error during indexing");
        }
    }


    @GetMapping("/titles")
    public List<String> getAllSongTitles() {
        return songsService.getAllSongTitles();
    }
    @GetMapping
    public List<SongDTO> getAllSongs() {
        return songsService.getAllSongs(); // Gọi tới service để lấy dữ liệu
    }
}



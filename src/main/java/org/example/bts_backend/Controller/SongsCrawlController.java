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
import java.util.Map;

@RestController
@RequestMapping("/api/crawl")
public class SongsCrawlController {

    @Autowired
    private SongsService songsService;
    @GetMapping("")
    public Map<String, String> compareWithNCTPlaylist() {
        return songsService.compareSongsFromNCT();
    }
}



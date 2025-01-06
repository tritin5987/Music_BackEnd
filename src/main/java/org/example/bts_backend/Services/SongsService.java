package org.example.bts_backend.Services;

import org.example.bts_backend.Models.Songs;
import org.example.bts_backend.Repository.SongsRepository;
import org.example.bts_backend.dto.SongDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SongsService {

    @Autowired
    private SongsRepository songsRepository;

    public List<Songs> searchSongsByLyrics(String keyword) {
        return songsRepository.searchTop3ByLyrics(keyword);
    }
//    public List<String> getAllSongTitles() {
//        return songsRepository.findAllSongTitles();
//    }

    public List<SongDTO> getAllSongs() {
        return songsRepository.findAllSongs(); // Trả về danh sách các bài hát dưới dạng SongDTO
    }
}

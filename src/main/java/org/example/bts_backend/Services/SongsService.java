package org.example.bts_backend.Services;

import org.example.bts_backend.Models.Songs;
import org.example.bts_backend.Repository.SongsRepository;
import org.example.bts_backend.dto.SongDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.text.Normalizer;

@Service
public class SongsService {

    @Autowired
    private SongsRepository songsRepository;

    @Autowired
    private LuceneIndexer luceneIndexer;

    @Autowired
    private LuceneSearcher luceneSearcher;


    public List<String> getAllSongTitles() {
        return songsRepository.findAllSongTitles();
    }
    public void indexAllSongs() throws IOException {
        List<Songs> allSongs = songsRepository.findAll();
        luceneIndexer.indexSongs(allSongs);
    }

    public String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return "";
        }
        // Loại bỏ dấu
        String normalized = Normalizer.normalize(keyword, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        // Loại bỏ ký tự đặc biệt và chuyển thành chữ thường
        return normalized.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase();
    }

    // Tìm kiếm bằng Lucene với từ khóa đã chuẩn hóa
    public List<String> searchSongsByKeyword(String keyword) throws Exception {
        String normalizedKeyword = normalizeKeyword(keyword);
        return luceneSearcher.searchSongs(normalizedKeyword);
    }
    public List<SongDTO> getAllSongs() {
        return songsRepository.findAllSongs(); // Trả về danh sách các bài hát dưới dạng SongDTO
    }
}

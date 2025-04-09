package org.example.bts_backend.Services;

import org.example.bts_backend.Models.Songs;
import org.example.bts_backend.Repository.SongsRepository;
import org.example.bts_backend.dto.SongDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.text.Normalizer;

@Service
public class SongsService {

    @Autowired
    private SongsRepository songsRepository;

    @Autowired
    private LuceneIndexer luceneIndexer;
    
    @Autowired
    private LuceneSearcher luceneSearcher;


    // Phương thức thêm bài hát chỉ cần 3 tham số: title, artist, source
    public Songs addSong(String title, String artist, String source) {
        Songs song = new Songs();
        song.setTitle(title);
        song.setArtist(artist);
        song.setSource(source);

        // Các trường khác có thể để giá trị mặc định
        song.setImage("");  // Nếu không có hình ảnh, có thể để trống
        song.setDuration(0);  // Nếu không có thời gian, có thể để là 0
        song.setLyrics("");  // Nếu không có lời bài hát, có thể để trống
        song.setFavorite(false);  // Giá trị mặc định
        song.setCounter(0);  // Giá trị mặc định
        song.setReplay(0);  // Giá trị mặc định

        // Lưu bài hát vào database
        return songsRepository.save(song);
    }
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
    public List<SongDTO> searchSongsByKeyword(String keyword) throws Exception {
        String normalizedKeyword = normalizeKeyword(keyword);
        return luceneSearcher.searchSongs(normalizedKeyword);
    }

    public List<SongDTO> getAllSongs() {
        return songsRepository.findAllSongs(); // Trả về danh sách các bài hát dưới dạng SongDTO
    }

}

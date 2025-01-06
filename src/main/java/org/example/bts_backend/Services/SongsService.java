package org.example.bts_backend.Services;

import org.example.bts_backend.Models.Songs;
import org.example.bts_backend.Repository.SongsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class SongsService {

    @Autowired
    private SongsRepository songsRepository;

    @Autowired
    private LuceneIndexer luceneIndexer;

    @Autowired
    private LuceneSearcher luceneSearcher;

    public List<Songs> searchSongsByLyrics(String keyword) {
        return songsRepository.searchTop3ByLyrics(keyword);
    }
    public List<String> getAllSongTitles() {
        return songsRepository.findAllSongTitles();
    }
    public void indexAllSongs() throws IOException {
        List<Songs> allSongs = songsRepository.findAll();
        luceneIndexer.indexSongs(allSongs);
    }

    public List<String> searchSongsByKeyword(String keyword) throws Exception {
        return luceneSearcher.searchSongs(keyword);
    }
}

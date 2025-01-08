package org.example.bts_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter

public class SongDTO {
    private String title;
    private String artist;
    private String image;
    private String source;
    private String matchedField;
    public SongDTO(String title, String artist, String image, String source) {
        this.title = title;
        this.artist = artist;
        this.image = image;
        this.source = source;
    }

    // Constructor đầy đủ 5 tham số cho LuceneSearcher
    public SongDTO(String title, String artist, String image, String source, String matchedField) {
        this.title = title;
        this.artist = artist;
        this.image = image;
        this.source = source;
        this.matchedField = matchedField;
    }

}
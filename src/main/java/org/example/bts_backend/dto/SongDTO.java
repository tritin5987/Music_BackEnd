package org.example.bts_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SongDTO {
    private String title;
    private String artist;
    private String image;
    private String source;
}

package org.example.bts_backend.Models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "songs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Songs {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "album")
    private String album;

    @Column(name = "artist", nullable = false)
    private String artist;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "image")
    private String image;

    @Column(name = "duration")
    private int duration;

    @Column(name = "favorite")
    private boolean favorite;

    @Column(name = "counter")
    private int counter;

    @Column(name = "replay")
    private int replay;

    @Lob
    @Column(name = "lyrics", columnDefinition = "LONGTEXT")
    private String lyrics;
}

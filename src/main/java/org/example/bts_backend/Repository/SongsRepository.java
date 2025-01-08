package org.example.bts_backend.Repository;

import org.example.bts_backend.Models.Songs;
import org.example.bts_backend.dto.SongDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongsRepository extends JpaRepository<Songs, String> {

    // Full-Text Search với MySQL
//    @Query(value = "SELECT * FROM songs WHERE MATCH(lyrics) AGAINST(:keyword IN BOOLEAN MODE)", nativeQuery = true)
//    List<Songs> searchByLyrics(@Param("keyword") String keyword);
    @Query(value = "SELECT * FROM songs WHERE LOWER(lyrics) LIKE LOWER(CONCAT('%', :keyword, '%')) LIMIT 3", nativeQuery = true)
    List<Songs> searchTop3ByLyrics(@Param("keyword") String keyword);

    @Query("SELECT s.title FROM Songs s")
    List<String> findAllSongTitles();

    @Query("SELECT new org.example.bts_backend.dto.SongDTO(s.title, s.artist, s.image, s.source) FROM Songs s")
    List<SongDTO> findAllSongs();


}


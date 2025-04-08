package org.example.bts_backend.Controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
@CrossOrigin("*")
@RestController
@RequestMapping("/api/music")
public class MusicController {

    private final String musicDir = "C:/Users/bdtcl/Desktop/Flutter/music"; // Đường dẫn thư mục nhạc

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getMusicFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(musicDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("audio/mpeg")) // Xác định MIME type
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

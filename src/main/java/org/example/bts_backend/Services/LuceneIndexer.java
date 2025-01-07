package org.example.bts_backend.Services;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.example.bts_backend.Models.Songs;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.Normalizer;
import java.util.List;

@Service
public class LuceneIndexer {

    private Directory directory = new ByteBuffersDirectory();
    private StandardAnalyzer analyzer = new StandardAnalyzer();

    // Phương thức chuẩn hóa dữ liệu
    private String normalizeText(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        // Chuẩn hóa: Loại bỏ dấu và ký tự đặc biệt, chuyển thành chữ thường
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")  // Loại bỏ dấu
                .replaceAll("[^a-zA-Z0-9\\s]", "")  // Loại bỏ ký tự đặc biệt
                .toLowerCase();  // Chuyển thành chữ thường
    }

    // Lập chỉ mục bài hát
    public void indexSongs(List<Songs> songs) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(directory, config);

        for (Songs song : songs) {
            Document doc = new Document();
            doc.add(new StringField("id", song.getId(), Field.Store.YES));

            // Chuẩn hóa dữ liệu
            String normalizedTitle = normalizeText(song.getTitle());
            String normalizedLyrics = normalizeText(song.getLyrics());
            String normalizedArtist = normalizeText(song.getArtist());

            // Thêm các trường tìm kiếm
            doc.add(new TextField("title", song.getTitle() != null ? song.getTitle() : "", Field.Store.YES));
            doc.add(new TextField("lyrics", song.getLyrics() != null ? song.getLyrics() : "", Field.Store.YES));
            doc.add(new TextField("artist", song.getArtist() != null ? song.getArtist() : "", Field.Store.YES));
            doc.add(new StringField("source", song.getSource() != null ? song.getSource() : "", Field.Store.YES));
            doc.add(new StringField("image", song.getImage() != null ? song.getImage() : "", Field.Store.YES));

            // Chỉ mục kết hợp để tăng cường tìm kiếm
            doc.add(new TextField("combined",
                    normalizedTitle + " " + normalizedLyrics + " " + normalizedArtist,
                    Field.Store.NO));

            writer.addDocument(doc);
        }

        writer.close();
    }

    public Directory getDirectory() {
        return directory;
    }
}

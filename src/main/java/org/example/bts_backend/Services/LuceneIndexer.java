package org.example.bts_backend.Services;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.example.bts_backend.Models.Songs;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class LuceneIndexer {

    private Directory directory = new ByteBuffersDirectory();
    private StandardAnalyzer analyzer = new StandardAnalyzer();

    public void indexSongs(List<Songs> songs) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(directory, config);

        for (Songs song : songs) {
            Document doc = new Document();
            doc.add(new StringField("id", song.getId(), Field.Store.YES));
            doc.add(new TextField("title", song.getTitle() != null ? song.getTitle() : "", Field.Store.YES));
            doc.add(new TextField("lyrics", song.getLyrics() != null ? song.getLyrics() : "", Field.Store.YES));
            doc.add(new TextField("combined",
                    (song.getTitle() != null ? song.getTitle() : "") + " " +
                            (song.getLyrics() != null ? song.getLyrics() : ""),
                    Field.Store.NO));
            writer.addDocument(doc);
        }

        writer.close();
    }

    public Directory getDirectory() {
        return directory;
    }
}



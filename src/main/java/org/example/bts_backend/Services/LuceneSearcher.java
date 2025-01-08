package org.example.bts_backend.Services;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.example.bts_backend.dto.SongDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LuceneSearcher {

    private final LuceneIndexer luceneIndexer;

    public LuceneSearcher(LuceneIndexer luceneIndexer) {
        this.luceneIndexer = luceneIndexer;
    }

    public List<SongDTO> searchSongs(String queryStr) throws Exception {
        Directory directory = luceneIndexer.getDirectory();
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);

        // Tìm kiếm trên nhiều trường: tiêu đề, lời bài hát và tác giả
        QueryParser titleParser = new QueryParser("title", new StandardAnalyzer());
        QueryParser lyricsParser = new QueryParser("lyrics", new StandardAnalyzer());
        QueryParser artistParser = new QueryParser("artist", new StandardAnalyzer());

        Query titleQuery = titleParser.parse(queryStr);
        Query lyricsQuery = lyricsParser.parse(queryStr);
        Query artistQuery = artistParser.parse(queryStr);

        // Kết hợp tất cả các truy vấn bằng cách sử dụng BooleanQuery
        BooleanQuery.Builder combinedQuery = new BooleanQuery.Builder();
        combinedQuery.add(titleQuery, BooleanClause.Occur.SHOULD);
        combinedQuery.add(lyricsQuery, BooleanClause.Occur.SHOULD);
        combinedQuery.add(artistQuery, BooleanClause.Occur.SHOULD);

        TopDocs results = searcher.search(combinedQuery.build(), 3);  // Tối đa 10 kết quả

        List<SongDTO> songs = new ArrayList<>();
        for (ScoreDoc scoreDoc : results.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);

            // Định dạng kết quả trả về, hiển thị loại kết quả (tiêu đề/lời bài hát/tác giả)
            String matchedField;
            if (doc.get("title").toLowerCase().contains(queryStr.toLowerCase())) {
                matchedField = "Ten Bai Hat";
            } else if (doc.get("lyrics") != null && doc.get("lyrics").toLowerCase().contains(queryStr.toLowerCase())) {
                matchedField = "Loi Cua Bai Hat";
            } else {
                matchedField = "Tac Gia";
            }

            SongDTO song = new SongDTO(
                    doc.get("title"),
                    doc.get("artist"),
                    doc.get("image"),
                    doc.get("source"),
                    matchedField // Gửi thêm thông tin về loại kết quả
            );
            songs.add(song);
        }
        reader.close();
        return songs;
    }
}

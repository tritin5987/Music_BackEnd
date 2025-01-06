package org.example.bts_backend.Services;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LuceneSearcher {

    private final LuceneIndexer luceneIndexer;

    public LuceneSearcher(LuceneIndexer luceneIndexer) {
        this.luceneIndexer = luceneIndexer;
    }

    public List<String> searchSongs(String queryStr) throws Exception {
        Directory directory = luceneIndexer.getDirectory();
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);

        QueryParser titleParser = new QueryParser("title", new StandardAnalyzer());
        QueryParser lyricsParser = new QueryParser("lyrics", new StandardAnalyzer());
        QueryParser artistParser = new QueryParser("artist", new StandardAnalyzer());

        Query titleQuery = titleParser.parse(queryStr);
        Query lyricsQuery = lyricsParser.parse(queryStr);
        Query artistQuery = artistParser.parse(queryStr);

        // Thực hiện tìm kiếm với mỗi tiêu chí
        TopDocs titleResults = searcher.search(titleQuery, 5);  // Tối đa 5 kết quả
        TopDocs lyricsResults = searcher.search(lyricsQuery, 5);
        TopDocs artistResults = searcher.search(artistQuery, 5);

        List<String> songTitles = new ArrayList<>();

        // Lấy kết quả từ title và lyrics
        for (ScoreDoc scoreDoc : titleResults.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            songTitles.add("Tên bài hát: " + doc.get("title"));
        }
        for (ScoreDoc scoreDoc : lyricsResults.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            songTitles.add("Lời của bài hát: " + doc.get("title"));
        }
        for (ScoreDoc scoreDoc : artistResults.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            songTitles.add("Tác giả: " + doc.get("artist"));
        }

        reader.close();
        return songTitles;
    }

}


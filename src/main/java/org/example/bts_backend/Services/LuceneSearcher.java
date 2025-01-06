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

        QueryParser parser = new QueryParser("combined", new StandardAnalyzer());
        Query query = parser.parse(queryStr);

        TopDocs results = searcher.search(query, 3);  // Trả về tối đa 3 kết quả
        List<String> songTitles = new ArrayList<>();

        for (ScoreDoc scoreDoc : results.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            songTitles.add(doc.get("title"));
        }
        reader.close();
        return songTitles;
    }
}


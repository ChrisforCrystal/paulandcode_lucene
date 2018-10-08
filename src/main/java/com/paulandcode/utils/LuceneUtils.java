package com.paulandcode.utils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 操作Lucene
 * @Author: paulandcode
 * @Email: paulandcode@gmail.com
 * @Date: 2018/9/21 13:27
 */
@Component
public class LuceneUtils {
    /**
     * 默认分页缓存时间, 单位: 秒.
     */
    private static int luceneCacheTime;

    /**
     * Lucene索引根路径
     */
    private static String rootPath;

    /**
     * Redis模板
     */
    private static RedisTemplate<String, Object> redisTemplate;

    /**
     * 将Spring的Been注入给静态成员变量
     *
     * @param redisTemplate Spring注入的Redis模板Been
     * @return void
     */
    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        LuceneUtils.redisTemplate = redisTemplate;
    }

    @Value("${spring.redis.lucene-cache-time}")
    public void setLuceneCacheTime(int luceneCacheTime) {
        LuceneUtils.luceneCacheTime = luceneCacheTime;
    }

    @Value("${spring.lucene.root-path}")
    public void setRootPath(String rootPath) {
        LuceneUtils.rootPath = rootPath;
    }

    /**
     * 获得索引写入流
     *
     * @param indexName 索引存储相对路径
     * @param isChinese Text形式索引是否为中文
     * @return org.apache.lucene.index.IndexWriter
     */
    public static IndexWriter getIndexWriter(String indexName, boolean isChinese) {
        Analyzer analyzer;
        // 如果是中文, 就用中文分词器, 否则使用西文分词器
        if (isChinese) {
            analyzer = new SmartChineseAnalyzer();
        } else {
            analyzer = new SimpleAnalyzer();
        }
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        Directory dir;//索引存储的位置
        IndexWriter indexWriter = null;
        try {
            dir = FSDirectory.open(Paths.get(rootPath + indexName));
            indexWriter = new IndexWriter(dir, config);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return indexWriter;
    }

    /**
     * 获得索引读出流
     *
     * @param indexName 索引存储相对路径
     * @return org.apache.lucene.index.IndexReader
     */
    public static IndexReader getIndexReader(String indexName) {
        Directory dir; //获取要查询的路径，也就是索引所在的位置
        IndexReader indexReader = null;
        try {
            dir = FSDirectory.open(Paths.get(rootPath + indexName));
            indexReader = DirectoryReader.open(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return indexReader;
    }

    /**
     * 删除整个索引文件夹
     *
     * @param indexName
     * @return void
     */
    public static void deleteTheWholeIndex(String indexName) {
        FileUtils.deleteAll(new File(rootPath + indexName));
    }

    /**
     * 创建索引
     *
     * @param indexWriter 索引写入流.
     * @param data 多条数据集合, 二维数据, 第一条为领域名称
     * @param textColumnNums 需要进行Text形式的索引的集合元素下标, 其他元素进行String形式索引
     * @return void
     */
    public static void addIndexByListList(IndexWriter indexWriter, List<List<String>> data, List<Integer> textColumnNums) {
        if (indexWriter == null) {
            return;
        }
        List<String> fieldNames = data.get(0);
        try {
            for (int i = 1; i < data.size(); i++) {
                indexWriter.addDocument(setListToDoc(fieldNames, textColumnNums, data.get(i)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                indexWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建索引
     *
     * @param indexWriter
     * @param data
     * @param textColumns
     * @return void
     */
    public static void addIndexByListMap(IndexWriter indexWriter, List<Data> data, List<String> textColumns) {
        if (indexWriter == null) {
            return;
        }
        try {
            for (Data oneData: data) {
                indexWriter.addDocument(setMapToDoc(textColumns, oneData));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                indexWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除某条数据的索引
     *
     * @param indexWriter 索引写入流
     * @param fieldName 检索域名称(一般为id等唯一字段)
     * @param keyword 关键词(如果是id, 输入整个id, 确保删除唯一数据)
     * @return void
     */
    public static void deleteIndex(IndexWriter indexWriter, String fieldName, String keyword) {
        if (indexWriter == null) {
            return;
        }
        try {
            indexWriter.deleteDocuments(new Term(fieldName, keyword));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                indexWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 更新索引
     *
     * @param indexWriter 索引写入流
     * @param keywordColumnNum 关键词所在元素下标(一般为id等唯一字段, 输入整个id, 确保删除唯一数据)
     * @param data 多条数据集合, 二维数据, 第一条为领域名称
     * @param textColumnNums 需要进行Text形式的索引的集合元素下标, 其他元素进行String形式索引
     * @return void
     */
    public static void updateIndexByListList(IndexWriter indexWriter, int keywordColumnNum, List<List<String>> data, List<Integer> textColumnNums) {
        if (indexWriter == null) {
            return;
        }
        List<String> fieldNames = data.get(0);
        try {
            for (int i = 1; i < data.size(); i++) {
                List<String> oneData = data.get(i);
                indexWriter.updateDocument(new Term(fieldNames.get(keywordColumnNum), oneData.get(keywordColumnNum)), setListToDoc(fieldNames, textColumnNums, oneData));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                indexWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 更新索引
     *
     * @param indexWriter
     * @param keywordColumn
     * @param data
     * @param textColumns
     * @return void
     */
    public static void updateIndexByListMap(IndexWriter indexWriter, String keywordColumn, List<Data> data, List<String> textColumns) {
        if (indexWriter == null) {
            return;
        }
        try {
            for (Data oneData: data) {
                indexWriter.updateDocument(new Term(keywordColumn, oneData.get(keywordColumn)), setMapToDoc(textColumns, oneData));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                indexWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据关键字及相关条件进行检索
     *
     * @param indexName 索引存储相对路径
     * @param paging 是否分页, 如果分页, 则从上一次的分页缓存中获得数据, 并从上一次的缓存数据开始查找, 并将查找的最后一个结果放到缓存中
     * @param isChinese Text形式索引是否为中文
     * @param searchFieldName 要检索的检索域, 暂时没加入多检索域的检索功能
     * @param resultFieldNames 要返回的检索域
     * @param keyword 检索关键字
     * @param num 本次检索的条数, 必须大于0
     * @param preTag 关键字高亮的前缀, 如果前后缀都为null或空字符串, 则代表不高亮
     * @param postTag 关键字高亮的后缀
     * @return java.util.List<java.lang.String[]> String[]中为每个检索域内容, List中为各条检索结果
     */
    public static List<String[]> search(String indexName, boolean paging, boolean isChinese, String searchFieldName, String[] resultFieldNames, String keyword, int num, String preTag, String postTag) {
        List<String[]> result = new ArrayList<>();
        Analyzer analyzer;
        // 如果是中文, 就用中文分词器, 否则使用西文分词器
        if (isChinese) {
            analyzer = new SmartChineseAnalyzer();
        } else {
            analyzer = new SimpleAnalyzer();
        }
        // 查询解析器
        QueryParser parser = new QueryParser(searchFieldName, analyzer);
        // 查询的每个结果的字段数
        int fieldSize = resultFieldNames.length;
        ScoreDoc lastBottom = null;
        String redisKey = rootPath + indexName + "_" + keyword;
        if (paging) {
            Object doc = redisTemplate.boundListOps(redisKey).rightPop();
            Object score = redisTemplate.boundListOps(redisKey).rightPop();
            Object shardIndex = redisTemplate.boundListOps(redisKey).rightPop();
            if (doc != null && score != null && shardIndex != null) {
                lastBottom = new ScoreDoc(Integer.parseInt(doc.toString()), Float.parseFloat(score.toString()), Integer.parseInt(shardIndex.toString()));
            }
        }
        Query query;
        TopDocs docs;
        Highlighter highlighter = null;
        IndexReader indexReader = null;
        try {
            // 获取要查询的路径，也就是索引所在的位置
            Directory dir = FSDirectory.open(Paths.get(rootPath + indexName));
            indexReader = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(indexReader);
            // 通过解析要查询的String, 获取查询对象.
            query = parser.parse(keyword);
            // 是否创建高亮处理器
            if (!StringUtils.isEmpty(preTag) || !StringUtils.isEmpty(postTag)) {
                QueryScorer scorer = new QueryScorer(query);
                Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);
                SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter(preTag == null ? "" : preTag, postTag == null ? "" : postTag);
                highlighter = new Highlighter(simpleHTMLFormatter, scorer);
                highlighter.setTextFragmenter(fragmenter);
            }
            // 开始查询, 查询前num条数据, 将记录保存在docs中.
            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
            // 可以添加多个查询条件, 这里暂时只支持一个
            booleanQuery.add(query, BooleanClause.Occur.SHOULD);
            docs = searcher.searchAfter(lastBottom, booleanQuery.build(), num);
            ScoreDoc[] scoreDocs = docs.scoreDocs;
            // 取出每条查询结果
            for (ScoreDoc scoreDoc : scoreDocs) {
                String[] oneResult = new String[fieldSize];
                // scoreDocForFastJson.doc相当于docID,根据这个docID来获取文档
                Document doc = searcher.doc(scoreDoc.doc);
                // 是否高亮处理
                if (highlighter == null) {
                    for (int i = 0; i < fieldSize; i++) {
                        oneResult[i] = doc.get(resultFieldNames[i]);
                    }
                } else {
                    for (int i = 0; i < fieldSize; i++) {
                        String fieldName = resultFieldNames[i];
                        String preSubOneResult = doc.get(fieldName);
                        TokenStream tokenStream = analyzer.tokenStream(fieldName, new StringReader(preSubOneResult));
                        oneResult[i] = highlighter.getBestFragment(tokenStream, preSubOneResult);
                    }
                }
                result.add(oneResult);
            }
            redisTemplate.delete(redisTemplate.keys(redisKey));
            if (paging && scoreDocs.length > 0) {
                ScoreDoc scoreDoc = scoreDocs[scoreDocs.length - 1];
                redisTemplate.boundListOps(redisKey).rightPush(scoreDoc.doc);
                redisTemplate.boundListOps(redisKey).rightPush(scoreDoc.score);
                redisTemplate.boundListOps(redisKey).rightPush(scoreDoc.shardIndex);
                redisTemplate.expire(redisKey, luceneCacheTime, TimeUnit.SECONDS);
            }
        } catch (ParseException | IOException | InvalidTokenOffsetsException e) {
            e.printStackTrace();
        } finally {
            if (indexReader != null) {
                try {
                    indexReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private static Document setMapToDoc(List<String> textColumns, Data oneData) {
        Document doc = new Document();
        Set<Map.Entry<String, String>> entries = oneData.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            String fieldName = entry.getKey();
            if (textColumns.contains(fieldName)) {
                doc.add(new TextField(fieldName, String.valueOf(entry.getValue()), Field.Store.YES));
            } else {
                doc.add(new StringField(fieldName, String.valueOf(entry.getValue()), Field.Store.YES));
            }
        }
        return doc;
    }

    private static Document setListToDoc(List<String> fieldNames, List<Integer> textColumnNums, List<String> oneData) {
        Document doc = new Document();
        for (int j = 0; j < oneData.size(); j++) {
            String fieldName = fieldNames.get(j);
            String value = oneData.get(j);
            if (textColumnNums.contains(j)) {
                // 进行Text形式的索引, 一般用于文章等正文部分, 会被分词化.
                // 分词化指一句话会被分成多个词, 输入一句话进行检索时不会被搜到.
                doc.add(new TextField(fieldName, value, Field.Store.YES));
            } else {
                // 进行String形式的索引, 一般用于国家, ID等非正文内容, 不会被分词化
                doc.add(new StringField(fieldName, value, Field.Store.YES));
            }
        }
        return doc;
    }
}

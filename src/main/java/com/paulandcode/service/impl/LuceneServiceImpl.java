package com.paulandcode.service.impl;

import com.alibaba.fastjson.JSON;
import com.paulandcode.service.LuceneService;
import com.paulandcode.utils.Data;
import com.paulandcode.utils.FileUtils;
import com.paulandcode.utils.LuceneUtils;
import org.apache.lucene.index.IndexWriter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 检索服务
 *
 * @Author: paulandcode
 * @Email: paulandcode@gmail.com
 * @Date: 2018/9/23 12:27
 */
@Service("luceneService")
public class LuceneServiceImpl implements LuceneService {
    @Override
    public void addIndex(Map<String, Object> params, MultipartFile file) {
        String indexName = params.get("indexName").toString();
        boolean isChinese = "1".equals(params.get("isChinese").toString());
        String preTextColumnNums = params.get("textColumnNums").toString();
        List<Integer> textColumnNums = new ArrayList<>();
        if (!"".equals(preTextColumnNums)) {
            String[] arrTextColumnNums = preTextColumnNums.split(",");
            for (String textColumnNum : arrTextColumnNums) {
                textColumnNums.add(Integer.parseInt(textColumnNum));
            }
        }
        List<List<List<String>>> data = FileUtils.excelToList(file, String.class);
        IndexWriter indexWriter = LuceneUtils.getIndexWriter(indexName, isChinese);
        // 创建索引
        LuceneUtils.addIndexByListList(indexWriter, data.get(0), textColumnNums);
    }

    @Override
    public void addIndexByParams(Map<String, Object> params) {
        String indexName = params.get("indexName").toString();
        boolean isChinese = "1".equals(params.get("isChinese").toString());
        String[] textColumns = params.get("textColumns").toString().split(",");
        String dataListString = params.get("dataListString").toString();
        List<Data> data = JSON.parseArray(dataListString, Data.class);
        IndexWriter indexWriter = LuceneUtils.getIndexWriter(indexName, isChinese);
        // 创建索引
        LuceneUtils.addIndexByListMap(indexWriter, data, Arrays.asList(textColumns));
    }

    @Override
    public List<String[]> search(Map<String, Object> params) {
        String indexName = params.get("indexName").toString();
        boolean paging = "1".equals(params.get("paging").toString());
        boolean isChinese = "1".equals(params.get("isChinese").toString());
        String searchFieldName = params.get("searchFieldName").toString();
        String[] resultFieldNames = params.get("resultFieldNames").toString().split(",");
        String keyword = params.get("keyword").toString();
        int num = Integer.parseInt(params.get("num").toString());
        String preTag = params.get("preTag").toString();
        String postTag = params.get("postTag").toString();
        return LuceneUtils.search(indexName, paging, isChinese, searchFieldName, resultFieldNames, keyword, num, preTag, postTag);
    }

    @Override
    public List<String> suggest(Map<String, Object> params) {
        String indexName = params.get("indexName").toString();
        boolean isChinese = "1".equals(params.get("isChinese").toString());
        String searchFieldName = params.get("searchFieldName").toString();
        String keyword = params.get("keyword").toString();
        int num = Integer.parseInt(params.get("num").toString());
        boolean paging = "1".equals(params.get("paging").toString());
        String preTag = params.get("preTag").toString();
        String postTag = params.get("postTag").toString();
        List<String[]> searchResult = LuceneUtils.search(indexName, paging, isChinese, searchFieldName, new String[]{searchFieldName}, keyword, num, preTag, postTag);
        int count = searchResult.size();
        List<String> result = new ArrayList<>(count);
        for (String[] aSearchResult : searchResult) {
            result.add(aSearchResult[0]);
        }
        return result;
    }

    @Override
    public void deleteIndex(Map<String, Object> params) {
        String indexName = params.get("indexName").toString();
        boolean isChinese = "1".equals(params.get("isChinese").toString());
        String fieldName = params.get("fieldName").toString();
        String keyword = params.get("keyword").toString();
        IndexWriter indexWriter = LuceneUtils.getIndexWriter(indexName, isChinese);
        LuceneUtils.deleteIndex(indexWriter, fieldName, keyword);
    }

    @Override
    public void updateIndex(Map<String, Object> params, MultipartFile file) {
        String indexName = params.get("indexName").toString();
        boolean isChinese = "1".equals(params.get("isChinese").toString());
        String textColumnNum = params.get("textColumnNums").toString();
        int keywordColumnNum = Integer.parseInt(params.get("keywordColumnNum").toString());
        List<Integer> textColumnNums = new ArrayList<>();
        if (!"".equals(textColumnNum)) {
            String[] stringTextColumnNums = textColumnNum.split(",");
            for (String stringTextColumnNum :stringTextColumnNums) {
                textColumnNums.add(Integer.parseInt(stringTextColumnNum));
            }
        }
        List<List<List<String>>> data = FileUtils.excelToList(file, String.class);
        IndexWriter indexWriter = LuceneUtils.getIndexWriter(indexName, isChinese);
        LuceneUtils.updateIndexByListList(indexWriter, keywordColumnNum, data.get(0), textColumnNums);
    }

    @Override
    public void updateIndexByParams(Map<String, Object> params) {
        String indexName = params.get("indexName").toString();
        boolean isChinese = "1".equals(params.get("isChinese").toString());
        String[] textColumns = params.get("textColumns").toString().split(",");
        String keywordColumn = params.get("keywordColumn").toString();
        String dataListString = params.get("dataListString").toString();
        List<Data> data = JSON.parseArray(dataListString, Data.class);
        IndexWriter indexWriter = LuceneUtils.getIndexWriter(indexName, isChinese);
        LuceneUtils.updateIndexByListMap(indexWriter, keywordColumn, data, Arrays.asList(textColumns));
    }

    @Override
    public void deleteTheWholeIndex(Map<String, Object> params) {
        String indexName = params.get("indexName").toString();
        LuceneUtils.deleteTheWholeIndex(indexName);
    }
}

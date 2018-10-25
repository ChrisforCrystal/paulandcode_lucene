package com.paulandcode.controller;

import com.paulandcode.service.LuceneService;
import com.paulandcode.utils.LuceneUtils;
import com.paulandcode.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @Description 全文检索
 * @Author paulandcode
 * @Date 2018/9/23 14:46
 * @Email: paulandcode@gmail.com
 **/
@RestController
@RequestMapping("lucene")
public class LuceneController {
	private final LuceneService luceneService;

    @Autowired
    public LuceneController(LuceneService luceneService) {
        this.luceneService = luceneService;
    }

    /**
     * 通过Excel文件增加索引
     *
     * @param params indexName, isChinese, textColumnNums
     * @param file Excel文件
     * @return com.paulandcode.utils.R
     */
    @RequestMapping(value = "addIndex", method = RequestMethod.POST)
	public R addIndex(@RequestParam Map<String, Object> params, @RequestParam("file") MultipartFile file) {
        luceneService.addIndex(params, file);
		return R.ok();
	}

    /**
     * 通过Post的请求参数增加索引
     *
     * @param params indexName, isChinese, textColumns, dataListString
     * @return com.paulandcode.utils.R
     */
    @RequestMapping(value = "addIndexByParams", method = RequestMethod.POST)
    public R addIndexByParams(@RequestParam Map<String, Object> params) {
        luceneService.addIndexByParams(params);
        return R.ok();
    }

	/**
	 * 删除个别索引
	 *
     * @param params indexName, isChinese, fieldName, keyword
	 * @return com.paulandcode.utils.R
	 */
    @RequestMapping(value = "deleteIndex")
    public R deleteIndex(@RequestParam Map<String, Object> params) {
        luceneService.deleteIndex(params);
        return R.ok();
    }

    /**
     * 删除整个索引文件夹
     *
     * @param params indexName
     * @return com.paulandcode.utils.R
     */
    @RequestMapping(value = "deleteTheWholeIndex")
    public R deleteTheWholeIndex(@RequestParam Map<String, Object> params) {
        luceneService.deleteTheWholeIndex(params);
        return R.ok();
    }

    /**
     * 通过Excel批量更新索引
     *
     * @param params indexName, isChinese, textColumnNums, keywordColumnNum
     * @param file Excel附件
     * @return com.paulandcode.utils.R
     */
    @RequestMapping(value = "updateIndex", method = RequestMethod.POST)
    public R updateIndex(@RequestParam Map<String, Object> params, @RequestParam("file") MultipartFile file) {
        luceneService.updateIndex(params, file);
        return R.ok();
    }

    /**
     * 通过Post的请求参数批量更新索引
     *
     * @param params indexName, isChinese, textColumns, keywordColumn, dataListString
     * @return com.paulandcode.utils.R
     */
    @RequestMapping(value = "updateIndexByParams", method = RequestMethod.POST)
    public R updateIndexByParams(@RequestParam Map<String, Object> params) {
        luceneService.updateIndexByParams(params);
        return R.ok();
    }

    /**
     * 检索
     *
     * @param params indexName, paging, isChinese, searchFieldName, resultFieldNames, keyword, num, preTag, postTag
     * @return com.paulandcode.utils.R
     */
    @RequestMapping(value = "search")
    public R search(@RequestParam Map<String, Object> params) {
        return R.ok().put("data", luceneService.search(params));
    }

    /**
     * 自动补全
     *
     * @param params indexName, isChinese, searchFieldName, keyword, num, paging, preTag, postTag
     * @return com.paulandcode.utils.R
     */
    @RequestMapping(value = "suggest")
    public R suggest(@RequestParam Map<String, Object> params) {
        return R.ok().put("data", luceneService.suggest(params));
    }

    /**
     * 增加权重, 可以在每次点击某一条记录时增加这条记录的权重
     *
     * @param params 参数
     * @return com.paulandcode.utils.R
     */
    @RequestMapping(value = "addWeight")
    public R addWeight(@RequestParam Map<String, Object> params) {

        return R.ok().put("data", LuceneUtils.addWeight(params));
    }
}

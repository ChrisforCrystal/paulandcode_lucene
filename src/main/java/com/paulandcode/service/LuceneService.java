package com.paulandcode.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @Description 检索服务
 * @Author paulandcode
 * @Email: paulandcode@gmail.com
 * @Date 2018/9/23 12:38
 **/
@Service
public interface LuceneService {
    /**
     * 给数据增加索引
     *
     * @param params
     * @param file
     * @return void
     */
    void addIndex(Map<String, Object> params, MultipartFile file);

    /**
     * 通过参数创建索引
     *
     * @param params 参数
     * @return void
     */
    void addIndexByParams(Map<String, Object> params);

    /**
     * 根据关键字检索内容
     *
     * @param params
     * @return java.util.List<java.lang.String[]>
     */
    List<String[]> search(Map<String, Object> params);

    /**
     * 自动补全
     *
     * @param params
     * @return java.util.List<java.lang.String>
     */
    List<String> suggest(Map<String, Object> params);

    /**
     * 删除索引
     *
     * @param params
     * @return void
     */
    void deleteIndex(Map<String, Object> params);

    /**
     * 更新索引
     *
     * @param params
     * @param file
     * @return void
     */
    void updateIndex(Map<String, Object> params, MultipartFile file);

    /**
     * 通过参数更新索引
     *
     * @param params
     * @return void
     */
    void updateIndexByParams(Map<String, Object> params);

    /**
     * 删除整个索引文件夹
     *
     * @param params
     * @return void
     */
    void deleteTheWholeIndex(Map<String, Object> params);
}
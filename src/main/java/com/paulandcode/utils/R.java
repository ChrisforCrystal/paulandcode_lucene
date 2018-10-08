package com.paulandcode.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: Request响应信息
 * @Author: paulandcode
 * @Email: paulandcode@gmail.com
 * @Date: 2018/9/23 15:11
 */
public class R extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    /**
     *
     * 构造函数.
     */
    private R() {
        put("code", 1);
    }

    /**
     *
     * 响应未知异常.
     * @return
     */
    public static R error() {
        return error(0, "未知异常，请联系管理员");
    }

    /**
     *
     * 响应自定义异常信息.
     * @param msg
     * @return
     */
    public static R error(String msg) {
        return error(0, msg);
    }

    /**
     *
     * 响应自定义异常信息和状态码.
     * @param code
     * @param msg
     * @return
     */
    public static R error(int code, String msg) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    /**
     *
     * 响应信息移除指定键值对.
     * @param key
     * @return
     */
    public R remove(String key){
        super.remove(key);
        return this;
    }

    /**
     *
     * 响应成功并自定义信息.
     * @param msg
     * @return
     */
    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    /**
     *
     * 响应成功并加入一些键值对.
     * @param map
     * @return
     */
    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    /**
     *
     * 响应成功.
     * @return
     */
    public static R ok() {
        return new R();
    }

    /**
     *
     * 响应中加入一个键值对.
     *
     * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}

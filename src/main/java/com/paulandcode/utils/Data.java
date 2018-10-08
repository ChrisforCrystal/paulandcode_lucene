package com.paulandcode.utils;

import java.io.Serializable;
import java.util.HashMap;

/**
 * 将Json字符串转换为Map所需要的实体类
 *
 * @author paulandcode
 * @email paulandcode@gmail.com
 * @date 2018/10/1 10:05
 */
public class Data extends HashMap<String, String> implements Serializable {
    private static final long serialVersionUID = -3415612874826306911L;

    @Override
    public String get(Object key) {
        return String.valueOf(super.get(key));
    }
}

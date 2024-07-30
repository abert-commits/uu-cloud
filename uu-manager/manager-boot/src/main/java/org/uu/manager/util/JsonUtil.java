package org.uu.manager.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;
import java.util.TreeMap;

public class JsonUtil {

    /**
     * 校验字符串是否是json格式
     *
     * @param json
     * @return boolean
     */
    public static boolean isValidJSONObjectOrArray(String json) {
        try {
            Object obj = JSON.parse(json);
            return obj instanceof JSONObject || obj instanceof JSONArray;
        } catch (JSONException e) {
            return false;
        }
    }


    /**
     * 对json进行排序
     *
     * @param jsonObject
     * @return {@link String}
     */
    public static String sortJsonByKey(JSONObject jsonObject) {
        Map<String, Object> sortedMap = new TreeMap<>();

        for (String key : jsonObject.keySet()) {
            sortedMap.put(key, jsonObject.get(key));
        }

        return JSON.toJSONString(sortedMap);
    }


    /**
     * 将JSON字符串转换为指定类型的Java对象。
     * @param jsonString JSON格式的字符串
     * @param clazz 目标对象的类类型
     * @param <T> 泛型参数，表示返回的对象类型
     * @return 转换后的对象，如果转换失败则返回null
     */
    public static <T> T fromJson(String jsonString, Class<T> clazz) {
        try {
            return JSON.parseObject(jsonString, clazz);
        } catch (JSONException e) {
            // 处理解析错误
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将Java对象转换为JSON字符串。
     * @param object 要转换的Java对象
     * @return JSON格式的字符串
     */
    public static String toJson(Object object) {
        return JSON.toJSONString(object);
    }

}

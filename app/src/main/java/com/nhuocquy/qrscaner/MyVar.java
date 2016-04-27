package com.nhuocquy.qrscaner;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by NhuocQuy on 4/26/2016.
 */
public class MyVar {
    public static Map<String, Object> map = new HashMap<>();
    public static final String CURRENT_LOCATION = "cLocation";
    public static void put(String key, Object value){
        map.put(key, value);
    }
    public static Object get(String key){
        return map.get(key);
    }
}

package com.yunji.flurry.doc.util;


import com.yunji.flurry.metadata.tag.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author with struy.
 * Create by 2018/5/16 13:06
 * email :yq1724555319@gmail.com
 */

public class MapSortUtil {

    /**
     * 使用 Map按key进行排序
     *
     * @param map
     * @return
     */
    public static Map<String, Collection<Service>> sortMapByKey(Map<String, Collection<Service>> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        Map<String, Collection<Service>> sortMap = new TreeMap<>(String::compareTo);
        sortMap.putAll(map);

        return sortMap;
    }
}

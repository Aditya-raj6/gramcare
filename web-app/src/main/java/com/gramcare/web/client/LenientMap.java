package com.gramcare.web.client;

import java.util.*;

/**
 * A HashMap that reports every key as present. Spring's SpEL MapAccessor (used
 * by Thymeleaf for ${obj.field}) throws when a key is missing instead of
 * returning null; backend JSON omits nothing deliberately, but empty forms and
 * "service down" fallbacks would otherwise crash the page. With this wrapper a
 * missing field simply renders as blank.
 */
public class LenientMap extends HashMap<String, Object> {

    public LenientMap() { super(); }

    public LenientMap(Map<String, Object> source) { super(source); }

    @Override
    public boolean containsKey(Object key) { return true; }

    /** Recursively wraps maps (and maps inside lists) from a JSON response. */
    @SuppressWarnings("unchecked")
    public static Object wrap(Object value) {
        if (value instanceof Map<?, ?> m) {
            LenientMap out = new LenientMap();
            m.forEach((k, v) -> out.put(String.valueOf(k), wrap(v)));
            return out;
        }
        if (value instanceof List<?> l) {
            List<Object> out = new ArrayList<>(l.size());
            for (Object o : l) out.add(wrap(o));
            return out;
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> of(Map<String, Object> m) {
        return m == null ? new LenientMap() : (Map<String, Object>) wrap(m);
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> ofList(List<Map<String, Object>> l) {
        return l == null ? new ArrayList<>() : (List<Map<String, Object>>) wrap(l);
    }
}

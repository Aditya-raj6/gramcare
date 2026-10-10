package com.gramcare.web;

import com.gramcare.web.client.LenientMap;
import com.gramcare.web.config.Fmt;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WebHelpersTests {

    @Test
    void lenientMapReturnsNullForMissingKeysAndWrapsNestedMaps() {
        Map<String, Object> wrapped = LenientMap.of(Map.of("a", 1, "nested", Map.of("b", 2), "list", List.of(Map.of("c", 3))));
        assertTrue(wrapped.containsKey("missing"));
        assertNull(wrapped.get("missing"));
        assertTrue(wrapped.get("nested") instanceof LenientMap);
        assertTrue(((List<?>) wrapped.get("list")).get(0) instanceof LenientMap);
    }

    @Test
    void formatsIsoDateTimeStringsForDisplay() {
        Fmt fmt = new Fmt();
        assertEquals("2026-10-10 14:32", fmt.dt("2026-10-10T14:32:05.123"));
        assertEquals("2026-10-10", fmt.date("2026-10-10T14:32:05"));
        assertEquals("", fmt.dt(null));
        assertTrue(fmt.isPast("2000-01-01"));
    }
}

package com.gramcare.web.config;

import org.springframework.stereotype.Component;

/**
 * Template helper, used as ${@fmt.dt(value)}. Backend services return dates
 * as ISO-8601 strings in JSON (e.g. 2026-10-10T14:32:05.123), so the
 * templates format them here instead of with #temporals, which needs a real
 * java.time object.
 */
@Component("fmt")
public class Fmt {

    public String dt(Object value) {
        if (value == null) return "";
        String s = value.toString().replace('T', ' ');
        return s.length() > 16 ? s.substring(0, 16) : s;
    }

    public String date(Object value) {
        if (value == null) return "";
        String s = value.toString();
        return s.length() > 10 ? s.substring(0, 10) : s;
    }

    public boolean isPast(Object isoDate) {
        if (isoDate == null) return false;
        try {
            return java.time.LocalDate.parse(date(isoDate)).isBefore(java.time.LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }
}

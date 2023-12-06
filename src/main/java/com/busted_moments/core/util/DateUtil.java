package com.busted_moments.core.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    public static Date fromISOString(String dateString) {
        return Date.from(LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME).atZone(ZoneOffset.UTC).toInstant());
    }

    public static Date fromString(String dateString, String pattern, ZoneOffset timezone) {
        return Date.from(LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)).atZone(timezone).toInstant());
    }

    public static Date fromString(String string, DateTimeFormatter formatter) {
        return Date.from(formatter.parse(string, Instant::from));
    }
}

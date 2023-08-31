package com.busted_moments.core.util;

import java.util.Collections;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    public static String nCopies(String s, int n) {
        return String.join("", Collections.nCopies(n, s));
    }

    public static String replaceLast(String string, String substring, String replacement) {
        int index = string.lastIndexOf(substring);
        if (index == -1)
            return string;
        return string.substring(0, index) + replacement
                + string.substring(index+substring.length());
    }

    public static int indexOf(String string, Pattern pattern) {
        Matcher matcher = pattern.matcher(string);
        return matcher.find() ? matcher.start(): -1;
    }
}

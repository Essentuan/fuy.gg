package com.busted_moments.core.util;

import com.busted_moments.core.tuples.Pair;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    public static <T> Optional<T> bestMatch(String string, Iterable<T> options, Function<T, String> mapper, boolean caseSensitive) {
        Pair<T, Double> bestMatch = null;

        for (T t : options) {
            String option = mapper.apply(t);

            if (option.length() != string.length())
                continue;

            if (option.equals(string))
                return Optional.of(t);

            if (caseSensitive)
                continue;

            double matches = 0;

            for (int i = 0; i < string.length(); i++) {
                if (string.charAt(i) == option.charAt(i))
                    matches+= 1;
                else if (CharUtil.equalsIgnoreCase(string.charAt(i), option.charAt(i)))
                    matches+= 0.5;
            }

            if (bestMatch == null || bestMatch.two() < matches)
                bestMatch = Pair.of(t, matches);
        }

        return Optional.ofNullable(bestMatch).map(Pair::one);
    }

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

    public static String truncate(String string, int chars) {
        return string.length() > chars ? string.substring(0, chars) + "..." : string;
    }

    public static StringBuilder build(StackTraceElement[] stacktrace) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < stacktrace.length; i++) {
            builder
                    .append("\t")
                    .append(i == 0 ? "   " : "at ")
                    .append(stacktrace[i].toString())
                    .append("\n");
        }

        return builder;
    }
}

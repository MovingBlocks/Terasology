// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;

public final class StringUtility {

    private StringUtility() { }

    /**
     * Returns the ordinal indicator of an integer.
     * <br><br>
     * Most readable when called with class name:
     * getOrdinal(22) returns "nd"
     *
     * @param number the integer
     * @return The ordinal indicator ("st", "nd", "rd" or "th").
     */
    public static String getOrdinal(int number) {
        int x = Math.abs(number);
        x %= 100;

        switch (x) {
            case 11:
            case 12:
            case 13:
                return "th";
        }

        x %= 10;

        switch (x) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    /**
     * Returns the integer combined with it's ordinal indicator as String.
     * <br><br>
     * Most readable when called with class name:
     * toOrdinalString(22) returns "22nd"
     *
     * @param x the integer
     * @return The integer with it's ordinal indicator attached.
     */
    public static String toOrdinalString(int x) {
        return String.format("%d%s", x, getOrdinal(x));
    }

    /**
     * reference: http://stackoverflow.com/questions/745415/regex-to-match-from-partial-or-camel-case-string
     *
     * Match a string by a wild card expression and a collection of strings to match against
     *
     * query: MPRString, M, MyP*RString, *PosResString, MyP*RString, My*String, M
     * match: MyPossibleResultString
     *
     * @param queryStr string to query by
     * @param commands a list of strings that will be matched
     * @return the collection of matching strings
     */
    public static Set<String> wildcardMatch(String queryStr, Collection<String> commands,
                                            boolean includeCaseInsensitiveStartingWith) {
        Set<String> matches = Sets.newHashSet();

        String query = queryStr.replaceAll("\\*", ".*?");
        query = query.replaceAll("\\(|\\)|\\[|\\]|\\{|\\}", "");
        query = query.replaceFirst("\\b([a-z]+)", "$1[a-z]*");

        String re = "\\b(" + query.replaceAll("([A-Z][^A-Z]*)", "$1[^A-Z]*") + ".*?)\\b";

        Pattern regex = Pattern.compile(re);
        for (String cmd : commands) {
            if (includeCaseInsensitiveStartingWith && cmd.toLowerCase().startsWith(queryStr.toLowerCase())) {
                matches.add(cmd);
            }
            Matcher m = regex.matcher(cmd);
            if (m.find()) {
                matches.add(m.group());
            }
        }
        return matches;
    }
}

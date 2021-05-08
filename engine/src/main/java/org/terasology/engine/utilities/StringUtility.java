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

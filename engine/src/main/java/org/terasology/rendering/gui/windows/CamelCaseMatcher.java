/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.rendering.gui.windows;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;

/**
 * Inspired by
 * <p>
 * http://stackoverflow.com/questions/745415/regex-to-match-from-partial-or-camel-case-string
 * </p>
 * @author Martin Steiger
 */
public final class CamelCaseMatcher {

    private CamelCaseMatcher() {
        // avoid instantiation
    }

    /**
     * @param commandName
     * @param commands
     * @return
     */
    public static Set<String> getMatches(String queryStr, Collection<String> commands) {
        Set<String> matches = Sets.newHashSet();

        String query = queryStr.replaceAll("\\*", ".*?");
        query = query.replaceFirst("\\b([a-z]+)", "$1[a-z]*");
        String re = "\\b(" + query.replaceAll("([A-Z][^A-Z]*)", "$1[^A-Z]*") + ".*?)\\b";

        Pattern regex = Pattern.compile(re);

        for (String cmd : commands) {
            Matcher m = regex.matcher(cmd);

            if (m.find()) {
                matches.add(m.group());
            } 
        }
        
        return matches;
    }
}

/*
 * Copyright 2015 MovingBlocks
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

package org.terasology.persistence;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.terasology.module.sandbox.API;

/**
 * A simple template engine that replaces <code>${text}<code> expressions
 * based on a given text mapping function.
 */
@API
public class TemplateEngineImpl implements TemplateEngine {

    /**
     * The unescaped pattern is <code>${[^}]+})}</code>. Searches for <code>${text}</code> expressions.
     */
    private static final Pattern ID_PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");

    private final Function<String, String> mapping;

    public TemplateEngineImpl(Function<String, String> mapping) {
        this.mapping = mapping;
    }

    @Override
    public String transform(String text) {
        int cursor = 0;
        Matcher m = ID_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            sb.append(text, cursor, m.start());
            String id = m.group(1);
            String replacement = mapping.apply(id);
            if (replacement != null) {
                sb.append(replacement);
            }
            cursor = m.end();
        }

        sb.append(text, cursor, text.length());
        return sb.toString();
    }
}

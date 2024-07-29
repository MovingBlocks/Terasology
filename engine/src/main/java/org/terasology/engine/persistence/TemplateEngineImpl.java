// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.terasology.context.annotation.API;

/**
 * A simple template engine that replaces <code>${text}</code> expressions
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

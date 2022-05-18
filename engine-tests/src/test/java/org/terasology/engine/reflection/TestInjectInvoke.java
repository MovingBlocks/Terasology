// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.reflection;

import org.junit.jupiter.api.Test;
import org.terasology.engine.context.internal.ContextImpl;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import static com.google.common.truth.Truth.assertThat;

public class TestInjectInvoke {
    Integer red(String s) {
        return s.length();
    }

    URI green(File file) {
        return file.toURI();
    }

    String yellow(Properties p, String s) {
        return p.getProperty("hello") + s;
    }

    InvokingContext makeContext() {
        InvokingContext context = new InvokingContext(new ContextImpl());
        context.put(String.class, "12345678");
        context.put(File.class, new File("/dev/null"));
        Properties p = new Properties();
        p.put("hello", "zero");
        context.put(Properties.class, p);
        return context;
    }

    @Test
    void canSupplyArgumentFromContextUsingSerialization() throws URISyntaxException {
        var context = makeContext();
        assertThat(context.invoke(this::red)).isEqualTo(8);
        assertThat(context.invoke(this::green)).isEqualTo(new URI("file:/dev/null"));
    }

    @Test
    void canSupplyTwoArgumentsFromContextUsingSerialization() {
        var context = makeContext();
        assertThat(context.invoke(this::yellow)).isEqualTo("zero12345678");
    }

}

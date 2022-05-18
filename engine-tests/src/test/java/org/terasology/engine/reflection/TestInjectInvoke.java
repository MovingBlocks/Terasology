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
import static org.terasology.engine.reflection.InvokingHelpers.argType;

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
    void canGetTypeOfFunctionArgument() {
        assertThat(argType(this::red)).isAssignableTo(String.class);
        assertThat(argType(this::green)).isAssignableTo(File.class);
    }

    @Test
    void canGetTypeOfFunctionArgumentThenDoStuffIfUsingReturnedFunction() throws URISyntaxException {
        var r = TypedFunction.of(this::green);
        URI greenResult = r.apply(new File("/dev/null"));
        assertThat(greenResult).isEqualTo(new URI("file:/dev/null"));
    }

    @Test
    void canSupplyArgumentFromContext() throws URISyntaxException {
        var context = makeContext();

        var greenResult = context.invoke(this::green);
        assertThat(greenResult).isEqualTo(new URI("file:/dev/null"));
        assertThat(greenResult.getScheme()).isEqualTo("file");

        // TODO: That worked but this fails. JVM bug?
        // --debug=verboseResolution=all looks okay on the compiler side, must be runtime.
        // assertThat(context.invoke(this::green)).isEqualTo(new URI("file:/dev/null"));

        assertThat(context.invoke(this::red)).isEqualTo(8);
    }

    @Test
    void canSupplyArgumentFromContextUsingSerialization() throws URISyntaxException {
        var context = makeContext();
        assertThat(context.invokeS(this::red)).isEqualTo(8);
        assertThat(context.invokeS(this::green)).isEqualTo(new URI("file:/dev/null"));
    }

    @Test
    void canSupplyTwoArgumentsFromContextUsingSerialization() {
        var context = makeContext();
        assertThat(context.invokeS(this::yellow)).isEqualTo("zero12345678");
    }

}

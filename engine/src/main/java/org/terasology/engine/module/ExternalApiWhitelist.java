/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine.module;


import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public final class ExternalApiWhitelist {
    public static final Set<String> PACKAGES = new ImmutableSet.Builder<String>()
            // TODO: This one org.terasology entry is a hack and needs a proper fix
            .add("org.terasology.world.biomes")
            .add("org.terasology.math.geom")
            .add("java.lang")
            .add("java.lang.invoke")
            .add("java.lang.ref")
            .add("java.math")
            .add("java.util")
            .add("java.util.concurrent")
            .add("java.util.concurrent.atomic")
            .add("java.util.concurrent.locks")
            .add("java.util.function")
            .add("java.util.regex")
            .add("java.util.stream")
            .add("java.awt")
            .add("java.awt.geom")
            .add("java.awt.image")
            .add("com.google.common.annotations")
            .add("com.google.common.cache")
            .add("com.google.common.collect")
            .add("com.google.common.base")
            .add("com.google.common.math")
            .add("com.google.common.primitives")
            .add("com.google.common.util.concurrent")
            .add("gnu.trove")
            .add("gnu.trove.decorator")
            .add("gnu.trove.function")
            .add("gnu.trove.iterator")
            .add("gnu.trove.iterator.hash")
            .add("gnu.trove.list")
            .add("gnu.trove.list.array")
            .add("gnu.trove.list.linked")
            .add("gnu.trove.map")
            .add("gnu.trove.map.hash")
            .add("gnu.trove.map.custom_hash")
            .add("gnu.trove.procedure")
            .add("gnu.trove.procedure.array")
            .add("gnu.trove.queue")
            .add("gnu.trove.set")
            .add("gnu.trove.set.hash")
            .add("gnu.trove.stack")
            .add("gnu.trove.stack.array")
            .add("gnu.trove.strategy")
            .add("javax.vecmath")
            .add("com.yourkit.runtime")
            .add("com.bulletphysics.linearmath")
            .add("sun.reflect")
            .build();

    public static final Set<Class<?>> CLASSES = new ImmutableSet.Builder<Class<?>>()
            .add(com.esotericsoftware.reflectasm.MethodAccess.class)
            .add(IOException.class)
            .add(InvocationTargetException.class)
            .add(LoggerFactory.class)
            .add(Logger.class)
            .add(Reader.class)
            .add(StringReader.class)
            .add(BufferedReader.class)
            .add(java.awt.datatransfer.UnsupportedFlavorException.class)
            .add(java.nio.ByteBuffer.class)
            .add(java.nio.IntBuffer.class)
            .build();

    private ExternalApiWhitelist() {
    }
}

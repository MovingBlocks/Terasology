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


import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;

public final class ExternalApiWhitelist {
    public static final Set<Class<?>> CLASSES;
    public static final Set<String> PACKAGES;

    private ExternalApiWhitelist() {
    }

    static {
        Set<String> tmpPackages = Sets.newHashSet();
        // TODO: This one org.terasology entry is a hack and needs a proper fix
        tmpPackages.add("org.terasology.world.biomes");
        tmpPackages.add("org.terasology.math.geom");
        tmpPackages.add("java.lang");
        tmpPackages.add("java.lang.invoke");
        tmpPackages.add("java.lang.ref");
        tmpPackages.add("java.math");
        tmpPackages.add("java.util");
        tmpPackages.add("java.util.concurrent");
        tmpPackages.add("java.util.concurrent.atomic");
        tmpPackages.add("java.util.concurrent.locks");
        tmpPackages.add("java.util.function");
        tmpPackages.add("java.util.regex");
        tmpPackages.add("java.util.stream");
        tmpPackages.add("java.awt");
        tmpPackages.add("java.awt.geom");
        tmpPackages.add("java.awt.image");
        tmpPackages.add("com.google.common.annotations");
        tmpPackages.add("com.google.common.cache");
        tmpPackages.add("com.google.common.collect");
        tmpPackages.add("com.google.common.base");
        tmpPackages.add("com.google.common.math");
        tmpPackages.add("com.google.common.primitives");
        tmpPackages.add("com.google.common.util.concurrent");
        tmpPackages.add("gnu.trove");
        tmpPackages.add("gnu.trove.decorator");
        tmpPackages.add("gnu.trove.function");
        tmpPackages.add("gnu.trove.iterator");
        tmpPackages.add("gnu.trove.iterator.hash");
        tmpPackages.add("gnu.trove.list");
        tmpPackages.add("gnu.trove.list.array");
        tmpPackages.add("gnu.trove.list.linked");
        tmpPackages.add("gnu.trove.map");
        tmpPackages.add("gnu.trove.map.hash");
        tmpPackages.add("gnu.trove.map.custom_hash");
        tmpPackages.add("gnu.trove.procedure");
        tmpPackages.add("gnu.trove.procedure.array");
        tmpPackages.add("gnu.trove.queue");
        tmpPackages.add("gnu.trove.set");
        tmpPackages.add("gnu.trove.set.hash");
        tmpPackages.add("gnu.trove.stack");
        tmpPackages.add("gnu.trove.stack.array");
        tmpPackages.add("gnu.trove.strategy");
        tmpPackages.add("javax.vecmath");
        tmpPackages.add("com.yourkit.runtime");
        tmpPackages.add("com.bulletphysics.linearmath");
        tmpPackages.add("sun.reflect");
        PACKAGES = Collections.unmodifiableSet(tmpPackages);

        Set<Class<?>> tmpClasses = Sets.newHashSet();
        tmpClasses.add(com.esotericsoftware.reflectasm.MethodAccess.class);
        tmpClasses.add(IOException.class);
        tmpClasses.add(InvocationTargetException.class);
        tmpClasses.add(LoggerFactory.class);
        tmpClasses.add(Logger.class);
        tmpClasses.add(Reader.class);
        tmpClasses.add(StringReader.class);
        tmpClasses.add(BufferedReader.class);
        tmpClasses.add(java.awt.datatransfer.UnsupportedFlavorException.class);
        tmpClasses.add(java.nio.ByteBuffer.class);
        tmpClasses.add(java.nio.IntBuffer.class);
        CLASSES = Collections.unmodifiableSet(tmpClasses);
    }
}

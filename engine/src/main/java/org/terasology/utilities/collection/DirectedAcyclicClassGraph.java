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
package org.terasology.utilities.collection;

import com.google.api.client.util.Maps;

import java.util.Map;

public class DirectedAcyclicClassGraph<T> extends DirectedAcyclicGraph<T> {

    private Map<Class<?>, T> classObjectMap;

    public DirectedAcyclicClassGraph() {
        super();
        classObjectMap = Maps.newHashMap();
    }

    public <G> G get(Class<G> c) {
        return (G) classObjectMap.get(c);
    } // FIXME: is there a better way to do this?

    public boolean add(T o) {
        return this.addNode(o);
    }

    @Override
    public boolean addNode(T o) {
        if (classObjectMap.containsKey(o)) {
            return false;
        }

        classObjectMap.put(o.getClass(), o);
        return super.addNode(o);
    }
}

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
package org.terasology.config;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.terasology.naming.Name;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

/**
 * Configuration file for all the socket BS you can think of.
 */
// TODO make thread safe and stuff
public class SocketConfig {
    /**
     * All the TCP whitelists. Keyed by module ID.
     */
    private Map<Name, Hosts> tcpWhitelists = Maps.newTreeMap();

    /**
     * Retrieves the TCP whitelists.
     *
     * @return The TCP whitelists.
     */
    public Map<Name, Hosts> getTcpWhitelists() {
        return tcpWhitelists;
    }

    /**
     * POJO representing the hosts a module is allowed to access.
     */
    public static class Hosts {
        /**
         * The hosts themselves.
         */
        private Map<String, Ports> hosts = Maps.newTreeMap();

        /**
         * Retrieves the hosts.
         *
         * @return The hosts.
         */
        public Map<String, Ports> getHosts() {
            return hosts;
        }

        public static class Handler implements JsonSerializer<Hosts>, JsonDeserializer<Hosts> {

            @Override
            public Hosts deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                // We need this for the treemap.
                Map<String, Ports> map;
                try {
                    // Ugh java
                    map = context.deserialize(json, Hosts.class.getDeclaredField("hosts").getGenericType());
                } catch (Exception e) {
                    // Who cares
                    throw new JsonParseException(e);
                }
                if (map == null) {
                    return null;
                }
                Hosts h = new Hosts();
                h.hosts = map;
                return h;
            }

            @Override
            public JsonElement serialize(Hosts src, Type typeOfSrc, JsonSerializationContext context) {
                if (src == null) {
                    return JsonNull.INSTANCE;
                }
                return context.serialize(src.hosts);
            }
        }
    }

    /**
     * POJO representing the ports a module is allowed to access.
     */
    public static class Ports {
        /**
         * The ports themselves.
         */
        private TIntSet ports = new TIntHashSet();

        /**
         * Retrieves the ports.
         *
         * @return The ports.
         */
        public TIntSet getPorts() {
            return ports;
        }

        public static class Handler implements JsonSerializer<Ports>, JsonDeserializer<Ports> {
            @Override
            public Ports deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                // We need this custom deserialization (and this class) for 2 reasons:
                // 1. We need to control TIntHashSet's no_entry_value.
                // 2. We need to sanitize user input.
                int[] array = context.deserialize(json, int[].class);
                if (array == null) {
                    return null;
                }
                Arrays.stream(array).filter(x -> (x < 1 || x > 65535) && x != -1).findAny().ifPresent(x -> {
                    throw new IllegalStateException("Port must be in range 1 <= port <= 65535, or -1 for wildcard value: " + x);
                });
                TIntHashSet hashSet = new TIntHashSet(10, .5f, Integer.MIN_VALUE);
                hashSet.addAll(array);
                Ports p = new Ports();
                p.ports = hashSet;
                return p;
            }

            @Override
            public JsonElement serialize(Ports src, Type typeOfSrc, JsonSerializationContext context) {
                if (src == null) {
                    return JsonNull.INSTANCE;
                }
                // Just defer to standard TIntHashSet serialization. This is fine.
                return context.serialize(src.ports);
            }
        }
    }
}

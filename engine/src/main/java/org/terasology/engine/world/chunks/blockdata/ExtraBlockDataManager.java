// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.blockdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.context.annotation.API;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Keeps track of the extra per-block storage fields which may be registered by mods for
 * their own use. If multiple fields of the same size are registered for disjoint sets
 * of blocks, they may be made aliases of the same field, to save space.
 *
 * To register extra data fields, annotate a class with @ExtraDataSystem, and include a
 * public static method annotated with @RegisterExtraData which determines, for each block,
 * whether the field is applicable for that block. For example:
 *
 * <pre>
 * {@code @ExtraDataSystem}
 * public class ExampleExtraDataSystem {
 *     {@code @RegisterExtraData(name="exampleModule.grassNutrients", bitSize=8)}
 *     public static boolean shouldHaveNutrients(Block block) {
 *         return block.isGrass();
 *     }
 *  }
 * </pre>
 */
@API
public class ExtraBlockDataManager {
    private static final Logger logger = LoggerFactory.getLogger(ExtraBlockDataManager.class);
    private static final Map<Integer, TeraArray.Factory<? extends TeraArray>> TERA_ARRAY_FACTORIES = new HashMap<>();
    static {
        TERA_ARRAY_FACTORIES.put(4,  new TeraSparseArray4Bit.Factory());
        TERA_ARRAY_FACTORIES.put(8,  new TeraSparseArray8Bit.Factory());
        TERA_ARRAY_FACTORIES.put(16, new TeraSparseArray16Bit.Factory());
    }

    private final Map<String, Integer> slots;
    private final TeraArray.Factory[] slotFactories;

    /**
     * Construct a trivial instance for testing purposes: don't add any data-fields.
     */
    public ExtraBlockDataManager() {
        slots = new HashMap<>();
        slotFactories = new TeraArray.Factory[0];
    }

    /**
     * Set extra-data fields based on the modules available through the context.
     */
    public ExtraBlockDataManager(Context context) {
        Map<Integer, Map<String, Set<Block>>> fieldss = getFieldsFromAnnotations(context);

        // Work out which fields don't overlap and can be aliased together.
        slots = new HashMap<>();
        ArrayList<TeraArray.Factory<?>> tempSlotTypes = new ArrayList<>();
        fieldss.forEach((size, fields) -> {
            Graph disjointnessGraph = getDisjointnessGraph(fields);
            ArrayList<ArrayList<String>> cliques = findCliqueCover(disjointnessGraph);
            for (ArrayList<String> clique : cliques) {
                for (String label : clique) {
                    slots.put(label, tempSlotTypes.size());
                }
                tempSlotTypes.add(TERA_ARRAY_FACTORIES.get(size));
            }
        });
        slotFactories = tempSlotTypes.toArray(new TeraArray.Factory<?>[0]);

        StringBuilder loggingOutput = new StringBuilder("Extra data slots registered:");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : slots.entrySet()) {
            loggingOutput.append(first ? " " : ", ").append(entry.getKey()).append(" -> ").append(entry.getValue());
            first = false;
        }
        logger.info("{}", loggingOutput);
    }

    // Find requests for extensions and which blocks they apply to.
    private Map<Integer, Map<String, Set<Block>>> getFieldsFromAnnotations(Context context) {
        ModuleEnvironment environment = context.get(ModuleManager.class).getEnvironment();
        Collection<Block> blocks = context.get(BlockManager.class).listRegisteredBlocks();

        Map<Integer, Map<String, Set<Block>>> fieldss = new HashMap<>();
        TERA_ARRAY_FACTORIES.forEach((size, fac) -> fieldss.put(size, new HashMap<>()));

        for (Class<?> type : environment.getTypesAnnotatedWith(ExtraDataSystem.class)) {
            for (Method method : type.getMethods()) {
                RegisterExtraData registerAnnotation = method.getAnnotation(RegisterExtraData.class);
                if (registerAnnotation != null) {
                    String errorType = validRegistrationMethod(method, registerAnnotation);
                    if (errorType != null) {
                        logger.atError().log("Unable to register extra block data: {} for {}.{}: should be \"public static"
                                        + " boolean {}(Block block)\", and bitSize should be 4, 8 or 16.",
                                errorType, type.getName(), method.getName(), method.getName());
                        continue;
                    }
                    method.setAccessible(true);
                    Set<Block> includedBlocks = new HashSet<>();
                    for (Block block : blocks) {
                        try {
                            if ((boolean) method.invoke(null, block)) {
                                includedBlocks.add(block);
                            }
                        } catch (IllegalAccessException e) {
                            // This should not get to this point.
                            throw new RuntimeException("Incorrect access modifier on register extra data method", e);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    fieldss.get(registerAnnotation.bitSize()).put(registerAnnotation.name(), includedBlocks);
                }
            }
        }
        return fieldss;
    }

    private static String validRegistrationMethod(Method method, RegisterExtraData annotation) {
        Class<?>[] argumentTypes = method.getParameterTypes();
        return method.getReturnType() != boolean.class ? "incorrect return type"
               : !TERA_ARRAY_FACTORIES.containsKey(annotation.bitSize()) ? "invalid bitSize"
               : !Modifier.isStatic(method.getModifiers()) ? "method not static"
               : argumentTypes.length != 1 ? "arguments list has wrong length"
               : argumentTypes[0] != Block.class ? "incorrect argument type"
               : null;
    }

    private static Graph getDisjointnessGraph(Map<String, Set<Block>> fields) {
        Graph graph = new Graph(fields.keySet().toArray(new String[0]));
        fields.forEach((name0, blockSet0) ->
            fields.forEach((name1, blockSet1) -> {
                if (name0.compareTo(name1) < 0 && Collections.disjoint(blockSet0, blockSet1)) {
                    graph.addEdge(name0, name1);
                }
            })
        );
        return graph;
    }

    //This is exponential time, but the problem is known to be NP-hard in general and large cases are unlikely to come up.
    /**
     * Find the smallest-possible number of cliques that contain all of the vertices of the graph.
     * Used to determine how to most efficiently assign aliases of requested extra-data fields.
     */
    private static ArrayList<ArrayList<String>> findCliqueCover(Graph graph) {
        return findCliqueCover(graph, Integer.MAX_VALUE, "");
    }

    private static ArrayList<ArrayList<String>> findCliqueCover(Graph graph, int bestSize, String tabs) {
        verboseLog(tabs + "findCliqueCover up to " + bestSize + ", " + graph.toString());
        for (int i = 0; i < graph.size(); i++) {
            if (i >= bestSize - 1) {
                verboseLog(tabs + "giving up");
                return null;
            }
            String v0 = graph.getVert(i);
            if (!graph.getEdges(v0).isEmpty()) {
                verboseLog(tabs + "Selected vertex " + v0);
                String v1 = graph.getEdges(v0).iterator().next();
                ArrayList<ArrayList<String>> bestCover0 = findCliqueCover(graph.ntract(v0, v1), bestSize, tabs + "----");
                int bestSize0 = bestCover0 == null ? bestSize : bestCover0.size();
                graph.removeEdge(v0, v1);
                ArrayList<ArrayList<String>> bestCover1 = findCliqueCover(graph, bestSize0, tabs + "    ");
                graph.addEdge(v0, v1);
                if (bestCover1 != null) {
                    return bestCover1;
                } else {
                    if (bestCover0 != null) {
                        bestCover0.get(i).add(v1);
                    }
                    return bestCover0;
                }
            }
        }
        verboseLog(tabs + "done, " + graph.size());
        ArrayList<ArrayList<String>> bestCover = new ArrayList<>();
        for (int i = 0; i < graph.size(); i++) {
            ArrayList<String> singleton = new ArrayList<>();
            singleton.add(graph.getVert(i));
            bestCover.add(singleton);
        }
        return bestCover;
    }

    // Log something, but only when this class is being tested.
    private static void verboseLog(String string) {
        //logger.info(string);
    }

    /**
     * Get the numerical index associated with the extra data field name.
     * This numerical index is needed for most extra-data access methods.
     */
    public int getSlotNumber(String name) {
        Integer index = slots.get(name);
        if (index == null) {
            throw new IllegalArgumentException("Extra-data name not registered: " + name);
        }
        return index;
    }

    public TeraArray[] makeDataArrays(int sizeX, int sizeY, int sizeZ) {
        TeraArray[] extraData = new TeraArray[slotFactories.length];
        for (int i = 0; i < extraData.length; i++) {
            extraData[i] = slotFactories[i].create(sizeX, sizeY, sizeZ);
        }
        return extraData;
    }

    /**
     * Undirected graphs with string-labelled vertices.
     * Used to represent the overlaps between requested extra-data fields.
     */
    private static class Graph {
        private final String[] verts;
        private final Map<String, Set<String>> edges;

        private Graph(String[] verts, Map<String, Set<String>> edges) {
            this.verts = verts;
            this.edges = edges;
        }

        Graph(String[] verts) {
            this.verts = verts.clone();
            this.edges = new HashMap<>();
            for (String vert : verts) {
                edges.put(vert, new HashSet<>());
            }
        }

        public int size() {
            return verts.length;
        }

        public String getVert(int i) {
            return verts[i];
        }

        public Set<String> getEdges(String v) {
            return Collections.unmodifiableSet(edges.get(v));
        }

        public Graph addEdge(String s0, String s1) {
            edges.get(s0).add(s1);
            edges.get(s1).add(s0);
            return this;
        }

        public Graph removeEdge(String s0, String s1) {
            edges.get(s0).remove(s1);
            edges.get(s1).remove(s0);
            return this;
        }

        // Creates a new graph containing the complement of the contraction of the complement.
        public Graph ntract(String s0, String s1) {
            int v1 = -1;
            for (int i = 0; i < verts.length; i++) {
                if (verts[i].equals(s1)) {
                    v1 = i;
                }
            }
            String[] newVerts = new String[verts.length - 1];
            System.arraycopy(verts, 0, newVerts, 0, v1);
            System.arraycopy(verts, v1 + 1, newVerts, v1, verts.length - v1 - 1);
            Map<String, Set<String>> newEdges = new HashMap<>();
            for (String s : verts) {
                newEdges.put(s, new HashSet<>(edges.get(s)));
            }
            newEdges.remove(s1);
            Set<String> e0 = newEdges.get(s0);
            Set<String> e1 = edges.get(s1);
            for (String s2 : verts) {
                if (e0.contains(s2) && !e1.contains(s2)) {
                    e0.remove(s2);
                    newEdges.get(s2).remove(s0);
                }
                if (e1.contains(s2)) {
                    newEdges.get(s2).remove(s1);
                }
            }
            return new Graph(newVerts, newEdges);
        }

        public String toString() {
            StringBuilder result = new StringBuilder("Graph:");
            for (String vert : verts) {
                result.append(" (").append(vert).append(" ->");
                for (String v : edges.get(vert)) {
                    result.append(" ").append(v);
                }
                result.append(")");
            }
            return result.toString();
        }
    }
}

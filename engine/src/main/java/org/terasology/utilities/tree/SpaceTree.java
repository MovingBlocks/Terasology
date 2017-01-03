/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.utilities.tree;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;

/**
 * A data structure that allows to add, remove and find nearest nodes in an N-dimensional space.
 * This can be used to locate entities (or block entities) nearest to player of a specific type, provided that system
 * using it keeps track of all loaded &amp; active entities of this specific type.
 * <br><br>
 * For three dimensions this is an octree and for two dimensions this is quadtree implementations.
 * <br><br>
 * It is possible to provide a DistanceFunction in the constructor that will be used instead of EuclideanDistanceFunction.
 * This allows to prefer a specific axis (i.e. objects above/below have higher priority than front/back or sides) or
 * it might have some other applications. Please note, that if it is used, the distance returned in an "Entry" is
 * according to the DistanceFunction.
 *
 * @param <T> The type of object stored as a value in this SpaceTree.
 */
public class SpaceTree<T> extends AbstractDimensionalMap<T> {
    private static final int DEFAULT_BUCKET_SIZE = 24;
    private static final DistanceFunction DEFAULT_DISTANCE_FUNCTION = new EuclideanDistanceFunction();

    private final int bucketSize;
    private final int dimensions;
    private final int subNodeCount;
    private final DistanceFunction distanceFunction;

    private Node rootNode;

    public SpaceTree(int dimensions) {
        this(dimensions, DEFAULT_BUCKET_SIZE, DEFAULT_DISTANCE_FUNCTION);
    }

    public SpaceTree(int dimensions, DistanceFunction distanceFunction) {
        this(dimensions, DEFAULT_BUCKET_SIZE, distanceFunction);
    }

    public SpaceTree(int dimensions, int bucketSize) {
        this(dimensions, bucketSize, DEFAULT_DISTANCE_FUNCTION);
    }

    public SpaceTree(int dimensions, int bucketSize, DistanceFunction distanceFunction) {
        this.dimensions = dimensions;
        this.bucketSize = bucketSize;
        this.distanceFunction = distanceFunction;

        int subNodes = 2;
        for (int i = 1; i < dimensions; i++) {
            subNodes *= 2;
        }
        subNodeCount = subNodes;
    }

    @Override
    public T add(float[] position, T value) {
        validatePosition(position);
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        if (rootNode == null) {
            float[] min = new float[dimensions];
            float[] max = new float[dimensions];
            for (int i = 0; i < dimensions; i++) {
                min[i] = Float.MIN_VALUE;
                max[i] = Float.MAX_VALUE;
            }
            rootNode = createNewNode(position, min, max, value);
            return null;
        } else {
            return addToNode(position, rootNode, value);
        }
    }

    @Override
    public T remove(float[] position) {
        validatePosition(position);

        if (rootNode == null) {
            return null;
        } else {
            if (rootNode.center != null) {
                // This is not a leaf
                int subNodeIndex = getSubNodeIndex(position, rootNode.center);
                if (subNodeIndex == -1) {
                    Node oldRootNode = rootNode;
                    rootNode = null;

                    for (Node subNode : oldRootNode.subNodes) {
                        addAllFromNode(subNode, null);
                    }

                    return oldRootNode.centerValue;
                } else {
                    return removeFromSubNodeOfNode(position, rootNode, subNodeIndex);
                }
            } else {
                // This is a leaf so need to check bucket
                for (NodeEntry<T> nodeEntry : rootNode.nodeBucket) {
                    if (distanceFunction.getDistance(nodeEntry.position, position) == 0) {
                        rootNode.nodeBucket.remove(nodeEntry);
                        if (rootNode.nodeBucket.size() == 0) {
                            rootNode = null;
                        }

                        return nodeEntry.value;
                    }
                }

                // It was not found in this leaf's bucket
                return null;
            }
        }
    }

    @Override
    public Collection<Entry<T>> findNearest(float[] position, int count, float within) {
        validatePosition(position);
        if (count < 1) {
            throw new IllegalArgumentException("Count cannot be smaller than 1");
        }
        if (within < 0) {
            throw new IllegalArgumentException("Within cannot be smaller than 0");
        }

        if (rootNode == null) {
            return Collections.emptyList();
        } else {
            TreeSearch<T> treeSearch = new TreeSearch<>(within, count);
            executeSearchInNode(position, rootNode, treeSearch);
            return Collections.unmodifiableCollection(treeSearch.results.values());
        }
    }

    private void executeSearchInNode(float[] position, Node node, TreeSearch<T> treeSearch) {
        if (node.center != null) {
            // This is not a leaf
            float distance = distanceFunction.getDistance(position, node.center);
            if (distance <= treeSearch.maxDistance) {
                treeSearch.addEntry(new Entry<>(distance, node.centerValue));
            }

            for (Node subNode : node.subNodes) {
                if (subNode != null) {
                    if (distanceFunction.getPointRegionDistance(position, subNode.minValues, subNode.maxValues) <= treeSearch.maxDistance) {
                        executeSearchInNode(position, subNode, treeSearch);
                    }
                }
            }
        } else {
            // This is a leaf so need to check bucket
            for (NodeEntry<T> nodeEntry : node.nodeBucket) {
                float distance = distanceFunction.getDistance(nodeEntry.position, position);
                if (distance <= treeSearch.maxDistance) {
                    treeSearch.addEntry(new Entry<>(distance, nodeEntry.value));
                }
            }
        }
    }

    private T removeFromSubNodeOfNode(float[] position, Node node, int subNodeIndex) {
        Node processedNode = node;
        int processedSubNodeIndex = subNodeIndex;
        while (true) {
            Node subNode = processedNode.subNodes[processedSubNodeIndex];
            if (subNode == null) {
                return null;
            } else {
                if (subNode.center != null) {
                    // This is not a leaf
                    int subSubNodeIndex = getSubNodeIndex(position, subNode.center);
                    if (subSubNodeIndex == -1) {
                        processedNode.subNodes[processedSubNodeIndex] = null;

                        for (Node subSubNode : subNode.subNodes) {
                            addAllFromNode(subSubNode, processedNode);
                        }

                        return subNode.centerValue;
                    } else {
                        processedNode = subNode;
                        processedSubNodeIndex = subSubNodeIndex;
                    }
                } else {
                    // It is a leaf so need to check bucket
                    for (NodeEntry<T> nodeEntry : subNode.nodeBucket) {
                        if (distanceFunction.getDistance(nodeEntry.position, position) == 0) {
                            subNode.nodeBucket.remove(nodeEntry);
                            if (subNode.nodeBucket.size() == 0) {
                                processedNode.subNodes[processedSubNodeIndex] = null;
                            }

                            return nodeEntry.value;
                        }
                    }
                }
            }
        }
    }

    private void addAllFromNode(Node nodeToAddFrom, Node nodeToAddTo) {
        if (nodeToAddFrom != null) {
            if (nodeToAddTo == null) {
                add(nodeToAddFrom.center, nodeToAddFrom.centerValue);
            } else {
                addToNode(nodeToAddFrom.center, nodeToAddTo, nodeToAddFrom.centerValue);
            }
            for (Node subNode : nodeToAddFrom.subNodes) {
                addAllFromNode(subNode, nodeToAddTo);
            }
        }
    }

    private T addToNode(float[] position, Node node, T value) {
        Node processedNode = node;
        while (true) {
            if (processedNode.center != null) {
                // This is not a leaf
                int subNodeIndex = getSubNodeIndex(position, processedNode.center);
                if (subNodeIndex == -1) {
                    T oldValue = processedNode.centerValue;
                    processedNode.centerValue = value;
                    return oldValue;
                } else {
                    Node subNode = processedNode.subNodes[subNodeIndex];
                    if (subNode == null) {
                        float[] min = new float[dimensions];
                        float[] max = new float[dimensions];
                        for (int i = 0; i < dimensions; i++) {
                            if (position[i] > processedNode.center[i]) {
                                min[i] = processedNode.center[i];
                                max[i] = processedNode.maxValues[i];
                            } else {
                                min[i] = processedNode.minValues[i];
                                max[i] = processedNode.center[i];
                            }
                        }
                        processedNode.subNodes[subNodeIndex] = createNewNode(position, min, max, value);
                        return null;
                    } else {
                        processedNode = subNode;
                    }
                }
            } else {
                // This is a leaf, so need to check bucket

                // First check if the bucket already contains value for this position
                for (NodeEntry<T> nodeEntry : processedNode.nodeBucket) {
                    if (distanceFunction.getDistance(nodeEntry.position, position) == 0) {
                        processedNode.nodeBucket.remove(nodeEntry);
                        processedNode.nodeBucket.add(new NodeEntry<>(position, value));
                        return nodeEntry.value;
                    }
                }

                processedNode.nodeBucket.add(new NodeEntry<>(position, value));
                if (processedNode.nodeBucket.size() > bucketSize) {
                    processedNode.splitNode();
                }

                return null;
            }
        }
    }

    private int getSubNodeIndex(float[] position, float[] center) {
        int index = 0;
        int increment = 1;
        for (int i = 0; i < dimensions; i++) {
            if (position[i] > center[i]) {
                index += increment;
            }
            increment *= 2;
        }

        if (index == 0) {
            if (distanceFunction.getDistance(position, center) == 0) {
                return -1;
            }
        }
        return index;
    }

    private Node createNewNode(float[] position, float[] min, float[] max, T value) {
        float[] positionCopy = new float[dimensions];
        System.arraycopy(position, 0, positionCopy, 0, dimensions);
        return new Node(positionCopy, value, min, max);
    }

    private void validatePosition(float[] position) {
        if (position == null || position.length != dimensions) {
            throw new IllegalArgumentException("Invalid position, either null or invalid number of dimensions");
        }
    }

    private static final class TreeSearch<T> {
        private float maxDistance;
        private int maxCapacity;
        private TreeMultimap<Float, Entry<T>> results = TreeMultimap.create(
                new Comparator<Float>() {
                    @Override
                    public int compare(Float o1, Float o2) {
                        return o1.compareTo(o2);
                    }
                }, Ordering.arbitrary());

        private TreeSearch(float maxDistance, int maxCapacity) {
            this.maxDistance = maxDistance;
            this.maxCapacity = maxCapacity;
        }

        public void addEntry(Entry<T> entry) {
            results.put(entry.distance, entry);
            int size = results.size();
            if (size > maxCapacity) {
                Float maxDistanceInResults = results.keySet().last();
                NavigableSet<Entry<T>> entriesAtThisDistance = results.get(maxDistanceInResults);
                entriesAtThisDistance.pollLast();
                if (entriesAtThisDistance.size() == 0) {
                    results.removeAll(maxDistanceInResults);
                }
                maxDistance = results.keySet().last();
            } else if (size == maxCapacity) {
                maxDistance = results.keySet().last();
            }
        }
    }

    private static final class NodeEntry<T> {
        private float[] position;
        private T value;

        private NodeEntry(float[] position, T value) {
            this.position = position;
            this.value = value;
        }
    }

    private final class Node {
        private Set<NodeEntry<T>> nodeBucket;
        private float[] center;
        private T centerValue;
        private float[] minValues;
        private float[] maxValues;
        private Node[] subNodes;

        private Node(float[] position, T value, float[] minValues, float[] maxValues) {
            nodeBucket = new HashSet<>();
            nodeBucket.add(new NodeEntry<>(position, value));
            this.minValues = minValues;
            this.maxValues = maxValues;
        }

        public void splitNode() {
            Iterator<NodeEntry<T>> iterator = nodeBucket.iterator();
            NodeEntry<T> newCenter = iterator.next();
            center = newCenter.position;
            centerValue = newCenter.value;

            subNodes = new SpaceTree.Node[subNodeCount];

            while (iterator.hasNext()) {
                NodeEntry<T> nodeEntry = iterator.next();
                int subNodeIndex = getSubNodeIndex(nodeEntry.position, center);
                if (subNodes[subNodeIndex] == null) {
                    float[] min = new float[dimensions];
                    float[] max = new float[dimensions];
                    for (int i = 0; i < dimensions; i++) {
                        if (nodeEntry.position[i] > center[i]) {
                            min[i] = center[i];
                            max[i] = maxValues[i];
                        } else {
                            min[i] = minValues[i];
                            max[i] = center[i];
                        }
                    }

                    subNodes[subNodeIndex] = new Node(nodeEntry.position, nodeEntry.value, min, max);
                } else {
                    subNodes[subNodeIndex].nodeBucket.add(nodeEntry);
                }
            }
            nodeBucket = null;
        }
    }
}

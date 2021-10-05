// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.blockdata;

import com.google.common.base.Preconditions;
import org.terasology.engine.world.chunks.deflate.TeraVisitingDeflator;

import java.nio.ByteBuffer;

/**
 * <p>Dumb simple Octo tree implementation for TeraArray</p>
 * <p>Theoretical complexity</p>
 * <p>read: O(1) - O(log(n))</p>
 * <p>write: O(1) - O(log(n))+ (?)</p>
 * <p/>
 * Max size 64x64x64 (or 256x256x256 if you made ubyte ;) )
 */
public class TeraOcTree extends TeraArray {

    private byte size;
    private Node root;

    public TeraOcTree(byte size) {
        super(size, size, size, false);
        this.size = size;
        root = new Node(size);
    }

    @Override
    protected void initialize() {
    }

    @Override
    public boolean isSparse() {
        return true;
    }

    @Override
    public TeraArray copy() {
        return this;
    }

    @Override
    public TeraArray deflate(TeraVisitingDeflator deflator) {
        return null;
    }

    @Override
    public int getEstimatedMemoryConsumptionInBytes() {
        return 0;
    }

    @Override
    public int getElementSizeInBits() {
        return 32;
    }

    @Override
    public int get(int x, int y, int z) {
        Preconditions.checkArgument(x < size, "X should be less then {}", size);
        Preconditions.checkArgument(y < size, "Y should be less then {}", size);
        Preconditions.checkArgument(z < size, "Z should be less then {}", size);
        return root.get((byte) x, (byte) y, (byte) z);
    }

    @Override
    public int set(int x, int y, int z, int value) {
        Preconditions.checkArgument(x < size, "X should be less then {}", size);
        Preconditions.checkArgument(y < size, "Y should be less then {}", size);
        Preconditions.checkArgument(z < size, "Z should be less then {}", size);
        return root.set((byte) x, (byte) y, (byte) z, value);
    }

    @Override
    public boolean set(int x, int y, int z, int value, int expected) {
        Preconditions.checkArgument(x < size, "X should be less then {}", size);
        Preconditions.checkArgument(y < size, "Y should be less then {}", size);
        Preconditions.checkArgument(z < size, "Z should be less then {}", size);
        return set(x, y, z, value) == expected;
    }


    static final class Node {
        private final byte size;
        private final byte half;

        private Node[] children;
        private int value = 0;

        public Node(byte size) {
            this.size = size;
            this.half = (byte) (size / 2);
        }

        int get(byte x, byte y, byte z) {
            if (children == null) {
                return value;
            }
            int pos = pos(x, y, z);
            if (children[pos] == null) {
                return value;
            }
            return children[pos].get((byte) (x % half), (byte) (y % half), (byte) (z % half));
        }

        int set(byte x, byte y, byte z, int value) {
            if (size == 1) {
                int oldVal = this.value;
                this.value = value;
                return oldVal;
            }

            if (children == null) {
                if (value == this.value) {
                    return value;
                } else {
                    children = new Node[8];
                    int pos = pos(x, y, z);
                    children[pos] = new Node(half);
                    return children[pos].set((byte) (x % half), (byte) (y % half), (byte) (z % half), value);
                }
            } else {
                int pos = pos(x, y, z);
                Node child = children[pos];
                if (child == null) {
                    if (value == this.value) {
                        return value;
                    } else {
                        child = new Node(half);
                        children[pos] = child;
                        int oldValue = children[pos].set((byte) (x % half), (byte) (y % half), (byte) (z % half), value);
                        tryToDeflate(value, pos, child);
                        return oldValue;
                    }
                } else {
                    int oldVal = child.set((byte) (x % half), (byte) (y % half), (byte) (z % half), value);
                    tryToDeflate(value, pos, child);
                    return oldVal;
                }
            }
        }

        private void tryToDeflate(int value, int pos, Node child) {
            if (child.children == null) {
                if (child.value == this.value) {
                    children[pos] = null;
                    boolean empty = true;
                    for (int i = 0; i < 8; i++) {
                        if (children[i] != null) {
                            empty = false;
                            break;
                        }
                    }
                    if (empty) {
                        children = null;
                    }
                } else {
                    boolean same = true;
                    for (int i = 0; i < 8; i++) {
                        if (children[i] == null || children[i].children == null && children[i].value != value) {
                            same = false;
                            break;
                        }
                    }
                    if (same) {
                        this.value = value;
                        this.children = null;
                    }
                }
            }
        }

        private int pos(byte x, byte y, byte z) {
            int pos = 0;
            if (x >= half) {
                pos |= 1 << 2;
            }
            if (y >= half) {
                pos |= 1 << 1;
            }
            if (z >= half) {
                pos |= 1;
            }
            return pos;
        }
    }

    public static class SerializationHandler implements TeraArray.SerializationHandler<TeraOcTree> {

        @Override
        public int computeMinimumBufferSize(TeraOcTree array) {
            int size = 1; // first byte - size
            return computeNodeBufferSize(array.root) + size;
        }

        public int computeNodeBufferSize(Node node) {
            int nodeSize = 0;
            nodeSize += 4; // value
            nodeSize += 1; // mask
            if (node.children != null) {
                for (Node childNode : node.children) {
                    if (childNode != null) {
                        nodeSize += computeNodeBufferSize(childNode);
                    }
                }
            }
            return nodeSize;
        }

        @Override
        public ByteBuffer serialize(TeraOcTree array) {
            return serialize(array, ByteBuffer.allocateDirect(computeMinimumBufferSize(array)));
        }

        @Override
        public ByteBuffer serialize(TeraOcTree array, ByteBuffer toBuffer) {
            toBuffer.put(array.size);
            serializeNode(array.root, toBuffer);
            return toBuffer;
        }

        private void serializeNode(Node node, ByteBuffer toBuffer) {
            toBuffer.putInt(node.value);
            byte mask = 0;
            if (node.children != null) {
                for (int i = 0; i < 8; i++) {
                    mask |= (node.children[i] != null ? 1 : 0) << i;
                }
            }
            toBuffer.put(mask);
            if (node.children != null) {
                for (int i = 0; i < 8; i++) {
                    if (node.children[i] != null) {
                        serializeNode(node.children[i], toBuffer);
                    }
                }
            }
        }

        @Override
        public TeraOcTree deserialize(ByteBuffer buffer) {
            byte size = buffer.get();
            TeraOcTree tree = new TeraOcTree(size);
            deserializeNode(buffer, tree.root);
            return tree;
        }

        public void deserializeNode(ByteBuffer buffer, Node node) {
            node.value = buffer.getInt();
            byte mask = buffer.get();
            if (mask != 0) {
                node.children = new Node[8];
                for (int i = 0; i < 8; i++) {
                    if (((mask >> i) & 1) == 1) {
                        Node childNode = new Node(node.half);
                        deserializeNode(buffer, childNode);
                    }
                }
            }
        }

        @Override
        public boolean canHandle(Class<?> clazz) {
            return TeraOcTree.class.equals(clazz);
        }
    }
}

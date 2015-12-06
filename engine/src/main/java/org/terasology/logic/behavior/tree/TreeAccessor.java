/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior.tree;

import java.util.Arrays;
import java.util.List;

/**
 * Interface to access a tree structure.
 * <br><br>
 * Using a ChainedTreeAccessor the modification made to a tree is reflected to all trees in the chain.
 *
 */
public interface TreeAccessor<N> {
    void insertChild(int index, N child);

    void setChild(int index, N child);

    N removeChild(int index);

    N getChild(int index);

    int getChildrenCount();

    int getMaxChildren();

    public static class ChainedTreeAccessor<N> implements TreeAccessor<N> {
        private List<TreeAccessor<N>> accessors;

        public ChainedTreeAccessor(TreeAccessor<N>... accessors) {
            this.accessors = Arrays.asList(accessors);
        }

        @Override
        public void insertChild(int index, N child) {
            for (TreeAccessor<N> accessor : accessors) {
                accessor.insertChild(index, child);
            }
        }

        @Override
        public void setChild(int index, N child) {
            for (TreeAccessor<N> accessor : accessors) {
                accessor.setChild(index, child);
            }
        }

        @Override
        public N removeChild(int index) {
            N result = null;
            for (TreeAccessor<N> accessor : accessors) {
                result = accessor.removeChild(index);
            }
            return result;
        }

        @Override
        public N getChild(int index) {
            return accessors.get(0).getChild(index);
        }

        @Override
        public int getChildrenCount() {
            return accessors.get(0).getChildrenCount();
        }

        @Override
        public int getMaxChildren() {
            return accessors.get(0).getChildrenCount();
        }
    }
}

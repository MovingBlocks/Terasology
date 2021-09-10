// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.nui;

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

      class ChainedTreeAccessor<N> implements TreeAccessor<N> {
        private List<TreeAccessor<N>> accessors;

        @SafeVarargs
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

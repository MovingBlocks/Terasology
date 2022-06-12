// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.testUtil;

import com.google.common.truth.Correspondence;

public final class Correspondences {
    private Correspondences() {
    }

    public static <A, E extends Class<?>> Correspondence<A, E> instanceOfExpected() {
        // This is literally the example in the documentation of Correspondence.from.
        // They could have included the implementation in the library for us to use!
        return Correspondence.from((A a, E e) -> e.isInstance(a), "is an instance of");
    }
}

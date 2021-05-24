// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.unittest.stubs;

import org.terasology.engine.entitySystem.Component;
import org.terasology.reflection.MappedContainer;

public class MappedTypeComponent implements Component {

    public InnerType val = new InnerType();
    public Number number = 2;

    @MappedContainer
    public static class InnerType {
        public static final int STATIC_VALUE = 2;
    }

}

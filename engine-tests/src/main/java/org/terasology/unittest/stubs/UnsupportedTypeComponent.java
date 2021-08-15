// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.unittest.stubs;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.systems.internal.DoNotAutoRegister;

@DoNotAutoRegister
public class UnsupportedTypeComponent implements Component {
    public UnsupportedType value;
    public UnsupportedType2 value2;
    public UnsupportedType3 value3;

    public interface UnsupportedType3 {

    }

    public static final class UnsupportedType {
        private UnsupportedType() {
        }
    }

    public abstract static class UnsupportedType2 {

    }


}

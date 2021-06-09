// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.unittest.stubs;

import org.terasology.engine.entitySystem.systems.internal.DoNotAutoRegister;
import org.terasology.gestalt.entitysystem.component.Component;

@DoNotAutoRegister
public class UnsupportedTypeComponent implements Component<UnsupportedTypeComponent> {
    public UnsupportedType value;
    public UnsupportedType2 value2;
    public UnsupportedType3 value3;

    @Override
    public void copy(UnsupportedTypeComponent other) {
        this.value = other.value;
        this.value2 = other.value2;
        this.value3 = other.value3;
    }

    public interface UnsupportedType3 {

    }

    public static final class UnsupportedType {
        private UnsupportedType() {
        }
    }

    public abstract static class UnsupportedType2 {

    }


}

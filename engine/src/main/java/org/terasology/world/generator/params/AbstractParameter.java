/*
 * Copyright 2014 MovingBlocks
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

package org.terasology.world.generator.params;

import org.terasology.rendering.nui.databinding.Binding;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public abstract class AbstractParameter<T> implements Parameter {

    private final String label;
    private final Binding<T> binding;

    protected AbstractParameter(String label, Binding<T> binding) {
        this.label = label;
        this.binding = binding;
    }
    
    /**
     * @return the binding
     */
    public Binding<T> getBinding() {
        return binding;
    }
    
    /**
     * @return the parameter label
     */
    @Override
    public String getLabel() {
        return label;
    }
}

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
 * Represents a string parameter
 * @author Martin Steiger
 */
public class StringParameter extends AbstractParameter<String> {
    private final String regExMatch;

    /**
     * @param label the human-readable label
     * @param binding the value binding
     */
    public StringParameter(String label, Binding<String> binding) {
        this(label, binding, ".*");
    }
    
    /**
     * @param label the human-readable label
     * @param binding the value binding
     * @param regExMatch the regular expression the string needs to match
     */
    public StringParameter(String label, Binding<String> binding, String regExMatch) {
        super(label, binding);
        
        this.regExMatch = regExMatch;
    }

}

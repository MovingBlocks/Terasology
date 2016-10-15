/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.dag;

import org.terasology.context.Context;
import org.terasology.registry.InjectionHelper;

/**
 * TODO: Add javadocs
 */
public class NodeFactory {

    public static final boolean DELAY_INIT = true;

    private Context context;

    public NodeFactory(Context context) {
        this.context = context;
    }

    public <T extends Node> T createInstance(Class<T> type) {
        return createInstance(type, false);
    }

    public <T extends Node> T createInstance(Class<T> type, boolean delayInitialization) {
        // Attempt constructor-based injection first
        T node = InjectionHelper.createWithConstructorInjection(type, context);
        // Then fill @In fields
        InjectionHelper.inject(node, context);
        if (!delayInitialization) {
            node.initialise();
        }
        return type.cast(node);
        // node.initialise() -must- be called externally, to have parameters as necessary
    }
}

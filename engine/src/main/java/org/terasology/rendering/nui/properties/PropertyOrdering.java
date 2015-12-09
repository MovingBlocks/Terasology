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

package org.terasology.rendering.nui.properties;

import com.google.common.collect.Ordering;

/**
 * Provides different {@link java.util.Comparator}s for {@link Property} 
 */
public final class PropertyOrdering {

    private PropertyOrdering() {
        // avoid initialization
    }
    
    public static Ordering<Property<?, ?>> byLabel() {
        return new Ordering<Property<?, ?>>() {
            @Override
            public int compare(Property<?, ?> p1, Property<?, ?> p2) {
                String o1 = p1.getLabel().getText();
                String o2 = p2.getLabel().getText();

                if (o1 == null) {
                    return (o2 == null) ? 0 : -1;
                }
        
                if (o2 == null) {
                    return 1;
                }
                
                return o1.compareTo(o2);
            }
        };
    }
}


/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.rendering.dag.gsoc;

import org.terasology.context.Context;
import org.terasology.monitoring.PerformanceMonitor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PasstroughNode {
    public class PasstroughNode extends NewAbstractNode implements PropertyChangeListener {

        public PasstroughNode(String nodeUri, Context context) {
            super(nodeUri, context);
        }
        /**
         * Execute the final post processing on the rendering of the scene obtained so far.
         *
         * It uses the data stored in multiple FBOs as input and the FINAL FBO to store its output, rendering everything to a quad.
         */
        @Override
        public void process() {
            PerformanceMonitor.startActivity("rendering/" + getUri());

            PerformanceMonitor.endActivity();
        }

        @Override
        public void propertyChange(PropertyChangeEvent event) {

        }
    }

}

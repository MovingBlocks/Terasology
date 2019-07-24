/*
 * Copyright 2019 MovingBlocks
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
import org.terasology.naming.Name;

public class DummyNode extends NewAbstractNode {


    public DummyNode(String nodeId, String nodeAka, Name providingModule, Context context) {
        super(nodeId, nodeAka, providingModule, context);
    }

    public DummyNode(String nodeId, Name providingModule, Context context) {
        super(nodeId, nodeId, providingModule, context);
    }

    @Override
    public void setDependencies(Context context) {

    }

    @Override
    public void process() {

    }
}

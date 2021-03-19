/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.registry.InjectionHelper;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.family.BlockFamily;

public class RegisterBlockFamilies  extends SingleStepLoadProcess {
    private final Context context;

    public RegisterBlockFamilies(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Registering Block Families ...";
    }

    @Override
    public boolean step() {
        BlockManager blockManager = context.get(BlockManager.class);
        for (BlockFamily blockFamily : blockManager.listRegisteredBlockFamilies()) {
            InjectionHelper.inject(blockFamily, context);
        }
        return  true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}

/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.engine.modes.loadProcesses;

import org.terasology.context.Context;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.ConsoleImpl;

public class InitialiseCommandSystem extends SingleStepLoadProcess {

    private Context context;

    public InitialiseCommandSystem(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Initialising Command System...";
    }

    @Override
    public boolean step() {
        context.put(Console.class, new ConsoleImpl(context));
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}

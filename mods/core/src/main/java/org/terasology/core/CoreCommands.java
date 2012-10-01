/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.core;

import org.terasology.logic.commands.Command;
import org.terasology.logic.commands.CommandProvider;
import org.terasology.logic.manager.MessageManager;

/**
 * @author Immortius
 */
public class CoreCommands implements CommandProvider {

    @Command(shortDescription = "A test command for mod loading")
    public void testModCommand() {
        MessageManager.getInstance().addMessage("Your mod loading works perfectly", MessageManager.EMessageScope.PRIVATE);
    }
}

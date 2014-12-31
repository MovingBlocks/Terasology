/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.console.commands.adapter;

import org.terasology.naming.Name;

/**
 * @author Limeth
 */
public class NameAdapter implements CommandParameterAdapter<Name> {
    @Override
    public Name parse(String composed) {
        return new Name(composed);
    }

    @Override
    public String compose(Name parsed) {
        return parsed.toString();
    }
}

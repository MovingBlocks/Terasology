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
package org.terasology.i18n;

import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.In;

//the translation system is only refreshed as EngineSubsystem but not when the module environment is loaded
//this system allows the translation system to load the module assets
@RegisterSystem
public class TranslationRefreshSystem extends BaseComponentSystem {

    @In
    private TranslationSystem translationSystem;

    @Override
    public void initialise() {
        translationSystem.refresh();
    }
}

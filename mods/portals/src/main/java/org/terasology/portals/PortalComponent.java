/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.portals;

import org.terasology.entitySystem.Component;
import org.terasology.model.structures.BlockSelection;

/**
 * Component for future portal stuff
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class PortalComponent implements Component {
    /** The block positions that make up the portal itself (where stuff would spawn / teleport) */
    private BlockSelection _portalBlocks;

    /** The block positions making up the frame, damage to the frame could break the portal */
    private BlockSelection _frameBlocks;
}

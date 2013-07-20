/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.network;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;

/**
 * The component that marks an entity as being a Client Entity (essentially, a player) and ties them to a
 * client info entity (for replicated information) and character entity (their body).
 *
 * @author Immortius
 */
public class ClientComponent implements Component {
    public boolean local = false;

    @Replicate
    public EntityRef clientInfo = EntityRef.NULL;

    @Replicate
    public EntityRef character = EntityRef.NULL;
}

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

package org.terasology.entitySystem;

/**
 * Primarily for internal use, is informed of all component lifecycle events for all components.
 *
 * @author Immortius
 */
public interface EntityChangeSubscriber {

    void onEntityComponentAdded(EntityRef entity, Class<? extends Component> component);

    void onEntityComponentChange(EntityRef entity, Class<? extends Component> component);

    void onEntityComponentRemoved(EntityRef entity, Class<? extends Component> component);


}

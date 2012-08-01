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
package org.terasology.entitySystem.common;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;
import org.terasology.entitySystem.pojo.PojoEntityManager;

/**
 * Null entity implementation - acts the same as an empty entity, except you cannot add anything to it.
 *
 * @author Immortius <immortius@gmail.com>
 */
public class NullEntityRef extends EntityRef {
    private static NullEntityRef instance = new NullEntityRef();

    public static NullEntityRef getInstance() {
        return instance;
    }

    private NullEntityRef() {
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        return false;
    }

    @Override
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return null;
    }

    @Override
    public <T extends Component> T addComponent(T component) {
        return null;
    }

    @Override
    public void removeComponent(Class<? extends Component> componentClass) {
    }

    @Override
    public void saveComponent(Component component) {
    }

    @Override
    public Iterable<Component> iterateComponents() {
        return NullIterator.newInstance();
    }

    @Override
    public void destroy() {
    }

    @Override
    public void send(Event event) {
    }

    @Override
    public int getId() {
        return PojoEntityManager.NULL_ID;
    }

    @Override
    public String toString() {
        return "EntityRef{" +
                "id=" + PojoEntityManager.NULL_ID +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof EntityRef) {
            return !((EntityRef) o).exists();
        }
        return o == null;
    }

    @Override
    public int hashCode() {
        return 0;
    }

}

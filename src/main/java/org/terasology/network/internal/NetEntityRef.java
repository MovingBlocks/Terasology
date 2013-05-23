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

package org.terasology.network.internal;

import org.terasology.asset.AssetUri;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.prefab.Prefab;

/**
 * @author Immortius
 */
public class NetEntityRef extends EntityRef {

    private int networkId;
    private NetworkSystemImpl networkSystem;

    public NetEntityRef(int networkId, NetworkSystemImpl system) {
        this.networkId = networkId;
        this.networkSystem = system;
    }

    private EntityRef getActualEntityRef() {
        return networkSystem.getEntity(networkId);
    }

    @Override
    public boolean exists() {
        return getActualEntityRef().exists();
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        return getActualEntityRef().hasComponent(component);
    }

    @Override
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return getActualEntityRef().getComponent(componentClass);
    }

    @Override
    public <T extends Component> T addComponent(T component) {
        return getActualEntityRef().addComponent(component);
    }

    @Override
    public void removeComponent(Class<? extends Component> componentClass) {
        getActualEntityRef().removeComponent(componentClass);
    }

    @Override
    public void saveComponent(Component component) {
        getActualEntityRef().saveComponent(component);
    }

    @Override
    public Iterable<Component> iterateComponents() {
        return getActualEntityRef().iterateComponents();
    }

    @Override
    public void destroy() {
        getActualEntityRef().destroy();
    }

    @Override
    public void send(Event event) {
        getActualEntityRef().send(event);
    }

    @Override
    public int getId() {
        return getActualEntityRef().getId();
    }

    @Override
    public boolean isPersisted() {
        return getActualEntityRef().isPersisted();
    }

    @Override
    public void setPersisted(boolean persisted) {
        getActualEntityRef().setPersisted(persisted);
    }

    @Override
    public Prefab getParentPrefab() {
        return getActualEntityRef().getParentPrefab();
    }

    @Override
    public AssetUri getPrefabURI() {
        return getActualEntityRef().getPrefabURI();
    }

    @Override
    public boolean equals(Object obj) {
        return getActualEntityRef().equals(obj);
    }

    @Override
    public int hashCode() {
        return getActualEntityRef().hashCode();
    }

    @Override
    public String toString() {
        return "NetEntityRef(netId = " + networkId + ")" + getActualEntityRef().toString();
    }
}

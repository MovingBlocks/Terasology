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
 * An implementation of EntityRef that deals with entities propagated to a client. These entities may enter and
 * leave relevance over time, and may have a different Entity id each time. NetEntityRef links to them via their
 * network id, and survives them dropping in and out of relevance.
 *
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
    public boolean isActive() {
        return getActualEntityRef().isActive();
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
    public boolean isPersistent() {
        return getActualEntityRef().isPersistent();
    }

    @Override
    public void setPersistent(boolean persistent) {
        getActualEntityRef().setPersistent(persistent);
    }

    @Override
    public boolean isAlwaysRelevant() {
        return getActualEntityRef().isAlwaysRelevant();
    }

    @Override
    public void setAlwaysRelevant(boolean alwaysRelevant) {
        getActualEntityRef().setAlwaysRelevant(alwaysRelevant);
    }

    @Override
    public EntityRef getOwner() {
        return getActualEntityRef().getOwner();
    }

    @Override
    public void setOwner(EntityRef owner) {
        getActualEntityRef().setOwner(owner);
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
        return obj == this || obj instanceof EntityRef && getActualEntityRef().equals(obj);
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

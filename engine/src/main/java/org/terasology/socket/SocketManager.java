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
package org.terasology.socket;

import org.terasology.context.Context;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.module.sandbox.ModuleClassLoader;
import org.terasology.naming.Name;
import org.terasology.registry.InjectionHelper;
import org.terasology.registry.Share;
import org.terasology.socket.internal.SocketAccessImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * TODO
 */
@RegisterSystem(RegisterMode.AUTHORITY)
@Share(SocketManager.class)
public class SocketManager extends BaseComponentSystem {
    private Map<Name, SocketAccess> socketAccessMap = new HashMap<>();

    private SocketAccess getOrCreate(Name id) {
        return socketAccessMap.computeIfAbsent(id, SocketAccessImpl::new);
    }

    public void inject(Object o) {
        // TODO refactor the whole "get module name for type" into an @API
        ClassLoader cl = o.getClass().getClassLoader();
        if (cl instanceof ModuleClassLoader) {
            ModuleClassLoader mcl = ((ModuleClassLoader) cl);
            SocketAccess access = getOrCreate(mcl.getModuleId());
            SocketInjectionContext injectionContext = new SocketInjectionContext(access);
            InjectionHelper.inject(o, injectionContext);
            if (!injectionContext.status) {
                throw new IllegalArgumentException("Attempt to inject sockets into socketless object");
            }
        }
    }

    private class SocketInjectionContext implements Context {
        private SocketAccess socketAccess;
        private boolean status = false;

        private SocketInjectionContext(SocketAccess socketAccess) {
            Objects.requireNonNull(socketAccess);
            this.socketAccess = socketAccess;
        }

        @Override
        public <T> T get(Class<? extends T> type) {
            if (type == SocketAccess.class) {
                status = true;
                return type.cast(socketAccess);
            }
            return null;
        }

        @Override
        public <T, U extends T> void put(Class<T> type, U object) {
            throw new UnsupportedOperationException("Leaked SocketInjectionContext");
        }
    }
}

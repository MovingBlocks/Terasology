/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.engine.module;

import java.awt.AWTPermission;
import java.security.AccessControlException;
import java.security.Permission;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * @author Immortius
 */
public class ModuleSecurityManager extends SecurityManager {

    private static final Logger logger = LoggerFactory.getLogger(ModuleSecurityManager.class);
    private static final Permission ADD_ALLOWED_PERMISSION = new RuntimePermission("addAllowedPermission");
    private static final Permission ADD_API_CLASS = new RuntimePermission("addAPIClass");

    private Set<Class> apiClasses = Sets.newHashSet();
    private Set<String> apiPackages = Sets.newHashSet();
    private Set<String> loadablePackages = Sets.newHashSet();
    private Set<Class<? extends Permission>> allowedPermissions = Sets.newHashSet();
    private Set<Permission> allowedInstances = Sets.newHashSet();

    private ThreadLocal<Boolean> calculatingPermission = new ThreadLocal<>();

    public ModuleSecurityManager() {
        loadablePackages.add("sun.reflect");
    }

    public void addAllowedPermission(Class<? extends Permission> allowedPermission) {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ADD_ALLOWED_PERMISSION);
        }
        allowedPermissions.add(allowedPermission);
    }

    public void addAllowedPermission(Permission allowedPermission) {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ADD_ALLOWED_PERMISSION);
        }
        allowedInstances.add(allowedPermission);
    }
    
    public void addAPIClass(Class clazz) {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ADD_API_CLASS);
        }
        apiClasses.add(clazz);
    }

    public void checkModAccess(Permission perm) {
        if (calculatingPermission.get() != null) {
            return;
        }
        if (allowedPermissions.contains(perm.getClass())) {
            return;
        }

        if (allowedInstances.contains(perm)) {
            return;
        }
        
        calculatingPermission.set(true);
        try {
            Class[] classes = getClassContext();
            for (int i = 0; i < classes.length; ++i) {
                if (apiClasses.contains(classes[i]) || (classes[i].getPackage() != null && apiPackages.contains(classes[i].getPackage().getName()))) {
                    return;
                }
                ClassLoader owningLoader = classes[i].getClassLoader();
                if (owningLoader != null && owningLoader instanceof ModuleClassLoader) {
                    if (i - 1 > 0) {
                        throw new AccessControlException(
                                String.format("Module class '%s' calling into '%s' requiring permission '%s'", classes[i].getName(), classes[i - 1].getName(), perm));
                    } else {
                        throw new AccessControlException(String.format("Module class '%s' requiring permission '%s'", classes[i].getName(), perm));
                    }
                }
            }
        } finally {
            calculatingPermission.set(null);
        }
    }

    @Override
    public void checkPermission(Permission perm) {
        checkModAccess(perm);
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        checkModAccess(perm);
    }

    public boolean checkAccess(Class type) {
        return apiClasses.contains(type) || apiPackages.contains(type.getPackage().getName()) || loadablePackages.contains(type.getPackage().getName());
    }

    public void addAPIPackage(String packageName) {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ADD_ALLOWED_PERMISSION);
        }
        apiPackages.add(packageName);
    }
}

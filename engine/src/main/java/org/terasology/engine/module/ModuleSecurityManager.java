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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Permission;
import java.util.Set;

/**
 * @author Immortius
 */
public class ModuleSecurityManager extends SecurityManager {

    private static final Logger logger = LoggerFactory.getLogger(ModuleSecurityManager.class);
    private static final Permission ADD_ALLOWED_PERMISSION = new RuntimePermission("addAllowedPermission");
    private static final Permission ADD_API_CLASS = new RuntimePermission("addAPIClass");

    private Set<Class> apiClasses = Sets.newHashSet();
    private Set<String> apiPackages = Sets.newHashSet();
    private Set<String> fullPrivilegePackages = Sets.newHashSet();

    private Set<Class<? extends Permission>> globallyAllowedPermissionsTypes = Sets.newHashSet();
    private Set<Permission> globallyAllowedPermissionsInstances = Sets.newHashSet();
    private SetMultimap<Class<? extends Permission>, Class> allowedPermissionsTypes = HashMultimap.create();
    private SetMultimap<Permission, Class> allowedPermissionInstances = HashMultimap.create();
    private SetMultimap<Class<? extends Permission>, String> allowedPackagePermissionsTypes = HashMultimap.create();
    private SetMultimap<Permission, String> allowedPackagePermissionInstances = HashMultimap.create();

    private ThreadLocal<Boolean> calculatingPermission = new ThreadLocal<>();

    public ModuleSecurityManager() {
        apiPackages.add("sun.reflect");
    }

    /**
     * Registers a global permission that all modules are granted
     *
     * @param permission
     */
    public void addAllowedPermission(Class<? extends Permission> permission) {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ADD_ALLOWED_PERMISSION);
        }
        globallyAllowedPermissionsTypes.add(permission);
    }

    /**
     * Registers a global permission that all modules are granted
     *
     * @param allowedPermission
     */
    public void addAllowedPermission(Permission allowedPermission) {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ADD_ALLOWED_PERMISSION);
        }
        globallyAllowedPermissionsInstances.add(allowedPermission);
    }

    /**
     * Registers a permission that modules are granted when working (directly or indirectly) through the given apiType
     *
     * @param apiType
     * @param allowedPermission
     */
    public void addAllowedPermission(Class apiType, Class<? extends Permission> allowedPermission) {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ADD_ALLOWED_PERMISSION);
        }
        allowedPermissionsTypes.put(allowedPermission, apiType);
    }

    /**
     * Registers a permission that modules are granted when working (directly or indirectly) through the given apiType
     *
     * @param apiType
     * @param allowedPermission
     */
    public void addAllowedPermission(Class apiType, Permission allowedPermission) {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ADD_ALLOWED_PERMISSION);
        }
        allowedPermissionInstances.put(allowedPermission, apiType);
    }

    /**
     * Registers a permission that modules are granted when working (directly or indirectly) through the given package
     *
     * @param packageName
     * @param allowedPermission
     */
    public void addAllowedPermission(String packageName, Class<? extends Permission> allowedPermission) {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ADD_ALLOWED_PERMISSION);
        }
        allowedPackagePermissionsTypes.put(allowedPermission, packageName);
    }

    /**
     * Registers a permission that modules are granted when working (directly or indirectly) through the given package
     *
     * @param packageName
     * @param allowedPermission
     */
    public void addAllowedPermission(String packageName, Permission allowedPermission) {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ADD_ALLOWED_PERMISSION);
        }
        allowedPackagePermissionInstances.put(allowedPermission, packageName);
    }

    public void addAPIClass(Class clazz) {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ADD_API_CLASS);
        }
        apiClasses.add(clazz);
    }

    /**
     * The process for this is:
     * <ol>
     * <li>If the permission is globally allowed, then permission is granted</li>
     * <li>Determine if a module is involved in the stack. If not, permission is granted</li>
     * <li>If a module is involved, determine whether it is calling through an API class that grants the necessary permission</li>
     * <li>If not, permission denied</li>
     * </ol>
     *
     * @param perm
     */
    public boolean checkModAccess(Permission perm) {

        if (calculatingPermission.get() != null) {
            return true;
        }

        if (globallyAllowedPermissionsTypes.contains(perm.getClass()) || globallyAllowedPermissionsInstances.contains(perm)) {
            return true;
        }

        calculatingPermission.set(true);

        try {
            Class[] stack = getClassContext();
            for (int i = 0; i < stack.length; ++i) {
                ClassLoader owningLoader = stack[i].getClassLoader();
                if (owningLoader != null && owningLoader instanceof ModuleClassLoader) {
                    return checkAPIPermissionsFor(perm, i, stack);
                }
            }
        } finally {
            calculatingPermission.set(null);
        }
        return false;
    }

    private boolean checkAPIPermissionsFor(Permission permission, int moduleDepth, Class[] stack) {
        Set<Class> allowed = Sets.union(allowedPermissionInstances.get(permission), allowedPermissionsTypes.get(permission.getClass()));
        Set<String> allowedPackages = Sets.union(Sets.union(allowedPackagePermissionInstances.get(permission), allowedPackagePermissionsTypes.get(permission.getClass())),
                fullPrivilegePackages);
        for (int i = moduleDepth - 1; i >= 0; i--) {
            if (allowed.contains(stack[i]) || allowedPackages.contains(stack[i].getPackage().getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void checkPermission(Permission perm) {
        if (!checkModAccess(perm)) {
            super.checkPermission(perm);
        }
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        if (!checkModAccess(perm)) {
            super.checkPermission(perm);
        }
    }

    public boolean checkAccess(Class type) {
        return apiClasses.contains(type) || apiPackages.contains(type.getPackage().getName());
    }

    public void addAPIPackage(String packageName) {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ADD_ALLOWED_PERMISSION);
        }
        apiPackages.add(packageName);
    }

    public void addFullPrivilegePackage(String packageName) {
        fullPrivilegePackages.add(packageName);
    }
}

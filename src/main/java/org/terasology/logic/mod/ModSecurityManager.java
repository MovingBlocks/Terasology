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

package org.terasology.logic.mod;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.AccessControlException;
import java.security.Permission;
import java.util.Set;
import java.util.logging.LoggingPermission;

/**
 * @author Immortius
 */
public class ModSecurityManager extends SecurityManager {
    private static final Logger logger = LoggerFactory.getLogger(ModSecurityManager.class);

    private Set<ClassLoader> modClassLoaders = Sets.newHashSet();
    private Set<Class> modAvailableClasses = Sets.newHashSet();

    public void setModClassLoader(ClassLoader classLoader) {
        modClassLoaders.add(classLoader);
    }

    public void addModAvailableClass(Class clazz) {
        checkModAccess(new RuntimePermission("Install Mod Available Class"));
        modAvailableClasses.add(clazz);
    }

    public void checkModAccess(Permission perm) {
        if (perm instanceof LoggingPermission) {
            return;
        }
        Class[] classes = getClassContext();
        for (int i = 0; i < classes.length; ++i) {
            if (modClassLoaders.contains(classes[i].getClassLoader())) {
                if (modAvailableClasses.contains(classes[i - 1])) {
                    return;
                }
                logger.debug("Mod calling into {} requiring {}", classes[i - 1].getName(), perm);
                throw new AccessControlException("Mod attempted protected action " + perm.toString());
            }
        }
    }

    public void checkPermission(Permission perm) {
        checkModAccess(perm);
    }

    public void checkPermission(Permission perm, Object context) {
        checkModAccess(perm);
    }

}

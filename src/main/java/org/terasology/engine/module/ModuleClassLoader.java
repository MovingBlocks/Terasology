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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Immortius
 */
public class ModuleClassLoader extends URLClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(ModuleClassLoader.class);
    private ModuleSecurityManager securityManager;

    public ModuleClassLoader(URL[] urls, ClassLoader parent, ModuleSecurityManager securityManager) {
        super(urls, parent);
        this.securityManager = securityManager;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = super.loadClass(name, resolve);
        if (clazz.getClassLoader() != this) {
            if (securityManager.checkAccess(clazz)) {
                return clazz;
            } else {
                logger.error("Denied access to class (not available to modules): {}", name);
                return null;
            }
        }
        return clazz;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        return super.findClass(name);
    }
}

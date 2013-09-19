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

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.Modifier;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.event.Event;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Immortius
 */
public class ModuleClassLoader extends URLClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(ModuleClassLoader.class);
    private ModuleSecurityManager securityManager;
    private ClassPool pool;

    public ModuleClassLoader(URL[] urls, ClassLoader parent, ModuleSecurityManager securityManager) {
        super(urls, parent);
        this.securityManager = securityManager;
        pool = new ClassPool(ClassPool.getDefault());
        for (URL url : urls) {
            try {
                pool.appendClassPath(url.getFile());
            } catch (NotFoundException e) {
                logger.error("Failed to process module url: {}", url);
            }
        }
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
        try {
            CtClass cc = pool.get(name);
            // Ensure empty constructor of Components and Events are not private, if they exist.
            if (needsNonPrivateConstructor(cc)) {
                try {
                    CtConstructor constructor = cc.getDeclaredConstructor(new CtClass[0]);
                    if ((constructor.getModifiers() & Modifier.PRIVATE) != 0) {
                        constructor.setModifiers(constructor.getModifiers() & ~Modifier.PRIVATE);
                    }
                } catch (NotFoundException e) {
                    // This may be fine, not necessarily expecting an empty constructor.
                }
            }

            byte[] b = cc.toBytecode();
            return defineClass(name, b, 0, b.length);
        } catch (CannotCompileException | NotFoundException | IOException e) {
            logger.error("Failed to load {}", name, e);
            throw new ClassNotFoundException("Failed to find or load class " + name, e);
        }
    }

    private boolean needsNonPrivateConstructor(CtClass cc) throws NotFoundException {
        return isSubtype(cc, Component.class) || isSubtype(cc, Event.class);

    }

    private boolean isSubtype(CtClass cc, Class parentType) throws NotFoundException {
        if (parentType.isInterface()) {
            for (CtClass parentInterface : cc.getInterfaces()) {
                if (parentInterface.getName().equals(parentInterface.getName())) {
                    return true;
                } else if (isSubtype(parentInterface, parentType)) {
                    return true;
                }
            }
            if (cc.getSuperclass() != null) {
                return isSubtype(cc, parentType);
            }
        }
        return false;
    }
}

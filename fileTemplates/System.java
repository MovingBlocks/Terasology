// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};#end
#parse("File Header.java")

import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;

/**
 * 
 */
@RegisterSystem
public class ${NAME} extends BaseComponentSystem {
    
}

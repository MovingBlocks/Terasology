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

package org.terasology.entitySystem;

import org.terasology.network.NetworkMode;

/**
 * @author Immortius
 */
public enum RegisterMode {
    ALWAYS(true, true),
    AUTHORITY(true, false),
    CLIENT(false, true);

    private boolean validWhenAuthority;
    private boolean validWhenClient;

    private RegisterMode(boolean validWhenAuthority, boolean validWhenClient) {
        this.validWhenAuthority = validWhenAuthority;
        this.validWhenClient = validWhenClient;
    }

    public boolean isValidFor(NetworkMode mode) {
        return (mode.isAuthority()) ? validWhenAuthority : validWhenClient;
    }
}

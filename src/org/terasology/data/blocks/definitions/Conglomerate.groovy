/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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

package org.terasology.data.blocks.definitions

/**
 *  Conglomerates are rocks consisting of individual clasts within a finer grained
 *  matrix that have become cemented together. Conglomerates are sedimentary rocks;
 *  Glaciers carry a lot of coarse-grained material and many glacial deposits are
 *  conglomeratic.
 */
block {
    version = 1
    shape = "cube"

    hardness = 2

    physics {
        mass = 32000
    }
}
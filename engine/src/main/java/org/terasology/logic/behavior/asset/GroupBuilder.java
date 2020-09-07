/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.logic.behavior.asset;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.terasology.gestalt.module.sandbox.API;
import org.terasology.registry.In;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class is responsible for implementing the
 * serialization functionality required by the Group
 * asset class to load group information from .group
 * asset files.
 * @see Group
 */
@API
public class GroupBuilder {

    private GroupData groupData;

    @In
    private Gson gson;

    public GroupData loadFromJson(InputStream json) {

        GroupData groupFromFile = new GroupData();
        try {
            gson = new Gson();
            groupFromFile = gson.fromJson(new InputStreamReader(json), GroupData.class);
        } catch (JsonSyntaxException e) {
            groupFromFile.setGroupLabel("ERROR - Syntax");
            e.printStackTrace();
        } catch (JsonIOException e) {
            groupFromFile.setGroupLabel("ERROR - IO");
            e.printStackTrace();
        }
        return groupFromFile;
    }

}

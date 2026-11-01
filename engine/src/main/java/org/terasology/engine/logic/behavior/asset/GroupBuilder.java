// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.asset;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.terasology.context.annotation.API;
import org.terasology.engine.registry.In;

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
@SuppressWarnings("PMD.AvoidPrintStackTrace")
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

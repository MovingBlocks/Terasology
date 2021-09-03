// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.config

trait ItemConfig {
    File directory
    String defaultBranch
    String defaultOrg
    List<String> excludes
}

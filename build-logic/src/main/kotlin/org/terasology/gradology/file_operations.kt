// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gradology

import org.gradle.api.internal.file.copy.CopySpecInternal
import org.gradle.api.tasks.Copy


/**
 * Copy, but never overwrite any existing file.
 *
 * Preserves existing files regardless of how up-to-date they are.
 *
 * Useful for providing boilerplate or defaults.
 */
abstract class CopyButNeverOverwrite : Copy() {

    override fun createRootSpec(): CopySpecInternal {
        val copySpec = super.createRootSpec()
        copySpec.eachFile {
            if (this.relativePath.getFile(destinationDir).exists()) {
                this.exclude()
            }
        }
        return copySpec;
    }
}

// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.recording;

import java.util.List;

/**
 * Stores a list of classes to record/replay.
 */
public class RecordingClasses {
    private final List<Class<?>> classesToRecord;

    public RecordingClasses(List<Class<?>> classesToRecord) {
        this.classesToRecord = classesToRecord;
    }

    public List<Class<?>> getClassesToRecord() {
        return classesToRecord;
    }
}

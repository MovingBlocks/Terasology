/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.core.debug;

import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.ChunkMath;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.rendering.nui.BaseInteractionScreen;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIDropdownScrollable;
import org.terasology.rendering.nui.widgets.UIText;
import org.terasology.world.chunks.ChunkConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This screen can be shown with the showScreen command in order to measure the performance of the block placement.
 */
public class BenchmarkScreen extends BaseInteractionScreen {
    static final int DEFAULT_ITERATION_COUNT = 200;

    private UIText textArea;
    private UIButton closeButton;
    private UIButton startStopButton;
    private UIDropdownScrollable dropdown;

    @In
    private Context context;
    private long iterationsDone;
    private double sum;
    private double min;
    private double max;
    private List<Double> sortedDurations = new ArrayList<Double>(DEFAULT_ITERATION_COUNT);
    private AbstractBenchmarkInstance runningBenchmark;
    private BenchmarkType selectedBenchmarkType = BenchmarkType.WORLD_PROVIDER_SET_BLOCK;

    @Override
    protected void initializeWithInteractionTarget(EntityRef interactionTarget) {
        // nothing to do
    }

    @Override
    public void initialise() {

        textArea = find("textArea", UIText.class);
        dropdown = find("dropdown", UIDropdownScrollable.class);
        if (dropdown != null) {

            dropdown.bindSelection(new Binding() {
                @Override
                public Object get() {
                    return selectedBenchmarkType;
                }

                @Override
                public void set(Object value) {
                    selectedBenchmarkType = (BenchmarkType) value;
                    if (runningBenchmark != null) {
                        runningBenchmark = null;
                    }
                    textArea.setText(selectedBenchmarkType.getDescription());
                }
            });
            dropdown.setOptions(Arrays.asList(BenchmarkType.values()));
        }

        closeButton = find("closeButton", UIButton.class);
        if (closeButton != null) {
            closeButton.subscribe(this::onCloseButton);
        }

        startStopButton = find("startStopButton", UIButton.class);
        if (startStopButton != null) {
            startStopButton.subscribe(this::onStartStopButton);
        }

    }

    private void onStartStopButton(UIWidget uiWidget) {
        if (runningBenchmark == null) {
            handleBenchmarkStart();
        } else {
            handleBenchmarkEnd();
        }
    }

    private void handleBenchmarkStart() {
        runningBenchmark = selectedBenchmarkType.createInstance(context);
        iterationsDone = 0;
        sum = 0;
        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;
        sortedDurations.clear();
        updateStartStopButton();
    }


    private void handleBenchmarkEnd() {
        runningBenchmark = null;
        updateStartStopButton();
    }

    private void updateStartStopButton() {
        if (runningBenchmark != null) {
            startStopButton.setText("Stop Benchmark");
        } else {
            startStopButton.setText("Start Benchmark");
        }
    }

    @Override
    public void onOpened() {
        handleBenchmarkEnd();
        if (textArea != null) {
            textArea.setText(selectedBenchmarkType.getDescription());
        }
    }

    private void onCloseButton(UIWidget uiWidget) {
        getManager().popScreen();
    }

    static Region3i getChunkRegionAbove(Vector3f location) {
        Vector3i charecterPos = new Vector3i(location);
        Vector3i chunkAboveCharacter = ChunkMath.calcChunkPos(charecterPos);
        chunkAboveCharacter.addY(1);
        Vector3i chunkRelativePos = ChunkMath.calcBlockPos(charecterPos);
        Vector3i characterChunkOriginPos = new Vector3i(charecterPos);
        characterChunkOriginPos.sub(chunkRelativePos);

        Vector3i chunkAboveOrigin = new Vector3i(characterChunkOriginPos);
        chunkAboveOrigin.addY(ChunkConstants.CHUNK_SIZE.getY());
        return ChunkConstants.CHUNK_REGION.move(chunkAboveOrigin);
    }

    @Override
    public void update(float delta) {
        if (runningBenchmark == null) {
            return;
        }
        long startNs = System.nanoTime();
        runningBenchmark.runStep();
        long endNs = System.nanoTime();
        long durationInNs = endNs - startNs;
        double durationInMs = durationInNs / 1000000.0;
        iterationsDone += 1;
        sortedDurations.add(durationInMs);
        // sort to calculate median:
        Collections.sort(sortedDurations);
        double median = sortedDurations.get(sortedDurations.size() / 2);
        sum += durationInMs;
        min = Math.min(min, durationInMs);
        max = Math.max(max, durationInMs);
        double avgMs = sum / iterationsDone;
        StringBuilder sb = new StringBuilder();
        sb.append("benchmark: ");
        sb.append(selectedBenchmarkType.getTitle());
        sb.append("\n");
        sb.append("iteration:  ");
        sb.append(Long.toString(iterationsDone));
        sb.append(" / ");
        sb.append(selectedBenchmarkType.getMaxIterations());
        sb.append("\n");
        sb.append("last duration:  ");
        sb.append(String.format("%.1f", durationInMs));
        sb.append(" ms\n");
        sb.append("min duration:  ");
        sb.append(String.format("%.1f", min));
        sb.append(" ms\n");
        sb.append("median duration:  ");
        sb.append(String.format("%.1f", median));
        sb.append(" ms\n");
        sb.append("avg duration:  ");
        sb.append(String.format("%.1f", avgMs));
        sb.append(" ms\n");
        sb.append("max duration:  ");
        sb.append(String.format("%.1f", max));
        sb.append(" ms\n");

        if (textArea != null) {
            textArea.setText(sb.toString());
        }
        if (iterationsDone >= selectedBenchmarkType.getMaxIterations()) {
            handleBenchmarkEnd();
        }
    }

}

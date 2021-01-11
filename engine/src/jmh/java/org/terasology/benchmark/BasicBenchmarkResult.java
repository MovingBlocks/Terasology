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
package org.terasology.benchmark;


/**
 * BasicBenchmarkResult extends BenchmarkResult and adds three basic columns for pretty printing.
 *
 */
public class BasicBenchmarkResult extends BenchmarkResult {

    public BasicBenchmarkResult(Benchmark benchmark) {
        super(benchmark);
        addColumn(new Column<BasicBenchmarkResult>(this, Alignment.RIGHT, "#") {
            @Override
            public String getValueInternal(int rep) {
                return String.valueOf(rep + 1);
            }
        });
        addColumn(new Column<BasicBenchmarkResult>(this, Alignment.RIGHT, "Repetitions") {
            @Override
            protected String getValueInternal(int rep) {
                return String.valueOf(owner.getRepetitions(rep));
            }
        });
        addColumn(new Column<BasicBenchmarkResult>(this, Alignment.RIGHT, "Time in ms") {
            @Override
            protected String getValueInternal(int rep) {
                return String.valueOf(owner.getRunTime(rep));
            }
        });
    }

}

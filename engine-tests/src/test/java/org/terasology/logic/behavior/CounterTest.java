/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior;

import org.junit.Test;
import org.terasology.logic.behavior.tree.CounterNode;
import org.terasology.logic.behavior.tree.Interpreter;
import org.terasology.logic.behavior.tree.RepeatNode;
import org.terasology.logic.behavior.tree.SequenceNode;

/**
 * Created by synopia on 11.01.14.
 */
public class CounterTest {
    @Test
    public void test() {
        Interpreter interpreter = new Interpreter(null);
        SequenceNode first = new SequenceNode();
        SequenceNode left = new SequenceNode();
        first.setChild(0, left);
        RepeatNode root = new RepeatNode(first);
        root.setChild(0, first);
        first.setChild(1, new CounterNode(1));

        left.setChild(0, new CounterNode(1));
        left.setChild(1, new CounterNode(1));

        interpreter.setRoot(root);
        interpreter.start();

        for (int i = 0; i < 100; i++) {
            interpreter.tick(0);
        }
    }
}

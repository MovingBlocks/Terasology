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

import org.lwjgl.util.vector.Vector3f;
import com.github.begla.blockmania.utilities.AABB;
import com.github.begla.blockmania.blocks.Block;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class AABBTest {

    private AABB _box1, _box2, _box3, _box4, _box5;
    private AABB _block1;
    private AABB _player1, _player2;

    public AABBTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        _box1 = new AABB(new Vector3f(0f, 0f, 0f), new Vector3f(0.5f, 0.5f, 0.5f));
        _box2 = new AABB(new Vector3f(0f, 0f, 0f), new Vector3f(0.1f, 0.1f, 0.1f));
        _box3 = new AABB(new Vector3f(15f, 15f, 15f), new Vector3f(0.1f, 0.1f, 0.1f));
        _box4 = new AABB(new Vector3f(0.6f, 0.0f, 0.0f), new Vector3f(0.1f, 0.1f, 0.1f));
        _box5 = new AABB(new Vector3f(0.7f, 0.0f, 0.0f), new Vector3f(0.1f, 0.1f, 0.1f));
        _block1 = Block.AABBForBlockAt(0, 0, 0);
        _player1 = new AABB(new Vector3f(0.0f, 0.6f, 0.0f), new Vector3f(0.1f, 0.1f, 0.1f));
        _player2 = new AABB(new Vector3f(0.0f, 0.7f, 0.0f), new Vector3f(0.1f, 0.1f, 0.1f));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSimpleIntersections() {
        // Box 2 is within box 1
        boolean result = _box1.isIntersecting(_box2);
        assertTrue(result);
        // Box 3 is far away from box 1
        result = _box1.isIntersecting(_box3);
        assertFalse(result);
        // Box 4 is adjacent to box 1
        result = _box1.isIntersecting(_box4);
        assertTrue(result);
        // Box 5 is not intersecting box 1
        result = _box1.isIntersecting(_box5);
        assertFalse(result);
    }
    
    @Test
    public void testBlockIntersections() {
        // Player is standing on the block
        boolean result = _player1.isIntersecting(_block1);
        assertTrue(result);
        result = _block1.isIntersecting(_player1);
        assertTrue(result);
        // Player is above the block
        result = _block1.isIntersecting(_player2);
        assertFalse(result);
        result = _player2.isIntersecting(_block1);
        assertFalse(result);
    }
}
